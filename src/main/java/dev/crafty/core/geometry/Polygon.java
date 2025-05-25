package dev.crafty.core.geometry;

import org.bukkit.Location;

/**
 * A polygon
 * @since 1.0.3
 */
public interface Polygon {
    default boolean contains(Location location) {
        return contains(new Point3d(location.getX(), location.getY(), location.getZ()));
    }

    boolean contains(Point3d point);
    int getVertexCount();
}
