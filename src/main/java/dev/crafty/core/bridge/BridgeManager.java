package dev.crafty.core.bridge;

import dev.crafty.core.CraftyCore;

import java.util.*;

/**
 * Manages the current bridges
 * @since 1.0.0
 */
public class BridgeManager {
    private static final Map<Class<? extends Bridge>, List<Bridge>> bridges = new HashMap<>();

    public static <T extends Bridge> void registerBridge(Class<T> clazz, T bridge) {
        List<Bridge> bridges = BridgeManager.bridges.get(clazz);

        if (bridges == null) {
            bridges = new ArrayList<>();
        }

        if (!bridge.canRegister()) {
            return;
        }

        bridges.add(bridge);

        BridgeManager.bridges.put(clazz, bridges);
    }

    public static <T extends Bridge> T getBridge(Class<T> clazz) {
        List<Bridge> registered = bridges.get(clazz);
        if (registered == null || registered.isEmpty()) {
            return null;
        }

        String bridgeName = registered.getFirst().bridgeName().toLowerCase();
        List<String> priority = CraftyCore.INSTANCE.getConfig().getStringList("bridges." + bridgeName + ".priority");

        for (String name : priority) {
            for (Bridge bridge : registered) {
                if (bridge.getName().equalsIgnoreCase(name)) {
                    return clazz.cast(bridge);
                }
            }
        }

        return clazz.cast(registered.getFirst());
    }

    public static boolean isBridgePresent(Class<? extends Bridge> clazz) {
        return bridges.containsKey(clazz);
    }
}
