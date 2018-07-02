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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.commons.driver.Converter;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.Result;
import org.jevis.commons.driver.TimeConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author broder
 */
public class CSVParser {

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
    private String _decimalSeperator;
    private String _thousandSeperator;
    private Integer _currLineIndex;
    private Charset charset;

    private List<Result> _results = new ArrayList<Result>();
    private List<DataPoint> _dataPoints = new ArrayList<DataPoint>();
    private Converter _converter;
    private CSVReport _report = new CSVReport();

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
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "MAP: " + sb.toString());


        for (DataPoint dp : _dataPoints) {
            String mappingIdentifier = dp.getMappingIdentifier();
            //VERY VERY VERY DIRTY CODE, PLEASE DONT USE IT
            Integer column = getColumnByIdentifier(mappingIdentifier, columnMap);
            dp.setValueIndex(column);
        }
    }

    //KRUCKE
    private Integer getColumnByIdentifier(String mapIdent, Map<String, Integer> columnMap) {
        Integer result;
        result = columnMap.get(mapIdent);
        if (result == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "FIND MAP failed: " + mapIdent);
            mapIdent = mapIdent.replace("ä", "?");
            mapIdent = mapIdent.replace("Ä", "?");
            mapIdent = mapIdent.replace("ü", "?");
            mapIdent = mapIdent.replace("Ü", "?");
            mapIdent = mapIdent.replace("ö", "?");
            mapIdent = mapIdent.replace("Ö", "?");
            mapIdent = mapIdent.replace("ß", "?");
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "FIND MAP replaced: " + mapIdent);
            result = columnMap.get(mapIdent);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "FIND MAP result: " + result);
        }
        return result;
    }

    private void parseLine(String[] line) {
        System.out.println("Parse Line");
        DateTime dateTime = getDateTime(line);
        System.out.println("DateTime: " + dateTime);

        if (dateTime == null) {
            _report.addError(new LineError(-3, -2, null, "Date Error"));
            return;//if there is no date the whole line is invalid... or generate a date?
        }

        for (DataPoint dp : _dataPoints) {
            try {
                String mappingIdentifier = dp.getMappingIdentifier();
                Integer valueIndex = dp.getValueIndex();
                Long target = dp.getTarget();

                Boolean mappingError = false;
                try {
                    String currentMappingValue = null;

                    if (_dpIndex != null) {
                        if (dpType.equals("COLUMN")) {
                            currentMappingValue = line[_dpIndex];
                        }
                    }

                    if (mappingIdentifier != null && !currentMappingValue.equals(mappingIdentifier)) {
                        mappingError = true;
                        _report.addError(new LineError(_currLineIndex, valueIndex, null, "Mapping Error"));
                        continue;
                    }
                } catch (Exception ex) {
                    _report.addError(new LineError(_currLineIndex, valueIndex, ex, "Mapping Exeption"));
//                    Logger.getLogger(this.getClass().getName()).log(Level.WARN, "This line in the file is not valid: " + _currLineIndex);
                }

                Boolean valueValid = false;
                String sVal = null;
                Double value = null;
                //               try {
                sVal = line[valueIndex];
                System.out.println("lineValue: " + sVal);
//                if (_thousandSeperator != null && !_thousandSeperator.equals("")) {
//                    sVal = sVal.replaceAll("\\" + _thousandSeperator, "");
//                }
//                if (_decimalSeperator != null && !_decimalSeperator.equals("")) {
//                    sVal = sVal.replaceAll("\\" + _decimalSeperator, ".");
//                }

                //todo bind locale to language or location?? ad thousands separator without regex
                if (_decimalSeperator == null || _decimalSeperator.equals(",")) {
                    NumberFormat nf_in = NumberFormat.getCurrencyInstance(Locale.GERMANY);
                    value = nf_in.parse(sVal).doubleValue();
                } else if (_decimalSeperator.equals(".")) {
                    NumberFormat nf_out = NumberFormat.getNumberInstance(Locale.UK);
                    value = nf_out.parse(sVal).doubleValue();
                }
                System.out.println("Value: " + value);
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
                System.out.println("tempResult: " + target + " Value: " + value + " dateTime: " + dateTime);
                _results.add(tempResult);
                _report.addSuccess(_currLineIndex, valueIndex);
            } catch (Exception ex) {
                _report.addError(new LineError(_currLineIndex, -2, ex, "Unexpected Exception"));
//                ex.printStackTrace();
            }
        }
        System.out.println("Results: " + _results.size());
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

    public void parse(List<InputStream> inputList, DateTimeZone timeZone) {
        Logger.getLogger(this.getClass().getName()).log(Level.ALL, "Start CSV parsing");
        this.timeZone = timeZone;
        for (InputStream inputStream : inputList) {

            _converter.convertInput(inputStream, charset);
            String[] stringArrayInput = (String[]) _converter.getConvertedInput(String[].class);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Total count of lines " + stringArrayInput.length);
            System.out.println("Total count of lines: " + stringArrayInput.length);
            if (dpType != null && dpType.equals("ROW")) {
                calculateColumns(stringArrayInput[_dpIndex]);
            }

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
                    _report.addError(new LineError(_currLineIndex, -2, e, "Detect a Problem in the Parsing Process"));
//                    Logger.getLogger(this.getClass().getName()).log(Level.WARN, "Detect a Problem in the Parsing Process");
                }
            }
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Number of Results: " + _results.size());
            if (!_results.isEmpty()) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "LastResult (Date,Target,Value): " + _results.get(_results.size() - 1).getDate() + "," + _results.get(_results.size() - 1).getOnlineID() + "," + _results.get(_results.size() - 1).getValue());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.ERROR, "Cant parse or cant find any parsable data");
            }
        }

        //print error report based on Logger level
        _report.print();

    }

    interface CSVDataPoint extends DataCollectorTypes.DataPoint {

        String NAME = "CSV Data Point";
        String MAPPING_IDENTIFIER = "Mapping Identifier";
        String VALUE_INDEX = "Value Index";
        String TARGET = "Target";

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
            Logger.getLogger(this.getClass().getName()).log(Level.ALL, "No Datetime found");
            return new DateTime();
        }
        String input = "";
        try {
            String date = line[_dateIndex].trim();
            String pattern = _dateFormat;
            input = date;

            if (_timeFormat != null && _timeIndex > -1) {
                String time = line[_timeIndex].trim();
                pattern += " " + _timeFormat;
                input += " " + time;
            }
//            Logger.getLogger(this.getClass().getName()).log(Level.ALL, "complete time " + format);
//            Logger.getLogger(this.getClass().getName()).log(Level.ALL, "complete pattern " + pattern);

//            DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
            DateTime parsedDateTime = TimeConverter.parserDateTime(input, pattern, timeZone);
            return parsedDateTime;
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARN, "Date not parsable: " + input);
            Logger.getLogger(this.getClass().getName()).log(Level.WARN, "LINE not parsable: " + Arrays.toString(line));
            Logger.getLogger(this.getClass().getName()).log(Level.WARN, "DateFormat: " + _dateFormat);
            Logger.getLogger(this.getClass().getName()).log(Level.WARN, "DateIndex: " + _dateIndex);
            Logger.getLogger(this.getClass().getName()).log(Level.WARN, "TimeFormat: " + _timeFormat);
            Logger.getLogger(this.getClass().getName()).log(Level.WARN, "TimeIndex: " + _timeIndex);
            Logger.getLogger(this.getClass().getName()).log(Level.WARN, "Exception: " + ex);
            return null;
        }

    }

    public void setDpType(String dpType) {
        this.dpType = dpType;
    }

    public List<Result> getResult() {
        System.out.println("getResult: " + _results.size());
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
        this._decimalSeperator = _decimalSeperator;
    }

    public void setThousandSeperator(String _thousandSeperator) {
        this._thousandSeperator = _thousandSeperator;
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
}
