package dev.crafty.core.config.serializer.builtin;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.ConfigSerializer;
import dev.crafty.core.util.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Serializer for ItemStack objects.
 * This serializer handles converting ItemStack objects to and from configuration sections.
 *
 * @since 1.0.0
 */
public class ItemStackSerializer implements ConfigSerializer<ItemStack> {

    @Override
    public void serialize(SerializationArgs<ItemStack> args) {
        var value = args.value();
        var section = args.section();
        var path = args.path();

        if (value == null) {
            section.set(path, null);
            return;
        }
        
        section.set(path, value);

        save(args);
    }

    @Override
    public Optional<ItemStack> deserialize(SectionWrapper sectionWrapper, String path) {
        if (!sectionWrapper.contains(path)) {
            return Optional.empty();
        }

        var itemSection = sectionWrapper.getSection(path);

        AtomicReference<ItemStack> itemStack = new AtomicReference<>();

        itemSection.ifPresent(section -> {
           var configName = section.getString("name");
           var configMaterialString = section.getString("material");
           var configLore = section.getStringList("lore");
           boolean glow = Optionals.isOptionalTrue(section.getBoolean("glow"));

           Optionals.ifAllPresent(configName, configMaterialString, (name, materialString) -> {
                Material material = Nullables.orElse(
                        Material.matchMaterial(materialString),
                        Material.STONE
                );

                ItemStack item = ItemStack.of(material);
                item.editMeta(meta -> {
                    meta.displayName(Components.fromString(name));
                    configLore.ifPresent(lore -> meta.lore(Components.fromStringList(configLore.get())));
                });

                if (glow) {
                    Items.setItemGlow(item);
                }

                itemStack.set(item);
           });
        });

        return Optional.ofNullable(itemStack.get());
    }

    @Override
    public Class<ItemStack> getType() {
        return ItemStack.class;
    }
}