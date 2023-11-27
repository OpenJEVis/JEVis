/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jecc.application.unit;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UnitChooser {

    private final ObjectProperty<Unit> unit = new SimpleObjectProperty<>();
    HBox _root = new HBox();

    double HEIGHT = 20;
    int FONT_SIZE = 12;

    public UnitChooser(Unit unit) {
        List<Unit> units = new ArrayList<>();
        units.add(SI.WATT);
        units.add(SI.AMPERE);
        units.add(SI.HERTZ);

        VBox spinner = new VBox();
        final Button prefixUp = new Button("+");
        Button prefixDown = new Button("-");
        prefixDown.setId("prefixdown");
        prefixUp.setId("prefixup");

        Label prefix = new Label(" k ");
        prefix.setId("prefix");

        ComboBox unitBox = buildUnitBox(units);
        unitBox.setStyle("-fx-background-radius: 0 10 10 0;");

        prefixDown.getStylesheets().add("/styles/unitchooser.css");
        prefixUp.getStylesheets().add("/styles/unitchooser.css");
        prefix.getStylesheets().add("/styles/unitchooser.css");
        unitBox.getStylesheets().add("/styles/unitchooser.css");

        spinner.setMaxHeight(HEIGHT);
        prefixDown.setMaxHeight(HEIGHT / 2);
        prefixUp.setMaxHeight(HEIGHT / 2);
        prefix.setMaxHeight(HEIGHT);
        unitBox.setMaxHeight(HEIGHT);

        spinner.setMinHeight(HEIGHT);
        prefixDown.setMinHeight(HEIGHT / 2);
        prefixUp.setMinHeight(HEIGHT / 2);
        prefix.setMinHeight(HEIGHT);
        unitBox.setMinHeight(HEIGHT);

        unitBox.setPrefWidth(60);
        prefixUp.setPrefWidth(15);
        prefixDown.setPrefWidth(15);

        prefixDown.setFont(new Font(FONT_SIZE / 2));
        prefixUp.setFont(new Font(FONT_SIZE / 2));
        prefix.setFont(new Font(FONT_SIZE));

//        prefixDown.setStyle(UnitChooser.class.getResource(null));
//        prefixUp.setPrefSize(100, 100);
        /* top-left, top-right, bottom-right, and bottom-left corners, in that order. */
//        prefixUp.setStyle("-fx-background-color: linear-gradient(#ff5400, #be1d00);"
//                + "    -fx-background-radius: 30;"
//                + "    -fx-background-insets: 0;"
//                + "    -fx-text-fill: white;");
//        prefixDown.setStyle("-fx-background-radius-bottom-left-radius: 40px;");
        Separator sep = new Separator(Orientation.HORIZONTAL);
//        sep.setPadding(new Insets(0));
//        sep.setStyle("-fx-background-insets: 0;");
        spinner.getChildren().setAll(prefixUp, prefixDown);

        _root.getChildren().setAll(spinner, prefix, unitBox);
    }

    public UnitChooser(Unit unit, int nouse) {
        List<Unit> units = new ArrayList<>();
        units.add(SI.WATT);
        units.add(SI.AMPERE);
        units.add(SI.HERTZ);

        Label prefix = new Label(" k ");
        prefix.setId("prefix");

        ComboBox unitBox = buildUnitBox(units);
        unitBox.setStyle("-fx-background-radius: 0 10 10 0;");

        List<String> prefixList = new ArrayList<>();
        prefixList.add("k");
//        ObservableList<String> options2 = FXCollections.observableArrayList(prefixList);
        ComboBox uprefixBox = buildPrefixBox(prefixList);
        uprefixBox.setId("prefixBox");
        uprefixBox.setStyle("-fx-background-radius: 10 0 0 10;");

        prefix.getStylesheets().add("/styles/unitchooser.css");
        unitBox.getStylesheets().add("/styles/unitchooser.css");
        uprefixBox.getStylesheets().add("/styles/unitchooser.css");

        uprefixBox.setMaxHeight(HEIGHT);

        unitBox.setMaxHeight(HEIGHT);

        uprefixBox.setMinHeight(HEIGHT);
        unitBox.setMinHeight(HEIGHT);

        unitBox.setPrefWidth(60);
        uprefixBox.setPrefWidth(30);

        prefix.setFont(new Font(FONT_SIZE));

        _root.getChildren().setAll(uprefixBox, unitBox);
    }

    /**
     * @param units
     * @return
     */
    private ComboBox buildUnitBox(List<Unit> units) {

        ObservableList<Unit> options = FXCollections.observableArrayList(units);

        final ComboBox<Unit> comboBox = new ComboBox<Unit>(options);

        //TODO JFX17

        comboBox.setConverter(new StringConverter<Unit>() {
            @Override
            public String toString(Unit object) {
                return object.toString();
            }

            @Override
            public Unit fromString(String string) {
                return comboBox.getItems().get(comboBox.getSelectionModel().getSelectedIndex());
            }
        });

        //TODO: load default language from config file or so
        comboBox.getSelectionModel().selectFirst();

        comboBox.setMaxWidth(Integer.MAX_VALUE);//workaround

        return comboBox;

    }

    private ComboBox buildPrefixBox(List<String> units) {

        ObservableList<String> options = FXCollections.observableArrayList(units);

        final ComboBox<String> comboBox = new ComboBox<String>(options);

        //TODO JFX17

        //no converter needed??

        //TODO: load default language from config file or so
        comboBox.getSelectionModel().selectFirst();

        comboBox.setMaxWidth(Integer.MAX_VALUE);//workaround

        return comboBox;

    }

    public Unit getUnit() {
        return unit.get();
    }

    public void setUnit(Unit newColor) {
        unit.set(newColor);
    }

    public ObjectProperty<Unit> unitProperty() {
        return unit;
    }

    public void setValue(Double value) {

    }

    public Node getGraphic() {
        return _root;
    }

}
