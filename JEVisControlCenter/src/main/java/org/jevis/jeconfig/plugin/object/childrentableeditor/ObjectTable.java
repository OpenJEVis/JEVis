package org.jevis.jeconfig.plugin.object.childrentableeditor;

import com.jfoenix.controls.JFXDatePicker;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.sample.SampleEditor;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ObjectTable {

    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final TableView<TableData> tableView = new TableView<TableData>();

    private static final Logger logger = LogManager.getLogger(ObjectTable.class);
    private DateTime start;
    private DateTime end;

    public ObjectTable(JEVisObject parentObject, JFXDatePicker startDatePicker, JFXDatePicker endDatePicker, ToggleButton reloadButton) {
        reloadButton.setOnAction(event -> tableView.refresh());
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> start = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0, 0, 0));
        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> end = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 23, 59, 59, 999));
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
                        se.show(JEConfig.getStage(), attribute);
                    } catch (Exception e) {
                        logger.error("Could not open sample editor for row:col {}:{}, object {}:{} and attribute {}", row, col, tableData.getObject().getName(), attributeName, e);
                    }
                }
            }
        });


        try {
            List<JEVisObject> children = parentObject.getChildren();
            List<JEVisAttribute> attributes = new ArrayList<>();
            ObservableList<TableData> tableData = FXCollections.observableArrayList();
//            List<TableColumn<String, JEVisAttribute>> columns = new ArrayList<>();


            TableColumn<TableData, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.object.attribute.overview.name"));
            nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getObject().getName()));

            TableColumn<TableData, String> classColumn = new TableColumn<>(I18n.getInstance().getString("plugin.dtrc.dialog.classlabel"));
            classColumn.setCellValueFactory(param -> {
                try {
                    String jeVisClassName = param.getValue().getObject().getJEVisClassName();
                    try {
                        jeVisClassName = I18nWS.getInstance().getClassName(jeVisClassName);
                    } catch (Exception e) {
                        logger.error("Could not get class name for {} class", jeVisClassName, e);
                    }
                    return new ReadOnlyObjectWrapper<>(jeVisClassName);
                } catch (Exception ex) {
                    return new ReadOnlyObjectWrapper<>("");
                }
            });


            this.tableView.getColumns().addAll(nameColumn, classColumn);

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
                    try {
                        if (attribute.hasSample() && attribute.getName().equals("Value")) {
                            List<JEVisSample> samples = attribute.getSamples(start, end);

                            String resultString = "";
                            if (!samples.isEmpty()) {
                                JEVisSample sample = samples.get(samples.size() - 1);
                                resultString += sample.getValueAsString() + "@" + sample.getTimestamp().toString(PATTERN)
                                        + " (" + I18n.getInstance().getString("plugin.object.attribute.overview.totalsamplecount") + ": " + samples.size() + ")";
                            } else {
                                resultString += "(" + I18n.getInstance().getString("plugin.object.attribute.overview.totalsamplecount") + ": 0)";
                            }

                            return new ReadOnlyObjectWrapper<>(resultString);
                        } else if (attribute.hasSample()) {
                            JEVisSample latestSample = attribute.getLatestSample();
                            return new ReadOnlyObjectWrapper<>(latestSample.getValueAsString());
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                    }

                    return new ReadOnlyObjectWrapper<>("");
                });

//                columns.add(column);
                this.tableView.getColumns().add(column);
            }
            this.tableView.getItems().setAll(tableData);


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


        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void addChildren
            (ObservableList<TableData> tableData, List<JEVisAttribute> attributes, JEVisObject parent) {
        try {
            for (JEVisObject child : parent.getChildren()) {
                for (JEVisAttribute attribute : child.getAttributes()) {
                    addAttributeSave(attributes, attribute);
                }
                tableData.add(new TableData(child));
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


}
