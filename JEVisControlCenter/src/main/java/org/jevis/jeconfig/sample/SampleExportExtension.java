/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.ExceptionDialog;
import org.jevis.jeconfig.sample.csvexporttable.CSVExportTableSampleTable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This Dialog export JEVisSamples as csv files with different configurations.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SampleExportExtension implements SampleEditorExtension {
    private static final Logger logger = LogManager.getLogger(SampleExportExtension.class);
    private final static String TITLE = "Export";
    public static String ICON = "1415654364_stock_export.png";
    final JFXButton ok = new JFXButton("OK");
    private final BorderPane _view = new BorderPane();
    Label lLineSep = new Label("Field Seperator:");
    JFXTextField fLineSep = new JFXTextField(";");
    Label lEnclosedBy = new Label("Enclosed by:");
    JFXTextField fEnclosedBy = new JFXTextField("");
    Label lDateTimeFormat = new Label("Date Formate:");
    JFXTextField fDateTimeFormat = new JFXTextField("yyyy-MM-dd HH:mm:ss");
    Label lTimeFormate = new Label("Time Formate:");
    Label lDateFormat = new Label("Date Formate:");
    JFXTextField fTimeFormate = new JFXTextField("HH:mm:ss");
    JFXTextField fDateFormat = new JFXTextField("yyyy-MM-dd");
    JFXRadioButton bDateTime = new JFXRadioButton("Date and time in one field:");
    JFXRadioButton bDateTime2 = new JFXRadioButton("Date and time seperated:");
    Label lValueFormate = new Label("Value Formate:");
    JFXTextField fValueFormat = new JFXTextField("###.###");
    Label lHeader = new Label("Custom CSV Header");
    JFXTextField fHeader = new JFXTextField("Example header mit Attribute namen");
    Label lExample = new Label("Preview:");
    JFXTextArea fTextArea = new JFXTextArea("Example");
    Label lPFilePath = new Label("File:");
    JFXTextField fFile = new JFXTextField();
    JFXButton bFile = new JFXButton("Change");
    JFXButton export = new JFXButton("Export");
    File destinationFile;
    List<JEVisSample> _samples = new ArrayList<>();
    TableView tablel = new TableView();
    TableColumn dateTimeColumn = new TableColumn("Datetime");
    TableColumn dateColum = new TableColumn("Date");
    TableColumn valueColum = new TableColumn("Value");
    TableColumn timeColum = new TableColumn("Time");
    private JEVisAttribute _att;
    private boolean needSave = false;
    private boolean _isBuild = false;
    private Boolean xlsx = false;

    public SampleExportExtension(JEVisAttribute att) {
        _att = att;

    }

    @Override
    public boolean sendOKAction() {

        if (needSave) {
            try {
                if (doExport()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText(I18n.getInstance().getString("csv.export.dialog.success.header"));
                        alert.setContentText(I18n.getInstance().getString("csv.export.dialog.success.message"));
                        alert.showAndWait();
                    });

                    return true;
                } else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setHeaderText(I18n.getInstance().getString("csv.export.dialog.failed.header"));
                        alert.setContentText(I18n.getInstance().getString("csv.export.dialog.failed.message"));
                        alert.showAndWait();
                    });

                    return false;
                }

            } catch (FileNotFoundException ex) {
                logger.fatal(ex);
                ExceptionDialog errDia = new ExceptionDialog();
                errDia.show("Error", "Error while exporting", "Could not write to file", ex, null);
            } catch (UnsupportedEncodingException ex) {
                logger.fatal(ex);
                ExceptionDialog errDia = new ExceptionDialog();
                errDia.show("Error", "Error while exporting", "Unsupported encoding", ex, null);
            }
        }

        return false;
    }

    public void buildGUI(final JEVisAttribute attribute, final List<JEVisSample> samples) {
        _isBuild = true;
        TabPane tabPane = new TabPane();
        tabPane.setMaxWidth(2000);

        String sampleHeader = "";
        if (!samples.isEmpty()) {
            DateTimeFormatter dtfDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter timezone = DateTimeFormat.forPattern("z");
            try {
                sampleHeader = " " + dtfDateTime.print(samples.get(0).getTimestamp())
                        + " - " + dtfDateTime.print(samples.get(samples.size() - 1).getTimestamp())
                        + " " + timezone.print(samples.get(0).getTimestamp());
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        }

//        fHeader.setText(attribute.getObject().getName() + "[" + attribute.getObject().getID() + "] - " + attribute.getName());
        fHeader.setText(attribute.getObject().getName() + " " + attribute.getName() + " " + sampleHeader);

        _samples = samples;
        final ToggleGroup group = new ToggleGroup();

        bDateTime.setToggleGroup(group);
        bDateTime2.setToggleGroup(group);
        bDateTime.setSelected(true);

        fTimeFormate.setDisable(true);
        fDateFormat.setDisable(true);

        JFXButton bValueFaormateHelp = new JFXButton("?");

        HBox fielBox = new HBox(5d);
        HBox.setHgrow(fFile, Priority.ALWAYS);
        fFile.setPrefWidth(300);
        fielBox.getChildren().addAll(fFile, bFile);

//        bFile.setStyle("-fx-background-color: #5db7de;");
        Label lFileOrder = new Label("Field Order:");

        fHeader.setPrefColumnCount(1);
//        fHeader.setDisable(true);

        fTextArea.setPrefColumnCount(5);
        fTextArea.setPrefWidth(500d);
        fTextArea.setPrefHeight(110d);
        fTextArea.setStyle("-fx-font-size: 14;");

        GridPane gp = new GridPane();
        gp.setStyle("-fx-background-color: transparent;");
        gp.setPadding(new Insets(10));
        gp.setHgap(7);
        gp.setVgap(7);

        int y = 0;
        gp.add(lLineSep, 0, y);
        gp.add(fLineSep, 1, y);

        gp.add(lEnclosedBy, 0, ++y);
        gp.add(fEnclosedBy, 1, y);

        gp.add(lValueFormate, 0, ++y);
        gp.add(fValueFormat, 1, y);

        gp.add(new Separator(Orientation.HORIZONTAL), 0, ++y, 2, 1);

        gp.add(bDateTime, 0, ++y, 2, 1);
        gp.add(lDateTimeFormat, 0, ++y);
        gp.add(fDateTimeFormat, 1, y);

        gp.add(bDateTime2, 0, ++y, 2, 1);
        gp.add(lTimeFormate, 0, ++y);
        gp.add(fTimeFormate, 1, y);
        gp.add(lDateFormat, 0, ++y);
        gp.add(fDateFormat, 1, y);

        gp.add(new Separator(Orientation.HORIZONTAL), 0, ++y, 2, 1);

//        gp.add(new Separator(Orientation.HORIZONTAL_TOP_LEFT), 0, ++y, 2, 1);
        gp.add(lHeader, 0, ++y);
        gp.add(fHeader, 1, y);

        gp.add(lFileOrder, 0, ++y);
        gp.add(buildFieldOrder(), 1, y);

        gp.add(lPFilePath, 0, ++y);
        gp.add(fielBox, 1, y);

        gp.add(lExample, 0, ++y, 2, 1);
        gp.add(fTextArea, 0, ++y, 2, 1);

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() != null) {
                    dateChanged();
                    updateOderField();
                }
            }
        });

        fEnclosedBy.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                updatePreview();
            }
        });

        fLineSep.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                updatePreview();
            }
        });

        fValueFormat.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                logger.info("key pressed");
                try {
                    DateTimeFormat.forPattern(fDateTimeFormat.getText());
                    updatePreview();
                } catch (Exception ex) {
                    logger.error("invalid Format");
                }

            }
        });

        fDateTimeFormat.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                logger.info("updateData linesep");
                try {
                    DateTimeFormat.forPattern(fDateTimeFormat.getText());
                    updatePreview();
                } catch (Exception ex) {
                    logger.error("invalid Format");
                }

            }
        });

        fDateFormat.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                logger.info("updateData linesep");
                try {
                    DateTimeFormat.forPattern(fDateFormat.getText());
                    updatePreview();
                } catch (Exception ex) {
                    logger.error("invalid Format");
                }

            }
        });

        fTimeFormate.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                logger.info("updateData linesep");
                try {
                    DateTimeFormat.forPattern(fTimeFormate.getText());
                    updatePreview();
                } catch (Exception ex) {
                    logger.error("invalid Format");

                }

            }
        });

        bFile.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
                FileChooser.ExtensionFilter xlsxFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx");
                fileChooser.getExtensionFilters().addAll(csvFilter, xlsxFilter);
                fileChooser.setSelectedExtensionFilter(csvFilter);
                fileChooser.setTitle("CSV File Destination");
                DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");
                JEVisClass cleanDataClass = null;
                try {
                    cleanDataClass = attribute.getObject().getDataSource().getJEVisClass("Clean data");

                    if (attribute.getObject().getJEVisClass().equals(cleanDataClass)) {
                        fileChooser.setInitialFileName(attribute.getObject().getParents().get(0).getName() + "_"
                                + attribute.getObject().getName() + "_"
                                + attribute.getName() + "_"
                                + fmtDate.print(new DateTime()) + ".csv");
                    } else {
                        fileChooser.setInitialFileName(attribute.getObject().getName() + "_" + attribute.getName() + "_" + fmtDate.print(new DateTime()) + ".csv");
                    }
                    File file = fileChooser.showSaveDialog(JEConfig.getStage());
                    if (file != null) {
                        destinationFile = file;
                        if (fileChooser.getSelectedExtensionFilter().equals(xlsxFilter)) {
                            xlsx = true;
                        }
                        fFile.setText(file.toString());
                        needSave = true;
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        });

//        export.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent t) {
//                writeFile(fFile.getText(), createCSVString(Integer.MAX_VALUE));
//            }
//        });
        fHeader.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                updatePreview();
            }
        });

        updateOderField();
        updatePreview();

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(gp);
//        _view.getChildren().setAll(scroll);
        _view.setCenter(scroll);
//        return gp;
    }

    @Override
    public boolean isForAttribute(JEVisAttribute obj) {
        return true;
    }

    @Override
    public Node getView() {
        return _view;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void update() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!_isBuild) {
                    buildGUI(_att, _samples);
                } else {
//                    _samples = samples;
//                    updateOderField();
                    updatePreview();
                }
            }
        });
    }

    @Override
    public void setSamples(final JEVisAttribute att, final List<JEVisSample> samples) {
        _samples = samples;
        _att = att;
    }

    @Override
    public void setDateTimeZone(DateTimeZone dateTimeZone) {

    }

    @Override
    public void disableEditing(boolean disable) {
        //TODO
    }

    public boolean doExport() throws FileNotFoundException, UnsupportedEncodingException {

        if (!xlsx) {
            String exportStrg = createCSVString(Integer.MAX_VALUE);
            if (!fFile.getText().isEmpty() && exportStrg.length() > 90) {
                writeFile(fFile.getText(), exportStrg);
                return true;
            }
        } else {
            XSSFWorkbook workbook = new XSSFWorkbook(); //create workbook

            XSSFDataFormat dataFormatDates = workbook.createDataFormat();
            dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
            CellStyle cellStyleDateTime = workbook.createCellStyle();
            cellStyleDateTime.setDataFormat((short) 165);

            dataFormatDates.putFormat((short) 166, "YYYY-MM-dd");
            CellStyle cellStyleDate = workbook.createCellStyle();
            cellStyleDate.setDataFormat((short) 166);

            dataFormatDates.putFormat((short) 167, "HH:MM:ss");
            CellStyle cellStyleTime = workbook.createCellStyle();
            cellStyleTime.setDataFormat((short) 167);

            CellStyle cellStyleValues = workbook.createCellStyle();
            cellStyleValues.setDataFormat((short) 4);

            Sheet sheet = null;

            JEVisClass cleanDataClass = null;
            try {
                cleanDataClass = _att.getObject().getDataSource().getJEVisClass("Clean Data");

                if (_att.getObject().getJEVisClass().equals(cleanDataClass)) {
                    sheet = workbook.createSheet(_att.getObject().getParents().get(0).getName() + "_"
                            + _att.getObject().getName() + "_"
                            + _att.getName());
                } else {
                    sheet = workbook.createSheet(_att.getObject().getName() + "_" + _att.getName());
                }
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            if (sheet != null) {
                /**
                 * create the table header
                 */
                Cell headerCell = getOrCreateCell(sheet, 0, 0);
                headerCell.setCellValue(fHeader.getText());

                DateTimeFormatter dtfDateTime = DateTimeFormat.forPattern("yyyy");
                DateTimeFormatter dtfDate = DateTimeFormat.forPattern("yyyy");
                DateTimeFormatter dtfTime = DateTimeFormat.forPattern("yyyy");

                if (bDateTime.isSelected()) {
                    dtfDateTime = DateTimeFormat.forPattern(fDateTimeFormat.getText());
                } else {
                    dtfDate = DateTimeFormat.forPattern(fDateFormat.getText());
                    dtfTime = DateTimeFormat.forPattern(fTimeFormate.getText());
                }
                List<TableColumn> fieldOrder = new ArrayList<>();

                for (Object column : tablel.getColumns()) {
                    fieldOrder.add((TableColumn) column);
                }
                int primType = JEVisConstants.PrimitiveType.STRING;
                int guiType = 0;
                try {
                    primType = _att.getType().getPrimitiveType();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                int count = 0;
                for (TableColumn tableColumn : fieldOrder) {
                    int i = fieldOrder.indexOf(tableColumn);
                    if (tableColumn.equals(valueColum)) {
                        ((XSSFSheet) sheet).getColumnHelper().setColDefaultStyle(i, cellStyleValues);
                    } else if (tableColumn.equals(dateTimeColumn)) {
                        ((XSSFSheet) sheet).getColumnHelper().setColDefaultStyle(i, cellStyleDateTime);
                    } else if (tableColumn.equals(dateColum)) {
                        ((XSSFSheet) sheet).getColumnHelper().setColDefaultStyle(i, cellStyleDate);
                    } else if (tableColumn.equals(timeColum)) {
                        ((XSSFSheet) sheet).getColumnHelper().setColDefaultStyle(i, cellStyleTime);
                    }
                }

                for (JEVisSample sample : _samples) {
                    count++;
                    try {
                        for (TableColumn column : fieldOrder) {
                            if (column.equals(valueColum)) {
                                switch (primType) {
                                    case JEVisConstants.PrimitiveType.DOUBLE:
                                        Cell doubleCell = getOrCreateCell(sheet, count, fieldOrder.indexOf(column));
                                        doubleCell.setCellValue(sample.getValueAsDouble());
                                        doubleCell.setCellStyle(cellStyleValues);
                                        break;
                                    case JEVisConstants.PrimitiveType.LONG:
                                        Cell longCell = getOrCreateCell(sheet, count, fieldOrder.indexOf(column));
                                        longCell.setCellValue(sample.getValueAsLong());
                                        longCell.setCellStyle(workbook.getCellStyleAt(1));
                                        break;
                                    case JEVisConstants.PrimitiveType.STRING:
                                        Cell stringCell = getOrCreateCell(sheet, count, fieldOrder.indexOf(column));
                                        if (guiType == JEVisConstants.DisplayType.TEXT_PASSWORD) {
                                            stringCell.setCellValue("**********");
                                        }
                                        stringCell.setCellValue(sample.getValueAsString());
                                        break;
                                    case JEVisConstants.PrimitiveType.PASSWORD_PBKDF2:
                                        Cell pbkdf2Cell = getOrCreateCell(sheet, count, fieldOrder.indexOf(column));
                                        pbkdf2Cell.setCellValue("**********");
                                        break;
                                    default:
                                        Cell defaultCell = getOrCreateCell(sheet, count, fieldOrder.indexOf(column));
                                        defaultCell.setCellValue(sample.getValueAsString());
                                }
                            } else if (column.equals(dateTimeColumn)) {
                                Cell dtfCell = getOrCreateCell(sheet, count, fieldOrder.indexOf(column));
                                dtfCell.setCellValue(dtfDateTime.print(sample.getTimestamp()));
                                dtfCell.setCellStyle(cellStyleDateTime);
                            } else if (column.equals(dateColum)) {
                                Cell dateCell = getOrCreateCell(sheet, count, fieldOrder.indexOf(column));
                                dateCell.setCellValue(dtfDate.print(sample.getTimestamp()));
                                dateCell.setCellStyle(cellStyleDate);
                            } else if (column.equals(timeColum)) {
                                Cell timeCell = getOrCreateCell(sheet, count, fieldOrder.indexOf(column));
                                timeCell.setCellValue(dtfTime.print(sample.getTimestamp()));
                                timeCell.setCellStyle(cellStyleTime);
                            }
                        }
                    } catch (JEVisException ex) {
                        logger.fatal(ex);
                    }
                }

                File exportFile = new File(fFile.getText());
                try {
                    workbook.write(new FileOutputStream(exportFile));
                    workbook.close();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return false;

    }

    private org.apache.poi.ss.usermodel.Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }

        org.apache.poi.ss.usermodel.Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }

        return cell;
    }

    private String createCSVString(int lineCount) {
        final StringBuilder sb = new StringBuilder();

        DateTimeFormatter dtfDateTime = DateTimeFormat.forPattern("yyyy");
        DateTimeFormatter dtfDate = DateTimeFormat.forPattern("yyyy");
        DateTimeFormatter dtfTime = DateTimeFormat.forPattern("yyyy");

        if (bDateTime.isSelected()) {
            dtfDateTime = DateTimeFormat.forPattern(fDateTimeFormat.getText());
        } else {
            dtfDate = DateTimeFormat.forPattern(fDateFormat.getText());
            dtfTime = DateTimeFormat.forPattern(fTimeFormate.getText());
        }

        DecimalFormat decimalFormat = new DecimalFormat(fValueFormat.getText());

        String enclosed = fEnclosedBy.getText();
        String fSeperator = fLineSep.getText();
        List<TableColumn> fieldOrder = new ArrayList<>();

        for (Object column : tablel.getColumns()) {
            fieldOrder.add((TableColumn) column);
        }

        sb.append(fHeader.getText());
        if (!fHeader.getText().isEmpty()) {
            sb.append(System.getProperty("line.separator"));
        }

        int primType = JEVisConstants.PrimitiveType.STRING;
        int guiType = 0;
        try {
            primType = _att.getType().getPrimitiveType();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        int count = 0;
        for (JEVisSample sample : _samples) {
            if (count > lineCount) {
                break;
            } else {
                count++;
            }

            try {

                for (TableColumn column : fieldOrder) {
                    sb.append(enclosed);

                    if (column.equals(valueColum)) {
                        switch (primType) {
                            case JEVisConstants.PrimitiveType.DOUBLE:
                                sb.append(NumberFormat.getInstance().format(sample.getValueAsDouble()));
                                break;
                            case JEVisConstants.PrimitiveType.LONG:
                                sb.append(NumberFormat.getInstance().format(sample.getValueAsLong()));
                                break;
                            case JEVisConstants.PrimitiveType.STRING:
                                if (guiType == JEVisConstants.DisplayType.TEXT_PASSWORD) {
                                    sb.append("**********");
                                }
                                sb.append(sample.getValueAsString());
                                break;
                            case JEVisConstants.PrimitiveType.PASSWORD_PBKDF2:
                                sb.append("**********");
                                break;
                            default:
                                sb.append(sample.getValueAsString());
                        }
//                        sb.append(decimalFormat.format(sample.getValueAsString()));
                    } else if (column.equals(dateTimeColumn)) {
                        sb.append(dtfDateTime.print(sample.getTimestamp()));
                    } else if (column.equals(dateColum)) {
                        sb.append(dtfDate.print(sample.getTimestamp()));
                    } else if (column.equals(timeColum)) {
                        sb.append(dtfTime.print(sample.getTimestamp()));
                    }

                    sb.append(enclosed);
                    if (fieldOrder.indexOf(column) != fieldOrder.size() - 1) {
                        sb.append(fSeperator);
                    }
                }

//                sb.append(enclosed);
//                sb.append(fmtDate.print(sample.getTimestamp()));
//                sb.append(enclosed);
//
//                sb.append(fSeperator);
//
//                sb.append(enclosed);
//                sb.append(decimalFormat.format(sample.getValueAsDouble()));
//                sb.append(enclosed);
                sb.append(System.getProperty("line.separator"));

            } catch (JEVisException ex) {
                logger.fatal(ex);
            }

        }

        return sb.toString();
    }

    private void updateOderField() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (bDateTime.isSelected()) {
                    tablel.getColumns().removeAll(dateColum, timeColum, valueColum);
                    tablel.getColumns().addAll(dateTimeColumn, valueColum);
                } else {
                    tablel.getColumns().removeAll(dateTimeColumn, valueColum);
                    tablel.getColumns().addAll(dateColum, timeColum, valueColum);
                }
            }
        });

    }

    private Node buildFieldOrder() {
        HBox root = new HBox();

//        String help = "Use Drag&Drop to change the oder.";
        tablel.setMaxHeight(18);
        tablel.setPlaceholder(new Region());

        dateColum.setSortable(false);
        dateColum.setCellValueFactory(new PropertyValueFactory<CSVExportTableSampleTable.TableSample, String>("Date"));

        valueColum.setSortable(false);
        valueColum.setCellValueFactory(new PropertyValueFactory<CSVExportTableSampleTable.TableSample, String>("Value"));

        timeColum.setSortable(false);
        timeColum.setCellValueFactory(new PropertyValueFactory<CSVExportTableSampleTable.TableSample, String>("time"));

        dateTimeColumn.setSortable(false);
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<CSVExportTableSampleTable.TableSample, String>("Datetime"));

        tablel.setMinWidth(555d);//TODo: replace Dirty workaround
        tablel.setPrefHeight(200d);//TODo: replace Dirty workaround
        tablel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tablel.getColumns().addListener(new ListChangeListener() {

            @Override
            public void onChanged(ListChangeListener.Change change) {
                updatePreview();
            }
        });

        root.getChildren().add(tablel);

        return root;

    }

    private void writeFile(String file, String text) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer;
//        try {
        writer = new PrintWriter(file, "UTF-8");
        writer.println(text);
        writer.close();

//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(CSVExport.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        } catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(CSVExport.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void dateChanged() {

        lDateTimeFormat.setDisable(bDateTime2.isSelected());
        fDateTimeFormat.setDisable(bDateTime2.isSelected());

        lTimeFormate.setDisable(bDateTime.isSelected());
        lDateFormat.setDisable(bDateTime.isSelected());
        fTimeFormate.setDisable(bDateTime.isSelected());
        fDateFormat.setDisable(bDateTime.isSelected());

    }

    private void updatePreview() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                fTextArea.setText(createCSVString(5));
            }
        });

    }

    public enum Response {

        YES, CANCEL
    }

    public enum FIELDS {

        DATE, TIME, VALUE, DATE_TIME
    }

}
