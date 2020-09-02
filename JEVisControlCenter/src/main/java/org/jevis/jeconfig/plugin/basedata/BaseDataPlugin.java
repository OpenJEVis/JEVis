package org.jevis.jeconfig.plugin.basedata;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.unit.UnitManager;
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
import org.jevis.jeconfig.dialog.EnterDataDialog;
import org.jevis.jeconfig.dialog.ImageViewerDialog;
import org.jevis.jeconfig.dialog.PDFViewerDialog;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.equipment.EquipmentPlugin;
import org.jevis.jeconfig.plugin.meters.AttributeValueChange;
import org.jevis.jeconfig.plugin.meters.RegisterTableRow;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;
import org.jevis.jeconfig.plugin.unit.UnitSelectUI;
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

public class BaseDataPlugin implements Plugin {
    public static final String BASE_DATA_CLASS = "Base Data";
    private static final Logger logger = LogManager.getLogger(EquipmentPlugin.class);
    private static final double EDITOR_MAX_HEIGHT = 50;
    public static String PLUGIN_NAME = "Base Data Plugin";
    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private final int toolBarIconSize = 20;
    private final int tableIconSize = 18;
    private final Image taskImage = JEConfig.getImage("building_equipment.png");
    private final ObjectRelations objectRelations;
    private final JEVisDataSource ds;
    private final String title;
    private final BorderPane borderPane = new BorderPane();
    private final ToolBar toolBar = new ToolBar();
    Map<JEVisAttribute, AttributeValueChange> changeMap = new HashMap<>();
    private boolean initialized = false;
    private JEVisClass baseDataClass;

    public BaseDataPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;
        this.objectRelations = new ObjectRelations(ds);

        initToolBar();

    }

    public static void autoFitTable(TableView<RegisterTableRow> tableView) {
        for (TableColumn<RegisterTableRow, ?> column : tableView.getColumns()) {
//            if (column.isVisible()) {
            try {
                if (tableView.getSkin() != null) {
                    columnToFitMethod.invoke(tableView.getSkin(), column, -1);
                }
            } catch (Exception e) {
            }
//            }
        }
    }

    private void createColumns(TableView<RegisterTableRow> tableView) {

        try {
            TableColumn<RegisterTableRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.meters.table.measurementpoint.columnname"));
            nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getName()));
            nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
            nameColumn.setSortable(true);
            nameColumn.setSortType(TableColumn.SortType.ASCENDING);

            tableView.getColumns().add(nameColumn);
            tableView.getSortOrder().addAll(nameColumn);


            for (JEVisType type : baseDataClass.getTypes()) {
                TableColumn<RegisterTableRow, JEVisAttribute> column = new TableColumn<>(I18nWS.getInstance().getTypeName(baseDataClass.getName(), type.getName()));
                column.setStyle("-fx-alignment: CENTER;");
                column.setSortable(false);

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
                        column.setCellFactory(valueCellDouble(type.getGUIDisplayType()));
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

            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    private boolean isMultiSite() {

        try {
            JEVisClass baseDataClass = ds.getJEVisClass(BASE_DATA_CLASS);
            List<JEVisObject> objects = ds.getObjects(baseDataClass, true);

            List<JEVisObject> buildingParents = new ArrayList<>();
            for (JEVisObject jeVisObject : objects) {
                JEVisObject buildingParent = objectRelations.getBuildingParent(jeVisObject);
                if (!buildingParents.contains(buildingParent)) {
                    buildingParents.add(buildingParent);

                    if (buildingParents.size() > 1) {
                        return true;
                    }
                }
            }

        } catch (Exception e) {

        }

        return false;
    }

    private Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellDateTime() {
        return new Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>>() {
            @Override
            public TableCell<RegisterTableRow, JEVisAttribute> call(TableColumn<RegisterTableRow, JEVisAttribute> param) {
                return new TableCell<RegisterTableRow, JEVisAttribute>() {
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

                                    if (item.getName().equals("Verification Date")) {
                                        if (date.isBefore(new DateTime())) {
                                            pickerDate.setDefaultColor(Color.RED);
                                        }
                                    }
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

    private Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellTargetSelection() {
        return new Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>>() {
            @Override
            public TableCell<RegisterTableRow, JEVisAttribute> call(TableColumn<RegisterTableRow, JEVisAttribute> param) {
                return new TableCell<RegisterTableRow, JEVisAttribute>() {
                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            RegisterTableRow registerTableRow = (RegisterTableRow) getTableRow().getItem();

                            Button manSampleButton = new Button("", JEConfig.getImage("if_textfield_add_64870.png", tableIconSize, tableIconSize));
                            manSampleButton.setDisable(true);
                            manSampleButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.table.mansample")));
                            Button treeButton = new Button("",
                                    JEConfig.getImage("folders_explorer.png", tableIconSize, tableIconSize));
                            treeButton.wrapTextProperty().setValue(true);

                            Button gotoButton = new Button("",
                                    JEConfig.getImage("1476393792_Gnome-Go-Jump-32.png", tableIconSize, tableIconSize));//icon
                            gotoButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.attribute.target.goto.tooltip")));

                            try {
                                if (item.hasSample()) {
                                    addEventManSampleAction(item.getLatestSample(), manSampleButton, registerTableRow.getName());
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

                                    selectTargetDialog = new SelectTargetDialog(allFilter, allDataFilter, null, SelectionMode.MULTIPLE);
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
                                            addEventManSampleAction(newTargetSample, manSampleButton, registerTableRow.getName());
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

                            hBox.getChildren().add(gotoButton);
                            gotoButton.setDisable(!setToolTipText(treeButton, item));

                            VBox vBox = new VBox(hBox);
                            vBox.setAlignment(Pos.CENTER);
                            setGraphic(vBox);
                        }

                    }
                };
            }

        };
    }

    private void addEventManSampleAction(JEVisSample targetSample, Button buttonToAddEvent, String headerText) {
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
            enterDataDialog.showPopup(buttonToAddEvent, headerText);
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

    private Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellString() {
        return new Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>>() {
            @Override
            public TableCell<RegisterTableRow, JEVisAttribute> call(TableColumn<RegisterTableRow, JEVisAttribute> param) {
                return new TableCell<RegisterTableRow, JEVisAttribute>() {
                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            RegisterTableRow registerTableRow = (RegisterTableRow) getTableRow().getItem();

                            JFXTextField textField = new JFXTextField();
                            try {
                                JEVisAttribute attribute = registerTableRow.getAttributeMap().get(item.getType());
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

    private Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellStringPassword() {
        return null;
    }

    private Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellBoolean() {
        return null;
    }

    private Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellFile() {
        return new Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>>() {

            @Override
            public TableCell<RegisterTableRow, JEVisAttribute> call(TableColumn<RegisterTableRow, JEVisAttribute> param) {
                return new TableCell<RegisterTableRow, JEVisAttribute>() {

                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Button downloadButton = new Button("", JEConfig.getImage("698925-icon-92-inbox-download-48.png", tableIconSize, tableIconSize));
                            Button previewButton = new Button("", JEConfig.getImage("export-image.png", tableIconSize, tableIconSize));
                            Button uploadButton = new Button("", JEConfig.getImage("1429894158_698394-icon-130-cloud-upload-48.png", tableIconSize, tableIconSize));

                            downloadButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.table.download")));
                            previewButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.table.preview")));
                            uploadButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.table.upload")));

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

                            previewButton.setDisable(true);
                            try {
                                downloadButton.setDisable(!item.hasSample());
                                if (item.hasSample()) {
                                    setPreviewButton(previewButton, valueChange);
                                    previewButton.setDisable(false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            HBox hBox = new HBox();
                            hBox.setAlignment(Pos.CENTER);
                            hBox.setSpacing(4);

                            downloadButton.setOnAction(event -> {
                                try {
                                    if (item.hasSample()) {
                                        JEVisFile file = item.getLatestSample().getValueAsFile();
                                        if (file != null) {
                                            FileChooser fileChooser = new FileChooser();
                                            fileChooser.setInitialFileName(file.getFilename());
                                            fileChooser.setTitle(I18n.getInstance().getString("plugin.object.attribute.file.download.title"));
                                            fileChooser.getExtensionFilters().addAll(
                                                    new FileChooser.ExtensionFilter("All Files", "*.*"));
                                            File selectedFile = fileChooser.showSaveDialog(null);
                                            if (selectedFile != null) {
                                                JEConfig.setLastPath(selectedFile);
                                                file.saveToFile(selectedFile);
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    logger.error(ex);
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

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Task clearCacheTask = new Task() {
                        @Override
                        protected Object call() throws Exception {
                            try {
                                this.updateTitle(I18n.getInstance().getString("plugin.meters.download"));
                                boolean isPDF = false;
                                JEVisFile file = valueChange.getAttribute().getLatestSample().getValueAsFile();
                                String fileName = file.getFilename();

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
                                    Platform.runLater(() -> {
                                        PDFViewerDialog pdfViewerDialog = new PDFViewerDialog();
                                        pdfViewerDialog.show(valueChange.getAttribute(), file, JEConfig.getStage());
                                    });

                                } else {
                                    Platform.runLater(() -> {
                                        ImageViewerDialog imageViewerDialog = new ImageViewerDialog();
                                        imageViewerDialog.show(valueChange.getAttribute(), file, JEConfig.getStage());
                                    });

                                }
                            } catch (Exception ex) {
                                failed();
                            } finally {
                                done();
                            }
                            return null;
                        }
                    };
                    JEConfig.getStatusBar().addTask(PLUGIN_NAME, clearCacheTask, JEConfig.getImage("measurement_instrument.png"), true);


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellDouble(String guiDisplayType) {
        return new Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>>() {
            @Override
            public TableCell<RegisterTableRow, JEVisAttribute> call(TableColumn<RegisterTableRow, JEVisAttribute> param) {
                return new TableCell<RegisterTableRow, JEVisAttribute>() {

                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            RegisterTableRow registerTableRow = (RegisterTableRow) getTableRow().getItem();

                            JFXTextField textField = new JFXTextField();
                            JFXTextField unitValue = new JFXTextField();
                            try {
                                JEVisAttribute attribute = registerTableRow.getAttributeMap().get(item.getType());
                                if (attribute != null && attribute.hasSample()) {
                                    textField.setText(attribute.getLatestSample().getValueAsDouble().toString());

                                    if (attribute.getDisplayUnit() != null && !attribute.getInputUnit().getLabel().isEmpty()) {
                                        unitValue.setText(UnitManager.getInstance().format(attribute.getDisplayUnit().getLabel()));
                                    } else {
                                        unitValue.setText(UnitManager.getInstance().format(attribute.getInputUnit().getLabel()));
                                    }
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

                            if (guiDisplayType.equals(GUIConstants.NUMBER_WITH_UNIT.getId())) {
                                unitValue.setOnMouseClicked(event -> {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.setUnitAndPeriodRecursive.title"));
                                    alert.setHeaderText(null);
                                    alert.setResizable(true);

                                    final Label l_prefixL = new Label(I18n.getInstance().getString("attribute.editor.unit.prefix"));
                                    final Label l_unitL = new Label(I18n.getInstance().getString("attribute.editor.unit.unit"));
                                    final Label l_example = new Label(I18n.getInstance().getString("attribute.editor.unit.symbol"));

                                    GridPane gp = new GridPane();
                                    gp.setHgap(4);
                                    gp.setVgap(6);

                                    gp.add(l_prefixL, 0, 0);
                                    gp.add(l_unitL, 0, 1);
                                    gp.add(l_example, 0, 2);


                                    try {
                                        final UnitSelectUI unitUI = new UnitSelectUI(ds, item.getInputUnit());

                                        unitUI.getPrefixBox().setPrefWidth(95);
                                        unitUI.getUnitButton().setPrefWidth(95);
                                        unitUI.getSymbolField().setPrefWidth(95);

                                        gp.add(unitUI.getPrefixBox(), 1, 0);
                                        gp.add(unitUI.getUnitButton(), 1, 1);
                                        gp.add(unitUI.getSymbolField(), 1, 2);

                                        alert.getDialogPane().setContent(gp);

                                        alert.showAndWait().ifPresent(buttonType -> {
                                            if (buttonType.equals(ButtonType.OK)) {
                                                try {
                                                    item.setDisplayUnit(unitUI.getUnit());
                                                    item.setInputUnit(unitUI.getUnit());
                                                    item.commit();

                                                    if (item.getDisplayUnit() != null && !item.getInputUnit().getLabel().isEmpty()) {
                                                        Platform.runLater(() -> {
                                                            try {
                                                                unitValue.setText(UnitManager.getInstance().format(item.getDisplayUnit().getLabel()));
                                                            } catch (JEVisException e) {
                                                                e.printStackTrace();
                                                            }
                                                        });
                                                    } else {
                                                        Platform.runLater(() -> {
                                                            try {
                                                                unitValue.setText(UnitManager.getInstance().format(item.getInputUnit().getLabel()));
                                                            } catch (JEVisException e) {
                                                                e.printStackTrace();
                                                            }
                                                        });
                                                    }
                                                } catch (JEVisException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    } catch (JEVisException e) {
                                        e.printStackTrace();
                                    }
                                });

                                textField.setAlignment(Pos.CENTER_RIGHT);
                                unitValue.setAlignment(Pos.CENTER_LEFT);
                                unitValue.setPrefWidth(40);
                                unitValue.setEditable(false);

                                HBox hBox = new HBox(2, textField, unitValue);
                                VBox vBox = new VBox(hBox);
                                vBox.setAlignment(Pos.CENTER);
                                setGraphic(vBox);
                            } else {
                                VBox vBox = new VBox(textField);
                                vBox.setAlignment(Pos.CENTER);
                                setGraphic(vBox);
                            }
                        }

                    }
                };
            }

        };
    }

    private Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellInteger() {
        return new Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>>() {
            @Override
            public TableCell<RegisterTableRow, JEVisAttribute> call(TableColumn<RegisterTableRow, JEVisAttribute> param) {
                return new TableCell<RegisterTableRow, JEVisAttribute>() {
                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            RegisterTableRow registerTableRow = (RegisterTableRow) getTableRow().getItem();

                            JFXTextField textField = new JFXTextField();
                            try {
                                JEVisAttribute attribute = registerTableRow.getAttributeMap().get(item.getType());
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


        toolBar.getItems().setAll(reload);
    }

    @Override
    public String getClassName() {
        return "Base Data Plugin";
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
        return I18n.getInstance().getString("plugin.basedata.tooltip");
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
                break;
            case Constants.Plugin.Command.DELETE:
                break;
            case Constants.Plugin.Command.EXPAND:
                break;
            case Constants.Plugin.Command.NEW:
                break;
            case Constants.Plugin.Command.RELOAD:

                Task clearCacheTask = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            this.updateTitle(I18n.getInstance().getString("Clear Cache"));
                            if (initialized) {
                                ds.clearCache();
                                ds.preload();
                            } else {
                                initialized = true;
                            }
                            updateList();
                            succeeded();
                        } catch (Exception ex) {
                            failed();
                        } finally {
                            done();
                        }
                        return null;
                    }
                };
                JEConfig.getStatusBar().addTask(PLUGIN_NAME, clearCacheTask, JEConfig.getImage("building_equipment.png"), true);

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

    private void loadData(List<JEVisObject> allBaseData) throws InterruptedException {
        AtomicBoolean hasActiveLoadTask = new AtomicBoolean(false);
        for (Map.Entry<Task, String> entry : JEConfig.getStatusBar().getTaskList().entrySet()) {
            Task task = entry.getKey();
            String s = entry.getValue();
            if (s.equals(EquipmentPlugin.class.getName() + "Load")) {
                hasActiveLoadTask.set(true);
                break;
            }
        }
        if (!hasActiveLoadTask.get()) {
            Task<TableView> task = new Task<TableView>() {
                @Override
                protected TableView call() {
                    TableView<RegisterTableRow> tableView = new TableView<>();

                    try {
                        AlphanumComparator ac = new AlphanumComparator();

                        tableView.setFixedCellSize(EDITOR_MAX_HEIGHT);
                        tableView.setSortPolicy(param -> {
                            Comparator<RegisterTableRow> comparator = (t1, t2) -> ac.compare(t1.getObject().getName(), t2.getObject().getName());
                            FXCollections.sort(tableView.getItems(), comparator);
                            return true;
                        });
                        tableView.setTableMenuButtonVisible(true);


                        createColumns(tableView);

                        List<RegisterTableRow> registerTableRows = new ArrayList<>();

                        JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");
                        JEVisType multiplierType = cleanDataClass.getType("Value Multiplier");

                        for (JEVisObject meterObject : allBaseData) {
                            Map<JEVisType, JEVisAttribute> map = new HashMap<>();

                            for (JEVisAttribute meterObjectAttribute : meterObject.getAttributes()) {
                                JEVisType type = null;

                                try {
                                    type = meterObjectAttribute.getType();

                                    map.put(type, meterObjectAttribute);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            RegisterTableRow tableData = new RegisterTableRow(map, meterObject, isMultiSite());
                            registerTableRows.add(tableData);
                        }

                        tableView.getItems().setAll(registerTableRows);
                        this.succeeded();
                    } catch (Exception e) {
                        logger.error(e);
                        this.failed();
                    } finally {
                        Platform.runLater(() -> borderPane.setCenter(tableView));
                        Platform.runLater(() -> autoFitTable(tableView));
                        Platform.runLater(tableView::sort);
                        this.done();
                    }
                    return tableView;
                }
            };
            JEConfig.getStatusBar().addTask(EquipmentPlugin.class.getName(), task, taskImage, true);

        } else {
            Thread.sleep(500);
            loadData(allBaseData);
        }
    }

    private void updateList() {

        changeMap.clear();

        List<JEVisObject> allBaseData = getAllBaseData();
        AlphanumComparator ac = new AlphanumComparator();
        allBaseData.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));

        Task loadData = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    loadData(allBaseData);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        JEConfig.getStatusBar().addTask(EquipmentPlugin.class.getName(), loadData, taskImage, true);
    }

    private List<JEVisObject> getAllBaseData() {
        List<JEVisObject> list = new ArrayList<>();
        try {
            baseDataClass = ds.getJEVisClass(BASE_DATA_CLASS);
            list.addAll(ds.getObjects(baseDataClass, true));
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("base_data.png", 20, 20);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {

        Task loadTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    this.updateTitle(I18n.getInstance().getString("plugin.meters.load"));
                    if (!initialized) {
                        initialized = true;
                        updateList();
                    }
                    succeeded();
                } catch (Exception ex) {
                    failed();
                } finally {
                    done();
                }
                return null;
            }
        };
        JEConfig.getStatusBar().addTask(PLUGIN_NAME, loadTask, JEConfig.getImage("base_data.png"), true);

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 7;
    }

}
