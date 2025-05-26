package dev.crafty.core.items;

import dev.crafty.core.util.Expressions;
import dev.crafty.core.util.Lang;
import org.bukkit.entity.Player;

public record ItemActionCondition(String expression, String denyMessage) {

    public boolean canExecute(Player player) {
        String replacedExpression = Lang.quotePlaceholderValues(expression, player);

        return Expressions.evaluateForBoolean(replacedExpression);
    }
}
