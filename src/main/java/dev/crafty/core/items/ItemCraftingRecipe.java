package dev.crafty.core.items;

import java.util.List;

public record ItemCraftingRecipe(List<String> shape, List<ItemCraftingRecipeItem> ingredients) {
}
