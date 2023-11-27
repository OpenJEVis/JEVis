package org.jevis.jecc.application.Chart.ChartPluginElements;


import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jevis.api.JEVisException;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

public class TableTopDatePicker extends HBox {


    private final ImageView leftImage;
    private final ImageView rightImage;
    private final ComboBox<DateTime> selectionBox;

    public TableTopDatePicker() {
        super();
        setAlignment(Pos.CENTER);
        selectionBox = new ComboBox<>();
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

        //TODO JFX17
        selectionBox.setConverter(new StringConverter<DateTime>() {
            @Override
            public String toString(DateTime object) {
                return object.toString("yyyy-MM-dd HH:mm");
            }

            @Override
            public DateTime fromString(String string) {
                return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(string);
            }
        });

        leftImage = ControlCenter.getImage("left.png", 20, 20);
        rightImage = ControlCenter.getImage("right.png", 20, 20);
        Separator sep1 = new Separator(Orientation.VERTICAL);
        Separator sep2 = new Separator(Orientation.VERTICAL);

        getChildren().setAll(leftImage, sep1, selectionBox, sep2, rightImage);
    }

    public void initialize(ChartDataRow chartDataRow, DateTime date) {
        setPadding(new Insets(4, 4, 4, 4));

        List<DateTime> dates = new ArrayList<>();
        chartDataRow.getSamples().forEach(jeVisSample -> {
            try {
                dates.add(jeVisSample.getTimestamp());
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });
        selectionBox.getItems().addAll(dates);

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

        Platform.runLater(() -> selectionBox.getSelectionModel().select(date));
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
