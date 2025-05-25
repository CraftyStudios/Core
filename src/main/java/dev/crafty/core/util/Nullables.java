package dev.crafty.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Nullables {
    public static <T> @NotNull T orElse(T value, T orElse) {
        return value == null ? orElse : value;
    }

    public static <T> void ifNotNull(T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    public static <T> void ifNotNullOrElse(T value, Consumer<T> consumer, Runnable orElse) {
        if (value != null) {
            consumer.accept(value);
        } else {
            orElse.run();
        }
    }
}
