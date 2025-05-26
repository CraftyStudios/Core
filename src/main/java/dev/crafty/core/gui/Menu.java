package dev.crafty.core.gui;

import com.google.common.base.Preconditions;
import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.config.serializer.builtin.ItemStackSerializer;
import dev.crafty.core.plugin.CraftyPlugin;
import dev.crafty.core.util.Components;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @since 1.0.0
 */
public abstract class Menu implements Listener {
    private static final String MENU_FOLDER = "menus/";

    private Inventory inventory;

    protected final String id;
    protected final Player player;
    protected final Plugin plugin;

    private final Map<String, Consumer<InventoryClickEvent>> actionsRegistry = new HashMap<>();
    private final Map<String, Supplier<ItemStack>> itemSuppliers = new HashMap<>();
    private final Map<String, MappedItemConfig<?>> mappedItemConfigs = new HashMap<>();

    private final List<GuiItem> inventoryItems = new ArrayList<>();

    public Menu(String id, Player player, CraftyPlugin plugin) {
        this.id = id;
        this.player = player;
        this.plugin = plugin;

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    protected abstract void setup();

    public void open() {
        setup();

        var inventory = createInventory();
        this.inventory = inventory;
        this.player.openInventory(inventory);
    }

    private Inventory createInventory() {
        YamlConfiguration config = getConfigurationFile();
        Queue<Character> layout = getMenuLayout(config);
        List<MenuItemEntry> items = getMenuItems(config);
        FillConfig fillConfig = getFillConfig(config);

        return createFilledInventory(layout, items, fillConfig, config.getString("title"), this, this.inventoryItems);
    }

    private @NotNull YamlConfiguration getConfigurationFile() {
        File yamlFile = this.plugin.getDataPath().resolve(MENU_FOLDER + this.id + ".yml").toFile();

        if (!yamlFile.exists()) {
            this.plugin.saveResource(MENU_FOLDER + this.id + ".yml", false);
        }

        return YamlConfiguration.loadConfiguration(yamlFile);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(this.inventory)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        this.inventoryItems.stream()
                .filter(guiItem -> guiItem.item().isSimilar(clickedItem))
                .findFirst()
                .ifPresent(item -> item.actions().forEach(action -> action.accept(event)));
    }

    private static FillConfig getFillConfig(YamlConfiguration config) {
        var fillSection = new SectionWrapper(config.getConfigurationSection("fill"));

        var enabled = fillSection.getBoolean("enabled").orElse(false);
        var item = new ItemStackSerializer().deserialize(fillSection, "item").orElse(ItemStack.of(Material.AIR));

        return new FillConfig(enabled, item);
    }

    private static List<MenuItemEntry> getMenuItems(YamlConfiguration config) {
        SectionWrapper itemsSection = new SectionWrapper(config.getConfigurationSection("items"));

        return itemsSection.getKeys(false).stream()
                .map(key -> new MenuItemEntry(
                        key.charAt(0),
                        itemsSection.getSection(key).get(),
                        ItemSupplierRegistry.getSupplier(
                                ItemType.valueOf(itemsSection.getString(key + ".type").orElse("STATIC"))
                        )
                ))
                .toList();
    }

    private static Queue<Character> getMenuLayout(YamlConfiguration config) {
        LinkedList<Character> layout = new LinkedList<>();
        List<String> layoutList = config.getStringList("layout");

        for (String s : layoutList) {
            char[] chars = s.toCharArray();
            for (char c : chars) {
                layout.add(c);
            }
        }

        return layout;
    }


    private static Inventory createFilledInventory(Queue<Character> layout, List<MenuItemEntry> items, FillConfig fillConfig, String title, Menu menu, List<GuiItem> inventoryItems) {
        var baseInv = createBaseInventory(layout, title);

        ItemStack[] inventory = new ItemStack[baseInv.getSize()];
        Character[] layoutChars = layout.toArray(new Character[0]);

        Preconditions.checkArgument(inventory.length == layoutChars.length, "Inventory size does not match layout size");

        for (int i = 0; i < inventory.length; i++) {
            Character c = layoutChars[i];
            if (c != '#') {
                MenuItemEntry item = items.stream().filter(entry -> entry.character() == c).findFirst().orElse(null);
                if (item != null) {
                    // Count how many times 'c' appears in layoutChars
                    int count = (int) Arrays.stream(layoutChars).filter(ch -> Objects.equals(ch, c)).count();
                    ItemStack[] slotsToApplyTo = new ItemStack[count];
                    GuiItem[] guiItems = item.supplier().apply(slotsToApplyTo, item.section(), menu);
                    slotsToApplyTo = Arrays.stream(guiItems).map(GuiItem::item).filter(Objects::nonNull).toArray(ItemStack[]::new);

                    inventoryItems.addAll(Arrays.asList(guiItems));

                    int slotIndex = 0;
                    for (int j = 0; j < layoutChars.length; j++) {
                        if (layoutChars[j] == c && slotIndex < slotsToApplyTo.length) {
                            inventory[j] = slotsToApplyTo[slotIndex++];
                        }
                    }
                } else {
                    inventory[i] = getFillItem(fillConfig);
                }
            } else {
                inventory[i] = getFillItem(fillConfig);
            }
        }

        baseInv.setContents(inventory);
        return baseInv;
    }

    private static int getCountOfCharacter(char c, Queue<Character> layout) {
        return (int) layout.stream().filter(layoutChar -> layoutChar == c).count();
    }

    private static ItemStack getFillItem(FillConfig fillConfig) {
        return fillConfig.enabled ? fillConfig.item() : ItemStack.of(Material.AIR);
    }

    private static Inventory createBaseInventory(Queue<Character> layout, String title) {
        int rows = (int) Math.ceil(layout.size() / 9.0);
        return Bukkit.createInventory(null, rows * 9, Components.fromString(title));
    }

    protected void registerAction(String id, Consumer<InventoryClickEvent> action) {
        this.actionsRegistry.put(id, action);
    }

    public Consumer<InventoryClickEvent> getAction(String id) {
        return this.actionsRegistry.get(id);
    }

    protected void registerItemSupplier(String id, Supplier<ItemStack> supplier) {
        this.itemSuppliers.put(id, supplier);
    }

    public Supplier<ItemStack> getItemSupplier(String id) {
        return this.itemSuppliers.get(id);
    }

    public void addItem(GuiItem item) {
        this.inventoryItems.add(item);
    }

    public void addItems(GuiItem... items) {
        this.inventoryItems.addAll(Arrays.asList(items));
    }

    public Player getViewer() {
        return this.player;
    }

    protected <T> void registerMappedItemConfig(String id, Function<T, Map<String, String>> replacements, List<T> items) {
        this.mappedItemConfigs.put(id, new MappedItemConfig<>(replacements, items));
    }

    public Optional<MappedItemConfig<?>> getMappedItemConfig(String id) {
        return Optional.ofNullable(this.mappedItemConfigs.get(id));
    }

    private record MenuItemEntry(char character, SectionWrapper section, ItemSupplier supplier) { }

    public record MappedItemConfig<T>(Function<T, Map<String, String>> replacements, List<T> items) {}

    private record FillConfig(boolean enabled, ItemStack item) {}
}
