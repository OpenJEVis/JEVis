/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.unit;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisUnit;
import org.jevis.application.unit.SimpleTreeUnitChooser;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonUnit;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UnitSelectUI {

    private final JEVisUnit originalUnit;

    private ObjectProperty<JEVisUnit> unitProperty = new SimpleObjectProperty<>();
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(UnitSelectUI.class);

    final TextField labelField = new TextField();
    final Button changeBaseUnit = new Button();//new Button("Basic Unit");
    final ComboBox<String> prefixBox = new ComboBox(FXCollections.observableArrayList(UnitManager.getInstance().getPrefixes()));
    //workaround
    final BooleanProperty valueChangedProperty = new SimpleBooleanProperty(false);

    public UnitSelectUI(JEVisDataSource ds, JEVisUnit unit) {
        final JEVisUnit.Prefix prefix = unit.getPrefix();
        originalUnit = unit;
        unitProperty.setValue(unit);

        prefixBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String t, boolean bln) {
                super.updateItem(t, bln); //To change body of generated methods, choose Tools | Templates.
                setGraphic(null);
                if (!bln) {
                    setAlignment(Pos.CENTER);
                    if (t == null || t.isEmpty()) {
                        setText("Unitless");
                    } else {
                        setText(t);
                    }

                }
            }
        });
        prefixBox.getSelectionModel().select(UnitManager.getInstance().getPrefixName(unit.getPrefix(), Locale.getDefault()));
        changeBaseUnit.setText(UnitManager.getInstance().formate(unit));
        labelField.setText(unit.getLabel());

        unitProperty.addListener(new ChangeListener<JEVisUnit>() {
            @Override
            public void changed(ObservableValue<? extends JEVisUnit> observable, JEVisUnit oldValue, JEVisUnit newValue) {
                System.out.println("ffffffffffffffffffffffffff");
            }
        });
        
        prefixBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                
                unitProperty.getValue().setPrefix(prefix);
                labelField.setText(UnitManager.getInstance().formate(unitProperty.getValue()));
            }
        });

        labelField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    System.out.println("Label event: "+newValue);
                    JEVisUnit cloneUnit = cloneUnit(unitProperty.getValue());
                    cloneUnit.setLabel(newValue);
                    unitProperty.setValue(cloneUnit);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        changeBaseUnit.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                SimpleTreeUnitChooser stc = new SimpleTreeUnitChooser();
                try {
                    if (stc.show(new Point2D(100, 100), ds) == SimpleTreeUnitChooser.Response.YES) {
//                        JEVisUnit cloneUnit = cloneUnit(unitProperty.getValue());
//                        cloneUnit.setFormula(stc.getUnit().getFormula());
//                        unitProperty.setValue(cloneUnit);
                        unitProperty.getValue().setFormula(stc.getUnit().getFormula());
                        changeBaseUnit.setText(unitProperty.getValue().getFormula());
                        labelField.setText(UnitManager.getInstance().formate(unitProperty.getValue()));//proble: the unitChang event cone tow times
                    }
                } catch (JEVisException ex) {
                    Logger.getLogger(UnitSelectUI.class.getName()).log(Level.SEVERE, null, ex);
                }

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
        ju.setPrefix(UnitManager.getInstance().getPrefixName(unit.getPrefix(), Locale.getDefault()));
        JEVisUnit cloneUnit = new JEVisUnitImp(ju);
        return cloneUnit;
    }

    public ObjectProperty<JEVisUnit> unitProperty() {
        return unitProperty;
    }

    public TextField getLabelField() {
        return labelField;
    }

    public Button getUnitButton() {
        return changeBaseUnit;
    }

    public ComboBox getPrefixBox() {
        return prefixBox;
    }

    /**
     * Not used anymore but i will leave it as an template for later
     *
     * @param ds
     * @param unit
     * @return
     */
    private Node buildUnitpanel(final JEVisDataSource ds, JEVisUnit unit) {
        GridPane gp = new GridPane();
        Label prefixL = new Label("Prefix:");
        Label unitL = new Label("Unit:");
        Label example = new Label("Custom Symbol: ");

        prefixBox.setMaxWidth(520);//workaround
        labelField.setEditable(false);

        changeBaseUnit.setText(unit.toString());

        HBox unitBox = new HBox(5);
        unitBox.getChildren().setAll(changeBaseUnit);

        unitBox.setMaxWidth(520);
        labelField.setPrefWidth(100);
        changeBaseUnit.setPrefWidth(100);
        prefixBox.setPrefWidth(100);

        gp.setHgap(5);
        gp.setVgap(5);
        gp.setPadding(new Insets(10, 10, 10, 10));

        gp.add(prefixL, 0, 0);
        gp.add(unitL, 0, 1);
        gp.add(example, 0, 2);

        gp.add(prefixBox, 1, 0);
        gp.add(unitBox, 1, 1);
        gp.add(labelField, 1, 2);

//        printExample(labelField, originalUnit);
        return gp;
    }

    public JEVisUnit getSelectedUnit() {
        return originalUnit;
    }

}
