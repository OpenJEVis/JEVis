package org.jevis.jecc.application.control;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.constants.EnterDataTypes;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.joda.time.Period;

public class DataTypeBox extends MFXComboBox<EnterDataTypes> {

    public DataTypeBox() {
        super();

        ObservableList<EnterDataTypes> enterDataTypes = FXCollections.observableArrayList(EnterDataTypes.values());
        setItems(enterDataTypes);

        //TODO JFX17

        setConverter(new StringConverter<EnterDataTypes>() {
            @Override
            public String toString(EnterDataTypes object) {
                String text = "";
                if (object != null) {
                    try {
                        switch (object) {
                            case YEAR:
                                text = I18n.getInstance().getString("dialog.enterdata.type.year");
                                break;
                            case MONTH:
                                text = I18n.getInstance().getString("dialog.enterdata.type.month");
                                break;
                            case DAY:
                                text = I18n.getInstance().getString("dialog.enterdata.type.day");
                                break;
                            case SPECIFIC_DATETIME:
                                text = I18n.getInstance().getString("dialog.enterdata.type.specific");
                                break;
                        }
                    } catch (Exception ignored) {

                    }
                }

                return text;
            }

            @Override
            public EnterDataTypes fromString(String string) {
                return getItems().get(getSelectedIndex());
            }
        });

        selectItem(EnterDataTypes.MONTH);
    }

    public Period selectFromPeriod(JEVisObject selectedObject) {
        Period p = Period.ZERO;
        if (selectedObject != null) {
            JEVisAttribute periodAttribute = null;
            try {
                periodAttribute = selectedObject.getAttribute(CleanDataObject.AttributeName.PERIOD.getAttributeName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (periodAttribute != null) {

                try {
                    p = new Period(periodAttribute.getLatestSample().getValueAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (p.equals(Period.days(1))) {
                    Platform.runLater(() -> selectItem(EnterDataTypes.DAY));
                } else if (p.equals(Period.weeks(1))) {
                    Platform.runLater(() -> selectItem(EnterDataTypes.DAY));
                } else if (p.equals(Period.months(1))) {
                    Platform.runLater(() -> selectItem(EnterDataTypes.MONTH));
                } else if (p.equals(Period.years(1))) {
                    Platform.runLater(() -> selectItem(EnterDataTypes.YEAR));
                } else {
                    Platform.runLater(() -> selectItem(EnterDataTypes.SPECIFIC_DATETIME));
                }
            }
        }
        return p;
    }
}
