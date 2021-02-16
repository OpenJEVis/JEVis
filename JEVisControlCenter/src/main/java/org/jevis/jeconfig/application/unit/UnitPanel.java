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
package org.jevis.jeconfig.application.unit;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.CustomPrefix;
import org.jevis.commons.unit.UnitManager;

import javax.measure.MetricPrefix;
import javax.measure.Prefix;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 * @deprecated
 */
public class UnitPanel extends GridPane {
    private static final Logger logger = LogManager.getLogger(UnitPanel.class);

    private final JEVisUnit _returnUnit;

    public UnitPanel(JEVisDataSource ds, JEVisUnit unit, boolean showRemember) {
        super();
        _returnUnit = unit;
        buildUnit(ds, unit, showRemember);
    }

    private void buildUnit(final JEVisDataSource ds, JEVisUnit unit, boolean showRemember) {
        Label prefixL = new Label("Prefix:");
        Label unitL = new Label("Unit:");
        Label example = new Label("Symbol: ");

        final JEVisUnit.Prefix prefix = unit.getPrefix();

        List<Prefix> list = new ArrayList<>();
        list.add(CustomPrefix.NONE);
        Collections.addAll(list, MetricPrefix.values());
        JFXComboBox<Prefix> prefixBox = new JFXComboBox(FXCollections.observableArrayList(list));
        prefixBox.setMaxWidth(520);
//        prefixBox.getSelectionModel().select("");//toto get elsewhere?!

        prefixBox.setButtonCell(new ListCell<Prefix>() {
            @Override
            protected void updateItem(Prefix t, boolean bln) {
                super.updateItem(t, bln); //To change body of generated methods, choose Tools | Templates.
                if (!bln) {
                    setAlignment(Pos.CENTER);
                    setText(t.getName());//TODo: replace this dirty workaround to center the text in line with he buttonbelow
                }
            }
        });
        prefixBox.getSelectionModel().select(UnitManager.getInstance().getPrefix(prefix));

        final JFXTextField labelField = new JFXTextField();
        labelField.setEditable(false);
        final JFXButton changeBaseUnit = new JFXButton();//new JFXButton("Basic Unit");
        changeBaseUnit.setText(unit.toString());
//
        HBox unitBox = new HBox(5);
        unitBox.getChildren().setAll(changeBaseUnit);

        unitBox.setMaxWidth(520);
        labelField.setPrefWidth(100);
        changeBaseUnit.setPrefWidth(100);
        prefixBox.setPrefWidth(100);

//        changeBaseUnit.setMinWidth(100);
//        prefixBox.setMinWidth(100);
        setHgap(5);
        setVgap(5);
        setPadding(new Insets(10, 10, 10, 10));

        add(prefixL, 0, 0);
        add(unitL, 0, 1);
        add(example, 0, 2);

        add(prefixBox, 1, 0);
        add(unitBox, 1, 1);
        add(labelField, 1, 2);
//        if (showRemember) {
//            add(setDefault, 2, 2, 1, 1);
//        }

        prefixBox.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {

            _returnUnit.setPrefix(UnitManager.getInstance().getPrefix(t1));
            printExample(labelField, _returnUnit);
        });

        labelField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    _returnUnit.setLabel(labelField.getText());
                } catch (Exception ex) {

                }
            }
        });

        changeBaseUnit.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                SimpleTreeUnitChooser stc = new SimpleTreeUnitChooser();
                if (stc.show(new Point2D(100, 100), ds) == SimpleTreeUnitChooser.Response.YES) {//TODO: replace tjis hardcode position
                    _returnUnit.setFormula(stc.getUnit().getFormula());
                    _returnUnit.setLabel(UnitManager.getInstance().format(stc.getUnit()));

                    changeBaseUnit.setText(_returnUnit.toString());
                    printExample(labelField, _returnUnit);
                }

            }
        });

        printExample(labelField, _returnUnit);
    }

    private void printExample(final JFXTextField tf, final JEVisUnit unit) {
        logger.debug("UpdateLabel: '{}' '{}' '{}'", unit.getLabel(), unit.getFormula(), unit.getPrefix());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tf.setText(UnitManager.getInstance().format(unit));
            }
        });

    }

    public JEVisUnit getSelectedUnit() {
        return _returnUnit;
    }

}
