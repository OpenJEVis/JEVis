/*
 * Copyright (C) 2016 AI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jevis.wdparser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.jevis.wdparser.JEVisDWDWDParser.NODATA;

/**
 * @author Artur Iablokov
 */
public class DWDWDDataPoint {

    private static final int MAPSIZE = 7;
    public static String NAME = "DWD Data Point";
    //couples - the option name and its column number in the dwd table
    static Map<String, String> WEATHEROPTIONS = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            put(DWDDataPointAttribute.HEIGHT, "HÖHE");
            put(DWDDataPointAttribute.ATMOPRESSURE, "LUFTD.");
            put(DWDDataPointAttribute.TEMPERATURE, "TEMP.");
            put(DWDDataPointAttribute.TMIN, "T-Min");
            put(DWDDataPointAttribute.TMAX, "T-Max");
            put(DWDDataPointAttribute.HUMIDITY, "U%");
            put(DWDDataPointAttribute.PRECIPITATION, "RR1");
            put(DWDDataPointAttribute.PRECIPITATION12, "RR12");
            put(DWDDataPointAttribute.SNOWHEIGHT, "SSS");
            put(DWDDataPointAttribute.WINDDIRECTION, "DD");
            put(DWDDataPointAttribute.WINDSPEED, "FF");
            put(DWDDataPointAttribute.WINDPEAKS, "FX");
            put(DWDDataPointAttribute.CLIMATECLOUDS, "Wetter+Wolken");
            put(DWDDataPointAttribute.SQUALL, "Böen");
        }
    });
    static Map<String, String> ALTERNATIVE_WEATHEROPTION_NAMES = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            put("RR1", "RR30");
            put("RR30", "RR1");
        }
    });
    static Map<String, String> ALTERNATIVE_CITY_NAME = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            put("Münster", "Münster/Osnabr.-Flh.");
            put("Osnabrück", "Münster/Osnabr.-Flh.");
            put("Köln", "Köln/Bonn-Flh.");
            put("Bonn", "Köln/Bonn-Flh.");
            put("Gießen", "Giessen");
            put("Wasserkuppe", "Waßerkuppe");
            put("Frankfurt am Main", "Frankfurt/M-Flh.");
            put("Hohenpeißenberg", "Hohenpeissenberg");
        }
    });
    private final Map<String, String> _targets = new HashMap<>(MAPSIZE);
    private String _city;

    /**
     * get city
     *
     * @return _city
     */
    public String getCity() {
        return _city;
    }

    /**
     * set _city
     *
     * @param city
     */
    public void setCity(String city) {
        _city = city;
    }

    /**
     * set height target to target map
     *
     * @param heightTarget
     */
    public void setHeightTarget(String heightTarget) {
        setValue(heightTarget, DWDWDDataPoint.DWDDataPointAttribute.HEIGHT);
    }

    /**
     * set atmosphere pressure target to target map
     *
     * @param apTarget
     */
    public void setApTarget(String apTarget) {
        setValue(apTarget, DWDWDDataPoint.DWDDataPointAttribute.ATMOPRESSURE);
    }

    /**
     * set temperature target to target map
     *
     * @param temperTarget
     */
    public void setTemperTarget(String temperTarget) {
        setValue(temperTarget, DWDWDDataPoint.DWDDataPointAttribute.TEMPERATURE);
    }

    /**
     * set temperature min target to target map
     *
     * @param temperatureMinTarget
     */
    public void setTemperatureMinTarget(String temperatureMinTarget) {
        setValue(temperatureMinTarget, DWDWDDataPoint.DWDDataPointAttribute.TMIN);
    }

    /**
     * set temperature max target to target map
     *
     * @param temperatureMaxTarget
     */
    public void setTemperatureMaxTarget(String temperatureMaxTarget) {
        setValue(temperatureMaxTarget, DWDWDDataPoint.DWDDataPointAttribute.TMAX);
    }

    /**
     * set humidity target to target map
     *
     * @param humidTarget
     */
    public void setHumidTarget(String humidTarget) {
        setValue(humidTarget, DWDWDDataPoint.DWDDataPointAttribute.HUMIDITY);
    }

    /**
     * set wind speed target to target map
     *
     * @param wsTarget
     */
    public void setWsTarget(String wsTarget) {
        setValue(wsTarget, DWDWDDataPoint.DWDDataPointAttribute.WINDSPEED);
    }

    /**
     * set wind peaks target to target map
     *
     * @param windPeaksTarget
     */
    public void setWindPeaksTarget(String windPeaksTarget) {
        setValue(windPeaksTarget, DWDWDDataPoint.DWDDataPointAttribute.WINDPEAKS);
    }

    /**
     * set precipitation target to target map
     *
     * @param precTarget
     */
    public void setPrecTarget(String precTarget) {
        setValue(precTarget, DWDWDDataPoint.DWDDataPointAttribute.PRECIPITATION);
    }

    /**
     * set precipitation12 target to target map
     *
     * @param prec12Target
     */
    public void setPrec12Target(String prec12Target) {
        setValue(prec12Target, DWDWDDataPoint.DWDDataPointAttribute.PRECIPITATION12);
    }

    /**
     * set snow height target to target map
     *
     * @param snowHeightTarget
     */
    public void setSnowHeightTarget(String snowHeightTarget) {
        setValue(snowHeightTarget, DWDWDDataPoint.DWDDataPointAttribute.SNOWHEIGHT);
    }

    /**
     * set wind direction target to target map
     *
     * @param windDirectionTarget
     */
    public void setWindDirectionTarget(String windDirectionTarget) {
        setValue(windDirectionTarget, DWDWDDataPoint.DWDDataPointAttribute.WINDDIRECTION);
    }

    /**
     * set climate and clouds target to target map
     *
     * @param climateAndCloudsTarget
     */
    public void setClimateAndCloudsTarget(String climateAndCloudsTarget) {
        setValue(climateAndCloudsTarget, DWDWDDataPoint.DWDDataPointAttribute.CLIMATECLOUDS);
    }

    /**
     * set squall target to target map
     *
     * @param squallTarget
     */
    public void setSquallTargetTarget(String squallTarget) {
        setValue(squallTarget, DWDWDDataPoint.DWDDataPointAttribute.SQUALL);
    }

    /**
     * check target and add value to map
     *
     * @param target
     * @param attrName
     */
    private void setValue(String target, String attrName) {
        if (!target.equalsIgnoreCase(NODATA)) {
            String option = WEATHEROPTIONS.get(attrName);
            _targets.put(target, option);
        }
    }

    /**
     * get target map
     *
     * @return _targets
     */
    public Map<String, String> getTargetsMap() {
        return _targets;
    }

    interface DWDDataPointsDirectory {
        String NAME = "DWD Data Point Directory";
    }

    //attrinutes of data point
    interface DWDDataPointAttribute {
        String CITY = "City";
        String HEIGHT = "Height Target";
        String ATMOPRESSURE = "Atmospheric Pressure Target";
        String TEMPERATURE = "Temperature Target";
        String TMIN = "Temperature Min Target";
        String TMAX = "Temperature Max Target";
        String HUMIDITY = "Humidity Target";
        String PRECIPITATION = "Precipitation Target";
        String PRECIPITATION12 = "Precipitation12 Target";
        String SNOWHEIGHT = "Snow Height Target";
        String WINDSPEED = "Wind Speed Target";
        String WINDPEAKS = "Wind Peaks Target";
        String WINDDIRECTION = "Wind Direction Target";
        String CLIMATECLOUDS = "Climate and Clouds Target";
        String SQUALL = "Squall Target";

    }
}
