package dev.crafty.core.geometry.serializers;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.ConfigSerializer;
import dev.crafty.core.geometry.Point2d;
import dev.crafty.core.geometry.polygons.Polygon3d;

import java.util.List;
import java.util.Optional;

/**
 * @since 1.0.3
 */
public class Polygon3dSerializer implements ConfigSerializer<Polygon3d> {
    @Override
    public void serialize(SerializationArgs<Polygon3d> args) {
        var section = args.section();
        var path = args.path();
        var value = args.value();

        List<Point2d> points = value.getVertices();
        double minY = value.getMinY();
        double maxY = value.getMaxY();

        section.set(path + ".minY", minY);
        section.set(path + ".maxY", maxY);

        SectionWrapper verticesSection = section.createSection("vertices");

        for (int i = 1; i <= points.size(); i++) {
            Point2d point = points.get(i - 1);
            SerializationArgs<Point2d> pointArgs = new SerializationArgs<>(point, verticesSection, i + "", args.configFile(), false);
            getSerializer(Point2d.class).serialize(pointArgs);
        }

        save(args);
    }

    @Override
    public Optional<Polygon3d> deserialize(SectionWrapper section, String path) {
        var minY = section.getDouble(path + ".minY").orElse(0.0);
        var maxY = section.getDouble(path + ".maxY").orElse(0.0);

        var _verticesSection = section.getSection(path + ".vertices");
        if (_verticesSection.isEmpty()) return Optional.empty();
        var verticesSection = _verticesSection.get();

        List<Point2d> points = verticesSection.getKeys(false).stream()
                .map(key -> getSerializer(Point2d.class).deserialize(verticesSection, key))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return Optional.of(new Polygon3d(points, minY, maxY));
    }

    @Override
    public Class<Polygon3d> getType() {
        return Polygon3d.class;
    }
}
