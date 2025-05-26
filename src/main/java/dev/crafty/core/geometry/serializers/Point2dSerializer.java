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
    /**
     * Serializes a Point2d object into the given configuration section at the specified path.
     *
     * */
    @Override
    public void serialize(SerializationArgs<Point2d> args) {
        var section = args.section();
        var path = args.path();
        var value = args.value();

        section.set(path + ".x", value.x());
        section.set(path + ".z", value.z());

        save(args);
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
