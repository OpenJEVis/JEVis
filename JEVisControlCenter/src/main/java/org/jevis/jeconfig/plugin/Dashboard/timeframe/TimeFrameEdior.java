package org.jevis.jeconfig.plugin.Dashboard.timeframe;

import com.sun.javafx.scene.control.skin.DatePickerSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.time.LocalDate;

public class TimeFrameEdior extends Popup {

    public ObjectProperty<Interval> intervalProperty = new SimpleObjectProperty<>();

    DatePicker datePicker;

    public TimeFrameEdior(TimeFrames.TimeFrameType type, Interval interval) {
        super();
//        setAutoHide(false);
        datePicker = new DatePicker(toLocalDate(interval.getEnd()));
        datePicker.setShowWeekNumbers(true);
//        datePicker.show();
//        JFXDatePicker jfxDatePicker = new JFXDatePicker(toLocalDate(interval.getEnd()));

        DatePickerSkin datePickerSkin = new DatePickerSkin(datePicker);
        Node popupContent = datePickerSkin.getPopupContent();

        Button ok = new Button("OK");
        ok.setDefaultButton(true);
        ok.setAlignment(Pos.CENTER);

        ok.setOnAction(event -> {
            setIntervalresult();
            this.hide();
        });

        BorderPane borderPane = new BorderPane();
//        borderPane.setPadding(new Insets(20));
        borderPane.setCenter(popupContent);
//        BorderPane buttonPane = new BorderPane();
//        buttonPane.setCenter(ok);
//        borderPane.setBottom(buttonPane);

//        borderPane.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
//

//        VBox vBox = new VBox(20);
//        vBox.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
//
//        vBox.setStyle("-fx-background-color:#f5f5f5; -fx-opacity:1;");
//        vBox.getChildren().addAll(popupContent, ok);
        getContent().add(borderPane);
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                setIntervalresult();
                this.hide();
            }

        });
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            setIntervalresult();
            this.hide();
        });

    }


    private void dayPicker() {

    }


    private void setIntervalresult() {
        DateTime newDateTime = new DateTime(datePicker.valueProperty().getValue().getYear(), datePicker.valueProperty().getValue().getMonthValue(), datePicker.valueProperty().getValue().getDayOfMonth(), 0, 0);
        Interval newInterval = new Interval(newDateTime, newDateTime);
        intervalProperty.setValue(newInterval);
    }

    public ObjectProperty<Interval> getIntervalProperty() {
        return intervalProperty;
    }

    public LocalDate toLocalDate(DateTime dateTime) {
        LocalDate newLocalDate = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        return newLocalDate;
    }


}
