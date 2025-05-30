package dev.crafty.core.i18n;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.plugin.CraftyPlugin;
import dev.crafty.core.util.Configs;
import dev.crafty.core.util.Lang;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
/**
 * Manages the translations for a plugin
 * @since 1.0.2
 */
public class i18nManager {

    private SectionWrapper langConfig;
    private final CraftyPlugin plugin;

    public i18nManager(CraftyPlugin plugin) {
        this.langConfig = Configs.getOrCreateConfigFile(plugin, "lang.yml");
        this.plugin = plugin;
    }

    public void reload() {
        this.langConfig = Configs.getOrCreateConfigFile(plugin, "lang.yml");
    }

    public LocalizationQuery query(i18nEnum key) {
        return new LocalizationQuery(key);
    }

    public class LocalizationQuery {
        private final i18nEnum key;
        private Optional<Player> player = Optional.empty();
        private boolean replacePlaceholders = true;
        private Optional<String> fallbackString = Optional.empty();
        private final Map<String, String> placeholders = new HashMap<>();

        LocalizationQuery(i18nEnum key) {
            this.key = key;
        }

        public LocalizationQuery player(Player player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public LocalizationQuery replacePlaceholders(boolean replacePlaceholders) {
            this.replacePlaceholders = replacePlaceholders;
            return this;
        }

        public LocalizationQuery orElse(String fallbackString) {
            this.fallbackString = Optional.ofNullable(fallbackString);
            return this;
        }

        /**
         * Adds a placeholder to the localization query. Placeholders are used to dynamically
         * replace keys in the localized string with corresponding values.
         *
         * @param key   the placeholder key to be replaced in the localized string
         * @param value the value to replace the placeholder key with
         * @return the current {@link LocalizationQuery} instance for method chaining
         */
        public LocalizationQuery placeholder(String key, String value) {
            this.placeholders.put(key, value);
            return this;
        }

        public String get() {
            AtomicReference<String> value = new AtomicReference<>();

            boolean useFallback = this.key == null;

            if (!useFallback) {
                String configKey = this.key.getTranslationKey();

                langConfig.getString(configKey).ifPresent(string -> {
                    String processed;
                    if (replacePlaceholders) {
                        if (player.isPresent()) {
                            Player found = player.get();
                            processed = Lang.colorize(Lang.replacePlaceholders(string, found));
                        } else {
                            processed = Lang.colorize(Lang.replacePlaceholders(string, null));
                        }
                    } else {
                        processed = Lang.colorize(string);
                    }
                    replacePlaceholders(value, processed);
                });
            } else {
                String processed = fallbackString.map(Lang::colorize).orElse("key is null!");
                replacePlaceholders(value, processed);
            }

            return value.get();
        }

        private void replacePlaceholders(AtomicReference<String> value, String processed) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String colorizedEntryValue = Lang.colorize(entry.getValue());
                processed = processed.replace("{" + entry.getKey() + "}", colorizedEntryValue);
            }
            value.set(processed);
        }

        public void send() {
            player.ifPresent((p) -> p.sendMessage(get()));
        }
    }
}