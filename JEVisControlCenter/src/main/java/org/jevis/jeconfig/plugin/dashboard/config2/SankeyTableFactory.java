package org.jevis.jeconfig.plugin.dashboard.config2;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.List;
import java.util.Optional;

public class SankeyTableFactory {


    private static final Logger logger = LogManager.getLogger(WidgetColumnFactory.class);
    private final Double numberColumDefaultSize = 70d;
    private TableView<SankeyDataRow> tableView;

    public ObservableList<SankeyDataRow> observableList = FXCollections.observableArrayList();

    private BooleanProperty disable = new SimpleBooleanProperty(false) {
    };
    private final ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (!newValue) {
                SankeyTableFactory.this.tableView.refresh();
            }
        }
    };

    public TableView<SankeyDataRow> buildTable(ObservableList<SankeyDataRow> sankeyDataRows) {
        tableView = new TableView<>();
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.tableView.getColumns().add(buildID());
        this.tableView.getColumns().add(buildName());
        this.tableView.getColumns().add(advSettingAttributeColumn());
        this.tableView.getColumns().add(move());

        //this.tableView.getColumns().add(buildLevel());
        observableList = sankeyDataRows;

        return tableView;

    }


    private TableColumn<SankeyDataRow, String> buildName() {
        TableColumn<SankeyDataRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.meters.table.measurementpoint.columnname"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(param -> {
            try {
                if (param.getValue().getJeVisObject().getJEVisClassName().equals(JC.Data.CleanData.name)) {
                    return new SimpleObjectProperty<>(param.getValue().getJeVisObject().getParent().getName() + " / " + param.getValue().getJeVisObject().getName());
                } else {
                    return new SimpleObjectProperty<>(param.getValue().getJeVisObject().getName());
                }
            } catch (JEVisException e) {
                throw new RuntimeException(e);
            }
        });
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        nameColumn.setPrefWidth(numberColumDefaultSize);


        return nameColumn;
    }

//    private TableColumn<SankeyDataRow, String> buildLevel() {
//        TableColumn<SankeyDataRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("Level"));
//        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
//        nameColumn.setCellValueFactory(sankeyDataRowStringCellDataFeatures -> new SimpleStringProperty(String.valueOf(sankeyDataRowStringCellDataFeatures.getValue().getLevel())));
//        nameColumn.setOnEditCommit(sankeyDataRowStringCellEditEvent -> sankeyDataRowStringCellEditEvent.getRowValue().setLevel(Integer.parseInt(sankeyDataRowStringCellEditEvent.getNewValue())));
//        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
//        nameColumn.setEditable(true);
//        nameColumn.setPrefWidth(numberColumDefaultSize);
//
//
//        return nameColumn;
//    }



    private TableColumn<SankeyDataRow, String> buildID() {
        TableColumn<SankeyDataRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.meters.table.measurementpoint.columnname"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().jeVisObject.getID())));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        nameColumn.setPrefWidth(numberColumDefaultSize);


        return nameColumn;
    }


    public TableColumn<SankeyDataRow, SankeyDataRow> advSettingAttributeColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<SankeyDataRow, SankeyDataRow>, TableCell<SankeyDataRow, SankeyDataRow>>() {
            @Override
            public TableCell<SankeyDataRow, SankeyDataRow> call(TableColumn<SankeyDataRow, SankeyDataRow> param) {
                TableCell<SankeyDataRow, SankeyDataRow> cell = new TableCell<SankeyDataRow, SankeyDataRow>() {
                    @Override
                    protected void updateItem(SankeyDataRow item, boolean empty) {


                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            CheckComboBox<SankeyDataRow> checkComboBox = new CheckComboBox();
                            checkComboBox.getItems().addAll(observableList);
                            for (JEVisObject jeVisObject : item.getChildren()) {
                                Optional<SankeyDataRow> sankeyDataRowOptional = checkComboBox.getItems().stream().filter(sankeyDataRow -> sankeyDataRow.getJeVisObject().getID().intValue() == jeVisObject.getID().intValue()).findAny();
                                if (sankeyDataRowOptional.isPresent()) {
                                    checkComboBox.getCheckModel().check(sankeyDataRowOptional.get());
                                }
                            }
                            checkComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener() {
                                @Override
                                public void onChanged(Change change) {
                                    item.setAllChildren(change.getList());
                                }
                            });

                            setText("");
                            setGraphic(checkComboBox);
                        }


                    }
                };
                return cell;
            }
        };

        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<SankeyDataRow, SankeyDataRow>, ObservableValue<SankeyDataRow>>() {
            @Override
            public ObservableValue<SankeyDataRow> call(TableColumn.CellDataFeatures<SankeyDataRow, SankeyDataRow> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue());
                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<SankeyDataRow, SankeyDataRow> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.settings"));
        column.setId("Settings");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
        column.setPrefWidth(numberColumDefaultSize);

        return column;
    }    public TableColumn<SankeyDataRow, SankeyDataRow> move() {

        Callback treeTableColumnCallback = new Callback<TableColumn<SankeyDataRow, SankeyDataRow>, TableCell<SankeyDataRow, SankeyDataRow>>() {
            @Override
            public TableCell<SankeyDataRow, SankeyDataRow> call(TableColumn<SankeyDataRow, SankeyDataRow> param) {
                TableCell<SankeyDataRow, SankeyDataRow> cell = new TableCell<SankeyDataRow, SankeyDataRow>() {
                    @Override
                    protected void updateItem(SankeyDataRow item, boolean empty) {


                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            HBox hBox = new HBox();
                            JFXButton jfxButtonMoveUp = new JFXButton("move up");
                            JFXButton jfxButtonMoveDown = new JFXButton("moveDown");
                            hBox.setSpacing(3);
                            hBox.getChildren().addAll(jfxButtonMoveUp, jfxButtonMoveDown);

                            jfxButtonMoveDown.setOnAction(actionEvent -> {

                                System.out.println("old list");
                                System.out.println(observableList);
                                int index = observableList.indexOf(item);


                                SankeyDataRow swap = observableList.get(index + 1);

                                observableList.set(index + 1, item);
                                observableList.set(index, swap);
                                System.out.println("new list");
                                System.out.println(observableList);

                                System.out.println("tableview list");
                                System.out.printf(tableView.getItems().toString());
                                tableView.getItems().setAll(observableList);

                                tableView.refresh();

                            });

                            jfxButtonMoveUp.setOnAction(actionEvent -> {
                                int index = observableList.indexOf(item);


                                SankeyDataRow swap = observableList.get(index - 1);

                                observableList.set(index - 1, item);
                                observableList.set(index, swap);
                                tableView.getItems().setAll(observableList);
                                tableView.refresh();


                            });

                            setText("");
                            setGraphic(hBox);
                        }


                    }
                };
                return cell;
            }
        };

        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<SankeyDataRow, SankeyDataRow>, ObservableValue<SankeyDataRow>>() {
            @Override
            public ObservableValue<SankeyDataRow> call(TableColumn.CellDataFeatures<SankeyDataRow, SankeyDataRow> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue());
                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<SankeyDataRow, SankeyDataRow> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.settings"));
        column.setId("Settings");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
        column.setPrefWidth(numberColumDefaultSize);

        return column;
    }


    private JFXTextField buildTextField(String text) {
        JFXTextField textField = new JFXTextField(text);
        textField.focusedProperty().addListener(SankeyTableFactory.this.focusListener);
        //textField.setMaxWidth(this.numberColumDefaultSize);
        return textField;
    }

    public void addFocusRefreshListener(Control field) {
        field.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue == true && newValue == false) {
                    SankeyTableFactory.this.tableView.refresh();
                }
            }
        });
    }

    public boolean isDisable() {
        return disable.get();
    }

    public BooleanProperty disableProperty() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable.set(disable);
    }

}
