package org.jevis.commons.driver.dwd;

public enum Aggregation {
    TEN_MINUTES("10_minutes"),
    ONE_MINUTE("1_minute"),
    FIVE_MINUTES("5_minutes"),
    ANNUAL("annual"),
    DAILY("daily"),
    HOURLY("hourly"),
    MONTHLY("monthly"),
    MULTI_ANNUAL("multi_annual"),
    SUBDAILY("subdaily");

    private final String value;

    Aggregation(String value) {
        this.value = value;
    }

    public static Aggregation getEnum(String value) {
        for (Aggregation v : values())
            if (v.getValue().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }
}
