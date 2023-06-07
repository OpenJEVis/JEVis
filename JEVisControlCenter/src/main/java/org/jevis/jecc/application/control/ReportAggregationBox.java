package org.jevis.jecc.application.control;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.i18n.I18n;

public class ReportAggregationBox extends MFXComboBox<AggregationPeriod> {

    public ReportAggregationBox() {
        super();
        final String keyNone = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.none");
        final String keyQuarterHourly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.quarterhourly");
        final String keyHourly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.hourly");
        final String keyDaily = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.daily");
        final String keyWeekly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.weekly");
        final String keyMonthly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.monthly");
        final String keyQuarterly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.quarterly");
        final String keyYearly = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.yearly");

        setItems(FXCollections.observableArrayList(AggregationPeriod.values()));

        //TODO JFX17

        setConverter(new StringConverter<AggregationPeriod>() {
            @Override
            public String toString(AggregationPeriod object) {
                String text = "";
                switch (object) {
                    case NONE:
                        text = keyNone;
                        break;
                    case QUARTER_HOURLY:
                        text = keyQuarterHourly;
                        break;
                    case HOURLY:
                        text = keyHourly;
                        break;
                    case DAILY:
                        text = keyDaily;
                        break;
                    case WEEKLY:
                        text = keyWeekly;
                        break;
                    case MONTHLY:
                        text = keyMonthly;
                        break;
                    case QUARTERLY:
                        text = keyQuarterly;
                        break;
                    case YEARLY:
                        text = keyYearly;
                        break;
                }

                return text;
            }

            @Override
            public AggregationPeriod fromString(String string) {
                return getItems().get(getSelectedIndex());
            }
        });

        getSelectionModel().selectFirst();
    }
}
