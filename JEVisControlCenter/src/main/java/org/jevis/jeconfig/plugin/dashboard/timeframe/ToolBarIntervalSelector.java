package org.jevis.jeconfig.plugin.dashboard.timeframe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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


        TimeFactoryBox timeFactoryBox = new TimeFactoryBox(false);
        timeFactoryBox.setPrefWidth(200);
        timeFactoryBox.setMinWidth(200);

        ObservableList<TimeFrameFactory> timeFrames = FXCollections.observableArrayList(controller.getAllTimeFrames().getAll());
        timeFactoryBox.getItems().addAll(timeFrames);


        dateButton.setText(controller.getActiveTimeFrame().format(controller.getInterval()));
        dateButton.setTooltip(new Tooltip(controller.getInterval().toString()));

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

        timeFactoryBox.selectValue(controller.getActiveTimeFrame());

        timeFactoryBox.valueProperty().addListener((observable, oldValue, newValue) -> {
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

        getChildren().addAll(timeFactoryBox, spacer, prevButton, dateButton, nextButton);


    }


}
