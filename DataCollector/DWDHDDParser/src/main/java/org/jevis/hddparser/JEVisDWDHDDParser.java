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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.driver.Parser;
import org.jevis.commons.driver.ParserReport;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerrit Schutz
 */
public class JEVisDWDHDDParser implements Parser {
    private static final Logger log = LogManager.getLogger(JEVisDWDHDDParser.class);
    private final String NODATA = "NODATA";
    private final ParserReport report = new ParserReport();
    private DWDHDDParser _parser;

    /**
     * collects in the list all data points related to the parser
     *
     * @param parserObject
     * @return List of datapoints
     */
    public static List<JEVisObject> prepareDataPoints(JEVisObject parserObject) {
        try {
            JEVisClass dataPointDirClass = parserObject.getDataSource().getJEVisClass(DWDHDDDataPoint.DWDDataPointsDirectory.NAME);
            JEVisObject dataPointDir = parserObject.getChildren(dataPointDirClass, false).get(0);
            JEVisClass dataPointClass = parserObject.getDataSource().getJEVisClass(DWDHDDDataPoint.NAME);

            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            List<JEVisObject> dataPoints = dataPointDir.getChildren(dataPointClass, false);
            List<JEVisObject> filteredList = new ArrayList<>();
            log.info("Found " + dataPoints.size() + " channel objects in " + dataPointDir.getName() + ":" + dataPointDir.getID());

            dataPoints.forEach(channelObject -> {
                if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {
                    filteredList.add(channelObject);
                    counterCheckForErrorInAPI.add(channelObject.getID());
                }
            });

            log.info(dataPointDir.getName() + ":" + dataPointDir.getID() + " has " + filteredList.size() + " channels.");

            return filteredList;
        } catch (JEVisException ex) {
            log.error("Data Points not found");
            return null;
        }

    }

    @Override
    public void initialize(JEVisObject parserObject) {
        //initializeAttributes(parserObject);
        _parser = new DWDHDDParser();
        initializeDWDDataPointParser(parserObject);
        log.info(parserObject.getName() + " - initialize finished");
    }

    @Override
    public void parse(List<InputStream> input, DateTimeZone timezone) {
        log.info("Parse started");
        _parser.parse(input);
    }

    @Override
    public List<Result> getResult() {
        return _parser.getResult();
    }

    @Override
    public ParserReport getReport() {
        return report;
    }

    private void initializeDWDDataPointParser(JEVisObject parserObject) {

        List<JEVisObject> dataPoints = prepareDataPoints(parserObject);

        List<DWDHDDDataPoint> dwddatapoints = new ArrayList<>();
        for (JEVisObject dp : dataPoints) {

            String station = null;
            try {
                station = getAttValue(dp, DWDHDDDataPoint.DWDHDDDataPointAttribute.STATION);
            } catch (Exception e) {
                log.error("Could not get station attribute.");
            }

            String hddTargetString = null;
            try {
                hddTargetString = getAttValue(dp, DWDHDDDataPoint.DWDHDDDataPointAttribute.HEATING_DEGREE_DAYS);
            } catch (Exception e) {
                log.error("Could not get heating degree days attribute.");
            }

            DWDHDDDataPoint dwddp = new DWDHDDDataPoint();
            dwddp.setStation(station);
            dwddp.setTarget(hddTargetString);

            dwddatapoints.add(dwddp);
        }
        _parser.setDataPoints(dwddatapoints);
    }

    /**
     * get parameter values from the JEVis DB
     *
     * @param <T>     return type
     * @param obj     JEVisobject
     * @param attType attribut type (string)
     * @return specific value
     */
    private <T> T getAttValue(JEVisObject obj, String attType) {

        try {
            JEVisAttribute att = obj.getAttribute(attType);
            if (att == null) {
                log.debug("Attribute is null: " + obj.getName() + ":" + obj.getID() + "." + attType);
                return (T) NODATA;
            }
            if (!att.hasSample()) {
                log.debug("Attribute has no samples: " + obj.getName() + ":" + obj.getID() + "." + attType);
                return (T) NODATA;
            }
            JEVisSample lastS = att.getLatestSample();
            String str = lastS.getValueAsString();
            if (null == str || str.isEmpty()) {
                log.debug("{0} - JEVis Sample is null or empty: " + obj.getName() + ":" + obj.getID() + "." + attType);
                return (T) NODATA;
            } else {
                return (T) lastS.getValueAsString();
            }
        } catch (JEVisException ex) {
            log.error("Failed to set the parameter value", ex);
        }

        log.debug("Get attribute value failed: " + obj.getName() + ":" + obj.getID() + "." + attType);
        throw new NullPointerException();
    }
}
