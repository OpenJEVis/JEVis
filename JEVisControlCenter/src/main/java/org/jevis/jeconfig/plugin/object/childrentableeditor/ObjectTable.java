package org.jevis.jeconfig.plugin.object.childrentableeditor;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;

public class ObjectTable {

    TableView tableView = new TableView<TableData>();

    private static final Logger logger = LogManager.getLogger(ObjectTable.class);

    public ObjectTable(JEVisObject parentObject) {

        try {
            List<JEVisObject> children = parentObject.getChildren();
            List<JEVisAttribute> attributes = new ArrayList<>();
            ObservableList<TableData> tableData = FXCollections.observableArrayList();
//            List<TableColumn<String, JEVisAttribute>> columns = new ArrayList<>();

            TableColumn<TableData, String> nameColumn = new TableColumn<>("Object Name");
            nameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableData, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<TableData, String> param) {
                    return new ReadOnlyObjectWrapper<>(param.getValue().getObject().getName());
                }
            });

            TableColumn<TableData, String> classColumn = new TableColumn<>("Object Name");
            classColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableData, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<TableData, String> param) {
                    try {
                        return new ReadOnlyObjectWrapper<>(param.getValue().getObject().getJEVisClassName());
                    } catch (Exception ex) {
                        return new ReadOnlyObjectWrapper<>("");
                    }
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
                TableColumn<TableData, String> column = new TableColumn<>(attribute.getName());
                column.setId(attribute.getName());
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableData, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<TableData, String> param) {
                        try {
                            JEVisAttribute att = param.getValue().getObject().getAttribute(attribute.getName());
                            if (att != null && att.hasSample()) {
                                return new ReadOnlyObjectWrapper<>(att.getLatestSample().getValueAsString());
                            }
                        } catch (Exception ex) {
                            logger.error(ex);
                        }

                        return new ReadOnlyObjectWrapper<>("");
                    }
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

    private void addChildren(ObservableList<TableData> tableData, List<JEVisAttribute> attributes, JEVisObject parent) {
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
