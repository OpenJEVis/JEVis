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
    private final String NAME;
    private final String IN;
    private final String NOTE;
    private final String MIN;
    private final String MAX;
    private final String AVG;
    private final String SUM;
    private final String DATE;
    private final String UNIT;
    private final String ID;
    private final String DATA_NOTES = "Data Notes";
    private final String USER_NOTES = "User Notes";
    private final String LINE_SEPARATOR = "line.separator";
    private final String COL_SEP = ";";
    private final String SPACE = " ";
    private final DateTimeFormatter standard = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private final GraphDataModel model;
    private final JEVisDataSource ds;
    private Boolean xlsx = false;
    private boolean needSave = false;
    private File destinationFile;
    private final DateTime minDate;
    private final DateTime maxDate;
    private Boolean multiAnalyses = false;
    private final ObservableList<Locale> choices = FXCollections.observableArrayList(Locale.getAvailableLocales());
    private List<ChartSettings> charts;
    private Locale selectedLocale;
    private NumberFormat numberFormat;
    private Boolean withUserNotes = false;

    public GraphExportCSV(JEVisDataSource ds, GraphDataModel model, DateTime xAxisLowerBound, DateTime xAxisUpperBound) {
        this.NAME = I18n.getInstance().getString("plugin.graph.export.text.name");
        this.IN = I18n.getInstance().getString("plugin.graph.export.text.in");
        this.NOTE = I18n.getInstance().getString("plugin.graph.export.text.note");
        this.MIN = I18n.getInstance().getString("plugin.graph.export.text.min");
        this.MAX = I18n.getInstance().getString("plugin.graph.export.text.max");
        this.AVG = I18n.getInstance().getString("plugin.graph.export.text.avg");
        this.SUM = I18n.getInstance().getString("plugin.graph.export.text.sum");
        this.DATE = I18n.getInstance().getString("plugin.graph.export.text.date");
        this.UNIT = I18n.getInstance().getString("plugin.graph.export.text.unit");
        this.ID = I18n.getInstance().getString("plugin.graph.export.text.id");

        this.model = model;
        this.charts = model.getCharts();
        this.ds = ds;
//        this.setDates();
        this.minDate = xAxisLowerBound;
        this.maxDate = xAxisUpperBound;
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
                    formattedName = model.getCurrentAnalysis().getName().replaceAll(SPACE, "_");

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("CSV File Destination");
                DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");
                FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", ".csv");
                FileChooser.ExtensionFilter xlsxFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", ".xlsx");
                fileChooser.getExtensionFilters().addAll(csvFilter, xlsxFilter);
                fileChooser.setSelectedExtensionFilter(csvFilter);

                fileChooser.setInitialFileName(formattedName + I18n.getInstance().getString("plugin.graph.dialog.export.from")
                        + fmtDate.print(minDate) + I18n.getInstance().getString("plugin.graph.dialog.export.to")
                        + fmtDate.print(maxDate) + "_" + fmtDate.print(new DateTime()));
                File file = fileChooser.showSaveDialog(JEConfig.getStage());
                if (file != null) {
                    destinationFile = new File(file + fileChooser.getSelectedExtensionFilter().getExtensions().get(0));
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
//            if (minDate == null || startNow.isBefore(minDate)) minDate = startNow;
//            if (maxDate == null || endNow.isAfter(maxDate)) maxDate = endNow;
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
            Cell nameHeaderCell = getOrCreateCell(sheet, 0, columnIndex);
            nameHeaderCell.setCellValue(NAME);
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
                    nameHeader.setCellValue(header.toString());
                    columnIndex++;

                    if (withUserNotes) {
                        columnIndex++;
                    }
                }
            }
        }

        columnIndex = 0;
        for (ChartSettings cset : charts) {
            Cell idHeaderCell = getOrCreateCell(sheet, 1, columnIndex);
            idHeaderCell.setCellValue(ID);
            columnIndex++;
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    Cell idHeader = getOrCreateCell(sheet, 1, columnIndex);
                    if (mdl.getDataProcessor() != null) {
                        idHeader.setCellValue(mdl.getDataProcessor().getID());
                    } else {
                        idHeader.setCellValue(mdl.getObject().getID());
                    }
                    columnIndex++;

                    if (withUserNotes) {
                        columnIndex++;
                    }
                }
            }
        }

        columnIndex = 0;
        for (ChartSettings cset : charts) {
            Cell unitCell = getOrCreateCell(sheet, 3, columnIndex);
            unitCell.setCellValue(UNIT);
            columnIndex++;
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    Cell unitHeader = getOrCreateCell(sheet, 3, columnIndex);
                    String currentUnit = UnitManager.getInstance().format(mdl.getUnit());
                    if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString())) {
                        currentUnit = mdl.getUnit().getLabel();
                    }
                    unitHeader.setCellValue(currentUnit);
                    columnIndex++;

                    if (withUserNotes) {
                        columnIndex++;
                    }
                }
            }
        }

        columnIndex = 0;
        for (ChartSettings cset : charts) {
            for (int i = 0; i < 4; i++) {
                Cell descriptorCell = getOrCreateCell(sheet, i + 4, columnIndex);
                switch (i) {
                    case 0:
                        descriptorCell.setCellValue(MIN);
                        break;
                    case 1:
                        descriptorCell.setCellValue(MAX);
                        break;
                    case 2:
                        descriptorCell.setCellValue(AVG);
                        break;
                    case 3:
                        descriptorCell.setCellValue(SUM);
                        break;
                }
            }
            columnIndex++;

            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    for (int i = 0; i < 4; i++) {
                        Cell valueCell = getOrCreateCell(sheet, i + 4, columnIndex);
                        switch (i) {
                            case 0:
                                valueCell.setCellValue(numberFormat.format(mdl.getMin()));
                                break;
                            case 1:
                                valueCell.setCellValue(numberFormat.format(mdl.getMax()));
                                break;
                            case 2:
                                valueCell.setCellValue(numberFormat.format(mdl.getAvg()));
                                break;
                            case 3:
                                valueCell.setCellValue(numberFormat.format(mdl.getSum()));
                                break;
                        }
                    }
                    columnIndex++;

                    if (withUserNotes) {
                        columnIndex++;
                    }
                }
            }
        }

        columnIndex = 0;
        for (ChartSettings cset : charts) {
            Cell dateHeaderCell = getOrCreateCell(sheet, 9, columnIndex);
            dateHeaderCell.setCellValue(DATE);
            columnIndex++;
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    columnIndex++;

                    if (withUserNotes) {
                        Cell noteHeader = getOrCreateCell(sheet, 9, columnIndex);
                        noteHeader.setCellValue(NOTE);
                        columnIndex++;
                    }
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
                        if (sample.getTimestamp().equals(minDate)
                                || (sample.getTimestamp().isAfter(minDate) && sample.getTimestamp().isBefore(maxDate))
                                || sample.getTimestamp().equals(maxDate)) {
                            dateColumn.add(standard.print(sample.getTimestamp()));
                            dateTimes.put(sample.getTimestamp(), dateCounter);
                            dateCounter++;
                        }
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
                    List<JEVisSample> filteredSamples = new ArrayList<>();
                    for (JEVisSample jeVisSample : mdl.getSamples()) {
                        if (jeVisSample.getTimestamp().equals(minDate)
                                || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                                || jeVisSample.getTimestamp().equals(maxDate)) {
                            filteredSamples.add(jeVisSample);
                        }
                    }
                    map.put(mdl.getObject().getName(), filteredSamples);

                    Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
                    for (JEVisObject jeVisObject : mdl.getObject().getChildren()) {
                        try {
                            if (jeVisObject.getJEVisClassName().equals(DATA_NOTES)) {
                                JEVisAttribute notes = jeVisObject.getAttribute(USER_NOTES);
                                if (notes != null && notes.hasSample()) {
                                    for (JEVisSample jeVisSample : notes.getSamples(mdl.getSelectedStart(), mdl.getSelectedEnd())) {
                                        if (jeVisSample.getTimestamp().equals(minDate)
                                                || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                                                || jeVisSample.getTimestamp().equals(maxDate)) {
                                            sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
                                        }
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
                Cell dateCell = getOrCreateCell(sheet, i + 10, columnIndex);
                dateCell.setCellValue(listDateColumns.get(chartsIndex).get(i));
                dateCell.setCellStyle(cellStyleDateTime);
            }
            columnIndex++;

            for (ChartDataModel mdl : model.getSelectedData()) {
                String objName = mdl.getObject().getName();
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    List<JEVisSample> jeVisSamples = listMaps.get(chartsIndex).get(objName);
                    for (JEVisSample jeVisSample : jeVisSamples) {
                        Cell valueCell = getOrCreateCell(sheet, jeVisSamples.indexOf(jeVisSample) + 10, columnIndex);
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
                                Cell noteCell = getOrCreateCell(sheet, dateTimeLongMap.get(dateTime).intValue() + 10, columnIndex);
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
        Cell nameHeaderCell = getOrCreateCell(sheet, 0, 0);
        nameHeaderCell.setCellValue(NAME);

        int columnIndex = 1;
        for (ChartDataModel mdl : model.getSelectedData()) {

            String objectName = mdl.getObject().getName();
            Cell nameHeader = getOrCreateCell(sheet, 0, columnIndex);
            StringBuilder header = new StringBuilder(objectName);
            if (mdl.getDataProcessor() != null) {
                String dpName = mdl.getDataProcessor().getName();
                header.append(" (").append(dpName).append(")");
            }
            nameHeader.setCellValue(header.toString());
            columnIndex++;

            if (withUserNotes) {
                columnIndex++;
            }
        }

        columnIndex = 0;
        Cell idHeaderCell = getOrCreateCell(sheet, 1, columnIndex);
        idHeaderCell.setCellValue(ID);
        columnIndex++;
        for (ChartDataModel mdl : model.getSelectedData()) {
            Cell idHeader = getOrCreateCell(sheet, 1, columnIndex);
            if (mdl.getDataProcessor() != null) {
                idHeader.setCellValue(mdl.getDataProcessor().getID());
            } else {
                idHeader.setCellValue(mdl.getObject().getID());
            }
            columnIndex++;

            if (withUserNotes) {
                columnIndex++;
            }
        }

        columnIndex = 0;
        Cell unitCell = getOrCreateCell(sheet, 3, columnIndex);
        unitCell.setCellValue(UNIT);
        columnIndex++;
        for (ChartDataModel mdl : model.getSelectedData()) {
            Cell unitHeader = getOrCreateCell(sheet, 3, columnIndex);
            String currentUnit = UnitManager.getInstance().format(mdl.getUnit());
            if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString())) {
                currentUnit = mdl.getUnit().getLabel();
            }
            unitHeader.setCellValue(currentUnit);
            columnIndex++;

            if (withUserNotes) {
                columnIndex++;
            }
        }

        columnIndex = 1;
        for (int i = 0; i < 4; i++) {
            Cell descriptorCell = getOrCreateCell(sheet, i + 4, 0);
            switch (i) {
                case 0:
                    descriptorCell.setCellValue(MIN);
                    break;
                case 1:
                    descriptorCell.setCellValue(MAX);
                    break;
                case 2:
                    descriptorCell.setCellValue(AVG);
                    break;
                case 3:
                    descriptorCell.setCellValue(SUM);
                    break;
            }
        }

        for (ChartDataModel mdl : model.getSelectedData()) {
            for (int i = 0; i < 4; i++) {
                Cell valueCell = getOrCreateCell(sheet, i + 4, columnIndex);
                switch (i) {
                    case 0:
                        valueCell.setCellValue(numberFormat.format(mdl.getMin()));
                        break;
                    case 1:
                        valueCell.setCellValue(numberFormat.format(mdl.getMax()));
                        break;
                    case 2:
                        valueCell.setCellValue(numberFormat.format(mdl.getAvg()));
                        break;
                    case 3:
                        valueCell.setCellValue(numberFormat.format(mdl.getSum()));
                        break;
                }
            }
            columnIndex++;

            if (withUserNotes) {
                columnIndex++;
            }
        }

        Cell dateHeaderCell = getOrCreateCell(sheet, 9, 0);
        dateHeaderCell.setCellValue(DATE);
        columnIndex = 1;
        for (ChartDataModel mdl : model.getSelectedData()) {
            columnIndex++;
            if (withUserNotes) {
                Cell noteHeader = getOrCreateCell(sheet, 9, columnIndex);
                noteHeader.setCellValue(NOTE);
                columnIndex++;
            }
        }

        List<String> dateColumn = new ArrayList<>();
        Map<DateTime, Long> dateTimes = new HashMap<>();
        boolean firstSet = true;
        long dateCounter = 0;
        for (ChartDataModel mdl : model.getSelectedData()) {
            if (firstSet) {
                for (JEVisSample sample : mdl.getSamples()) {
                    if (sample.getTimestamp().equals(minDate)
                            || (sample.getTimestamp().isAfter(minDate) && sample.getTimestamp().isBefore(maxDate))
                            || sample.getTimestamp().equals(maxDate)) {
                        dateColumn.add(standard.print(sample.getTimestamp()));
                        dateTimes.put(sample.getTimestamp(), dateCounter);
                        dateCounter++;
                    }
                }
                firstSet = false;
            }
        }

        Map<String, Map<DateTime, JEVisSample>> mapNotes = new HashMap<>();
        Map<String, List<JEVisSample>> map = new HashMap<>();
        for (ChartDataModel mdl : model.getSelectedData()) {
            List<JEVisSample> filteredSamples = new ArrayList<>();
            for (JEVisSample jeVisSample : mdl.getSamples()) {
                if (jeVisSample.getTimestamp().equals(minDate)
                        || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                        || jeVisSample.getTimestamp().equals(maxDate)) {
                    filteredSamples.add(jeVisSample);
                }
            }
            map.put(mdl.getObject().getName(), filteredSamples);

            Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
            for (JEVisObject jeVisObject : mdl.getObject().getChildren()) {
                try {
                    if (jeVisObject.getJEVisClassName().equals(DATA_NOTES)) {
                        JEVisAttribute notes = jeVisObject.getAttribute(USER_NOTES);
                        if (notes != null && notes.hasSample()) {
                            for (JEVisSample jeVisSample : notes.getSamples(mdl.getSelectedStart(), mdl.getSelectedEnd())) {
                                if (jeVisSample.getTimestamp().equals(minDate)
                                        || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                                        || jeVisSample.getTimestamp().equals(maxDate)) {
                                    sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
                                }
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
            Cell dateCell = getOrCreateCell(sheet, i + 10, 0);
            dateCell.setCellValue(dateColumn.get(i));
            dateCell.setCellStyle(cellStyleDateTime);
        }

        for (ChartDataModel mdl : model.getSelectedData()) {
            String name = mdl.getObject().getName();
            List<JEVisSample> jeVisSamples = map.get(name);
            for (JEVisSample sample : jeVisSamples) {
                DateTime timeStamp = sample.getTimestamp();
                Cell valueCell = getOrCreateCell(sheet, dateTimes.get(timeStamp).intValue() + 10, columnIndex);
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
                        Cell noteCell = getOrCreateCell(sheet, dateTimes.get(timeStamp).intValue() + 10, columnIndex);
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

        /**
         * Building the header
         */
        StringBuilder header = new StringBuilder(NAME);
        for (ChartDataModel mdl : model.getSelectedData()) {
            String objectName = mdl.getObject().getName();

            header.append(COL_SEP).append(objectName);
            if (mdl.getDataProcessor() != null) {
                String dpName = mdl.getDataProcessor().getName();
                header.append(" (").append(dpName).append(")");
            }

            if (withUserNotes) {
                header.append(COL_SEP);
            }
        }

        header.append(COL_SEP);
        header.append(System.getProperty(LINE_SEPARATOR));

        header.append(ID);
        header.append(COL_SEP);

        for (ChartDataModel mdl : model.getSelectedData()) {
            if (mdl.getDataProcessor() != null) {
                header.append(mdl.getDataProcessor().getID());
            } else {
                header.append(mdl.getObject().getID());
            }
            header.append(COL_SEP);

            if (withUserNotes) {
                header.append(COL_SEP);
            }
        }

        header.append(System.getProperty(LINE_SEPARATOR));
        header.append(System.getProperty(LINE_SEPARATOR));


        header.append(UNIT);
        header.append(COL_SEP);

        for (ChartDataModel mdl : model.getSelectedData()) {
            String currentUnit = UnitManager.getInstance().format(mdl.getUnit());
            if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString())) {
                currentUnit = mdl.getUnit().getLabel();
            }
            header.append(currentUnit);
            header.append(COL_SEP);

            if (withUserNotes) {
                header.append(COL_SEP);
            }
        }

        for (int i = 0; i < 4; i++) {
            header.append(System.getProperty(LINE_SEPARATOR));
            switch (i) {
                case 0:
                    header.append(MIN);
                    header.append(COL_SEP);
                    break;
                case 1:
                    header.append(MAX);
                    header.append(COL_SEP);
                    break;
                case 2:
                    header.append(AVG);
                    header.append(COL_SEP);
                    break;
                case 3:
                    header.append(SUM);
                    header.append(COL_SEP);
                    break;
            }
            for (ChartDataModel mdl : model.getSelectedData()) {
                switch (i) {
                    case 0:
                        header.append(numberFormat.format(mdl.getMin()));
                        header.append(COL_SEP);
                        break;
                    case 1:
                        header.append(numberFormat.format(mdl.getMax()));
                        header.append(COL_SEP);
                        break;
                    case 2:
                        header.append(numberFormat.format(mdl.getAvg()));
                        header.append(COL_SEP);
                        break;
                    case 3:
                        header.append(numberFormat.format(mdl.getSum()));
                        header.append(COL_SEP);
                        break;
                }

                if (withUserNotes) {
                    header.append(COL_SEP);
                }
            }
        }

        sb.append(header);
        sb.append(System.getProperty(LINE_SEPARATOR));
        sb.append(System.getProperty(LINE_SEPARATOR));
        sb.append(DATE);
        sb.append(COL_SEP);
        for (ChartDataModel mdl : model.getSelectedData()) {
            sb.append(COL_SEP);
            if (withUserNotes) {
                sb.append(NOTE);
                sb.append(COL_SEP);
            }
        }
        sb.append(System.getProperty(LINE_SEPARATOR));

        /**
         * Building a list for the date Column
         */

        List<String> dateColumn = new ArrayList<>();
        boolean firstSet = true;
        for (ChartDataModel mdl : model.getSelectedData()) {
            if (firstSet) {
                for (JEVisSample sample : mdl.getSamples()) {
                    if (sample.getTimestamp().equals(minDate)
                            || (sample.getTimestamp().isAfter(minDate) && sample.getTimestamp().isBefore(maxDate))
                            || sample.getTimestamp().equals(maxDate)) {
                        dateColumn.add(standard.print(sample.getTimestamp()));
                    }
                }
                firstSet = false;
            }
        }

        /**
         * Building maps for the samples
         */
        Map<String, Map<DateTime, JEVisSample>> mapNotes = new HashMap<>();
        Map<String, List<JEVisSample>> map = new HashMap<>();
        for (ChartDataModel mdl : model.getSelectedData()) {
            List<JEVisSample> filteredSamples = new ArrayList<>();
            for (JEVisSample jeVisSample : mdl.getSamples()) {
                if (jeVisSample.getTimestamp().equals(minDate)
                        || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                        || jeVisSample.getTimestamp().equals(maxDate)) {
                    filteredSamples.add(jeVisSample);
                }
            }
            map.put(mdl.getObject().getName(), filteredSamples);

            Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
            for (JEVisObject jeVisObject : mdl.getObject().getChildren()) {
                try {
                    if (jeVisObject.getJEVisClassName().equals(DATA_NOTES)) {
                        JEVisAttribute notes = jeVisObject.getAttribute(USER_NOTES);
                        if (notes != null && notes.hasSample()) {
                            for (JEVisSample jeVisSample : notes.getAllSamples()) {
                                if (jeVisSample.getTimestamp().equals(minDate)
                                        || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                                        || jeVisSample.getTimestamp().equals(maxDate)) {
                                    sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
                                }
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mapNotes.put(mdl.getObject().getName(), sampleMap);
        }

        /**
         * Building the final date and value columns
         */
        for (int i = 0; i < dateColumn.size(); i++) {
            StringBuilder s = new StringBuilder();
            s.append(dateColumn.get(i)).append(COL_SEP);

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
                s.append(COL_SEP);
                if (withUserNotes && timeStamp != null) {
                    Map<DateTime, JEVisSample> notes = mapNotes.get(name);
                    JEVisSample sample = notes.get(timeStamp);
                    if (sample != null) {
                        s.append(sample.getValueAsString());
                    }
                    s.append(COL_SEP);
                } else if (withUserNotes) {
                    s.append(COL_SEP);
                }
            }
            sb.append(s);
            sb.append(System.getProperty(LINE_SEPARATOR));
        }

        return sb.toString();
    }

    private String createCSVStringMulti() throws JEVisException {
        final StringBuilder sb = new StringBuilder();

        for (ChartSettings cset : charts) {
            StringBuilder header = new StringBuilder(NAME);
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    String objectName = mdl.getObject().getName();

                    header.append(COL_SEP).append(objectName);
                    if (mdl.getDataProcessor() != null) {
                        String dpName = mdl.getDataProcessor().getName();
                        header.append(" (").append(dpName).append(")");
                    }

                    if (withUserNotes) {
                        header.append(COL_SEP);
                    }
                }
            }
            header.append(COL_SEP);
            sb.append(header);
        }

        sb.append(System.getProperty(LINE_SEPARATOR));

        for (ChartSettings cset : charts) {
            sb.append(ID);
            sb.append(COL_SEP);

            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    if (mdl.getDataProcessor() != null) {
                        sb.append(mdl.getDataProcessor().getID());
                    } else {
                        sb.append(mdl.getObject().getID());
                    }
                    sb.append(COL_SEP);

                    if (withUserNotes) {
                        sb.append(COL_SEP);
                    }
                }
            }
        }

        sb.append(System.getProperty(LINE_SEPARATOR));
        sb.append(System.getProperty(LINE_SEPARATOR));

        for (ChartSettings cset : charts) {
            sb.append(UNIT);
            sb.append(COL_SEP);

            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    String currentUnit = UnitManager.getInstance().format(mdl.getUnit());
                    if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString())) {
                        currentUnit = mdl.getUnit().getLabel();
                    }
                    sb.append(currentUnit);
                    sb.append(COL_SEP);

                    if (withUserNotes) {
                        sb.append(COL_SEP);
                    }
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            StringBuilder row = new StringBuilder();
            row.append(System.getProperty(LINE_SEPARATOR));
            for (ChartSettings cset : charts) {
                switch (i) {
                    case 0:
                        row.append(MIN);
                        row.append(COL_SEP);
                        break;
                    case 1:
                        row.append(MAX);
                        row.append(COL_SEP);
                        break;
                    case 2:
                        row.append(AVG);
                        row.append(COL_SEP);
                        break;
                    case 3:
                        row.append(SUM);
                        row.append(COL_SEP);
                        break;
                }
                for (ChartDataModel mdl : model.getSelectedData()) {
                    if (mdl.getSelectedcharts().contains(cset.getId())) {
                        switch (i) {
                            case 0:
                                row.append(numberFormat.format(mdl.getMin()));
                                row.append(COL_SEP);
                                break;
                            case 1:
                                row.append(numberFormat.format(mdl.getMax()));
                                row.append(COL_SEP);
                                break;
                            case 2:
                                row.append(numberFormat.format(mdl.getAvg()));
                                row.append(COL_SEP);
                                break;
                            case 3:
                                row.append(numberFormat.format(mdl.getSum()));
                                row.append(COL_SEP);
                                break;
                        }

                        if (withUserNotes) {
                            row.append(COL_SEP);
                        }
                    }
                }
            }
            sb.append(row);
        }

        sb.append(System.getProperty(LINE_SEPARATOR));
        sb.append(System.getProperty(LINE_SEPARATOR));
        for (ChartSettings cset : charts) {
            StringBuilder dateHeader = new StringBuilder(DATE);
            dateHeader.append(COL_SEP);
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelectedcharts().contains(cset.getId())) {
                    dateHeader.append(COL_SEP);

                    if (withUserNotes) {
                        dateHeader.append(NOTE);
                        dateHeader.append(COL_SEP);
                    }
                }
            }
            sb.append(dateHeader);
        }
        sb.append(System.getProperty(LINE_SEPARATOR));

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
                        if (sample.getTimestamp().equals(minDate)
                                || (sample.getTimestamp().isAfter(minDate) && sample.getTimestamp().isBefore(maxDate))
                                || sample.getTimestamp().equals(maxDate)) {
                            dateColumn.add(standard.print(sample.getTimestamp()));
                        }
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
                    List<JEVisSample> filteredSamples = new ArrayList<>();
                    for (JEVisSample jeVisSample : mdl.getSamples()) {
                        if (jeVisSample.getTimestamp().equals(minDate)
                                || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                                || jeVisSample.getTimestamp().equals(maxDate)) {
                            filteredSamples.add(jeVisSample);
                        }
                    }
                    map.put(mdl.getObject().getName(), filteredSamples);

                    Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
                    for (JEVisObject jeVisObject : mdl.getObject().getChildren()) {
                        try {
                            if (jeVisObject.getJEVisClassName().equals(DATA_NOTES)) {
                                JEVisAttribute notes = jeVisObject.getAttribute(USER_NOTES);
                                if (notes != null && notes.hasSample()) {
                                    for (JEVisSample jeVisSample : notes.getAllSamples()) {
                                        if (jeVisSample.getTimestamp().equals(minDate)
                                                || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                                                || jeVisSample.getTimestamp().equals(maxDate)) {
                                            sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
                                        }
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
                    str.append(listDateColumns.get(chartsIndex).get(i)).append(COL_SEP);
                } else {
                    str.append(COL_SEP);
                }

                for (ChartDataModel mdl : model.getSelectedData()) {
                    String objName = mdl.getObject().getName();
                    if (mdl.getSelectedcharts().contains(cset.getId())) {
                        if (hasValues) {
                            JEVisSample sample1 = listMaps.get(chartsIndex).get(objName).get(i);
                            String formattedValue = numberFormat.format(sample1.getValueAsDouble());
                            str.append(formattedValue).append(COL_SEP);
                            if (withUserNotes) {
                                JEVisSample sample = listNotes.get(chartsIndex).get(objName).get(sample1.getTimestamp());
                                if (sample != null) {
                                    str.append(sample.getValueAsString());
                                }
                                str.append(COL_SEP);
                            }
                        } else {
                            str.append(COL_SEP);
                            if (withUserNotes) {
                                str.append(COL_SEP);
                            }
                        }
                    }
                }
            }
            sb.append(str);
            sb.append(System.getProperty(LINE_SEPARATOR));
        }

        return sb.toString();
    }
}
