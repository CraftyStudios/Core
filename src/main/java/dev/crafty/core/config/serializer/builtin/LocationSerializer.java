package dev.crafty.core.config.serializer.builtin;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.ConfigSerializer;
import org.bukkit.Location;

import java.util.Optional;

/**
 * @since 1.0.9
 */
public class LocationSerializer implements ConfigSerializer<Location> {
    @Override
    public void serialize(SerializationArgs<Location> args) {
        var section = args.section();
        var path = args.path();
        var value = args.value();

        if (value == null) {
            section.set(path, null);
            return;
        }

        section.set(path, value.serialize());
    }

    @Override
    public Optional<Location> deserialize(SectionWrapper section, String path) {
        return section.getLocation(path);
    }

    @Override
    public Class<Location> getType() {
        return Location.class;
    }
}
