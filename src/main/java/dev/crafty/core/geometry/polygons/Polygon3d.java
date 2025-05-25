package dev.crafty.core.geometry.polygons;

import dev.crafty.core.geometry.Point2d;
import dev.crafty.core.geometry.Point3d;
import dev.crafty.core.geometry.Polygon;
import lombok.Getter;

import java.util.List;

/**
 * Polygon in 3D space
 * @since 1.0.3
 */
@Getter
public class Polygon3d implements Polygon {
    private final List<Point2d> vertices;
    private final double minY;
    private final double maxY;

    public Polygon3d(List<Point2d> vertices, double minY, double maxY) {
        this.vertices = vertices;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public boolean contains(Point3d point) {
        if (point.y() < minY || point.y() > maxY) return false;

        boolean inside = false;
        for (int i = 0, j = vertices.size() - 1; i < vertices.size(); j = i++) {
            double xi = vertices.get(i).x(), zi = vertices.get(i).z();
            double xj = vertices.get(j).x(), zj = vertices.get(j).z();

            boolean intersect = ((zi > point.z()) != (zj > point.z())) &&
                    (point.x() < (xj - xi) * (point.z() - zi) / (zj - zi + 0.0) + xi);
            if (intersect) inside = !inside;
        }

        return inside;
    }

    @Override
    public int getVertexCount() {
        return vertices.size();
    }
}
