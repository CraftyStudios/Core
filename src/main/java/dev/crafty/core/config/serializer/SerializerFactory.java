package dev.crafty.core.config.serializer;

import dev.crafty.core.config.SectionWrapper;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Factory for creating serializers for common types.
 * This class provides methods for creating serializers for primitive types
 * and other common Java types.
 *
 * @since 1.0.0
 */
public class SerializerFactory {

    /**
     * Create a serializer for a primitive type or other simple type that
     * can be directly stored in a configuration section.
     *
     * @param type The class of the type to create a serializer for
     * @param getter The function to get the value from the configuration section
     * @param <T> The type to create a serializer for
     * @return A serializer for the specified type
     */
    public static <T> ConfigSerializer<T> createSimpleSerializer(
            Class<T> type,
            BiFunction<SectionWrapper, String, Optional<T>> getter) {
        
        return new ConfigSerializer<>() {
            @Override
            public void serialize(T value, SectionWrapper section, String path) {
                section.set(path, value);
            }

            @Override
            public Optional<T> deserialize(SectionWrapper section, String path) {
                if (!section.contains(path)) {
                    return Optional.empty();
                }
                
                return getter.apply(section, path);
            }

            @Override
            public Class<T> getType() {
                return type;
            }
        };
    }

    /**
     * Create a serializer for a list of items.
     *
     * @param type The class of the list type
     * @param itemType The class of the items in the list
     * @param getter The function to get the list from the configuration section
     * @param <T> The type of the items in the list
     * @return A serializer for the specified list type
     */
    public static <T> ConfigSerializer<List<T>> createListSerializer(
            Class<List<T>> type,
            Class<T> itemType,
            BiFunction<SectionWrapper, String, Optional<List<T>>> getter) {
        
        return new ConfigSerializer<>() {
            @Override
            public void serialize(List<T> value, SectionWrapper section, String path) {
                section.set(path, value);
            }

            @Override
            public Optional<List<T>> deserialize(SectionWrapper section, String path) {
                if (!section.contains(path)) {
                    return Optional.empty();
                }

                return getter.apply(section, path);
            }

            @Override
            public Class<List<T>> getType() {
                return type;
            }
        };
    }

    /**
     * Register all the built-in serializers.
     * This method should be called when the plugin is enabled.
     */
    public static void registerBuiltInSerializers() {
        // Register primitive type serializers
        SerializerRegistry.register(createSimpleSerializer(String.class, SectionWrapper::getString));
        SerializerRegistry.register(createSimpleSerializer(Integer.class, SectionWrapper::getInt));
        SerializerRegistry.register(createSimpleSerializer(Boolean.class, SectionWrapper::getBoolean));
        SerializerRegistry.register(createSimpleSerializer(Double.class, SectionWrapper::getDouble));
        SerializerRegistry.register(createSimpleSerializer(Float.class, SectionWrapper::getFloat));
        SerializerRegistry.register(createSimpleSerializer(Long.class, SectionWrapper::getLong));

        // Register list serializers
        SerializerRegistry.register(createListSerializer(
                (Class<List<String>>) (Class<?>) List.class,
                String.class,
                SectionWrapper::getStringList));
        
        SerializerRegistry.register(createListSerializer(
                (Class<List<Integer>>) (Class<?>) List.class,
                Integer.class,
                SectionWrapper::getIntegerList));
        
        SerializerRegistry.register(createListSerializer(
                (Class<List<Boolean>>) (Class<?>) List.class,
                Boolean.class,
                SectionWrapper::getBooleanList));
        
        SerializerRegistry.register(createListSerializer(
                (Class<List<Double>>) (Class<?>) List.class,
                Double.class,
                SectionWrapper::getDoubleList));
        
        SerializerRegistry.register(createListSerializer(
                (Class<List<Float>>) (Class<?>) List.class,
                Float.class,
                SectionWrapper::getFloatList));
        
        SerializerRegistry.register(createListSerializer(
                (Class<List<Long>>) (Class<?>) List.class,
                Long.class,
                SectionWrapper::getLongList));
    }
}