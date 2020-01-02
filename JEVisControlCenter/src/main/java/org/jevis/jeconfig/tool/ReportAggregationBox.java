package org.jevis.jeconfig.tool;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.report.ReportAggregation;

public class ReportAggregationBox extends ComboBox<String> {

    public ReportAggregationBox(ObservableList<String> items) {
        super(items);
        final String keyNone = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.none");
        final String keyHourly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.hourly");
        final String keyDaily = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.daily");
        final String keyWeekly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.weekly");
        final String keyMonthly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.monthly");
        final String keyQuarterly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.quarterly");
        final String keyYearly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.yearly");

        final String keyAverage = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.average");
        final String keySortedMin = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.sortedmin");
        final String keySortedMax = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.sortedmax");
        final String keyMin = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.min");
        final String keyMax = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.max");
        final String keyMedian = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.median");
        final String keyRunningMean = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.runningmean");
        final String keyCentricRunningMean = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.centricrunningmean");

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
                            if (aggregationPeriod.contains("NONE_")) {
                                text = keyNone;
                            } else if (aggregationPeriod.contains("HOURLY_")) {
                                text = keyHourly;
                            } else if (aggregationPeriod.contains("DAILY_")) {
                                text = keyDaily;
                            } else if (aggregationPeriod.contains("WEEKLY_")) {
                                text = keyWeekly;
                            } else if (aggregationPeriod.contains("MONTHLY_")) {
                                text = keyMonthly;
                            } else if (aggregationPeriod.contains("QUARTERLY_")) {
                                text = keyQuarterly;
                            } else if (aggregationPeriod.contains("YEARLY_")) {
                                text = keyYearly;
                            }

                            if (aggregationPeriod.contains("AVERAGE")) {
                                text += ", ";
                                text += keyAverage;
                            } else if (aggregationPeriod.contains("SORTED_MIN")) {
                                text += ", ";
                                text += keySortedMin;
                            } else if (aggregationPeriod.contains("SORTED_MAX")) {
                                text += ", ";
                                text += keySortedMax;
                            } else if (aggregationPeriod.contains("MIN")) {
                                text += ", ";
                                text += keyMin;
                            } else if (aggregationPeriod.contains("MAX")) {
                                text += ", ";
                                text += keyMax;
                            } else if (aggregationPeriod.contains("MEDIAN")) {
                                text += ", ";
                                text += keyMedian;
                            } else if (aggregationPeriod.contains("CENTRIC_RUNNING_MEAN")) {
                                text += ", ";
                                text += keyCentricRunningMean;
                            } else if (aggregationPeriod.contains("RUNNING_MEAN")) {
                                text += ", ";
                                text += keyRunningMean;
                            } else if (aggregationPeriod.contains("_NONE")) {
                                text += ", ";
                                text += keyNone;
                            } else if (aggregationPeriod.equals("NONE")) {
                                text = keyNone;
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
