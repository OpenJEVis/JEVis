package org.jevis.jeconfig.plugin.dashboard.config2;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.controls.FontPostureBox;
import org.jevis.jeconfig.plugin.dashboard.controls.FontWeightBox;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

public class SideConfigPanel extends GridPane {


    private static final Logger logger = LogManager.getLogger(SideConfigPanel.class);
    private final DashboardControl control;
    private final double iconSize = 16;
    private final JFXComboBox<Integer> layerComboBox = new JFXComboBox<>();
    private final ColorPickerAdv bgColorPicker = new ColorPickerAdv();
    private final ColorPickerAdv fColorPicker = new ColorPickerAdv();
    private final JFXCheckBox showShadowField = new JFXCheckBox();
    private final JFXCheckBox showValueField = new JFXCheckBox();
    private final Spinner<Integer> fontSizeSpinner = new Spinner<Integer>(5, 50, 12);
    private final FontWeightBox fontWeightBox = new FontWeightBox();
    private final FontPostureBox fontPostureBox = new FontPostureBox();
    private final JFXCheckBox fontUnderlined = new JFXCheckBox(I18n.getInstance().getString("plugin.dashboard.controls.fontunderlined"));
    private final Spinner<Integer> precisionSpinner = new Spinner<Integer>(0, 20, 2);
    private final Label fColorLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontcolor"));
    private final Label bgColorLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.color"));
    private final Label shadowLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.shadow"));
    private final Label fontSizeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontsize"));
    private final Label fontWeightLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontweight"));
    private final Label fontPostureLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontposture"));
    private final Label precisionLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.precision"));
    private final Label showValueLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.showvalue"));
    private final Label widthLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.width"));
    private final Label heightLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.height"));
    private final Label moveLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.move"));
    private final Label xPosLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.xpos"));
    private final Label yPosLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.ypos"));
    private final Label alignmentLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.alignment"));
    private final JFXTextField widthText = new JFXTextField();
    private final JFXTextField heightText = new JFXTextField();
    private final JFXTextField xPosText = new JFXTextField();
    private final JFXTextField yPosText = new JFXTextField();
    private final JFXButton leftButton = new JFXButton("", JEConfig.getImage("arrow_left.png", iconSize, iconSize));
    private final JFXButton rightButton = new JFXButton("", JEConfig.getImage("arrow_right.png", iconSize, iconSize));
    private final JFXButton downButton = new JFXButton("", JEConfig.getImage("arrow_down.png", iconSize, iconSize));
    private final JFXButton upButton = new JFXButton("", JEConfig.getImage("arrow_up.png", iconSize, iconSize));
    private final JFXButton switchSide = new JFXButton("", JEConfig.getImage("Arrow_BothDirections.png", 20, 20));
    private final JFXButton equalizeDataModelButton = new JFXButton(I18n.getInstance().getString("plugin.dashboard.edit.general.equalizeDataModel"));
    private final JFXComboBox<Pos> alignmentBox = new JFXComboBox<>(FXCollections.observableArrayList(Pos.TOP_LEFT, Pos.TOP_CENTER, Pos.TOP_RIGHT, Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER_RIGHT, Pos.BOTTOM_LEFT, Pos.BOTTOM_CENTER, Pos.BOTTOM_RIGHT));
    private final JFXTextField titleText = new JFXTextField();
    private final TextField pixels = new TextField("25");
    private final GridPane dataPointConfigPane = new GridPane();
    //-----------
    //ObservableList<String> dataItems = FXCollections.observableArrayList("One", "Two", "Three", "Four", "Five", "Six","Seven", "Eight", "Nine", "Ten");
    JFXComboBox<JEVisObject> objectSelectionBox = new JFXComboBox<>();
    ListView<JEVisObject> selectedObjectsListView = new ListView();
    ObservableList<JEVisObject> dataItems = FXCollections.observableArrayList();
    FilteredList<JEVisObject> filteredItems = new FilteredList<>(dataItems, p -> true);
    FlowPane dataEditor = new FlowPane();
    private boolean isUpdating = false;
    private Widget selectedWidget = null;


    public SideConfigPanel(DashboardControl control) {
        super();
        this.control = control;
        setStyle("-fx-background-color: ffffff;"); //fcfcfc


        this.setPadding(new Insets(12, 12, 12, 12));
        VBox accordionBox = new VBox();
        accordionBox.setStyle("-fx-background-color: ffffff;");
        accordionBox.getChildren().addAll(buildName(), buildMoveTab(), buildLayer(), buildColors(), buildDataSourceTab());

        //this.add(switchSide, 0, 0);
        this.add(accordionBox, 0, 1);

        GridPane.setHalignment(switchSide, HPos.CENTER);
        GridPane.setVgrow(accordionBox, Priority.ALWAYS);

        switchSide.setStyle("-fx-background-color: transparent;");
        switchSide.setOnAction(event -> {
            if (control.getConfigSideProperty().get().equals(Side.RIGHT)) {
                control.configPanePos(Side.LEFT, this);
            } else if (control.getConfigSideProperty().get().equals(Side.LEFT)) {
                control.configPanePos(Side.RIGHT, this);
            }

        });

        /* add experiment
        JEVisDataSource ds = control.getDataSource();

        try {
            JEVisClass dataClass = ds.getJEVisClass(EnvidatecClasses.Data.name);
            List<JEVisObject> datas = ds.getObjects(dataClass, false);
            datas.forEach(jeVisObject -> {
                try {
                    dataItems.add(jeVisObject);
                    // dataItems.add(objectRelations.getObjectPath(jeVisObject) +  + jeVisObject.getName());
                } catch (Exception ex) {
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
        }
**/
    }


    public void setLastSelectedWidget(Widget widget) {
        isUpdating = true;
        this.selectedWidget = widget;

        if (widget != null) {
            this.setDisable(false);
            layerComboBox.setValue(widget.getConfig().getLayer());
            fColorPicker.setValue(widget.getConfig().getFontColor());
            bgColorPicker.setValue(widget.getConfig().getBackgroundColor());
            showShadowField.setSelected(widget.getConfig().getShowShadow());
            showValueField.setSelected(widget.getConfig().getShowValue());
            fontSizeSpinner.getValueFactory().setValue(widget.getConfig().getFontSize().intValue());
            fontWeightBox.getSelectionModel().select(widget.getConfig().getFontWeight());
            fontPostureBox.getSelectionModel().select(widget.getConfig().getFontPosture());
            fontUnderlined.setSelected(widget.getConfig().getFontUnderlined());
            widthText.setText(widget.getConfig().getSize().getWidth() + "");
            xPosText.setText(widget.getConfig().getxPosition() + "");
            yPosText.setText(widget.getConfig().getyPosition() + "");
            heightText.setText(widget.getConfig().getSize().getHeight() + "");
            pixels.setText(control.getActiveDashboard().getxGridInterval().intValue() + "");
            alignmentBox.getSelectionModel().select(widget.getConfig().getTitlePosition());
            titleText.setText(widget.getConfig().getTitle());

            selectedObjectsListView.getItems().clear();
            try {
                DataModelDataHandler sampleHandler = new DataModelDataHandler(
                        this.control.getDataSource(), this.control,
                        widget.getConfig(), widget.getId());

                sampleHandler.getChartDataRows().forEach(chartDataRow -> {

                    Platform.runLater(() -> {
                        try {
                            selectedObjectsListView.getItems().add(chartDataRow.getObject());
                        } catch (Exception ex) {

                        }
                    });

                });
            } catch (Exception ex) {

            }
        } else {
            this.setDisable(true);
            //layerComboBox.setValue();
            //fColorPicker.setValue(widget.getConfig().getFontColor());
            //bgColorPicker.setValue(widget.getConfig().getBackgroundColor());
            //showShadowField.setSelected(widget.getConfig().getShowShadow());
            //showValueField.setSelected(widget.getConfig().getShowValue());
            //fontSizeSpinner.getValueFactory().setValue(widget.getConfig().getFontSize().intValue());
            widthText.setText("");
            xPosText.setText("");
            yPosText.setText("");
            heightText.setText("");
            pixels.setText(control.getActiveDashboard().getxGridInterval().intValue() + "");
            //alignmentBox.getSelectionModel().select();
            titleText.setText("");

            selectedObjectsListView.getItems().clear();
        }

        isUpdating = widget == null;//Workaround to stop all events from happen


    }


    private TitledPane buildLayer() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText(I18n.getInstance().getString("plugin.dashboard.edit.general.layer"));
        titledPane.setExpanded(true);

        GridPane gp = new GridPane();
        //gp.setStyle("-fx-background-color: orange;");
        gp.setPadding(new Insets(8, 8, 8, 8));
        gp.setHgap(8);
        gp.setVgap(8);

        Label label = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.layer") + ":");


        layerComboBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        layerComboBox.setOnAction(event -> {
            if (!isUpdating) {
                control.layerSelected(Integer.parseInt(layerComboBox.getValue().toString()));
            }
        });


        gp.add(label, 0, 0);
        gp.add(layerComboBox, 1, 0);
        titledPane.setContent(gp);

        return titledPane;
    }

    private TitledPane buildName() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText(I18n.getInstance().getString("plugin.dashboard.edit.general.general"));
        titledPane.setExpanded(true);

        GridPane gp = new GridPane();
        //gp.setStyle("-fx-background-color: orange;");
        gp.setPadding(new Insets(8, 8, 8, 8));
        gp.setHgap(8);
        gp.setVgap(8);

        Label label = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.name"));

        titleText.setOnAction(event -> {
            if (!isUpdating) {
                control.setWidgetTitle(titleText.getText());
            }
        });

        gp.add(label, 0, 0);
        gp.add(titleText, 1, 0);
        GridPane.setHgrow(titleText, Priority.ALWAYS);
        titledPane.setContent(gp);

        return titledPane;
    }

    private TitledPane buildColors() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText(I18n.getInstance().getString("plugin.dashboard.edit.general.coloreffect"));
        titledPane.setExpanded(true);

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(8, 8, 8, 8));
        gp.setHgap(8);
        gp.setVgap(8);

        fontSizeSpinner.setMaxWidth(80);
        precisionSpinner.setMaxWidth(80);
        int row = 0;
        gp.add(fColorLabel, 0, row);
        gp.add(fColorPicker, 1, row);
        row++;

        gp.add(bgColorLabel, 0, row);
        gp.add(bgColorPicker, 1, row);
        row++;

        gp.add(shadowLabel, 0, row);
        gp.add(showShadowField, 1, row);
        row++;

        gp.add(fontSizeLabel, 0, row);
        gp.add(fontSizeSpinner, 1, row);
        row++;

        gp.add(fontWeightLabel, 0, row);
        gp.add(fontWeightBox, 1, row);
        row++;

        gp.add(fontPostureLabel, 0, row);
        gp.add(fontPostureBox, 1, row);
        row++;

        gp.add(fontUnderlined, 1, row);
        row++;

        gp.add(precisionLabel, 0, row);
        gp.add(precisionSpinner, 1, row);
        row++;

        gp.add(showValueLabel, 0, row);
        gp.add(showValueField, 1, row);
        row++;

        gp.add(alignmentLabel, 0, row);
        gp.add(alignmentBox, 1, row);
        //gp.add(equalizeDataModelButton, 0, 7, 2, 1);

        titledPane.setContent(gp);

        bgColorPicker.selectColorProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdating) {
                control.bgColorSelected(newValue);
            }
        });

        fColorPicker.selectColorProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdating) {
                control.fgColorSelected(newValue);
            }
        });

        fontSizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdating) {
                control.fontSizeSelected(newValue);
            }
        });

        fontWeightBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdating) {
                control.fontWeightSelected(newValue);
            }
        });

        fontPostureBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdating) {
                control.fontPostureSelected(newValue);
            }
        });

        fontUnderlined.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdating) {
                control.fontUnderlinedSelected(newValue);
            }
        });

        precisionSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdating) {
                control.decimalsSelected(newValue.intValue());
            }
        });

        showShadowField.setOnAction(event -> {
            if (!isUpdating) {
                control.shadowSelected(showShadowField.isSelected());
            }
        });

        showValueField.setOnAction(event -> {
            if (!isUpdating) {
                control.showValueSelected(showValueField.isSelected());
            }
        });


        alignmentBox.setPrefWidth(100);
        alignmentBox.setMinWidth(100);
        Callback<ListView<Pos>, ListCell<Pos>> cellFactory = new Callback<ListView<Pos>, ListCell<Pos>>() {
            @Override
            public ListCell<Pos> call(ListView<Pos> param) {
                final ListCell<Pos> cell = new ListCell<Pos>() {

                    @Override
                    protected void updateItem(Pos item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            switch (item) {
                                case CENTER:
                                    setText(I18n.getInstance().getString("javafx.pos.center"));
                                    break;
                                case CENTER_LEFT:
                                    setText(I18n.getInstance().getString("javafx.pos.centerleft"));
                                    break;
                                case CENTER_RIGHT:
                                    setText(I18n.getInstance().getString("javafx.pos.centerright"));
                                    break;
                                case BOTTOM_RIGHT:
                                    setText(I18n.getInstance().getString("javafx.pos.bottomright"));
                                    break;
                                case BOTTOM_LEFT:
                                    setText(I18n.getInstance().getString("javafx.pos.bottomleft"));
                                    break;
                                case BOTTOM_CENTER:
                                    setText(I18n.getInstance().getString("javafx.pos.bottomcenter"));
                                    break;
                                /**
                                 case BASELINE_LEFT:
                                 setText(I18n.getInstance().getString("javafx.pos.center"));
                                 break;
                                 case BASELINE_RIGHT:
                                 setText(I18n.getInstance().getString("javafx.pos.center"));
                                 break;
                                 case BASELINE_CENTER:
                                 setText(I18n.getInstance().getString("javafx.pos.center"));
                                 break;
                                 **/
                                case TOP_LEFT:
                                    setText(I18n.getInstance().getString("javafx.pos.topleft"));
                                    break;
                                case TOP_RIGHT:
                                    setText(I18n.getInstance().getString("javafx.pos.topright"));
                                    break;
                                case TOP_CENTER:
                                    setText(I18n.getInstance().getString("javafx.pos.topcenter"));
                                    break;
                                default:
                                    setText(item.toString());


                            }


                        } else {
                            setText(null);
                        }
                    }
                };

                return cell;
            }
        };

        alignmentBox.setCellFactory(cellFactory);
        alignmentBox.setButtonCell(cellFactory.call(null));
        alignmentBox.setOnAction(event -> {
            if (!isUpdating) {
                control.alignSelected(alignmentBox.getSelectionModel().getSelectedItem());
            }
        });


        return titledPane;
    }

    private TitledPane buildMoveTab() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText(I18n.getInstance().getString("plugin.dashboard.edit.general.tab.sizepos"));
        titledPane.setExpanded(true);

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(8, 8, 8, 8));
        gp.setHgap(8);
        gp.setVgap(8);

        widthText.setMaxWidth(80);
        heightText.setMaxWidth(80);
        xPosText.setMaxWidth(80);
        yPosText.setMaxWidth(80);
        pixels.setMaxWidth(40);

        GridPane gpMove = new GridPane();
        gpMove.setPadding(new Insets(4));
        gpMove.setHgap(5);
        gpMove.setVgap(5);
        gpMove.add(upButton, 1, 0);
        gpMove.add(leftButton, 0, 1);
        gpMove.add(pixels, 1, 1);
        gpMove.add(rightButton, 2, 1);
        gpMove.add(downButton, 1, 2);


        gp.add(heightLabel, 0, 0);
        gp.add(heightText, 1, 0);
        gp.add(widthLabel, 0, 1);
        gp.add(widthText, 1, 1);
        gp.add(xPosLabel, 0, 2);
        gp.add(xPosText, 1, 2);
        gp.add(yPosLabel, 0, 3);
        gp.add(yPosText, 1, 3);
        gp.add(moveLabel, 0, 4);
        gp.add(gpMove, 1, 4);

        GridPane.setHalignment(upButton, HPos.CENTER);
        GridPane.setHalignment(downButton, HPos.CENTER);
        GridPane.setHalignment(gpMove, HPos.CENTER);
        GridPane.setValignment(moveLabel, VPos.CENTER);

        upButton.setOnAction(event -> {
            control.moveSelected(Double.parseDouble(pixels.getText()), 0, 0, 0);
        });
        downButton.setOnAction(event -> {
            control.moveSelected(0, Double.parseDouble(pixels.getText()), 0, 0);
        });

        leftButton.setOnAction(event -> {
            control.moveSelected(0, 0, Double.parseDouble(pixels.getText()), 0);
        });
        rightButton.setOnAction(event -> {
            control.moveSelected(0, 0, 0, Double.parseDouble(pixels.getText()));
        });
        xPosText.setOnAction(event -> {
            if (!isUpdating) {
                control.positionSelected(Double.parseDouble(xPosText.getText()), -1);
            }
        });

        yPosText.setOnAction(event -> {
            if (!isUpdating) {
                control.positionSelected(-1, Double.parseDouble(yPosText.getText()));
            }
        });

        widthText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isUpdating) {
                    control.sizeSelected(Double.parseDouble(widthText.getText()), -1);
                }
            }
        });
        heightText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isUpdating) {
                    control.sizeSelected(-1, Double.parseDouble(heightText.getText()));
                }
            }
        });

        titledPane.setContent(gp);
        return titledPane;
    }


    private TitledPane buildDataSourceTab() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText(I18n.getInstance().getString("plugin.dashboard.edit.general.tab.data"));
        titledPane.setExpanded(true);

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(8, 8, 8, 8));
        gp.setHgap(8);
        gp.setVgap(8);

        widthText.setMaxWidth(80);
        heightText.setMaxWidth(80);
        xPosText.setMaxWidth(80);
        yPosText.setMaxWidth(80);
        pixels.setMaxWidth(40);

        JEVisDataSource ds = control.getDataSource();
        ObjectRelations objectRelations = new ObjectRelations(ds);
        JFXTextField filterTextField = new JFXTextField();
        filterTextField.setPromptText("Type to filter...");
        filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            //System.out.println("newValue: " + newValue);
            if (newValue.isEmpty()) {
                // System.out.println("Dont filter");
                filteredItems.setPredicate(item -> {
                    return true;
                });
            } else {
                // System.out.println("Filter");
                filteredItems.setPredicate(item -> {
                    //TODo split space and make it and
                    String fullName = (objectRelations.getObjectPath(item) + objectRelations.getRelativePath(item) + item.getName()).toUpperCase();
                    boolean allMatch = true;
                    for (String s : newValue.toUpperCase().split(" ")) {
                        if (!fullName.contains(s.toUpperCase())) {
                            allMatch = false;
                        }
                        //System.out.println("All match: " + allMatch);
                        return allMatch;
                    }
                    return false;
                    /*
                    if (fullName.contains(newValue.toUpperCase())) {
                        return true;
                    } else {
                        return false;
                    }*/

                });
            }


        });

        objectSelectionBox.setEditable(false);
        //objectSelectionBox.setPromptText("Type to filter...");

        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<javafx.scene.control.ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(javafx.scene.control.ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject jeVisObject, boolean empty) {
                        super.updateItem(jeVisObject, empty);
                        //System.out.println("selListBox: " + jeVisObject + "    empty: " + empty);
                        if (empty || jeVisObject == null) {
                            setText(null);
                        } else {
                            setText(objectRelations.getObjectPath(jeVisObject) + objectRelations.getRelativePath(jeVisObject) + jeVisObject.getName());
                            setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
                            //setText(jeVisObject.getName());
                            try {
                                setTooltip(new Tooltip(
                                        objectRelations.getObjectPath(jeVisObject)
                                                + objectRelations.getRelativePath(jeVisObject)
                                                + jeVisObject.getName()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                };
            }
        };
        //objectSelectionBox.

        objectSelectionBox.setCellFactory(cellFactory);
        objectSelectionBox.setButtonCell(cellFactory.call(null));
        objectSelectionBox.setItems(filteredItems);


        objectSelectionBox.setMaxWidth(250);
        selectedObjectsListView.setPrefHeight(100);
        selectedObjectsListView.setCellFactory(new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);
                        //System.out.println("Update Item: " + item + "   is empty: " + empty);
                        if (item != null) {
                            setText(item.getName());
                            try {
                                setTooltip(new Tooltip(objectRelations.getObjectPath(item) + objectRelations.getRelativePath(item) + item.getName()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            setText(null);
                        }

                    }
                };
            }
        });

        Button addButton = new Button("add");
        addButton.setOnAction(event -> {
            Platform.runLater(() -> {
                Object test = objectSelectionBox.getSelectionModel().getSelectedItem();
                logger.debug("test.class: " + test.getClass());
                logger.debug("intencof: " + (objectSelectionBox.getSelectionModel().getSelectedItem() instanceof JEVisObject));
                if (!objectSelectionBox.getSelectionModel().isEmpty() && objectSelectionBox.getSelectionModel().getSelectedItem() instanceof JEVisObject) {
                    logger.debug("add: " + objectSelectionBox.getSelectionModel().getSelectedItem());
                    selectedObjectsListView.getItems().add(objectSelectionBox.getSelectionModel().getSelectedItem());
                    logger.debug("Done");
                } else {
                    logger.debug("wrong add: " + objectSelectionBox.getSelectionModel().getSelectedItem());
                }
            });

        });

        selectedObjectsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            logger.debug("selectedObjectsListView: " + newValue);
            // todo add gui elements by widget support type
            // add to dataModel

            try {
                DataModelDataHandler sampleHandler = new DataModelDataHandler(
                        this.control.getDataSource(),
                        this.control,
                        this.selectedWidget.getConfig(),
                        this.selectedWidget.getId());


                sampleHandler.getChartDataRows().forEach(chartDataRow -> {

                    if (chartDataRow.getObject().equals(newValue)) {
                        Platform.runLater(() -> {
                            TextField color = new TextField(chartDataRow.getUnit().toString());
                            dataPointConfigPane.add(color, 0, 0);
                        });

                    } else {
                    }
                });

            } catch (Exception ex) {

            }
        });


        equalizeDataModelButton.setOnAction(event -> {
            control.equalizeDataModel();
        });

        /**
         GridPane gpMove = new GridPane();
         gpMove.setPadding(new Insets(4));
         gpMove.setHgap(5);
         gpMove.setVgap(5);
         gpMove.add(objectSelectionBox, 0, 1);
         gpMove.add(addButton, 1, 1);
         gpMove.add(selectedObjectsListView, 2, 1);
         */
        //gp.add(filterTextField, 0, 0, 2, 1);
        //gp.add(objectSelectionBox, 0, 1);
        //gp.add(addButton, 1, 1);
        gp.add(selectedObjectsListView, 0, 2, 2, 1);
        if (JEConfig.getExpert()) {
            gp.add(equalizeDataModelButton, 0, 3, 1, 1);
        }

        //gp.add(dataEditor, 0, 3, 2, 1);
        //gp.add(dataPointConfigPane, 0, 4, 2, 1);

        GridPane.setHalignment(filterTextField, HPos.LEFT);
        GridPane.setHalignment(objectSelectionBox, HPos.LEFT);
        GridPane.setHalignment(addButton, HPos.RIGHT);
        GridPane.setHalignment(selectedObjectsListView, HPos.CENTER);


        titledPane.setContent(gp);
        return titledPane;
    }

}
