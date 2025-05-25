package dev.crafty.core.i18n;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.plugin.CraftyPlugin;
import dev.crafty.core.util.Configs;
import dev.crafty.core.util.Lang;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Manages the translations for a plugin
 * @since 1.0.2
 */
public class i18nManager {

    private final SectionWrapper langConfig;

    public i18nManager(CraftyPlugin plugin) {
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
        private Function<String, String> replacer = null;

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

        public LocalizationQuery replace(Function<String, String> replacer) {
            this.replacer = replacer;
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
                    if (replacer != null) {
                        processed = replacer.apply(processed);
                    }
                    value.set(processed);
                });
            } else {
                String processed = fallbackString.map(Lang::colorize).orElse("key is null!");
                if (replacer != null) {
                    processed = replacer.apply(processed);
                }
                value.set(processed);
            }

            return value.get();
        }

        public void send() {
            player.ifPresent((p) -> p.sendMessage(get()) );
        }
    }
}
