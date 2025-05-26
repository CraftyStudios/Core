package dev.crafty.core.items.serializers;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.ConfigSerializer;
import dev.crafty.core.items.ItemCraftingRecipe;
import dev.crafty.core.items.ItemCraftingRecipeItem;
import org.bukkit.Material;

import java.util.List;
import java.util.Optional;

public class ItemCraftingRecipeSerializer implements ConfigSerializer<ItemCraftingRecipe> {
    @Override
    public void serialize(SerializationArgs<ItemCraftingRecipe> args) {
        SectionWrapper section = args.section().getOrCreateSection(args.path());
        ItemCraftingRecipe recipe = args.value();
        section.set("shape", recipe.shape());

        section.set("ingredients", recipe.ingredients().stream()
                .map(ingredient -> ingredient.c() + ":" + ingredient.material().name())
                .toList());
    }

    @Override
    public Optional<ItemCraftingRecipe> deserialize(SectionWrapper section, String path) {
        var _shape = section.getStringList(path + ".shape");
        if (_shape.isEmpty()) {
            return Optional.empty();
        }
        var shape = _shape.get();
        List<ItemCraftingRecipeItem> ingredients = section.getStringList(path + ".ingredients")
                .orElse(List.of())
                .stream()
                .map(ingredient -> {
                    String[] parts = ingredient.split(":");
                    return new ItemCraftingRecipeItem(parts[0].charAt(0), Material.valueOf(parts[1]));
                })
                .toList();

        return Optional.of(new ItemCraftingRecipe(shape, ingredients));
    }

    @Override
    public Class<ItemCraftingRecipe> getType() {
        return ItemCraftingRecipe.class;
    }
}
