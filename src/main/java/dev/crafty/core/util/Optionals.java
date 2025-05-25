package dev.crafty.core.util;

import java.util.Optional;
import java.util.function.BiConsumer;

public class Optionals {
    public static boolean isOptionalTrue(Optional<?> optional) {
        return optional.isPresent() && (boolean) optional.get();
    }

    public static <A, B> void ifAllPresent(Optional<A> a, Optional<B> b, BiConsumer<A, B> consumer) {
        if (areAllPresent(a, b)) {
            consumer.accept(a.get(), b.get());
        }
    }

    public static <A, B, C> void ifAllPresent(Optional<A> a, Optional<B> b, Optional<C> c, TriConsumer<A, B, C> consumer) {
        if (areAllPresent(a, b, c)) {
            consumer.accept(a.get(), b.get(), c.get());
        }
    }

    public static <A, B, C, D> void ifAllPresent(Optional<A> a, Optional<B> b, Optional<C> c, Optional<D> d, QuadConsumer<A, B, C, D> consumer) {
        if (areAllPresent(a, b, c, d)) {
            consumer.accept(a.get(), b.get(), c.get(), d.get());
        }
    }

    public static boolean areAllPresent(Optional<?>... optionals) {
        for (Optional<?> optional : optionals) {
            if (optional.isEmpty()) {
                return false;
            }
        }
        
        return true;
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
    
    @FunctionalInterface
    public interface QuadConsumer<A, B, C, D> {
        void accept(A a, B b, C c, D d);
    }
}
