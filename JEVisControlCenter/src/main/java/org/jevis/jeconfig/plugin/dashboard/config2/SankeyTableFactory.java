package org.jevis.jeconfig.plugin.dashboard.config2;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
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
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;

import java.util.Optional;

public class SankeyTableFactory {


    private static final Logger logger = LogManager.getLogger(WidgetColumnFactory.class);
    private final Double numberColumDefaultSize = 280d;
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
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.tableView.getColumns().add(buildIDColumn());
        this.tableView.getColumns().add(buildNameColumn());
        this.tableView.getColumns().add(buildFlowsIntoColumn());
        this.tableView.getColumns().add(buildMoveColumn());

        observableList = sankeyDataRows;

        return tableView;

    }


    private TableColumn<SankeyDataRow, String> buildNameColumn() {
        TableColumn<SankeyDataRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.sankey.objectname"));
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
    private TableColumn<SankeyDataRow, String> buildIDColumn() {
        TableColumn<SankeyDataRow, String> column = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.sankey.id"));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().jeVisObject.getID())));
        column.setStyle("-fx-alignment: CENTER-LEFT;");
        column.setPrefWidth(numberColumDefaultSize/2);
        column.setMaxWidth(numberColumDefaultSize);


        return column;
    }


    public TableColumn<SankeyDataRow, SankeyDataRow> buildFlowsIntoColumn() {

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
                            checkComboBox.setPrefWidth(1000d);
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


        TableColumn<SankeyDataRow, SankeyDataRow> column = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.sankey.flowchildren"));
        column.setId("FlowsInto");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
        column.setPrefWidth(numberColumDefaultSize);

        return column;
    }    public TableColumn<SankeyDataRow, SankeyDataRow> buildMoveColumn() {

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
                            MFXButton MFXButtonMoveUp = new MFXButton("", JEConfig.getSVGImage(Icon.ARROW_UP, 10, 10));
                            MFXButton MFXButtonMoveDown = new MFXButton("", JEConfig.getSVGImage(Icon.ARROW_DOWN, 10, 10));
                            hBox.setSpacing(3);
                            hBox.getChildren().addAll(MFXButtonMoveUp, MFXButtonMoveDown);

                            MFXButtonMoveDown.setOnAction(actionEvent -> {


                                int index = observableList.indexOf(item);


                                SankeyDataRow swap = observableList.get(index + 1);

                                observableList.set(index + 1, item);
                                observableList.set(index, swap);

                                tableView.getItems().setAll(observableList);

                                tableView.refresh();

                            });

                            MFXButtonMoveUp.setOnAction(actionEvent -> {
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


        TableColumn<SankeyDataRow, SankeyDataRow> column = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.sankey.move"));
        column.setId("Move");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
        column.setPrefWidth(numberColumDefaultSize);
        column.setMaxWidth(numberColumDefaultSize * 4);

        return column;
    }


    private MFXTextField buildTextField(String text) {
        MFXTextField textField = new MFXTextField(text);
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
