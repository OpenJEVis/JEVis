package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.widget.Size;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.jevis.jeconfig.tool.I18n;


public class WidgetColumnFactory {


    private static final Logger logger = LogManager.getLogger(WidgetColumnFactory.class);
    private final DashboardControl control;
    private TableView<Widget> table;
    private Double numberColumDefaultSize = 70d;
    //    private ObservableList<Widget> fullList;
//    private FilteredList<Widget> filteredData;
    private ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (!newValue) {
                WidgetColumnFactory.this.table.refresh();
            }
        }
    };

    public WidgetColumnFactory(DashboardControl control) {
        this.control = control;
    }

    public TableView<Widget> buildTable(ObservableList<Widget> list) {
        list.addListener(new ListChangeListener<Widget>() {
            @Override
            public void onChanged(Change<? extends Widget> c) {
                System.out.println("Tree-itemlistQuelle change: " + list);
            }
        });

//        this.fullList = list.sorted();
//        list.addListener(new ListChangeListener<Widget>() {
//            @Override
//            public void onChanged(Change<? extends Widget> c) {
//                if (c.next()) {
//                    if (c.wasAdded()) {
//                        fullList.addAll(c.getAddedSubList());
//                    } else if (c.wasRemoved()) {
//                        System.out.println("sdsfdsfdsfdsfdsfdsf ----- was removed: " + c.wasRemoved());
//                        fullList.removeAll(c.getRemoved());
//                    }
//                }
//            }
//        });


        this.table = new TableView<>();
        this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        this.table.getColumns().add(typeAttributeColumn());
        this.table.getColumns().add(titleAttributeColumn());
        this.table.getColumns().add(advSettingAttributeColumn());
        this.table.getColumns().add(xAttributeColumn());
        this.table.getColumns().add(yAttributeColumn());
        this.table.getColumns().add(widthAttributeColumn());
        this.table.getColumns().add(heightAttributeColumn());
        this.table.getColumns().add(bgColorAttributeColumn());
        this.table.getColumns().add(fgColorAttributeColumn());

        this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        this.table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.table.getItems().forEach(widget -> {
                this.control.highlightWidgetInView(widget, this.table.getSelectionModel().getSelectedItems().contains(widget));
            });
        });
//        this.filteredData = new FilteredList<Widget>(list);
        this.table.setItems(list);

        return this.table;
    }


    public TableColumn<Widget, Widget> advSettingAttributeColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, Widget>, TableCell<Widget, Widget>>() {
            @Override
            public TableCell<Widget, Widget> call(TableColumn<Widget, Widget> param) {
                TableCell<Widget, Widget> cell = new TableCell<Widget, Widget>() {
                    @Override
                    protected void updateItem(Widget item, boolean empty) {


                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Button button = new Button("", JEConfig.getImage("Service Manager.png", 15, 15));

                            button.setOnAction(event -> {
                                item.openConfig();
//                                item.updateData(null);
//                            WidgetColumnFactory.this.table.refresh();
                            });

                            setText("");
                            setGraphic(button);
                        }


                    }
                };
                return cell;
            }
        };

        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<Widget, Widget>, ObservableValue<Widget>>() {
            @Override
            public ObservableValue<Widget> call(TableColumn.CellDataFeatures<Widget, Widget> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue());
                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<Widget, Widget> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.settings"));
        column.setId("Settings");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

        return column;
    }


    public TableColumn<Widget, String> typeAttributeColumn() {
        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, String>, TableCell<Widget, String>>() {
            @Override
            public TableCell<Widget, String> call(TableColumn<Widget, String> param) {
                TableCell<Widget, String> cell = new TableCell<Widget, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        if (item != null && !empty) {
                            try {
                                ImageView icon = ((Widget) getTableRow().getItem()).getImagePreview();
                                icon.setPreserveRatio(true);
                                icon.setFitHeight(18d);
                                setGraphic(icon);
                            } catch (Exception ex) {
//                                logger.warn(ex.getMessage());
                            }
//                            System.out.println("TableRow value: " + getTableRow().getItem());
                            setText(item);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                };
                return cell;
            }
        };
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<Widget, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Widget, String> param) {
                try {

                    return new SimpleStringProperty(param.getValue().getConfig().getType());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<Widget, String> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.type"));

        column.setId("Type");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

//        ObservableList types = FXCollections.observableArrayList();
//
//        types.clear();
//        types.add("-");
//        Widgets.getAvabableWidgets(control, new WidgetPojo()).forEach(widget -> {
//            widget.getConfig().getType();
//        });
//        this.fullList.forEach(o -> {
//            if (!types.contains(o.getConfig().getType())) {
//                types.add(o.getConfig().getType());
//            }
//        });


//        JFXComboBox<String> typeFilterBox = new JFXComboBox<>(types);
//        typeFilterBox.getSelectionModel().selectFirst();
//
//        typeFilterBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            this.filteredData.setPredicate(widget -> {
//                if (newValue.equals("-")) {
//                    return true;
//                }
//                try {
//                    if (widget.getConfig().getType().equals(newValue)) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    return false;
//                }
//            });
//            this.table.refresh();
//        });

//        Label label = new Label(I18n.getInstance().getString("jevistree.widget.column.type"));
//        label.setAlignment(Pos.BOTTOM_LEFT);
//        GridPane gridPane = new GridPane();
//        gridPane.add(label, 0, 0);
//        gridPane.add(typeFilterBox, 1, 0);

//        column.setGraphic(gridPane);
//        column.setText("");

        column.setPrefWidth(200);

        return column;
    }

    public TableColumn<Widget, String> titleAttributeColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, String>, TableCell<Widget, String>>() {
            @Override
            public TableCell<Widget, String> call(TableColumn<Widget, String> param) {
                TableCell<Widget, String> cell = new TableCell<Widget, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        if (item != null && !empty) {
                            TextField textField = new TextField(item.toString());

                            textField.setOnKeyPressed(event -> {
                                if (event.getCode() == KeyCode.ENTER) {
                                    setWidgetTitle(textField.getText(), (Widget) getTableRow().getItem());
                                }
                                if (event.getCode() == KeyCode.ESCAPE) {
                                    WidgetColumnFactory.this.table.refresh();
                                }
                            });

                            setGraphic(textField);
                        }

                    }
                };
                return cell;
            }
        };

        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<Widget, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Widget, String> param) {
                try {
                    return new SimpleStringProperty(param.getValue().getConfig().getTitle());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<Widget, String> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.title"));
        column.setId("title");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

        return column;
    }


    public TableColumn<Widget, Color> fgColorAttributeColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, Color>, TableCell<Widget, Color>>() {
            @Override
            public TableCell<Widget, Color> call(TableColumn<Widget, Color> param) {
                TableCell<Widget, Color> cell = new TableCell<Widget, Color>() {
                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        if (item != null && !empty) {
                            ColorPicker colorPicker = new ColorPicker(item);
                            colorPicker.setMaxWidth(100d);

                            colorPicker.setStyle("-fx-color-label-visible: false ;");
                            colorPicker.setOnAction(event -> {
                                setWidgetFGColor(colorPicker.getValue(), (Widget) getTableRow().getItem());
                            });

                            setGraphic(colorPicker);
                        }

                    }
                };
                return cell;
            }
        };

        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<Widget, Color>, ObservableValue<Color>>() {
            @Override
            public ObservableValue<Color> call(TableColumn.CellDataFeatures<Widget, Color> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getConfig().getFontColor());
                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<Widget, Color> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.fgcolor"));
        column.setId("fgColor");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

        return column;
    }

    public TableColumn<Widget, Color> bgColorAttributeColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, Color>, TableCell<Widget, Color>>() {
            @Override
            public TableCell<Widget, Color> call(TableColumn<Widget, Color> param) {
                TableCell<Widget, Color> cell = new TableCell<Widget, Color>() {
                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        if (item != null && !empty) {
                            ColorPicker colorPicker = new ColorPicker(item);
                            colorPicker.setMaxWidth(100d);
                            colorPicker.setStyle("-fx-color-label-visible: false ;");

                            colorPicker.setOnAction(event -> {
                                System.out.println("Picker set color: " + colorPicker.getValue());
                                setWidgetBGColor(colorPicker.getValue(), (Widget) getTableRow().getItem());
                            });

                            setGraphic(colorPicker);
                        }

                    }
                };
                return cell;
            }
        };

        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<Widget, Color>, ObservableValue<Color>>() {
            @Override
            public ObservableValue<Color> call(TableColumn.CellDataFeatures<Widget, Color> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getConfig().getBackgroundColor());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<Widget, Color> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.bgcolor"));
        column.setId("Title");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

        return column;
    }


    public TableColumn<Widget, Double> xAttributeColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, Double>, TableCell<Widget, Double>>() {
            @Override
            public TableCell<Widget, Double> call(TableColumn<Widget, Double> param) {
                TableCell<Widget, Double> cell = new TableCell<Widget, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        if (item != null && !empty) {
                            TextField textField = buildDoubleTextField(item.toString());
                            textField.setOnKeyPressed(event -> {
                                if (event.getCode() == KeyCode.ENTER) {
                                    setWidgetXPosition(textField, (Widget) getTableRow().getItem());
                                }
                                if (event.getCode() == KeyCode.ESCAPE) {
                                    WidgetColumnFactory.this.table.refresh();
                                }
                            });


                            setGraphic(textField);
                        }
                    }
                };
                return cell;
            }
        };
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<Widget, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<Widget, Double> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getConfig().getxPosition());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<Widget, Double> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.xpos"));
        column.setId("xPos");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
        column.setEditable(true);
//        column.setPrefWidth(100d);

        return column;
    }

    public TableColumn<Widget, Double> yAttributeColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, Double>, TableCell<Widget, Double>>() {
            @Override
            public TableCell<Widget, Double> call(TableColumn<Widget, Double> param) {
                TableCell<Widget, Double> cell = new TableCell<Widget, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        if (item != null && !empty) {
                            TextField textField = buildDoubleTextField(item.toString());
                            textField.setOnKeyPressed(event -> {
                                if (event.getCode() == KeyCode.ENTER) {
                                    setWidgetYPosition(textField, (Widget) getTableRow().getItem());
                                }
                                if (event.getCode() == KeyCode.ESCAPE) {
                                    WidgetColumnFactory.this.table.refresh();
                                }
                            });


                            setGraphic(textField);
                        }
                    }
                };
                return cell;
            }
        };
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<Widget, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<Widget, Double> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getConfig().getyPosition());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };

        TableColumn<Widget, Double> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.ypos"));
        column.setId("yPos");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
//        column.setPrefWidth(100d);

        return column;
    }

    public TableColumn<Widget, Double> widthAttributeColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, Double>, TableCell<Widget, Double>>() {
            @Override
            public TableCell<Widget, Double> call(TableColumn<Widget, Double> param) {
                TableCell<Widget, Double> cell = new TableCell<Widget, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        if (item != null && !empty) {
                            TextField textField = buildDoubleTextField(item.toString());
                            textField.setOnKeyPressed(event -> {
                                if (event.getCode() == KeyCode.ENTER) {
                                    setWidgetWidth(textField, (Widget) getTableRow().getItem());
                                }
                                if (event.getCode() == KeyCode.ESCAPE) {
                                    WidgetColumnFactory.this.table.refresh();
                                }
                            });


                            setGraphic(textField);
                        }
                    }
                };
                return cell;
            }
        };
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<Widget, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<Widget, Double> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getConfig().getSize().getWidth());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };

        TableColumn<Widget, Double> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.width"));
        column.setId("Width");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
//        column.setPrefWidth(100d);
        return column;
    }


    private void setWidgetYPosition(TextField textField, Widget srcWidget) {
        Double pos = Double.parseDouble(textField.getText());
        srcWidget.getConfig().setyPosition(pos);


        if (this.table.getSelectionModel().getSelectedItems().contains(srcWidget)) {
            this.table.getSelectionModel().getSelectedItems().forEach(widget -> {
                widget.getConfig().setyPosition(pos);
                WidgetColumnFactory.this.control.requestViewUpdate(widget);
            });
        }

        this.table.refresh();
    }


    private void setWidgetFGColor(Color color, Widget srcWidget) {
        srcWidget.getConfig().setFontColor(color);
        WidgetColumnFactory.this.control.requestViewUpdate(srcWidget);

        if (this.table.getSelectionModel().getSelectedItems().contains(srcWidget)) {
            this.table.getSelectionModel().getSelectedItems().forEach(widget -> {
                srcWidget.getConfig().setFontColor(color);
                WidgetColumnFactory.this.control.requestViewUpdate(widget);
            });
        }

        this.table.refresh();
    }

    private void setWidgetBGColor(Color color, Widget srcWidget) {
        srcWidget.getConfig().setBackgroundColor(color);
        WidgetColumnFactory.this.control.requestViewUpdate(srcWidget);

        if (this.table.getSelectionModel().getSelectedItems().contains(srcWidget)) {
            this.table.getSelectionModel().getSelectedItems().forEach(widget -> {
                srcWidget.getConfig().setBackgroundColor(color);
                WidgetColumnFactory.this.control.requestViewUpdate(widget);
            });
        }

        this.table.refresh();
    }

    private void setWidgetXPosition(TextField textField, Widget srcWidget) {
        Double xPos = Double.parseDouble(textField.getText());

        srcWidget.getConfig().setxPosition(xPos);
        WidgetColumnFactory.this.control.requestViewUpdate(srcWidget);

        if (this.table.getSelectionModel().getSelectedItems().contains(srcWidget)) {
            this.table.getSelectionModel().getSelectedItems().forEach(widget -> {
                widget.getConfig().setxPosition(xPos);
                WidgetColumnFactory.this.control.requestViewUpdate(widget);
            });
        }

        this.table.refresh();
    }

    private void setWidgetHeight(TextField textField, Widget srcWidget) {
        Double height = Double.parseDouble(textField.getText());
        Size newSize = new Size(height, srcWidget.getConfig().getSize().getWidth());
        srcWidget.getConfig().setSize(newSize);
        WidgetColumnFactory.this.control.requestViewUpdate(srcWidget);

        if (this.table.getSelectionModel().getSelectedItems().contains(srcWidget)) {
            this.table.getSelectionModel().getSelectedItems().forEach(widget -> {
                widget.getConfig().setSize(newSize);
                WidgetColumnFactory.this.control.requestViewUpdate(widget);
            });
        }

        this.table.refresh();
    }

    private void setWidgetWidth(TextField textField, Widget srcWidget) {
        Double width = Double.parseDouble(textField.getText());
        Size newSize = new Size(srcWidget.getConfig().getSize().getWidth(), width);
        srcWidget.getConfig().setSize(newSize);
        WidgetColumnFactory.this.control.requestViewUpdate(srcWidget);

        if (this.table.getSelectionModel().getSelectedItems().contains(srcWidget)) {
            this.table.getSelectionModel().getSelectedItems().forEach(widget -> {
                widget.getConfig().setSize(newSize);
                WidgetColumnFactory.this.control.requestViewUpdate(widget);
            });
        }

        this.table.refresh();
    }

    private void setWidgetTitle(String title, Widget srcWidget) {
        srcWidget.getConfig().setTitle(title);
        WidgetColumnFactory.this.control.requestViewUpdate(srcWidget);

        if (this.table.getSelectionModel().getSelectedItems().contains(srcWidget)) {
            this.table.getSelectionModel().getSelectedItems().forEach(widget -> {
                widget.getConfig().setTitle(title);
                WidgetColumnFactory.this.control.requestViewUpdate(widget);
            });
        }

        this.table.refresh();
    }


    private TextField buildDoubleTextField(String text) {
        TextField textField = new TextField(text);
        textField.focusedProperty().addListener(WidgetColumnFactory.this.focusListener);
        textField.setMaxWidth(this.numberColumDefaultSize);
        return textField;
    }

    public TableColumn<Widget, Double> heightAttributeColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, Double>, TableCell<Widget, Double>>() {
            @Override
            public TableCell<Widget, Double> call(TableColumn<Widget, Double> param) {
                TableCell<Widget, Double> cell = new TableCell<Widget, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        if (item != null && !empty) {
                            TextField textField = buildDoubleTextField(item.toString());
                            textField.setOnKeyPressed(event -> {
                                if (event.getCode() == KeyCode.ENTER) {
                                    setWidgetHeight(textField, (Widget) getTableRow().getItem());
                                }
                                if (event.getCode() == KeyCode.ESCAPE) {
                                    WidgetColumnFactory.this.table.refresh();
                                }
                            });


                            setGraphic(textField);
                        }
                    }
                };
                return cell;
            }
        };
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<Widget, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<Widget, Double> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getConfig().getSize().getHeight());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };

        TableColumn<Widget, Double> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.height"));
        column.setId("Height");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);
//        column.setPrefWidth(100d);
        return column;
    }

    private static void setDefaultFieldStyle(TableCell cell, Control... field) {
        for (Control control : field) {
            control.setStyle("-fx-background-color: transparent;" +
                    "    -fx-background-insets: 0;" +
                    "    -fx-padding: 0 0 0 0;");
//            control.focusedProperty().addListener((observable, oldValue, newValue) -> {
//                if (newValue) {
//                    getSelectionModel().clearAndSelect(cell.getTableRow().getIndex());
//                }
//            });
        }

    }

}
