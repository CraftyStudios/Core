package dev.crafty.core.bridge;

import org.bukkit.Bukkit;

/**
 * Bridges provide a unified api for accessing multiple plugins.
 * @since 1.0.0
 */
public interface Bridge {
    /**
     * Returns the unique name of this bridge instance.
     *
     * @return the name of the bridge
     */
    String getName();

    /**
     * Returns the type or category name of this bridge.
     *
     * @return the bridge type name
     */
    String bridgeName();

    /**
     * Checks if this bridge can be registered.
     *
     * @return true if the bridge can be registered, false otherwise
     */
    boolean canRegister();

    default boolean pluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }
}