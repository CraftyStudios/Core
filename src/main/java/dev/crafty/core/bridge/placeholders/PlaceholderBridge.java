package dev.crafty.core.bridge.placeholders;

import dev.crafty.core.bridge.Bridge;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The {@code PlaceholderBridge} interface defines the contract for integrating
 * placeholder functionality within the application. Implementations of this interface
 * allow for the registration and replacement of placeholders in strings, typically
 * used for dynamic content rendering.
 *
 * <p>This bridge is intended to be used with external placeholder plugins or systems.
 * It extends the {@link Bridge} interface and provides methods for registering
 * custom placeholders and replacing them in input strings.</p>
 *
 * @since 1.0.4
 */
public interface PlaceholderBridge extends Bridge {

    /**
     * Registers a new placeholder with the given identifier and replacement function.
     *
     * @param identifier the unique identifier for the placeholder (e.g., "player_name")
     * @param replacer   a function that takes the input string and returns the replacement value
     */
    void registerPlaceholder(String identifier, BiFunction<Player, String, String> replacer);

    /**
     * Replaces all registered placeholders in the given input string with their corresponding values.
     *
     * @param input the input string containing placeholders to be replaced
     * @return the input string with all placeholders replaced
     */
    String replacePlaceholders(String input);

    /**
     * Replaces all registered placeholders in the given input string with their corresponding values,
     * using the provided {@link Player} context for player-specific placeholders.
     *
     * @param input  the input string containing placeholders to be replaced
     * @param player the player whose context should be used for placeholder replacement
     * @return the input string with all placeholders replaced, using player context where applicable
     */
    String replacePlaceholders(String input, Player player);

    /**
     * Returns the name of this bridge implementation.
     *
     * @return the string "Placeholder"
     */
    @Override
    default String bridgeName() {
        return "Placeholder";
    }
}