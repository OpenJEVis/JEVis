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
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jevis.commons.datetime.JodaConverters;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.ParserReport;
import org.jevis.commons.driver.Result;
import org.jevis.commons.driver.TimeConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gerrit.schutz@envidatec.com
 */
public class XLSXParser {
    private static final Logger logger = LogManager.getLogger(XLSXParser.class);
    private final List<Result> results = new ArrayList<Result>();
    private final ParserReport report = new ParserReport();
    private DateTimeZone timeZone;
    private String dpType;
    private Integer headerLines;
    private Integer dateIndex;
    private Integer timeIndex;
    private Integer dpIndex;
    private List<DataPoint> dataPoints = new ArrayList<DataPoint>();
    private String dateFormat;
    private String timeFormat;

    public void parse(List<InputStream> inputList, DateTimeZone timeZone) {
        this.timeZone = timeZone;
        for (InputStream inputStream : inputList) {
            logger.info("Importing importSteam");

            try {
                XSSFWorkbook wb = new XSSFWorkbook(inputStream);
                XSSFSheet sheet = wb.getSheetAt(0);
                XSSFRow row;
                XSSFCell cell = null;
                XSSFCell dateCell;
                XSSFCell timeCell;

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

                int currLineIndex = 0;
                if (headerLines != null) {
                    currLineIndex = currLineIndex + headerLines;
                }

                if (dpType != null && dpType.equals("ROW")) {
                    for (int r = currLineIndex; r < rows; r++) {
                        row = sheet.getRow(r);
                        dateCell = null;
                        timeCell = null;

                        if (row != null) {
                            dateCell = row.getCell(dateIndex);
                            if (timeIndex != null) {
                                timeCell = row.getCell(timeIndex);
                            }
                            DateTime dateTime = null;

                            dateTime = getDateForCell(dateCell, timeCell);

                            for (DataPoint dataPoint : dataPoints) {
                                if (dataPoint.getValueIndex() != null) {
                                    cell = row.getCell(dataPoint.getValueIndex());
                                } else {
                                    XSSFRow dpNameRow = sheet.getRow(dpIndex);
                                    for (int c = 0; c < cols; c++) {
                                        XSSFCell mappingCell = dpNameRow.getCell(c);
                                        try {
                                            if (mappingCell.getStringCellValue().equals(dataPoint.getMappingIdentifier())) {
                                                cell = row.getCell(c);
                                                break;
                                            }
                                        } catch (Exception e) {
                                            logger.error(e);
                                        }
                                    }
                                }

                                if (dateTime != null && cell != null) {
                                    Result result = null;
                                    if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().isEmpty()) {
                                        result = new Result(dataPoint.getTarget(), cell.getStringCellValue(), dateTime);
                                    } else if (cell.getCellType() == CellType.NUMERIC) {
                                        try {
                                            result = new Result(dataPoint.getTarget(), cell.getNumericCellValue(), dateTime);
                                        } catch (Exception e) {
                                            logger.error("Numeric cell type but no valid number", e);
                                        }
                                    }

                                    if (result != null) {
                                        results.add(result);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    XSSFRow dateRow = sheet.getRow(dateIndex);
                    for (int r = currLineIndex; r < rows; r++) {

                        for (int c = dpIndex; c < cols; c++) {
                            DataPoint currentDatapoint = null;
                            dateCell = null;
                            timeCell = null;
                            DateTime dateTime = null;

                            dateCell = dateRow.getCell(c);
                            dateTime = getDateForCell(dateCell, timeCell);

                            for (DataPoint dataPoint : dataPoints) {
                                if (dataPoint.getValueIndex() != null && c == dataPoint.getValueIndex()) {
                                    currentDatapoint = dataPoint;
                                    break;
                                } else {
                                    for (int r1 = 0; r1 < rows; r1++) {
                                        XSSFCell mappingCell = sheet.getRow(r1).getCell(c);
                                        try {
                                            if (mappingCell.getStringCellValue().equals(dataPoint.getMappingIdentifier())) {
                                                currentDatapoint = dataPoint;
                                                break;
                                            }
                                        } catch (Exception e) {
                                            logger.error(e);
                                        }
                                    }
                                }
                            }

                            cell = sheet.getRow(r).getCell(c);

                            if (currentDatapoint != null && dateTime != null && cell != null) {
                                Result result = null;
                                if (cell.getCellType() == CellType.STRING) {
                                    result = new Result(currentDatapoint.getTarget(), cell.getStringCellValue(), dateTime);
                                } else if (cell.getCellType() == CellType.NUMERIC) {
                                    result = new Result(currentDatapoint.getTarget(), cell.getNumericCellValue(), dateTime);
                                }

                                if (result != null) {
                                    results.add(result);
                                }
                            }
                        }
                    }
                }

                if (!results.isEmpty()) {
                    logger.info("LastResult Date {}, Target {}, Value {}", results.get(results.size() - 1).getDate(), results.get(results.size() - 1).getTargetStr(), results.get(results.size() - 1).getValue());
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

    private DateTime getDateForCell(XSSFCell dateCell, XSSFCell timeCell) {

        if (dateFormat == null && timeFormat == null) {
            if (DateUtil.isCellDateFormatted(dateCell)) {
                LocalDateTime dateCellValue = dateCell.getLocalDateTimeCellValue();
                return JodaConverters.javaToJodaLocalDateTime(dateCellValue).toDateTime();
            }
        } else {
            String input = "";
            String pattern = "";
            try {
                String date = dateCell.getStringCellValue();
                pattern = dateFormat;
                input = date;

                if (timeFormat != null && timeIndex > -1) {
                    String time = timeCell.getStringCellValue();
                    pattern += " " + timeFormat;
                    input += " " + time;
                }
                logger.debug("-Parse: pattern: {}, timezone: {}, input: '{}'", pattern, timeZone, input);
                return TimeConverter.parseDateTime(input, pattern, timeZone);
            } catch (Exception e) {
                logger.error(e);
            }
        }

        return null;
    }

    public ParserReport getReport() {
        return report;
    }

    public void setDpType(String dpType) {
        this.dpType = dpType;
    }

    public List<Result> getResult() {
        return results;
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

    public void setDataPoints(List<DataPoint> _dataPoints) {
        this.dataPoints = _dataPoints;
    }

    public void setHeaderLines(Integer _headerLines) {
        this.headerLines = _headerLines;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    // interfaces
    interface XLSX extends DataCollectorTypes.Parser {

        String NAME = "XLSX Parser";
        String DATAPOINT_INDEX = "Datapoint Index";
        String DATE_INDEX = "Date Index";
        String NUMBER_HEADLINES = "Number Of Headlines";
        String TIME_INDEX = "Time Index";
    }

    interface XLSXDataPointDirectory extends DataCollectorTypes.DataPointDirectory {

        String NAME = "XLSX Data Point Directory";
    }

    interface XLSXDataPoint extends DataCollectorTypes.DataPoint {

        String NAME = "XLSX Data Point";
        String MAPPING_IDENTIFIER = "Mapping Identifier";
        String VALUE_INDEX = "Value Index";
        String TARGET = "Target";

    }
}
