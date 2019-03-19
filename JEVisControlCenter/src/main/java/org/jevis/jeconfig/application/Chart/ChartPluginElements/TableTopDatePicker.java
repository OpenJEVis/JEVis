package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.DateTimePicker.DatePicker;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.List;

public class TableTopDatePicker extends HBox {


    private DatePicker datePicker;
    private ImageView leftImage;
    private ImageView rightImage;

    public TableTopDatePicker() {
        super();
    }

    public void initialize(List<ChartDataModel> chartDataModels, DateTime date) {
        setPadding(new Insets(4, 4, 4, 4));
        LocalDate ld = LocalDate.of(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        datePicker = new DatePicker(ld);

        datePicker.setChartDataModels(chartDataModels);

        leftImage = JEConfig.getImage("left.png", 20, 20);
        rightImage = JEConfig.getImage("right.png", 20, 20);
        Separator sep1 = new Separator(Orientation.VERTICAL);
        Separator sep2 = new Separator(Orientation.VERTICAL);

        getChildren().addAll(leftImage, sep1, datePicker, sep2, rightImage);
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public ImageView getLeftImage() {
        return leftImage;
    }

    public ImageView getRightImage() {
        return rightImage;
    }
}
