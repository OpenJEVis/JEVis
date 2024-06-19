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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DateUtil;
import org.jevis.commons.datetime.JodaConverters;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.ParserReport;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class XLSXParser {
    private static final Logger logger = LogManager.getLogger(XLSXParser.class);
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

    public void parse(List<InputStream> inputList, DateTimeZone timeZone) {
        this.timeZone = timeZone;
        for (InputStream inputStream : inputList) {
            logger.info("Importing importSteam");

            try {
                POIFSFileSystem fs = new POIFSFileSystem(inputStream);
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(0);
                HSSFRow row;
                HSSFCell cell;
                HSSFCell dateCell;

                int rows; // No of rows
                rows = sheet.getPhysicalNumberOfRows();

                int cols = 0; // No of columns
                int tmp = 0;

                // This trick ensures that we get the data properly even if it doesn't start from first few rows
                for (int i = 0; i < 10 || i < rows; i++) {
                    row = sheet.getRow(i);
                    if (row != null) {
                        tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                        if (tmp > cols) cols = tmp;
                    }
                }

                if (dpType != null && dpType.equals("ROW")) {
                    logger.info("Traversing ROWs");
                    for (int r = 0; r < rows; r++) {
                        row = sheet.getRow(r);
                        if (row != null) {
                            dateCell = row.getCell(dateIndex);
                            DateTime dateTime;
                            if (DateUtil.isCellDateFormatted(dateCell)) {
                                LocalDateTime dateCellValue = dateCell.getLocalDateTimeCellValue();
                                dateTime = JodaConverters.javaToJodaLocalDateTime(dateCellValue).toDateTime();
                            }

                            for (DataPoint dataPoint : _dataPoints) {
                                cell = row.getCell(dataPoint.getValueIndex());
                                if (cell != null) {
                                    // Your code here
                                }
                            }

                            for (int c = 0; c < cols; c++) {
                                cell = row.getCell((short) c);
                                if (cell != null) {
                                    // Your code here
                                }
                            }
                        }
                    }
                }

                if (!_results.isEmpty()) {
                    logger.info("LastResult Date {}, Target {}, Value {}", _results.get(_results.size() - 1).getDate(), _results.get(_results.size() - 1).getTargetStr(), _results.get(_results.size() - 1).getValue());
                } else {
                    logger.error("Cant parse or cant find any data to parse");
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }

        //print error report based on Logger level
        report.print();
        logger.info("Finished Importing importSteam");

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
