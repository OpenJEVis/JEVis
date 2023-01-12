package org.jevis.dwddriver;

import org.jevis.commons.driver.dwd.Aggregation;
import org.jevis.commons.driver.dwd.Attribute;

import java.util.ArrayList;
import java.util.List;

public class DataStructure {


    private final List<String> allPaths = new ArrayList<>();

    public DataStructure() {

        createAllPaths();
    }

    private void createAllPaths() {
        for (Aggregation aggregation : Aggregation.values()) {

            for (Attribute attribute : Attribute.values()) {
                String s = aggregation.toString().toLowerCase() + "/" + attribute.toString().toLowerCase() + "/";
                switch (aggregation) {
                    case TEN_MINUTES:
                        if (attribute == Attribute.AIR_TEMPERATURE || attribute == Attribute.EXTREME_TEMPERATURE
                                || attribute == Attribute.EXTREME_WIND || attribute == Attribute.PRECIPITATION
                                || attribute == Attribute.SOLAR || attribute == Attribute.WIND) {
                            allPaths.add(s);
                        }
                        break;
                    case ONE_MINUTE:
                        if (attribute == Attribute.PRECIPITATION) {
                            allPaths.add(s);
                        }
                        break;
                    case FIVE_MINUTES:
                        if (attribute == Attribute.PRECIPITATION) {
                            allPaths.add(s);
                        }
                        break;
                    case ANNUAL:
                        if (attribute == Attribute.WEATHER_PHENOMENA) {
                            allPaths.add(s);
                        }
                        break;
                    case DAILY:
                        if (attribute == Attribute.SOIL_TEMPERATURE || attribute == Attribute.SOLAR || attribute == Attribute.WEATHER_PHENOMENA) {
                            allPaths.add(s);
                        }
                        break;
                    case HOURLY:
                        if (attribute == Attribute.AIR_TEMPERATURE || attribute == Attribute.CLOUD_TYPE
                                || attribute == Attribute.CLOUDINESS || attribute == Attribute.DEW_POINT
                                || attribute == Attribute.EXTREME_WIND || attribute == Attribute.MOISTURE
                                || attribute == Attribute.PRECIPITATION || attribute == Attribute.PRESSURE
                                || attribute == Attribute.SOIL_TEMPERATURE || attribute == Attribute.SOLAR
                                || attribute == Attribute.SUN || attribute == Attribute.VISIBILITY
                                || attribute == Attribute.WEATHER_PHENOMENA || attribute == Attribute.WIND) {
                            allPaths.add(s);
                        }
                        break;
                    case MONTHLY:
                        if (attribute == Attribute.WEATHER_PHENOMENA) {
                            allPaths.add(s);
                        }
                        break;
                    case MULTI_ANNUAL:
                        break;
                    case SUBDAILY:
                        if (attribute == Attribute.AIR_TEMPERATURE
                                || attribute == Attribute.CLOUDINESS || attribute == Attribute.EXTREME_WIND
                                || attribute == Attribute.MOISTURE || attribute == Attribute.PRESSURE
                                || attribute == Attribute.SOIL || attribute == Attribute.VISIBILITY
                                || attribute == Attribute.WIND) {
                            allPaths.add(s);
                        }
                        break;
                }
            }
        }
    }

    public String getPath(Aggregation aggregation, Attribute attribute) {
        String path = "";

        String s = aggregation.toString().toLowerCase() + "/" + attribute.toString().toLowerCase() + "/";
        switch (aggregation) {
            case TEN_MINUTES:
                switch (attribute) {
                    case AIR_TEMPERATURE:
                    case EXTREME_TEMPERATURE:
                    case EXTREME_WIND:
                    case PRECIPITATION:
                    case SOLAR:
                    case WIND:
                        path = s;
                        break;
                }
                break;
            case ONE_MINUTE:
                if (attribute == Attribute.PRECIPITATION) {
                    path = s;
                }
                break;
            case FIVE_MINUTES:
                if (attribute == Attribute.PRECIPITATION) {
                    path = s;
                }
                break;
            case ANNUAL:
                if (attribute == Attribute.WEATHER_PHENOMENA) {
                    path = s;
                }
                break;
            case DAILY:
                switch (attribute) {
                    case SOIL_TEMPERATURE:
                    case SOLAR:
                    case WEATHER_PHENOMENA:
                        path = s;
                        break;
                }
                break;
            case HOURLY:
                switch (attribute) {
                    case AIR_TEMPERATURE:
                    case CLOUD_TYPE:
                    case CLOUDINESS:
                    case DEW_POINT:
                    case EXTREME_WIND:
                    case MOISTURE:
                    case PRECIPITATION:
                    case PRESSURE:
                    case SOIL_TEMPERATURE:
                    case SOLAR:
                    case SUN:
                    case VISIBILITY:
                    case WEATHER_PHENOMENA:
                    case WIND:
                        path = s;
                        break;
                }
                break;
            case MONTHLY:
                if (attribute == Attribute.WEATHER_PHENOMENA) {
                    path = s;
                }
                break;
            case MULTI_ANNUAL:
                break;
            case SUBDAILY:
                switch (attribute) {
                    case AIR_TEMPERATURE:
                    case CLOUDINESS:
                    case EXTREME_WIND:
                    case MOISTURE:
                    case PRESSURE:
                    case SOIL:
                    case VISIBILITY:
                    case WIND:
                        path = s;
                }
                break;
        }

        return path;

    }

}
