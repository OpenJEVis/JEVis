package org.jevis.jeconfig.plugin.dashboard.config2;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

public class SideConfigPanel extends GridPane {


    private static final Logger logger = LogManager.getLogger(SideConfigPanel.class);
    private DashboardControl control;
    private boolean isUpdating = false;
    private double iconSize = 16;

    private JFXComboBox<Integer> layerComboBox = new JFXComboBox();
    private ColorPickerAdv bgColorPicker = new ColorPickerAdv();
    private ColorPickerAdv fColorPicker = new ColorPickerAdv();
    private JFXCheckBox showShadowField = new JFXCheckBox();
    private JFXCheckBox showValueField = new JFXCheckBox();
    private Spinner<Integer> fontSizeSpinner = new Spinner<Integer>(5, 50, 12);
    private Spinner<Integer> precisionSpinner = new Spinner<Integer>(0, 20, 2);
    private Label fColorLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontcolor"));
    private Label bgColorLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.color"));
    private Label shadowLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.shadow"));
    private Label fontSizeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontsize"));
    private Label precisionLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.precision"));
    private Label showValueLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.showvalue"));
    private Label widthLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.width"));
    private Label heightLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.height"));
    private Label moveLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.move"));
    private Label xPosLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.xpos"));
    private Label yPosLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.ypos"));
    private Label alignmentLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.alignment"));
    private JFXTextField widthText = new JFXTextField();
    private JFXTextField heightText = new JFXTextField();
    private JFXTextField xPosText = new JFXTextField();
    private JFXTextField yPosText = new JFXTextField();
    private JFXButton leftButton = new JFXButton("", JEConfig.getImage("arrow_left.png", iconSize, iconSize));
    private JFXButton rightButton = new JFXButton("", JEConfig.getImage("arrow_right.png", iconSize, iconSize));
    private JFXButton downButton = new JFXButton("", JEConfig.getImage("arrow_down.png", iconSize, iconSize));
    private JFXButton upButton = new JFXButton("", JEConfig.getImage("arrow_up.png", iconSize, iconSize));
    private JFXButton switchSide = new JFXButton("", JEConfig.getImage("Arrow_BothDirections.png", 20, 20));
    private JFXButton equalizeDataModelButton = new JFXButton(I18n.getInstance().getString("plugin.dashboard.edit.general.equalizeDataModel"));
    private TextField pixels = new TextField("25.0");
    private JFXComboBox<Pos> alignmentBox = new JFXComboBox<>(FXCollections.observableArrayList(Pos.TOP_LEFT, Pos.TOP_CENTER, Pos.TOP_RIGHT, Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER_RIGHT, Pos.BOTTOM_LEFT, Pos.BOTTOM_CENTER, Pos.BOTTOM_RIGHT));


    public SideConfigPanel(DashboardControl control) {
        super();
        this.control = control;
        setStyle("-fx-background-color: ffffff;"); //fcfcfc


        this.setPadding(new Insets(12, 12, 12, 12));
        VBox accordionBox = new VBox();
        accordionBox.setStyle("-fx-background-color: ffffff;");
        accordionBox.getChildren().addAll(buildMoveTab(), buildLayer(), buildColors());

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

    }


    public void setLastSelectedWidget(Widget widget) {
        isUpdating = true;

        layerComboBox.setValue(widget.getConfig().getLayer());
        fColorPicker.setValue(widget.getConfig().getFontColor());
        bgColorPicker.setValue(widget.getConfig().getBackgroundColor());
        showShadowField.setSelected(widget.getConfig().getShowShadow());
        showValueField.setSelected(widget.getConfig().getShowValue());
        fontSizeSpinner.getValueFactory().setValue(widget.getConfig().getFontSize().intValue());
        widthText.setText(widget.getConfig().getSize().getWidth() + "");
        xPosText.setText(widget.getConfig().getxPosition() + "");
        yPosText.setText(widget.getConfig().getyPosition() + "");
        heightText.setText(widget.getConfig().getSize().getHeight() + "");
        pixels.setText(control.getActiveDashboard().getxGridInterval() + "");
        alignmentBox.getSelectionModel().select(widget.getConfig().getTitlePosition());

        isUpdating = false;
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

        gp.add(fColorLabel, 0, 0);
        gp.add(fColorPicker, 1, 0);
        gp.add(bgColorLabel, 0, 1);
        gp.add(bgColorPicker, 1, 1);
        gp.add(shadowLabel, 0, 2);
        gp.add(showShadowField, 1, 2);
        gp.add(fontSizeLabel, 0, 3);
        gp.add(fontSizeSpinner, 1, 3);
        gp.add(precisionLabel, 0, 4);
        gp.add(precisionSpinner, 1, 4);
        gp.add(showValueLabel, 0, 5);
        gp.add(showValueField, 1, 5);
        gp.add(alignmentLabel, 0, 6);
        gp.add(alignmentBox, 1, 6);
        gp.add(equalizeDataModelButton, 0, 7, 2, 1);

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

        equalizeDataModelButton.setOnAction(event -> {
            control.equalizeDataModel();
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


}
