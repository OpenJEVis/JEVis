/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.unit;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.ws.json.JsonUnit;
import org.jevis.jeconfig.application.unit.SimpleTreeUnitChooser;

import javax.measure.MetricPrefix;
import javax.measure.Prefix;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UnitSelectUI {

    private final TextField symbolField = new TextField();
    private static final Logger logger = LogManager.getLogger(UnitSelectUI.class);
    private final ComboBox<Prefix> prefixBox;
    private final Button changeBaseUnit = new Button();//new Button("Basic Unit");
    private JEVisUnit jeVisUnit;
    //workaround
    private final BooleanProperty valueChangedProperty = new SimpleBooleanProperty(false);

    public UnitSelectUI(JEVisDataSource ds, JEVisUnit unit) {
        final JEVisUnit.Prefix prefix = unit.getPrefix();
        jeVisUnit = unit;
        List<Prefix> list = new ArrayList<>();
        list.add(null);
        Collections.addAll(list, MetricPrefix.values());
        prefixBox = new ComboBox<>(FXCollections.observableArrayList(MetricPrefix.values()));


        prefixBox.setButtonCell(new ListCell<Prefix>() {
            @Override
            protected void updateItem(Prefix prefix, boolean bln) {
                super.updateItem(prefix, bln);
                setGraphic(null);
                if (!bln) {
                    setAlignment(Pos.CENTER);
                    setText(prefix.getName());
                }
            }
        });
        prefixBox.getSelectionModel().select(UnitManager.getInstance().getPrefix(prefix));
        if (unit.getUnit().toString().length() > 1) {
            String sub = unit.toString().substring(0, 0);
            if (UnitManager.getInstance().getPrefixFromShort(sub) != null) {
                changeBaseUnit.setText(unit.getUnit().toString().replace(sub, ""));
            } else {
                changeBaseUnit.setText(unit.getUnit().toString().replace(sub, ""));
            }
        }

        symbolField.setText(unit.getLabel());


        prefixBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                String oldSymbol = "";
                if (symbolField.getText() != null) {
                    oldSymbol = symbolField.getText();
                }
                if (oldSymbol.length() > 1) {
                    String sub = oldSymbol.substring(0, 1);
                    if (UnitManager.getInstance().getPrefixFromShort(sub) != null && !oldSymbol.equals("m²") && !oldSymbol.equals("m³") && !oldSymbol.equals("min")) {
                        oldSymbol = oldSymbol.substring(1);
                    }
                }
                if (newValue != null) {
                    symbolField.setText(newValue.getSymbol() + oldSymbol);
                    JEVisUnit.Prefix newPrefix = UnitManager.getInstance().getPrefix(newValue);
                    jeVisUnit.setPrefix(newPrefix);
                } else {
                    symbolField.setText(oldSymbol);
                    jeVisUnit.setPrefix(JEVisUnit.Prefix.NONE);
                }
            }
        });

        symbolField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                try {
                    jeVisUnit.setLabel(newValue);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        changeBaseUnit.setOnAction(event -> {
            SimpleTreeUnitChooser stc = new SimpleTreeUnitChooser();
            if (stc.show(new Point2D(100, 100), ds) == SimpleTreeUnitChooser.Response.YES) {
                logger.info("Unit selected: {}", stc.getUnit().getFormula());
                jeVisUnit = stc.getUnit();
                prefixBox.getSelectionModel().select(null);
                changeBaseUnit.setText(jeVisUnit.getFormula());
                symbolField.setText(jeVisUnit.getLabel());
            }
        });

    }

    public BooleanProperty valueChangedProperty() {
        return valueChangedProperty;
    }

    /**
     * workaround to fire the change event on the unit property
     *
     * @return
     */
    private JEVisUnit cloneUnit(JEVisUnit unit) {
        JsonUnit ju = new JsonUnit();
        ju.setFormula(unit.getFormula());
        ju.setLabel(unit.getLabel());
        ju.setPrefix(unit.getPrefix().toString());
        return new JEVisUnitImp(ju);
    }

    public JEVisUnit getUnit() {
        return jeVisUnit;
    }

    public void setUnit(JEVisUnit unit) {
        this.jeVisUnit = unit;
    }

    public TextField getSymbolField() {
        return symbolField;
    }

    public Button getUnitButton() {
        return changeBaseUnit;
    }

    public ComboBox<Prefix> getPrefixBox() {
        return prefixBox;
    }
//
//    /**
//     * Not used anymore but i will leave it as an template for later
//     *
//     * @param ds
//     * @param unit
//     * @return
//     */
//    private Node buildUnitpanel(final JEVisDataSource ds, JEVisUnit unit) {
//        GridPane gp = new GridPane();
//        Label prefixL = new Label("Prefix:");
//        Label unitL = new Label("Unit:");
//        Label example = new Label("Custom Symbol: ");
//
//        prefixBox.setMaxWidth(520);//workaround
//        labelField.setEditable(false);
//
//        changeBaseUnit.setText(unit.toString());
//
//        HBox unitBox = new HBox(5);
//        unitBox.getChildren().setAll(changeBaseUnit);
//
//        unitBox.setMaxWidth(520);
//        labelField.setPrefWidth(100);
//        changeBaseUnit.setPrefWidth(100);
//        prefixBox.setPrefWidth(100);
//
//        gp.setHgap(5);
//        gp.setVgap(5);
//        gp.setPadding(new Insets(10, 10, 10, 10));
//
//        gp.add(prefixL, 0, 0);
//        gp.add(unitL, 0, 1);
//        gp.add(example, 0, 2);
//
//        gp.add(prefixBox, 1, 0);
//        gp.add(unitBox, 1, 1);
//        gp.add(labelField, 1, 2);
//
////        printExample(labelField, originalUnit);
//        return gp;
//    }

}
