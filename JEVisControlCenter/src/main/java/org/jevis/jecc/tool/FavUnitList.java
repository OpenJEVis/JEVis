package org.jevis.jecc.tool;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jecc.dialog.UnitDialog;

import javax.measure.quantity.Dimensionless;

public class FavUnitList extends MFXComboBox<JEVisUnit> {
    private static final Logger logger = LogManager.getLogger(FavUnitList.class);

    public FavUnitList(JEVisAttribute att, JEVisUnit selectedUnit, boolean autoCommit) {
        ObservableList<JEVisUnit> units = FXCollections.observableArrayList();

        units.add(selectedUnit);
        units.add(new JEVisUnitImp(Dimensionless.UNIT, "", ""));//select no unit
        units.add(new JEVisUnitImp(Dimensionless.UNIT, "other", ""));
        units.addAll(UnitManager.getInstance().getFavoriteJUnits());

        this.setItems(units);

        //TODO JFX17
        setConverter(new StringConverter<JEVisUnit>() {
            @Override
            public String toString(JEVisUnit object) {
                String text = "";
                if (isOtherUnit(object)) {
                    //setGraphic(JEConfig.getImage("1404843819_node-tree.png", 18, 18));
                    //setTooltip(new Tooltip(I18n.getInstance().getString("favunitlist.select.tooltip")));
                    text = "Other...";
                } else {
                    text = UnitManager.getInstance().format(object);
                }

                return text;
            }

            @Override
            public JEVisUnit fromString(String string) {
                return null;
            }
        });

        setMinWidth(50);
        setMaxWidth(200);
        selectItem(selectedUnit);

        getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (isOtherUnit(newValue)) {
                    try {
                        UnitDialog unitDialog = new UnitDialog(att, this);
                        unitDialog.show();

                    } catch (JEVisException e) {
                        logger.error("Could not create unit dialog", e);
                    }
                } else {
                    att.setDisplayUnit(newValue);
                    att.setInputUnit(newValue);
                    if (autoCommit) {
                        att.commit();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

    }

    public boolean isOtherUnit(JEVisUnit unit) {
        return unit.getLabel().equals("other");
    }

}
