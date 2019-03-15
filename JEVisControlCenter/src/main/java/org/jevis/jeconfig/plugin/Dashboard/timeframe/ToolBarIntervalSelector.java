package org.jevis.jeconfig.plugin.Dashboard.timeframe;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.joda.time.DateTime;
import org.joda.time.Interval;

public class ToolBarIntervalSelector extends FlowPane {


    public ObjectProperty<Interval> intervalProperty = new SimpleObjectProperty<>();
    public ObjectProperty<TimeFrameFactory> timeFrameProperty = new SimpleObjectProperty<>();
    public ObjectProperty<DateTime> dateTimereferrenzProperty = new SimpleObjectProperty<>(new DateTime());


    public ToolBarIntervalSelector(Double iconSize, final Interval interval) {
        super();
        intervalProperty.addListener((observable, oldValue, newValue) -> {
            System.out.println("New Interval selected: " + newValue);
        });

        intervalProperty.setValue(interval);
        Button dateButton = new Button("");
        dateButton.setMinWidth(100);

        ToggleButton prevButton = new ToggleButton("", JEConfig.getImage("arrow_left.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(prevButton);

        ToggleButton nextButton = new ToggleButton("", JEConfig.getImage("arrow_right.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(nextButton);


        ComboBox<TimeFrameFactory> timeFrameBox = new ComboBox();
        timeFrameBox.setPrefWidth(100);
        timeFrameBox.setMinWidth(100);

        Callback<ListView<TimeFrameFactory>, ListCell<TimeFrameFactory>> cellFactory = new Callback<ListView<TimeFrameFactory>, ListCell<TimeFrameFactory>>() {
            @Override
            public ListCell<TimeFrameFactory> call(ListView<TimeFrameFactory> param) {
                final ListCell<TimeFrameFactory> cell = new ListCell<TimeFrameFactory>() {

//                    {
//                        super.setPrefWidth(300);
//                    }

                    @Override
                    protected void updateItem(TimeFrameFactory item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getListName());
                            setGraphic(null);
                        }


                    }
                };

                return cell;
            }
        };
        TimeFrames timeFrames = new TimeFrames();
        timeFrameBox.setItems(timeFrames.getAll());

        intervalProperty.addListener((observable, oldValue, newValue) -> {
            try {
                dateButton.setText(timeFrameBox.valueProperty().getValue().format(intervalProperty.getValue()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        dateButton.setOnAction(event -> {
            System.out.println("Show edior");
            Point2D point = dateButton.localToScreen(0.0, 0.0);
            System.out.println("Button Location: " + point);

            TimeFrameEdior popup = new TimeFrameEdior(TimeFrames.TimeFrameType.valueOf(timeFrameProperty.getValue().getID()), intervalProperty.getValue());
            popup.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    intervalProperty.setValue(timeFrameProperty.getValue().getInterval(popup.getIntervalProperty().getValue().getStart()));
                }

            });
            popup.show(dateButton, point.getX() - 40, point.getY() + 40);
            System.out.println("...");
        });

        timeFrameBox.setCellFactory(cellFactory);
        timeFrameBox.setButtonCell(cellFactory.call(null));
        timeFrameBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            intervalProperty.setValue(newValue.getInterval(new DateTime()));
            timeFrameProperty.setValue(newValue);
        });


        prevButton.setOnAction(event -> {
            intervalProperty.setValue(timeFrameProperty.getValue().previousPeriod(intervalProperty.get(), 1));
        });

        nextButton.setOnAction(event -> {
            intervalProperty.setValue(timeFrameProperty.getValue().nextPeriod(intervalProperty.get(), 1));
        });

        Region spacer = new Region();
        spacer.setMinWidth(10);

        getChildren().addAll(timeFrameBox, spacer, prevButton, dateButton, nextButton);

        Platform.runLater(() -> {
            timeFrameBox.getSelectionModel().selectFirst();
        });


    }

    public ObjectProperty<TimeFrameFactory> getTimeFrameProperty() {
        return timeFrameProperty;
    }

    public ObjectProperty<Interval> getIntervalProperty() {
        return intervalProperty;
    }


}
