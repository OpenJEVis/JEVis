package org.jevis.jeconfig.application.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.constants.EnterDataTypes;
import org.jevis.commons.i18n.I18n;

public class DataTypeBox extends ComboBox<EnterDataTypes> {

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
}
