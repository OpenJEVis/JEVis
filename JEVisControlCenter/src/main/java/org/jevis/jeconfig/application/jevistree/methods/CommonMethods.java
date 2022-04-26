package org.jevis.jeconfig.application.jevistree.methods;

import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CommonMethods {
    private static final Logger logger = LogManager.getLogger(CommonMethods.class);

    public static final String RECALCULATION = "Recalculation";
    public static final String WAIT_FOR_TIMEZONE = "Wait for TZ";

    public static JEVisFile createXLSXFile(String name, TableView tableView) throws IOException {
        if (name == null || name.equals("")) {
            name = "table";
        }
        DateTime now = new DateTime();
        name += "_" + now.toString(" YYYYMMdd ");
        XSSFWorkbook workbook = new XSSFWorkbook(); //create workbook

        XSSFFont defaultFont = workbook.createFont();
        defaultFont.setFontHeightInPoints((short) 10);
        defaultFont.setFontName("Arial");
        defaultFont.setColor(IndexedColors.BLACK.getIndex());
        defaultFont.setBold(false);
        defaultFont.setItalic(false);

        XSSFFont boldFont = workbook.createFont();
        boldFont.setFontHeightInPoints((short) 10);
        boldFont.setFontName("Arial");
        boldFont.setColor(IndexedColors.BLACK.getIndex());
        boldFont.setBold(true);
        boldFont.setItalic(false);

        XSSFFont boldHeaderFont = workbook.createFont();
        boldHeaderFont.setFontHeightInPoints((short) 12);
        boldHeaderFont.setFontName("Arial");
        boldHeaderFont.setColor(IndexedColors.BLACK.getIndex());
        boldHeaderFont.setBold(true);
        boldHeaderFont.setItalic(false);

        CellStyle defaultStyle = workbook.createCellStyle();
        defaultStyle.setFont(defaultFont);

        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);

        CellStyle boldHeaderStyle = workbook.createCellStyle();
        boldHeaderStyle.setFont(boldHeaderFont);

        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
        CellStyle cellStyleDates = workbook.createCellStyle();
        cellStyleDates.setDataFormat((short) 165);
        cellStyleDates.setFont(defaultFont);

        CellStyle cellStyleValues = workbook.createCellStyle();
        cellStyleValues.setDataFormat((short) 4);
        cellStyleValues.setFont(defaultFont);

        List<TableColumn> allColumns = tableView.getColumns();
        List<TableColumn> visibleColumns = allColumns.stream().filter(TableColumnBase::isVisible).collect(Collectors.toList());
        int width = visibleColumns.size();
        List<List<TableColumn>> lists = new ArrayList<>();

        if (width > 50) {
            int noOfSubLists = width / 50 + 1;
            for (int i = 0; i < noOfSubLists; i++) {
                List<TableColumn> subList = new ArrayList<>();
                lists.add(subList);
            }

            int i = 0;
            for (int j = 0; j < lists.size(); j++) {
                while (i < visibleColumns.size() && i < (50 * (j + 1))) {
                    List<TableColumn> columns = lists.get(j);
                    TableColumn column = visibleColumns.get(i);
                    columns.add(column);
                    i++;
                }
            }

        } else {
            lists.add(visibleColumns);
        }

        for (List<TableColumn> tableColumns : lists) {
            int number = lists.indexOf(tableColumns);
            int sheetWidth = tableColumns.size();
            Sheet sheet = workbook.createSheet("Data" + number); //create sheet
            String lastCellColumnName = CellReference.convertNumToColString(sheetWidth);

            for (int i = 0; i < tableColumns.size(); i++) {
                TableColumn column = tableColumns.get(i);
                Cell caption = getOrCreateCell(sheet, 0, i);
                caption.setCellValue(column.getText());
                caption.setCellStyle(boldHeaderStyle);

                int counter = 1;
                for (Object item : tableView.getItems()) {
                    Cell attributeValueCell = getOrCreateCell(sheet, counter, i);
                    attributeValueCell.setCellValue((String) column.getCellObservableValue(item).getValue());
                    attributeValueCell.setCellStyle(defaultStyle);

                    counter++;
                }
            }
        }

        Path templatePath = Files.createTempFile("template", "xlsx");
        File templateFile = new File(templatePath.toString());
        templateFile.deleteOnExit();
        workbook.write(new FileOutputStream(templateFile));
        workbook.close();
        return new JEVisFileImp(name + ".xlsx", templateFile);
    }

    private static Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }

        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }

        return cell;
    }

    public static JEVisObject getFirstParentalDataObject(JEVisObject jeVisObject) throws JEVisException {
        for (JEVisObject object : jeVisObject.getParents()) {
            if (object.getJEVisClassName().equals("Data")) {
                return object;
            } else {
                return getFirstParentalDataObject(object);
            }
        }
        return jeVisObject;
    }

    public static JEVisObject getFirstCleanObject(JEVisObject jeVisObject) throws JEVisException {
        for (JEVisObject object : jeVisObject.getChildren()) {
            if (object.getJEVisClassName().equals("Data") || object.getJEVisClassName().equals("Clean data")) {
                return object;
            } else {
                return getFirstCleanObject(object);
            }
        }
        return jeVisObject;
    }

    public static void setEnabled(ProgressForm pForm, JEVisObject object, String selectedClass, boolean b) {
        try {
            if (object.getJEVisClassName().equals(selectedClass) || selectedClass.equals("All")) {
                JEVisAttribute enabled = object.getAttribute("Enabled");
                if (enabled != null) {
                    JEVisSample sample = enabled.buildSample(new DateTime(), b);
                    sample.commit();
                    pForm.addMessage("Set enabled attribute of object " + object.getName() + ":" + object.getID() + " to " + b);
                }
            }
            for (JEVisObject child : object.getChildren()) {
                setEnabled(pForm, child, selectedClass, b);
            }
        } catch (Exception e) {
            logger.error("Could not set enabled for {}:{}", object.getName(), object.getID());
        }
    }

    public static void checkForActiveRecalculation(TimeZone timeZone, DateTimeZone dateTimeZone) throws InterruptedException {
        Thread.sleep(1000);
        AtomicBoolean hasActiveCleaning = new AtomicBoolean(false);
        ConcurrentHashMap<Task, String> taskList = JEConfig.getStatusBar().getTaskList();
        for (Map.Entry<Task, String> entry : taskList.entrySet()) {
            String s = entry.getValue();
            if (s.equals(RECALCULATION)) {
                hasActiveCleaning.set(true);
                break;
            }
        }
        if (!hasActiveCleaning.get()) {
            logger.debug("Setting default timezone to old default {}", timeZone.getID());
            TimeZone.setDefault(timeZone);
            DateTimeZone.setDefault(dateTimeZone);
        } else {
            Thread.sleep(1000);
            checkForActiveRecalculation(timeZone, dateTimeZone);
        }
    }

    public static void deleteSamplesInList(ProgressForm pForm, DateTime from, DateTime to, List<JEVisObject> list) {
        for (JEVisObject object : list) {
            JEVisAttribute valueAtt = null;
            try {
                valueAtt = object.getAttribute("Value");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (valueAtt != null) {
                if (from == null && to == null) {
                    try {
                        pForm.addMessage("Deleting all samples of object " + object.getName() + ":" + object.getID());
                        valueAtt.deleteAllSample();

                        allSamplesMathData(object, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (from != null && to != null) {
                    try {
                        pForm.addMessage("Deleting samples of object " + object.getName() + ":" + object.getID()
                                + " from " + from.toString("YYYY-MM-dd HH:mm:ss") + " to " + to.toString("YYYY-MM-dd HH:mm:ss"));
                        valueAtt.deleteSamplesBetween(from, to);

                        fromToMathData(object, true, from, to);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (from != null) {
                    try {
                        pForm.addMessage("Deleting samples of object " + object.getName() + ":" + object.getID()
                                + " from " + from.toString("YYYY-MM-dd HH:mm:ss") + " to " + new DateTime().toString("YYYY-MM-dd HH:mm:ss"));
                        DateTime t = new DateTime();
                        valueAtt.deleteSamplesBetween(from, t);

                        fromToMathData(object, true, from, t);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        pForm.addMessage("Deleting samples of object " + object.getName() + ":" + object.getID()
                                + " from " + new DateTime(1990, 1, 1, 0, 0, 0).toString("YYYY-MM-dd HH:mm:ss") + " to " + to.toString("YYYY-MM-dd HH:mm:ss"));
                        DateTime f = new DateTime(1990, 1, 1, 0, 0, 0);
                        valueAtt.deleteSamplesBetween(f, to);

                        fromToMathData(object, true, f, to);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            pForm.addMessage("Deleted samples of object " + object.getName() + ":" + object.getID());
        }
    }

    public static void deleteAllSamples(ProgressForm pForm, JEVisObject object, boolean rawData, boolean cleanData) {
        try {
            JEVisAttribute value = object.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
            if (value != null) {
                if (((object.getJEVisClassName().equals("Clean Data") || object.getJEVisClassName().equals("Math Data")) && cleanData)
                        || (object.getJEVisClassName().equals("Data") && rawData)) {
                    pForm.addMessage("Deleting all samples of object " + object.getName() + ":" + object.getID());
                    value.deleteAllSample();

                    allSamplesMathData(object, cleanData);
                }
            }
            for (JEVisObject child : object.getChildren()) {
                deleteAllSamples(pForm, child, rawData, cleanData);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", object.getName(), object.getID());
        }
    }

    private static void allSamplesMathData(JEVisObject object, boolean cleanData) throws JEVisException {
        if (object.getJEVisClassName().equals("Math Data") && cleanData) {
            try {
                JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
                if (lastRunAttribute != null) {
                    List<JEVisSample> allSamples = lastRunAttribute.getAllSamples();
                    if (allSamples.size() > 1) {
                        allSamples.remove(0);
                        DateTime finalTS = allSamples.get(0).getTimestamp();
                        DateTime lastTS = allSamples.get(allSamples.size() - 1).getTimestamp();

                        lastRunAttribute.deleteSamplesBetween(finalTS, lastTS);
                    }
                }

            } catch (JEVisException e) {
                logger.error("Could not get math data last run time: ", e);
            }
        }
    }

    public static void deleteAllSamples(ProgressForm pForm, JEVisObject object, DateTime from, DateTime to, boolean rawData, boolean cleanData) {
        try {
            JEVisAttribute value = object.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
            if (value != null) {
                if (((object.getJEVisClassName().equals("Clean Data") || object.getJEVisClassName().equals("Math Data")) && cleanData)
                        || (object.getJEVisClassName().equals("Data") && rawData)) {
                    DateTime f = null;
                    if (from == null) {
                        f = new DateTime(1990, 1, 1, 0, 0, 0);
                    } else {
                        f = from;
                    }

                    DateTime t = null;
                    if (to == null) {
                        t = new DateTime();
                    } else {
                        t = to;
                    }
                    pForm.addMessage("Deleting samples of object " + object.getName() + ":" + object.getID()
                            + " from " + f.toString("YYYY-MM-dd HH:mm:ss") + " to " + t.toString("YYYY-MM-dd HH:mm:ss"));
                    value.deleteSamplesBetween(f, t);

                    fromToMathData(object, cleanData, f, t);
                }
            }
            for (JEVisObject child : object.getChildren()) {
                deleteAllSamples(pForm, child, from, to, rawData, cleanData);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", object.getName(), object.getID());
        }
    }

    private static void fromToMathData(JEVisObject object, boolean cleanData, DateTime f, DateTime t) throws JEVisException {
        if (object.getJEVisClassName().equals("Math Data") && cleanData) {
            try {
                JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
                JEVisAttribute periodOffsetAttribute = object.getAttribute("Period Offset");
                JEVisAttribute periodAttribute = object.getAttribute("Period");
                if (lastRunAttribute != null && periodOffsetAttribute != null && periodAttribute != null) {
                    List<JEVisSample> allSamples = lastRunAttribute.getAllSamples();
                    Long periodOffset = periodOffsetAttribute.getLatestSample().getValueAsLong();
                    Period period = new Period(periodAttribute.getLatestSample().getValueAsString());

                    if (periodOffset > 0) {
                        f = PeriodHelper.minusPeriodToDate(f, period);
                    } else if (periodOffset < 0) {
                        f = PeriodHelper.addPeriodToDate(f, period);
                    }

                    if (allSamples.size() > 0) {
                        allSamples.remove(0);
                        DateTime finalTS = null;
                        for (JEVisSample sample : allSamples) {
                            if (new DateTime(sample.getValueAsString()).isAfter(f)) {
                                finalTS = sample.getTimestamp();
                                break;
                            }
                        }

                        lastRunAttribute.deleteSamplesBetween(finalTS, t);
                    }
                }

            } catch (JEVisException e) {
                logger.error("Could not get math data last run time: ", e);
            }
        }
    }
}
