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
package org.jevis.hddparser;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gerrit Schutz
 */
public class DWDHDDDataPoint {
    public static String NAME = "DWD HDD Data Point";
    Map<String, String> HDD_OPTIONS = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            put(DWDHDDDataPoint.DWDHDDDataPointAttribute.STATION, "STATION");
        }
    });
    private String station;
    private String target;

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    interface DWDDataPointsDirectory {
        String NAME = "DWD Data Point Directory";
    }

    interface DWDHDDDataPointAttribute {
        String STATION = "Station";
        String HEATING_DEGREE_DAYS = "Heating Degree Days Target";
    }
}
