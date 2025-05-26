package dev.crafty.core.gui;

import dev.crafty.core.config.SectionWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class ItemSupplier {

    public ItemSupplier(ItemType type) {
        ItemSupplierRegistry.register(type, this);
    }

    public abstract GuiItem[] apply(ItemStack[] slotsToApplyTo, SectionWrapper itemSection, Menu menu);

    protected void applyToAll(ItemStack[] slotsToApplyTo, ItemStack itemStack) {
        Arrays.fill(slotsToApplyTo, itemStack);
    }

    protected GuiItem[] toArray(GuiItem... items) {
        return items;
    }

    protected GuiItem[] toArray(GuiItem item, int times) {
        GuiItem[] array = new GuiItem[times];
        Arrays.fill(array, item);
        return array;
    }

    protected List<Consumer<InventoryClickEvent>> getActions(SectionWrapper section, Menu menu) {
        if (!section.contains("actions")) {
            return List.of();
        }

        List<Consumer<InventoryClickEvent>> foundActions = new ArrayList<>();

        section.getStringList("actions")
                .ifPresent(actions -> foundActions.addAll(
                        actions
                            .stream()
                            .map(menu::getAction)
                            .filter(Objects::nonNull)
                            .toList())
                );

        return foundActions;
    }
}
