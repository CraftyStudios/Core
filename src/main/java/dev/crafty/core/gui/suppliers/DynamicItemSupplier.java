package dev.crafty.core.gui.suppliers;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.gui.GuiItem;
import dev.crafty.core.gui.ItemSupplier;
import dev.crafty.core.gui.ItemType;
import dev.crafty.core.gui.Menu;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @since 1.0.0
 */
public class DynamicItemSupplier extends ItemSupplier {
    public DynamicItemSupplier() {
        super(ItemType.DYNAMIC);
    }

    @Override
    public GuiItem[] apply(ItemStack[] slotsToApplyTo, SectionWrapper itemSection, Menu menu) {
        // Example Section:
        //  type: DYNAMIC
        //  provider: "custom_item"
        //  actions:
        //    - "open_gui"

        List<Consumer<InventoryClickEvent>> actions = getActions(itemSection, menu);

        var optionalProvider = itemSection.getString("provider");

        if (optionalProvider.isEmpty()) {
            return toArray(GuiItem.empty());
        }

        Supplier<ItemStack> itemSupplier = menu.getItemSupplier(optionalProvider.get());

        if (itemSupplier == null) {
            return toArray(GuiItem.empty());
        }

        applyToAll(slotsToApplyTo, itemSupplier.get());

        return toArray(
                new GuiItem(itemSupplier.get(), actions)
        );
    }
}
