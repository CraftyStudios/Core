package dev.crafty.core.bridge;

import dev.crafty.core.CraftyCore;
import dev.crafty.core.bridge.economy.vault.VaultEconomyBridgeProvider;
import dev.crafty.core.bridge.placeholders.placeholderapi.PlaceholderApiBridgeProvider;
import dev.crafty.core.plugin.CraftyPlugin;
import lombok.AllArgsConstructor;
// Removed unused import for org.bukkit.plugin.Plugin

import java.util.List;

/**
 * Bridge registry
 * @since 1.0.0
 */
@AllArgsConstructor
public class BridgeAutoRegistrar {
    private CraftyCore plugin;

    private final List<BridgeProvider<? extends Bridge>> providers = List.of(
            // Economy
            new VaultEconomyBridgeProvider(),


            // Placeholders
            new PlaceholderApiBridgeProvider()
    );

    public void registerAll() {
        for (BridgeProvider<? extends Bridge> provider : providers) {
            plugin.logger.info("Checking availability for " + provider.getBridgeClass().getSimpleName());
            if (provider.isAvailable()) {
                registerBridge(provider);
            } else {
                plugin.logger.info(provider.getBridgeClass().getSimpleName() + " is not available.");
            }
        }
    }

    private <T extends Bridge> void registerBridge(BridgeProvider<T> provider) {
        BridgeManager.registerBridge(provider.getBridgeClass(), provider.create());
        plugin.logger.info("Loaded " + provider.getBridgeClass().getSimpleName() + " bridge.");
    }
}
