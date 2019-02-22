package org.jevis.jeconfig.plugin.Dashboard;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordAnalysis;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widgets;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import javax.swing.event.ChangeEvent;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DashBoardPane extends Pane {

    //    private GridLayer gridLayer = new GridLayer();
    private static final Logger logger = LogManager.getLogger(DashBoardPane.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final DashBordAnalysis analysis;
    private ObservableList<Widget> widgetList = FXCollections.observableArrayList();
    private List<Double> xGrids = new ArrayList<>();
    private List<Double> yGrids = new ArrayList<>();
    private Scale scale = new Scale();
    private TimerTask updateTask;

    public DashBoardPane(DashBordAnalysis analysis) {
        super();

        this.analysis = analysis;

        this.analysis.getWidgets().forEach(widgetConfig -> {
            Widget widget = createWidget(widgetConfig);
            if (widget != null) {
                logger.info("Add widget: " + widget);
                addNode(widget);
            } else {
                logger.warn("Found no widget for config: {}", widgetConfig);
            }
        });

//        setStyle("-fx-background-color : lightblue;");
        addConfigListener();
        setBackground();

        getTransforms().add(scale);
        setOnScroll(event -> {
            if (event.isControlDown()) {

                if (event.getDeltaY() < 0) {
                    analysis.zoomOut();
                } else {
                    analysis.zoomIn();
                }
            }
        });

    }

    public Widget createWidget(WidgetConfig widget) {
        System.out.println("createWidget for: " + widget.getType());
        for (Widget availableWidget : Widgets.getAvabableWidgets(analysis.getDataSource())) {
            System.out.println("lll: " + availableWidget.typeID());
            if (availableWidget.typeID().equalsIgnoreCase(widget.getType())) {
                System.out.println("true");
                availableWidget.setConfig(widget);
                availableWidget.init();
                return availableWidget;
            }
        }

        return null;
    }

    private void addConfigListener() {
        ChangeListener sizeListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                updateChildren();
            }
        };
        this.heightProperty().addListener(sizeListener);
        this.widthProperty().addListener(sizeListener);

        analysis.zoomFactor.addListener((observable, oldValue, newValue) -> {
            System.out.println("Change zoom to: " + newValue);
            scale.setX(newValue.doubleValue());
            scale.setY(newValue.doubleValue());
            requestLayout();
        });
        analysis.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("Config changed update ui");
                updateChildren();
            }
        });

        analysis.imageBoardBackground.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && (oldValue == null || !oldValue.equals(newValue))) {
                setBackground();
            }
        });

        Timer timer = new Timer();

        analysis.updateIsRunningProperty.addListener((observable, oldValue, newValue) -> {
            if (updateTask != null) {
                try {
                    updateTask.cancel();
                } catch (Exception ex) {

                }
            }
            updateTask = updateTask();

            if (newValue) {
                timer.scheduleAtFixedRate(updateTask, 0, analysis.updateRate.getValue() * 1000);
            }
        });


    }

    private void setBackground() {
//        Background colorBackground = new Background(new BackgroundFill(analysis.colorDashBoardBackground.getValue(), CornerRadii.EMPTY, Insets.EMPTY));
//        analysis.colorDashBoardBackground.addListener((observable, oldValue, newValue) -> {
//            setBackground(new Background(new BackgroundFill(newValue, CornerRadii.EMPTY, Insets.EMPTY)));
//        });
//        final Image overlay = overlayImages.computeIfAbsent(
//                imageName,
//                image -> {
//                    return this.load(OverlayLoader.class, image, width, height);
//                });

//        final Image image = JEConfig.getImage("Dashbord.jpg");
        try {
            final Image image = analysis.imageBoardBackground.getValue();

            final BackgroundSize backgroundSize = new BackgroundSize(image.getWidth(), image.getHeight(), false, false, false, false);
            final BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
            final Background background = new Background(backgroundImage);
            setBackground(background);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public Interval buildInterval() {
        Period period = analysis.dataPeriodProperty.getValue();
        DateTime now = new DateTime();

        Interval interval = new Interval(now.minus(period), now);

        //TODO: remove this dev test workaround
        DateTime fakeDate = new DateTime(2018, 02, 01, 0, 0).plusHours(now.getHourOfDay()).plusMinutes(now.getMinuteOfHour());
        interval = new Interval(
                fakeDate.minusHours(2),
                fakeDate);

        return interval;
    }

    public TimerTask updateTask() {
        return new TimerTask() {
            @Override
            public void run() {
                logger.info("Starting Update");
                Interval interval = buildInterval();
                widgetList.forEach(widget -> {
                    logger.info("Update widget: {}", widget.getUUID());
                    widget.getSampleHandler().durationProperty.setValue(interval);
                    widget.getSampleHandler().update();
                });
            }
        };
    }

    public Runnable updateRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                logger.info("Starting Update");
                Interval interval = buildInterval();
                widgetList.forEach(widget -> {
                    logger.info("Update widget: {}", widget.getUUID());
                    widget.getSampleHandler().durationProperty.setValue(interval);
                    widget.getSampleHandler().update();
                });
            }
        };
    }


    /**
     * Add an grid to the pane.
     * <p>
     * TODO: make the grid bigger than the visible view, so the user can zoom out an still has an grid
     *
     * @param xWidth
     * @param height
     */
    public void setGrid(double xWidth, double height) {
        int maxColumns = Double.valueOf(DashBoardPane.this.getWidth() / xWidth).intValue() + 1;
        int maxRows = Double.valueOf(DashBoardPane.this.getHeight() / height).intValue() + 1;
        double opacity = 0.8;
        Double[] strokeDashArray = new Double[]{4d};
        xGrids.clear();
        yGrids.clear();

        /** rows **/
        for (int i = 0; i < maxColumns; i++) {
            double xPos = i * xWidth;
            xGrids.add(xPos);
            if (analysis.showGridProperty.getValue()) {
                Line line = new Line();
                line.setStartX(xPos);
                line.setStartY(0.0f);
                line.setEndX(xPos);
                line.setEndY(DashBoardPane.this.getHeight());
                line.getStrokeDashArray().addAll(strokeDashArray);
                DashBoardPane.this.getChildren().add(line);
                line.setOpacity(opacity);
            }

        }

        /** columns **/
        for (int i = 0; i < maxRows; i++) {
            double yPos = i * height;
            yGrids.add(yPos);
            if (analysis.showGridProperty.getValue()) {
                Line line = new Line();
                line.setStartX(0);
                line.setStartY(yPos);
                line.setEndX(DashBoardPane.this.getWidth());
                line.setEndY(yPos);
                line.getStrokeDashArray().addAll(strokeDashArray);
                line.setOpacity(opacity);
                DashBoardPane.this.getChildren().add(line);
            }
        }

    }

    public void removeNode(Widget widget) {
        widgetList.remove(widget);
    }

    public void addNode(Widget widget) {
        widgetList.add(widget);
        widget.init();
        widget.setDashBoard(this);
//        getChildren().add(widget);
    }


    public double getNextGridX(double xPos) {
        double c = xGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - xPos)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
//        System.out.println("Next xPos: " + c);
        return c;
    }

    public double getNextGridY(double yPos) {
        double c = yGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - yPos)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
//        System.out.println("Next yPos: " + c);
        return c;
    }

    public DashBordAnalysis getDashBordAnalysis() {
        return analysis;
    }

    public void updateChildren() {
        System.out.println("Update");
        getChildren().clear();
        setGrid(analysis.xGridInterval.get(), analysis.yGridInterval.get());


        widgetList.forEach(node -> {
            getChildren().add(node);
        });

//        getChildren().clear();s
//        getChildren().add(gridLayer);
//        widgetList.forEach(node -> {
//            getChildren().add(node);
//        });
    }

}
