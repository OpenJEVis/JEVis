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
package org.jevis.csvparser;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
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
public class JEVisCSVParser implements Parser {
    public static final String VERSION = "Version 1.3.0 2026-10-07 (symmetric ROW/COLUMN, one-based indexes)";
    private static final Logger logger = LogManager.getLogger(JEVisCSVParser.class);
    private DateTimeZone timeZone;
    private CSVParser _csvParser;

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
            JEVisType decimalSeparatorType = jeClass.getType(CSVParserTypes.DECIMAL_SEPERATOR);
            JEVisType thousandSeparatorType = jeClass.getType(CSVParserTypes.THOUSAND_SEPERATOR);
            JEVisType charsetType = jeClass.getType(CSVParserTypes.CHARSET);

            String delim = DatabaseHelper.getObjectAsString(parserObject, seperatorColumn);
            String quote = DatabaseHelper.getObjectAsString(parserObject, enclosedBy);
            Integer headerLines = DatabaseHelper.getObjectAsInteger(parserObject, ignoreFirstNLines);
            if (headerLines == null) {
                headerLines = 0;
            }

            Integer dpIndex = toZeroBasedIndex(
                    DatabaseHelper.getObjectAsInteger(parserObject, dpIndexType),
                    CSVParserTypes.DATAPOINT_INDEX, false);

            String dpType = DatabaseHelper.getObjectAsString(parserObject, dpTypeType);
            if (dpType == null || dpType.trim().isEmpty()) {
                dpType = "ROW";
            } else {
                dpType = dpType.trim().toUpperCase();
                if (!dpType.equals("ROW") && !dpType.equals("COLUMN")) {
                    logger.warn("Unknown Datapoint Alignment '{}'; using ROW", dpType);
                    dpType = "ROW";
                }
            }

            Integer dateIndex = toZeroBasedIndex(
                    DatabaseHelper.getObjectAsInteger(parserObject, dateIndexType),
                    CSVParserTypes.DATE_INDEX, true);

            Integer timeIndex = toZeroBasedIndex(
                    DatabaseHelper.getObjectAsInteger(parserObject, timeIndexType),
                    CSVParserTypes.TIME_INDEX, false);

            String charset = DatabaseHelper.getObjectAsString(parserObject, charsetType);
            Charset cset;
            if (charset == null || charset.equals("")) {
                cset = Charset.defaultCharset();
            } else {
                cset = Charset.forName(charset);
            }

            String dateFormat = DatabaseHelper.getObjectAsString(parserObject, dateFormatType);

            String timeFormat = DatabaseHelper.getObjectAsString(parserObject, timeFormatType);
            if (timeFormat != null && !timeFormat.trim().isEmpty() && timeIndex == null) {
                throw new IllegalArgumentException("Time Index is required when Time Format is configured");
            }

            String decimalSeparator = DatabaseHelper.getObjectAsString(parserObject, decimalSeparatorType);

            String thousandSeparator = DatabaseHelper.getObjectAsString(parserObject, thousandSeparatorType);

            _csvParser = new CSVParser();
            _csvParser.setDateFormat(dateFormat);
            _csvParser.setDateIndex(dateIndex);
            _csvParser.setDecimalSeparator(decimalSeparator);
            _csvParser.setDelimiter(delim);
            _csvParser.setDpIndex(dpIndex);
            _csvParser.setDpType(dpType);
            _csvParser.setHeaderLines(headerLines);
            _csvParser.setQuote(quote);
            _csvParser.setThousandSeparator(thousandSeparator);
            _csvParser.setTimeFormat(timeFormat);
            _csvParser.setTimeIndex(timeIndex);
            _csvParser.setCharset(cset);

        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    private Integer toZeroBasedIndex(Integer configuredIndex, String name, boolean required) {
        if (configuredIndex == null) {
            if (required) {
                throw new IllegalArgumentException(name + " is required and must start at 1");
            }
            return null;
        }
        if (configuredIndex < 1) {
            throw new IllegalArgumentException(name + " must be 1 or greater; 0 is not a valid CSV row/column number");
        }
        return configuredIndex - 1;
    }

    private void initializeCSVDataPointParser(JEVisObject parserObject) {
        try {
            JEVisClass dpClass = parserObject.getDataSource().getJEVisClass(CSVDataPointTypes.NAME);
            List<JEVisObject> dataPoints = CommonMethods.getChildrenRecursive(parserObject, dpClass);
            List<DataPoint> csvdatapoints = new ArrayList<DataPoint>();

            for (JEVisObject dp : dataPoints) {
                try {
                    JEVisType mappingIdentifierType = dpClass.getType(CSVDataPointTypes.MAPPING_IDENTIFIER);
                    JEVisType targetType = dpClass.getType(CSVDataPointTypes.TARGET);
                    JEVisType valueIdentifierType = dpClass.getType(CSVDataPointTypes.VALUE_INDEX);

                    Long datapointID = dp.getID();
                    String mappingIdentifier = DatabaseHelper.getObjectAsString(dp, mappingIdentifierType);
                    if (mappingIdentifier != null && mappingIdentifier.trim().isEmpty()) {
                        mappingIdentifier = null;
                    }
                    String targetString = DatabaseHelper.getObjectAsString(dp, targetType);
                    String target = null;

                    TargetHelper targetHelper = new TargetHelper(dp.getDataSource(), targetString);
                    if (targetString != null && targetHelper.isValid() && targetHelper.targetObjectAccessible()) {
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
                        if (valueString != null && !valueString.trim().isEmpty()) {
                            int configuredValueIndex = Integer.parseInt(valueString.trim());
                            if (configuredValueIndex < 1) {
                                throw new IllegalArgumentException("Value Index must be 1 or greater; 0 is not valid");
                            }
                            valueIndex = configuredValueIndex - 1;
                        }
                    } catch (Exception ex) {
                        logger.warn("DataPoint value index error: {}:{}", dp.getName(), dp.getID(), ex);
                    }
                    if (mappingIdentifier == null && valueIndex == null) {
                        logger.warn("Skipping CSV Data Point {}:{} because neither Mapping Identifier nor a valid Value Index is configured",
                                dp.getName(), dp.getID());
                        continue;
                    }
                    DataPoint csvdp = new DataPoint();
                    csvdp.setMappingIdentifier(mappingIdentifier);
                    csvdp.setTarget(target);
                    csvdp.setValueIndex(valueIndex);
                    csvdatapoints.add(csvdp);
                } catch (Exception e) {
                    logger.error(e);
                }
            }
            _csvParser.setDataPoints(csvdatapoints);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    /**
     * @param inputList
     * @param timeZone
     */
    @Override
    public void parse(List<InputStream> inputList, DateTimeZone timeZone) {
        logger.debug("CSV Parser.parse Streams: {} tz: {}", inputList.size(), timeZone);
        _csvParser.parse(inputList, timeZone);
    }

    @Override
    public List<Result> getResult() {
        return _csvParser.getResult();
    }

    @Override
    public ParserReport getReport() {
        return _csvParser.getReport();
    }

    @Override
    public void initialize(JEVisObject parserObject) {
        logger.info("Initialize JEVisCSVParser build: {}", VERSION);

        initializeAttributes(parserObject);

        Converter converter = ConverterFactory.getConverter(parserObject);
        _csvParser.setConverter(converter);

        initializeCSVDataPointParser(parserObject);
    }

    interface CSVParserTypes extends DataCollectorTypes.Parser {

        String NAME = "CSV Parser";
        String DATAPOINT_INDEX = "Datapoint Index";
        String DATAPOINT_TYPE = "Datapoint Alignment";
        String DATE_INDEX = "Date Index";
        String DELIMITER = "Delimiter";
        String NUMBER_HEADLINES = "Number Of Headlines";
        String QUOTE = "Quote";
        String TIME_INDEX = "Time Index";
        String DATE_FORMAT = "Date Format";
        String DECIMAL_SEPERATOR = "Decimal Separator";
        String TIME_FORMAT = "Time Format";
        String THOUSAND_SEPERATOR = "Thousand Separator";
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