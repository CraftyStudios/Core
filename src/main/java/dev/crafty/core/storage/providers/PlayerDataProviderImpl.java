package dev.crafty.core.storage.providers;

import lombok.AllArgsConstructor;
import dev.crafty.core.storage.PlayerDataProvider;
import dev.crafty.core.storage.StorageProvider;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @since 1.0.0
 */
@AllArgsConstructor
public class PlayerDataProviderImpl<T> implements PlayerDataProvider<T> {
    private final StorageProvider<T, String> delegate;
    private final UUID playerId;

    @Override
    public CompletableFuture<Void> save(String key, T value) {
        return delegate.save(toKey(playerId, key), value);
    }

    @Override
    public CompletableFuture<Optional<T>> get(String key) {
        return delegate.get(toKey(playerId, key));
    }

    @Override
    public CompletableFuture<Void> delete(String key) {
        return delegate.delete(toKey(playerId, key));
    }

    @Override
    public CompletableFuture<Boolean> exists(String key) {
        return delegate.exists(toKey(playerId, key));
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return delegate.initialize();
    }

    @Override
    public CompletableFuture<Void> close() {
        return delegate.close();
    }
}
