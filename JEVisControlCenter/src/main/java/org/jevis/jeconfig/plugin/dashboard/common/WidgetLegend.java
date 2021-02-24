package org.jevis.jeconfig.plugin.dashboard.common;

import com.sun.javafx.charts.Legend;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;

import java.lang.reflect.Field;

public class WidgetLegend extends Legend {


    public Legend.LegendItem buildHorizontalLegendItem(String name, Color color, Color fontcolor, double fontSize, JEVisObject obj, boolean isAlert, String alertText, boolean wrapText) {
        Rectangle r = new Rectangle();
        r.setX(0);
        r.setY(0);
        r.setWidth(12);
        r.setHeight(12);
        r.setArcWidth(20);
        r.setArcHeight(20);
        r.setStroke(color);
        r.setFill(color);

        Label alertLabel = new Label();
        alertLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        ImageView alertImage = JEConfig.getImage("Warning-icon.png", 12d, 12d);
        alertLabel.setGraphic(alertImage);
        alertLabel.setTooltip(new Tooltip(alertText));

        GridPane gridPane = new GridPane();
        gridPane.add(r, 0, 0);
        gridPane.setAlignment(Pos.BASELINE_LEFT);
        gridPane.setHgap(5);
        if (isAlert) {
            gridPane.add(alertLabel, 1, 0);
        }


        /**
         * TODO: replace this hack with an own implementation of an legend
         */
        Legend.LegendItem item = new Legend.LegendItem(name, gridPane);
        try {
            Field privateStringField = Legend.LegendItem.class.
                    getDeclaredField("label");
            privateStringField.setAccessible(true);
            Label label = (Label) privateStringField.get(item);
            label.setWrapText(wrapText);
//            label.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
            label.setTextFill(fontcolor);
            label.setFont(new Font(fontSize));
//            label.setMaxWidth(180);
            //label.setMaxWidth(140);
            label.setMinWidth(140);

            try {
                label.setOnMouseClicked(event -> {
                    if (event.isShiftDown() && event.getClickCount() == 2) {
                        JEConfig.openObjectInPlugin(ObjectPlugin.PLUGIN_NAME, obj);
                    }
                });
            } catch (Exception ex) {

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return item;
    }


    public Legend.LegendItem buildVerticalLegendItem(String name, Color color, Color fontcolor, double fontSize, JEVisObject obj, boolean isAlert, String alertText, boolean wrapText) {

        Rectangle r = new Rectangle();
        r.setX(0);
        r.setY(0);
        r.setWidth(12);
        r.setHeight(12);
        r.setArcWidth(20);
        r.setArcHeight(20);
        r.setStroke(color);
        r.setFill(color);

        Label alertLabel = new Label();
        alertLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        ImageView alertImage = JEConfig.getImage("Warning-icon.png", 12d, 12d);
        alertLabel.setGraphic(alertImage);
        alertLabel.setTooltip(new Tooltip(alertText));

        GridPane gridPane = new GridPane();
        gridPane.add(r, 0, 0);
        gridPane.setAlignment(Pos.BASELINE_LEFT);
        gridPane.setHgap(5);
        if (isAlert) {
            gridPane.add(alertLabel, 1, 0);
        }


        /**
         * TODO: replace this hack with an own implementation of an legend
         */
        Legend.LegendItem item = new Legend.LegendItem(name, gridPane);
        try {
            Field privateStringField = Legend.LegendItem.class.
                    getDeclaredField("label");
            privateStringField.setAccessible(true);
            Label label = (Label) privateStringField.get(item);
            label.setWrapText(wrapText);
//            label.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
            label.setTextFill(fontcolor);
            label.setFont(new Font(fontSize));
//            label.setMaxWidth(180);
            label.setMaxWidth(140);
            label.setMinWidth(140);

            try {
                label.setOnMouseClicked(event -> {
                    if (event.isShiftDown() && event.getClickCount() == 2) {
                        JEConfig.openObjectInPlugin(ObjectPlugin.PLUGIN_NAME, obj);
                    }
                });
            } catch (Exception ex) {

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return item;
    }
}
