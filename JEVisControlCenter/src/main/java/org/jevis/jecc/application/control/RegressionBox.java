package org.jevis.jecc.application.control;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.Chart.Charts.regression.RegressionType;

public class RegressionBox extends ComboBox<RegressionType> {

    public RegressionBox() {
        super();

        ObservableList<RegressionType> regressionTypes = FXCollections.observableArrayList(RegressionType.values());
        regressionTypes.remove(0);
        setItems(regressionTypes);

        Callback<ListView<RegressionType>, ListCell<RegressionType>> cellFactory = new Callback<ListView<RegressionType>, ListCell<RegressionType>>() {
            @Override
            public ListCell<RegressionType> call(ListView<RegressionType> param) {
                return new ListCell<RegressionType>() {
                    @Override
                    public void updateItem(RegressionType item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            try {
                                switch (item) {
                                    case NONE:
                                        setText(I18n.getInstance().getString("dialog.regression.type.none"));
                                        break;
                                    case POLY:
                                        setText(I18n.getInstance().getString("dialog.regression.type.poly"));
                                        break;
                                    case EXP:
                                        setText(I18n.getInstance().getString("dialog.regression.type.exp"));
                                        break;
                                    case LOG:
                                        setText(I18n.getInstance().getString("dialog.regression.type.log"));
                                        break;
                                    case POW:
                                        setText(I18n.getInstance().getString("dialog.regression.type.pow"));
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

        //TODO JFX17
        setConverter(new StringConverter<RegressionType>() {
            @Override
            public String toString(RegressionType object) {
                switch (object) {
                    default:
                    case NONE:
                        return (I18n.getInstance().getString("dialog.regression.type.none"));
                    case POLY:
                        return (I18n.getInstance().getString("dialog.regression.type.poly"));
                    case EXP:
                        return (I18n.getInstance().getString("dialog.regression.type.exp"));
                    case LOG:
                        return (I18n.getInstance().getString("dialog.regression.type.log"));
                    case POW:
                        return (I18n.getInstance().getString("dialog.regression.type.pow"));
                }
            }

            @Override
            public RegressionType fromString(String string) {
                return getItems().get(getSelectionModel().getSelectedIndex());
            }
        });

        getSelectionModel().select(RegressionType.POLY);
        setDisable(true);

        getSelectionModel().select(RegressionType.POLY);
    }
}
