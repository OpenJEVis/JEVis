package org.jevis.jeconfig.plugin.charts;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;
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
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.FileNames;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.Chart;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import tech.units.indriya.AbstractUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;

public class ChartExportCSV {
    private static final Logger logger = LogManager.getLogger(ChartExportCSV.class);
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
    private final String USER_NOTES = "Value";
    private final String LINE_SEPARATOR = "line.separator";
    private final String COL_SEP = ";";
    private final String SPACE = " ";
    private final DateTimeFormatter standard = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private final Map<Chart, List<ChartDataRow>> model;
    private final JEVisDataSource ds;
    private final DateTime minDate;
    private final DateTime maxDate;
    private final ObservableList<Locale> choices = FXCollections.observableArrayList(Locale.getAvailableLocales());
    private final AlphanumComparator ac = new AlphanumComparator();
    private Boolean xlsx = false;
    private boolean needSave = false;
    private File destinationFile;
    private Boolean multipleCharts = false;
    private Locale selectedLocale;
    private NumberFormat numberFormat;
    private Boolean withUserNotes = false;

    public ChartExportCSV(JEVisDataSource ds, Map<Chart, List<ChartDataRow>> model, String analysisName, DateTime xAxisLowerBound, DateTime xAxisUpperBound) {
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

        this.ds = ds;
//        this.setDates();
        this.minDate = xAxisLowerBound;
        this.maxDate = xAxisUpperBound;
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

                if (model.size() > 1) {
                    multipleCharts = true;
                }

                String formattedName = FileNames.fixName(analysisName);

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("CSV File Destination");
                DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");
                FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", ".csv");
                FileChooser.ExtensionFilter xlsxFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", ".xlsx");
                fileChooser.getExtensionFilters().addAll(csvFilter, xlsxFilter);
                fileChooser.setSelectedExtensionFilter(csvFilter);

                fileChooser.setInitialFileName(
                        formattedName + "_"
                                + I18n.getInstance().getString("plugin.graph.dialog.export.from") + "_"
                                + fmtDate.print(minDate) + "_" + I18n.getInstance().getString("plugin.graph.dialog.export.to") + "_"
                                + fmtDate.print(maxDate));
                File file = fileChooser.showSaveDialog(JEConfig.getStage());
                if (file != null) {
                    String fileExtension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0);
                    if (!file.getAbsolutePath().contains(fileExtension)) {
                        destinationFile = new File(file + fileExtension);
                    } else {
                        destinationFile = file;
                    }
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


    public void export() throws FileNotFoundException, UnsupportedEncodingException, JEVisException {
        StringBuilder exportStrg = null;
        if (!multipleCharts) {
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

        if (!xlsx && destinationFile != null && exportStrg != null) {
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
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            Cell nameHeaderCell = getOrCreateCell(sheet, 0, columnIndex);
            nameHeaderCell.setCellValue(NAME);
            columnIndex++;

            for (ChartDataRow chartDataRow : chartDataRows) {

                String modelTitle = chartDataRow.getName();

                Cell nameHeader = getOrCreateCell(sheet, 0, columnIndex);
                nameHeader.setCellValue(modelTitle);
                columnIndex++;

                if (withUserNotes) {
                    columnIndex++;
                }
            }
        }

        columnIndex = 0;
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            Cell idHeaderCell = getOrCreateCell(sheet, 1, columnIndex);
            idHeaderCell.setCellValue(ID);
            columnIndex++;
            for (ChartDataRow chartDataRow : chartDataRows) {
                Cell idHeader = getOrCreateCell(sheet, 1, columnIndex);
                if (chartDataRow.getDataProcessor() != null) {
                    idHeader.setCellValue(chartDataRow.getDataProcessor().getID());
                } else {
                    idHeader.setCellValue(chartDataRow.getObject().getID());
                }
                columnIndex++;

                if (withUserNotes) {
                    columnIndex++;
                }
            }
        }

        columnIndex = 0;
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            Cell unitCell = getOrCreateCell(sheet, 3, columnIndex);
            unitCell.setCellValue(UNIT);
            columnIndex++;
            for (ChartDataRow chartDataRow : chartDataRows) {

                Cell unitHeader = getOrCreateCell(sheet, 3, columnIndex);
                String currentUnit = UnitManager.getInstance().format(chartDataRow.getUnit());
                if (currentUnit.equals("") || currentUnit.equals(AbstractUnit.ONE.toString())) {
                    currentUnit = chartDataRow.getUnit().getLabel();
                }

                QuantityUnits qu = new QuantityUnits();
                if (chartDataRow.getAggregationPeriod() != AggregationPeriod.NONE && !qu.isQuantityUnit(chartDataRow.getUnit()) && qu.isSumCalculable(chartDataRow.getUnit())) {
                    currentUnit += " (∑ ->" + qu.getSumUnit(chartDataRow.getUnit()).getLabel() + ")";
                }

                unitHeader.setCellValue(currentUnit);
                columnIndex++;

                if (withUserNotes) {
                    columnIndex++;
                }
            }
        }

        columnIndex = 0;
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
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

            for (ChartDataRow chartDataRow : chartDataRows) {
                for (int i = 0; i < 4; i++) {
                    Cell valueCell = getOrCreateCell(sheet, i + 4, columnIndex);
                    switch (i) {
                        case 0:
                            valueCell.setCellValue(numberFormat.format(chartDataRow.getMin().getValue()));
                            break;
                        case 1:
                            valueCell.setCellValue(numberFormat.format(chartDataRow.getMax().getValue()));
                            break;
                        case 2:
                            valueCell.setCellValue(numberFormat.format(chartDataRow.getAvg()));
                            break;
                        case 3:
                            valueCell.setCellValue(numberFormat.format(chartDataRow.getSum()));
                            break;
                    }
                }
                columnIndex++;

                if (withUserNotes) {
                    columnIndex++;
                }
            }
        }

        columnIndex = 0;
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            Cell dateHeaderCell = getOrCreateCell(sheet, 9, columnIndex);
            dateHeaderCell.setCellValue(DATE);
            columnIndex++;
            for (ChartDataRow chartDataRow : chartDataRows) {
                columnIndex++;

                if (withUserNotes) {
                    Cell noteHeader = getOrCreateCell(sheet, 9, columnIndex);
                    noteHeader.setCellValue(NOTE);
                    columnIndex++;
                }
            }
        }

        Map<DateTime, String> allDates = new HashMap<>();
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            DateTime currentStart = null;
            DateTime currentEnd = null;
            for (ChartDataRow chartDataRow : chartDataRows) {

                if (currentStart == null) currentStart = chartDataRow.getSelectedStart();
                else chartDataRow.setSelectedStart(currentStart);
                if (currentEnd == null) currentEnd = chartDataRow.getSelectedEnd();
                else chartDataRow.setSelectedEnd(currentEnd);

                for (JEVisSample sample : chartDataRow.getSamples()) {
                    if (!allDates.containsKey(sample.getTimestamp()) && sample.getTimestamp().equals(minDate)
                            || (sample.getTimestamp().isAfter(minDate) && sample.getTimestamp().isBefore(maxDate))
                            || sample.getTimestamp().equals(maxDate)) {
                        allDates.put(sample.getTimestamp(), standard.print(sample.getTimestamp()));
                    }
                }

            }
        }
        List<DateTime> dateList = new ArrayList<>(allDates.keySet());
        dateList.sort((o1, o2) -> {
            if (o1.isAfter(o2)) return 1;
            else if (o1.equals(o2)) return 0;
            else return -1;
        });

        Map<String, Map<String, List<JEVisSample>>> listMaps = new HashMap<>();
        Map<String, Map<String, Map<DateTime, JEVisSample>>> listNotes = new HashMap<>();
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            Map<String, Map<DateTime, JEVisSample>> mapNotes = new HashMap<>();
            Map<String, List<JEVisSample>> map = new HashMap<>();
            for (ChartDataRow chartDataRow : chartDataRows) {

                List<JEVisSample> filteredSamples = new ArrayList<>();
                for (JEVisSample jeVisSample : chartDataRow.getSamples()) {
                    if (jeVisSample.getTimestamp().equals(minDate)
                            || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                            || jeVisSample.getTimestamp().equals(maxDate)) {
                        filteredSamples.add(jeVisSample);
                    }
                }
                map.put(chartDataRow.getName(), filteredSamples);

                Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
                for (JEVisObject jeVisObject : chartDataRow.getObject().getChildren()) {
                    try {
                        if (jeVisObject.getJEVisClassName().equals(DATA_NOTES)) {
                            JEVisAttribute notes = jeVisObject.getAttribute(USER_NOTES);
                            if (notes != null && notes.hasSample()) {
                                for (JEVisSample jeVisSample : notes.getSamples(chartDataRow.getSelectedStart(), chartDataRow.getSelectedEnd())) {
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
                mapNotes.put(chartDataRow.getName(), sampleMap);
            }
            listMaps.put(entry.getKey().getChartName(), map);
            listNotes.put(entry.getKey().getChartName(), mapNotes);
        }

        columnIndex = 0;
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();

            for (int i = 0; i < dateList.size(); i++) {
                Cell dateCell = getOrCreateCell(sheet, i + 10, columnIndex);
                dateCell.setCellValue(standard.print(dateList.get(i)));
                dateCell.setCellStyle(cellStyleDateTime);
            }
            columnIndex++;

            for (ChartDataRow chartDataRow : chartDataRows) {

                String modelName = chartDataRow.getName();
                List<JEVisSample> jeVisSamples = listMaps.get(entry.getKey().getChartName()).get(modelName);
                Map<DateTime, JEVisSample> dateTimeJEVisSampleMap = listNotes.get(entry.getKey().getChartName()).get(modelName);
                for (JEVisSample jeVisSample : jeVisSamples) {
                    Integer index = null;
                    for (DateTime dateTime : dateList) {
                        if (jeVisSample.getTimestamp().equals(dateTime)) {
                            index = dateList.indexOf(dateTime);
                            break;
                        }
                    }

                    if (index != null) {
                        Cell valueCell = getOrCreateCell(sheet, index + 10, columnIndex);
                        valueCell.setCellValue(jeVisSample.getValueAsDouble());
                        valueCell.setCellStyle(cellStyleValues);

                        if (withUserNotes) {
                            JEVisSample sample = dateTimeJEVisSampleMap.get(jeVisSample.getTimestamp());
                            if (sample != null) {
                                Cell noteCell = getOrCreateCell(sheet, index + 10, columnIndex + 1);
                                noteCell.setCellValue(jeVisSample.getValueAsString());
                            }
                        }
                    }
                }

                columnIndex++;

                if (withUserNotes) columnIndex++;
            }
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
            workbook.write(fileOutputStream);
            workbook.close();
            fileOutputStream.close();
        } catch (Exception e) {
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

        List<ChartDataRow> chartDataRows = new ArrayList<>();
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            chartDataRows = entry.getValue();
        }

        int columnIndex = 1;
        for (ChartDataRow chartDataRow : chartDataRows) {

            String modelTitle = chartDataRow.getName();
            Cell nameHeader = getOrCreateCell(sheet, 0, columnIndex);
            nameHeader.setCellValue(modelTitle);
            columnIndex++;

            if (withUserNotes) {
                columnIndex++;
            }
        }

        columnIndex = 0;
        Cell idHeaderCell = getOrCreateCell(sheet, 1, columnIndex);
        idHeaderCell.setCellValue(ID);
        columnIndex++;
        for (ChartDataRow chartDataRow : chartDataRows) {
            Cell idHeader = getOrCreateCell(sheet, 1, columnIndex);
            if (chartDataRow.getDataProcessor() != null) {
                idHeader.setCellValue(chartDataRow.getDataProcessor().getID());
            } else {
                idHeader.setCellValue(chartDataRow.getObject().getID());
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
        for (ChartDataRow chartDataRow : chartDataRows) {
            Cell unitHeader = getOrCreateCell(sheet, 3, columnIndex);
            String currentUnit = UnitManager.getInstance().format(chartDataRow.getUnit());
            if (currentUnit.equals("") || currentUnit.equals(AbstractUnit.ONE.toString())) {
                try {
                    currentUnit = chartDataRow.getUnit().getLabel();
                } catch (Exception e) {
                    logger.error("Could not get unit.", e);
                }
            }

            QuantityUnits qu = new QuantityUnits();
            if (chartDataRow.getAggregationPeriod() != AggregationPeriod.NONE && !qu.isQuantityUnit(chartDataRow.getUnit()) && qu.isSumCalculable(chartDataRow.getUnit())) {
                currentUnit += " (∑ ->" + qu.getSumUnit(chartDataRow.getUnit()).getLabel() + ")";
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

        for (ChartDataRow chartDataRow : chartDataRows) {
            for (int i = 0; i < 4; i++) {
                Cell valueCell = getOrCreateCell(sheet, i + 4, columnIndex);
                switch (i) {
                    case 0:
                        valueCell.setCellValue(numberFormat.format(chartDataRow.getMin().getValue()));
                        break;
                    case 1:
                        valueCell.setCellValue(numberFormat.format(chartDataRow.getMax().getValue()));
                        break;
                    case 2:
                        valueCell.setCellValue(numberFormat.format(chartDataRow.getAvg()));
                        break;
                    case 3:
                        valueCell.setCellValue(numberFormat.format(chartDataRow.getSum()));
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
        for (ChartDataRow chartDataRow : chartDataRows) {
            columnIndex++;
            if (withUserNotes) {
                Cell noteHeader = getOrCreateCell(sheet, 9, columnIndex);
                noteHeader.setCellValue(NOTE);
                columnIndex++;
            }
        }

        List<String> dateColumn = new ArrayList<>();
        List<DateTime> dates = new ArrayList<>();
        Map<DateTime, Long> dateTimes = new HashMap<>();
        long dateCounter = 0;
        for (ChartDataRow chartDataRow : chartDataRows) {
            for (JEVisSample sample : chartDataRow.getSamples()) {
                if (!dates.contains(sample.getTimestamp()) && (sample.getTimestamp().equals(minDate)
                        || (sample.getTimestamp().isAfter(minDate) && sample.getTimestamp().isBefore(maxDate))
                        || sample.getTimestamp().equals(maxDate))) {
                    dates.add(sample.getTimestamp());
                }
            }
        }

        dates.sort(DateTimeComparator.getInstance());
        for (DateTime dateTime : dates) {
            dateColumn.add(standard.print(dateTime));
            dateTimes.put(dateTime, dateCounter);
            dateCounter++;
        }

        Map<String, Map<DateTime, JEVisSample>> mapNotes = new HashMap<>();
        Map<String, List<JEVisSample>> map = new HashMap<>();
        for (ChartDataRow chartDataRow : chartDataRows) {
            List<JEVisSample> filteredSamples = new ArrayList<>();
            for (JEVisSample jeVisSample : chartDataRow.getSamples()) {
                if (jeVisSample.getTimestamp().equals(minDate)
                        || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                        || jeVisSample.getTimestamp().equals(maxDate)) {
                    filteredSamples.add(jeVisSample);
                }
            }
            map.put(chartDataRow.getName(), filteredSamples);

            Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
            for (JEVisObject jeVisObject : chartDataRow.getObject().getChildren()) {
                try {
                    if (jeVisObject.getJEVisClassName().equals(DATA_NOTES)) {
                        JEVisAttribute notes = jeVisObject.getAttribute(USER_NOTES);
                        if (notes != null && notes.hasSample()) {
                            for (JEVisSample jeVisSample : notes.getSamples(chartDataRow.getSelectedStart(), chartDataRow.getSelectedEnd())) {
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
            mapNotes.put(chartDataRow.getName(), sampleMap);
        }

        columnIndex = 1;
        for (int i = 0; i < dateColumn.size(); i++) {
            Cell dateCell = getOrCreateCell(sheet, i + 10, 0);
            dateCell.setCellValue(dateColumn.get(i));
            dateCell.setCellStyle(cellStyleDateTime);
        }

        for (ChartDataRow chartDataRow : chartDataRows) {
            String modelTitle = chartDataRow.getName();
            List<JEVisSample> jeVisSamples = map.get(modelTitle);
            for (JEVisSample sample : jeVisSamples) {
                DateTime timeStamp = sample.getTimestamp();
                Cell valueCell = getOrCreateCell(sheet, dateTimes.get(timeStamp).intValue() + 10, columnIndex);
                if (!chartDataRow.isStringData()) {
                    valueCell.setCellValue(sample.getValueAsDouble());
                } else {
                    valueCell.setCellValue(sample.getValueAsString());
                }
                valueCell.setCellStyle(cellStyleValues);
            }
            columnIndex++;

            if (withUserNotes) {
                Map<DateTime, JEVisSample> notes = mapNotes.get(modelTitle);
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
            FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
            workbook.write(fileOutputStream);
            workbook.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeFile(File file, StringBuilder sb) {
        try {
            FileUtils.writeStringToFile(file, sb.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private StringBuilder createCSVString() throws JEVisException {
        final StringBuilder sb = new StringBuilder();
        List<ChartDataRow> chartDataRows = new ArrayList<>();
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            chartDataRows = entry.getValue();
        }

        /**
         * Building the header
         */
        StringBuilder header = new StringBuilder(NAME);
        for (ChartDataRow chartDataRow : chartDataRows) {
            String modelTitle = chartDataRow.getName();

            header.append(COL_SEP).append(modelTitle);

            if (withUserNotes) {
                header.append(COL_SEP);
            }
        }

        header.append(COL_SEP);
        header.append(System.getProperty(LINE_SEPARATOR));

        header.append(ID);
        header.append(COL_SEP);

        for (ChartDataRow chartDataRow : chartDataRows) {
            if (chartDataRow.getDataProcessor() != null) {
                header.append(chartDataRow.getDataProcessor().getID());
            } else {
                header.append(chartDataRow.getObject().getID());
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

        for (ChartDataRow chartDataRow : chartDataRows) {
            String currentUnit = UnitManager.getInstance().format(chartDataRow.getUnit());
            if (currentUnit.equals("") || currentUnit.equals(AbstractUnit.ONE.toString())) {
                currentUnit = chartDataRow.getUnit().getLabel();
            }

            QuantityUnits qu = new QuantityUnits();
            if (chartDataRow.getAggregationPeriod() != AggregationPeriod.NONE && !qu.isQuantityUnit(chartDataRow.getUnit()) && qu.isSumCalculable(chartDataRow.getUnit())) {
                currentUnit += " (∑ ->" + qu.getSumUnit(chartDataRow.getUnit()).getLabel() + ")";
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
            for (ChartDataRow chartDataRow : chartDataRows) {
                switch (i) {
                    case 0:
                        header.append(numberFormat.format(chartDataRow.getMin().getValue()));
                        header.append(COL_SEP);
                        break;
                    case 1:
                        header.append(numberFormat.format(chartDataRow.getMax().getValue()));
                        header.append(COL_SEP);
                        break;
                    case 2:
                        header.append(numberFormat.format(chartDataRow.getAvg()));
                        header.append(COL_SEP);
                        break;
                    case 3:
                        header.append(numberFormat.format(chartDataRow.getSum()));
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
        for (ChartDataRow mdl : chartDataRows) {
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
        for (ChartDataRow chartDataRow : chartDataRows) {
            if (firstSet) {
                for (JEVisSample sample : chartDataRow.getSamples()) {
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
        for (ChartDataRow chartDataRow : chartDataRows) {
            List<JEVisSample> filteredSamples = new ArrayList<>();
            for (JEVisSample jeVisSample : chartDataRow.getSamples()) {
                if (jeVisSample.getTimestamp().equals(minDate)
                        || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                        || jeVisSample.getTimestamp().equals(maxDate)) {
                    filteredSamples.add(jeVisSample);
                }
            }
            map.put(chartDataRow.getName(), filteredSamples);

            Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
            for (JEVisObject jeVisObject : chartDataRow.getObject().getChildren()) {
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
            mapNotes.put(chartDataRow.getName(), sampleMap);
        }

        /**
         * Building the final date and value columns
         */
        for (int i = 0; i < dateColumn.size(); i++) {
            StringBuilder s = new StringBuilder();
            s.append(dateColumn.get(i)).append(COL_SEP);

            for (ChartDataRow chartDataRow : chartDataRows) {
                String modelTitle = chartDataRow.getName();
                List<JEVisSample> jeVisSamples = map.get(modelTitle);
                DateTime timeStamp = null;
                if (i < jeVisSamples.size()) {
                    JEVisSample sample = jeVisSamples.get(i);
                    timeStamp = sample.getTimestamp();
                    String formattedValue;
                    if (!chartDataRow.isStringData()) {
                        formattedValue = numberFormat.format(sample.getValueAsDouble());
                    } else {
                        formattedValue = sample.getValueAsString();
                    }

                    s.append(formattedValue);
                }
                s.append(COL_SEP);
                if (withUserNotes && timeStamp != null) {
                    Map<DateTime, JEVisSample> notes = mapNotes.get(modelTitle);
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

        return sb;
    }

    private StringBuilder createCSVStringMulti() throws JEVisException {
        final StringBuilder sb = new StringBuilder();

        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            StringBuilder header = new StringBuilder(NAME);
            for (ChartDataRow chartDataRow : chartDataRows) {
                String modelTitle = chartDataRow.getName();

                header.append(COL_SEP).append(modelTitle);

                if (withUserNotes) {
                    header.append(COL_SEP);
                }
            }
            header.append(COL_SEP);
            sb.append(header);
        }

        sb.append(System.getProperty(LINE_SEPARATOR));

        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            sb.append(ID);
            sb.append(COL_SEP);

            for (ChartDataRow chartDataRow : chartDataRows) {
                if (chartDataRow.getDataProcessor() != null) {
                    sb.append(chartDataRow.getDataProcessor().getID());
                } else {
                    sb.append(chartDataRow.getObject().getID());
                }
                sb.append(COL_SEP);

                if (withUserNotes) {
                    sb.append(COL_SEP);
                }
            }
        }

        sb.append(System.getProperty(LINE_SEPARATOR));
        sb.append(System.getProperty(LINE_SEPARATOR));

        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            sb.append(UNIT);
            sb.append(COL_SEP);

            for (ChartDataRow chartDataRow : chartDataRows) {
                String currentUnit = chartDataRow.getUnitLabel();
                if (currentUnit.equals("") || currentUnit.equals(AbstractUnit.ONE.toString())) {
                    currentUnit = chartDataRow.getUnit().getLabel();
                }

                QuantityUnits qu = new QuantityUnits();
                if (chartDataRow.getAggregationPeriod() != AggregationPeriod.NONE && !qu.isQuantityUnit(chartDataRow.getUnit()) && qu.isSumCalculable(chartDataRow.getUnit())) {
                    currentUnit += " (∑ ->" + qu.getSumUnit(chartDataRow.getUnit()).getLabel() + ")";
                }

                sb.append(currentUnit);
                sb.append(COL_SEP);

                if (withUserNotes) {
                    sb.append(COL_SEP);
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            StringBuilder row = new StringBuilder();
            row.append(System.getProperty(LINE_SEPARATOR));
            for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
                List<ChartDataRow> chartDataRows = entry.getValue();
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
                for (ChartDataRow chartDataRow : chartDataRows) {
                    switch (i) {
                        case 0:
                            row.append(numberFormat.format(chartDataRow.getMin().getValue()));
                            row.append(COL_SEP);
                            break;
                        case 1:
                            row.append(numberFormat.format(chartDataRow.getMax().getValue()));
                            row.append(COL_SEP);
                            break;
                        case 2:
                            row.append(numberFormat.format(chartDataRow.getAvg()));
                            row.append(COL_SEP);
                            break;
                        case 3:
                            row.append(numberFormat.format(chartDataRow.getSum()));
                            row.append(COL_SEP);
                            break;
                    }

                    if (withUserNotes) {
                        row.append(COL_SEP);
                    }
                }
            }
            sb.append(row);
        }

        sb.append(System.getProperty(LINE_SEPARATOR));
        sb.append(System.getProperty(LINE_SEPARATOR));
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            StringBuilder dateHeader = new StringBuilder(DATE);
            dateHeader.append(COL_SEP);
            for (ChartDataRow chartDataRow : chartDataRows) {
                dateHeader.append(COL_SEP);

                if (withUserNotes) {
                    dateHeader.append(NOTE);
                    dateHeader.append(COL_SEP);
                }
            }
            sb.append(dateHeader);
        }
        sb.append(System.getProperty(LINE_SEPARATOR));

        Map<DateTime, String> allDates = new HashMap<>();
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            DateTime currentStart = null;
            DateTime currentEnd = null;
            for (ChartDataRow chartDataRow : chartDataRows) {
                if (currentStart == null) currentStart = chartDataRow.getSelectedStart();
                else chartDataRow.setSelectedStart(currentStart);
                if (currentEnd == null) currentEnd = chartDataRow.getSelectedEnd();
                else chartDataRow.setSelectedEnd(currentEnd);

                for (JEVisSample sample : chartDataRow.getSamples()) {
                    if (!allDates.containsKey(sample.getTimestamp()) && sample.getTimestamp().equals(minDate)
                            || (sample.getTimestamp().isAfter(minDate) && sample.getTimestamp().isBefore(maxDate))
                            || sample.getTimestamp().equals(maxDate)) {
                        allDates.put(sample.getTimestamp(), standard.print(sample.getTimestamp()));
                    }
                }
            }
        }
        List<DateTime> dateList = new ArrayList<>(allDates.keySet());
        dateList.sort((o1, o2) -> {
            if (o1.isAfter(o2)) return 1;
            else if (o1.equals(o2)) return 0;
            else return -1;
        });

        Map<String, Map<String, Map<DateTime, JEVisSample>>> listMaps = new HashMap<>();
        Map<String, Map<String, Map<DateTime, JEVisSample>>> listNotes = new HashMap<>();
        for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
            List<ChartDataRow> chartDataRows = entry.getValue();
            Map<String, Map<DateTime, JEVisSample>> mapNotes = new HashMap<>();
            Map<String, Map<DateTime, JEVisSample>> map = new HashMap<>();
            for (ChartDataRow chartDataRow : chartDataRows) {
                Map<DateTime, JEVisSample> filteredSamples = new HashMap<>();
                for (JEVisSample jeVisSample : chartDataRow.getSamples()) {
                    if (jeVisSample.getTimestamp().equals(minDate)
                            || (jeVisSample.getTimestamp().isAfter(minDate) && jeVisSample.getTimestamp().isBefore(maxDate))
                            || jeVisSample.getTimestamp().equals(maxDate)) {
                        filteredSamples.put(jeVisSample.getTimestamp(), jeVisSample);
                    }
                }
                map.put(chartDataRow.getName(), filteredSamples);

                Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
                for (JEVisObject jeVisObject : chartDataRow.getObject().getChildren()) {
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
                    mapNotes.put(chartDataRow.getName(), sampleMap);
                }
            }
            listMaps.put(entry.getKey().getChartName(), map);
            listNotes.put(entry.getKey().getChartName(), mapNotes);
        }

        for (DateTime ts : dateList) {
            StringBuilder str = new StringBuilder();
            for (Map.Entry<Chart, List<ChartDataRow>> entry : model.entrySet()) {
                List<ChartDataRow> chartDataRows = entry.getValue();

                str.append(standard.print(ts)).append(COL_SEP);

                for (ChartDataRow chartDataRow : chartDataRows) {
                    String modelTitle = chartDataRow.getName();
                    JEVisSample sample1 = listMaps.get(entry.getKey().getChartName()).get(modelTitle).get(ts);
                    if (sample1 != null) {
                        String formattedValue = numberFormat.format(sample1.getValueAsDouble());
                        str.append(formattedValue);
                    }

                    str.append(COL_SEP);

                    if (withUserNotes) {
                        if (sample1 != null) {
                            JEVisSample sample = listNotes.get(entry.getKey().getChartName()).get(modelTitle).get(sample1.getTimestamp());
                            if (sample != null) {
                                str.append(sample.getValueAsString());
                            }
                        }
                        str.append(COL_SEP);
                    }
                }
            }
            sb.append(str);
            sb.append(System.getProperty(LINE_SEPARATOR));
        }

        return sb;
    }
}
