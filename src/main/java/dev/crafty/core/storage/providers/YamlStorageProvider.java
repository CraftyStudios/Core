package dev.crafty.core.storage.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crafty.core.storage.AbstractStorageProvider;
import dev.crafty.core.storage.serialization.StorageSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A storage provider that stores objects in YAML files.
 *
 * @param <T> The type of object to store
 * @since 1.0.0
 */
public class YamlStorageProvider<T> extends AbstractStorageProvider<T, String> {

    private final Path directory;
    private final String fileExtension;
    private final ObjectMapper mapper;

    /**
     * Creates a new YamlStorageProvider.
     *
     * @param valueType The class of the value type
     * @param directory The directory to store files in
     */
    public YamlStorageProvider(Class<T> valueType, Path directory) {
        this(valueType, directory, ".yml");
    }

    /**
     * Creates a new YamlStorageProvider with a custom file extension.
     *
     * @param valueType The class of the value type
     * @param directory The directory to store files in
     * @param fileExtension The file extension to use (including the dot)
     */
    public YamlStorageProvider(Class<T> valueType, Path directory, String fileExtension) {
        super(valueType);
        this.directory = directory;
        this.fileExtension = fileExtension;
        this.mapper = StorageSerializer.getYamlMapper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory: " + directory, e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> save(String key, T value) {
        return CompletableFuture.runAsync(() -> {
            try {
                File file = getFile(key);
                mapper.writeValue(file, value);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save object with key: " + key, e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<T>> get(String key) {
        return CompletableFuture.supplyAsync(() -> {
            File file = getFile(key);
            if (!file.exists()) {
                return Optional.empty();
            }

            try {
                T value = mapper.readValue(file, valueType);
                return Optional.ofNullable(value);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read object with key: " + key, e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Collection<T>> getAll() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get all files with the correct extension
                return Files.list(directory)
                        .filter(path -> path.toString().endsWith(fileExtension))
                        .map(path -> {
                            try {
                                return mapper.readValue(path.toFile(), valueType);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read object from file: " + path, e);
                            }
                        })
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException("Failed to list files in directory: " + directory, e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> delete(String key) {
        return CompletableFuture.runAsync(() -> {
            File file = getFile(key);
            if (file.exists() && !file.delete()) {
                throw new RuntimeException("Failed to delete file: " + file);
            }
        });
    }

    /**
     * Gets the file for the given key.
     *
     * @param key The key
     * @return The file
     */
    private File getFile(String key) {
        return directory.resolve(key + fileExtension).toFile();
    }

    /**
     * Gets the key from a file path.
     *
     * @param path The file path
     * @return The key
     */
    private String getKeyFromPath(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.length() - fileExtension.length());
    }
}
