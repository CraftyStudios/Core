package dev.crafty.core.gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

/**
 * Item that has click action for the gui
 * @since 1.0.0
 * @param item The item to display
 * @param actions The actions to run when clicked
 */
public record GuiItem(ItemStack item, List<Consumer<InventoryClickEvent>> actions) {
    public static GuiItem empty() {
        return new GuiItem(ItemStack.of(Material.AIR), List.of());
    }
}
