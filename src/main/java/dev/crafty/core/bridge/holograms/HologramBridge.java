package dev.crafty.core.bridge.holograms;

import dev.crafty.core.bridge.Bridge;
import org.bukkit.Location;

import java.util.List;

public interface HologramBridge extends Bridge {

    /**
     * Create a new hologram at the given location with the specified lines.
     * @param location The location to spawn the hologram.
     * @param lines The lines of text for the hologram.
     * @return The created Hologram instance.
     */
    Hologram createHologram(Location location, String name, List<String> lines);

    @Override
    default String bridgeName() {
        return "Holograms";
    }
}
