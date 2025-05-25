package dev.crafty.core.storage;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @since 1.0.0
 */
public class PlayerDataBuilder {
    private final UUID playerId;
    private final Map<String, Object> data = new HashMap<>();
    private final Map<String, Class<?>> types = new HashMap<>();

    private PlayerDataBuilder(UUID playerId) {
        this.playerId = playerId;
    }

    public static PlayerDataBuilder forPlayer(UUID playerId) {
        return new PlayerDataBuilder(playerId);
    }

    public PlayerDataBuilder storeString(String key, String value) {
        data.put(key, value);
        types.put(key, String.class);
        return this;
    }

    public PlayerDataBuilder storeInt(String key, int value) {
        data.put(key, value);
        types.put(key, Integer.class);
        return this;
    }

    public PlayerDataBuilder storeLong(String key, long value) {
        data.put(key, value);
        types.put(key, Long.class);
        return this;
    }

    public PlayerDataBuilder storeDouble(String key, double value) {
        data.put(key, value);
        types.put(key, Double.class);
        return this;
    }

    public PlayerDataBuilder storeBoolean(String key, boolean value) {
        data.put(key, value);
        types.put(key, Boolean.class);
        return this;
    }

    public PlayerDataBuilder storeFloat(String key, float value) {
        data.put(key, value);
        types.put(key, Float.class);
        return this;
    }

    public <T> PlayerDataBuilder store(String key, T value, Class<T> type) {
        data.put(key, value);
        types.put(key, type);
        return this;
    }

    public <T> PlayerDataBuilder withValue(String key, Consumer<T> consumer, Class<T> type) {
        Optional<T> value = get(key, type);
        value.ifPresent(consumer);
        return this;
    }

    public CompletableFuture<Void> save() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        ProviderManager manager = ProviderManager.getInstance();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Class<?> type = types.get(key);

            PlayerDataProvider provider = manager.forPlayer(playerId, type);
            futures.add(provider.save(key, value));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        PlayerDataProvider<T> provider = ProviderManager.getInstance().forPlayer(playerId, type);
        return provider.get(key).join();
    }
}