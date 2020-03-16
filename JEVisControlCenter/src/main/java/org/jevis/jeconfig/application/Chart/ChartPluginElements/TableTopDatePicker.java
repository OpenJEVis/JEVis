package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.jevis.api.JEVisException;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.ChartDataModel;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TableTopDatePicker extends HBox {


    private final ChartDataModel chartDataModel;
    private ImageView leftImage;
    private ImageView rightImage;
    private ComboBox<DateTime> selectionBox;

    public TableTopDatePicker(ChartDataModel chartDataModel) {
        super();
        this.chartDataModel = chartDataModel;
    }

    public void initialize(DateTime date) {
        setPadding(new Insets(4, 4, 4, 4));
        LocalDate ld = LocalDate.of(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());

        List<DateTime> dates = new ArrayList<>();
        chartDataModel.getSamples().forEach(jeVisSample -> {
            try {
                dates.add(jeVisSample.getTimestamp());
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });
        selectionBox = new ComboBox<>(FXCollections.observableArrayList(dates));
        Callback<ListView<DateTime>, ListCell<DateTime>> cellFactory = new Callback<ListView<DateTime>, ListCell<DateTime>>() {
            @Override
            public ListCell<DateTime> call(ListView<DateTime> param) {
                return new ListCell<DateTime>() {
                    @Override
                    protected void updateItem(DateTime obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(obj.toString("yyyy-MM-dd HH:mm"));
                        }
                    }
                };
            }
        };

        selectionBox.setCellFactory(cellFactory);
        selectionBox.setButtonCell(cellFactory.call(null));

        selectionBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                if (!dates.contains(newValue)) {
                    DateTime correctTimestamp = null;
                    for (DateTime dateTime : dates) {
                        if (dateTime.isAfter(newValue)) {
                            correctTimestamp = dateTime;
                            break;
                        }
                    }
                    if (correctTimestamp != null) {
                        selectionBox.getSelectionModel().select(correctTimestamp);
                    } else if (dates.size() > 0) {
                        selectionBox.getSelectionModel().select(dates.get(dates.size() - 1));
                    }
                }
            }
        });

        leftImage = JEConfig.getImage("left.png", 20, 20);
        rightImage = JEConfig.getImage("right.png", 20, 20);
        Separator sep1 = new Separator(Orientation.VERTICAL);
        Separator sep2 = new Separator(Orientation.VERTICAL);

        getChildren().addAll(leftImage, sep1, selectionBox, sep2, rightImage);

        selectionBox.getSelectionModel().select(date);
    }

    public ComboBox<DateTime> getDatePicker() {
        return selectionBox;
    }

    public ImageView getLeftImage() {
        return leftImage;
    }

    public ImageView getRightImage() {
        return rightImage;
    }
}
