package org.jevis.commons.unit.ChartUnits;

public enum MoneyUnit {
    EUR("EUR"),
    USD("USD"),
    GBP("GBP"),
    JPY("HPY"),
    AUD("AUD"),
    CAD("CAD"),
    CNY("CNY"),
    KRW("KRW"),
    TWD("TWD");


    private final String name;

    MoneyUnit(String s) {
        this.name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
