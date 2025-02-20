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
    private final List<Result> _results = new ArrayList<Result>();
    private final ParserReport report = new ParserReport();
    private DateTimeZone timeZone;
    private String dpType;
    private String quote;
    private String delimiter;
    private Integer headerLines;
    private Integer dateIndex;
    private Integer timeIndex;
    private Integer dpIndex;
    private String dateFormat;
    private String timeFormat;
    private String decimalSeparator;
    private String thousandSeparator;
    private Integer currLineIndex;
    private Charset charset;
    private List<DataPoint> _dataPoints = new ArrayList<DataPoint>();
    private Converter _converter;

    private void calculateColumns(String stringArrayInput) {
        String[] line = stringArrayInput.split(String.valueOf(delimiter), -1);
        if (quote != null) {
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
        logger.debug("MAP: {}", sb.toString());


        for (DataPoint dp : _dataPoints) {
            String mappingIdentifier = dp.getMappingIdentifier();
            //VERY VERY VERY DIRTY CODE, PLEASE DONT USE IT
            Integer columnIndex = null;
            if (mappingIdentifier != null) {
                columnIndex = columnMap.get(mappingIdentifier);
                dp.setValueIndex(columnIndex);
            }
        }
    }

    private void calculateColumnsColumn(String[] stringArrayInput) {

        List<String> columns = new ArrayList<>();
        for (String s : stringArrayInput) {
            String[] line = s.split(String.valueOf(delimiter), -1);
            for (int i = 0, lineLength = line.length; i < lineLength; i++) {
                String lineSub = line[i];

                if (columns.size() > i) {
                    String s1 = columns.get(i);
                    s1 += delimiter + lineSub;
                    columns.set(i, s1);
                } else {
                    columns.add(lineSub);
                }

            }
        }

        if (quote != null) {
            List<String> columnsWOQuotes = new ArrayList<>();
            for (String s : columns) {
                String newStr = s.replaceAll(quote, "");
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
        logger.debug("MAP: {}", sb.toString());


        for (DataPoint dp : _dataPoints) {
            String mappingIdentifier = dp.getMappingIdentifier();
            Integer column = null;
            if (mappingIdentifier != null && dp.getValueIndex() == null) {
                column = getIntByIdentifier(mappingIdentifier, columnMap);
            } else if (dp.getValueIndex() == null) {
                column = dpIndex;
            }

            if (column != null) {
                dp.setValueIndex(column);
            }
        }
    }

    private Integer getIntByIdentifier(String mapIdent, Map<String, Integer> columnMap) {
        Integer result;
        for (Map.Entry<String, Integer> entry : columnMap.entrySet()) {
            String[] line = entry.getKey().split(String.valueOf(delimiter), -1);

            for (int i = 0; i < line.length; i++) {
                if (line[i].equals(mapIdent)) {
                    return i;
                }
            }
        }
        result = columnMap.get(mapIdent);
        if (result == null) {
            logger.debug("FIND MAP failed: {}", mapIdent);
            mapIdent = mapIdent.replace("ä", "?");
            mapIdent = mapIdent.replace("Ä", "?");
            mapIdent = mapIdent.replace("ü", "?");
            mapIdent = mapIdent.replace("Ü", "?");
            mapIdent = mapIdent.replace("ö", "?");
            mapIdent = mapIdent.replace("Ö", "?");
            mapIdent = mapIdent.replace("ß", "?");
            logger.debug("FIND MAP replaced: {}", mapIdent);
            result = columnMap.get(mapIdent);
            logger.debug("FIND MAP result: {}", result);
        }
        return result;
    }

    private void parseLine(String[] line) {
        logger.debug("Parse line: {}", line);
        DateTime dateTime = getDateTime(line);

        if (dateTime == null) {
            logger.debug("DateTime var is null: Date Error");
            report.addError(new LineError(-3, -2, null, "Date Error"));
            return;//if there is no date the whole line is invalid... or generate a date?
        } else {
            logger.debug("DateTime parsed: {}", dateTime);
        }

        for (DataPoint dp : _dataPoints) {
            try {
                logger.debug("-DP: value index: {}, mapping identifier: '{}', target: {}", dp.getValueIndex(), dp.getMappingIdentifier(), dp.getTarget());
                Integer valueIndex = dp.getValueIndex();
                String target = dp.getTarget();

                String sVal = null;
                Double value = null;
                sVal = line[valueIndex];
                logger.debug("-- ValueString: {}", sVal);

                //todo bind locale to language or location?? ad thousands separator without regex
                if (decimalSeparator == null || decimalSeparator.equals(",")) {
                    NumberFormat nf_in = NumberFormat.getNumberInstance(Locale.GERMANY);
                    value = nf_in.parse(sVal).doubleValue();
                } else if (decimalSeparator.equals(".")) {
                    NumberFormat nf_out = NumberFormat.getNumberInstance(Locale.UK);
                    value = nf_out.parse(sVal).doubleValue();
                }

                Result tempResult = new Result(target, value, dateTime);
                _results.add(tempResult);
                report.addSuccess(currLineIndex, valueIndex);
            } catch (Exception ex) {
                report.addError(new LineError(currLineIndex, -2, ex, "Unexpected Exception"));
//                ex.printStackTrace();
            }
        }
        logger.debug("Result: {}", _results.size());
    }

    public void parse(List<InputStream> inputList, DateTimeZone timeZone) {
        this.timeZone = timeZone;
        for (InputStream inputStream : inputList) {
            logger.info("Importing importSteam");
            _converter.convertInput(inputStream, charset);

            String[] stringArrayInput = (String[]) _converter.getConvertedInput(String[].class);

            if (charset.equals(StandardCharsets.UTF_8) && stringArrayInput.length > 0) {
                if (stringArrayInput[0].startsWith(UTF8_BOM)) {
                    stringArrayInput[0] = stringArrayInput[0].substring(1);
                }
            }

            logger.info("Total count of lines {}", stringArrayInput.length);
            if (dpType != null && dpType.equals("ROW")) {
                calculateColumns(stringArrayInput[dpIndex]);
            } else {
                calculateColumnsColumn(stringArrayInput);
            }
            logger.error("Total lines/columns: {}", stringArrayInput.length);

            if (dpType != null && dpType.equals("ROW")) {
                logger.debug("Traversing ROWs");
                for (int i = headerLines; i < stringArrayInput.length; i++) {
                    currLineIndex = i;
                    try {
                        //TODO 1,"1,1",1 is not working yet
                        String[] line = stringArrayInput[i].split(String.valueOf(delimiter), -1);
                        if (quote != null) {
                            line = removeQuotes(line);
                        }

                        parseLine(line);
                    } catch (Exception e) {
                        report.addError(new LineError(currLineIndex, -2, e, "Detect a Problem in the Parsing Process"));
                        logger.error("Detected a Problem in the Parsing Process in line {}", currLineIndex, e);
                    }
                }
            } else {
                logger.debug("Traversing Columns");
                for (int i = headerLines; i < stringArrayInput.length; i++) {
                    currLineIndex = i;
                    try {

                        String[] line;
                        if (quote != null) {
                            line = stringArrayInput[i].split(delimiter + "(?=(?:[^" + quote + "]*" + quote + "[^" + quote + "]*" + quote + ")*[^" + quote + "]*$)");
                            line = removeQuotes(line);
                        } else {
                            line = stringArrayInput[i].split(String.valueOf(delimiter), -1);
                        }

                        DateTime dateTime = getDateTime(line);

                        if (dateTime == null) {
                            report.addError(new LineError(-3, -2, null, "Date Error"));
                            logger.error("Detected a Problem in the Parsing Process in line {}. Date Error", currLineIndex);
                            return;
                        }


                        for (DataPoint dp : _dataPoints) {
                            for (String s : line) {
                                if (s.equals(dp.getMappingIdentifier())) {
                                    try {
                                        String mappingIdentifier = dp.getMappingIdentifier();
                                        Integer valueIndex = dp.getValueIndex();
                                        String target = dp.getTarget();

                                        String sVal = null;
                                        Double value = null;
                                        sVal = line[valueIndex];
                                        //todo bind locale to language or location?? add thousands separator without regex
                                        if (decimalSeparator == null || decimalSeparator.equals(",")) {
                                            NumberFormat nf_in = NumberFormat.getNumberInstance(Locale.GERMANY);
                                            value = nf_in.parse(sVal).doubleValue();
                                        } else if (decimalSeparator.equals(".")) {
                                            NumberFormat nf_out = NumberFormat.getNumberInstance(Locale.UK);
                                            value = nf_out.parse(sVal).doubleValue();
                                        }
                                        Result tempResult = new Result(target, value, dateTime);
                                        _results.add(tempResult);
                                        report.addSuccess(currLineIndex, valueIndex);
                                    } catch (Exception ex) {
                                        report.addError(new LineError(currLineIndex, -2, ex, "Unexpected Exception"));
                                        logger.error("Detect a Problem in the Parsing Process in line {}. Value parsing Error", currLineIndex);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        report.addError(new LineError(currLineIndex, -2, e, "Detected a Problem in the Parsing Process"));
                        logger.error("Detect a Problem in the Parsing Process in line {}", currLineIndex, e);
                    }
                }
            }
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Number of Results: " + _results.size());
            if (!_results.isEmpty()) {
                logger.info("LastResult Date {}, Target {}, Value {}", _results.get(_results.size() - 1).getDate(), _results.get(_results.size() - 1).getTargetStr(), _results.get(_results.size() - 1).getValue());
            } else {
                logger.error("Cant parse or cant find any data to parse");
            }
        }

        //print error report based on Logger level
        report.print();
        logger.info("Finished Importing importSteam");

    }

    private String[] removeQuotes(String[] line) {
        String[] removed = new String[line.length];
        for (int i = 0; i < line.length; i++) {
            removed[i] = line[i].replace(quote, "");
        }
        return removed;
    }

    private DateTime getDateTime(String[] line) {
        logger.debug("getDateTime column: {} pattern: '{}' line: {}", dateIndex, dateFormat, line);

        if (dateFormat == null) {
            logger.error("No date format found");
            return null;
        }
        String input = "";
        String pattern = "";
        try {
            String date = line[dateIndex].trim();
            pattern = dateFormat;
            input = date;

            if (timeFormat != null && timeIndex > -1) {
                String time = line[timeIndex].trim();
                pattern += " " + timeFormat;
                input += " " + time;
            }
            logger.debug("-Parse: pattern: {}, timezone: {}, input: '{}'", pattern, timeZone, input);
            return TimeConverter.parseDateTime(input, pattern, timeZone);
        } catch (Exception ex) {
            logger.warn("Pattern: {}", pattern);
            logger.warn("Date not parsable: {}", input);
            logger.warn("Line not parsable: {}", Arrays.toString(line));
            logger.warn("DateFormat: {}", dateFormat);
            logger.warn("DateIndex: {}", dateIndex);
            logger.warn("TimeFormat: {}", timeFormat);
            logger.warn("TimeIndex: {}", timeIndex);
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
        this.quote = _quote;
    }

    public void setDelimiter(String _delimiter) {
        this.delimiter = _delimiter;
    }

    public void setDateIndex(Integer _dateIndex) {
        this.dateIndex = _dateIndex;
    }

    public void setTimeIndex(Integer _timeIndex) {
        this.timeIndex = _timeIndex;
    }

    public void setDpIndex(Integer _dpIndex) {
        this.dpIndex = _dpIndex;
    }

    public void setDateFormat(String _dateFormat) {
        this.dateFormat = _dateFormat;
    }

    public void setTimeFormat(String _timeFormat) {
        this.timeFormat = _timeFormat;
    }

    public void setDecimalSeparator(String _decimalSeparator) {
        this.decimalSeparator = _decimalSeparator;
    }

    public void setThousandSeparator(String _thousandSeparator) {
        this.thousandSeparator = _thousandSeparator;
    }

    public void setDataPoints(List<DataPoint> _dataPoints) {
        this._dataPoints = _dataPoints;
    }

    public void setConverter(Converter _converter) {
        this._converter = _converter;
    }

    public void setHeaderLines(Integer _headerLines) {
        this.headerLines = _headerLines;
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
        String DECIMAL_SEPARATOR = "Decimal Separator";
        String TIME_FORMAT = "Time Format";
        String THOUSAND_SEPARATOR = "Thousand Separator";
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
