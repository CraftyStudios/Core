package dev.crafty.core.gui.suppliers;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.builtin.ItemStackSerializer;
import dev.crafty.core.gui.GuiItem;
import dev.crafty.core.gui.ItemSupplier;
import dev.crafty.core.gui.ItemType;
import dev.crafty.core.gui.Menu;
import dev.crafty.core.util.Expressions;
import dev.crafty.core.util.Lang;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * @since 1.0.0
 */
public class ConditionalItemSupplier extends ItemSupplier {
    public ConditionalItemSupplier() {
        super(ItemType.CONDITIONAL);
    }

    @Override
    public GuiItem[] apply(ItemStack[] slotsToApplyTo, SectionWrapper itemSection, Menu menu) {
        // Example Section:
        //    type: CONDITIONAL
        //    conditions:
        //      - "%player_has_permission_player.vip%"
        //    true-item:
        //      material: DIAMOND
        //      name: "<aqua>VIP Access"
        //      lore: ["<gray>You are a VIP"]
        //    false-item:
        //      material: COAL
        //      name: "<gray>No Access"
        //      lore: ["<red>You need VIP"]
        //    actions:
        //      - "message:<red>You clicked the VIP icon."

        var conditions = itemSection.getStringList("conditions").orElse(List.of());

        boolean evaluation = evaluateAllConditions(conditions, menu.getViewer());

        var trueItemSection = itemSection.getSection("true-item");
        var falseItemSection = itemSection.getSection("false-item");

        if (evaluation) {
            if (trueItemSection.isEmpty()) {
                return toArray(GuiItem.empty());
            } else {
                return applyGuiItems(slotsToApplyTo, menu, trueItemSection);
            }
        } else {
            if (falseItemSection.isEmpty()) {
                return toArray(GuiItem.empty());
            }

            return applyGuiItems(slotsToApplyTo, menu, falseItemSection);
        }
    }

    private GuiItem[] applyGuiItems(ItemStack[] slotsToApplyTo, Menu menu, Optional<SectionWrapper> conditionalItemSection) {
        var optionalItem = new ItemStackSerializer().deserialize(conditionalItemSection.get(), "item");
        if (optionalItem.isEmpty()) {
            return toArray(GuiItem.empty());
        }

        var item = optionalItem.get();

        applyToAll(slotsToApplyTo, item);

        return toArray(
                new GuiItem(item, getActions(conditionalItemSection.get(), menu))
        );
    }

    private static boolean evaluateAllConditions(List<String> conditions, Player viewer) {
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }

        for (String condition : conditions) {
            String parsedCondition = Lang.quotePlaceholderValues(condition, viewer);

            boolean result = Expressions.evaluateForBoolean(parsedCondition);
            if (!result) {
                return false;
            }
        }

        return true;
    }
}
