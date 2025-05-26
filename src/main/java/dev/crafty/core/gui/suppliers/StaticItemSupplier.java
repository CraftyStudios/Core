package dev.crafty.core.gui.suppliers;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.builtin.ItemStackSerializer;
import dev.crafty.core.gui.GuiItem;
import dev.crafty.core.gui.ItemSupplier;
import dev.crafty.core.gui.ItemType;
import dev.crafty.core.gui.Menu;
import org.bukkit.inventory.ItemStack;

/**
 * @since 1.0.0
 */
public class StaticItemSupplier extends ItemSupplier {
    public StaticItemSupplier() {
        super(ItemType.STATIC);
    }

    @Override
    public GuiItem[] apply(ItemStack[] slotsToApplyTo, SectionWrapper itemSection, Menu menu) {
        // Example Section:
        //    type: STATIC
        //    item:
        //      material: CHEST
        //      name: "<gold>Storage"
        //      lore: ["<gray>Open your storage"]
        //    actions:
        //      - "open_new_gui"

        var optionalItem = new ItemStackSerializer().deserialize(itemSection, "item");
        if (optionalItem.isEmpty()) {
            return toArray(GuiItem.empty());
        }

        var item = optionalItem.get();

        applyToAll(slotsToApplyTo, item);

        return toArray(
                new GuiItem(item, getActions(itemSection, menu)),
                slotsToApplyTo.length
        );
    }
}
