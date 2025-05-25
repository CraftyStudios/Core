package dev.crafty.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class Components {
    public static String toString(Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    public static List<String> toStringList(List<Component> components) {
        return components.stream().map(Components::toString).toList();
    }

    public static Component fromString(String string) {
        return MiniMessage.miniMessage().deserialize(string).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> fromStringList(List<String> strings) {
        return strings.stream().map(Components::fromString).toList();
    }

    public static Component unwrapSquareBrackets(Component component) {
        if (component instanceof TranslatableComponent tc) {
            if ("chat.square_brackets".equals(tc.key())) {
                if (!tc.args().isEmpty() && tc.args().get(0) instanceof Component inner) {
                    return inner;
                }
            }
        }

        return component;
    }

}
