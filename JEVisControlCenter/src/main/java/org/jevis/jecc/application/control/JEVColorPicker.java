package org.jevis.jecc.application.control;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import org.jevis.jecc.application.Chart.ChartElements.ColorTable;

/**
 * Feature Idea: Store the last colors the user used while the application is running for all instances
 * Feature Idea: Store name corporate design colors in the DB
 */


public class JEVColorPicker extends ColorPicker {

    private static final ObservableList<Color> lastUsedColors = FXCollections.observableArrayList();

    public JEVColorPicker() {
        super();
        getCustomColors().addAll(ColorTable.color_list);
        //setOnAction(event -> addLastColor());
    }

    public JEVColorPicker(Color color) {
        super(color);
        getCustomColors().addAll(ColorTable.color_list);
        getCustomColors().addAll(getCustomColors());
        //setOnHiding(event -> addLastColor());
    }

    private synchronized void addLastColor(){
        if(!getLastUsedColors().contains(this.getValue())){
            Platform.runLater(() -> getLastUsedColors().add(this.getValue()));
        }

    }

    private ObservableList<Color> getLastUsedColors(){
        return lastUsedColors;
    }
}
