package org.jevis.jeconfig.plugin.dashboard.config2;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
    private IntegerProperty integerProperty = new SimpleIntegerProperty(1);
    private BooleanProperty isUpdating = new SimpleBooleanProperty(false);

    public SideConfigPanel(DashboardControl control) {
        super();
        this.control = control;
        setStyle("-fx-background-color: fcfcfc;");

        this.setPadding(new Insets(12, 12, 12, 12));
        List<TitledPane> tabs = new ArrayList<>();
        VBox accordionBox = new VBox();
        accordionBox.getChildren().addAll(buildMoveTab(), buildLayer(), buildColors());

        GridPane.setVgrow(accordionBox, Priority.ALWAYS);

        this.add(accordionBox, 0, 0);

    }

    public void setLastSelectedWidget(Widget widget) {
        isUpdating.set(true);
        integerProperty.setValue(widget.getConfig().getLayer());

        isUpdating.set(false);
    }


    private TitledPane buildLayer() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Layer");
        titledPane.setExpanded(true);

        GridPane gp = new GridPane();
        //gp.setStyle("-fx-background-color: orange;");
        gp.setPadding(new Insets(8, 8, 8, 8));
        gp.setHgap(8);
        gp.setVgap(8);

        Label label = new Label("Ebene:");
        JFXComboBox jfxComboBox = new JFXComboBox();
        jfxComboBox.getItems().addAll("1", "2", "3", "4", "5", "6");

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
                    jfxComboBox.selectionModelProperty().setValue(newValue);
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
        titledPane.setText("Farben");
        titledPane.setExpanded(true);

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(8, 8, 8, 8));
        gp.setHgap(8);
        gp.setVgap(8);


        Label fColorLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontcolor"));
        Label bgColorLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.color"));
        ColorPickerAdv bgColorPicker = new ColorPickerAdv();
        ColorPickerAdv fColorPicker = new ColorPickerAdv();
        gp.add(fColorLabel, 0, 0);
        gp.add(fColorPicker, 1, 0);
        gp.add(bgColorLabel, 0, 1);
        gp.add(bgColorPicker, 1, 1);

        titledPane.setContent(gp);

        bgColorPicker.selectColorProperty().addListener((observable, oldValue, newValue) -> {
            control.bgColorSelected(newValue);
        });

        fColorPicker.selectColorProperty().addListener((observable, oldValue, newValue) -> {
            control.fgColorSelected(newValue);
        });


        return titledPane;
    }

    private TitledPane buildMoveTab() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Position");
        titledPane.setExpanded(true);

        GridPane gp = new GridPane();
        //gp.setStyle("-fx-background-color: orange;");
        gp.setPadding(new Insets(8, 8, 8, 8));
        gp.setHgap(8);
        gp.setVgap(8);

        JFXButton leftButton = new JFXButton("", JEConfig.getImage("arrow_left.png", 12, 12));
        JFXButton rightButton = new JFXButton("", JEConfig.getImage("arrow_right.png", 12, 12));
        JFXButton downButton = new JFXButton("", JEConfig.getImage("arrow_down.png", 12, 12));
        JFXButton upButton = new JFXButton("", JEConfig.getImage("arrow_up.png", 12, 12));

        TextField pixels = new TextField(control.getActiveDashboard().getxGridInterval() + "");
        pixels.setMaxWidth(50);

        gp.add(upButton, 1, 0);
        gp.add(leftButton, 0, 1);
        gp.add(pixels, 1, 1);
        gp.add(rightButton, 2, 1);
        gp.add(downButton, 1, 2);

        GridPane.setHalignment(upButton, HPos.CENTER);
        GridPane.setHalignment(downButton, HPos.CENTER);

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
        titledPane.setContent(gp);
        return titledPane;
    }


}
