package dev.crafty.core.bridge.mobs;

import dev.crafty.core.bridge.Bridge;
import dev.crafty.core.geometry.Point3d;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * MobBridge defines an interface for bridging mob-related actions,
 * such as spawning and despawning mobs, between different systems.
 * It extends the {@link Bridge} interface.
 *
 * @since 1.0.25
 */
public interface MobBridge extends Bridge {

    /**
     * Spawns a mob with the given name at the specified {@link Point3d} location.
     *
     * @param mobName  the name of the mob to spawn
     * @param location the 3D point where the mob should be spawned
     */
    Optional<Entity> spawnMob(String mobName, Point3d location, String world, int level);

    /**
     * Spawns a mob with the given name at the specified Bukkit {@link Location}.
     * This is a default method that converts the Bukkit Location to a {@link Point3d}.
     *
     * @param mobName  the name of the mob to spawn
     * @param location the Bukkit location where the mob should be spawned
     */
    default Optional<Entity> spawnMob(String mobName, Location location) {
        return spawnMob(mobName, new Point3d(location.getX(), location.getY(), location.getZ()), location.getWorld().getName(), 1);
    }

    /**
     * Spawns a mob with the given name at the specified {@link Point3d} location,
     * using a default level of 1.
     *
     * @param mobName  the name of the mob to spawn
     * @param location the 3D point where the mob should be spawned
     */
    default Optional<Entity> spawnMob(String mobName, Point3d location, String world) {
        return spawnMob(mobName, location, world, 1);
    }

    @Override
    default String bridgeName() {
        return "Mobs";
    }
}