package dev.crafty.core.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class Items {
    public static void setItemGlow(ItemStack item) {
        item.editMeta(meta -> {
            meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
    }
}
