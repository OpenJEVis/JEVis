package org.jevis.jeconfig.tool;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.report.PeriodMode;

public class ReportPeriodBox extends ComboBox<PeriodMode> {

    public ReportPeriodBox(ObservableList<PeriodMode> items) {
        super(items);
        final String keyCurrent = I18n.getInstance().getString("plugin.object.report.dialog.period.current");
        final String keyLast = I18n.getInstance().getString("plugin.object.report.dialog.period.last");
        final String keyAll = I18n.getInstance().getString("plugin.object.report.dialog.period.all");

        setItems(FXCollections.observableArrayList(PeriodMode.values()));

        Callback<ListView<PeriodMode>, ListCell<PeriodMode>> cellFactory = new Callback<javafx.scene.control.ListView<PeriodMode>, ListCell<PeriodMode>>() {
            @Override
            public ListCell<PeriodMode> call(javafx.scene.control.ListView<PeriodMode> param) {
                return new ListCell<PeriodMode>() {
                    @Override
                    protected void updateItem(PeriodMode aggregationPeriod, boolean empty) {
                        super.updateItem(aggregationPeriod, empty);
                        if (empty || aggregationPeriod == null) {
                            setText("");
                        } else {
                            String text = "";
                            if (aggregationPeriod.equals(PeriodMode.CURRENT)) {
                                text = keyCurrent;
                            } else if (aggregationPeriod.equals(PeriodMode.LAST)) {
                                text = keyLast;
                            } else if (aggregationPeriod.equals(PeriodMode.ALL)) {
                                text = keyAll;
                            }

                            setText(text);
                        }
                    }
                };
            }
        };
        setCellFactory(cellFactory);
        setButtonCell(cellFactory.call(null));

        getSelectionModel().selectFirst();
    }
}
