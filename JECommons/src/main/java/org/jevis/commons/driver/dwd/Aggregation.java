package org.jevis.commons.driver.dwd;

public enum Aggregation {
    TEN_MINUTES, ONE_MINUTE, FIVE_MINUTES, ANNUAL, DAILY, HOURLY, MONTHLY, MULTI_ANNUAL, SUBDAILY;


    @Override
    public String toString() {
        switch (this) {
            case TEN_MINUTES:
                return "10_minutes";
            case ONE_MINUTE:
                return "1_minute";
            case FIVE_MINUTES:
                return "5_minutes";
            case ANNUAL:
                return "annual";
            case DAILY:
                return "daily";
            case HOURLY:
                return "hourly";
            case MONTHLY:
                return "monthly";
            case MULTI_ANNUAL:
                return "multi_annual";
            case SUBDAILY:
                return "subdaily";
        }
        return "hourly";
    }
}
