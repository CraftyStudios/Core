package dev.crafty.core.util;

import dev.crafty.core.bridge.BridgeManager;
import dev.crafty.core.bridge.placeholders.PlaceholderBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lang {
    public static String colorize(String str) {
        Component component = MiniMessage.miniMessage().deserialize(str).decoration(TextDecoration.ITALIC, false);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static List<String> colorizeList(List<String> str) {
        return str.stream().map(Lang::colorize).toList();
    }

    /**
     * Apply PlaceholderAPI replacements on the plain text of the given component,
     * then parse the result as MiniMessage, and apply optional style fixes.
     *
     * @param viewer The player for PlaceholderAPI context
     * @param component The input component to process
     * @return A styled Component with placeholders and MiniMessage parsed
     */
    public static Component colorizeWithPlaceholders(Player viewer, Component component) {
        Component cleaned = Components.unwrapSquareBrackets(component);
        String plainText = PlainTextComponentSerializer.plainText().serialize(cleaned);

        String replaced = replacePlaceholders(plainText, viewer);

        return MiniMessage.miniMessage()
                .deserialize(replaced)
                .decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Used in expressions
     */
    public static String quotePlaceholderValues(String expr, Player viewer) {
        Pattern pattern = Pattern.compile("%[^%]+%");
        Matcher matcher = pattern.matcher(expr);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String placeholder = matcher.group();
            String value = replacePlaceholders(placeholder, viewer);

            String replacement;
            value = value.trim();
            boolean isNumber = value.matches("-?\\d+(\\.\\d+)?");
            boolean isBoolean = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");

            if (isNumber || isBoolean) {
                replacement = value;
            } else {
                replacement = "'" + value.replace("'", "\\'") + "'";
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }


    /**
     * Helper to do the same on a list of Components (like lore lines).
     */
    public static List<Component> colorizeListWithPlaceholders(Player viewer, List<Component> components) {
        return components.stream()
                .map(c -> colorizeWithPlaceholders(viewer, c))
                .toList();
    }

    public static void replaceText(ItemStack item, Map<String, String> mappings) {
        if (item == null || !item.hasItemMeta()) return;

        var meta = item.getItemMeta();

        if (meta.displayName() != null) {
            String name = Components.toString(meta.displayName());
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                name = name.replace(entry.getKey(), entry.getValue());
            }
            meta.displayName(Components.fromString(name));
        }

        if (meta.hasLore()) {
            List<String> lore = meta.lore().stream()
                    .map(Components::toString)
                    .map(line -> {
                        for (Map.Entry<String, String> entry : mappings.entrySet()) {
                            line = line.replace(entry.getKey(), entry.getValue());
                        }
                        return line;
                    })
                    .toList();

            meta.lore(Components.fromStringList(lore));
        }

        item.setItemMeta(meta);
    }

    /**
     * Replaces placeholders in the given message using the PlaceholderAPI context.
     *
     * @param message The input string containing placeholders to be replaced.
     * @param player The player context for placeholder replacement. If null, global placeholders are used.
     * @return The message with placeholders replaced. If no PlaceholderBridge is available, the original message is returned.
     */
    public static String replacePlaceholders(String message, Player player) {
        PlaceholderBridge bridge = BridgeManager.getBridge(PlaceholderBridge.class);
        if (bridge == null) {
            return message;
        }

        return bridge.replacePlaceholders(message, player);
    }
}
