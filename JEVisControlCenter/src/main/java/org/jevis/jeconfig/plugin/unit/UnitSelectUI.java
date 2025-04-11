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

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.CustomPrefix;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.ws.json.JsonUnit;
import org.jevis.jeconfig.application.unit.SimpleTreeUnitChooser;

import javax.measure.BinaryPrefix;
import javax.measure.MetricPrefix;
import javax.measure.Prefix;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UnitSelectUI {

    private final JFXTextField symbolField = new JFXTextField();
    private static final Logger logger = LogManager.getLogger(UnitSelectUI.class);
    private final JFXComboBox<Prefix> prefixBox;
    private final JFXButton changeBaseUnit = new JFXButton();//new JFXButton("Basic Unit");
    private JEVisUnit jeVisUnit;
    //workaround
    private final BooleanProperty valueChangedProperty = new SimpleBooleanProperty(false);

    public UnitSelectUI(JEVisDataSource ds, JEVisUnit unit) {
        final Prefix prefix = unit.getPrefix();
        jeVisUnit = unit;

        ObservableList<Prefix> prefixes = FXCollections.observableArrayList(MetricPrefix.values());
        prefixes.addAll(BinaryPrefix.values());

        prefixes.add(0, CustomPrefix.NONE);

        prefixBox = new JFXComboBox<>(prefixes);
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
        prefixBox.getSelectionModel().select(prefix);
        if (unit.getUnit().toString().length() > 1) {
            Prefix p = UnitManager.getInstance().prefixForUnit(unit.getUnit());
            if (p != null) {
                changeBaseUnit.setText(unit.getUnit().toString());
            } else {
                changeBaseUnit.setText(unit.getUnit().toString());
                //TODO
            }
        }

        symbolField.setText(unit.getLabel());

        prefixBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                String oldSymbol = "";
                if (symbolField.getText() != null) {
                    oldSymbol = symbolField.getText();
                }

                if (oldSymbol.length() > 2) {
                    String sub = oldSymbol.substring(0, 2);
                    if (UnitManager.getInstance().getPrefixFromShort(sub) != null && !oldSymbol.equals("min")) {
                        oldSymbol = oldSymbol.substring(2);
                    }
                } else if (oldSymbol.length() > 1) {
                    String sub = oldSymbol.substring(0, 1);
                    if (UnitManager.getInstance().getPrefixFromShort(sub) != null && !oldSymbol.equals("m²") && !oldSymbol.equals("m³") && !oldSymbol.equals("min")) {
                        oldSymbol = oldSymbol.substring(1);
                    }
                }

                if (newValue != null) {
                    symbolField.setText(newValue.getSymbol() + oldSymbol);
                    jeVisUnit.setPrefix(newValue);
                } else {
                    symbolField.setText(oldSymbol);
                    jeVisUnit.setPrefix(CustomPrefix.NONE);
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

    public JFXTextField getSymbolField() {
        return symbolField;
    }

    public JFXButton getUnitButton() {
        return changeBaseUnit;
    }

    public JFXComboBox<Prefix> getPrefixBox() {
        return prefixBox;
    }
}
