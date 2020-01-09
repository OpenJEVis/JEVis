package org.jevis.jeconfig.plugin.charts;

import com.jfoenix.svg.SVGGlyph;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class CalendarIcon {
    private Region icon;

    public CalendarIcon() {
        icon = new SVGGlyph(0,
                "calendar",
                "M320 384h128v128h-128zM512 384h128v128h-128zM704 384h128v128h-128zM128 "
                        + "768h128v128h-128zM320 768h128v128h-128zM512 768h128v128h-128zM320 "
                        + "576h128v128h-128zM512 576h128v128h-128zM704 576h128v128h-128zM128 "
                        + "576h128v128h-128zM832 0v64h-128v-64h-448v64h-128v-64h-128v1024h960v-1024h-128zM896"
                        + " 960h-832v-704h832v704z",
                null);
        setColor(Color.valueOf("#009688"));
        setSize(20d, 20d);
    }

    public void setColor(Color color) {
        ((SVGGlyph) icon).setFill(color);
    }

    public void setSize(Double width, Double height) {
        ((SVGGlyph) icon).setSize(width, height);
    }

    public Region getIcon() {
        return icon;
    }
}
