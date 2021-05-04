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
 * This parser handles the specific HTML documents received from the ftp server
 * of "Deutscher Wetterdienst".
 *
 * @author Artur Iablokov
 */
public class JEVisDWDWDParser implements Parser {
    public static final String NODATA = "NODATA";
    private static final Logger log = LogManager.getLogger(JEVisDWDWDParser.class);
    private final ParserReport report = new ParserReport();
    private DWDWDParser _parser;

    /**
     * collects in the list all data points related to the parser
     *
     * @param parserObject
     * @return List of datapoints
     */
    public static List<JEVisObject> prepareDataPoints(JEVisObject parserObject) {
        try {
            JEVisClass dataPointDirClass = parserObject.getDataSource().getJEVisClass(DWDWDDataPoint.DWDDataPointsDirectory.NAME);
            JEVisObject dataPointDir = parserObject.getChildren(dataPointDirClass, false).get(0);
            JEVisClass dataPointClass = parserObject.getDataSource().getJEVisClass(DWDWDDataPoint.NAME);

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
        _parser = new DWDWDParser();
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

    /**
     * Sets the parameter for all data points and transmits them to the parser.
     *
     * @param parserObject parser
     */
    private void initializeDWDDataPointParser(JEVisObject parserObject) {

        List<JEVisObject> dataPoints = prepareDataPoints(parserObject);

        List<DWDWDDataPoint> dwddatapoints = new ArrayList<>();
        for (JEVisObject dp : dataPoints) {

            String city = null;
            try {
                city = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.CITY);
            } catch (Exception e) {
                log.error("Could not get city attribute.");
            }

            String heightTarget = null;
            try {
                heightTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.HEIGHT);
            } catch (Exception e) {
                log.error("Could not get height attribute.");
            }

            String apTarget = null;
            try {
                apTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.ATMOPRESSURE);
            } catch (Exception e) {
                log.error("Could not get atmospheric pressure attribute.");
            }
            String temperTarget = null;
            try {
                temperTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.TEMPERATURE);
            } catch (Exception e) {
                log.error("Could not get temperature attribute.");
            }
            String temperMinTarget = null;
            try {
                temperMinTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.TMIN);
            } catch (Exception e) {
                log.error("Could not get tmin attribute.");
            }
            String temperMaxTarget = null;
            try {
                temperMaxTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.TMAX);
            } catch (Exception e) {
                log.error("Could not get tmax attribute.");
            }
            String humidTarget = null;
            try {
                humidTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.HUMIDITY);
            } catch (
                    Exception e) {
                log.error("Could not get humidity attribute.");
            }

            String wsTarget = null;
            try {
                wsTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.WINDSPEED);
            } catch (Exception e) {
                log.error("Could not get windspeed attribute.");
            }
            String windPeaksTarget = null;
            try {
                windPeaksTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.WINDPEAKS);
            } catch (
                    Exception e) {
                log.error("Could not get windpeaks attribute.");
            }

            String precTarget = null;
            try {
                precTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.PRECIPITATION);
            } catch (Exception e) {
                log.error("Could not get precipitation attribute.");
            }
            String prec12Target = null;
            try {
                prec12Target = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.PRECIPITATION12);
            } catch (Exception e) {
                log.error("Could not get precipitation12 attribute.");
            }
            String snowHeightTarget = null;
            try {
                snowHeightTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.SNOWHEIGHT);
            } catch (Exception e) {
                log.error("Could not get snow height attribute.");
            }
            String windDirectionTarget = null;
            try {
                windDirectionTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.WINDDIRECTION);
            } catch (Exception e) {
                log.error("Could not get wind direction attribute.");
            }
            String climateCloudsTarget = null;
            try {
                climateCloudsTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.CLIMATECLOUDS);
            } catch (Exception e) {
                log.error("Could not get climate + clouds attribute.");
            }
            String squallTarget = null;
            try {
                squallTarget = getAttValue(dp, DWDWDDataPoint.DWDDataPointAttribute.SQUALL);
            } catch (Exception e) {
                log.error("Could not get squall attribute.");
            }

            DWDWDDataPoint dwddp = new DWDWDDataPoint();
            dwddp.setCity(city);
            dwddp.setApTarget(apTarget);
            dwddp.setHeightTarget(heightTarget);
            dwddp.setTemperTarget(temperTarget);
            dwddp.setTemperatureMinTarget(temperMinTarget);
            dwddp.setTemperatureMaxTarget(temperMaxTarget);
            dwddp.setHumidTarget(humidTarget);
            dwddp.setWsTarget(wsTarget);
            dwddp.setWindPeaksTarget(windPeaksTarget);
            dwddp.setPrecTarget(precTarget);
            dwddp.setPrec12Target(prec12Target);
            dwddp.setSnowHeightTarget(snowHeightTarget);
            dwddp.setWindDirectionTarget(windDirectionTarget);
            dwddp.setClimateAndCloudsTarget(climateCloudsTarget);
            dwddp.setSquallTargetTarget(squallTarget);

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
