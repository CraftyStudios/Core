package dev.crafty.core.number.units;

import dev.crafty.core.number.UnitEnum;

/**
 * Time units
 * @since 1.0.0
 */
public enum TimeUnit implements UnitEnum<TimeUnit> {
    MILLISECOND(1),
    SECOND(1000),
    MINUTE(60 * 1000),
    HOUR(60 * 60 * 1000),
    TICK(50)
    ;
    
    private final double toMillisecondsFactor;
    
    TimeUnit(double toMillisecondsFactor) {
        this.toMillisecondsFactor = toMillisecondsFactor;
    }

    @Override
    public Number convert(Number number, TimeUnit targetUnit) {
        double valueInMilliseconds = number.doubleValue() * this.toMillisecondsFactor;
        return valueInMilliseconds / targetUnit.toMillisecondsFactor;
    }
}