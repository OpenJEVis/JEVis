package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
//        list.addListener(new ListChangeListener<Widget>() {
//            @Override
//            public void onChanged(Change<? extends Widget> c) {
//                System.out.println("Tree-itemlistQuelle change: " + list);
//            }
//        });

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
        this.table.getColumns().add(buildIDColoumn());
        this.table.getColumns().add(buildOrderColoumn());
        this.table.getColumns().add(typeAttributeColumn());
        this.table.getColumns().add(titleAttributeColumn());
        this.table.getColumns().add(advSettingAttributeColumn());
        this.table.getColumns().add(xAttributeColumn());
        this.table.getColumns().add(yAttributeColumn());
        this.table.getColumns().add(widthAttributeColumn());
        this.table.getColumns().add(heightAttributeColumn());
        this.table.getColumns().add(fgColorAttributeColumn());
        this.table.getColumns().add(bgColorAttributeColumn());


        this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        /** disable all sorting, the problem is that is its the same observable list the Controller is using and we don't want so sort it**/
        this.table.getColumns().forEach(widgetTableColumn -> {
            widgetTableColumn.setSortable(false);
        });

        this.table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.table.getItems().forEach(widget -> {
                this.control.highlightWidgetInView(widget, this.table.getSelectionModel().getSelectedItems().contains(widget));
            });
        });
//        this.filteredData = new FilteredList<Widget>(list);
        this.table.setItems(list);

        return this.table;
    }

    private void moveWidget(Widget widget, int offset) {
        int oldIndex = table.getItems().indexOf(widget);
        int newIndex = oldIndex + offset;

        if (newIndex < 0) {
            newIndex = 0;
        }
        if (newIndex > table.getItems().size() - 1) {
            newIndex = table.getItems().size() - 1;
        }

        Collections.swap(table.getItems(), oldIndex, newIndex);
    }

    public TableColumn<Widget, Widget> buildOrderColoumn() {

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
                            Button downButton = new Button("", JEConfig.getImage("1395085233_arrow_return_right_down.png", 15, 15));
                            Button upButton = new Button("", JEConfig.getImage("1395085229_arrow_return_right_up.png", 15, 15));

                            upButton.setOnAction(event -> {
                                if (table.getSelectionModel().getSelectedItems().contains(item)) {
                                    List<Widget> toMove = new ArrayList<>(table.getSelectionModel().getSelectedItems());

                                    for (Widget widgetM : toMove) {
                                        moveWidget(widgetM, -1);
                                    }
                                } else {
                                    moveWidget(item, -1);
                                }
                            });

                            downButton.setOnAction(event -> {
                                if (table.getSelectionModel().getSelectedItems().contains(item)) {
                                    List<Widget> toMove = new ArrayList<>(table.getSelectionModel().getSelectedItems());

                                    for (int i = toMove.size() - 1; i >= 0; i--) {
                                        Widget widgetM = toMove.get(i);
                                        moveWidget(widgetM, +1);
                                    }

                                } else {
                                    moveWidget(item, +1);
                                }
                            });

                            HBox hBox = new HBox(downButton, upButton);

                            setText("");
                            setGraphic(hBox);
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


        TableColumn<Widget, Widget> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.order"));
        column.setId("oder");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

        return column;
    }

    public TableColumn<Widget, Integer> buildIDColoumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, Integer>, TableCell<Widget, Integer>>() {
            @Override
            public TableCell<Widget, Integer> call(TableColumn<Widget, Integer> param) {
                TableCell<Widget, Integer> cell = new TableCell<Widget, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        if (item != null && !empty) {
                            setText(item.toString());
                        } else {
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<Widget, Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Widget, Integer> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getConfig().getUuid());
                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<Widget, Integer> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.uuid"));

        column.setId("ID");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

        column.setPrefWidth(80);
        column.setEditable(false);

        return column;
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
                                WidgetColumnFactory.this.table.refresh();
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
        column.setMaxWidth(45d);

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

        column.setPrefWidth(100);

        return column;
    }

    public void addFocusRefreshListener(Control field) {
        field.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue == true && newValue == false) {
                    WidgetColumnFactory.this.table.refresh();
                }
            }
        });
    }

    public TableColumn<Widget, String> titleAttributeColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<Widget, String>, TableCell<Widget, String>>() {
            @Override
            public TableCell<Widget, String> call(TableColumn<Widget, String> param) {
                TableCell<Widget, String> cell = new TableCell<Widget, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        if (item != null && !empty) {
                            TextField textField = new TextField(item);

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                setWidgetTitle(textField.getText(), (Widget) getTableRow().getItem());
                            });

                            addFocusRefreshListener(textField);

//                            textField.setOnKeyPressed(event -> {
//                                if (event.getCode() == KeyCode.ENTER) {
//                                    setWidgetTitle(textField.getText(), (Widget) getTableRow().getItem());
//                                }
//                                if (event.getCode() == KeyCode.ESCAPE) {
//                                    WidgetColumnFactory.this.table.refresh();
//                                }
//                            });

                            setGraphic(textField);
                        }else{
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
                            ColorPickerAdv colorPicker = new ColorPickerAdv();
                            colorPicker.setMaxWidth(100d);
                            colorPicker.setValue(item);

                            colorPicker.setStyle("-fx-color-label-visible: false ;");
                            colorPicker.setOnAction(event -> {
                                setWidgetFGColor(colorPicker.getValue(), (Widget) getTableRow().getItem());
                            });

//                            addFocusRefreshListener(colorPicker);

                            setGraphic(colorPicker);
                        }else{
                            setGraphic(null);
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
//                            ColorPicker colorPicker = new ColorPicker(item);
//                            colorPicker.setMaxWidth(100d);
//                            colorPicker.setStyle("-fx-color-label-visible: false ;");
//
//                            colorPicker.setOnAction(event -> {
//                                setWidgetBGColor(colorPicker.getValue(), (Widget) getTableRow().getItem());
//                            });

                            ColorPickerAdv colorPicker = new ColorPickerAdv();
                            colorPicker.setValue(item);
                            setGraphic(colorPicker);
                            colorPicker.selectColorProperty().addListener((observable, oldValue, newValue) -> {
                                setWidgetBGColor(newValue, (Widget) getTableRow().getItem());
                            });

//                            addFocusRefreshListener(colorPicker);
//                            setGraphic(colorPicker);
                        }else {
                            setGraphic(null);
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
                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setWidgetXPosition(textField, (Widget) getTableRow().getItem());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });

                            addFocusRefreshListener(textField);
                            setGraphic(textField);
                        }else{
                            setGraphic(null);
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
                        setGraphic(null);
                        if (item != null && !empty) {
                            TextField textField = buildDoubleTextField(item.toString());
                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setWidgetYPosition(textField, (Widget) getTableRow().getItem());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                            addFocusRefreshListener(textField);

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
                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setWidgetWidth(textField, (Widget) getTableRow().getItem());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });

//                            textField.setOnKeyPressed(event -> {
//                                if (event.getCode() == KeyCode.ENTER) {
//                                    setWidgetWidth(textField, (Widget) getTableRow().getItem());
//                                }
//                                if (event.getCode() == KeyCode.ESCAPE) {
//                                    WidgetColumnFactory.this.table.refresh();
//                                }
//                            });


                            setGraphic(textField);
                        }else{
                            setGraphic(null);
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

//        this.table.refresh();
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

//        this.table.refresh();
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

//        this.table.refresh();
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

//        this.table.refresh();
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

//        this.table.refresh();
    }

    private void setWidgetWidth(TextField textField, Widget srcWidget) {
        Double width = Double.parseDouble(textField.getText());
        Size newSize = new Size(srcWidget.getConfig().getSize().getHeight(), width);
        srcWidget.getConfig().setSize(newSize);
        WidgetColumnFactory.this.control.requestViewUpdate(srcWidget);

        if (this.table.getSelectionModel().getSelectedItems().contains(srcWidget)) {
            this.table.getSelectionModel().getSelectedItems().forEach(widget -> {
                widget.getConfig().setSize(newSize);
                WidgetColumnFactory.this.control.requestViewUpdate(widget);
            });
        }

//        this.table.refresh();
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

//        System.out.println("SelectItem.A: " + this.table.getSelectionModel().getSelectedItems());
//        this.table.refresh();
//        System.out.println("SelectItem.B: " + this.table.getSelectionModel().getSelectedItems());
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

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setWidgetHeight(textField, (Widget) getTableRow().getItem());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                            addFocusRefreshListener(textField);

                            setGraphic(textField);
                        }else{
                            setGraphic(null);
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
