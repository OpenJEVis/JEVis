package org.jevis.jeconfig.plugin.meters;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.JEVisDates;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.type.GUIConstants;
import org.jevis.jeconfig.dialog.*;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;

public class MeterPlugin implements Plugin {
    public static final String MEASUREMENT_INSTRUMENT_CLASS = "Measurement Instrument";
    private static final Logger logger = LogManager.getLogger(MeterPlugin.class);
    private static final double EDITOR_MAX_HEIGHT = 50;
    private final int toolBarIconSize = 20;
    private final int tableIconSize = 18;
    public static String PLUGIN_NAME = "Meter Plugin";
    private static Method columnToFitMethod;
    private Image taskImage = JEConfig.getImage("measurement_instrument.png");

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private final JEVisDataSource ds;
    private final String title;
    private final BorderPane borderPane = new BorderPane();
    private final ToolBar toolBar = new ToolBar();
    private TabPane tabPane = new TabPane();
    private boolean initialized = false;
    Map<JEVisAttribute, AttributeValueChange> changeMap = new HashMap<>();

    public MeterPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;
        this.borderPane.setCenter(tabPane);

        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                Platform.runLater(() -> {
                    Tab selectedItem = this.tabPane.getSelectionModel().getSelectedItem();
                    if (selectedItem != null && selectedItem.getContent() instanceof TableView) {
                        TableView<MeterRow> tableView = (TableView<MeterRow>) selectedItem.getContent();
                        autoFitTable(tableView);
                    }
                });
            }
        });

        initToolBar();

    }

    public static void autoFitTable(TableView<MeterRow> tableView) {
//        tableView.getItems().addListener(new ListChangeListener<Object>() {
//            @Override
//            public void onChanged(Change<?> c) {
        for (TableColumn<MeterRow, ?> column : tableView.getColumns()) {
            if (column.isVisible()) {
                try {
                    if (tableView.getSkin() != null) {
                        columnToFitMethod.invoke(tableView.getSkin(), column, -1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

//            }
//        });
    }

    private void createColumns(TableView<MeterRow> tableView, JEVisClass jeVisClass) {

        try {
            TableColumn<MeterRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.meters.table.measurementpoint.columnname"));
            nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getObject().getName()));
            nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

            tableView.getColumns().add(nameColumn);

            JEVisType onlineIdType = jeVisClass.getType("Online ID");
            JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");
            JEVisType multiplierType = cleanDataClass.getType("Value Multiplier");
            JEVisType locationType = jeVisClass.getType("Location");
            JEVisType pictureType = jeVisClass.getType("Picture");
            JEVisType measuringPointIdType = jeVisClass.getType("Measuring Point ID");
            JEVisType measuringPointName = jeVisClass.getType("Measuring Point Name");

            for (JEVisType type : jeVisClass.getTypes()) {
                TableColumn<MeterRow, JEVisAttribute> column = new TableColumn<>(I18nWS.getInstance().getTypeName(jeVisClass.getName(), type.getName()));
                column.setStyle("-fx-alignment: CENTER;");
                if (type.equals(locationType) || type.equals(measuringPointIdType) || type.equals(measuringPointName)) {
                    column.setVisible(false);
                }
                column.setId(type.getName());
                column.setCellValueFactory(param -> {
                    try {
                        return new ReadOnlyObjectWrapper<>(param.getValue().getObject().getAttribute(type));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return new ReadOnlyObjectWrapper<>();
                });

                switch (type.getPrimitiveType()) {
                    case JEVisConstants.PrimitiveType.LONG:
                        column.setCellFactory(valueCellInteger());
                        break;
                    case JEVisConstants.PrimitiveType.DOUBLE:
                        column.setCellFactory(valueCellDouble());
                        break;
                    case JEVisConstants.PrimitiveType.FILE:
                        column.setCellFactory(valueCellFile());
                        column.setMinWidth(125);
                        break;
                    case JEVisConstants.PrimitiveType.BOOLEAN:
                        column.setCellFactory(valueCellBoolean());
                        break;
                    default:
                        if (type.getName().equalsIgnoreCase("Password") || type.getPrimitiveType() == JEVisConstants.PrimitiveType.PASSWORD_PBKDF2) {
                            column.setCellFactory(valueCellStringPassword());
                        } else if (type.getGUIDisplayType().equals(GUIConstants.TARGET_OBJECT.getId()) || type.getGUIDisplayType().equals(GUIConstants.TARGET_ATTRIBUTE.getId())) {
                            column.setCellFactory(valueCellTargetSelection());
                            column.setMinWidth(120);
                        } else if (type.getGUIDisplayType().equals(GUIConstants.DATE_TIME.getId()) || type.getGUIDisplayType().equals(GUIConstants.BASIC_TEXT_DATE_FULL.getId())) {
                            column.setCellFactory(valueCellDateTime());
                            column.setMinWidth(110);
                        } else {
                            column.setCellFactory(valueCellString());
                        }

                        break;
                }

                tableView.getColumns().add(column);

                if (type.equals(onlineIdType) && true == false) {
                    TableColumn<MeterRow, Object> multiplierColumn = new TableColumn<>(I18nWS.getInstance().getTypeName(cleanDataClass.getName(), multiplierType.getName()));
                    multiplierColumn.setStyle("-fx-alignment: CENTER;");
                    multiplierColumn.setId(multiplierType.getName());
                    multiplierColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getObject()));
                    multiplierColumn.setCellFactory(new Callback<TableColumn<MeterRow, Object>, TableCell<MeterRow, Object>>() {
                        @Override
                        public TableCell<MeterRow, Object> call(TableColumn<MeterRow, Object> param) {
                            return new TableCell<MeterRow, Object>() {
                                @Override
                                protected void updateItem(Object item, boolean empty) {
                                    super.updateItem(item, empty);

                                    if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                                        setText(null);
                                        setGraphic(null);
                                    } else {
                                        MeterRow meterRow = (MeterRow) getTableRow().getItem();
                                        JEVisAttribute jeVisAttribute = meterRow.getAttributeMap().get(multiplierType);

                                        if (jeVisAttribute != null) {
                                            String hash = jeVisAttribute.getObject().getID() + ":" + jeVisAttribute.getName();

                                        }
                                    }
                                }
                            };
                        }
                    });

                    tableView.getColumns().add(multiplierColumn);
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    private Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>> valueCellDateTime() {
        return new Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>>() {
            @Override
            public TableCell<MeterRow, JEVisAttribute> call(TableColumn<MeterRow, JEVisAttribute> param) {
                return new TableCell<MeterRow, JEVisAttribute>() {
                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {

                            JFXDatePicker pickerDate = new JFXDatePicker();

                            if (item.hasSample()) {
                                try {
                                    DateTime date = JEVisDates.parseDefaultDate(item);
                                    LocalDateTime lDate = LocalDateTime.of(
                                            date.get(DateTimeFieldType.year()), date.get(DateTimeFieldType.monthOfYear()), date.get(DateTimeFieldType.dayOfMonth()),
                                            date.get(DateTimeFieldType.hourOfDay()), date.get(DateTimeFieldType.minuteOfHour()), date.get(DateTimeFieldType.secondOfMinute()));
                                    lDate.atZone(ZoneId.of(date.getZone().getID()));
                                    pickerDate.valueProperty().setValue(lDate.toLocalDate());
                                } catch (Exception ex) {
                                    logger.catching(ex);
                                }
                            }

                            pickerDate.valueProperty().addListener((observable, oldValue, newValue) -> {
                                if (newValue != oldValue) {
                                    try {
                                        updateDate(item, getCurrentDate(pickerDate));
                                    } catch (Exception e) {
                                        logger.error(e);
                                    }
                                }
                            });

                            setGraphic(pickerDate);
                        }
                    }

                    private DateTime getCurrentDate(JFXDatePicker pickerDate) {
                        return new DateTime(
                                pickerDate.valueProperty().get().getYear(), pickerDate.valueProperty().get().getMonthValue(), pickerDate.valueProperty().get().getDayOfMonth(),
                                0, 0, 0,
                                DateTimeZone.getDefault());
                    }

                    private void updateDate(JEVisAttribute item, DateTime datetime) throws JEVisException {
                        AttributeValueChange attributeValueChange = changeMap.get(item);
                        if (attributeValueChange != null) {
                            attributeValueChange.setDateTime(datetime);
                        } else {
                            AttributeValueChange valueChange = new AttributeValueChange(item.getPrimitiveType(), item.getType().getGUIDisplayType(), item, datetime);
                            changeMap.put(item, valueChange);
                        }
                    }
                };
            }
        };
    }

    private Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>> valueCellTargetSelection() {
        return new Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>>() {
            @Override
            public TableCell<MeterRow, JEVisAttribute> call(TableColumn<MeterRow, JEVisAttribute> param) {
                return new TableCell<MeterRow, JEVisAttribute>() {
                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            MeterRow meterRow = (MeterRow) getTableRow().getItem();

                            Button manSampleButton = new Button("", JEConfig.getImage("if_textfield_add_64870.png", tableIconSize, tableIconSize));
                            manSampleButton.setDisable(true);
                            Button treeButton = new Button("",
                                    JEConfig.getImage("folders_explorer.png", tableIconSize, tableIconSize));
                            treeButton.wrapTextProperty().setValue(true);

                            Button gotoButton = new Button("",
                                    JEConfig.getImage("1476393792_Gnome-Go-Jump-32.png", tableIconSize, tableIconSize));//icon
                            gotoButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.attribute.target.goto.tooltip")));

                            try {
                                if (item.hasSample()) {
                                    addEventManSampleAction(item.getLatestSample(), manSampleButton);
                                    Platform.runLater(() -> manSampleButton.setDisable(false));
                                }

                            } catch (Exception ex) {
                                logger.catching(ex);
                            }

                            gotoButton.setOnAction(event -> {
                                try {
                                    TargetHelper th = new TargetHelper(ds, item);
                                    if (th.isValid() && th.targetAccessible()) {
                                        JEVisObject findObj = ds.getObject(th.getObject().get(0).getID());
                                        JEConfig.openObjectInPlugin(ObjectPlugin.PLUGIN_NAME, findObj);
                                    }
                                } catch (Exception ex) {
                                    logger.catching(ex);
                                }
                            });

                            treeButton.setOnAction(event -> {
                                try {
                                    SelectTargetDialog selectTargetDialog = null;
                                    JEVisSample latestSample = item.getLatestSample();
                                    TargetHelper th = null;
                                    if (latestSample != null) {
                                        th = new TargetHelper(item.getDataSource(), latestSample.getValueAsString());
                                        if (th.isValid() && th.targetAccessible()) {
                                            logger.info("Target Is valid");
                                            setToolTipText(treeButton, item);
                                        }
                                    }

                                    List<JEVisTreeFilter> allFilter = new ArrayList<>();
                                    JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataFilter();
                                    allFilter.add(allDataFilter);

                                    selectTargetDialog = new SelectTargetDialog(allFilter, allDataFilter, null, SelectionMode.SINGLE);
                                    selectTargetDialog.setInitOwner(treeButton.getScene().getWindow());

                                    List<UserSelection> openList = new ArrayList<>();

                                    if (th != null && !th.getObject().isEmpty()) {
                                        for (JEVisObject obj : th.getObject()) {
                                            openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
                                        }
                                    }

                                    if (selectTargetDialog.show(
                                            ds,
                                            I18n.getInstance().getString("dialog.target.data.title"),
                                            openList
                                    ) == SelectTargetDialog.Response.OK) {
                                        logger.trace("Selection Done");

                                        String newTarget = "";
                                        List<UserSelection> selections = selectTargetDialog.getUserSelection();
                                        for (UserSelection us : selections) {
                                            int index = selections.indexOf(us);
                                            if (index > 0) newTarget += ";";

                                            newTarget += us.getSelectedObject().getID();
                                            if (us.getSelectedAttribute() != null) {
                                                newTarget += ":" + us.getSelectedAttribute().getName();

                                            } else {
                                                newTarget += ":Value";
                                            }
                                        }


                                        JEVisSample newTargetSample = item.buildSample(new DateTime(), newTarget);
                                        newTargetSample.commit();
                                        try {
                                            addEventManSampleAction(newTargetSample, manSampleButton);
                                            manSampleButton.setDisable(false);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }

                                    }
                                    setToolTipText(treeButton, item);

                                } catch (Exception ex) {
                                    logger.catching(ex);
                                }
                            });


                            HBox hBox = new HBox(treeButton, manSampleButton);
                            hBox.setAlignment(Pos.CENTER);
                            hBox.setSpacing(4);

                            if (setToolTipText(treeButton, item)) {
                                hBox.getChildren().add(gotoButton);
                            }

                            VBox vBox = new VBox(hBox);
                            vBox.setAlignment(Pos.CENTER);
                            setGraphic(vBox);
                        }

                    }
                };
            }

        };
    }

    private void addEventManSampleAction(JEVisSample targetSample, Button buttonToAddEvent) {
        EnterDataDialog enterDataDialog = new EnterDataDialog(getDataSource());
        if (targetSample != null) {
            try {
                TargetHelper th = new TargetHelper(getDataSource(), targetSample.getValueAsString());
                if (th.isValid() && th.targetAccessible()) {
                    JEVisSample lastValue = th.getAttribute().get(0).getLatestSample();
                    enterDataDialog.setTarget(false, th.getAttribute().get(0));
                    enterDataDialog.setSample(lastValue);
                    enterDataDialog.setShowValuePrompt(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        buttonToAddEvent.setOnAction(event -> {
            enterDataDialog.showPopup(buttonToAddEvent);
        });
    }

    private boolean setToolTipText(Button treeButton, JEVisAttribute att) {
        boolean foundTarget = false;
        try {
            TargetHelper th = new TargetHelper(ds, att);

            if (th.isValid() && th.targetAccessible()) {

                StringBuilder bText = new StringBuilder();

                JEVisClass cleanData = ds.getJEVisClass("Clean Data");

                for (JEVisObject obj : th.getObject()) {
                    int index = th.getObject().indexOf(obj);
                    if (index > 0) bText.append("; ");

                    if (obj.getJEVisClass().equals(cleanData)) {
                        List<JEVisObject> parents = obj.getParents();
                        if (!parents.isEmpty()) {
                            for (JEVisObject parent : parents) {
                                bText.append("[");
                                bText.append(parent.getID());
                                bText.append("] ");
                                bText.append(parent.getName());
                                bText.append(" / ");
                            }
                        }
                    }

                    bText.append("[");
                    bText.append(obj.getID());
                    bText.append("] ");
                    bText.append(obj.getName());

                    if (th.hasAttribute()) {

                        bText.append(" - ");
                        bText.append(th.getAttribute().get(index).getName());

                    }

                    foundTarget = true;
                }

                Platform.runLater(() -> treeButton.setTooltip(new Tooltip(bText.toString())));
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }
        return foundTarget;
    }

    private Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>> valueCellString() {
        return new Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>>() {
            @Override
            public TableCell<MeterRow, JEVisAttribute> call(TableColumn<MeterRow, JEVisAttribute> param) {
                return new TableCell<MeterRow, JEVisAttribute>() {
                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            MeterRow meterRow = (MeterRow) getTableRow().getItem();

                            JFXTextField textField = new JFXTextField();
                            try {
                                JEVisAttribute attribute = meterRow.getAttributeMap().get(item.getType());
                                if (attribute != null && attribute.hasSample()) {
                                    textField.setText(attribute.getLatestSample().getValueAsString());
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    AttributeValueChange attributeValueChange = changeMap.get(item);
                                    if (attributeValueChange != null) {
                                        attributeValueChange.setStringValue(newValue);
                                    } else {
                                        AttributeValueChange valueChange = new AttributeValueChange(item.getPrimitiveType(), item.getType().getGUIDisplayType(), item, newValue);
                                        changeMap.put(item, valueChange);
                                    }
                                } catch (Exception ex) {
                                    logger.error("Error in string text", ex);
                                }
                            });

                            setGraphic(textField);
                        }

                    }
                };
            }

        };
    }

    private Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>> valueCellStringPassword() {
        return null;
    }

    private Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>> valueCellBoolean() {
        return null;
    }

    private Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>> valueCellFile() {
        return new Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>>() {

            @Override
            public TableCell<MeterRow, JEVisAttribute> call(TableColumn<MeterRow, JEVisAttribute> param) {
                return new TableCell<MeterRow, JEVisAttribute>() {

                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Button downloadButton = new Button("", JEConfig.getImage("698925-icon-92-inbox-download-48.png", tableIconSize, tableIconSize));
                            String fileName = "";

                            AttributeValueChange valueChange;
                            if (changeMap.get(item) == null) {
                                valueChange = new AttributeValueChange();
                                try {
                                    valueChange.setPrimitiveType(item.getPrimitiveType());
                                    valueChange.setGuiDisplayType(item.getType().getGUIDisplayType());
                                    valueChange.setAttribute(item);
                                } catch (JEVisException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                valueChange = changeMap.get(item);
                            }

                            Button previewButton = new Button("", JEConfig.getImage("export-image.png", tableIconSize, tableIconSize));
                            previewButton.setDisable(true);
                            try {
                                downloadButton.setDisable(!item.hasSample());
                                if (item.hasSample()) {
                                    valueChange.setJeVisFile(item.getLatestSample().getValueAsFile());
                                    fileName = valueChange.getJeVisFile().getFilename();
                                    String finalFileName = fileName;
                                    Platform.runLater(() -> downloadButton.setTooltip(new Tooltip(finalFileName + " " + I18n.getInstance().getString("plugin.object.attribute.file.download"))));

                                    setPreviewButton(previewButton, valueChange);
                                    previewButton.setDisable(false);
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }

                            HBox hBox = new HBox();
                            hBox.setAlignment(Pos.CENTER);
                            hBox.setSpacing(4);

                            Button uploadButton = new Button("", JEConfig.getImage("1429894158_698394-icon-130-cloud-upload-48.png", tableIconSize, tableIconSize));

                            downloadButton.setOnAction(event -> {
                                try {
                                    if (valueChange.getJeVisFile() != null) {
                                        FileChooser fileChooser = new FileChooser();
                                        fileChooser.setInitialFileName(valueChange.getJeVisFile().getFilename());
                                        fileChooser.setTitle(I18n.getInstance().getString("plugin.object.attribute.file.download.title"));
                                        fileChooser.getExtensionFilters().addAll(
                                                new FileChooser.ExtensionFilter("All Files", "*.*"));
                                        File selectedFile = fileChooser.showSaveDialog(null);
                                        if (selectedFile != null) {
                                            JEConfig.setLastPath(selectedFile);
                                            valueChange.getJeVisFile().saveToFile(selectedFile);
                                        }
                                    }
                                } catch (Exception ex) {
                                    logger.fatal(ex);
                                }

                            });

                            uploadButton.setOnAction(event -> {
                                try {
                                    FileChooser fileChooser = new FileChooser();
                                    fileChooser.setInitialDirectory(JEConfig.getLastPath());
                                    fileChooser.setTitle(I18n.getInstance().getString("plugin.object.attribute.file.upload"));
                                    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
                                    File selectedFile = fileChooser.showOpenDialog(JEConfig.getStage());
                                    if (selectedFile != null) {
                                        try {
                                            JEConfig.setLastPath(selectedFile);
                                            JEVisFile jfile = new JEVisFileImp(selectedFile.getName(), selectedFile);
                                            JEVisSample fileSample = item.buildSample(new DateTime(), jfile);
                                            fileSample.commit();
                                            valueChange.setJeVisFile(jfile);

                                            downloadButton.setDisable(false);
                                            setPreviewButton(previewButton, valueChange);
                                            previewButton.setDisable(false);

                                        } catch (Exception ex) {
                                            logger.catching(ex);
                                        }
                                    }
                                } catch (Exception ex) {
                                    logger.error("Error in string text", ex);
                                }
                            });


                            hBox.getChildren().addAll(uploadButton, downloadButton, previewButton);

                            VBox vBox = new VBox(hBox);
                            vBox.setAlignment(Pos.CENTER);

                            setGraphic(vBox);
                        }
                    }
                };
            }

        };
    }

    private void setPreviewButton(Button button, AttributeValueChange valueChange) {

        button.setOnAction(event -> {
            boolean isPDF = false;
            String fileName = valueChange.getJeVisFile().getFilename();
            String s = FilenameUtils.getExtension(fileName);
            switch (s) {
                case "pdf":
                    isPDF = true;
                    break;
                case "png":
                case "jpg":
                case "jpeg":
                case "gif":
                    isPDF = false;
                    break;
            }

            if (isPDF) {
                PDFViewerDialog pdfViewerDialog = new PDFViewerDialog();
                pdfViewerDialog.show(valueChange.getJeVisFile(), JEConfig.getStage());
            } else {
                ImageViewerDialog imageViewerDialog = new ImageViewerDialog();
                imageViewerDialog.show(valueChange.getJeVisFile(), JEConfig.getStage());
            }
        });
    }

    private Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>> valueCellDouble() {
        return new Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>>() {
            @Override
            public TableCell<MeterRow, JEVisAttribute> call(TableColumn<MeterRow, JEVisAttribute> param) {
                return new TableCell<MeterRow, JEVisAttribute>() {

                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            MeterRow meterRow = (MeterRow) getTableRow().getItem();

                            JFXTextField textField = new JFXTextField();
                            try {
                                JEVisAttribute attribute = meterRow.getAttributeMap().get(item.getType());
                                if (attribute != null && attribute.hasSample()) {
                                    textField.setText(attribute.getLatestSample().getValueAsDouble().toString());
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }

                            NumberFormat numberFormat = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());
                            UnaryOperator<TextFormatter.Change> filter = t -> {
                                if (t.getText().length() > 1) {/** Copy&paste case **/
                                    try {
                                        Number newNumber = numberFormat.parse(t.getText());
                                        t.setText(String.valueOf(newNumber.doubleValue()));
                                    } catch (Exception ex) {
                                        t.setText("");
                                    }
                                } else if (t.getText().matches(",")) {/** to be use the Double.parse **/
                                    t.setText(".");
                                }

                                try {
                                    /** We don't use the NumberFormat to validate, because it is not strict enough **/
                                    Double parse = Double.parseDouble(t.getControlNewText());
                                } catch (Exception ex) {
                                    t.setText("");
                                }
                                return t;
                            };

                            textField.setTextFormatter(new TextFormatter<>(filter));

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    Double value = Double.parseDouble(newValue);
                                    AttributeValueChange attributeValueChange = changeMap.get(item);
                                    if (attributeValueChange != null) {
                                        attributeValueChange.setDoubleValue(value);
                                    } else {
                                        AttributeValueChange valueChange = new AttributeValueChange(item.getPrimitiveType(), item.getType().getGUIDisplayType(), item, value);
                                        changeMap.put(item, valueChange);
                                    }
                                } catch (Exception ex) {
                                    logger.error("Error in double text", ex);
                                }
                            });


                            setGraphic(textField);

                        }

                    }
                };
            }

        };
    }

    private Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>> valueCellInteger() {
        return new Callback<TableColumn<MeterRow, JEVisAttribute>, TableCell<MeterRow, JEVisAttribute>>() {
            @Override
            public TableCell<MeterRow, JEVisAttribute> call(TableColumn<MeterRow, JEVisAttribute> param) {
                return new TableCell<MeterRow, JEVisAttribute>() {
                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            MeterRow meterRow = (MeterRow) getTableRow().getItem();

                            JFXTextField textField = new JFXTextField();
                            try {
                                JEVisAttribute attribute = meterRow.getAttributeMap().get(item.getType());
                                if (attribute != null && attribute.hasSample()) {
                                    textField.setText(attribute.getLatestSample().getValueAsDouble().toString());
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }

                            UnaryOperator<TextFormatter.Change> filter = t -> {

                                if (t.getControlNewText().isEmpty()) {
                                    t.setText("0");
                                } else {
                                    try {
                                        Long bewValue = Long.parseLong(t.getControlNewText());
                                    } catch (Exception ex) {
                                        t.setText("");
                                    }
                                }

                                return t;
                            };

                            textField.setTextFormatter(new TextFormatter<>(filter));

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    Long value = Long.parseLong(newValue);
                                    AttributeValueChange attributeValueChange = changeMap.get(item);
                                    if (attributeValueChange != null) {
                                        attributeValueChange.setLongValue(value);
                                    } else {
                                        AttributeValueChange valueChange = new AttributeValueChange(item.getPrimitiveType(), item.getType().getGUIDisplayType(), item, value);
                                        changeMap.put(item, valueChange);
                                    }
                                } catch (Exception ex) {
                                    logger.error("Error in double text", ex);
                                }
                            });


                            setGraphic(textField);

                        }

                    }
                };
            }

        };
    }

    private void initToolBar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", toolBarIconSize, toolBarIconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.progress.tooltip"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> handleRequest(Constants.Plugin.Command.RELOAD));

        Separator sep1 = new Separator(Orientation.VERTICAL);

        ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        save.setOnAction(event -> handleRequest(Constants.Plugin.Command.SAVE));

        Separator sep2 = new Separator(Orientation.VERTICAL);

        ToggleButton newButton = new ToggleButton("", JEConfig.getImage("list-add.png", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newButton);
        newButton.setOnAction(event -> handleRequest(Constants.Plugin.Command.NEW));

        ToggleButton replaceButton = new ToggleButton("", JEConfig.getImage("text_replace.png", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(replaceButton);
        replaceButton.setOnAction(event -> {
            JEVisClassTab selectedItem = (JEVisClassTab) tabPane.getSelectionModel().getSelectedItem();
            TableView<MeterRow> tableView = (TableView<MeterRow>) selectedItem.getContent();

            MeterDialog meterDialog = new MeterDialog(ds, selectedItem.getJeVisClass());
            if (meterDialog.showReplaceWindow(tableView.getSelectionModel().getSelectedItem().getObject()) == Response.OK) {
                handleRequest(Constants.Plugin.Command.RELOAD);
            }
        });

        ToggleButton delete = new ToggleButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
        delete.setOnAction(event -> handleRequest(Constants.Plugin.Command.DELETE));

        Separator sep3 = new Separator(Orientation.VERTICAL);

        ToggleButton printButton = new ToggleButton("", JEConfig.getImage("Print_1493286.png", toolBarIconSize, toolBarIconSize));
        Tooltip printTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.print"));
        printButton.setTooltip(printTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(printButton);

        printButton.setOnAction(event -> {
            Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
            TableView<MeterRow> tableView = (TableView<MeterRow>) selectedItem.getContent();

            Printer printer = null;
            ObservableSet<Printer> printers = Printer.getAllPrinters();
            printer = printers.stream().findFirst().orElse(printer);

            if (printer != null) {
                PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT);
                PrinterJob job = PrinterJob.createPrinterJob(printer);

                if (job.showPrintDialog(JEConfig.getStage().getOwner())) {
                    double pagePrintableWidth = job.getJobSettings().getPageLayout().getPrintableWidth();
                    double pagePrintableHeight = job.getJobSettings().getPageLayout().getPrintableHeight();

                    double prefHeight = tableView.getPrefHeight();
                    double minHeight = tableView.getMinHeight();
                    double maxHeight = tableView.getMaxHeight();

                    tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(EDITOR_MAX_HEIGHT));
                    tableView.minHeightProperty().bind(tableView.prefHeightProperty());
                    tableView.maxHeightProperty().bind(tableView.prefHeightProperty());

                    double scaleX = pagePrintableWidth / tableView.getBoundsInParent().getWidth();
                    double scaleY = scaleX;
                    double localScale = scaleX;

                    double numberOfPages = Math.ceil((tableView.getPrefHeight() * localScale) / pagePrintableHeight);

                    tableView.getTransforms().add(new Scale(scaleX, (scaleY)));
                    tableView.getTransforms().add(new Translate(0, 0));

                    Translate gridTransform = new Translate();
                    tableView.getTransforms().add(gridTransform);

                    for (int i = 0; i < numberOfPages; i++) {
                        gridTransform.setY(-i * (pagePrintableHeight / localScale));
                        job.printPage(pageLayout, tableView);
                    }

                    job.endJob();

                    tableView.prefHeightProperty().unbind();
                    tableView.minHeightProperty().unbind();
                    tableView.maxHeightProperty().unbind();
                    tableView.getTransforms().clear();

                    tableView.setMinHeight(minHeight);
                    tableView.setMaxHeight(maxHeight);
                    tableView.setPrefHeight(prefHeight);
                }
            }
        });

        // -delete
        toolBar.getItems().setAll(reload, sep1, save, sep2, newButton, replaceButton, sep3, printButton);
    }

    @Override
    public String getClassName() {
        return "Meter Plugin";
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public StringProperty nameProperty() {
        return null;
    }

    @Override
    public String getUUID() {
        return null;
    }

    @Override
    public void setUUID(String id) {

    }

    @Override
    public String getToolTip() {
        return I18n.getInstance().getString("plugin.meters.tooltip");
    }

    @Override
    public StringProperty uuidProperty() {
        return null;
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
                return true;
            case Constants.Plugin.Command.DELETE:
                return true;
            case Constants.Plugin.Command.EXPAND:
                return false;
            case Constants.Plugin.Command.NEW:
                return true;
            case Constants.Plugin.Command.RELOAD:
                return true;
            case Constants.Plugin.Command.ADD_TABLE:
                return false;
            case Constants.Plugin.Command.EDIT_TABLE:
                return false;
            case Constants.Plugin.Command.CREATE_WIZARD:
                return false;
            case Constants.Plugin.Command.FIND_OBJECT:
                return false;
            case Constants.Plugin.Command.PASTE:
                return false;
            case Constants.Plugin.Command.COPY:
                return false;
            case Constants.Plugin.Command.CUT:
                return false;
            case Constants.Plugin.Command.FIND_AGAIN:
                return false;
            default:
                return false;
        }
    }

    @Override
    public Node getToolbar() {
        return toolBar;
    }

    @Override
    public void updateToolbar() {

    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {

    }

    @Override
    public void handleRequest(int cmdType) {
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
                DateTime saveTime = new DateTime();
                for (Map.Entry<JEVisAttribute, AttributeValueChange> entry : changeMap.entrySet()) {
                    JEVisAttribute a = entry.getKey();
                    AttributeValueChange attributeValueChange = entry.getValue();
                    try {
                        attributeValueChange.commit(saveTime);
                    } catch (JEVisException e) {
                        logger.error("Could not save {} for attribute {} of object {}:{}", attributeValueChange.toString(), a.getName(), a.getObject().getName(), a.getObject().getID(), e);
                    }

                }
                break;
            case Constants.Plugin.Command.DELETE:
                Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
                TableView<MeterRow> tableView = (TableView<MeterRow>) selectedItem.getContent();

                JEVisObject object = tableView.getSelectionModel().getSelectedItem().getObject();

                Dialog<ButtonType> reallyDelete = new Dialog<>();
                reallyDelete.setTitle(I18n.getInstance().getString("plugin.graph.dialog.delete.title"));
                final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.delete.ok"), ButtonBar.ButtonData.YES);
                final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.delete.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                reallyDelete.setContentText(I18n.getInstance().getString("plugin.meters.dialog.delete.message"));
                reallyDelete.getDialogPane().getButtonTypes().addAll(ok, cancel);
                reallyDelete.showAndWait().ifPresent(response -> {
                    if (response.getButtonData().getTypeCode().equals(ButtonType.YES.getButtonData().getTypeCode())) {
                        try {
                            if (ds.getCurrentUser().canDelete(object.getID())) {
                                ds.deleteObject(object.getID());
                                handleRequest(Constants.Plugin.Command.RELOAD);
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.meters.dialog.delete.error"), cancel);
                                alert.showAndWait();
                            }
                        } catch (JEVisException e) {
                            logger.error("Error: could not delete current meter", e);
                        }
                    }
                });
                break;
            case Constants.Plugin.Command.EXPAND:
                break;
            case Constants.Plugin.Command.NEW:
                MeterDialog meterDialog = new MeterDialog(ds, ((JEVisClassTab) tabPane.getSelectionModel().getSelectedItem()).getJeVisClass());
                if (meterDialog.showNewWindow() == Response.OK) {
                    handleRequest(Constants.Plugin.Command.RELOAD);
                }
                break;
            case Constants.Plugin.Command.RELOAD:
                final String loading = I18n.getInstance().getString("plugin.alarms.reload.progress.message");
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() {
                                updateMessage(loading);
                                try {
                                    if (initialized) {
                                        ds.clearCache();
                                        ds.preload();
                                    } else {
                                        initialized = true;
                                    }

                                    updateList();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        };
                    }
                };
                ProgressDialog pd = new ProgressDialog(service);
                pd.setHeaderText(I18n.getInstance().getString("plugin.reports.reload.progress.header"));
                pd.setTitle(I18n.getInstance().getString("plugin.reports.reload.progress.title"));
                pd.getDialogPane().setContent(null);

                service.start();

                break;
            case Constants.Plugin.Command.ADD_TABLE:
                break;
            case Constants.Plugin.Command.EDIT_TABLE:
                break;
            case Constants.Plugin.Command.CREATE_WIZARD:
                break;
            case Constants.Plugin.Command.FIND_OBJECT:
                break;
            case Constants.Plugin.Command.PASTE:
                break;
            case Constants.Plugin.Command.COPY:
                break;
            case Constants.Plugin.Command.CUT:
                break;
            case Constants.Plugin.Command.FIND_AGAIN:
                break;
        }
    }

    @Override
    public Node getContentNode() {
        return borderPane;
    }

    private void loadTabs(Map<JEVisClass, List<JEVisObject>> allMeters, List<JEVisClass> classes) throws InterruptedException {
        AtomicBoolean hasActiveLoadTask = new AtomicBoolean(false);
        for (Map.Entry<Task, String> entry : JEConfig.getStatusBar().getTaskList().entrySet()) {
            Task task = entry.getKey();
            String s = entry.getValue();
            if (s.equals(MeterPlugin.class.getName() + "Load")) {
                hasActiveLoadTask.set(true);
                break;
            }
        }
        if (!hasActiveLoadTask.get()) {
            classes.forEach(jeVisClass -> {
                Task<JEVisClassTab> task = new Task<JEVisClassTab>() {
                    @Override
                    protected JEVisClassTab call() {
                        TableView<MeterRow> tableView = new TableView<>();
                        JEVisClassTab tab = new JEVisClassTab();
                        List<JEVisObject> listObjects = allMeters.get(jeVisClass);

                        try {
                            tab.setClassName(I18nWS.getInstance().getClassName(jeVisClass));
                            tab.setTableView(tableView);
                            tab.setJEVisClass(jeVisClass);
                            AlphanumComparator ac = new AlphanumComparator();

                            tableView.setFixedCellSize(EDITOR_MAX_HEIGHT);
                            tableView.setSortPolicy(param -> {
                                Comparator<MeterRow> comparator = (t1, t2) -> ac.compare(t1.getObject().getName(), t2.getObject().getName());
                                FXCollections.sort(tableView.getItems(), comparator);
                                return true;
                            });
                            tableView.setTableMenuButtonVisible(true);

                            tab.setClosable(false);
                            createColumns(tableView, jeVisClass);

                            List<MeterRow> meterRows = new ArrayList<>();
                            JEVisType onlineIdType = jeVisClass.getType("Online ID");
                            JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");
                            JEVisType multiplierType = cleanDataClass.getType("Value Multiplier");

                            for (JEVisObject meterObject : listObjects) {
                                Map<JEVisType, JEVisAttribute> map = new HashMap<>();

                                for (JEVisAttribute meterObjectAttribute : meterObject.getAttributes()) {
                                    JEVisType type = null;

                                    try {
                                        type = meterObjectAttribute.getType();

                                        map.put(type, meterObjectAttribute);

                                        if (type.equals(onlineIdType)) {
                                            if (meterObjectAttribute.hasSample()) {
                                                TargetHelper th = new TargetHelper(ds, meterObjectAttribute);

                                                if (!th.getObject().isEmpty()) {
                                                    List<JEVisObject> children = th.getObject().get(0).getChildren(cleanDataClass, true);

                                                    for (JEVisObject cleanObject : children) {
                                                        JEVisAttribute cleanObjectAttribute = cleanObject.getAttribute(multiplierType);

                                                        if (cleanObjectAttribute != null) {
                                                            map.put(multiplierType, cleanObjectAttribute);
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                MeterRow tableData = new MeterRow(map, meterObject);
                                meterRows.add(tableData);
                            }

                            tableView.getItems().setAll(meterRows);
                            this.succeeded();
                        } catch (Exception e) {
                            logger.error(e);
                            this.failed();
                        } finally {
                            Platform.runLater(() -> tabPane.getTabs().add(tab));
                            Platform.runLater(() -> autoFitTable(tableView));
                            this.done();
                        }
                        return tab;
                    }
                };
                JEConfig.getStatusBar().addTask(MeterPlugin.class.getName(), task, taskImage, true);
            });
        } else {
            Thread.sleep(500);
            loadTabs(allMeters, classes);
        }
    }

    private void updateList() {

        Platform.runLater(() -> tabPane.getTabs().clear());
        changeMap.clear();

        List<JEVisClass> classes = new ArrayList<>();
        Map<JEVisClass, List<JEVisObject>> allMeters = new HashMap<>();
        Task load = new Task() {
            @Override
            protected Object call() throws Exception {
                allMeters.putAll(getAllMeters());

                allMeters.forEach((key, list) -> classes.add(key));
                AlphanumComparator ac = new AlphanumComparator();
                classes.sort((o1, o2) -> {
                    try {
                        return ac.compare(I18nWS.getInstance().getClassName(o1.getName()), I18nWS.getInstance().getClassName(o2.getName()));
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                    return 1;
                });
                return null;
            }
        };

        JEConfig.getStatusBar().addTask(MeterPlugin.class.getName() + "Load", load, taskImage, true);

        Task loadTabs = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    loadTabs(allMeters, classes);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        JEConfig.getStatusBar().addTask(MeterPlugin.class.getName(), loadTabs, taskImage, true);
    }

    private Map<JEVisClass, List<JEVisObject>> getAllMeters() {
        Map<JEVisClass, List<JEVisObject>> map = new HashMap<>();
        try {
            JEVisClass meterClass = ds.getJEVisClass(MEASUREMENT_INSTRUMENT_CLASS);
            List<JEVisObject> allObjects = ds.getObjects(meterClass, true);
            for (JEVisObject object : allObjects) {
                if (!map.containsKey(object.getJEVisClass())) {
                    List<JEVisObject> objectArrayList = new ArrayList<>();
                    objectArrayList.add(object);
                    map.put(object.getJEVisClass(), objectArrayList);
                } else {
                    map.get(object.getJEVisClass()).add(object);
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("measurement_instrument.png", 20, 20);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {
        if (!initialized) {
            initialized = true;
            updateList();
        }
    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 5;
    }

}
