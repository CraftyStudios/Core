package dev.crafty.core.plugin;

import co.aikar.commands.PaperCommandManager;
import com.vdurmont.semver4j.Semver;
import dev.crafty.core.CraftyCore;
import dev.crafty.core.bridge.BridgeAutoRegistrar;
import dev.crafty.core.bukkit.CraftyLogger;
import dev.crafty.core.bukkit.EnhancedBaseCommand;
import dev.crafty.core.bukkit.SelfRegisteringListener;
import dev.crafty.core.config.ConfigurationUtils;
import dev.crafty.core.config.ConfigWatcher;
import dev.crafty.core.i18n.i18nManager;
import dev.crafty.core.scheduler.ScheduledAction;
import dev.crafty.core.scheduler.SchedulerService;
import dev.crafty.core.task.api.Task;
import dev.crafty.core.task.providers.TaskProvider;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.holoeasy.HoloEasy;
import org.holoeasy.packet.PacketImpl;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * To be extended by any crafty plugin
 * @since 1.0.0
 */
public abstract class CraftyPlugin extends JavaPlugin {

    public i18nManager i18n;
    public CraftyLogger logger;
    public Task task;

    protected PaperCommandManager commandManager;
    protected CraftyCore craftyCore;

    private ConfigWatcher configWatcher;
    private boolean configWatcherEnabled = false;

    @Override
    public void onEnable() {
        this.logger = new CraftyLogger(this);
        this.i18n = new i18nManager(this);
        this.task = new TaskProvider();

        logger.info("Starting plugin enable process...");

        saveDefaultConfig();

        // Ensure CraftyCore dependency
        Plugin corePlugin = getServer().getPluginManager().getPlugin("CraftyCore");
        if (!(corePlugin instanceof CraftyCore)) {
            logger.error("CraftyCore plugin not found! This plugin requires CraftyCore to function.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        craftyCore = (CraftyCore) corePlugin;

        // Minimum version check
        if (!checkVersion()) {
            logger.error("This plugin requires CraftyCore version %s or higher.".formatted(minimumCoreVersion()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        logger.info("Checking plugin requirements...");
        if (!checkRequirements()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Hologram support
        if (hologramSupport()) {
            logger.info("Setting up hologram support...");
            if (isPluginInstalled("ProtocolLib")) {
                HoloEasy.bind(this, PacketImpl.ProtocolLib);
                logger.info("Bound hologram support via ProtocolLib.");
            } else if (isPluginInstalled("PacketEvents")) {
                HoloEasy.bind(this, PacketImpl.PacketEvents);
                logger.info("Bound hologram support via PacketEvents.");
            } else {
                logger.error("Hologram support requires either ProtocolLib or PacketEvents to be installed.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        logger.info("Scanning and loading configs...");
        craftyCore.scanAndLoadConfigs(this);

        // Config watcher
        if (isConfigWatcherEnabled()) {
            setupConfigWatcher();
        }

        // Commands
        logger.info("Registering commands...");
        registerCommands();

        // Listeners
        logger.info("Registering listeners...");
        autoRegisterSelfRegisteringListeners();

        // Scheduler
        logger.info("Setting up scheduler...");
        setupScheduler();

        // Bridge
        BridgeAutoRegistrar registrar = new BridgeAutoRegistrar(this);
        registrar.registerAll();
        logger.info("Bridges registered.");

        logger.info("Plugin enabled successfully.");
        try {
            onCraftyEnable();
        } catch (Exception ex) {
            logger.error("Error in onCraftyEnable: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        logger.info("Disabling plugin...");
        if (configWatcher != null) {
            configWatcher.stop();
        }
        try {
            onCraftyDisable();
        } catch (Exception ex) {
            logger.error("Error in onCraftyDisable: " + ex.getMessage());
            ex.printStackTrace();
        }
        logger.info("Plugin disabled.");
    }

    protected abstract void onCraftyEnable();
    protected abstract void onCraftyDisable();
    protected abstract void onConfigReloaded();
    protected abstract String minimumCoreVersion();
    protected abstract boolean hologramSupport();
    protected List<String> getRequiredPlugins() { return List.of(); }
    protected List<ScheduledAction> getScheduledActions() { return List.of(); }

    protected void setConfigWatcherEnabled(boolean enabled) {
        configWatcherEnabled = enabled;
        if (configWatcher != null) {
            configWatcher.setEnabled(enabled);
        } else if (enabled) {
            setupConfigWatcher();
        }
    }

    protected boolean isConfigWatcherEnabled() {
        return configWatcherEnabled;
    }

    public void reloadAllConfigurations() {
        reloadConfig();
        if (craftyCore != null) {
            craftyCore.scanAndReloadConfigs(this);
            logger.info("All configurations reloaded successfully.");
        }
    }

    protected void registerAndLoadConfig(Object configObject) {
        ConfigurationUtils.registerAndLoad(configObject, this);
    }

    private void setupConfigWatcher() {
        configWatcher = ConfigWatcher.forPluginConfig(this, () -> {
            logger.info("Detected changes to config.yml, reloading configuration...");
            reloadAllConfigurations();
            onConfigReloaded();
        });
        configWatcher.start();
        logger.info("Config watcher started.");
    }

    private boolean checkVersion() {
        Plugin runningCore = getServer().getPluginManager().getPlugin(CraftyCore.INSTANCE.getName());
        if (runningCore == null) return false;
        PluginMeta coreMeta = runningCore.getPluginMeta();
        Semver runningVersion = new Semver(coreMeta.getVersion(), Semver.SemverType.LOOSE);
        Semver requiredVersion = new Semver(minimumCoreVersion(), Semver.SemverType.LOOSE);
        boolean result = runningVersion.isGreaterThanOrEqualTo(requiredVersion);
        logger.info("CraftyCore version check: found %s, required %s. %s"
                .formatted(coreMeta.getVersion(), minimumCoreVersion(), result ? "OK" : "FAILED"));
        return result;
    }

    private boolean checkRequirements() {
        List<String> required = getRequiredPlugins();
        if (required.isEmpty()) return true;
        for (String plugin : required) {
            if (!isPluginInstalled(plugin)) {
                logger.error("Required plugin '%s' not found or not enabled.".formatted(plugin));
                return false;
            }
        }
        logger.info("All required plugins found.");
        return true;
    }

    private boolean isPluginInstalled(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        boolean installed = plugin != null && plugin.isEnabled();
        if (!installed) {
            logger.warn("Plugin '%s' is not installed or not enabled.".formatted(pluginName));
        }
        return installed;
    }

    private void registerCommands() {
        try {
            commandManager = new PaperCommandManager(this);
            String mainPackage = this.getClass().getPackageName();
            Reflections reflections = new Reflections(mainPackage);
            for (Class<? extends EnhancedBaseCommand> clazz : reflections.getSubTypesOf(EnhancedBaseCommand.class)) {
                if (!Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
                    try {
                        EnhancedBaseCommand command = clazz.getDeclaredConstructor().newInstance();
                        commandManager.registerCommand(command);
                        logger.info("Auto-registered command: " + clazz.getName());
                    } catch (Exception e) {
                        logger.warn("Failed to auto-register command: " + clazz.getName() + ". Error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to scan for commands. Error: " + e.getMessage());
        }
    }

    private void autoRegisterSelfRegisteringListeners() {
        try {
            String mainPackage = this.getClass().getPackageName();
            Reflections reflections = new Reflections(mainPackage);
            for (Class<? extends SelfRegisteringListener> clazz : reflections.getSubTypesOf(SelfRegisteringListener.class)) {
                if (!Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
                    try {
                        SelfRegisteringListener listener = clazz.getDeclaredConstructor().newInstance();
                        Bukkit.getPluginManager().registerEvents(listener, this);
                        logger.info("Auto-registered listener: " + clazz.getName());
                    } catch (Exception e) {
                        logger.warn("Failed to auto-register listener: " + clazz.getName() + ". Error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to scan for listeners. Error: " + e.getMessage());
        }
    }

    private void setupScheduler() {
        SchedulerService schedulerService = new SchedulerService();
        for (ScheduledAction action : getScheduledActions()) {
            schedulerService.schedule(action);
        }
        Bukkit.getScheduler().runTaskTimer(this, schedulerService::run, 0L, 20L);
        logger.info("Scheduler started with %d actions.".formatted(getScheduledActions().size()));
    }
}