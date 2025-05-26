package dev.crafty.core.config.serializer.builtin;

// Removed unused imports
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.crafty.core.CraftyCore;
import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.ConfigSerializer;
import dev.crafty.core.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
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

               if (material == Material.PLAYER_HEAD) {
                   handleSkull(section, item);
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

    private void handleSkull(SectionWrapper itemSection, ItemStack item) {
        var _skullUrl = itemSection.getString("skull-url");

        _skullUrl.ifPresent(skullUrl -> {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();

            try {
                textures.setSkin(new URL(skullUrl));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            profile.setTextures(textures);

            meta.setOwnerProfile(profile);
            item.setItemMeta(meta);
        });
    }

}