package org.jevis.commons.dataprocessing;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.commons.i18n.I18n;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public enum AggregationPeriod {

    NONE, MINUTELY, QUARTER_HOURLY, HOURLY, DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, THREEYEARS, FIVEYEARS, TENYEARS, CUSTOM, CUSTOM2;

    public static Period getJodaPeriod(AggregationPeriod aggregationPeriod) {
        Period period = Period.ZERO;

        switch (aggregationPeriod) {
            default:
            case NONE:
                break;
            case MINUTELY:
                period = Period.minutes(1);
                break;
            case QUARTER_HOURLY:
                period = Period.minutes(15);
                break;
            case HOURLY:
                period = Period.hours(1);
                break;
            case DAILY:
                period = Period.days(1);
                break;
            case WEEKLY:
                period = Period.weeks(1);
                break;
            case MONTHLY:
                period = Period.months(1);
                break;
            case QUARTERLY:
                period = Period.months(3);
                break;
            case YEARLY:
                period = Period.years(1);
                break;
            case THREEYEARS:
                period = Period.years(3);
                break;
            case FIVEYEARS:
                period = Period.years(5);
                break;
            case TENYEARS:
                period = Period.years(10);
                break;
        }

        return period;
    }

    public boolean isGreaterThenDays() {
        AggregationPeriod aggregationPeriod = this;
        return aggregationPeriod == AggregationPeriod.DAILY || aggregationPeriod == AggregationPeriod.WEEKLY || aggregationPeriod == AggregationPeriod.MONTHLY
                || aggregationPeriod == AggregationPeriod.QUARTERLY || aggregationPeriod == AggregationPeriod.YEARLY || aggregationPeriod == AggregationPeriod.THREEYEARS
                || aggregationPeriod == AggregationPeriod.FIVEYEARS || aggregationPeriod == AggregationPeriod.TENYEARS;
    }

    public static AggregationPeriod get(String modeName) {
        String[] modeArray = modeName.split("_");
        String mode = NONE.name();
        if (modeArray.length == 2) {
            if (modeArray[0].equalsIgnoreCase("QUARTER")) {
                mode = modeArray[0] + "_" + modeArray[1];
            } else {
                mode = modeArray[0];
            }
        } else {
            mode = modeArray[0];
        }
        return valueOf(mode);
    }

    public static AggregationPeriod parseAggregation(String aggregation) {
        switch (aggregation) {
            case ("Minute"):
            case ("MINUTE"):
            case ("Minutely"):
            case ("MINUTELY"):
                return MINUTELY;
            case ("Quarter Hour"):
            case ("Quarter Hourly"):
            case ("Quarterly Hour"):
            case ("Quarterly Hourly"):
            case ("QUARTER_HOUR"):
            case ("QUARTER_HOURLY"):
            case ("QUARTERLY_HOURLY"):
                return QUARTER_HOURLY;
            case ("HOUR"):
            case ("HOURLY"):
            case ("Hour"):
            case ("Hourly"):
                return HOURLY;
            case ("DAY"):
            case ("DAILY"):
            case ("Day"):
            case ("Daily"):
                return DAILY;
            case ("WEEK"):
            case ("WEEKLY"):
            case ("Week"):
            case ("Weekly"):
                return WEEKLY;
            case ("MONTH"):
            case ("MONTHLY"):
            case ("Month"):
            case ("Monthly"):
                return MONTHLY;
            case ("QUARTER"):
            case ("QUARTERLY"):
            case ("Quarter"):
            case ("Quarterly"):
                return QUARTERLY;
            case ("YEAR"):
            case ("YEARLY"):
            case ("Year"):
            case ("Yearly"):
                return YEARLY;
            case ("THREEYEARS"):
            case ("THREE_YEARS"):
            case ("Threeyears"):
            case ("Three years"):
                return THREEYEARS;
            case ("FIVEYEARS"):
            case ("FIVE_YEARS"):
            case ("Fiveyears"):
            case ("Five years"):
                return FIVEYEARS;
            case ("TENYEARS"):
            case ("TEN_YEARS"):
            case ("Tenyears"):
            case ("Ten years"):
                return TENYEARS;
            case ("CUSTOM"):
            case ("Custom"):
                return CUSTOM;
            case ("CUSTOM2"):
            case ("Custom2"):
                return CUSTOM2;
            case ("NONE"):
            case ("None"):
            default:
                return NONE;

        }
    }

    public static Integer parseAggregationIndex(AggregationPeriod aggregationPeriod) {
        if (aggregationPeriod != null) {
            switch (aggregationPeriod) {
                case MINUTELY:
                    return 1;
                case QUARTER_HOURLY:
                    return 2;
                case HOURLY:
                    return 3;
                case DAILY:
                    return 4;
                case WEEKLY:
                    return 5;
                case MONTHLY:
                    return 6;
                case QUARTERLY:
                    return 7;
                case YEARLY:
                    return 8;
                default:
                case NONE:
                    return 0;
            }
        } else return 0;
    }

    public static AggregationPeriod parseAggregationIndex(Integer aggregationIndex) {
        switch (aggregationIndex) {
            case (1):
                return MINUTELY;
            case (2):
                return QUARTER_HOURLY;
            case (3):
                return HOURLY;
            case (4):
                return DAILY;
            case (5):
                return WEEKLY;
            case (6):
                return MONTHLY;
            case (7):
                return QUARTERLY;
            case (8):
                return YEARLY;
            case (0):
            default:
                return NONE;
        }
    }

    public static ObservableList<String> getListNamesAggregationPeriods() {
        List<String> tempList = new ArrayList<>();

        for (AggregationPeriod aggregationPeriod : AggregationPeriod.values()) {
            switch (aggregationPeriod) {
                case MINUTELY:
                    tempList.add(I18n.getInstance().getString("plugin.unit.samplingrate.everyminute"));
                    break;
                case QUARTER_HOURLY:
                    tempList.add(I18n.getInstance().getString("plugin.graph.interval.quarterhourly"));
                    break;
                case HOURLY:
                    tempList.add(I18n.getInstance().getString("plugin.graph.interval.hourly"));
                    break;
                case DAILY:
                    tempList.add(I18n.getInstance().getString("plugin.graph.interval.daily"));
                    break;
                case WEEKLY:
                    tempList.add(I18n.getInstance().getString("plugin.graph.interval.weekly"));
                    break;
                case MONTHLY:
                    tempList.add(I18n.getInstance().getString("plugin.graph.interval.monthly"));
                    break;
                case QUARTERLY:
                    tempList.add(I18n.getInstance().getString("plugin.graph.interval.quarterly"));
                    break;
                case YEARLY:
                    tempList.add(I18n.getInstance().getString("plugin.graph.interval.yearly"));
                    break;
                case NONE:
                    tempList.add(I18n.getInstance().getString("plugin.graph.interval.preset"));
                    break;
            }
        }
        return FXCollections.observableArrayList(tempList);
    }

    public org.jevis.commons.datetime.Period toPeriod() {
        switch (this) {
            default:
            case NONE:
                return org.jevis.commons.datetime.Period.NONE;
            case MINUTELY:
                return org.jevis.commons.datetime.Period.MINUTELY;
            case QUARTER_HOURLY:
                return org.jevis.commons.datetime.Period.QUARTER_HOURLY;
            case HOURLY:
                return org.jevis.commons.datetime.Period.HOURLY;
            case DAILY:
                return org.jevis.commons.datetime.Period.DAILY;
            case WEEKLY:
                return org.jevis.commons.datetime.Period.WEEKLY;
            case MONTHLY:
                return org.jevis.commons.datetime.Period.MONTHLY;
            case QUARTERLY:
                return org.jevis.commons.datetime.Period.QUARTERLY;
            case YEARLY:
                return org.jevis.commons.datetime.Period.YEARLY;

        }
    }
}