package dev.crafty.core.storage.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Utility class for serializing and deserializing objects using Jackson.
 * @since 1.0.0
 */
public class StorageSerializer {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    
    /**
     * Converts an object to a JSON string.
     *
     * @param object The object to serialize
     * @return The JSON string representation of the object
     * @throws JsonProcessingException If serialization fails
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return JSON_MAPPER.writeValueAsString(object);
    }
    
    /**
     * Converts a JSON string to an object.
     *
     * @param json The JSON string to deserialize
     * @param valueType The class of the object to create
     * @param <T> The type of the object
     * @return The deserialized object
     * @throws IOException If deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> valueType) throws IOException {
        return JSON_MAPPER.readValue(json, valueType);
    }
    
    /**
     * Converts a JSON string to an object with generic type parameters.
     *
     * @param json The JSON string to deserialize
     * @param valueType The class of the object to create
     * @param parameterClasses The classes of the generic type parameters
     * @param <T> The type of the object
     * @return The deserialized object
     * @throws IOException If deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> valueType, Class<?>... parameterClasses) throws IOException {
        JavaType type = JSON_MAPPER.getTypeFactory().constructParametricType(valueType, parameterClasses);
        return JSON_MAPPER.readValue(json, type);
    }
    
    /**
     * Converts an object to a YAML string.
     *
     * @param object The object to serialize
     * @return The YAML string representation of the object
     * @throws JsonProcessingException If serialization fails
     */
    public static String toYaml(Object object) throws JsonProcessingException {
        return YAML_MAPPER.writeValueAsString(object);
    }
    
    /**
     * Converts a YAML string to an object.
     *
     * @param yaml The YAML string to deserialize
     * @param valueType The class of the object to create
     * @param <T> The type of the object
     * @return The deserialized object
     * @throws IOException If deserialization fails
     */
    public static <T> T fromYaml(String yaml, Class<T> valueType) throws IOException {
        return YAML_MAPPER.readValue(yaml, valueType);
    }
    
    /**
     * Converts an object to a Map.
     *
     * @param object The object to convert
     * @return A Map representation of the object
     */
    public static Map<String, Object> toMap(Object object) {
        return JSON_MAPPER.convertValue(object, JSON_MAPPER.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
    }
    
    /**
     * Converts a Map to an object.
     *
     * @param map The map to convert
     * @param valueType The class of the object to create
     * @param <T> The type of the object
     * @return The converted object
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> valueType) {
        return JSON_MAPPER.convertValue(map, valueType);
    }
    
    /**
     * Gets the JSON ObjectMapper instance.
     *
     * @return The JSON ObjectMapper
     */
    public static ObjectMapper getJsonMapper() {
        return JSON_MAPPER;
    }
    
    /**
     * Gets the YAML ObjectMapper instance.
     *
     * @return The YAML ObjectMapper
     */
    public static ObjectMapper getYamlMapper() {
        return YAML_MAPPER;
    }
}