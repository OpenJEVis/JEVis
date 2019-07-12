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
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.ExceptionDialog;
import org.jevis.jeconfig.sample.csvexporttable.CSVExportTableSampleTable;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
    final Button ok = new Button("OK");
    private final BorderPane _view = new BorderPane();
    Label lLineSep = new Label("Field Seperator:");
    TextField fLineSep = new TextField(";");
    Label lEnclosedBy = new Label("Enclosed by:");
    TextField fEnclosedBy = new TextField("");
    Label lDateTimeFormat = new Label("Date Formate:");
    TextField fDateTimeFormat = new TextField("yyyy-MM-dd HH:mm:ss");
    Label lTimeFormate = new Label("Time Formate:");
    Label lDateFormat = new Label("Date Formate:");
    TextField fTimeFormate = new TextField("HH:mm:ss");
    TextField fDateFormat = new TextField("yyyy-MM-dd");
    RadioButton bDateTime = new RadioButton("Date and time in one field:");
    RadioButton bDateTime2 = new RadioButton("Date and time seperated:");
    Label lValueFormate = new Label("Value Formate:");
    TextField fValueFormat = new TextField("###.###");
    Label lHeader = new Label("Custom CSV Header");
    TextField fHeader = new TextField("Example header mit Attribute namen");
    Label lExample = new Label("Preview:");
    TextArea fTextArea = new TextArea("Example");
    Label lPFilePath = new Label("File:");
    TextField fFile = new TextField();
    Button bFile = new Button("Change");
    Button export = new Button("Export");
    File destinationFile;
    List<JEVisSample> _samples = new ArrayList<>();
    TableView tabel = new TableView();
    TableColumn dateTimeColumn = new TableColumn("Datetime");
    TableColumn dateColum = new TableColumn("Date");
    TableColumn valueColum = new TableColumn("Value");
    TableColumn timeColum = new TableColumn("Time");
    private JEVisAttribute _att;
    private boolean needSave = false;
    private boolean _isBuild = false;

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

        Button bValueFaormateHelp = new Button("?");

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

//        gp.add(new Separator(Orientation.HORIZONTAL), 0, ++y, 2, 1);
        gp.add(lHeader, 0, ++y);
        gp.add(fHeader, 1, y);

        gp.add(lFileOrder, 0, ++y);
        gp.add(buildFildOrder(), 1, y);

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
                fileChooser.setTitle("CSV File Destination");
                DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");

                fileChooser.setInitialFileName(attribute.getObject().getName() + "_" + attribute.getName() + "_" + fmtDate.print(new DateTime()) + ".csv");
                File file = fileChooser.showSaveDialog(JEConfig.getStage());
                if (file != null) {
                    destinationFile = file;
                    fFile.setText(file.toString());
                    needSave = true;
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
    public void disableEditing(boolean disable) {
        //TODO
    }

    public boolean doExport() throws FileNotFoundException, UnsupportedEncodingException {

        String exportStrg = createCSVString(Integer.MAX_VALUE);

        if (!fFile.getText().isEmpty() && exportStrg.length() > 90) {
            writeFile(fFile.getText(), exportStrg);
            return true;
        }

        return false;

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

        for (Object column : tabel.getColumns()) {
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
                    tabel.getColumns().removeAll(dateColum, timeColum, valueColum);
                    tabel.getColumns().addAll(dateTimeColumn, valueColum);
                } else {
                    tabel.getColumns().removeAll(dateTimeColumn, valueColum);
                    tabel.getColumns().addAll(dateColum, timeColum, valueColum);
                }
            }
        });

    }

    private Node buildFildOrder() {
        HBox root = new HBox();

//        String help = "Use Drag&Drop to change the oder.";
        tabel.setMaxHeight(18);
        tabel.setPlaceholder(new Region());

        dateColum.setSortable(false);
        dateColum.setCellValueFactory(new PropertyValueFactory<CSVExportTableSampleTable.TableSample, String>("Date"));

        valueColum.setSortable(false);
        valueColum.setCellValueFactory(new PropertyValueFactory<CSVExportTableSampleTable.TableSample, String>("Value"));

        timeColum.setSortable(false);
        timeColum.setCellValueFactory(new PropertyValueFactory<CSVExportTableSampleTable.TableSample, String>("time"));

        dateTimeColumn.setSortable(false);
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<CSVExportTableSampleTable.TableSample, String>("Datetime"));

        tabel.setMinWidth(555d);//TODo: replace Dirty workaround
        tabel.setPrefHeight(200d);//TODo: replace Dirty workaround
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tabel.getColumns().addListener(new ListChangeListener() {

            @Override
            public void onChanged(ListChangeListener.Change change) {
                updatePreview();
            }
        });

        root.getChildren().add(tabel);

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
