package dev.crafty.core.items;

import dev.crafty.core.CraftyCore;
import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.SerializerRegistry;
import dev.crafty.core.config.serializer.builtin.ItemStackSerializer;
import dev.crafty.core.items.serializers.ItemActionSerializer;
import dev.crafty.core.items.serializers.ItemCraftingRecipeSerializer;
import dev.crafty.core.plugin.CraftyPlugin;
import dev.crafty.core.util.Lang;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Abstract base class for custom items in the CraftyCore plugin.
 * Handles item loading, crafting recipe registration, and action handling.
 *
 * @since 1.0.26
 */
@Getter
public abstract class Item implements Listener {
    private static final String ITEM_FOLDER = "items/";
    public static final NamespacedKey ITEM_KEY = new NamespacedKey("CraftyCore", "item_id");

    private final String id;
    private ItemStack item;
    private ItemCraftingRecipe craftingRecipe;
    private List<ItemAction> itemActions = new ArrayList<>();
    private final CraftyPlugin plugin;

    private final Map<String, BiConsumer<PlayerInteractEvent, ItemStack>> actions = new HashMap<>();

    /**
     * Constructs a new custom item.
     *
     * @param id     The unique identifier for this item.
     * @param plugin The plugin instance.
     */
    public Item(String id, CraftyPlugin plugin) {
        this.id = id;
        this.plugin = plugin;

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

        loadItem();
        registerActions();
    }

    /**
     * Registers a custom action for this item.
     *
     * @param actionId The action identifier.
     * @param action   The action logic as a BiConsumer.
     */
    protected void registerAction(String actionId, BiConsumer<PlayerInteractEvent, ItemStack> action) {
        this.actions.put(actionId, action);
    }

    protected abstract void registerActions();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isRelevantItem(event)) {
            return;
        }

        for (ItemAction action : this.itemActions) {
            if (!validateConditions(action, event)) {
                return;
            }
            executeAction(action, event);
        }
    }

    private boolean isRelevantItem(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() == Material.AIR) {
            return false;
        }
        ItemMeta meta = event.getItem().getItemMeta();

        if (meta == null || !meta.getPersistentDataContainer().has(ITEM_KEY, PersistentDataType.STRING)) {
            return false;
        }

        String itemId = meta.getPersistentDataContainer().get(ITEM_KEY, PersistentDataType.STRING);

        if (itemId == null || itemId.isEmpty()) {
            return false;
        }

        return itemId.equals(this.id);
    }

    private boolean validateConditions(ItemAction action, PlayerInteractEvent event) {
        if (action.conditions() != null) {
            for (ItemActionCondition condition : action.conditions()) {
                if (!canExecuteCondition(condition, event)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canExecuteCondition(ItemActionCondition condition, PlayerInteractEvent event) {
        if (!condition.canExecute(event.getPlayer())) {
            sendDenyMessageIfPresent(condition, event);
            return false;
        }
        return true;
    }

    private void sendDenyMessageIfPresent(ItemActionCondition condition, PlayerInteractEvent event) {
        if (!condition.denyMessage().isEmpty()) {
            event.getPlayer().sendMessage(Lang.colorize(condition.denyMessage()));
        }
    }

    private void executeAction(ItemAction action, PlayerInteractEvent event) {
        if (this.actions.containsKey(action.actionId())) {
            this.actions.get(action.actionId()).accept(event, this.item);
        } else {
            plugin.logger.warn("No action registered for item action: " + action.actionId() + " in item: " + this.id);
        }
    }

    private void loadItem() {
        YamlConfiguration configuration = getConfigurationFile();
        var configurationSection = configuration.getConfigurationSection("");

        if (configurationSection == null) {
            throw new IllegalStateException("Configuration section is null for item: " + this.id);
        }

        boolean craftable = configurationSection.getBoolean("craftable", true);

        ItemStackSerializer serializer = getItemStackSerializer();
        this.item = deserializeItemStack(serializer, configurationSection);

        ItemCraftingRecipeSerializer craftingSerializer = getCraftingRecipeSerializer();
        this.craftingRecipe = deserializeCraftingRecipe(craftingSerializer, configurationSection);

        ItemActionSerializer actionSerializer = getItemActionSerializer();
        this.itemActions = deserializeItemActions(actionSerializer, configurationSection);

        plugin.logger.info("Loaded item: " + this.id);

        if (this.craftingRecipe != null && craftable) {
            setupRecipe();
        } else {
            Bukkit.removeRecipe(new NamespacedKey(CraftyCore.INSTANCE, "crafting_recipe_" + this.id));
        }
    }

    private void setupRecipe() {
        NamespacedKey recipeName = new NamespacedKey(CraftyCore.INSTANCE, "crafting_recipe_" + this.id);

        ShapedRecipe recipe = new ShapedRecipe(recipeName, getItem());

        recipe.shape(this.craftingRecipe.shape().getFirst(),
                     this.craftingRecipe.shape().get(1),
                     this.craftingRecipe.shape().getLast()
        );

        for (ItemCraftingRecipeItem ingredient : this.craftingRecipe.ingredients()) {
            recipe.setIngredient(ingredient.c(), ingredient.material());
        }

        Bukkit.removeRecipe(recipeName); // prevent duplicate recipes
        Bukkit.addRecipe(recipe);
        plugin.logger.info("Registered crafting recipe for item: " + this.id);
    }

    /**
     * Returns the ItemStack representing this custom item.
     * Applies metadata if necessary.
     *
     * @return The ItemStack for this item.
     * @throws IllegalStateException if the item is null or AIR.
     */
    public ItemStack getItem() {
        if (this.item == null || this.item.getType() == Material.AIR) {
            throw new IllegalStateException("ItemStack is null or AIR for item: " + this.id);
        }
        applyMetadata();
        return this.item;
    }

    private void applyMetadata() {
        if (this.item == null || this.item.getType() == Material.AIR) {
            throw new IllegalStateException("ItemStack is null or AIR for item: " + this.id);
        }

        ItemMeta meta = this.item.getItemMeta();

        meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, this.id);

        item.setItemMeta(meta);
    }

    private ItemStackSerializer getItemStackSerializer() {
        var _serializer = SerializerRegistry.getSerializer(ItemStack.class);
        if (_serializer.isEmpty() || !(_serializer.get() instanceof ItemStackSerializer serializer)) {
            throw new IllegalStateException("ItemStackSerializer is not registered or is not an instance of ItemStackSerializer");
        }
        return serializer;
    }

    private ItemStack deserializeItemStack(ItemStackSerializer serializer, ConfigurationSection section) {
        var _item = serializer.deserialize(new SectionWrapper(section), "item");
        if (_item.isEmpty() || _item.get().getType() == Material.AIR) {
            throw new IllegalStateException("ItemStack is null or AIR for item: " + this.id);
        }
        return _item.get();
    }

    private ItemCraftingRecipeSerializer getCraftingRecipeSerializer() {
        var _craftingSerializer = SerializerRegistry.getSerializer(ItemCraftingRecipe.class);
        if (_craftingSerializer.isEmpty() || !(_craftingSerializer.get() instanceof ItemCraftingRecipeSerializer craftingSerializer)) {
            throw new IllegalStateException("ItemCraftingRecipeSerializer is not registered or is not an instance of ConfigSerializer<ItemCraftingRecipe>");
        }
        return craftingSerializer;
    }

    private ItemCraftingRecipe deserializeCraftingRecipe(ItemCraftingRecipeSerializer serializer, ConfigurationSection section) {
        var _craftingRecipe = serializer.deserialize(new SectionWrapper(section), "crafting_recipe");
        return _craftingRecipe.orElse(null);
    }

    private ItemActionSerializer getItemActionSerializer() {
        var _serializer = SerializerRegistry.getSerializer(ItemAction.class);
        if (_serializer.isEmpty() || !(_serializer.get() instanceof ItemActionSerializer serializer)) {
            throw new IllegalStateException("ItemActionSerializer is not registered or is not an instance of ItemActionSerializer");
        }
        return serializer;
    }

    private List<ItemAction> deserializeItemActions(ItemActionSerializer serializer, ConfigurationSection section) {
        var _actionsSection = section.getConfigurationSection("actions");
        if (_actionsSection == null) {
            return new ArrayList<>();
        }

        return _actionsSection.getKeys(false).stream()
                .map(key -> serializer.deserialize(new SectionWrapper(_actionsSection), key).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private @NotNull YamlConfiguration getConfigurationFile() {
        File yamlFile = this.plugin.getDataPath().resolve(ITEM_FOLDER + this.id + ".yml").toFile();

        if (!yamlFile.exists()) {
            this.plugin.saveResource(ITEM_FOLDER + this.id + ".yml", false);
        }

        return YamlConfiguration.loadConfiguration(yamlFile);
    }
}
