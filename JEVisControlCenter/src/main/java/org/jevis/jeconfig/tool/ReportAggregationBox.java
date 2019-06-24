package org.jevis.jeconfig.tool;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.report.ReportAggregation;

public class ReportAggregationBox extends ComboBox<String> {

    public ReportAggregationBox(String period) {
        final String keyNone = I18n.getInstance().getString("plugin.graph.interval.preset");
        final String keyHourly = I18n.getInstance().getString("plugin.graph.interval.hourly");
        final String keyDaily = I18n.getInstance().getString("plugin.graph.interval.daily");
        final String keyWeekly = I18n.getInstance().getString("plugin.graph.interval.weekly");
        final String keyMonthly = I18n.getInstance().getString("plugin.graph.interval.monthly");
        final String keyQuarterly = I18n.getInstance().getString("plugin.graph.interval.quarterly");
        final String keyYearly = I18n.getInstance().getString("plugin.graph.interval.yearly");

        setItems(FXCollections.observableArrayList(ReportAggregation.values()));

        Callback<ListView<String>, ListCell<String>> cellFactory = new Callback<javafx.scene.control.ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(javafx.scene.control.ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String aggregationPeriod, boolean empty) {
                        super.updateItem(aggregationPeriod, empty);
                        if (empty || aggregationPeriod == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (aggregationPeriod) {
                                case "NONE":
                                    text = keyNone;
                                    break;
                                case "HOURLY":
                                    text = keyHourly;
                                    break;
                                case "DAILY":
                                    text = keyDaily;
                                    break;
                                case "WEEKLY":
                                    text = keyWeekly;
                                    break;
                                case "MONTHLY":
                                    text = keyMonthly;
                                    break;
                                case "QUARTERLY":
                                    text = keyQuarterly;
                                    break;
                                case "YEARLY":
                                    text = keyYearly;
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        setCellFactory(cellFactory);
        setButtonCell(cellFactory.call(null));

        getSelectionModel().select(period);
    }
}
