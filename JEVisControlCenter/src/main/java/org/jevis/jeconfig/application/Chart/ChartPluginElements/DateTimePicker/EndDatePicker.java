package org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker;

import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.PresetDateBox;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EndDatePicker extends org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker.DatePicker {

    private EndTimePicker endTimePicker;
    private Boolean[] programmaticallySetPresetDate;
    private PresetDateBox presetDateBox;


    public EndDatePicker() {
        super(LocalDate.now());
    }

    public EndDatePicker(LocalDate localDate) {
        super(localDate);
    }

    public void initialize(GraphDataModel graphDataModel, List<ChartDataModel> chartDataModels, EndTimePicker endTimePicker, Boolean[] programmaticallySetPresetDate, PresetDateBox presetDateBox) {
        this.graphDataModel = graphDataModel;
        this.chartDataModels = chartDataModels;
        this.endTimePicker = endTimePicker;
        this.programmaticallySetPresetDate = programmaticallySetPresetDate;
        this.presetDateBox = presetDateBox;

        setPrefWidth(120d);
        updateCellFactory();

        if (chartDataModels != null && !chartDataModels.isEmpty()) {
            chartDataModels.stream().map(ChartDataModel::getSelectedEnd).findFirst().ifPresent(end ->
                    valueProperty().setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth())));
        }

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = valueProperty().getValue();
                LocalTime lt = endTimePicker.valueProperty().getValue();
                if (ld != null && lt != null) {
                    setSelectedEnd(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (programmaticallySetPresetDate != null && presetDateBox != null) {
                    if (!programmaticallySetPresetDate[2]) presetDateBox.getSelectionModel().select(0);
                    programmaticallySetPresetDate[2] = false;
                }
            }
        });
    }


    private void setSelectedEnd(DateTime selectedEnd) {
        if (chartDataModels == null || chartDataModels.isEmpty()) {
            graphDataModel.getSelectedData().forEach(dataModel -> {
                dataModel.setSelectedEnd(selectedEnd);
                dataModel.setSomethingChanged(true);
            });
        } else {
            chartDataModels.forEach(model -> {
                model.setSelectedEnd(selectedEnd);
                model.setSomethingChanged(true);
            });
        }

    }


}
