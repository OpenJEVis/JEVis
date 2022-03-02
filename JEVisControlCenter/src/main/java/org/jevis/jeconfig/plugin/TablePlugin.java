package org.jevis.jeconfig.plugin;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.commons.utils.JEVisDates;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.control.AnalysisLinkButton;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.EnterDataDialog;
import org.jevis.jeconfig.dialog.ImageViewerDialog;
import org.jevis.jeconfig.dialog.PDFViewerDialog;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.meters.AttributeValueChange;
import org.jevis.jeconfig.plugin.meters.JEVisClassTab;
import org.jevis.jeconfig.plugin.meters.MeterPlugin;
import org.jevis.jeconfig.plugin.meters.RegisterTableRow;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class TablePlugin implements Plugin {
    protected static final Logger logger = LogManager.getLogger(TablePlugin.class);
    private static Method columnToFitMethod;
    protected final TabPane tabPane = new TabPane();
    private Boolean multiSite;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    protected final JEVisDataSource ds;
    protected final int toolBarIconSize = 20;
    protected final int tableIconSize = 18;
    protected final Map<JEVisAttribute, AttributeValueChange> changeMap = new HashMap<>();
    protected final ObjectRelations objectRelations;
    protected final String title;
    protected final AlphanumComparator alphanumComparator = new AlphanumComparator();
    protected final JFXTextField filterInput = new JFXTextField("");
    protected final BorderPane borderPane = new BorderPane();
    protected final StackPane dialogContainer = new StackPane(borderPane);

    public TablePlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);
        this.title = title;
        this.filterInput.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));
        addListener();

        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                JEVisClassTab selectedItem = (JEVisClassTab) this.tabPane.getSelectionModel().getSelectedItem();
                String filter = filterInput.getText();
                if (selectedItem != null) {
                    if (!isMultiSite()) {
                        setFilterForTab(filter, selectedItem);
                    } else {
                        TabPane content = (TabPane) selectedItem.getContent();

                        JEVisClassTab selectedItem1 = (JEVisClassTab) content.getSelectionModel().getSelectedItem();

                        if (selectedItem1 != null) {
                            setFilterForTab(filter, selectedItem1);
                        }
                    }
                }
            }
        });
    }

    public static void autoFitTable(TableView<RegisterTableRow> tableView) {
        for (TableColumn<RegisterTableRow, ?> column : tableView.getColumns()) {
//            if (column.isVisible()) {
            try {
                if (tableView.getSkin() != null) {
                    TablePlugin.columnToFitMethod.invoke(tableView.getSkin(), column, -1);
                }
            } catch (Exception e) {
            }
//            }
        }
    }

    protected void addListener() {
        filterInput.textProperty().addListener(obs -> {
            String filter = filterInput.getText();
            JEVisClassTab selectedItem = (JEVisClassTab) tabPane.getSelectionModel().getSelectedItem();

            if (!isMultiSite())
                setFilterForTab(filter, selectedItem);
            else {
                TabPane content = (TabPane) selectedItem.getContent();

                JEVisClassTab selectedItem1 = (JEVisClassTab) content.getSelectionModel().getSelectedItem();

                if (selectedItem1 != null) {
                    setFilterForTab(filter, selectedItem1);
                }
            }
        });
    }

    private void setFilterForTab(String filter, JEVisClassTab selectedItem) {
        if (filter == null || filter.length() == 0) {
            selectedItem.getFilteredList().setPredicate(s -> true);
        } else {
            if (filter.contains(" ")) {
                String[] result = filter.split(" ");
                selectedItem.getFilteredList().setPredicate(s -> {
                    boolean match = false;
                    String string = s.getName().toLowerCase();
                    for (String value : result) {
                        String subString = value.toLowerCase();
                        if (!string.contains(subString))
                            return false;
                        else match = true;
                    }
                    return match;
                });
            } else {
                selectedItem.getFilteredList().setPredicate(s -> {
                    String string = s.getName().toLowerCase();
                    return string.contains(filter.toLowerCase());
                });
            }
        }
    }

    protected Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellDateTime() {
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

                            if (getTableRow().getIndex() % 2 == 0) {
                                pickerDate.setStyle("-fx-text-fill: white;");
                            } else {
                                pickerDate.setStyle("-fx-text-fill: black;");
                            }

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

    protected Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellTargetSelection() {
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

                            JFXButton manSampleButton = new JFXButton("", JEConfig.getImage("if_textfield_add_64870.png", tableIconSize, tableIconSize));
                            manSampleButton.setDisable(true);
                            manSampleButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.table.mansample")));
                            JFXButton treeButton = new JFXButton("",
                                    JEConfig.getImage("folders_explorer.png", tableIconSize, tableIconSize));
                            treeButton.wrapTextProperty().setValue(true);

                            JFXButton gotoButton = new JFXButton("",
                                    JEConfig.getImage("1476393792_Gnome-Go-Jump-32.png", tableIconSize, tableIconSize));//icon
                            gotoButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.attribute.target.goto.tooltip")));

                            AnalysisLinkButton analysisLinkButton = null;

                            try {
                                if (item.hasSample()) {
                                    addEventManSampleAction(item.getLatestSample(), manSampleButton, registerTableRow.getName());
                                    Platform.runLater(() -> manSampleButton.setDisable(false));
                                }

                            } catch (Exception ex) {
                                logger.error(ex);
                            }

                            try {
                                if (item.getLatestSample() != null) {
                                    TargetHelper th = new TargetHelper(getDataSource(), item.getLatestSample().getValueAsString());
                                    if (th.isValid() && th.targetAccessible() && !th.getAttribute().isEmpty()) {

                                        JEVisObject firstCleanObject = CommonMethods.getFirstCleanObject(th.getObject().get(0));
                                        if (firstCleanObject != null) {

                                            JEVisAttribute valueAttribute = firstCleanObject.getAttribute("Value");

                                            if (valueAttribute != null) {
                                                analysisLinkButton = new AnalysisLinkButton(valueAttribute);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                logger.error(ex);
                            }

                            gotoButton.setOnAction(event -> {
                                try {
                                    TargetHelper th = new TargetHelper(ds, item);
                                    if (th.isValid() && th.targetAccessible()) {
                                        JEVisObject findObj = ds.getObject(th.getObject().get(0).getID());
                                        JEConfig.openObjectInPlugin(ObjectPlugin.PLUGIN_NAME, findObj);
                                    }
                                } catch (Exception ex) {
                                    logger.error(ex);
                                }
                            });

                            treeButton.setOnAction(event -> {
                                try {
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

                                    List<UserSelection> openList = new ArrayList<>();

                                    if (th != null && !th.getObject().isEmpty()) {
                                        for (JEVisObject obj : th.getObject()) {
                                            openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
                                        }
                                    }

                                    SelectTargetDialog selectTargetDialog = new SelectTargetDialog(dialogContainer, allFilter, allDataFilter, null, SelectionMode.SINGLE, getDataSource(), openList);

                                    selectTargetDialog.setOnDialogClosed(event1 -> {
                                        try {
                                            if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
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
                                                    logger.error(ex);
                                                }

                                            }
                                            setToolTipText(treeButton, item);
                                        } catch (Exception ex) {
                                            logger.error(ex);
                                        }
                                    });
                                    selectTargetDialog.show();
                                } catch (Exception ex) {
                                    logger.error(ex);
                                }
                            });

                            HBox hBox = new HBox(treeButton, manSampleButton);
                            hBox.setAlignment(Pos.CENTER);
                            hBox.setSpacing(4);

                            hBox.getChildren().add(gotoButton);
                            gotoButton.setDisable(!setToolTipText(treeButton, item));

                            if (analysisLinkButton != null) {
                                hBox.getChildren().add(analysisLinkButton);
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

    protected void addEventManSampleAction(JEVisSample targetSample, JFXButton buttonToAddEvent, String headerText) {

        buttonToAddEvent.setOnAction(event -> {
            if (targetSample != null) {
                try {
                    TargetHelper th = new TargetHelper(getDataSource(), targetSample.getValueAsString());
                    if (th.isValid() && th.targetAccessible() && !th.getAttribute().isEmpty()) {
                        JEVisSample lastValue = th.getAttribute().get(0).getLatestSample();

                        EnterDataDialog enterDataDialog = new EnterDataDialog(dialogContainer, getDataSource());
                        enterDataDialog.setTarget(false, th.getAttribute().get(0));
                        enterDataDialog.setSample(lastValue);
                        enterDataDialog.setShowValuePrompt(true);

                        enterDataDialog.show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private boolean setToolTipText(JFXButton treeButton, JEVisAttribute att) {
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

    protected Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellString() {
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
                            textField.setStyle("-fx-text-fill: black;");

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

    protected Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellStringPassword() {
        return null;
    }

    protected Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellBoolean() {
        return null;
    }

    protected Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellFile() {
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
                            JFXButton downloadButton = new JFXButton("", JEConfig.getImage("698925-icon-92-inbox-download-48.png", tableIconSize, tableIconSize));
                            JFXButton previewButton = new JFXButton("", JEConfig.getImage("eye_visible.png", tableIconSize, tableIconSize));
                            JFXButton uploadButton = new JFXButton("", JEConfig.getImage("1429894158_698394-icon-130-cloud-upload-48.png", tableIconSize, tableIconSize));

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

    private void setPreviewButton(JFXButton button, AttributeValueChange valueChange) {

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
                    JEConfig.getStatusBar().addTask(MeterPlugin.PLUGIN_NAME, clearCacheTask, JEConfig.getImage("measurement_instrument.png"), true);


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    protected Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellDouble() {
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

                            /**
                             if (getTableRow().getIndex() % 2 == 0) {
                             textField.setStyle("-fx-text-fill: white;");
                             } else {
                             textField.setStyle("-fx-text-fill: black;");
                             }
                             **/

                            try {
                                JEVisAttribute attribute = registerTableRow.getAttributeMap().get(item.getType());
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

    protected Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCellInteger() {
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

                            /**
                             if (getTableRow().getIndex() % 2 == 0) {
                             textField.setStyle("-fx-text-fill: white;");
                             } else {
                             textField.setStyle("-fx-text-fill: black;");
                             }
                             **/

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

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public String getName() {
        return null;
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
        return null;
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
        return false;
    }

    @Override
    public Node getToolbar() {
        return null;
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

    }

    public Map<JEVisAttribute, AttributeValueChange> getChangeMap() {
        return changeMap;
    }

    @Override
    public Node getContentNode() {
        return dialogContainer;
    }

    @Override
    public ImageView getIcon() {
        return null;
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {

    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 0;
    }

    protected boolean isMultiSite(String directoryClassName) {
        if (multiSite == null) {
            boolean is = false;
            try {
                JEVisClass directoryClass = ds.getJEVisClass(directoryClassName);
                List<JEVisObject> objects = ds.getObjects(directoryClass, true);

                List<JEVisObject> buildingParents = new ArrayList<>();
                for (JEVisObject jeVisObject : objects) {
                    JEVisObject buildingParent = objectRelations.getBuildingParent(jeVisObject);
                    if (!buildingParents.contains(buildingParent)) {
                        buildingParents.add(buildingParent);

                        if (buildingParents.size() > 1) {
                            is = true;
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            multiSite = is;
        }

        return multiSite;
    }

    protected boolean isMultiSite() {
        return multiSite;
    }

    private void createExcelFile(File destinationFile) {

        XSSFWorkbook workbook = new XSSFWorkbook(); //create workbook

        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
        CellStyle cellStyleDateTime = workbook.createCellStyle();
        cellStyleDateTime.setDataFormat((short) 165);

        for (Tab tab : tabPane.getTabs()) {
            try {
                JEVisClassTab currentTab = (JEVisClassTab) tab;
                Sheet sheet = workbook.createSheet(I18nWS.getInstance().getClassName(currentTab.getJeVisClass()));
                int maxColumn = 0;

                int row = 1;
                for (RegisterTableRow registerTableRow : currentTab.getFilteredList()) {
                    String name = registerTableRow.getName();

                    Cell rowFirstColumn = getOrCreateCell(sheet, row, 0);
                    rowFirstColumn.setCellValue(name);

                    int index = currentTab.getFilteredList().indexOf(registerTableRow);
                    int col = 1;
                    for (Map.Entry<JEVisType, JEVisAttribute> entry : registerTableRow.getAttributeMap().entrySet()) {
                        JEVisType jeVisType = entry.getKey();
                        JEVisAttribute jeVisAttribute = entry.getValue();

                        if (index == 0) {
                            Cell columnHeader = getOrCreateCell(sheet, 0, 0);
                            columnHeader.setCellValue(I18nWS.getInstance().getTypeName(jeVisType));
                        }

                        Cell valueCell = getOrCreateCell(sheet, row, col);

                        if (jeVisType.getPrimitiveType() != 2) {
                            valueCell.setCellValue(jeVisAttribute.getLatestSample().getValueAsString());
                        } else {
                            valueCell.setCellValue(jeVisAttribute.getLatestSample().getValueAsDouble());

                            DataFormat format = workbook.createDataFormat();
                            CellStyle cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(format.getFormat("#,##0.00 [$" + jeVisAttribute.getInputUnit() + "]"));
                            valueCell.setCellStyle(cellStyle);
                        }

                        col++;
                    }
                    maxColumn = Math.max(maxColumn, col);
                }

                IntStream.rangeClosed(0, maxColumn).forEach(sheet::autoSizeColumn);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
            workbook.write(fileOutputStream);
            workbook.close();
            fileOutputStream.close();
        } catch (IOException e) {
            logger.error("Could not save file {}", destinationFile, e);
        }
    }

    private org.apache.poi.ss.usermodel.Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx) {
        return getOrCreateCell(sheet, rowIdx, colIdx, 1, 1);
    }

    private org.apache.poi.ss.usermodel.Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx, int rowSpan, int colSpan) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }

        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }

        if (rowSpan > 1 || colSpan > 1) {
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + rowSpan - 1, colIdx, colIdx + colSpan - 1));
        }

        return cell;
    }
}
