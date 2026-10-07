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

/**
 * @author broder
 */
public class CSVParser {
    private static final Logger logger = LogManager.getLogger(CSVParser.class);
    private static final String UTF8_BOM = "\uFEFF";
    private final List<Result> _results = new ArrayList<Result>();
    private final Map<ResultKey, Integer> resultIndexes = new LinkedHashMap<ResultKey, Integer>();
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
    private Charset charset;
    private List<DataPoint> _dataPoints = new ArrayList<DataPoint>();
    private Converter _converter;
    private int parsedCandidates;
    private int replacedDuplicates;

    /**
     * Adds a parsed value using last-file-wins semantics. Input streams are
     * expected to be ordered from the oldest to the newest file. If a later
     * stream contains the same target, attribute and timestamp, its value
     * replaces the value parsed from an earlier stream.
     */
    private void addResult(Result result) {
        parsedCandidates++;
        ResultKey key = new ResultKey(result);
        Integer existingIndex = resultIndexes.get(key);
        if (existingIndex == null) {
            resultIndexes.put(key, _results.size());
            _results.add(result);
        } else {
            replacedDuplicates++;
            Result previous = _results.set(existingIndex, result);
            logger.debug("Replacing duplicate CSV value for target {}, attribute {}, timestamp {}: {} -> {}",
                    result.getTargetStr(), result.getAttribute(), result.getDate(),
                    previous.getValue(), result.getValue());
        }
    }

    private String[] splitLine(String input) {
        String[] line;
        if (quote != null && !quote.isEmpty()) {
            line = input.split(delimiter + "(?=(?:[^" + quote + "]*" + quote + "[^" + quote + "]*" + quote + ")*[^" + quote + "]*$)", -1);
            line = removeQuotes(line);
        } else {
            line = input.split(String.valueOf(delimiter), -1);
        }
        return line;
    }

    private Map<DataPoint, Integer> resolveValueIndexes(String[][] table, boolean rowAlignment) {
        Map<DataPoint, Integer> indexes = new IdentityHashMap<DataPoint, Integer>();
        for (DataPoint dataPoint : _dataPoints) {
            Integer configuredIndex = dataPoint.getValueIndex();
            if (configuredIndex != null) {
                indexes.put(dataPoint, configuredIndex);
                continue;
            }

            String mappingIdentifier = dataPoint.getMappingIdentifier();
            if (mappingIdentifier == null || mappingIdentifier.trim().isEmpty()) {
                logger.error("CSV data point for target {} has neither Mapping Identifier nor Value Index", dataPoint.getTarget());
                continue;
            }

            Integer resolved = rowAlignment
                    ? findInRow(table, dpIndex, mappingIdentifier)
                    : findInColumn(table, dpIndex, mappingIdentifier);
            if (resolved == null) {
                logger.error("Mapping Identifier '{}' for target {} was not found in Datapoint Index {}",
                        mappingIdentifier, dataPoint.getTarget(), dpIndex == null ? null : dpIndex + 1);
            } else {
                indexes.put(dataPoint, resolved);
            }
        }
        return indexes;
    }

    private Integer findInRow(String[][] table, Integer rowIndex, String identifier) {
        if (rowIndex == null || rowIndex < 0 || rowIndex >= table.length) {
            logger.error("Datapoint Index must reference an existing row for ROW alignment");
            return null;
        }
        for (int column = 0; column < table[rowIndex].length; column++) {
            if (identifier.equals(table[rowIndex][column].trim())) {
                return column;
            }
        }
        return null;
    }

    private Integer findInColumn(String[][] table, Integer columnIndex, String identifier) {
        if (columnIndex == null || columnIndex < 0) {
            logger.error("Datapoint Index must reference an existing column for COLUMN alignment");
            return null;
        }
        for (int row = 0; row < table.length; row++) {
            String value = getCell(table, row, columnIndex);
            if (value != null && identifier.equals(value.trim())) {
                return row;
            }
        }
        return null;
    }

    private void parseRows(String[][] table) {
        logger.debug("Traversing ROW alignment (time axis from top to bottom)");
        Map<DataPoint, Integer> valueIndexes = resolveValueIndexes(table, true);
        int firstRow = headerLines == null ? 0 : Math.max(0, headerLines);
        for (int row = firstRow; row < table.length; row++) {
            if (dpIndex != null && row == dpIndex) {
                continue;
            }
            DateTime dateTime = getDateTime(getCell(table, row, dateIndex), getCell(table, row, timeIndex), row);
            if (dateTime == null) {
                report.addError(new LineError(row, -2, null, "Date Error"));
                continue;
            }
            for (DataPoint dataPoint : _dataPoints) {
                Integer valueIndex = valueIndexes.get(dataPoint);
                if (valueIndex != null) {
                    addDataPointValue(dataPoint, getCell(table, row, valueIndex), dateTime, row, valueIndex);
                }
            }
        }
    }

    private void parseColumns(String[][] table) {
        logger.debug("Traversing COLUMN alignment (time axis from left to right)");
        Map<DataPoint, Integer> valueIndexes = resolveValueIndexes(table, false);
        int firstColumn = headerLines == null ? 0 : Math.max(0, headerLines);
        int columns = getMaximumColumnCount(table);
        for (int column = firstColumn; column < columns; column++) {
            if (dpIndex != null && column == dpIndex) {
                continue;
            }
            DateTime dateTime = getDateTime(getCell(table, dateIndex, column), getCell(table, timeIndex, column), column);
            if (dateTime == null) {
                report.addError(new LineError(column, -2, null, "Date Error"));
                continue;
            }
            for (DataPoint dataPoint : _dataPoints) {
                Integer valueIndex = valueIndexes.get(dataPoint);
                if (valueIndex != null) {
                    addDataPointValue(dataPoint, getCell(table, valueIndex, column), dateTime, column, valueIndex);
                }
            }
        }
    }

    private void addDataPointValue(DataPoint dataPoint, String valueString, DateTime dateTime,
                                   int recordIndex, int valueIndex) {
        try {
            if (valueString == null || valueString.trim().isEmpty()) {
                return;
            }
            NumberFormat numberFormat = decimalSeparator == null || decimalSeparator.equals(",")
                    ? NumberFormat.getNumberInstance(Locale.GERMANY)
                    : NumberFormat.getNumberInstance(Locale.UK);
            Double value = numberFormat.parse(valueString.trim()).doubleValue();
            addResult(new Result(dataPoint.getTarget(), value, dateTime));
            report.addSuccess(recordIndex, valueIndex);
        } catch (Exception ex) {
            report.addError(new LineError(recordIndex, valueIndex, ex, "Value parsing error"));
            logger.warn("Could not parse CSV value '{}' for target {} at record {}",
                    valueString, dataPoint.getTarget(), recordIndex + 1, ex);
        }
    }

    private String getCell(String[][] table, Integer row, Integer column) {
        if (row == null || column == null || row < 0 || column < 0 || row >= table.length || column >= table[row].length) {
            return null;
        }
        return table[row][column];
    }

    private int getMaximumColumnCount(String[][] table) {
        int columns = 0;
        for (String[] row : table) {
            columns = Math.max(columns, row.length);
        }
        return columns;
    }

    public void parse(List<InputStream> inputList, DateTimeZone timeZone) {
        this.timeZone = timeZone;
        _results.clear();
        resultIndexes.clear();
        parsedCandidates = 0;
        replacedDuplicates = 0;
        int streamIndex = 0;
        for (InputStream inputStream : inputList) {
            streamIndex++;
            int candidatesBeforeStream = parsedCandidates;
            int replacementsBeforeStream = replacedDuplicates;
            logger.info("Importing importSteam");
            _converter.convertInput(inputStream, charset);

            String[] stringArrayInput = (String[]) _converter.getConvertedInput(String[].class);

            if (charset.equals(StandardCharsets.UTF_8) && stringArrayInput.length > 0) {
                if (stringArrayInput[0].startsWith(UTF8_BOM)) {
                    stringArrayInput[0] = stringArrayInput[0].substring(1);
                }
            }

            logger.info("Total count of lines {}", stringArrayInput.length);
            String[][] table = new String[stringArrayInput.length][];
            for (int row = 0; row < stringArrayInput.length; row++) {
                table[row] = splitLine(stringArrayInput[row]);
            }
            logger.info("CSV dimensions: rows={}, columns={}, alignment={}",
                    table.length, getMaximumColumnCount(table), dpType);

            if ("COLUMN".equals(dpType)) {
                parseColumns(table);
            } else {
                parseRows(table);
            }
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Number of Results: " + _results.size());
            if (!_results.isEmpty()) {
                logger.info("LastResult Date {}, Target {}, Value {}", _results.get(_results.size() - 1).getDate(), _results.get(_results.size() - 1).getTargetStr(), _results.get(_results.size() - 1).getValue());
            } else {
                logger.error("Cant parse or cant find any data to parse");
            }
            logger.info("CSV stream {}/{} parsed: candidates={}, duplicate replacements={}, unique results so far={}",
                    streamIndex, inputList.size(), parsedCandidates - candidatesBeforeStream,
                    replacedDuplicates - replacementsBeforeStream, _results.size());
        }

        //print error report based on Logger level
        report.print();
        logger.info("CSV parsing summary: streams={}, candidates={}, duplicate replacements={}, unique results={}",
                inputList.size(), parsedCandidates, replacedDuplicates, _results.size());
        logger.info("Finished Importing importSteam");

    }

    private DateTime getDateTime(String dateValue, String timeValue, int recordIndex) {
        if (dateFormat == null) {
            logger.error("No date format found");
            return null;
        }
        String input = "";
        String pattern = "";
        try {
            if (dateValue == null) {
                throw new IllegalArgumentException("Date Index is outside the CSV dimensions");
            }
            String date = dateValue.trim();
            pattern = dateFormat;
            input = date;

            if (timeFormat != null && !timeFormat.isEmpty() && timeIndex != null) {
                if (timeValue == null) {
                    throw new IllegalArgumentException("Time Index is outside the CSV dimensions");
                }
                String time = timeValue.trim();
                pattern += " " + timeFormat;
                input += " " + time;
            }
            logger.debug("-Parse: pattern: {}, timezone: {}, input: '{}'", pattern, timeZone, input);
            return TimeConverter.parseDateTime(input, pattern, timeZone);
        } catch (Exception ex) {
            logger.warn("Date not parsable at record {}: input='{}', pattern='{}', Date Index={}, Time Index={}",
                    recordIndex + 1, input, pattern,
                    dateIndex == null ? null : dateIndex + 1,
                    timeIndex == null ? null : timeIndex + 1, ex);
            return null;
        }
    }

    private String[] removeQuotes(String[] line) {
        String[] removed = new String[line.length];
        for (int i = 0; i < line.length; i++) {
            removed[i] = line[i].replace(quote, "");
        }
        return removed;
    }

    private static final class ResultKey {
        private final String target;
        private final String attribute;
        private final long timestamp;

        private ResultKey(Result result) {
            this.target = result.getTargetStr();
            this.attribute = result.getAttribute();
            this.timestamp = result.getDate().getMillis();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ResultKey)) {
                return false;
            }
            ResultKey other = (ResultKey) obj;
            return timestamp == other.timestamp
                    && Objects.equals(target, other.target)
                    && Objects.equals(attribute, other.attribute);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, attribute, timestamp);
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