package dev.crafty.core.player;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.entity.Player;

/**
 * AntiSpam provides a mechanism to prevent players from sending repeated messages
 * within a specified cooldown period. It uses a Caffeine cache to store cooldowns
 * per player and message key.
 *
 *
 * @since 1.0.22
 */
public class AntiSpam {
    private static final Cache<UUID, Map<String, Long>> cooldownCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    /**
     * Creates a new {@link Builder} instance for sending a message with anti-spam protection.
     *
     * @return a new Builder instance
     */
    public static Builder send() {
        return new Builder();
    }

    /**
     * Builder class for configuring and sending messages with anti-spam checks.
     */
    public static class Builder {
        private Player player;
        private String message;
        private Duration cooldown = Duration.ofSeconds(5);
        private String key;

        /**
         * Sets the target player to send the message to.
         *
         * @param player the target player
         * @return this Builder instance
         */
        public Builder to(Player player) {
            this.player = player;
            return this;
        }

        /**
         * Sets the message to be sent.
         *
         * @param message the message content
         * @return this Builder instance
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the cooldown duration between messages with the same key.
         *
         * @param cooldown the cooldown duration
         * @return this Builder instance
         */
        public Builder cooldown(Duration cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        /**
         * Sets the key used to identify the type of message for cooldown tracking.
         *
         * @param key the message key
         * @return this Builder instance
         */
        public Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * Sends the message to the player if the cooldown has expired for the given key.
         * If the cooldown has not expired, the message is not sent.
         */
        public void send() {
            if (player == null || message == null) return;

            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            Map<String, Long> typeMap = cooldownCache.get(uuid, k -> new ConcurrentHashMap<>());
            Long lastSent = typeMap.get(key);

            if (lastSent != null && now - lastSent < cooldown.toMillis()) {
                return;
            }

            player.sendMessage(message);
            typeMap.put(key, now);
        }
    }
}