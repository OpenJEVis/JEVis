package org.jevis.commons.unit;

import javax.measure.Prefix;
import javax.measure.Quantity;
import javax.measure.Unit;

public enum CustomPrefix implements Prefix {
    NONE("", 0);

    private final String symbol;
    private final int exponent;

    CustomPrefix(String symbol, int exponent) {
        this.symbol = symbol;
        this.exponent = exponent;
    }

    public static <Q extends Quantity<Q>> Unit<Q> NONE(Unit<Q> unit) {
        return unit.prefix(NONE);
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public Number getValue() {
        return 10;
    }

    @Override
    public int getExponent() {
        return exponent;
    }
}
