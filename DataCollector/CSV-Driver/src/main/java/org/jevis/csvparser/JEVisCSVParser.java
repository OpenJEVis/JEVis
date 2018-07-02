/**
 * Copyright (C) 2015 - 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEVis CSV-Driver.
 *
 * JEVis CSV-Driver is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEVis CSV-Driver is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEVis CSV-Driver. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEVis CSV-Driver is part of the OpenJEVis project, further project
 * information are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.csvparser;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.jevis.commons.driver.inputHandler.GenericConverter;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author broder
 */
public class JEVisCSVParser implements Parser {

    private DateTimeZone timeZone;
    public static final String VERSION = "Version 1.2.1 2017-08-28";
    
    public static void main(String[] args) throws FileNotFoundException {
        InputStream is = new FileInputStream("/home/bi/Downloads/CineStar_Kundenexport_senkrecht_5Tage_2017.08.28_093043.CSV");
        CSVParser parser = new CSVParser();
        parser.setConverter(new GenericConverter());
        DataPoint dp2 = new DataPoint();
        dp2.setMappingIdentifier("B03(2)-Wirkleistung");
        dp2.setTarget(10l);
        parser.setDataPoints(Arrays.asList(dp2));
        parser.setDateFormat("dd.MM.yyyy");
        parser.setDateIndex(0);
        parser.setTimeFormat("HH:mm");
        parser.setTimeIndex(1);
        parser.setDelim(";");
        parser.setDpIndex(5);
        parser.setDpType("ROW");
        parser.setHeaderLines(10);
        parser.setDecimalSeperator(",");
        parser.parse(Arrays.asList(is), DateTimeZone.forID("Europe/Berlin"));
        List<Result> result = parser.getResult();
        for (Result r : result) {
            Long onlineID = r.getOnlineID();
            Object value = r.getValue();
            String toString = r.getDate().toString(DateTimeFormat.fullDateTime());
            if (onlineID != null && onlineID == 10) {
                System.out.println(onlineID + ";" + value + ";" + toString);
            }
        }
        System.out.println("---"+result.size());
        
        InputStream is2 = new FileInputStream("/home/bi/Downloads/CineStar_Kundenexport_senkrecht_5Tage_2017.08.24_093055.CSV");
        CSVParser np = new CSVParser();
        np.setConverter(new GenericConverter());
        
        DataPoint dpy = new DataPoint();
        dpy.setValueIndex(7);
        dpy.setTarget(10l);
        np.setDataPoints(Arrays.asList(dpy));
        np.setDateFormat("dd.MM.yyyy");
        np.setDateIndex(0);
        np.setTimeFormat("HH:mm");
        np.setTimeIndex(1);
        np.setDelim(";");
        np.setHeaderLines(10);
        np.setDecimalSeperator(",");
        
        np.parse(Arrays.asList(is2), DateTimeZone.forID("Europe/Berlin"));
        List<Result> otherResult = np.getResult();
        for (Result r : otherResult) {
            Long onlineID = r.getOnlineID();
            Object value = r.getValue();
            String toString = r.getDate().toString(DateTimeFormat.fullDateTime());
            if (onlineID != null && onlineID == 10) {
                System.out.println(onlineID + ";" + value + ";" + toString);
            }
        }
        
        System.out.println("+++"+otherResult.size());
        
        System.out.println("results are equals: "+otherResult.containsAll(result));
    }

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
            JEVisType decimalSeperatorType = jeClass.getType(CSVParserTypes.DECIMAL_SEPERATOR);
            JEVisType thousandSeperatorType = jeClass.getType(CSVParserTypes.THOUSAND_SEPERATOR);
            JEVisType charsetType = jeClass.getType(CSVParserTypes.CHARSET);

            String delim = DatabaseHelper.getObjectAsString(parserObject, seperatorColumn);
            String quote = DatabaseHelper.getObjectAsString(parserObject, enclosedBy);
            Integer headerLines = DatabaseHelper.getObjectAsInteger(parserObject, ignoreFirstNLines);
            if (headerLines == null) {
                headerLines = 0;
            } else {
                headerLines--;
            }
            Integer dpIndex = DatabaseHelper.getObjectAsInteger(parserObject, dpIndexType);
            if (dpIndex != null) {
                dpIndex--;
            }

            String dpType = DatabaseHelper.getObjectAsString(parserObject, dpTypeType);

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
            if(charset==null || charset.equals("")){
               cset = Charset.defaultCharset();
            } else {
               cset = Charset.forName(charset);
            }

            String dateFormat = DatabaseHelper.getObjectAsString(parserObject, dateFormatType);

            String timeFormat = DatabaseHelper.getObjectAsString(parserObject, timeFormatType);

            String decimalSeperator = DatabaseHelper.getObjectAsString(parserObject, decimalSeperatorType);

            String thousandSeperator = DatabaseHelper.getObjectAsString(parserObject, thousandSeperatorType);

            _csvParser = new CSVParser();
            _csvParser.setDateFormat(dateFormat);
            System.out.println("Date Format: " + dateFormat);
            _csvParser.setDateIndex(dateIndex);
            System.out.println("DateIndex: " + dateIndex);
            _csvParser.setDecimalSeperator(decimalSeperator);
            System.out.println("DecimalSeparator" + decimalSeperator);
            _csvParser.setDelim(delim);
            System.out.println("delim: " + delim);
            _csvParser.setDpIndex(dpIndex);
            System.out.println("dpIndex: " + dpIndex);
            _csvParser.setDpType(dpType);
            System.out.println("dpType: " + dpType);
            _csvParser.setHeaderLines(headerLines);
            System.out.println("headerlines: " + headerLines);
            _csvParser.setQuote(quote);
            System.out.println("quote: " + quote);
            _csvParser.setThousandSeperator(thousandSeperator);
            System.out.println("thousandsSeparator: " + thousandSeperator);
            _csvParser.setTimeFormat(timeFormat);
            System.out.println("timeFormat: " + timeFormat);
            _csvParser.setTimeIndex(timeIndex);
            System.out.println("timeIndex: " + timeIndex);
            _csvParser.setCharset(cset);
            System.out.println("charset: " + cset);

        } catch (JEVisException ex) {
            Logger.getLogger(org.jevis.csvparser.JEVisCSVParser.class
                    .getName()).log(Level.ERROR, null, ex);
        }
    }

    private void initializeCSVDataPointParser(JEVisObject parserObject) {
        try {
            System.out.println("initialize csvDataPointParser");
            JEVisClass dirClass = parserObject.getDataSource().getJEVisClass(CSVDataPointDirectoryTypes.NAME);
            JEVisObject dir = parserObject.getChildren(dirClass, true).get(0);
            JEVisClass dpClass = parserObject.getDataSource().getJEVisClass(CSVDataPointTypes.NAME);
            List<JEVisObject> dataPoints = dir.getChildren(dpClass, true);
            List<DataPoint> csvdatapoints = new ArrayList<DataPoint>();
            for (JEVisObject dp : dataPoints) {
                System.out.println("DataPoint: " + dp.getName() + " ID: " + dp.getID());
                JEVisType mappingIdentifierType = dpClass.getType(CSVDataPointTypes.MAPPING_IDENTIFIER);
                JEVisType targetType = dpClass.getType(CSVDataPointTypes.TARGET);
                JEVisType valueIdentifierType = dpClass.getType(CSVDataPointTypes.VALUE_INDEX);

                Long datapointID = dp.getID();
                String mappingIdentifier = DatabaseHelper.getObjectAsString(dp, mappingIdentifierType);
                String targetString = DatabaseHelper.getObjectAsString(dp, targetType);
                Long target = null;
                try {
                    target = Long.parseLong(targetString);
                } catch (Exception ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DataPoint target error: " + ex.getMessage());
//                    ex.printStackTrace();
                }
                String valueString = DatabaseHelper.getObjectAsString(dp, valueIdentifierType);
                Integer valueIndex = null;
                try {
                    valueIndex = Integer.parseInt(valueString);
                    valueIndex--;
                } catch (Exception ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DataPoint ValueIdentidier error: " + ex.getMessage());
//                    ex.printStackTrace();
                }
                DataPoint csvdp = new DataPoint();
                csvdp.setMappingIdentifier(mappingIdentifier);
                csvdp.setTarget(target);
                System.out.println("target: " + csvdp.getTarget());
                csvdp.setValueIndex(valueIndex);
                csvdatapoints.add(csvdp);
            }
            _csvParser.setDataPoints(csvdatapoints);
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(JEVisCSVParser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
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

    private CSVParser _csvParser;

    /**
     * @param inputList
     * @param timeZone
     */
    @Override
    public void parse(List<InputStream> inputList, DateTimeZone timeZone) {
        _csvParser.parse(inputList, timeZone);
    }

    @Override
    public List<Result> getResult() {
        System.out.println("getResult form JEVisCSVParser: " + _csvParser.getResult().size());
        return _csvParser.getResult();
    }

    @Override
    public void initialize(JEVisObject parserObject) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Initialize JEVisCSVParser build: " + VERSION);
        System.out.println("Initialize JEVisCSVParser build: " + VERSION);

        initializeAttributes(parserObject);
        System.out.println("initialized Attributes");

        Converter converter = ConverterFactory.getConverter(parserObject);
        _csvParser.setConverter(converter);

        initializeCSVDataPointParser(parserObject);
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
