package org.jevis.commons.unit;

import org.jevis.commons.unit.dimensions.Currency;
import tech.units.indriya.unit.BaseUnit;
import tech.units.indriya.unit.UnitDimension;

import javax.measure.Unit;

public class Currencies {

    public static final Unit<Currency> EUR = new BaseUnit<>("€", "Euro", UnitDimension.parse('C'));
    public static final Unit<Currency> GBP = new BaseUnit<>("£", "British Pound", UnitDimension.parse('C'));
    public static final Unit<Currency> USD = new BaseUnit<>("$", "US Dollar", UnitDimension.parse('C'));
    public static final Unit<Currency> AUD = new BaseUnit<>("$", "Australian Dollar", UnitDimension.parse('C'));
    public static final Unit<Currency> CAD = new BaseUnit<>("$", "Canadian Dollar", UnitDimension.parse('C'));
    public static final Unit<Currency> YEN = new BaseUnit<>("¥", "Yen", UnitDimension.parse('C'));
    public static final Unit<Currency> CNY = new BaseUnit<>("¥", "Renminbi Yuan", UnitDimension.parse('C'));
    public static final Unit<Currency> KRW = new BaseUnit<>("₩", "Won", UnitDimension.parse('C'));
    public static final Unit<Currency> TWD = new BaseUnit<>("元", "Taiwan Dollar", UnitDimension.parse('C'));
    public static final Unit<Currency> INR = new BaseUnit<>("₹", "Indian Rupee", UnitDimension.parse('C'));
}
