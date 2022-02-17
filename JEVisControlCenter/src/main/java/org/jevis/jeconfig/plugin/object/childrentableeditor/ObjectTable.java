package org.jevis.jeconfig.plugin.object.childrentableeditor;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.jevistree.methods.CommonMethods;
import org.jevis.jeconfig.plugin.charts.TableViewContextMenuHelper;
import org.jevis.jeconfig.sample.SampleEditor;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.jevis.commons.utils.CommonMethods.getChildrenRecursive;

public class ObjectTable {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    protected final String STANDARD_TARGET_ATTRIBUTE_NAME = "Target";
    protected final String VALUE_ATTRIBUTE_NAME = "Value";
    public static final String PATTERN = DATE_FORMAT;
    private final TableView<TableData> tableView = new TableView<TableData>();

    private static final Logger logger = LogManager.getLogger(ObjectTable.class);
    private final Map<Long, List<Long>> targetLoytecXML = new HashMap<>();
    private final Map<Long, List<Long>> targetOPCUA = new HashMap<>();
    private DateTime start;
    private DateTime end;
    private final Map<Long, List<Long>> targetVIDA = new HashMap<>();
    private final Map<Long, List<Long>> targetCSV = new HashMap<>();
    private final Map<Long, List<Long>> targetXML = new HashMap<>();
    private final Map<Long, List<Long>> targetDWD = new HashMap<>();
    private final Map<Long, List<Long>> targetDataPoint = new HashMap<>();
    private final FilteredList<TableData> filteredData;
    private Map<Long, List<Long>> calcMap = new HashMap<>();
    private JEVisDataSource ds;

    public ObjectTable(JEVisObject parentObject, JFXDatePicker startDatePicker, JFXDatePicker endDatePicker, ToggleButton reloadButton, ToggleButton xlsxButton, JFXTextField filterInclude, JFXTextField filterExclude, JFXComboBox<String> columnBox, JFXCheckBox sourceDetails) {

        reloadButton.setOnAction(event -> tableView.refresh());
        LocalDate startLocalDate = startDatePicker.getValue();
        LocalDate endLocalDate = endDatePicker.getValue();
        start = new DateTime(startLocalDate.getYear(), startLocalDate.getMonthValue(), startLocalDate.getDayOfMonth(), 0, 0, 0, 0);
        end = new DateTime(endLocalDate.getYear(), endLocalDate.getMonthValue(), endLocalDate.getDayOfMonth(), 23, 59, 59, 999);
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> start = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0, 0, 0));
        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> end = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 23, 59, 59, 999));
        ObservableList<TableData> tableData = FXCollections.observableArrayList();

        tableView.setTableMenuButtonVisible(true);
        TableViewContextMenuHelper contextMenuHelper = new TableViewContextMenuHelper(tableView);

        xlsxButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("XLSX File Destination");
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", ".xlsx");
            fileChooser.getExtensionFilters().addAll(pdfFilter);
            fileChooser.setSelectedExtensionFilter(pdfFilter);

            try {
                JEVisFile xlsxFile = CommonMethods.createXLSXFile(parentObject.getName(), tableView);
                fileChooser.setInitialFileName(xlsxFile.getFilename());
                File selectedFile = fileChooser.showSaveDialog(JEConfig.getStage());
                if (selectedFile != null) {
                    JEConfig.setLastPath(selectedFile);
                    try {
                        xlsxFile.saveToFile(selectedFile);
                    } catch (IOException e) {
                        logger.error("Could not save xlsx file", e);
                    }
                }
            } catch (Exception e) {
                logger.error("Could not create xlsx file", e);
            }
        });

        tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    @SuppressWarnings("rawtypes")
                    TablePosition pos = tableView.getSelectionModel().getSelectedCells().get(0);
                    int row = pos.getRow();
                    int col = pos.getColumn();
                    @SuppressWarnings("rawtypes")
                    TableColumn column = pos.getTableColumn();
                    TableData tableData = tableView.getSelectionModel().getSelectedItem();
                    String attributeName = column.getId();
                    try {
                        JEVisAttribute attribute = tableData.getObject().getAttribute(attributeName);
                        SampleEditor se = new SampleEditor();
                        se.show(JEConfig.getStage(), attribute, start, end);
                    } catch (Exception e) {
                        logger.error("Could not open sample editor for row:col {}:{}, object {}:{} and attribute {}", row, col, tableData.getObject().getName(), attributeName, e);
                    }
                } else if (click.getButton() == MouseButton.SECONDARY) {
                    contextMenuHelper.showContextMenu();
                }
            }
        });

        try {
            List<JEVisObject> children = parentObject.getChildren();
            List<JEVisAttribute> attributes = new ArrayList<>();
//            List<TableColumn<String, JEVisAttribute>> columns = new ArrayList<>();

            TableColumn<TableData, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.object.attribute.overview.name"));
            nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getObject().getName()));

            TableColumn<TableData, String> classColumn = new TableColumn<>(I18n.getInstance().getString("plugin.dtrc.dialog.classlabel"));
            classColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getClassString()));

            ds = parentObject.getDataSource();
            calcMap = getCalcMap(ds);
            getDataServerTargetMaps();

            TableColumn<TableData, String> sourceColumn = new TableColumn<>(I18n.getInstance().getString("jevis.types.source"));
            tableView.setRowFactory(tv -> new TableRow<TableData>() {
                @Override
                protected void updateItem(TableData item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || item.getObject() == null)
                        setStyle("");
                    else if (item.isDuplicate())
                        setStyle("-fx-background-color: orange;");
                    else
                        setStyle("");
                }
            });

            sourceColumn.setCellValueFactory(param -> {
                if (!sourceDetails.isSelected()) return new ReadOnlyObjectWrapper<>(param.getValue().getSourceString());
                else return new ReadOnlyObjectWrapper<>(param.getValue().getSourceDetailed());
            });

            sourceDetails.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    tableView.refresh();
                }
            });

            TableColumn<TableData, String> minTSColumn = buildMinMaxTSColumn(false);
            TableColumn<TableData, String> maxTSColumn = buildMinMaxTSColumn(true);

            this.tableView.getColumns().addAll(nameColumn, classColumn, sourceColumn, minTSColumn, maxTSColumn);

            addChildren(tableData, attributes, parentObject);

//            for (JEVisObject child : children) {
//                for (JEVisAttribute attribute : child.getAttributes()) {
//                    addAttributeSave(attributes, attribute);
//                }
//                tableData.add(new TableData(child));
//            }

            for (JEVisAttribute attribute : attributes) {
                if (attribute.hasSample() && attribute.getName().equals("Value")) {
                    if (attribute.getTimestampFromLastSample().isBefore(start)) {
                        end = attribute.getTimestampFromLastSample();
                        start = end.minusDays(1);
                    }
                }
            }
            Platform.runLater(() -> {
                startDatePicker.setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
                endDatePicker.setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
            });

            for (JEVisAttribute attribute : attributes) {
                String attributeName = attribute.getName();
                try {
                    attributeName = I18nWS.getInstance().getAttributeName(attribute);
                } catch (Exception e) {
                    logger.error("Could not get name for {} attribute", attribute.getName(), e);
                }
                TableColumn<TableData, String> column = new TableColumn<>(attributeName);
                column.setId(attribute.getName());

                column.setCellValueFactory(param -> {
                    for (JEVisAttribute att : param.getValue().getAttributeList()) {
                        if (att.getName().equals(column.getId())) {
                            try {
                                if (att.hasSample() && att.getName().equals("Value")) {
                                    List<JEVisSample> samples = att.getSamples(start, end);

                                    String resultString = "";
                                    if (!samples.isEmpty()) {
                                        JEVisSample sample = samples.get(samples.size() - 1);
                                        resultString += sample.getValueAsString() + "@" + sample.getTimestamp().toString(PATTERN)
                                                + " (" + I18n.getInstance().getString("plugin.object.attribute.overview.totalsamplecount") + ": " + samples.size() + ")";
                                    } else {
                                        resultString += "(" + I18n.getInstance().getString("plugin.object.attribute.overview.totalsamplecount") + ": 0)";
                                    }

                                    return new ReadOnlyObjectWrapper<>(resultString);
                                } else if (att.hasSample()) {
                                    JEVisSample latestSample = att.getLatestSample();
                                    return new ReadOnlyObjectWrapper<>(latestSample.getValueAsString());
                                }
                            } catch (Exception ex) {
                                logger.error(ex);
                            }
                        }
                    }

                    return new ReadOnlyObjectWrapper<>("");
                });


                this.tableView.getColumns().add(column);
            }

            /**
             * Filter menu
             */
            CheckComboBox<String> checkComboBox = new CheckComboBox();
            final ObservableList<String> objectClasses = FXCollections.observableArrayList();
            tableData.forEach(tableData1 -> {
                try {
                    if (!objectClasses.contains(tableData1.getObject().getJEVisClassName())) {
                        objectClasses.add(tableData1.getObject().getJEVisClassName());
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });

            ContextMenu menu = new ContextMenu();
            MenuItem selectAll = new MenuItem();
            selectAll.setGraphic(checkComboBox);


            menu.getItems().addAll(selectAll);
            classColumn.setContextMenu(menu);
        } catch (
                Exception ex) {
            logger.error(ex);
        }

        filteredData = new FilteredList<>(tableData, s -> true);
        this.tableView.setItems(filteredData);

        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            String in = filterInclude.getText();
            String ex = filterExclude.getText();
            int column = columnBox.getSelectionModel().getSelectedIndex();

            if (in.contains(" ") || ex.contains(" ")) {
                List<String> resultIn = new ArrayList<>();
                if (in.contains(" ")) {
                    Collections.addAll(resultIn, in.split(" "));
                }

                List<String> resultEx = new ArrayList<>();
                if (ex.contains(" ")) {
                    Collections.addAll(resultEx, ex.split(" "));
                }

                filteredData.setPredicate(s -> {
                    boolean match = false;
                    String string = "";
                    if (column == 0) {
                        string = s.getObject().getName().toLowerCase();
                    } else if (column == 1) {
                        string = s.getClassString().toLowerCase();
                    } else {
                        string = s.getSourceString().toLowerCase();
                    }
                    for (String valueIn : resultIn) {
                        String subString = valueIn.toLowerCase();
                        if (!string.contains(subString)) {
                            return false;
                        } else {
                            match = true;
                        }
                    }
                    for (String valueEx : resultEx) {
                        String subString = valueEx.toLowerCase();
                        if (string.contains(subString)) {
                            return false;
                        } else {
                            match = true;
                        }
                    }

                    return match;
                });
            } else if (in.length() > 0 || ex.length() > 0) {
                filteredData.setPredicate(s -> {
                    String string = "";
                    if (column == 0) {
                        string = s.getObject().getName().toLowerCase();
                    } else if (column == 1) {
                        string = s.getClassString().toLowerCase();
                    } else {
                        string = s.getSourceString().toLowerCase();
                    }
                    if (in.length() > 0 && ex.length() > 0) {
                        return string.contains(in.toLowerCase()) && !string.contains(ex.toLowerCase());
                    } else if (in.length() > 0) {
                        return string.contains(in.toLowerCase());
                    } else return !string.contains(ex.toLowerCase());
                });
            } else filteredData.setPredicate(s -> true);
        };
        filterInclude.textProperty().addListener(changeListener);
        filterExclude.textProperty().addListener(changeListener);

    }

    private void addChildren
            (ObservableList<TableData> tableData, List<JEVisAttribute> attributes, JEVisObject parent) {
        try {
            for (JEVisObject child : parent.getChildren()) {
                for (JEVisAttribute attribute : child.getAttributes()) {
                    addAttributeSave(attributes, attribute);
                }
                tableData.add(new TableData(child, calcMap, targetLoytecXML, targetOPCUA, targetVIDA, targetCSV, targetXML, targetDWD, targetDataPoint));
                addChildren(tableData, attributes, child);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public TableView getTableView() {
        return this.tableView;
    }

    /**
     * Add an new attribute to the list id it does not allready exists.
     * TODO: for now we only check by name but we also need to check of its the same class or inherited.
     *
     * @param attributes
     * @param addAttribute
     */
    public static void addAttributeSave(List<JEVisAttribute> attributes, JEVisAttribute addAttribute) {
        boolean contains = false;
        for (JEVisAttribute attribute : attributes) {
            if (attribute.getName().equals(addAttribute.getName())) {
                contains = true;
            }
        }
        if (!contains) {
            attributes.add(addAttribute);
        }
    }

    public Map<Long, List<Long>> getCalcMap(JEVisDataSource ds) throws JEVisException {

        Map<Long, List<Long>> targetAndCalculation = new HashMap<>();

        JEVisClass calculation = ds.getJEVisClass("Calculation");
        JEVisClass outputClass = ds.getJEVisClass("Output");

        for (JEVisObject calculationObj : ds.getObjects(calculation, true)) {
            try {
                List<JEVisObject> outputs = calculationObj.getChildren(outputClass, true);

                if (outputs != null && !outputs.isEmpty()) {
                    for (JEVisObject output : outputs) {
                        JEVisAttribute targetAttribute = output.getAttribute("Output");
                        if (targetAttribute != null) {
                            try {
                                TargetHelper th = new TargetHelper(ds, targetAttribute);
                                if (th.getObject() != null && !th.getObject().isEmpty()) {
                                    Long id = th.getObject().get(0).getID();
                                    if (targetAndCalculation.get(id) == null) {
                                        List<Long> objects = new ArrayList<>();
                                        objects.add(calculationObj.getID());
                                        targetAndCalculation.put(id, objects);
                                    } else {
                                        List<Long> list = new ArrayList<>(targetAndCalculation.remove(id));
                                        list.add(calculationObj.getID());
                                        targetAndCalculation.put(id, list);
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return targetAndCalculation;

    }


    public void getDataServerTargetMaps() {
        try {
            JEVisClass channelClass = ds.getJEVisClass("Channel");
            JEVisClass loytecChannel = ds.getJEVisClass("Loytec XML-DL Channel");
            JEVisClass loytecOPCUAChannel = ds.getJEVisClass("OPC UA Channel");
            JEVisClass vida350Channel = ds.getJEVisClass("VIDA350 Channel");
            JEVisClass ftpChannelClass = ds.getJEVisClass("FTP Channel");
            JEVisClass httpChannelClass = ds.getJEVisClass("HTTP Channel");
            JEVisClass sFtpChannelClass = ds.getJEVisClass("sFTP Channel");
            JEVisClass soapChannelClass = ds.getJEVisClass("SOAP Channel");
            JEVisClass csvDataPointClass = ds.getJEVisClass("CSV Data Point");
            JEVisClass dwdDataPointClass = ds.getJEVisClass("DWD Data Point");
            JEVisClass xmlDataPointClass = ds.getJEVisClass("XML Data Point");
            JEVisClass dataPointClass = ds.getJEVisClass("Data Point");

            List<JEVisObject> objects = ds.getObjects(channelClass, true);

            objects.forEach(object -> {
                try {
                    if (object.getJEVisClass().equals(loytecChannel) || object.getJEVisClass().equals(loytecOPCUAChannel) || object.getJEVisClass().equals(vida350Channel)) {
                        String attributeName = "";
                        if (object.getJEVisClass().equals(loytecChannel) || object.getJEVisClass().equals(loytecOPCUAChannel)) {
                            attributeName = "Target ID";
                        } else if (object.getJEVisClass().equals(vida350Channel)) {
                            attributeName = "Target";
                        }

                        if (object.getAttribute(attributeName).hasSample()) {
                            TargetHelper th = new TargetHelper(ds, object.getAttribute(attributeName));
                            if (th.getObject() != null && !th.getObject().isEmpty()) {
                                Long id = th.getObject().get(0).getID();
                                if (object.getJEVisClass().equals(loytecChannel)) {
                                    if (targetLoytecXML.get(id) == null) {
                                        List<Long> list = new ArrayList<>();
                                        list.add(object.getID());
                                        targetLoytecXML.put(id, list);
                                    } else {
                                        List<Long> list = new ArrayList<>(targetLoytecXML.remove(id));
                                        list.add(object.getID());
                                        targetLoytecXML.put(id, list);
                                    }
                                } else if (object.getJEVisClass().equals(loytecOPCUAChannel)) {
                                    if (targetOPCUA.get(id) == null) {
                                        List<Long> list = new ArrayList<>();
                                        list.add(object.getID());
                                        targetOPCUA.put(id, list);
                                    } else {
                                        List<Long> list = new ArrayList<>(targetOPCUA.remove(id));
                                        list.add(object.getID());
                                        targetOPCUA.put(id, list);
                                    }
                                } else if (object.getJEVisClass().equals(vida350Channel)) {
                                    if (targetVIDA.get(id) == null) {
                                        List<Long> list = new ArrayList<>();
                                        list.add(object.getID());
                                        targetVIDA.put(id, list);
                                    } else {
                                        List<Long> list = new ArrayList<>(targetVIDA.remove(id));
                                        list.add(object.getID());
                                        targetVIDA.put(id, list);
                                    }
                                }
                            }
                        }
                    } else {
                        List<JEVisObject> dps = new ArrayList<>();

                        if (object.getJEVisClass().equals(ftpChannelClass)) {
                            dps.addAll(getChildrenRecursive(object, csvDataPointClass));
                            dps.addAll(getChildrenRecursive(object, dwdDataPointClass));
                            dps.addAll(getChildrenRecursive(object, xmlDataPointClass));
                            dps.addAll(getChildrenRecursive(object, dataPointClass));
                        } else {
                            dps.addAll(getChildrenRecursive(object, csvDataPointClass));
                            dps.addAll(getChildrenRecursive(object, xmlDataPointClass));
                            dps.addAll(getChildrenRecursive(object, dataPointClass));
                        }

                        for (JEVisObject dp : dps) {

                            if (dp.getJEVisClass().equals(csvDataPointClass) || dp.getJEVisClass().equals(xmlDataPointClass)) {
                                JEVisAttribute targetAtt = null;
                                JEVisSample lastSampleTarget = null;

                                targetAtt = dp.getAttribute(STANDARD_TARGET_ATTRIBUTE_NAME);

                                if (targetAtt != null) lastSampleTarget = targetAtt.getLatestSample();

                                TargetHelper th = null;
                                if (lastSampleTarget != null) {
                                    th = new TargetHelper(ds, lastSampleTarget.getValueAsString());
                                    if (th.getObject() != null && !th.getObject().isEmpty()) {
                                        Long id = th.getObject().get(0).getID();
                                        if (dp.getJEVisClass().equals(csvDataPointClass)) {
                                            if (targetCSV.get(id) == null) {
                                                List<Long> list = new ArrayList<>();
                                                list.add(dp.getID());
                                                targetCSV.put(id, list);
                                            } else {
                                                List<Long> list = new ArrayList<>(targetCSV.remove(id));
                                                list.add(dp.getID());
                                                targetCSV.put(id, list);
                                            }
                                        } else if (dp.getJEVisClass().equals(xmlDataPointClass)) {
                                            if (targetXML.get(id) == null) {
                                                List<Long> list = new ArrayList<>();
                                                list.add(dp.getID());
                                                targetXML.put(id, list);
                                            } else {
                                                List<Long> list = new ArrayList<>(targetXML.remove(id));
                                                list.add(dp.getID());
                                                targetXML.put(id, list);
                                            }
                                        }
                                    }
                                }
                            } else if (dp.getJEVisClass().equals(dwdDataPointClass)) {
                                JEVisAttribute targetAtt1 = null;
                                JEVisSample lastSampleTarget1 = null;
                                JEVisAttribute targetAtt2 = null;
                                JEVisSample lastSampleTarget2 = null;
                                JEVisAttribute targetAtt3 = null;
                                JEVisSample lastSampleTarget3 = null;
                                JEVisAttribute targetAtt4 = null;
                                JEVisSample lastSampleTarget4 = null;
                                JEVisAttribute targetAtt5 = null;
                                JEVisSample lastSampleTarget5 = null;

//                                targetAtt1 = dp.getAttribute(dwd1);
//                                targetAtt2 = dp.getAttribute(dwd2);
//                                targetAtt3 = dp.getAttribute(dwd3);
//                                targetAtt4 = dp.getAttribute(dwd4);
//                                targetAtt5 = dp.getAttribute(dwd5);
//
//                                if (targetAtt1 != null) lastSampleTarget1 = targetAtt1.getLatestSample();
//                                if (targetAtt2 != null) lastSampleTarget2 = targetAtt2.getLatestSample();
//                                if (targetAtt3 != null) lastSampleTarget3 = targetAtt3.getLatestSample();
//                                if (targetAtt4 != null) lastSampleTarget4 = targetAtt4.getLatestSample();
//                                if (targetAtt5 != null) lastSampleTarget5 = targetAtt5.getLatestSample();
//
//                                List<JEVisSample> samples = new ArrayList<>();
//                                if (lastSampleTarget1 != null) samples.add(lastSampleTarget1);
//                                if (lastSampleTarget2 != null) samples.add(lastSampleTarget2);
//                                if (lastSampleTarget3 != null) samples.add(lastSampleTarget3);
//                                if (lastSampleTarget4 != null) samples.add(lastSampleTarget4);
//                                if (lastSampleTarget5 != null) samples.add(lastSampleTarget5);
//
//                                TargetHelper th = null;
//                                for (JEVisSample lastSampleTarget : samples) {
//                                    th = new TargetHelper(ds, lastSampleTarget.getValueAsString());
//                                    JEVisObject target = th.getObject().get(0);
//                                    if (target != null) {
//
//                                        channelAndTarget.put(target, target);
//
//                                    }
//                                }
                            }
                        }
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private TableColumn<TableData, String> buildMinMaxTSColumn(boolean max) {
        String columnName;
        if (max) {
            columnName = I18n.getInstance().getString("jevistree.column.maxts");
        } else {
            columnName = I18n.getInstance().getString("jevistree.column.mints");
        }

        TableColumn<TableData, String> column = new TableColumn<>(columnName);
        column.setId(columnName);
        column.setPrefWidth(135);

        column.setCellValueFactory(param -> {
            try {
                JEVisAttribute valueAttribute = null;
                for (JEVisAttribute att : param.getValue().getAttributeList()) {
                    if (att.getName().equals("Value")) valueAttribute = att;
                }

                if (valueAttribute != null) {
                    if (valueAttribute.hasSample()) {
                        if (max) {
                            return new ReadOnlyObjectWrapper<>(valueAttribute.getTimestampFromLastSample().toString(DATE_FORMAT));
                        } else {
                            return new ReadOnlyObjectWrapper<>(valueAttribute.getTimestampFromFirstSample().toString(DATE_FORMAT));
                        }
                    } else {
                        return new ReadOnlyObjectWrapper<>("");
                    }

                } else {
                    return new ReadOnlyObjectWrapper<>("");
                }

            } catch (Exception ex) {
                logger.error(ex);
            }
            return new ReadOnlyObjectWrapper<>("");
        });

        return column;
    }
}
