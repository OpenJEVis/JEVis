package org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker;

import com.jfoenix.controls.JFXTimePicker;
import javafx.util.converter.LocalTimeStringConverter;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.PresetDateBox;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.List;

public class EndTimePicker extends JFXTimePicker {

    private GraphDataModel graphDataModel;
    private List<ChartDataModel> chartDataModels;
    private EndDatePicker endDatePicker;
    private Boolean[] programmaticallySetPresetDate;
    private PresetDateBox presetDateBox;

    public EndTimePicker() {
        super();
    }

    public void initialize(GraphDataModel graphDataModel, List<ChartDataModel> chartDataModels, EndDatePicker endDatePicker, Boolean[] programmaticallySetPresetDate, PresetDateBox presetDateBox) {
        this.graphDataModel = graphDataModel;
        this.chartDataModels = chartDataModels;
        this.endDatePicker = endDatePicker;
        this.programmaticallySetPresetDate = programmaticallySetPresetDate;
        this.presetDateBox = presetDateBox;

        setPrefWidth(100d);
        setMaxWidth(100d);
        setIs24HourView(true);
        setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        if (chartDataModels != null && !chartDataModels.isEmpty()) {
            chartDataModels.stream().map(ChartDataModel::getSelectedEnd).findFirst().ifPresent(end ->
                    valueProperty().setValue(LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute(), 999999999)));
        }

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = endDatePicker.valueProperty().getValue();
                LocalTime lt = valueProperty().getValue();
                if (ld != null && lt != null) {
                    setSelectedEnd(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (programmaticallySetPresetDate != null && presetDateBox != null) {
                    if (!programmaticallySetPresetDate[3]) presetDateBox.getSelectionModel().select(0);
                    programmaticallySetPresetDate[3] = false;
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
