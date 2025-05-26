package dev.crafty.core.player;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.entity.Player;

public class AntiSpam {
    private static final Cache<UUID, Map<String, Long>> cooldownCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public static Builder send() {
        return new Builder();
    }

    public static class Builder {
        private Player player;
        private String message;
        private Duration cooldown = Duration.ofSeconds(5);
        private String key;

        public Builder to(Player player) {
            this.player = player;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder cooldown(Duration cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public void send() {
            if (player == null || message == null) return;

            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            Map<String, Long> typeMap = cooldownCache.get(uuid, k -> new ConcurrentHashMap<>());
            if (typeMap == null) {
                typeMap = new ConcurrentHashMap<>();
            }
            Long lastSent = typeMap.get(key);

            if (lastSent != null && now - lastSent < cooldown.toMillis()) {
                return;
            }

            player.sendMessage(message);
            typeMap.put(key, now);
        }
    }
}