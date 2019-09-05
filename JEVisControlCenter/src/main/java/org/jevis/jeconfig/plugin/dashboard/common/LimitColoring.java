package org.jevis.jeconfig.plugin.dashboard.common;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LimitColoring {

    public static String JNODE_NAME = "limitColor";
    private static final Logger logger = LogManager.getLogger(LimitColoring.class);
    private final ObjectProperty<Color> colorUpperLimit = new SimpleObjectProperty<>(Color.INDIANRED);
    private final ObjectProperty<Color> colorLowerLimit = new SimpleObjectProperty<>(Color.LIMEGREEN);
    private final ObjectProperty<Color> colorDefault = new SimpleObjectProperty<>(Color.LIMEGREEN);


    private final ObjectProperty<Color> fontColorUpperLimit = new SimpleObjectProperty<>(Color.BLACK);
    private final ObjectProperty<Color> fontColorLowerLimit = new SimpleObjectProperty<>(Color.BLACK);
    private final ObjectProperty<Color> fontColorDefault = new SimpleObjectProperty<>(Color.BLACK);

    private Double upperLimit = 0d;
    private Double lowerLimit = 0d;


    public LimitColoring(JsonNode configNode, Color defaultFontColor, Color defaultBackGroundColor) {
        this.fontColorDefault.setValue(defaultFontColor);
        this.colorDefault.setValue(defaultBackGroundColor);

        try {
            this.upperLimit = (configNode.get("upperLimit").asDouble(0d));
        } catch (Exception ex) {
            logger.error(ex);
        }

        try {
            this.lowerLimit = (configNode.get("lowerLimit").asDouble(0d));
        } catch (Exception ex) {
            logger.error(ex);
        }


    }

    public void formateLabel(Label label, Double value) {
        Background bgColor;
        Color fontColor;


        if (value.isNaN() || value.isInfinite() || value >= this.upperLimit) {
            bgColor = new Background(new BackgroundFill(this.colorUpperLimit.get(), CornerRadii.EMPTY, Insets.EMPTY));
            fontColor = this.fontColorUpperLimit.get();
        } else if (value <= this.lowerLimit) {
            bgColor = new Background(new BackgroundFill(this.colorLowerLimit.get(), CornerRadii.EMPTY, Insets.EMPTY));
            fontColor = this.fontColorLowerLimit.get();
        } else {
            bgColor = new Background(new BackgroundFill(this.colorDefault.get(), CornerRadii.EMPTY, Insets.EMPTY));
            fontColor = this.colorDefault.getValue();
        }

        label.setBackground(bgColor);
        label.setTextFill(fontColor);
    }


}
