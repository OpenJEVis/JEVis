package org.jevis.jeconfig.plugin.graph.view;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jevis.api.*;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.measure.unit.Unit;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;

public class GraphExportCSV {
    private static final Logger logger = LogManager.getLogger(GraphExportCSV.class);
    final DateTimeFormatter standard = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private final GraphDataModel model;
    private final JEVisDataSource ds;
    private Boolean xlsx = false;
    private boolean needSave = false;
    private File destinationFile;
    private DateTime minDate = null;
    private DateTime maxDate = null;
    private Boolean multiAnalyses = false;
    private List<ChartSettings> charts = new ArrayList<>();
    final ObservableList<Locale> choices = FXCollections.observableArrayList(Locale.getAvailableLocales());
    private Locale selectedLocale;
    private NumberFormat numberFormat;
    private Boolean withUserNotes = false;

    public GraphExportCSV(JEVisDataSource ds, GraphDataModel model) {
        this.model = model;
        this.charts = model.getCharts();
        this.ds = ds;
        this.setDates();
        AlphanumComparator ac = new AlphanumComparator();
        choices.sort((o1, o2) -> ac.compare(o1.getDisplayLanguage(), o2.getDisplayLanguage()));

        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        JFXComboBox decimalSeparatorChoiceBox = new JFXComboBox(choices);

        Callback<ListView<Locale>, ListCell<Locale>> cellFactory = new Callback<ListView<Locale>, ListCell<Locale>>() {
            @Override
            public ListCell<Locale> call(ListView<Locale> param) {
                return new ListCell<Locale>() {
                    @Override
                    protected void updateItem(Locale item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item.getDisplayName());
                        }
                    }

                };
            }

        };
        decimalSeparatorChoiceBox.setCellFactory(cellFactory);
        decimalSeparatorChoiceBox.setButtonCell(cellFactory.call(null));

        decimalSeparatorChoiceBox.getSelectionModel().select(Locale.getDefault());

        decimalSeparatorChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                selectedLocale = LocaleUtils.toLocale(newValue.toString());
                updateNumberFormatter();
            }
        });

        ButtonType buttonOk = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.export.decimalseparator.button.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.export.decimalseparator.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        Dialog<ButtonType> selectDecimalSeparators = new Dialog<>();
        selectDecimalSeparators.setTitle(I18n.getInstance().getString("plugin.graph.dialog.export.decimalseparator.title"));
        selectDecimalSeparators.getDialogPane().setPrefWidth(400);

        VBox vBox = new VBox();

        Label selection = new Label(I18n.getInstance().getString("plugin.graph.dialog.export.decimalseparator.selection"));
        Label withUserNotesLabel = new Label(I18n.getInstance().getString("plugin.graph.dialog.export.withusernotes"));
        JFXCheckBox withUserNotes = new JFXCheckBox();
        Label emptyLine1 = new Label("");
        Label emptyLine2 = new Label("");

        withUserNotes.selectedProperty().addListener((observable, oldValue, newValue) -> this.withUserNotes = newValue);

        HBox hBox = new HBox();
        hBox.setSpacing(4);
        hBox.getChildren().setAll(withUserNotesLabel, withUserNotes);

        vBox.getChildren().setAll(selection, emptyLine1, decimalSeparatorChoiceBox, emptyLine2, hBox);

        selectDecimalSeparators.getDialogPane().setContent(vBox);

        selectDecimalSeparators.getDialogPane().getButtonTypes().addAll(buttonOk, cancel);

        selectDecimalSeparators.showAndWait().ifPresent(response -> {
            if (response.getButtonData().getTypeCode().equals(buttonOk.getButtonData().getTypeCode())) {

                if (charts.size() > 1) {
                    charts.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
                    multiAnalyses = true;
                }

                String formattedName = "";
                if (model.getCurrentAnalysis() != null)
                    formattedName = model.getCurrentAnalysis().getName().replaceAll(" ", "_");

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("CSV File Destination");
                DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");
                FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
                FileChooser.ExtensionFilter xlsxFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx");
                fileChooser.getExtensionFilters().addAll(csvFilter, xlsxFilter);
                fileChooser.setSelectedExtensionFilter(csvFilter);

                fileChooser.setInitialFileName(formattedName + I18n.getInstance().getString("plugin.graph.dialog.export.from")
                        + fmtDate.print(minDate) + I18n.getInstance().getString("plugin.graph.dialog.export.to")
                        + fmtDate.print(maxDate) + "_" + fmtDate.print(new DateTime()) + ".csv");
                File file = fileChooser.showSaveDialog(JEConfig.getStage());
                if (file != null) {
                    destinationFile = file;
                    if (fileChooser.getSelectedExtensionFilter().equals(xlsxFilter)) {
                        xlsx = true;
                    }
                    needSave = true;
                }
            }
        });


    }

    private void updateNumberFormatter() {
        if (selectedLocale != null)
            numberFormat = NumberFormat.getNumberInstance(selectedLocale);
        else {
            numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        }

        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
    }

    private void setDates() {
        for (ChartDataModel mdl : model.getSelectedData()) {
            DateTime startNow = mdl.getSelectedStart();
            DateTime endNow = mdl.getSelectedEnd();
            if (minDate == null || startNow.isBefore(minDate)) minDate = startNow;
            if (maxDate == null || endNow.isAfter(maxDate)) maxDate = endNow;
        }
    }

    public void export() throws FileNotFoundException, UnsupportedEncodingException, JEVisException {
        String exportStrg = "";
        if (!multiAnalyses) {
            if (!xlsx) {
                exportStrg = createCSVString();
            } else {
                createXLSXFile();
            }
        } else {
            if (!xlsx) {
                exportStrg = createCSVStringMulti();
            } else {
                createXLSXFileMulti();
            }
        }

        if (!xlsx && destinationFile != null && exportStrg.length() > 90) {
            writeFile(destinationFile, exportStrg);
        }
    }

    private void createXLSXFileMulti() throws JEVisException {
        XSSFWorkbook workbook = new XSSFWorkbook(); //create workbook

        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
        CellStyle cellStyleDateTime = workbook.createCellStyle();
        cellStyleDateTime.setDataFormat((short) 165);

        CellStyle cellStyleValues = workbook.createCellStyle();
        cellStyleValues.setDataFormat((short) 4);

        Sheet sheet = workbook.createSheet("Data");

        int columnIndex = 0;
        for (ChartSettings cset : charts) {
            Cell dateHeaderCell = getOrCreateCell(sheet, 0, columnIndex);
            dateHeaderCell.setCellValue("Date");
            columnIndex++;

            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    String objectName = mdl.getObject().getName();

                    Cell nameHeader = getOrCreateCell(sheet, 0, columnIndex);
                    StringBuilder header = new StringBuilder(objectName);
                    if (mdl.getDataProcessor() != null) {
                        String dpName = mdl.getDataProcessor().getName();
                        header.append(" (").append(dpName).append(")");
                    }
                    header.append(" in ");
                    String currentUnit = UnitManager.getInstance().format(mdl.getUnit());
                    if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString())) {
                        currentUnit = mdl.getUnit().getLabel();
                    }
                    header.append(currentUnit);
                    if (withUserNotes) {
                        Cell noteHeader = getOrCreateCell(sheet, 0, columnIndex);
                        noteHeader.setCellValue("Note");
                        columnIndex++;
                    }
                    nameHeader.setCellValue(header.toString());
                    columnIndex++;
                }
            }
        }

        long size = 0L;

        List<List<String>> listDateColumns = new ArrayList<>();
        List<Map<DateTime, Long>> listDateTimes = new ArrayList<>();
        for (ChartSettings cset : charts) {
            List<String> dateColumn = new ArrayList<>();
            Map<DateTime, Long> dateTimes = new HashMap<>();
            long dateCounter = 0;
            DateTime currentStart = null;
            DateTime currentEnd = null;
            boolean firstSet = true;
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (currentStart == null) currentStart = mdl.getSelectedStart();
                else mdl.setSelectedStart(currentStart);
                if (currentEnd == null) currentEnd = mdl.getSelectedEnd();
                else mdl.setSelectedEnd(currentEnd);

                if (firstSet && mdl.getSelectedcharts().contains(cset.getId())) {
                    for (JEVisSample sample : mdl.getSamples()) {
                        dateColumn.add(standard.print(sample.getTimestamp()));
                        dateTimes.put(sample.getTimestamp(), dateCounter);
                        dateCounter++;
                    }
                    firstSet = false;
                    size = Math.max(size, dateColumn.size());
                }
            }
            listDateColumns.add(dateColumn);
            listDateTimes.add(dateTimes);
        }

        List<Map<String, List<JEVisSample>>> listMaps = new ArrayList<>();
        List<Map<String, Map<DateTime, JEVisSample>>> listNotes = new ArrayList<>();
        for (ChartSettings cset : charts) {
            Map<String, Map<DateTime, JEVisSample>> mapNotes = new HashMap<>();
            Map<String, List<JEVisSample>> map = new HashMap<>();
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    map.put(mdl.getObject().getName(), mdl.getSamples());

                    Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
                    for (JEVisObject jeVisObject : mdl.getObject().getChildren()) {
                        try {
                            if (jeVisObject.getJEVisClassName().equals("Data Notes")) {
                                JEVisAttribute notes = jeVisObject.getAttribute("User Notes");
                                if (notes != null && notes.hasSample()) {
                                    for (JEVisSample jeVisSample : notes.getSamples(mdl.getSelectedStart(), mdl.getSelectedEnd())) {
                                        sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
                                    }

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mapNotes.put(mdl.getObject().getName(), sampleMap);
                }
            }
            listMaps.add(map);
            listNotes.add(mapNotes);
        }

        columnIndex = 0;
        for (ChartSettings cset : charts) {
            int chartsIndex = cset.getId();
            int currentSize = listDateColumns.get(chartsIndex).size();
            for (int i = 0; i < currentSize; i++) {
                Cell dateCell = getOrCreateCell(sheet, i + 1, columnIndex);
                dateCell.setCellValue(listDateColumns.get(chartsIndex).get(i));
                dateCell.setCellStyle(cellStyleDateTime);
            }
            columnIndex++;

            for (ChartDataModel mdl : model.getSelectedData()) {
                String objName = mdl.getObject().getName();
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    List<JEVisSample> jeVisSamples = listMaps.get(chartsIndex).get(objName);
                    for (JEVisSample jeVisSample : jeVisSamples) {
                        Cell valueCell = getOrCreateCell(sheet, jeVisSamples.indexOf(jeVisSample) + 1, columnIndex);
                        valueCell.setCellValue(jeVisSample.getValueAsDouble());
                        valueCell.setCellStyle(cellStyleValues);
                    }
                    columnIndex++;
                    if (withUserNotes) {
                        Map<DateTime, JEVisSample> dateTimeJEVisSampleMap = listNotes.get(chartsIndex).get(objName);
                        Map<DateTime, Long> dateTimeLongMap = listDateTimes.get(chartsIndex);
                        for (Map.Entry<DateTime, JEVisSample> entry : dateTimeJEVisSampleMap.entrySet()) {
                            DateTime dateTime = entry.getKey();
                            JEVisSample jeVisSample = entry.getValue();
                            if (jeVisSample != null) {
                                Cell noteCell = getOrCreateCell(sheet, dateTimeLongMap.get(dateTime).intValue() + 1, columnIndex);
                                noteCell.setCellValue(jeVisSample.getValueAsString());
                            }
                        }
                        columnIndex++;
                    }
                }
            }
        }

        try {
            workbook.write(new FileOutputStream(destinationFile));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private org.apache.poi.ss.usermodel.Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx) {
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

    private void createXLSXFile() throws JEVisException {
        XSSFWorkbook workbook = new XSSFWorkbook(); //create workbook

        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
        CellStyle cellStyleDateTime = workbook.createCellStyle();
        cellStyleDateTime.setDataFormat((short) 165);

        CellStyle cellStyleValues = workbook.createCellStyle();
        cellStyleValues.setDataFormat((short) 4);

        Sheet sheet = workbook.createSheet("Data");
        Cell dateHeaderCell = getOrCreateCell(sheet, 0, 0);
        dateHeaderCell.setCellValue("Date");


        int columnIndex = 1;
        for (ChartDataModel mdl : model.getSelectedData()) {

            String objectName = mdl.getObject().getName();
            Cell nameHeader = getOrCreateCell(sheet, 0, columnIndex);
            StringBuilder header = new StringBuilder(objectName);
            if (mdl.getDataProcessor() != null) {
                String dpName = mdl.getDataProcessor().getName();
                header.append(" (").append(dpName).append(")");
            }
            header.append(" in ");
            String currentUnit = UnitManager.getInstance().format(mdl.getUnit());
            if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString())) {
                currentUnit = mdl.getUnit().getLabel();
            }
            header.append(currentUnit);
            if (withUserNotes) {
                Cell noteHeader = getOrCreateCell(sheet, 0, columnIndex);
                noteHeader.setCellValue("Note");
                columnIndex++;
            }
            nameHeader.setCellValue(header.toString());
        }

        List<String> dateColumn = new ArrayList<>();
        Map<DateTime, Long> dateTimes = new HashMap<>();
        boolean firstSet = true;
        long dateCounter = 0;
        for (ChartDataModel mdl : model.getSelectedData()) {
            if (firstSet) {
                for (JEVisSample sample : mdl.getSamples()) {
                    dateColumn.add(standard.print(sample.getTimestamp()));
                    dateTimes.put(sample.getTimestamp(), dateCounter);
                    dateCounter++;
                }
                firstSet = false;
            }
        }

        Map<String, Map<DateTime, JEVisSample>> mapNotes = new HashMap<>();
        Map<String, List<JEVisSample>> map = new HashMap<>();
        for (ChartDataModel mdl : model.getSelectedData()) {
            map.put(mdl.getObject().getName(), mdl.getSamples());

            Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
            for (JEVisObject jeVisObject : mdl.getObject().getChildren()) {
                try {
                    if (jeVisObject.getJEVisClassName().equals("Data Notes")) {
                        JEVisAttribute notes = jeVisObject.getAttribute("User Notes");
                        if (notes != null && notes.hasSample()) {
                            for (JEVisSample jeVisSample : notes.getSamples(mdl.getSelectedStart(), mdl.getSelectedEnd())) {
                                sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mapNotes.put(mdl.getObject().getName(), sampleMap);
        }

        columnIndex = 1;
        for (int i = 0; i < dateColumn.size(); i++) {
            Cell dateCell = getOrCreateCell(sheet, i + 1, 0);
            dateCell.setCellValue(dateColumn.get(i));
            dateCell.setCellStyle(cellStyleDateTime);
        }

        for (ChartDataModel mdl : model.getSelectedData()) {
            String name = mdl.getObject().getName();
            List<JEVisSample> jeVisSamples = map.get(name);
            for (JEVisSample sample : jeVisSamples) {
                DateTime timeStamp = sample.getTimestamp();
                Cell valueCell = getOrCreateCell(sheet, dateTimes.get(timeStamp).intValue() + 1, columnIndex);
                valueCell.setCellValue(sample.getValueAsDouble());
                valueCell.setCellStyle(cellStyleValues);
            }
            columnIndex++;

            if (withUserNotes) {
                Map<DateTime, JEVisSample> notes = mapNotes.get(name);
                for (Map.Entry<DateTime, JEVisSample> entry : notes.entrySet()) {
                    DateTime timeStamp = entry.getKey();
                    JEVisSample sample = notes.get(timeStamp);
                    if (sample != null) {
                        Cell noteCell = getOrCreateCell(sheet, dateTimes.get(timeStamp).intValue() + 1, columnIndex);
                        noteCell.setCellValue(sample.getValueAsString());
                    }
                }
                columnIndex++;
            }
        }

        try {
            workbook.write(new FileOutputStream(destinationFile));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFile(File file, String text) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer;
        writer = new PrintWriter(file, "UTF-8");
        writer.println(text);
        writer.close();
    }

    private String createCSVString() throws JEVisException {
        final StringBuilder sb = new StringBuilder();

        StringBuilder header = new StringBuilder("Date");
        for (ChartDataModel mdl : model.getSelectedData()) {
            String objectName = mdl.getObject().getName();

            header.append(";").append(objectName);
            if (mdl.getDataProcessor() != null) {
                String dpName = mdl.getDataProcessor().getName();
                header.append(" (").append(dpName).append(")");
            }
            header.append(" in ");
            String currentUnit = UnitManager.getInstance().format(mdl.getUnit());
            if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString())) {
                currentUnit = mdl.getUnit().getLabel();
            }
            header.append(currentUnit);
            if (withUserNotes) {
                header.append(";Note");
            }
        }
        sb.append(header);
        sb.append(System.getProperty("line.separator"));

        List<String> dateColumn = new ArrayList<>();
        boolean firstSet = true;
        for (ChartDataModel mdl : model.getSelectedData()) {
            if (firstSet) {
                for (JEVisSample sample : mdl.getSamples()) {
                    dateColumn.add(standard.print(sample.getTimestamp()));
                }
                firstSet = false;
            }
        }

        Map<String, Map<DateTime, JEVisSample>> mapNotes = new HashMap<>();
        Map<String, List<JEVisSample>> map = new HashMap<>();
        for (ChartDataModel mdl : model.getSelectedData()) {
            map.put(mdl.getObject().getName(), mdl.getSamples());

            Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
            for (JEVisObject jeVisObject : mdl.getObject().getChildren()) {
                try {
                    if (jeVisObject.getJEVisClassName().equals("Data Notes")) {
                        JEVisAttribute notes = jeVisObject.getAttribute("User Notes");
                        if (notes != null && notes.hasSample()) {
                            for (JEVisSample jeVisSample : notes.getAllSamples()) {
                                sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mapNotes.put(mdl.getObject().getName(), sampleMap);
        }

        for (int i = 0; i < dateColumn.size(); i++) {
            StringBuilder s = new StringBuilder();
            s.append(dateColumn.get(i)).append(";");

            for (ChartDataModel mdl : model.getSelectedData()) {
                String name = mdl.getObject().getName();
                List<JEVisSample> jeVisSamples = map.get(name);
                DateTime timeStamp = null;
                if (i < jeVisSamples.size()) {
                    JEVisSample sample = jeVisSamples.get(i);
                    timeStamp = sample.getTimestamp();
                    String formattedValue = numberFormat.format(sample.getValueAsDouble());
                    s.append(formattedValue);
                }
                s.append(";");
                if (withUserNotes && timeStamp != null) {
                    Map<DateTime, JEVisSample> notes = mapNotes.get(name);
                    JEVisSample sample = notes.get(timeStamp);
                    if (sample != null) {
                        s.append(sample.getValueAsString());
                    }
                    s.append(";");
                } else if (withUserNotes) {
                    s.append(";");
                }
            }
            sb.append(s);
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }

    private String createCSVStringMulti() throws JEVisException {
        final StringBuilder sb = new StringBuilder();

        for (ChartSettings cset : charts) {
            StringBuilder header = new StringBuilder("Date");
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    String objectName = mdl.getObject().getName();

                    header.append(";").append(objectName);
                    if (mdl.getDataProcessor() != null) {
                        String dpName = mdl.getDataProcessor().getName();
                        header.append(" (").append(dpName).append(")");
                    }

                    header.append(" in ");
                    String currentUnit = UnitManager.getInstance().format(mdl.getUnit());
                    if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString())) {
                        currentUnit = mdl.getUnit().getLabel();
                    }
                    header.append(currentUnit);
                    if (withUserNotes) {
                        header.append(";Note");
                    }
                }
            }
            header.append(";");
            sb.append(header);
        }

        sb.append(System.getProperty("line.separator"));


        long size = 0L;

        List<List<String>> listDateColumns = new ArrayList<>();
        for (ChartSettings cset : charts) {
            List<String> dateColumn = new ArrayList<>();
            DateTime currentStart = null;
            DateTime currentEnd = null;
            boolean firstSet = true;
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (currentStart == null) currentStart = mdl.getSelectedStart();
                else mdl.setSelectedStart(currentStart);
                if (currentEnd == null) currentEnd = mdl.getSelectedEnd();
                else mdl.setSelectedEnd(currentEnd);

                if (firstSet && mdl.getSelectedcharts().contains(cset.getId())) {
                    for (JEVisSample sample : mdl.getSamples()) {
                        dateColumn.add(standard.print(sample.getTimestamp()));
                    }
                    firstSet = false;
                    size = Math.max(size, dateColumn.size());
                }
            }
            listDateColumns.add(dateColumn);
        }

        List<Map<String, List<JEVisSample>>> listMaps = new ArrayList<>();
        List<Map<String, Map<DateTime, JEVisSample>>> listNotes = new ArrayList<>();
        for (ChartSettings cset : charts) {
            Map<String, Map<DateTime, JEVisSample>> mapNotes = new HashMap<>();
            Map<String, List<JEVisSample>> map = new HashMap<>();
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    map.put(mdl.getObject().getName(), mdl.getSamples());

                    Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
                    for (JEVisObject jeVisObject : mdl.getObject().getChildren()) {
                        try {
                            if (jeVisObject.getJEVisClassName().equals("Data Notes")) {
                                JEVisAttribute notes = jeVisObject.getAttribute("User Notes");
                                if (notes != null && notes.hasSample()) {
                                    for (JEVisSample jeVisSample : notes.getAllSamples()) {
                                        sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
                                    }

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mapNotes.put(mdl.getObject().getName(), sampleMap);
                }
            }
            listMaps.add(map);
            listNotes.add(mapNotes);
        }

        for (int i = 0; i < size; i++) {
            StringBuilder str = new StringBuilder();
            for (ChartSettings cset : charts) {
                int chartsIndex = cset.getId();
                boolean hasValues = i < listDateColumns.get(chartsIndex).size();

                if (hasValues) {
                    str.append(listDateColumns.get(chartsIndex).get(i)).append(";");
                } else {
                    str.append(";");
                }

                for (ChartDataModel mdl : model.getSelectedData()) {
                    String objName = mdl.getObject().getName();
                    if (mdl.getSelectedcharts().contains(cset.getId())) {
                        if (hasValues) {
                            JEVisSample sample1 = listMaps.get(chartsIndex).get(objName).get(i);
                            String formattedValue = numberFormat.format(sample1.getValueAsDouble());
                            str.append(formattedValue).append(";");
                            if (withUserNotes) {
                                JEVisSample sample = listNotes.get(chartsIndex).get(objName).get(sample1.getTimestamp());
                                if (sample != null) {
                                    str.append(sample.getValueAsString());
                                }
                                str.append(";");
                            }
                        } else {
                            str.append(";");
                            if (withUserNotes) {
                                str.append(";");
                            }
                        }
                    }
                }
            }
            sb.append(str);
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }
}
