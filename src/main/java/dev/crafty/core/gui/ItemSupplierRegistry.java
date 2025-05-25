package dev.crafty.core.gui;

import dev.crafty.core.gui.suppliers.ConditionalItemSupplier;
import dev.crafty.core.gui.suppliers.DynamicItemSupplier;
import dev.crafty.core.gui.suppliers.MappedItemSupplier;
import dev.crafty.core.gui.suppliers.StaticItemSupplier;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.0.0
 */
public class ItemSupplierRegistry {
    private static final Map<ItemType, ItemSupplier> suppliers = new HashMap<>();

    static {
        new StaticItemSupplier();
        new MappedItemSupplier();
        new DynamicItemSupplier();
        new ConditionalItemSupplier();
    }

    public static void register(ItemType type, ItemSupplier supplier) {
        suppliers.put(type, supplier);
    }

    public static ItemSupplier getSupplier(ItemType type) {
        if (suppliers.containsKey(type)) {
            return suppliers.get(type);
        } else {
            throw new IllegalArgumentException("No supplier registered for type " + type.name());
        }
    }
}
