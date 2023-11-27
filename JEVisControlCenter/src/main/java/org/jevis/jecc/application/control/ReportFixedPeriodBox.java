package org.jevis.jecc.application.control;


import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jevis.commons.dataprocessing.FixedPeriod;
import org.jevis.commons.i18n.I18n;

public class ReportFixedPeriodBox extends ComboBox<FixedPeriod> {

    public ReportFixedPeriodBox() {
        super();
        final String keyNone = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.none");
        final String keyQuarterHour = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.quarterhour");
        final String keyHour = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.hour");
        final String keyDay = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.day");
        final String keyWeek = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.week");
        final String keyMonth = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.month");
        final String keyQuarter = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.quarter");
        final String keyYear = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.year");
        final String keyThreeYears = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.threeyears");
        final String keyFiveYears = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.fiveyears");
        final String keyTenYears = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.tenyears");

        setItems(FXCollections.observableArrayList(FixedPeriod.values()));

        //TODO JFX17
        setConverter(new StringConverter<FixedPeriod>() {
            @Override
            public String toString(FixedPeriod object) {
                String text = "";
                switch (object) {
                    case NONE:
                        text = keyNone;
                        break;
                    case QUARTER_HOUR:
                        text = keyQuarterHour;
                        break;
                    case HOUR:
                        text = keyHour;
                        break;
                    case DAY:
                        text = keyDay;
                        break;
                    case WEEK:
                        text = keyWeek;
                        break;
                    case MONTH:
                        text = keyMonth;
                        break;
                    case QUARTER:
                        text = keyQuarter;
                        break;
                    case YEAR:
                        text = keyYear;
                        break;
                    case THREEYEARS:
                        text = keyThreeYears;
                        break;
                    case FIVEYEARS:
                        text = keyFiveYears;
                        break;
                    case TENYEARS:
                        text = keyTenYears;
                        break;
                }

                return (text);
            }

            @Override
            public FixedPeriod fromString(String string) {
                return getItems().get(getSelectionModel().getSelectedIndex());
            }
        });

        getSelectionModel().selectFirst();
    }
}
