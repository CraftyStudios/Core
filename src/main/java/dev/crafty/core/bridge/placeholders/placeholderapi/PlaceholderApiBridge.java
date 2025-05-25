package dev.crafty.core.bridge.placeholders.placeholderapi;

import dev.crafty.core.CraftyCore;
import dev.crafty.core.bridge.placeholders.PlaceholderBridge;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * @since 1.0.4
 */
public class PlaceholderApiBridge implements PlaceholderBridge {

    @Override
    public void registerPlaceholder(String identifier, BiFunction<Player, String, String> replacer) {
        PlaceholderExpansion expansion = new PlaceholderExpansion() {
            @Override
            public @NotNull String getIdentifier() {
                return identifier;
            }

            @Override
            public @NotNull String getAuthor() {
                return "CraftyCore";
            }

            @Override
            public @NotNull String getVersion() {
                return CraftyCore.INSTANCE.getPluginMeta().getVersion();
            }

            @Override
            public String onPlaceholderRequest(Player player, @NotNull String params) {
                return replacer.apply(player, params);
            }

            @Override
            public boolean canRegister() {
                return true;
            }
        };

        expansion.register();
    }

    @Override
    public String replacePlaceholders(String input) {
        return replacePlaceholders(input, null);
    }

    @Override
    public String replacePlaceholders(String input, Player player) {
        return PlaceholderAPI.setPlaceholders(player, input);
    }

    @Override
    public String getName() {
        return "PlaceholderAPI";
    }
}
