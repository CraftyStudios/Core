package dev.crafty.core.bridge;

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
}