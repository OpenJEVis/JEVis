package org.jevis.jeconfig.application.control;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.constants.EnterDataTypes;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.joda.time.Period;

public class DataTypeBox extends JFXComboBox<EnterDataTypes> {

    public DataTypeBox() {
        super();

        ObservableList<EnterDataTypes> enterDataTypes = FXCollections.observableArrayList(EnterDataTypes.values());
        setItems(enterDataTypes);

        Callback<ListView<EnterDataTypes>, ListCell<EnterDataTypes>> cellFactory = new Callback<ListView<EnterDataTypes>, ListCell<EnterDataTypes>>() {
            @Override
            public ListCell<EnterDataTypes> call(ListView<EnterDataTypes> param) {
                return new ListCell<EnterDataTypes>() {
                    @Override
                    public void updateItem(EnterDataTypes item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            try {
                                switch (item) {
                                    case YEAR:
                                        setText(I18n.getInstance().getString("dialog.enterdata.type.year"));
                                        break;
                                    case MONTH:
                                        setText(I18n.getInstance().getString("dialog.enterdata.type.month"));
                                        break;
                                    case DAY:
                                        setText(I18n.getInstance().getString("dialog.enterdata.type.day"));
                                        break;
                                    case SPECIFIC_DATETIME:
                                        setText(I18n.getInstance().getString("dialog.enterdata.type.specific"));
                                        break;
                                }
                            } catch (Exception ex) {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };

        setCellFactory(cellFactory);
        setButtonCell(cellFactory.call(null));

        getSelectionModel().select(EnterDataTypes.MONTH);
    }

    public void selectFromPeriod(JEVisObject selectedObject) {
        if (selectedObject != null) {
            JEVisAttribute periodAttribute = null;
            try {
                periodAttribute = selectedObject.getAttribute(CleanDataObject.AttributeName.PERIOD.getAttributeName());
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            if (periodAttribute != null) {

                Period p = Period.ZERO;
                try {
                    p = new Period(periodAttribute.getLatestSample().getValueAsString());
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                if (p.equals(Period.days(1))) {
                    getSelectionModel().select(EnterDataTypes.DAY);
                } else if (p.equals(Period.weeks(1))) {
                    getSelectionModel().select(EnterDataTypes.DAY);
                } else if (p.equals(Period.months(1))) {
                    getSelectionModel().select(EnterDataTypes.MONTH);
                } else if (p.equals(Period.years(1))) {
                    getSelectionModel().select(EnterDataTypes.YEAR);
                } else {
                    getSelectionModel().select(EnterDataTypes.SPECIFIC_DATETIME);
                }
            }
        }
    }
}
