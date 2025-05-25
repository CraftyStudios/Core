package dev.crafty.core.gui.suppliers;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.builtin.ItemStackSerializer;
import dev.crafty.core.gui.GuiItem;
import dev.crafty.core.gui.ItemSupplier;
import dev.crafty.core.gui.ItemType;
import dev.crafty.core.gui.Menu;
import dev.crafty.core.util.Lang;
import dev.crafty.core.util.Nullables;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

/**
 * @since 1.0.0
 */
public class MappedItemSupplier extends ItemSupplier {
    public MappedItemSupplier() {
        super(ItemType.MAPPED);
    }

    @Override
    public GuiItem[] apply(ItemStack[] slotsToApplyTo, SectionWrapper itemSection, Menu menu) {
        // Example Section:
        //    type: MAPPED
        //    mapper:
        //      source: "get_all_player_items"
        //      icon-template:
        //        material: PLAYER_HEAD
        //        name: "<green>{item_name}"
        //        lore: ["<gray>Owned item"]
        //        skull-owner: "{item_owner}"
        //      actions:
        //        - "execute_function:handle_paginated_click"
        
        GuiItem[] items = new GuiItem[slotsToApplyTo.length];

        itemSection.getSection("mapper").ifPresent(mapper -> {
            var _source = mapper.getString("source");

            _source.ifPresent(source -> {
                var _item = new ItemStackSerializer().deserialize(mapper, "icon-template");

                _item.ifPresent(item -> {
                    Optional<Menu.MappedItemConfig<?>> _itemConfig = menu.getMappedItemConfig(source);

                    _itemConfig.ifPresent(itemConfig -> {
                        @SuppressWarnings("unchecked")
                        Menu.MappedItemConfig<Object> config = (Menu.MappedItemConfig<Object>) itemConfig;

                        for (int i = 0; i < slotsToApplyTo.length; i++) {
                            Object _itemToMap = config.items().get(i);

                            int finalI = i;
                            Nullables.ifNotNullOrElse(_itemToMap, itemToMap -> {
                               Map<String, String> replacements = config.replacements().apply(itemToMap);
                               ItemStack clone = item.clone();
                               Lang.replaceText(clone, replacements);

                               items[finalI] = new GuiItem(clone, getActions(mapper, menu));
                               slotsToApplyTo[finalI] = items[finalI].item();
                            }, () -> {
                                items[finalI] = GuiItem.empty();
                                slotsToApplyTo[finalI] = items[finalI].item();
                            });
                        }
                    });
                });
            });
        });

        return items;
    }
}
