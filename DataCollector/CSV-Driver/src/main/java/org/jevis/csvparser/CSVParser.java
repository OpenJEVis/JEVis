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
import org.jevis.commons.driver.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author broder
 */
public class CSVParser {
    private static final Logger logger = LogManager.getLogger(CSVParser.class);
    private static final String UTF8_BOM = "\uFEFF";
    private DateTimeZone timeZone;
    private String dpType;
    private String _quote;
    private String _delim;
    private Integer _headerLines;
    private Integer _dateIndex;
    private Integer _timeIndex;
    private Integer _dpIndex;
    private String _dateFormat;
    private String _timeFormat;
    private String _decimalSeparator;
    private String _thousandSeparator;
    private Integer _currLineIndex;
    private Charset charset;

    private final List<Result> _results = new ArrayList<Result>();
    private List<DataPoint> _dataPoints = new ArrayList<DataPoint>();
    private Converter _converter;
    private final ParserReport report = new ParserReport();

    private void calculateColumns(String stringArrayInput) {
        String[] line = stringArrayInput.split(String.valueOf(_delim), -1);
        if (_quote != null) {
            line = removeQuotes(line);
        }
        Map<String, Integer> columnMap = new HashMap<>();
        for (int i = 0; i < line.length; i++) {
            String curString = line[i].trim();
            if (!curString.isEmpty()) {
                columnMap.put(curString, i);
            }
        }

        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, Integer>> iter = columnMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Integer> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=').append('"');
            sb.append(entry.getValue());
            sb.append('"');
            if (iter.hasNext()) {
                sb.append(',').append(' ');
            }
        }
        logger.info("MAP: {}", sb.toString());


        for (DataPoint dp : _dataPoints) {
            String mappingIdentifier = dp.getMappingIdentifier();
            //VERY VERY VERY DIRTY CODE, PLEASE DONT USE IT
            Integer column = null;
            if (mappingIdentifier != null) {
                column = columnMap.get(mappingIdentifier);
                dp.setValueIndex(column);
            }
        }
    }

    private void calculateColumnsColumn(String[] stringArrayInput) {

        List<String> columns = new ArrayList<>();
        for (String s : stringArrayInput) {
            String[] line = s.split(String.valueOf(_delim), -1);
            for (int i = 0, lineLength = line.length; i < lineLength; i++) {
                String lineSub = line[i];

                if (columns.size() > i) {
                    String s1 = columns.get(i);
                    s1 += _delim + lineSub;
                    columns.set(i, s1);
                } else {
                    columns.add(lineSub);
                }

            }
        }

        if (_quote != null) {
            List<String> columnsWOQuotes = new ArrayList<>();
            for (String s : columns) {
                String newStr = s.replaceAll(_quote, "");
                columnsWOQuotes.add(newStr);
            }

            columns = columnsWOQuotes;
        }

        Map<String, Integer> columnMap = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            String curString = columns.get(i).trim();
            if (!curString.isEmpty()) {
                columnMap.put(curString, i);
            }
        }

        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, Integer>> iter = columnMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Integer> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=').append('"');
            sb.append(entry.getValue());
            sb.append('"');
            if (iter.hasNext()) {
                sb.append(',').append(' ');
            }
        }
        logger.info("MAP: {}", sb.toString());


        for (DataPoint dp : _dataPoints) {
            String mappingIdentifier = dp.getMappingIdentifier();
            Integer column = null;
            if (mappingIdentifier != null) {
                column = getIntByIdentifier(mappingIdentifier, columnMap);
            } else {
                column = _dpIndex;
            }

            dp.setValueIndex(column);
        }
    }

    private Integer getIntByIdentifier(String mapIdent, Map<String, Integer> columnMap) {
        Integer result;
        for (Map.Entry<String, Integer> entry : columnMap.entrySet()) {
            String[] line = entry.getKey().split(String.valueOf(_delim), -1);

            for (int i = 0; i < line.length; i++) {
                if (line[i].equals(mapIdent)) {
                    return i;
                }
            }
        }
        result = columnMap.get(mapIdent);
        if (result == null) {
            logger.info("FIND MAP failed: {}", mapIdent);
            mapIdent = mapIdent.replace("ä", "?");
            mapIdent = mapIdent.replace("Ä", "?");
            mapIdent = mapIdent.replace("ü", "?");
            mapIdent = mapIdent.replace("Ü", "?");
            mapIdent = mapIdent.replace("ö", "?");
            mapIdent = mapIdent.replace("Ö", "?");
            mapIdent = mapIdent.replace("ß", "?");
            logger.info("FIND MAP replaced: {}", mapIdent);
            result = columnMap.get(mapIdent);
            logger.info("FIND MAP result: {}", result);
        }
        return result;
    }

    private void parseLine(String[] line) {
        DateTime dateTime = getDateTime(line);

        if (dateTime == null) {
            report.addError(new LineError(-3, -2, null, "Date Error"));
            return;//if there is no date the whole line is invalid... or generate a date?
        }

        for (DataPoint dp : _dataPoints) {
            try {
                String mappingIdentifier = dp.getMappingIdentifier();
                Integer valueIndex = dp.getValueIndex();
                String target = dp.getTarget();

                Boolean mappingError = false;
//                try {
//                    String currentMappingValue = null;
//
//                    if (_dpIndex != null) {
//                        if (dpType.equals("COLUMN")) {
//                            currentMappingValue = line[_dpIndex];
//                        }
//                    }
//
//                    if (mappingIdentifier != null && currentMappingValue != null && !mappingIdentifier.equals(currentMappingValue)) {
//                        mappingError = true;
//                        _report.addError(new LineError(_currLineIndex, valueIndex, null, "Mapping Error"));
//                        continue;
//                    }
//                } catch (Exception ex) {
//                    _report.addError(new LineError(_currLineIndex, valueIndex, ex, "Mapping Exception"));
//                    logger.warn("This line in the file is not valid: {}", _currLineIndex);
//                }

                Boolean valueValid = false;
                String sVal = null;
                Double value = null;
                //               try {
                sVal = line[valueIndex];
//                if (_thousandSeperator != null && !_thousandSeperator.equals("")) {
//                    sVal = sVal.replaceAll("\\" + _thousandSeperator, "");
//                }
//                if (_decimalSeperator != null && !_decimalSeperator.equals("")) {
//                    sVal = sVal.replaceAll("\\" + _decimalSeperator, ".");
//                }

                //todo bind locale to language or location?? ad thousands separator without regex
                if (_decimalSeparator == null || _decimalSeparator.equals(",")) {
                    NumberFormat nf_in = NumberFormat.getNumberInstance(Locale.GERMANY);
                    value = nf_in.parse(sVal).doubleValue();
                } else if (_decimalSeparator.equals(".")) {
                    NumberFormat nf_out = NumberFormat.getNumberInstance(Locale.UK);
                    value = nf_out.parse(sVal).doubleValue();
                }

//                    valueValid = true;
//                } catch (Exception nfe) {
//                    _report.addError(new LineError(_currLineIndex, valueIndex, nfe, " Parser Exeption"));
//                    valueValid = false;
                //                   continue;
//                }

//                if (!valueValid) {
//                    StringBuilder failureLine = new StringBuilder();
//                    for (int current = 0; current < line.length; current++) {
//                        failureLine.append(line[current]);
//                        if (current < line.length - 1) {
//                            failureLine.append(_delim);
//                        }
//                    }
//                    Logger.getLogger(this.getClass().getName()).log(Level.DEBUG, "Value Index " + valueIndex + "is not valid in line: " + _currLineIndex);
//                    Logger.getLogger(this.getClass().getName()).log(Level.DEBUG, "Value Line: " + failureLine.toString());
//                    continue;
//                }
//                if (mappingError) {
//                    continue;
//                }
                Result tempResult = new Result(target, value, dateTime);
                _results.add(tempResult);
                report.addSuccess(_currLineIndex, valueIndex);
            } catch (Exception ex) {
                report.addError(new LineError(_currLineIndex, -2, ex, "Unexpected Exception"));
//                ex.printStackTrace();
            }
        }
    }

    public void parse(List<InputStream> inputList, DateTimeZone timeZone) {
        logger.info("Start CSV parsing");
        this.timeZone = timeZone;
        for (InputStream inputStream : inputList) {

            _converter.convertInput(inputStream, charset);

            String[] stringArrayInput = (String[]) _converter.getConvertedInput(String[].class);

            if (charset.equals(StandardCharsets.UTF_8) && stringArrayInput.length > 0) {
                if (stringArrayInput[0].startsWith(UTF8_BOM)) {
                    stringArrayInput[0] = stringArrayInput[0].substring(1);
                }
            }

            logger.info("Total count of lines {}", stringArrayInput.length);
            if (dpType != null && dpType.equals("ROW")) {
                calculateColumns(stringArrayInput[_dpIndex]);
            } else {
                calculateColumnsColumn(stringArrayInput);
            }

            if (dpType != null && dpType.equals("ROW")) {
                for (int i = _headerLines; i < stringArrayInput.length; i++) {
                    _currLineIndex = i;
                    try {
                        //TODO 1,"1,1",1 is not working yet
                        String[] line = stringArrayInput[i].split(String.valueOf(_delim), -1);
                        if (_quote != null) {
                            line = removeQuotes(line);
                        }

                        parseLine(line);
                    } catch (Exception e) {
                        report.addError(new LineError(_currLineIndex, -2, e, "Detect a Problem in the Parsing Process"));
                        logger.error("Detected a Problem in the Parsing Process in line {}", _currLineIndex, e);
                    }
                }
            } else {
                for (int i = _headerLines; i < stringArrayInput.length; i++) {
                    _currLineIndex = i;
                    try {

                        String[] line;
                        if (_quote != null) {
                            line = stringArrayInput[i].split(_delim + "(?=(?:[^" + _quote + "]*" + _quote + "[^" + _quote + "]*" + _quote + ")*[^" + _quote + "]*$)");
                            line = removeQuotes(line);
                        } else {
                            line = stringArrayInput[i].split(String.valueOf(_delim), -1);
                        }

                        DateTime dateTime = getDateTime(line);

                        if (dateTime == null) {
                            report.addError(new LineError(-3, -2, null, "Date Error"));
                            logger.error("Detected a Problem in the Parsing Process in line {}. Date Error", _currLineIndex);
                            return;
                        }

                        DataPoint currentDP = null;
                        for (DataPoint dp : _dataPoints) {
                            for (String s : line) {
                                if (s.equals(dp.getMappingIdentifier())) {
                                    currentDP = dp;
                                    break;
                                }
                            }
                        }

                        try {
                            String mappingIdentifier = currentDP.getMappingIdentifier();
                            Integer valueIndex = currentDP.getValueIndex();
                            String target = currentDP.getTarget();

                            String sVal = null;
                            Double value = null;
                            sVal = line[_dpIndex];
                            //todo bind locale to language or location?? add thousands separator without regex
                            if (_decimalSeparator == null || _decimalSeparator.equals(",")) {
                                NumberFormat nf_in = NumberFormat.getNumberInstance(Locale.GERMANY);
                                value = nf_in.parse(sVal).doubleValue();
                            } else if (_decimalSeparator.equals(".")) {
                                NumberFormat nf_out = NumberFormat.getNumberInstance(Locale.UK);
                                value = nf_out.parse(sVal).doubleValue();
                            }
                            Result tempResult = new Result(target, value, dateTime);
                            _results.add(tempResult);
                            report.addSuccess(_currLineIndex, _dpIndex);
                        } catch (Exception ex) {
                            report.addError(new LineError(_currLineIndex, -2, ex, "Unexpected Exception"));
                            logger.error("Detect a Problem in the Parsing Process in line {}. Value parsing Error", _currLineIndex);
                        }
                    } catch (Exception e) {
                        report.addError(new LineError(_currLineIndex, -2, e, "Detected a Problem in the Parsing Process"));
                        logger.error("Detect a Problem in the Parsing Process in line {}", _currLineIndex, e);
                    }
                }
            }
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Number of Results: " + _results.size());
            if (!_results.isEmpty()) {
                logger.info("LastResult Date {}, Target {}, Value {}", _results.get(_results.size() - 1).getDate(), _results.get(_results.size() - 1).getTargetStr(), _results.get(_results.size() - 1).getValue());
            } else {
                logger.error("Cant parse or cant find any parsable data");
            }
        }

        //print error report based on Logger level
        report.print();

    }

    private String[] removeQuotes(String[] line) {
        String[] removed = new String[line.length];
        for (int i = 0; i < line.length; i++) {
            removed[i] = line[i].replace(_quote, "");
        }
        return removed;
    }

    private DateTime getDateTime(String[] line) {

        if (_dateFormat == null) {
            logger.error("No date format found");
            return null;
        }
        String input = "";
        String pattern = "";
        try {
            String date = line[_dateIndex].trim();
            pattern = _dateFormat;
            input = date;

            if (_timeFormat != null && _timeIndex > -1) {
                String time = line[_timeIndex].trim();
                pattern += " " + _timeFormat;
                input += " " + time;
            }

            return TimeConverter.parseDateTime(input, pattern, timeZone);
        } catch (Exception ex) {
            logger.warn("Pattern: {}", pattern);
            logger.warn("Date not parsable: {}", input);
            logger.warn("Line not parsable: {}", Arrays.toString(line));
            logger.warn("DateFormat: {}", _dateFormat);
            logger.warn("DateIndex: {}", _dateIndex);
            logger.warn("TimeFormat: {}", _timeFormat);
            logger.warn("TimeIndex: {}", _timeIndex);
            logger.warn("Exception: ", ex);
            return null;
        }

    }

    public ParserReport getReport() {
        return report;
    }

    public void setDpType(String dpType) {
        this.dpType = dpType;
    }

    public List<Result> getResult() {
        return _results;
    }

    public void setQuote(String _quote) {
        this._quote = _quote;
    }

    public void setDelim(String _delim) {
        this._delim = _delim;
    }

    public void setDateIndex(Integer _dateIndex) {
        this._dateIndex = _dateIndex;
    }

    public void setTimeIndex(Integer _timeIndex) {
        this._timeIndex = _timeIndex;
    }

    public void setDpIndex(Integer _dpIndex) {
        this._dpIndex = _dpIndex;
    }

    public void setDateFormat(String _dateFormat) {
        this._dateFormat = _dateFormat;
    }

    public void setTimeFormat(String _timeFormat) {
        this._timeFormat = _timeFormat;
    }

    public void setDecimalSeperator(String _decimalSeperator) {
        this._decimalSeparator = _decimalSeperator;
    }

    public void setThousandSeperator(String _thousandSeperator) {
        this._thousandSeparator = _thousandSeperator;
    }

    public void setDataPoints(List<DataPoint> _dataPoints) {
        this._dataPoints = _dataPoints;
    }

    public void setConverter(Converter _converter) {
        this._converter = _converter;
    }

    public void setHeaderLines(Integer _headerLines) {
        this._headerLines = _headerLines;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    // interfaces
    interface CSV extends DataCollectorTypes.Parser {

        String NAME = "CSV Parser";
        String DATAPOINT_INDEX = "Datapoint Index";
        //        public final static String DATAPOINT_TYPE = "Datapoint Type";
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

    interface CSVDataPointDirectory extends DataCollectorTypes.DataPointDirectory {

        String NAME = "CSV Data Point Directory";
    }

    interface CSVDataPoint extends DataCollectorTypes.DataPoint {

        String NAME = "CSV Data Point";
        String MAPPING_IDENTIFIER = "Mapping Identifier";
        String VALUE_INDEX = "Value Index";
        String TARGET = "Target";

    }
}
