package org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker;

import com.jfoenix.controls.JFXTimePicker;
import javafx.util.converter.LocalTimeStringConverter;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.PresetDateBox;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.List;

public class StartTimePicker extends JFXTimePicker {

    private GraphDataModel graphDataModel;
    private List<ChartDataModel> chartDataModels;
    private StartDatePicker startDatePicker;
    private Boolean[] programmaticallySetPresetDate;
    private PresetDateBox presetDateBox;

    public StartTimePicker() {
        super();

    }

    public void initialize(GraphDataModel graphDataModel, List<ChartDataModel> chartDataModels, StartDatePicker startDatePicker, Boolean[] programmaticallySetPresetDate,
                           PresetDateBox presetDateBox) {
        this.graphDataModel = graphDataModel;
        this.chartDataModels = chartDataModels;
        this.startDatePicker = startDatePicker;
        this.programmaticallySetPresetDate = programmaticallySetPresetDate;
        this.presetDateBox = presetDateBox;

        setPrefWidth(100d);
        setMaxWidth(100d);
        setIs24HourView(true);
        setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        if (chartDataModels != null && !chartDataModels.isEmpty()) {
            chartDataModels.stream().map(ChartDataModel::getSelectedStart).findFirst().ifPresent(start ->
                    valueProperty().setValue(LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute())));
        }

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = startDatePicker.valueProperty().getValue();
                LocalTime lt = valueProperty().getValue();
                if (ld != null && lt != null) {
                    setSelectedStart(new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                            lt.getHour(), lt.getMinute(), lt.getSecond()));
                }
                if (programmaticallySetPresetDate != null && presetDateBox != null) {
                    if (!programmaticallySetPresetDate[1]) presetDateBox.getSelectionModel().select(0);
                    programmaticallySetPresetDate[1] = false;
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
