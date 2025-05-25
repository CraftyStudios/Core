package dev.crafty.core.number;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a number with a specific unit.
 * @since 1.0.0
 * @param <T> The type of unit enum
 */
public record UnitNumber<T extends Enum<T> & UnitEnum<T>>(Number value, T unit) {

    /**
     * Creates a new UnitNumber with the specified value and unit.
     *
     * @param value The numeric value
     * @param unit The unit of the value
     */
    public UnitNumber {
    }

    /**
     * Converts this UnitNumber to a different unit.
     * 
     * @param targetUnit The unit to convert to
     * @return A new UnitNumber with the converted value and target unit
     */
    public UnitNumber<T> convertTo(T targetUnit) {
        if (targetUnit == unit) {
            return this;
        }

        Number convertedValue = unit.convert(value, targetUnit);
        return new UnitNumber<>(convertedValue, targetUnit);
    }

    @Override
    public @NotNull String toString() {
        return value + " " + unit;
    }

    public static <T extends Enum<T> & UnitEnum<T>> UnitNumber<T> of(Number value, T unit) {
        return new UnitNumber<>(value, unit);
    }
}
