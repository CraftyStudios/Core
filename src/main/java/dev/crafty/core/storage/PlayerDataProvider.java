package dev.crafty.core.storage;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @since 1.0.0
 * @param <T>
 */
public interface PlayerDataProvider<T> {
    CompletableFuture<Void> save(String key, T value);

    CompletableFuture<Optional<T>> get(String key);

    CompletableFuture<Void> delete(String key);

    CompletableFuture<Boolean> exists(String key);

    CompletableFuture<Void> initialize();

    CompletableFuture<Void> close();


    default String toKey(UUID playerId, String key) {
        return playerId.toString() + ":" + key;
    }
}