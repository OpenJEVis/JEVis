package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;

public class AggregationBox extends JFXComboBox<AggregationPeriod> {


    public AggregationBox() {
        final String keyPreset = I18n.getInstance().getString("plugin.graph.interval.preset");
        final String keyMinutely = I18n.getInstance().getString("plugin.unit.samplingrate.everyminute");
        final String keyQuarterHourly = I18n.getInstance().getString("plugin.graph.interval.quarterhourly");
        final String keyHourly = I18n.getInstance().getString("plugin.graph.interval.hourly");
        final String keyDaily = I18n.getInstance().getString("plugin.graph.interval.daily");
        final String keyWeekly = I18n.getInstance().getString("plugin.graph.interval.weekly");
        final String keyMonthly = I18n.getInstance().getString("plugin.graph.interval.monthly");
        final String keyQuarterly = I18n.getInstance().getString("plugin.graph.interval.quarterly");
        final String keyYearly = I18n.getInstance().getString("plugin.graph.interval.yearly");

        setItems(FXCollections.observableArrayList(AggregationPeriod.values()));

        Callback<ListView<AggregationPeriod>, ListCell<AggregationPeriod>> cellFactory = new Callback<javafx.scene.control.ListView<AggregationPeriod>, ListCell<AggregationPeriod>>() {
            @Override
            public ListCell<AggregationPeriod> call(javafx.scene.control.ListView<AggregationPeriod> param) {
                return new ListCell<AggregationPeriod>() {
                    @Override
                    protected void updateItem(AggregationPeriod aggregationPeriod, boolean empty) {
                        super.updateItem(aggregationPeriod, empty);
                        if (empty || aggregationPeriod == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (aggregationPeriod) {
                                case MINUTELY:
                                    text = keyMinutely;
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
                                default:
                                case NONE:
                                    text = keyPreset;
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
    }

    public AggregationBox(AggregationPeriod period) {
        this();

        getSelectionModel().select(period);
    }

    public AggregationBox(AnalysisDataModel analysisDataModel, ChartDataRow data) {
        this(analysisDataModel.getAggregationPeriod());
    }

}
