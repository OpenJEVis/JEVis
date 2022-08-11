package org.jevis.jeconfig.plugin.dashboard.config2;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import javax.swing.text.html.ImageView;

public class GaugeSectionTableFactory {



    private static final Logger logger = LogManager.getLogger(WidgetColumnFactory.class);

    private TableView<GaugeSectionPojo> tableView;

    private final Double numberColumDefaultSize = 70d;

    public TableView<GaugeSectionPojo> buildTable() {
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY );
        this.tableView.getColumns().add(buildStart());
        this.tableView.getColumns().add(buildEnd());
        this.tableView.getColumns().add(buildColor());

        return tableView;

    }
    private final ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (!newValue) {
                GaugeSectionTableFactory.this.tableView.refresh();
            }
        }
    };

    public TableColumn<GaugeSectionPojo, Color> buildColor() {

        Callback treeTableColumnCallback = new Callback<TableColumn<GaugeSectionPojo, Color>, TableCell<GaugeSectionPojo, Color>>() {
            @Override
            public TableCell<GaugeSectionPojo, Color> call(TableColumn<GaugeSectionPojo, Color> param) {
                TableCell<GaugeSectionPojo, Color> cell = new TableCell<GaugeSectionPojo, Color>() {
                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        if (item != null && !empty) {
                            ColorPickerAdv colorPicker = new ColorPickerAdv();
                            colorPicker.setMaxWidth(100d);
                            colorPicker.setValue(item);

                            colorPicker.setStyle("-fx-color-label-visible: false ;");
                            colorPicker.setOnAction(event -> {
                                GaugeSectionPojo gaugeSectionPojo = (GaugeSectionPojo) getTableRow().getItem();
                                gaugeSectionPojo.setColor(colorPicker.getValue());
                            });

//                            addFocusRefreshListener(colorPicker);

                            setGraphic(colorPicker);
                        } else {
                            setGraphic(null);
                        }

                    }
                };
                return cell;
            }
        };

        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<GaugeSectionPojo, Color>, ObservableValue<Color>>() {
            @Override
            public ObservableValue<Color> call(TableColumn.CellDataFeatures<GaugeSectionPojo, Color> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getColor());
                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<GaugeSectionPojo, Color> column = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.gaugewidget.color"));
        column.setId("Color");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

        return column;
    }

    private TableColumn<GaugeSectionPojo, Double> buildStart() {
        Callback treeTableColumnCallback = new Callback<TableColumn<GaugeSectionPojo, Double>, TableCell<GaugeSectionPojo, Double>>() {
            @Override
            public TableCell<GaugeSectionPojo, Double> call(TableColumn<GaugeSectionPojo, Double> param) {
                TableCell<GaugeSectionPojo, Double> cell = new TableCell<GaugeSectionPojo, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        if (item != null && !empty) {
                            JFXTextField textField = buildDoubleTextField(item.toString());

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                   GaugeSectionPojo gaugeSectionPojo = (GaugeSectionPojo) getTableRow().getItem();
                                   gaugeSectionPojo.setStart(Double.parseDouble(newValue));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                            addFocusRefreshListener(textField);
                            textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                    try {
                                        tableView.getSelectionModel().select((GaugeSectionPojo) getTableRow().getItem());
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
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<GaugeSectionPojo, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<GaugeSectionPojo, Double> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getStart());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };




        TableColumn<GaugeSectionPojo, Double> column = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.gaugewidget.start"));
        column.setId("Start");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
        column.setEditable(true);
//        column.setPrefWidth(100d);

        return column;
    }

    private TableColumn<GaugeSectionPojo, Double> buildEnd() {
        Callback treeTableColumnCallback = new Callback<TableColumn<GaugeSectionPojo, Double>, TableCell<GaugeSectionPojo, Double>>() {
            @Override
            public TableCell<GaugeSectionPojo, Double> call(TableColumn<GaugeSectionPojo, Double> param) {
                TableCell<GaugeSectionPojo, Double> cell = new TableCell<GaugeSectionPojo, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        if (item != null && !empty) {
                            JFXTextField textField = buildDoubleTextField(item.toString());
                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    GaugeSectionPojo gaugeSectionPojo = (GaugeSectionPojo) getTableRow().getItem();
                                    gaugeSectionPojo.setEnd(Double.parseDouble(newValue));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });

                            textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                    try {
                                        tableView.getSelectionModel().select((GaugeSectionPojo) getTableRow().getItem());
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
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<GaugeSectionPojo, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<GaugeSectionPojo, Double> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getEnd());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };




        TableColumn<GaugeSectionPojo, Double> column = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.gaugewidget.end"));
        column.setId("End");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
        column.setEditable(true);
//        column.setPrefWidth(100d);

        return column;
    }


    private JFXTextField buildDoubleTextField(String text) {
        JFXTextField textField = new JFXTextField(text);
        textField.focusedProperty().addListener(GaugeSectionTableFactory.this.focusListener);
        //textField.setMaxWidth(this.numberColumDefaultSize);
        return textField;
    }
    public void addFocusRefreshListener(Control field) {
        field.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue == true && newValue == false) {
                    GaugeSectionTableFactory.this.tableView.refresh();
                }
            }
        });
    }





}
