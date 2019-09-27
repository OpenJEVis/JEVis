package org.jevis.jeconfig.plugin.dashboard.timeframe;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;

public class ToolBarIntervalSelector extends HBox {

    private static final Logger logger = LogManager.getLogger(ToolBarIntervalSelector.class);
    private TimeFrameEdior popup;

    public ToolBarIntervalSelector(JEVisDataSource ds, DashboardControl controller, Double iconSize) {
        super();
//        this.setStyle("-fx-background-color:orangered;");
        this.setAlignment(Pos.CENTER_LEFT);
        Button dateButton = new Button("");
        dateButton.setMinWidth(100);

        ToggleButton prevButton = new ToggleButton("", JEConfig.getImage("arrow_left.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(prevButton);

        ToggleButton nextButton = new ToggleButton("", JEConfig.getImage("arrow_right.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(nextButton);


        ComboBox<TimeFrameFactory> timeFrameBox = new ComboBox();
        timeFrameBox.setPrefWidth(200);
        timeFrameBox.setMinWidth(200);

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


        TimeFrames timeFrames = new TimeFrames(ds);
        timeFrames.setWorkdays(controller.getActiveDashboard().getDashboardObject());
        timeFrameBox.setItems(timeFrames.getAll());

        dateButton.setText(controller.getActiveTimeFrame().format(controller.getInterval()));


        this.popup = new TimeFrameEdior(controller.getActiveTimeFrame(), controller.getInterval());
        this.popup.getIntervalProperty().addListener((observable, oldValue, newValue) -> {
            controller.setInterval(newValue);
        });

        dateButton.setOnAction(event -> {
            if (this.popup.isShowing()) {
                this.popup.hide();
            } else {
                this.popup.setDate(controller.getInterval().getEnd());
                Point2D point = dateButton.localToScreen(0.0, 0.0);
                this.popup.show(dateButton, point.getX() - 40, point.getY() + 40);
            }
        });


        timeFrameBox.setCellFactory(cellFactory);
        timeFrameBox.setButtonCell(cellFactory.call(null));
        timeFrameBox.getItems().forEach(timeFrameFactory -> {
            if (controller.getActiveTimeFrame().getID().equals(timeFrameFactory.getID())) {
                timeFrameBox.getSelectionModel().select(timeFrameFactory);
                return;
            }
        });

        timeFrameBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("timeFrameBox: " + oldValue + " // " + newValue);
            if (oldValue != null && oldValue.getID().equals(newValue.getID())) {
                return;
            }
            controller.setActiveTimeFrame(newValue);
        });


        prevButton.setOnAction(event -> {
            controller.setPrevInteval();
        });

        nextButton.setOnAction(event -> {
            controller.setNextInterval();
        });

        Region spacer = new Region();
        spacer.setMinWidth(10);
        spacer.setMaxWidth(10);

        getChildren().addAll(timeFrameBox, spacer, prevButton, dateButton, nextButton);


//        Platform.runLater(() -> {
////            ToolBarIntervalSelector.this.setDisable(controller.disableIntervalUI.get());
////            nextButton.setDisable(controller.disableIntervalUI.get());
////            prevButton.setDisable(controller.disableIntervalUI.get());
////            dateButton.setDisable(controller.disableIntervalUI.get());
////            timeFrameBox.setDisable(controller.disableIntervalUI.get());
//
//            timeFrameBox.getItems().forEach(timeFrameFactory -> {
//                if (controller.getActiveTimeFrame().getID().equals(timeFrameFactory.getID())) {
//                    timeFrameBox.getSelectionModel().select(timeFrameFactory);
//                    return;
//                }
//            });
//
//        });


    }


}
