package dev.crafty.core.number;

/**
 * Interface for enums that represent a unit
 * @since 1.0.0
 * @param <T> The type of unit enum
 */
public interface UnitEnum<T extends Enum<T>> {
    /**
     * Converts a number from this unit to the target unit.
     *
     * @param number The number to convert
     * @param unit The unit to convert to
     * @return The converted number
     */
    Number convert(Number number, T unit);
}
