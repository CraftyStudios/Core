package dev.crafty.core.geometry.serializers;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.ConfigSerializer;
import dev.crafty.core.geometry.Point2d;
import dev.crafty.core.util.Optionals;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 1.0.3
 */
public class Point2dSerializer implements ConfigSerializer<Point2d> {
    @Override
    public void serialize(Point2d value, SectionWrapper section, String path, File configFile) {
        section.set(path + ".x", value.x());
        section.set(path + ".z", value.z());

        save(configFile);
    }

    @Override
    public Optional<Point2d> deserialize(SectionWrapper section, String path) {
        var _x = section.getDouble(path + ".x");
        var _z = section.getDouble(path + ".z");

        AtomicReference<Optional<Point2d>> point = new AtomicReference<>(Optional.empty());

        Optionals.ifAllPresent(_x, _z, (x, z) -> point.set(Optional.of(new Point2d(x, z))));

        return point.get();
    }

    @Override
    public Class<Point2d> getType() {
        return Point2d.class;
    }
}
