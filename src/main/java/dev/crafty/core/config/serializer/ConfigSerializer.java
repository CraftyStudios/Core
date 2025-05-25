package dev.crafty.core.config.serializer;

import dev.crafty.core.config.SectionWrapper;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Interface for serializing and deserializing objects to and from configuration sections.
 * Implementations of this interface can be registered with the configuration system
 * to handle complex types.
 *
 * @param <T> The type of object this serializer handles
 * @since 1.0.0
 */
public interface ConfigSerializer<T> {

    /**
     * Serialize an object to a configuration section.
     *
     * @param args The serialization arguments
     */
    void serialize(SerializationArgs<T> args);

    /**
     * Deserialize an object from a configuration section.
     *
     * @param section The configuration section to deserialize from
     * @param path The path within the section to deserialize from
     * @return The deserialized object
     */
    Optional<T> deserialize(SectionWrapper section, String path);

    /**
     * Get the type of object this serializer handles.
     *
     * @return The class of the object type
     */
    Class<T> getType();

    default <S> ConfigSerializer<S> getSerializer(Class<S> type) {
        return SerializerRegistry.getSerializer(type).orElseThrow(() -> new IllegalArgumentException("No serializer found for type " + type.getName()));
    }

    default void save(SerializationArgs<T> args) {
        if (!args.save) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(args.configFile);

        try {
            config.save(args.configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    record SerializationArgs<T>(
            T value, SectionWrapper section, String path, File configFile, boolean save
    ) {
        public SerializationArgs(T value, SectionWrapper section, String path, File configFile) {
            this(value, section, path, configFile, true);
        }
    }
}