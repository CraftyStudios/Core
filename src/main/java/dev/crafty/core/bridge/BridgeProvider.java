package dev.crafty.core.bridge;

import org.bukkit.Bukkit;

/**
 * Provider for a plugin
 * @since 1.0.0
 * @param <T> the bridge
 */
public interface BridgeProvider<T extends Bridge> {
    boolean isAvailable();
    T create();
    Class<T> getBridgeClass();

    default boolean pluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName); // wont be enabled on startup
    }
}
