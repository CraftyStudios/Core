package dev.crafty.core.items;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ItemAction(String actionId, @Nullable List<ItemActionCondition> conditions) {
}
