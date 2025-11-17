/**
 * Copyright (C) 2015 - 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEVis CSV-Driver.
 * <p>
 * JEVis CSV-Driver is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEVis CSV-Driver is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEVis CSV-Driver. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEVis CSV-Driver is part of the OpenJEVis project, further project
 * information are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.xlsxparser;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.Parser;
import org.jevis.commons.driver.ParserReport;
import org.jevis.commons.driver.Result;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.CommonMethods;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gerrit.schutz@envidatec.com
 */
public class JEVisXLSXParser implements Parser {
    private static final Logger logger = LogManager.getLogger(JEVisXLSXParser.class);
    private DateTimeZone timeZone;
    private XLSXParser xlsxParser;

    private void initializeAttributes(JEVisObject parserObject) {
        try {

            JEVisClass jeClass = parserObject.getJEVisClass();
            JEVisType ignoreFirstNLines = jeClass.getType(XLSXParserTypes.NUMBER_HEADLINES);
            JEVisType dpIndexType = jeClass.getType(XLSXParserTypes.DATAPOINT_INDEX);
            JEVisType dpTypeType = jeClass.getType(XLSXParserTypes.DATAPOINT_TYPE);
            JEVisType dateIndexType = jeClass.getType(XLSXParserTypes.DATE_INDEX);
            JEVisType timeIndexType = jeClass.getType(XLSXParserTypes.TIME_INDEX);
            JEVisType dateFormatType = jeClass.getType(XLSXParserTypes.DATE_FORMAT);
            JEVisType timeFormatType = jeClass.getType(XLSXParserTypes.TIME_FORMAT);
            Integer headerLines = DatabaseHelper.getObjectAsInteger(parserObject, ignoreFirstNLines);
            if (headerLines == null) {
                headerLines = 0;
            }

            Integer dpIndex = DatabaseHelper.getObjectAsInteger(parserObject, dpIndexType);

            String dpType = DatabaseHelper.getObjectAsString(parserObject, dpTypeType);
            if (dpType == null) {
                dpType = "ROW";
            }

            Integer dateIndex = DatabaseHelper.getObjectAsInteger(parserObject, dateIndexType);
            Integer timeIndex = DatabaseHelper.getObjectAsInteger(parserObject, timeIndexType);
            String dateFormat = DatabaseHelper.getObjectAsString(parserObject, dateFormatType);
            String timeFormat = DatabaseHelper.getObjectAsString(parserObject, timeFormatType);

            xlsxParser = new XLSXParser();

            xlsxParser.setDpIndex(dpIndex);
            xlsxParser.setDpType(dpType);
            xlsxParser.setHeaderLines(headerLines);
            xlsxParser.setDateIndex(dateIndex);
            xlsxParser.setTimeIndex(timeIndex);
            xlsxParser.setDateFormat(dateFormat);
            xlsxParser.setTimeFormat(timeFormat);

        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }

    private void initializeXLSXDataPointParser(JEVisObject parserObject) {
        try {
            JEVisClass dirClass = parserObject.getDataSource().getJEVisClass(XLSXParser.XLSXDataPointDirectory.NAME);
            JEVisObject dir = parserObject.getChildren(dirClass, true).get(0);
            JEVisClass dpClass = parserObject.getDataSource().getJEVisClass(XLSXParser.XLSXDataPoint.NAME);

            List<JEVisObject> dataPoints = CommonMethods.getChildrenRecursive(dir, dpClass);
            List<DataPoint> xlsxDatapoints = new ArrayList<DataPoint>();

            for (JEVisObject dp : dataPoints) {
                JEVisType mappingIdentifierType = dpClass.getType(XLSXParser.XLSXDataPoint.MAPPING_IDENTIFIER);
                JEVisType targetType = dpClass.getType(XLSXParser.XLSXDataPoint.TARGET);
                JEVisType valueIdentifierType = dpClass.getType(XLSXParser.XLSXDataPoint.VALUE_INDEX);

                Long datapointID = dp.getID();
                String mappingIdentifier = DatabaseHelper.getObjectAsString(dp, mappingIdentifierType);
                String targetString = DatabaseHelper.getObjectAsString(dp, targetType);
                String target = null;

                TargetHelper targetHelper = new TargetHelper(dp.getDataSource(), targetString);
                if (targetHelper.isValid() && targetHelper.targetObjectAccessible()) {
                    target = targetString;
                } else {
                    logger.warn("DataPoint target error: {}:{}", dp.getName(), dp.getID());
                    continue;
                }

                String valueString = null;
                try {
                    valueString = DatabaseHelper.getObjectAsString(dp, valueIdentifierType);
                } catch (Exception ex) {
                    logger.warn("DataPoint value string error: {}:{}", dp.getName(), dp.getID(), ex);
                }

                Integer valueIndex = null;
                try {
                    if (valueString != null) {
                        valueIndex = Integer.parseInt(valueString);
                        valueIndex--;
                    }
                } catch (Exception ex) {
                    logger.warn("DataPoint value index error: {}:{}", dp.getName(), dp.getID(), ex);
                }

                if (mappingIdentifier == null && valueIndex == null) {
                    continue;
                }

                DataPoint xlsxdp = new DataPoint();
                xlsxdp.setMappingIdentifier(mappingIdentifier);
                xlsxdp.setTarget(target);
                xlsxdp.setValueIndex(valueIndex);
                xlsxDatapoints.add(xlsxdp);
            }
            xlsxParser.setDataPoints(xlsxDatapoints);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    /**
     * @param inputList
     * @param timeZone
     */
    @Override
    public void parse(List<InputStream> inputList, DateTimeZone timeZone) {
        logger.info("CSV Parser.parse Streams: {} tz: {}", inputList.size(), timeZone);
        xlsxParser.parse(inputList, timeZone);
    }

    @Override
    public List<Result> getResult() {
        return xlsxParser.getResult();
    }

    @Override
    public ParserReport getReport() {
        return xlsxParser.getReport();
    }

    @Override
    public void initialize(JEVisObject parserObject) {

        initializeAttributes(parserObject);
        initializeXLSXDataPointParser(parserObject);
    }

    interface XLSXParserTypes extends DataCollectorTypes.Parser {

        String NAME = "XLSX Parser";
        String DATAPOINT_INDEX = "Datapoint Index";
        String DATAPOINT_TYPE = "Datapoint Alignment";
        String NUMBER_HEADLINES = "Number Of Headlines";
        String DATE_INDEX = "Date Index";
        String TIME_INDEX = "Time Index";
        String DATE_FORMAT = "Date Format";
        String TIME_FORMAT = "Time Format";
    }

    interface CSVDataPointDirectoryTypes extends DataCollectorTypes.DataPointDirectory {

        String NAME = "CSV Data Point Directory";
    }

    interface CSVDataPointTypes extends DataCollectorTypes.DataPoint {

        String NAME = "CSV Data Point";
        String MAPPING_IDENTIFIER = "Mapping Identifier";
        String VALUE_INDEX = "Value Index";
        String TARGET = "Target";

    }
}
