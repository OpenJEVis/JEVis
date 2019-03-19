package org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker;

import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.PresetDateBox;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class StartDatePicker extends org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker.DatePicker {
    private StartTimePicker startTimePicker;
    private Boolean[] programmaticallySetPresetDate;
    private PresetDateBox presetDateBox;

    public StartDatePicker() {
        super(LocalDate.now().minusDays(7));
    }

    public StartDatePicker(LocalDate localDate) {
        super(localDate);
    }

    public void initialize(GraphDataModel graphDataModel, List<ChartDataModel> chartDataModels, StartTimePicker startTimePicker, Boolean[] programmaticallySetPresetDate, PresetDateBox presetDateBox) {
        this.graphDataModel = graphDataModel;
        this.chartDataModels = chartDataModels;
        this.startTimePicker = startTimePicker;
        this.programmaticallySetPresetDate = programmaticallySetPresetDate;
        this.presetDateBox = presetDateBox;

        setPrefWidth(120d);
        updateCellFactory();

        if (chartDataModels != null && !chartDataModels.isEmpty()) {
            chartDataModels.stream().map(ChartDataModel::getSelectedStart).findFirst().ifPresent(start ->
                    valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth())));
        }

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = valueProperty().getValue();
                LocalTime lt = startTimePicker.valueProperty().getValue();
                if (ld != null && lt != null) {
                    setSelectedStart(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (programmaticallySetPresetDate != null && presetDateBox != null) {
                    if (!programmaticallySetPresetDate[0]) presetDateBox.getSelectionModel().select(0);
                    programmaticallySetPresetDate[0] = false;
                }
            }
        });
    }

    private void setSelectedStart(DateTime selectedStart) {
        if (chartDataModels == null || chartDataModels.isEmpty()) {
            graphDataModel.getSelectedData().forEach(dataModel -> {

                dataModel.setSelectedStart(selectedStart);
                dataModel.setSomethingChanged(true);

            });
        } else {
            chartDataModels.forEach(dataModel -> {
                dataModel.setSelectedStart(selectedStart);
                dataModel.setSomethingChanged(true);
            });
        }
    }

}
