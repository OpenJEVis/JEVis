package org.jevis.jeconfig.plugin.Dashboard;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.WorkIndicatorDialog;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordModel;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.widget.Size;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widgets;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.swing.event.ChangeEvent;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class DashBoardPane extends Pane {

    //    private GridLayer gridLayer = new GridLayer();
    private static final Logger logger = LogManager.getLogger(DashBoardPane.class);
    private final DashBordModel analysis;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private ObservableList<Widget> widgetList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private List<Double> xGrids = new ArrayList<>();
    private List<Double> yGrids = new ArrayList<>();
    private Scale scale = new Scale();
    private TimerTask updateTask;
    private WorkIndicatorDialog workIndicatorDialog = new WorkIndicatorDialog(JEConfig.getStage().getScene().getWindow(), "Aktualisiere...");
    private Task<Integer> loadingTask;
    private int jopsDone = 0;
    private AtomicBoolean isUpdating = new AtomicBoolean(false);

    public DashBoardPane(DashBordModel analysis) {
        super();
        logger.debug("Start DashBoardPane");

        showLoading(true);

        this.analysis = analysis;

        this.analysis.pageSize.addListener((observable, oldValue, newValue) -> {
            setWidth(newValue.getWidth());
            setHeight(newValue.getHeight());
        });


        this.analysis.getWidgets().forEach(widgetConfig -> {
            try {
                Widget widget = createWidget(widgetConfig);
                if (widget != null) {
                    logger.debug("Add widget: {}-{}", widget.typeID(), widget.getConfig().title);
                    addNode(widget);
                } else {
                    logger.error("Found no widget for config: {}", widgetConfig);
                }
            } catch (Exception ex) {
                logger.error(ex);
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

        setSize(this.analysis.pageSize.get());
        this.analysis.pageSize.addListener((observable, oldValue, newValue) -> {
            setSize(newValue);
        });


        showLoading(false);
        logger.debug("Done");
    }

    private void showLoading(boolean isLoading) {


        if (isLoading) {
            Platform.runLater(() -> {
                JEConfig.getStage().getScene().setCursor(Cursor.WAIT);
            });


        } else {
            Platform.runLater(() -> {
                JEConfig.getStage().getScene().setCursor(Cursor.DEFAULT);
            });
        }


    }

    private void setSize(Size newValue) {
        this.setMaxWidth(newValue.getWidth());
        this.setMinWidth(newValue.getWidth());
        this.setPrefWidth(newValue.getWidth());
        this.setMaxHeight(newValue.getHeight());
        this.setMinHeight(newValue.getHeight());
        this.setPrefHeight(newValue.getHeight());
    }

    public Widget createWidget(WidgetConfig widget) {
        for (Widget availableWidget : Widgets.getAvabableWidgets(analysis.getDataSource(), widget)) {
            try {
                if (availableWidget.typeID().equalsIgnoreCase(widget.getType())) {
                    widget.setType(availableWidget.getId());
                    availableWidget.init();

                    return availableWidget;
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return null;
    }

    private void addConfigListener() {
        ChangeListener sizeListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//                System.out.println(" ChangeListener sizeListener");
                updateChildren();
            }
        };
        this.heightProperty().addListener(sizeListener);
        this.widthProperty().addListener(sizeListener);

        analysis.pageSize.addListener((observable, oldValue, newValue) -> {
            setWidth(newValue.getWidth());
            setHeight(newValue.getHeight());
            setBackground();
        });

        analysis.zoomFactor.addListener((observable, oldValue, newValue) -> {
//            System.out.println("Change zoom to: " + newValue);
            scale.setX(newValue.doubleValue());
            scale.setY(newValue.doubleValue());
            requestLayout();
        });
        analysis.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("analysis.addChangeListener");
                updateChildren();
            }
        });

        analysis.imageBoardBackground.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && (oldValue == null || !oldValue.equals(newValue))) {
                setBackground();
            }
        });

        Timer timer = new Timer(true);

        analysis.intervalProperty.addListener((observable, oldValue, newValue) -> {
            logger.debug("New Interval: {}", newValue);
        });

        analysis.updateIsRunningProperty.addListener((observable, oldValue, newValue) -> {
            if (updateTask != null) {
                try {
                    updateTask.cancel();
                } catch (Exception ex) {

                }
            }
            updateTask = updateTask();

            if (newValue) {
                logger.info("Start update scheduler: {} sec", analysis.updateRate.getValue());
                timer.scheduleAtFixedRate(updateTask, 1000, analysis.updateRate.getValue() * 1000);
            }
        });

        analysis.displayedIntervalProperty.addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                showLoading(true);

                widgetList.forEach(widget -> {

                    addWidgetUpdateTask(widget, newValue);
                });
                showLoading(false);
            }

        });


    }

    private void addWidgetUpdateTask(Widget widget, Interval interval) {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                try {
                    widget.showProgressIndicator(true);
                    widget.update(interval);
                    widget.showProgressIndicator(false);
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        };
        executor.execute(updateTask);
    }

    private void setBackground() {
        try {
            final Image image = analysis.imageBoardBackground.getValue();

//            final BackgroundSize backgroundSize = new BackgroundSize(image.getWidth(), image.getHeight(), false, false, false, false);
//            final BackgroundSize backgroundSize = new BackgroundSize(analysis.pageSize.get().getWidth(), analysis.pageSize.get().getHeight(), false, false, false, false);
            final BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);

            final BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
            final Background background = new Background(backgroundImage);
            setBackground(background);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public Interval buildInterval() {
        System.out.println("buildInterval");
        analysis.intervalProperty.setValue(analysis.timeFrameProperty.getValue().getInterval(DateTime.now()));
//        logger.error("New Interval: " + analysis.intervalProperty.getValue());

        return analysis.intervalProperty.getValue();
    }

    public TimerTask updateTask() {
        return new TimerTask() {
            @Override
            public void run() {
                logger.debug("Starting Update");

                showLoading(true);

                Interval interval = buildInterval();
                widgetList.forEach(widget -> {
                    logger.debug("Update widget: {}", widget.getConfig().title.get());
                    addWidgetUpdateTask(widget, interval);
                });
                showLoading(false);
                logger.debug("Update done");
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

    public synchronized void addNode(Widget widget) {
        if(!widgetList.contains(widget)){
            logger.debug("Add widget to pane: {}", widget);
            widgetList.add(widget);
            widget.setDashBoard(this);
        }
    }


    public double getNextGridX(double xPos) {
        if (!analysis.snapToGridProperty.getValue()) {
            return xPos;
        }
        double c = xGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - xPos)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
//        System.out.println("Next xPos: " + c);
        return c;
    }

    public double getNextGridY(double yPos) {
        if (!analysis.snapToGridProperty.getValue()) {
            return yPos;
        }
        double c = yGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - yPos)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
//        System.out.println("Next yPos: " + c);
        return c;
    }

    public DashBordModel getDashBordAnalysis() {
        return analysis;
    }

    private void printChildren(){
        System.out.println("---");
        for(Widget node:widgetList){
            String id= "";

            if(node instanceof Widget){
                id= ((Widget)node).getId();
            }

            System.out.println("Child: "+id+"  "+node.getClass()+" "+node);
        }
    }

    public void updateChildren() {
        if(!isUpdating.get()){
//            System.out.println("Start updateChildren");
            isUpdating.set(true);
//            printChildren();
//            try {
//                printChildren();
//
//                System.out.println("uc------");
//                widgetList.forEach(widget -> {
//                    System.out.println("Widget: " + widget.getConfig().xPosition.get() + "/" + widget.getConfig().yPosition.get() + "  " + widget);
//                });
//            }catch (Exception ex){
//                logger.error(ex);
//            }
            Platform.runLater(() -> {
                try {
                    getChildren().setAll(widgetList);
                    setGrid(analysis.xGridInterval.get(), analysis.yGridInterval.get());
                    isUpdating.set(false);
                }catch (Exception ex){
                    logger.error("Thread problem: {}",ex.getMessage());
                    isUpdating.set(false);
                }
            });

        }



    }

}
