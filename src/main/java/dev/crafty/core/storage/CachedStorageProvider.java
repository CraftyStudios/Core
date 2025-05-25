package dev.crafty.core.storage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A decorator that adds caching to any storage provider.
 * Uses Caffeine for high-performance caching.
 *
 * @since 1.0.0
 * @param <T> The type of object to store
 * @param <K> The type of key used to identify objects
 */
public class CachedStorageProvider<T, K> implements StorageProvider<T, K> {

    private final StorageProvider<T, K> delegate;
    private final Cache<K, CompletableFuture<Optional<T>>> cache;
    private final Cache<String, CompletableFuture<Collection<T>>> collectionCache;

    /**
     * Creates a new CachedStorageProvider with default cache settings.
     *
     * @param delegate The storage provider to delegate to
     */
    public CachedStorageProvider(StorageProvider<T, K> delegate) {
        this(delegate, 
             Caffeine.newBuilder()
                 .expireAfterWrite(5, TimeUnit.MINUTES)
                 .maximumSize(10000),
             Caffeine.newBuilder()
                 .expireAfterWrite(1, TimeUnit.MINUTES)
                 .maximumSize(100));
    }

    /**
     * Creates a new CachedStorageProvider with custom cache settings.
     *
     * @param delegate The storage provider to delegate to
     * @param cacheBuilder The builder for the item cache
     * @param collectionCacheBuilder The builder for the collection cache
     */
    public CachedStorageProvider(
            StorageProvider<T, K> delegate, 
            Caffeine<Object, Object> cacheBuilder,
            Caffeine<Object, Object> collectionCacheBuilder) {
        this.delegate = delegate;
        this.cache = cacheBuilder.build();
        this.collectionCache = collectionCacheBuilder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> save(K key, T value) {
        // Invalidate the cache for this key and the collection cache
        cache.invalidate(key);
        collectionCache.invalidateAll();

        // Delegate to the underlying provider
        return delegate.save(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<T>> get(K key) {
        // Get from cache or compute if absent
        return cache.get(key, delegate::get);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Collection<T>> getAll() {
        // Use a constant key for the collection cache
        String cacheKey = "all";

        // Get from cache or compute if absent
        return collectionCache.get(cacheKey, k -> delegate.getAll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> delete(K key) {
        // Invalidate the cache for this key and the collection cache
        cache.invalidate(key);
        collectionCache.invalidateAll();

        // Delegate to the underlying provider
        return delegate.delete(key);
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
        return delegate.initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> close() {
        // Clear all caches
        cache.invalidateAll();
        collectionCache.invalidateAll();

        // Delegate to the underlying provider
        return delegate.close();
    }

    /**
     * Clears all cached entries from both the item and collection caches.
     * <p>
     * This method invalidates all entries in the internal caches, effectively
     * resetting the cache state. It does not affect the underlying storage provider.
     * </p>
     *
     * @return a {@link CompletableFuture} that is already completed when the caches are cleared
     */
    public CompletableFuture<Void> clear() {
        // Clear all caches
        cache.invalidateAll();
        collectionCache.invalidateAll();

        return CompletableFuture.completedFuture(null);
    }
}
