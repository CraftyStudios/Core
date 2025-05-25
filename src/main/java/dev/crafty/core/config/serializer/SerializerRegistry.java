package dev.crafty.core.config.serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for configuration serializers.
 * This class manages all the available serializers and provides methods
 * for registering and retrieving them.
 *
 * @since 1.0.0
 */
public class SerializerRegistry {

    private static final Map<Class<?>, ConfigSerializer<?>> SERIALIZERS = new HashMap<>();

    /**
     * Register a serializer for a specific type.
     *
     * @param serializer The serializer to register
     * @param <T> The type of object the serializer handles
     */
    public static <T> void register(ConfigSerializer<T> serializer) {
        SERIALIZERS.put(serializer.getType(), serializer);
    }

    /**
     * Get a serializer for a specific type.
     *
     * @param type The type to get a serializer for
     * @param <T> The type of object the serializer handles
     * @return An Optional containing the serializer if found, or empty if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<ConfigSerializer<T>> getSerializer(Class<T> type) {
        return Optional.ofNullable((ConfigSerializer<T>) SERIALIZERS.get(type));
    }

    /**
     * Check if a serializer exists for a specific type.
     *
     * @param type The type to check for
     * @return True if a serializer exists for the type, false otherwise
     */
    public static boolean hasSerializer(Class<?> type) {
        return SERIALIZERS.containsKey(type);
    }

    /**
     * Remove a serializer for a specific type.
     *
     * @param type The type to remove the serializer for
     */
    public static void unregister(Class<?> type) {
        SERIALIZERS.remove(type);
    }

    /**
     * Clear all registered serializers.
     */
    public static void clear() {
        SERIALIZERS.clear();
    }

    /**
     * Get the number of registered serializers.
     *
     * @return The number of registered serializers
     */
    public static int size() {
        return SERIALIZERS.size();
    }
}