package org.jevis.jeconfig.plugin.dashboard.config2;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.ArrayList;
import java.util.List;

public class SideConfigPanel extends GridPane {


    private static final Logger logger = LogManager.getLogger(SideConfigPanel.class);
    private Accordion accordion = new Accordion();
    private DashboardControl control;
    private IntegerProperty integerProperty = new SimpleIntegerProperty(0);
    private ObjectProperty<Color> fgColorProperty = new SimpleObjectProperty(null);
    private ObjectProperty<Color> bgColorProperty = new SimpleObjectProperty(null);
    private DoubleProperty widthProperty = new SimpleDoubleProperty();
    private DoubleProperty heightProperty = new SimpleDoubleProperty();
    private DoubleProperty xPosProperty = new SimpleDoubleProperty();
    private DoubleProperty yPosProperty = new SimpleDoubleProperty();
    private DoubleProperty fontSizeProperty = new SimpleDoubleProperty();
    private BooleanProperty isUpdating = new SimpleBooleanProperty(false);
    private BooleanProperty shadowsProperty = new SimpleBooleanProperty(false);

    public SideConfigPanel(DashboardControl control) {
        super();
        this.control = control;
        setStyle("-fx-background-color: fcfcfc;");

        this.setPadding(new Insets(12, 12, 12, 12));
        List<TitledPane> tabs = new ArrayList<>();
        VBox accordionBox = new VBox();
        accordionBox.getChildren().addAll(buildMoveTab(), buildLayer(), buildColors());

        GridPane.setVgrow(accordionBox, Priority.ALWAYS);

        JFXButton switchSide = new JFXButton("", JEConfig.getImage("Arrow_BothDirections.png", 20, 20));
        switchSide.setStyle("-fx-background-color: transparent;");
        //switchSide.setBackground(Background.EMPTY);
        switchSide.setOnAction(event -> {
            if (control.getConfigSideProperty().get().equals(Side.RIGHT)) {
                control.configPanePos(Side.LEFT, this);
            } else if (control.getConfigSideProperty().get().equals(Side.LEFT)) {
                control.configPanePos(Side.RIGHT, this);
            }

        });

        this.add(switchSide, 0, 0);
        this.add(accordionBox, 0, 1);

        GridPane.setHalignment(switchSide, HPos.CENTER);
    }


    public void setLastSelectedWidget(Widget widget) {
        System.out.println("setLastSelectedWidget: " + widget);
        isUpdating.set(true);
        System.out.println("Layer in config: " + widget.getConfig().getLayer());
        integerProperty.setValue(widget.getConfig().getLayer());
        fgColorProperty.setValue(widget.getConfig().getFontColor());
        bgColorProperty.setValue(widget.getConfig().getBackgroundColor());

        System.out.println("update size: " + widget.getConfig().getSize());
        widthProperty.setValue(widget.getConfig().getSize().getWidth());
        heightProperty.setValue(widget.getConfig().getSize().getHeight());
        xPosProperty.setValue(widget.getConfig().getxPosition());
        yPosProperty.setValue(widget.getConfig().getyPosition());
        shadowsProperty.setValue(widget.getConfig().getShowShadow());
        fontSizeProperty.setValue(widget.getConfig().getFontSize());
        isUpdating.set(false);
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

        JFXComboBox<Integer> jfxComboBox = new JFXComboBox();
        jfxComboBox.getItems().addAll(1, 2, 3, 4, 5, 6);

        jfxComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!isUpdating.getValue()) {
                    control.layerSelected(Integer.parseInt(newValue.toString()));
                }

            } catch (Exception ex) {
                logger.error(ex, ex);
            }
        });

        integerProperty.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                try {
                    jfxComboBox.setValue(newValue.intValue());
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });

        });


        gp.add(label, 0, 0);
        gp.add(jfxComboBox, 1, 0);
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


        Label fColorLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontcolor"));
        Label bgColorLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.color"));
        Label shadowLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.shadow"));
        Label fontSizeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontsize"));


        ColorPickerAdv bgColorPicker = new ColorPickerAdv();
        ColorPickerAdv fColorPicker = new ColorPickerAdv();
        JFXCheckBox showShadowField = new JFXCheckBox();
        Spinner<Integer> fontSizeSpinner = new Spinner<Integer>(5, 50, 12);
        fontSizeSpinner.setMaxWidth(80);
        gp.add(fColorLabel, 0, 0);
        gp.add(fColorPicker, 1, 0);
        gp.add(bgColorLabel, 0, 1);
        gp.add(bgColorPicker, 1, 1);
        gp.add(shadowLabel, 0, 2);
        gp.add(showShadowField, 1, 2);
        gp.add(fontSizeLabel, 0, 3);
        gp.add(fontSizeSpinner, 1, 3);

        titledPane.setContent(gp);

        bgColorPicker.selectColorProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdating.get()) {
                control.bgColorSelected(newValue);
            }
        });

        fColorPicker.selectColorProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdating.get()) {
                control.fgColorSelected(newValue);
            }
        });

        fgColorProperty.addListener((observable, oldValue, newValue) -> {
            fColorPicker.setValue(newValue);
        });

        bgColorProperty.addListener((observable, oldValue, newValue) -> {
            bgColorPicker.setValue(newValue);
        });

        showShadowField.setOnAction(event -> {
            if (!isUpdating.get()) {
                control.shadowSelected(showShadowField.isSelected());
            }
        });


        shadowsProperty.addListener((observable, oldValue, newValue) -> {
            showShadowField.setSelected(newValue);
        });

        fontSizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!isUpdating.get()) {
                control.fontSizeSelected(newValue);
            }
        });

        fontSizeProperty.addListener((observable, oldValue, newValue) -> {
            fontSizeSpinner.getValueFactory().setValue(newValue.intValue());
        });

        return titledPane;
    }

    private TitledPane buildMoveTab() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText(I18n.getInstance().getString("plugin.dashboard.edit.general.tab.sizepos"));
        titledPane.setExpanded(true);

        GridPane gp = new GridPane();
        //gp.setStyle("-fx-background-color: orange;");
        gp.setPadding(new Insets(8, 8, 8, 8));
        gp.setHgap(8);
        gp.setVgap(8);


        Label widthLabel = new Label("Width:");
        Label heightLabel = new Label("Height:");
        Label moveLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.move"));
        Label xPosLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.xpos"));
        Label yPosLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.ypos"));
        JFXTextField widthText = new JFXTextField();
        JFXTextField heightText = new JFXTextField();
        JFXTextField xPosText = new JFXTextField();
        JFXTextField yPosText = new JFXTextField();
        widthText.setMaxWidth(80);
        heightText.setMaxWidth(80);
        xPosText.setMaxWidth(80);
        yPosText.setMaxWidth(80);

        double iconSize = 16;
        JFXButton leftButton = new JFXButton("", JEConfig.getImage("arrow_left.png", iconSize, iconSize));
        JFXButton rightButton = new JFXButton("", JEConfig.getImage("arrow_right.png", iconSize, iconSize));
        JFXButton downButton = new JFXButton("", JEConfig.getImage("arrow_down.png", iconSize, iconSize));
        JFXButton upButton = new JFXButton("", JEConfig.getImage("arrow_up.png", iconSize, iconSize));

        TextField pixels = new TextField(control.getActiveDashboard().getxGridInterval() + "");
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
            if (!isUpdating.get()) {
                control.positionSelected(Double.parseDouble(xPosText.getText()), -1);
            }
        });

        yPosText.setOnAction(event -> {
            if (!isUpdating.get()) {
                control.positionSelected(-1, Double.parseDouble(yPosText.getText()));
            }
        });

        widthText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isUpdating.get()) {
                    control.sizeSelected(Double.parseDouble(widthText.getText()), -1);
                }
            }
        });
        heightText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isUpdating.get()) {
                    control.sizeSelected(-1, Double.parseDouble(heightText.getText()));
                }
            }
        });

        heightProperty.addListener((observable, oldValue, newValue) -> {
            heightText.setText(newValue.toString());
        });
        widthProperty.addListener((observable, oldValue, newValue) -> {
            widthText.setText(newValue.toString());
        });

        xPosProperty.addListener((observable, oldValue, newValue) -> {
            xPosText.setText(newValue.toString());
        });

        yPosProperty.addListener((observable, oldValue, newValue) -> {
            yPosText.setText(newValue.toString());
        });

        titledPane.setContent(gp);
        return titledPane;
    }


}
