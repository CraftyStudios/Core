package dev.crafty.core.config;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Utility class for watching configuration files for changes.
 * This class provides methods for setting up and managing file watchers
 * that automatically detect changes to configuration files.
 *
 * @since 1.0.0
 */
public class ConfigWatcher {
    private final Plugin plugin;
    private final File configFile;
    private final Runnable onChangeCallback;
    
    private ScheduledExecutorService watcherExecutor;
    private WatchService watchService;
    private boolean enabled = false;
    
    /**
     * Create a new config watcher.
     *
     * @param plugin The plugin that owns the configuration file
     * @param configFile The configuration file to watch
     * @param onChangeCallback The callback to run when the file changes
     */
    public ConfigWatcher(Plugin plugin, File configFile, Runnable onChangeCallback) {
        this.plugin = plugin;
        this.configFile = configFile;
        this.onChangeCallback = onChangeCallback;
    }
    
    /**
     * Create a new config watcher for the plugin's config.yml file.
     *
     * @param plugin The plugin that owns the configuration file
     * @param onChangeCallback The callback to run when the file changes
     * @return A new config watcher
     */
    public static ConfigWatcher forPluginConfig(Plugin plugin, Runnable onChangeCallback) {
        return new ConfigWatcher(plugin, new File(plugin.getDataFolder(), "config.yml"), onChangeCallback);
    }
    
    /**
     * Start watching the configuration file for changes.
     * This method sets up a file watcher that monitors the file for changes
     * and calls the callback when changes are detected.
     *
     * @return True if the watcher was started successfully, false otherwise
     */
    public boolean start() {
        if (enabled) {
            return true; // Already started
        }
        
        try {
            // Get the directory containing the config file
            Path directory = configFile.getParentFile().toPath();
            
            // Create a watch service
            watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            
            // Start a background thread to watch for changes
            watcherExecutor = Executors.newSingleThreadScheduledExecutor();
            watcherExecutor.scheduleAtFixedRate(() -> {
                WatchKey key;
                try {
                    key = watchService.poll();
                    if (key == null) {
                        return;
                    }
                    
                    for (WatchEvent<?> event : key.pollEvents()) {
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        Path changed = pathEvent.context();
                        
                        // If the target file was modified, call the callback
                        if (changed.toString().equals(configFile.getName())) {
                            // Run the callback on the main server thread
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    plugin.getLogger().info("Detected changes to " + configFile.getName() + ", reloading configuration...");
                                    onChangeCallback.run();
                                }
                            }.runTask(plugin);
                        }
                    }
                    
                    key.reset();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error watching for config changes", e);
                }
            }, 2, 2, TimeUnit.SECONDS);
            
            enabled = true;
            plugin.getLogger().info("Config watcher set up successfully for " + configFile.getName());
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error setting up config watcher", e);
            return false;
        }
    }
    
    /**
     * Stop watching the configuration file for changes.
     * This method cleans up resources used by the file watcher.
     */
    public void stop() {
        if (!enabled) {
            return; // Already stopped
        }
        
        // Clean up resources
        if (watcherExecutor != null) {
            watcherExecutor.shutdown();
            watcherExecutor = null;
        }
        
        if (watchService != null) {
            try {
                watchService.close();
                watchService = null;
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error closing watch service", e);
            }
        }
        
        enabled = false;
    }
    
    /**
     * Check if the watcher is enabled.
     *
     * @return True if the watcher is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set whether the watcher is enabled.
     * If enabled and not already started, the watcher will be started.
     * If disabled and currently running, the watcher will be stopped.
     *
     * @param enabled True to enable the watcher, false to disable
     * @return True if the operation was successful, false otherwise
     */
    public boolean setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return true; // No change needed
        }
        
        if (enabled) {
            return start();
        } else {
            stop();
            return true;
        }
    }
}