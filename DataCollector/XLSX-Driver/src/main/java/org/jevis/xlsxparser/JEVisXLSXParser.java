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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class JEVisXLSXParser implements Parser {
    private static final Logger logger = LogManager.getLogger(JEVisXLSXParser.class);
    private DateTimeZone timeZone;
    private XLSXParser _XLSXParser;

    private void initializeAttributes(JEVisObject parserObject) {
        try {

            JEVisClass jeClass = parserObject.getJEVisClass();
            JEVisType seperatorColumn = jeClass.getType(CSVParserTypes.DELIMITER);
            JEVisType enclosedBy = jeClass.getType(CSVParserTypes.QUOTE);
            JEVisType ignoreFirstNLines = jeClass.getType(CSVParserTypes.NUMBER_HEADLINES);
            JEVisType dpIndexType = jeClass.getType(CSVParserTypes.DATAPOINT_INDEX);
            JEVisType dpTypeType = jeClass.getType(CSVParserTypes.DATAPOINT_TYPE);
            JEVisType dateIndexType = jeClass.getType(CSVParserTypes.DATE_INDEX);
            JEVisType timeIndexType = jeClass.getType(CSVParserTypes.TIME_INDEX);
            JEVisType dateFormatType = jeClass.getType(CSVParserTypes.DATE_FORMAT);
            JEVisType timeFormatType = jeClass.getType(CSVParserTypes.TIME_FORMAT);
            JEVisType decimalSeparatorType = jeClass.getType(CSVParserTypes.DECIMAL_SEPARATOR);
            JEVisType thousandSeparatorType = jeClass.getType(CSVParserTypes.THOUSAND_SEPARATOR);
            JEVisType charsetType = jeClass.getType(CSVParserTypes.CHARSET);

            String delim = DatabaseHelper.getObjectAsString(parserObject, seperatorColumn);
            String quote = DatabaseHelper.getObjectAsString(parserObject, enclosedBy);
            Integer headerLines = DatabaseHelper.getObjectAsInteger(parserObject, ignoreFirstNLines);
            if (headerLines == null) {
                headerLines = 0;
            }

            Integer dpIndex = DatabaseHelper.getObjectAsInteger(parserObject, dpIndexType);
            if (dpIndex != null) {
                dpIndex--;
            }

            String dpType = DatabaseHelper.getObjectAsString(parserObject, dpTypeType);
            if (dpType == null) {
                dpType = "ROW";
            }

            Integer dateIndex = DatabaseHelper.getObjectAsInteger(parserObject, dateIndexType);
            if (dateIndex != null) {
                dateIndex--;
            }

            Integer timeIndex = DatabaseHelper.getObjectAsInteger(parserObject, timeIndexType);
            if (timeIndex != null) {
                timeIndex--;
            }

            String charset = DatabaseHelper.getObjectAsString(parserObject, charsetType);
            Charset cset;
            if (charset == null || charset.equals("")) {
                cset = Charset.defaultCharset();
            } else {
                cset = Charset.forName(charset);
            }

            String dateFormat = DatabaseHelper.getObjectAsString(parserObject, dateFormatType);

            String timeFormat = DatabaseHelper.getObjectAsString(parserObject, timeFormatType);

            String decimalSeparator = DatabaseHelper.getObjectAsString(parserObject, decimalSeparatorType);

            String thousandSeparator = DatabaseHelper.getObjectAsString(parserObject, thousandSeparatorType);

            _XLSXParser = new XLSXParser();
            _XLSXParser.setDateFormat(dateFormat);
            _XLSXParser.setDateIndex(dateIndex);
            _XLSXParser.setDecimalSeparator(decimalSeparator);
            _XLSXParser.setDelimiter(delim);
            _XLSXParser.setDpIndex(dpIndex);
            _XLSXParser.setDpType(dpType);
            _XLSXParser.setHeaderLines(headerLines);
            _XLSXParser.setQuote(quote);
            _XLSXParser.setThousandSeparator(thousandSeparator);
            _XLSXParser.setTimeFormat(timeFormat);
            _XLSXParser.setTimeIndex(timeIndex);
            _XLSXParser.setCharset(cset);

        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }

    private void initializeXLSXDataPointParser(JEVisObject parserObject) {
        try {
            JEVisClass dirClass = parserObject.getDataSource().getJEVisClass(CSVDataPointDirectoryTypes.NAME);
            JEVisObject dir = parserObject.getChildren(dirClass, true).get(0);
            JEVisClass dpClass = parserObject.getDataSource().getJEVisClass(CSVDataPointTypes.NAME);

            List<JEVisObject> dataPoints = CommonMethods.getChildrenRecursive(dir, dpClass);
            List<DataPoint> csvdatapoints = new ArrayList<DataPoint>();
            for (JEVisObject dp : dataPoints) {
                JEVisType mappingIdentifierType = dpClass.getType(CSVDataPointTypes.MAPPING_IDENTIFIER);
                JEVisType targetType = dpClass.getType(CSVDataPointTypes.TARGET);
                JEVisType valueIdentifierType = dpClass.getType(CSVDataPointTypes.VALUE_INDEX);

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
//                    ex.printStackTrace();
                }

                Integer valueIndex = null;
                try {
                    if (valueString != null) {
                        valueIndex = Integer.parseInt(valueString);
                        valueIndex--;
                    }
                } catch (Exception ex) {
                    logger.warn("DataPoint value index error: {}:{}", dp.getName(), dp.getID(), ex);
//                    ex.printStackTrace();
                }
                DataPoint xlsxdp = new DataPoint();
                xlsxdp.setMappingIdentifier(mappingIdentifier);
                xlsxdp.setTarget(target);
                xlsxdp.setValueIndex(valueIndex);
                csvdatapoints.add(xlsxdp);
            }
            _XLSXParser.setDataPoints(csvdatapoints);
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
        _XLSXParser.parse(inputList, timeZone);
    }

    @Override
    public List<Result> getResult() {
        return _XLSXParser.getResult();
    }

    @Override
    public ParserReport getReport() {
        return _XLSXParser.getReport();
    }

    @Override
    public void initialize(JEVisObject parserObject) {

        initializeAttributes(parserObject);
        initializeXLSXDataPointParser(parserObject);
    }

    interface CSVParserTypes extends DataCollectorTypes.Parser {

        String NAME = "XLSX Parser";
        String DATAPOINT_INDEX = "Datapoint Index";
        String DATAPOINT_TYPE = "Datapoint Alignment";
        String DATE_INDEX = "Date Index";
        String DELIMITER = "Delimiter";
        String NUMBER_HEADLINES = "Number Of Headlines";
        String QUOTE = "Quote";
        String TIME_INDEX = "Time Index";
        String DATE_FORMAT = "Date Format";
        String DECIMAL_SEPARATOR = "Decimal Separator";
        String TIME_FORMAT = "Time Format";
        String THOUSAND_SEPARATOR = "Thousand Separator";
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
