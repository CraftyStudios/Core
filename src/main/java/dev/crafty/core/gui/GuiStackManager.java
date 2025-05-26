package dev.crafty.core.gui;

import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages a stack of GUI menus for each player, allowing navigation between menus.
 * When a player opens a new menu, it's added to their stack.
 * The back button allows players to navigate to the previous menu in the stack.
 * If there are no more menus in the stack, the GUI is closed.
 * @since 1.0.20
 */
public class GuiStackManager {
    private static final Map<UUID, Deque<Menu>> stacks = new HashMap<>();
    private static final Map<UUID, Boolean> navigatingBack = new HashMap<>();
    private static final Set<UUID> suppressCloseHandling = new HashSet<>();


    /**
     * Registers a menu in the player's stack.
     * This should be called when a menu is opened.
     *
     * @param player The player viewing the menu
     * @param menu The menu being opened
     */
    public static void registerInStack(Player player, Menu menu) {
        push(player, menu);
    }

    public static void markSuppressClose(Player player) {
        suppressCloseHandling.add(player.getUniqueId());
    }


    /**
     * Navigates back to the previous menu in the stack.
     * If there are no more menus in the stack, closes the current inventory.
     *
     * @param player The player to navigate back for
     */
    public static void navigateBack(Player player) {
        Deque<Menu> stack = stacks.get(player.getUniqueId());
        if (stack != null && stack.size() > 1) {
            navigatingBack.put(player.getUniqueId(), true);

            stack.pop();
            Menu previousMenu = stack.peek();

            // pass true: do not re-stack
            previousMenu.open(true);
        } else {
            if (stack != null) {
                stack.clear();
            }
            player.closeInventory();
        }
    }



    /**
     * Adds a menu to the player's stack.
     *
     * @param player The player
     * @param menu The menu to add
     */
    private static void push(Player player, Menu menu) {
        stacks.computeIfAbsent(player.getUniqueId(), p -> new ArrayDeque<>()).push(menu);
    }

    /**
     * Removes and returns the top menu from the player's stack.
     *
     * @param player The player
     */
    private static void pop(Player player) {
        Deque<Menu> stack = stacks.get(player.getUniqueId());
        if (stack != null && !stack.isEmpty()) {
            stack.pop();
        }
    }

    /**
     * Handles when a player closes an inventory without using the back button.
     * This removes the current menu from the stack.
     *
     * @param player The player who closed the inventory
     */
    public static void handleInventoryClose(Player player) {
        if (suppressCloseHandling.remove(player.getUniqueId())) {
            return;
        }

        if (Boolean.TRUE.equals(navigatingBack.get(player.getUniqueId()))) {
            navigatingBack.remove(player.getUniqueId());
        } else {
            pop(player);
        }
    }


    /**
     * Clears the menu stack for a player.
     * This should be called when a player logs out or when all menus should be closed.
     *
     * @param player The player whose stack should be cleared
     */
    public static void clearStack(Player player) {
        stacks.remove(player.getUniqueId());
        navigatingBack.remove(player.getUniqueId());
    }
}
