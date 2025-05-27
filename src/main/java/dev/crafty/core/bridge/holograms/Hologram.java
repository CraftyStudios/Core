package dev.crafty.core.bridge.holograms;

import org.bukkit.Location;

/**
 * Represents a hologram that can be shown, hidden, and manipulated in the game world.
 * Implementations should provide the logic for displaying and updating holographic displays.
 */
public interface Hologram {
    /**
     * Displays the hologram in the game world.
     */
    void show();

    /**
     * Hides the hologram from the game world.
     */
    void hide();

    /**
     * Sets the content of a specific line in the hologram.
     *
     * @param line the index of the line to set
     * @param content the new content for the line
     */
    void setLine(int line, String content);

    /**
     * Updates the location of the hologram.
     *
     * @param location the new location for the hologram
     */
    void setLocation(Location location);

    /**
     * Retrieves the current location of the hologram.
     *
     * @return the hologram's location
     */
    Location getLocation();
}