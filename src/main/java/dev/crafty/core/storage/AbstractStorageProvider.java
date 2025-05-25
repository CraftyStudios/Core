package dev.crafty.core.storage;

import lombok.Getter;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base implementation of StorageProvider that provides common functionality.
 *
 * @since 1.0.0
 * @param <T> The type of object to store
 * @param <K> The type of key used to identify objects
 */
@Getter
public abstract class AbstractStorageProvider<T, K> implements StorageProvider<T, K> {

    protected final Class<T> valueType;
    
    /**
     * Creates a new AbstractStorageProvider.
     *
     * @param valueType The class of the value type
     */
    protected AbstractStorageProvider(Class<T> valueType) {
        this.valueType = valueType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> exists(K key) {
        return get(key).thenApply(Optional::isPresent);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.completedFuture(null);
    }
}