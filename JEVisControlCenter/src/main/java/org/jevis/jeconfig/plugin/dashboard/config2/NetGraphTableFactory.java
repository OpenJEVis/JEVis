package org.jevis.jeconfig.plugin.dashboard.config2;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;

public class NetGraphTableFactory {


    private static final Logger logger = LogManager.getLogger(WidgetColumnFactory.class);
    private final Double numberColumDefaultSize = 70d;
    private TableView<NetGraphDataRow> tableView;
    private BooleanProperty disable = new SimpleBooleanProperty(false) {
    };
    private final ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (!newValue) {
                NetGraphTableFactory.this.tableView.refresh();
            }
        }
    };

    public TableView<NetGraphDataRow> buildTable(boolean disableMinMax) {
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.tableView.getColumns().add(buildID());
        this.tableView.getColumns().add(buildName());
        this.tableView.getColumns().add(buildMin());
        this.tableView.getColumns().add(buildMax());


        return tableView;

    }


    private TableColumn<NetGraphDataRow, Double> buildMin() {
        Callback treeTableColumnCallback = new Callback<TableColumn<NetGraphDataRow, Double>, TableCell<NetGraphDataRow, Double>>() {
            @Override
            public TableCell<NetGraphDataRow, Double> call(TableColumn<NetGraphDataRow, Double> param) {
                TableCell<NetGraphDataRow, Double> cell = new TableCell<NetGraphDataRow, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        if (item != null && !empty) {
                            MFXTextField textField = buildTextField(item.toString());
                            textField.setDisable(disable.getValue());
                            disable.addListener((observable, oldValue, newValue) -> {
                                textField.setDisable(newValue);
                            });

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    NetGraphDataRow gaugeSectionPojo = (NetGraphDataRow) getTableRow().getItem();
                                    gaugeSectionPojo.setMin(Double.parseDouble(newValue));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                            addFocusRefreshListener(textField);
                            textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                    try {
                                        tableView.getSelectionModel().select((NetGraphDataRow) getTableRow().getItem());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            setGraphic(textField);
                        } else {
                            setGraphic(null);
                        }
                    }
                };
                return cell;
            }
        };
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<NetGraphDataRow, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<NetGraphDataRow, Double> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getMin());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<NetGraphDataRow, Double> column = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.min"));
        column.setId("min");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
//        column.setPrefWidth(100d);

        return column;
    }

    private TableColumn<NetGraphDataRow, String> buildName() {
        TableColumn<NetGraphDataRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.meters.table.measurementpoint.columnname"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().jeVisObject.getName()));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");


        return nameColumn;
    }

    private TableColumn<NetGraphDataRow, String> buildID() {
        TableColumn<NetGraphDataRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.meters.table.measurementpoint.columnname"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().jeVisObject.getID())));
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");


        return nameColumn;
    }


    private TableColumn<NetGraphDataRow, Double> buildMax() {
        Callback treeTableColumnCallback = new Callback<TableColumn<NetGraphDataRow, Double>, TableCell<NetGraphDataRow, Double>>() {
            @Override
            public TableCell<NetGraphDataRow, Double> call(TableColumn<NetGraphDataRow, Double> param) {
                TableCell<NetGraphDataRow, Double> cell = new TableCell<NetGraphDataRow, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        if (item != null && !empty) {
                            MFXTextField textField = buildTextField(item.toString());
                            textField.setDisable(disable.getValue());
                            disable.addListener((observable, oldValue, newValue) -> {
                                textField.setDisable(newValue);
                            });
                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    NetGraphDataRow gaugeSectionPojo = (NetGraphDataRow) getTableRow().getItem();
                                    gaugeSectionPojo.setMax(Double.parseDouble(newValue));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });

                            textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                    try {
                                        tableView.getSelectionModel().select((NetGraphDataRow) getTableRow().getItem());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });


                            addFocusRefreshListener(textField);
                            setGraphic(textField);
                        } else {
                            setGraphic(null);
                        }
                    }
                };
                return cell;
            }
        };
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<NetGraphDataRow, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<NetGraphDataRow, Double> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getMax());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<NetGraphDataRow, Double> column = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.net.max"));
        column.setId("max");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
//        column.setPrefWidth(100d);

        return column;
    }


    private MFXTextField buildTextField(String text) {
        MFXTextField textField = new MFXTextField(text);
        textField.focusedProperty().addListener(NetGraphTableFactory.this.focusListener);
        //textField.setMaxWidth(this.numberColumDefaultSize);
        return textField;
    }

    public void addFocusRefreshListener(Control field) {
        field.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue == true && newValue == false) {
                    NetGraphTableFactory.this.tableView.refresh();
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
