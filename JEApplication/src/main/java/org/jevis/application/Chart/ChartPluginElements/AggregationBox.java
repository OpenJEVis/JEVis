package org.jevis.application.Chart.ChartPluginElements;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.commons.dataprocessing.AggregationPeriod;

import java.util.ArrayList;
import java.util.List;

public class AggregationBox {
    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
    private ChoiceBox aggregationBox;

    public AggregationBox(final ChartDataModel data) {
        List<String> aggList = new ArrayList<>();

        final String keyPreset = rb.getString("graph.interval.preset");
        String keyHourly = rb.getString("graph.interval.hourly");
        String keyDaily = rb.getString("graph.interval.daily");
        String keyWeekly = rb.getString("graph.interval.weekly");
        String keyMonthly = rb.getString("graph.interval.monthly");
        String keyQuarterly = rb.getString("graph.interval.quarterly");
        String keyYearly = rb.getString("graph.interval.yearly");

        aggList.add(keyPreset);
        aggList.add(keyHourly);
        aggList.add(keyDaily);
        aggList.add(keyWeekly);
        aggList.add(keyMonthly);
        aggList.add(keyQuarterly);
        aggList.add(keyYearly);

        ChoiceBox aggregate = new ChoiceBox(FXCollections.observableArrayList(aggList));

        aggregate.setMinWidth(80);

        aggregate.getSelectionModel().selectFirst();
        switch (data.getAggregationPeriod()) {
            case NONE:
                aggregate.getSelectionModel().selectFirst();
                break;
            case HOURLY:
                aggregate.getSelectionModel().select(1);
                break;
            case DAILY:
                aggregate.getSelectionModel().select(2);
                break;
            case WEEKLY:
                aggregate.getSelectionModel().select(3);
                break;
            case MONTHLY:
                aggregate.getSelectionModel().select(4);
                break;
            case QUARTERLY:
                aggregate.getSelectionModel().select(5);
                break;
            case YEARLY:
                aggregate.getSelectionModel().select(6);
                break;
        }

        aggregate.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
            //TODO:replace this quick and dirty workaround

            if (newValue.equals(keyPreset)) {
                data.setAggregationPeriod(AggregationPeriod.NONE);
            } else if (newValue.equals(keyHourly)) {
                data.setAggregationPeriod(AggregationPeriod.HOURLY);
            } else if (newValue.equals(keyDaily)) {
                data.setAggregationPeriod(AggregationPeriod.DAILY);
            } else if (newValue.equals(keyWeekly)) {
                data.setAggregationPeriod(AggregationPeriod.WEEKLY);
            } else if (newValue.equals(keyMonthly)) {
                data.setAggregationPeriod(AggregationPeriod.MONTHLY);
            } else if (newValue.equals(keyQuarterly)) {
                data.setAggregationPeriod(AggregationPeriod.QUARTERLY);
            } else if (newValue.equals(keyYearly)) {
                data.setAggregationPeriod(AggregationPeriod.YEARLY);
            }


        });

        this.aggregationBox = aggregate;
    }

    public ChoiceBox getAggregationBox() {
        return aggregationBox;
    }
}
