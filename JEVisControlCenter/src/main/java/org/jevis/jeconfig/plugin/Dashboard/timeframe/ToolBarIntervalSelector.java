package org.jevis.jeconfig.plugin.Dashboard.timeframe;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordModel;
import org.joda.time.DateTime;
import org.joda.time.Interval;

public class ToolBarIntervalSelector extends HBox {

    private static final Logger logger = LogManager.getLogger(ToolBarIntervalSelector.class);
    private final DashBordModel analysis;
    private final JEVisDataSource ds;
    private TimeFrameEdior popup;

    public ToolBarIntervalSelector(JEVisDataSource dataSource, DashBordModel analysis, Double iconSize, final Interval interval) {
        super();
        this.ds = dataSource;
        this.analysis = analysis;


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
        timeFrames.setWorkdays(analysis.getAnalysisObject());
        timeFrameBox.setItems(timeFrames.getAll());


        analysis.intervalProperty.addListener((observable, oldValue, newValue) -> {
            try {
                if (analysis.timeFrameProperty.getValue() != null) {
                    Platform.runLater(() -> {
                        dateButton.setText(analysis.timeFrameProperty.getValue().format(analysis.intervalProperty.getValue()));
                    });
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        });

        popup = new TimeFrameEdior(analysis.timeFrameProperty, analysis.intervalProperty);

        dateButton.setOnAction(event -> {
            if (popup.isShowing()) {
                popup.hide();
            } else {
                popup.setDate(analysis.intervalProperty.getValue().getEnd());
                Point2D point = dateButton.localToScreen(0.0, 0.0);
                popup.show(dateButton, point.getX() - 40, point.getY() + 40);
            }
        });


        timeFrameBox.setCellFactory(cellFactory);
        timeFrameBox.setButtonCell(cellFactory.call(null));
        timeFrameBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && oldValue.getID().equals(newValue.getID())) {
                return;
            }
            analysis.timeFrameProperty.setValue(newValue);
            if (analysis.intervalProperty.get() != null) {
                analysis.intervalProperty.setValue(newValue.getInterval(analysis.intervalProperty.get().getEnd()));
            } else {
                analysis.intervalProperty.setValue(newValue.getInterval(new DateTime()));
            }

        });


        prevButton.setOnAction(event -> {
            Interval nextInterval = analysis.timeFrameProperty.getValue().previousPeriod(analysis.intervalProperty.get(),1);
            if(nextInterval.getStart().isBeforeNow()){
                analysis.intervalProperty.setValue(analysis.timeFrameProperty.getValue().previousPeriod(analysis.intervalProperty.get(), 1));
            }
        });

        nextButton.setOnAction(event -> {
            Interval nextInterval = analysis.timeFrameProperty.getValue().nextPeriod(analysis.intervalProperty.get(),1);
//            analysis.intervalProperty.setValue(analysis.timeFrameProperty.getValue().nextPeriod(analysis.intervalProperty.get(), 1));
            if(nextInterval.getStart().isBeforeNow()){
                analysis.intervalProperty.setValue(analysis.timeFrameProperty.getValue().nextPeriod(analysis.intervalProperty.get(), 1));
            }
        });

        Region spacer = new Region();
        spacer.setMinWidth(10);
        spacer.setMaxWidth(10);

        getChildren().addAll(timeFrameBox, spacer, prevButton, dateButton, nextButton);


        Platform.runLater(() -> {
            ToolBarIntervalSelector.this.setDisable(analysis.disableIntervalUI.get());
            nextButton.setDisable(analysis.disableIntervalUI.get());
            prevButton.setDisable(analysis.disableIntervalUI.get());
            dateButton.setDisable(analysis.disableIntervalUI.get());
            timeFrameBox.setDisable(analysis.disableIntervalUI.get());

            timeFrameBox.getItems().forEach(timeFrameFactory -> {
                if (analysis.timeFrameProperty.getValue().getID().equals(timeFrameFactory.getID())) {
                    timeFrameBox.getSelectionModel().select(timeFrameFactory);
                    return;
                }
            });

        });


    }


    public ObjectProperty<Interval> getIntervalProperty() {
        return analysis.intervalProperty;
    }


}
