package org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker;

import com.jfoenix.controls.JFXDatePicker;
import javafx.scene.control.DateCell;
import javafx.util.Callback;
import org.jevis.api.JEVisAttribute;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.List;

public class DatePicker extends JFXDatePicker {
    GraphDataModel graphDataModel;
    List<ChartDataModel> chartDataModels;
    private LocalDate minDate;
    private LocalDate maxDate;

    public DatePicker(LocalDate localDate) {
        super(localDate);
    }

    private void updateMinMax() {
        minDate = null;
        maxDate = null;

        if (chartDataModels == null) {
            for (ChartDataModel mdl : graphDataModel.getSelectedData()) {
                if (!mdl.getSelectedcharts().isEmpty()) {
                    JEVisAttribute att = mdl.getAttribute();
                    setMinMax(att);
                }
            }
        } else {
            for (ChartDataModel model : chartDataModels) {
                JEVisAttribute att = model.getAttribute();
                setMinMax(att);
            }
        }
    }

    private void setMinMax(JEVisAttribute att) {
        DateTime timeStampFromFirstSample = att.getTimestampFromFirstSample();
        DateTime timeStampFromLastSample = att.getTimestampFromLastSample();

        LocalDate min_check = LocalDate.of(
                timeStampFromFirstSample.getYear(),
                timeStampFromFirstSample.getMonthOfYear(),
                timeStampFromFirstSample.getDayOfMonth());

        LocalDate max_check = LocalDate.of(
                timeStampFromLastSample.getYear(),
                timeStampFromLastSample.getMonthOfYear(),
                timeStampFromLastSample.getDayOfMonth());

        if (minDate == null || min_check.isBefore(minDate)) minDate = min_check;
        if (maxDate == null || max_check.isAfter(maxDate)) maxDate = max_check;
    }

    public void updateCellFactory() {
        setDayCellFactory(new Callback<javafx.scene.control.DatePicker, DateCell>() {
            @Override
            public DateCell call(final javafx.scene.control.DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        updateMinMax();

                        if (minDate != null && item.isBefore(minDate)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }

                        if (maxDate != null && item.isAfter(maxDate)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        });
    }
}
