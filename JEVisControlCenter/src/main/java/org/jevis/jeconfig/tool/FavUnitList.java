package org.jevis.jeconfig.tool;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.UnitDialog;
import tech.units.indriya.AbstractUnit;

public class FavUnitList extends JFXComboBox<JEVisUnit> {
    private static final Logger logger = LogManager.getLogger(FavUnitList.class);

    public FavUnitList(JEVisAttribute att, JEVisUnit selectedUnit, boolean autoCommit) {
        ObservableList<JEVisUnit> units = FXCollections.observableArrayList();

        units.add(selectedUnit);
        units.add(new JEVisUnitImp(AbstractUnit.ONE, "", ""));//select no unit
        units.add(new JEVisUnitImp(AbstractUnit.ONE, "other", ""));
        units.addAll(UnitManager.getInstance().getFavoriteJUnits());

        this.setItems(units);
        Callback<ListView<JEVisUnit>, ListCell<JEVisUnit>> unitFactory = getUnitLIstFactory();
        setCellFactory(unitFactory);
        setButtonCell(unitFactory.call(null));
        setMinWidth(50);
        setMaxWidth(200);
        getSelectionModel().select(selectedUnit);

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

    private Callback<ListView<JEVisUnit>, ListCell<JEVisUnit>> getUnitLIstFactory() {
        Callback<ListView<JEVisUnit>, ListCell<JEVisUnit>> unitRenderer = new Callback<ListView<JEVisUnit>, ListCell<JEVisUnit>>() {
            @Override
            public ListCell<JEVisUnit> call(ListView<JEVisUnit> param) {
                return new ListCell<JEVisUnit>() {
                    {
                        //super.setMinWidth(260);
                    }

                    @Override
                    protected void updateItem(JEVisUnit unitItem, boolean empty) {
                        super.updateItem(unitItem, empty);
                        setGraphic(null);
                        if (!empty) {
                            setAlignment(Pos.CENTER);
                            setText("");
                            setTextAlignment(TextAlignment.LEFT);

                            if (isOtherUnit(unitItem)) {
                                setGraphic(JEConfig.getImage("1404843819_node-tree.png", 18, 18));
                                setTooltip(new Tooltip(I18n.getInstance().getString("favunitlist.select.tooltip")));
                                //setText("Other...");
                            } else {
                                setText(UnitManager.getInstance().format(unitItem));
                            }
                        }
                    }
                };
            }
        };

        return unitRenderer;
    }
}
