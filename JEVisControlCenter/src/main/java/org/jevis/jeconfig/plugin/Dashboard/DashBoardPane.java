package org.jevis.jeconfig.plugin.Dashboard;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;
import javafx.stage.Popup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.WorkIndicatorDialog;
import org.jevis.jeconfig.dialog.HiddenConfig;
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
    private ExecutorService executor;
    private ObservableList<Widget> widgetList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private List<Double> xGrids = new ArrayList<>();
    private List<Double> yGrids = new ArrayList<>();
    private Scale scale = new Scale();
    private TimerTask updateTask;
    private WorkIndicatorDialog workIndicatorDialog = new WorkIndicatorDialog(JEConfig.getStage().getScene().getWindow(), "Aktualisiere...");
    private Task<Integer> loadingTask;
    private int jopsDone = 0;
    private AtomicBoolean isUpdating = new AtomicBoolean(false);
    private List<Line> visibleGrid = new ArrayList<>();
    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Interval loadedInterval = null;
    private ObservableList<Task> runningUpdateTaskList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    public DashBoardPane(DashBordModel analysis) {
        super();
        logger.debug("Start DashBoardPane");

        this.executor = Executors.newFixedThreadPool(HiddenConfig.DASH_THREADS);

        showLoading(true);

        this.analysis = analysis;

        this.analysis.pageSize.addListener((observable, oldValue, newValue) -> {
            setWidth(newValue.getWidth());
            setHeight(newValue.getHeight());
        });


        ChangeListener<Number> gridListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                createGrid(analysis.xGridInterval.get(), analysis.yGridInterval.get());
            }
        };

        this.analysis.xGridInterval.addListener(gridListener);
        this.analysis.yGridInterval.addListener(gridListener);

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

        getTransforms().add(this.scale);
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

        createGrid(analysis.xGridInterval.get(), analysis.yGridInterval.get());

        setOnKeyPressed(event -> {
            if (event.isAltDown()) {

            }
        });

        Popup popup = new Popup();
        Popup linePopup = new Popup();

        final DoubleProperty lastAltX = new SimpleDoubleProperty(0d);
        final DoubleProperty lastAltY = new SimpleDoubleProperty(0d);

        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isAltDown()) {
                    String text;
                    Double lineStartX;
                    Double lineStartY;
                    double lenght = 0;
                    double height = 0;

                    if (popup.isShowing()) {
                        text = String.format("x: %s -> %s [%s]\ny: %s -> %s [%s]"
                                , lastAltX.get(), event.getX(), (lastAltX.get() - event.getX())
                                , lastAltY.get(), event.getY(), (lastAltY.get() - event.getY()));

                        /**
                         * Not working red line
                         */
//                        if(event.getX()>lastAltX.get()){
//                            lenght=event.getX()-lastAltX.get();
//                            lineStartX=lastAltX.get();
//                        }else{
//                            lenght=lastAltX.get()-event.getX();
//                            lineStartX=event.getX();
//                        }
//
//                        if(event.getY()>lastAltY.get()){
//                            height=event.getY()-lastAltY.get();
//                            lineStartY=lastAltY.get();
//                        }else{
//                            height=lastAltY.get()-event.getY();
//                            lineStartY=event.getY();
//                        }
//
//
//                        Line line = new Line();
//                        line.setStartX(0);
//                        line.setStartY(0);
//                        line.setEndX(lenght);
//                        line.setEndY(height);
//                        line.setFill(Color.RED);
//
//                        linePopup.getContent().setAll(line);
//                        popup.show(DashBoardPane.this,event.getScreenX(),event.getScreenY()-20);

                    } else {
                        text = String.format("x: %s\ny: %s", event.getX(), event.getY());
                    }


                    Label label = new Label(text);
                    popup.getContent().setAll(label);
                    lastAltX.setValue(event.getX());
                    lastAltY.setValue(event.getY());

                    popup.show(DashBoardPane.this, event.getScreenX(), event.getScreenY() - 20);
                } else {
                    popup.hide();
                }
            }
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
        for (Widget availableWidget : Widgets.getAvabableWidgets(this.analysis.getDataSource(), widget)) {
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

        this.analysis.pageSize.addListener((observable, oldValue, newValue) -> {
            setWidth(newValue.getWidth());
            setHeight(newValue.getHeight());
            setBackground();
        });

        this.analysis.zoomFactor.addListener((observable, oldValue, newValue) -> {
//            System.out.println("Change zoom to: " + newValue);
            this.scale.setX(newValue.doubleValue());
            this.scale.setY(newValue.doubleValue());
            requestLayout();
        });
        this.analysis.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateChildren();
            }
        });

        this.analysis.imageBoardBackground.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && (oldValue == null || !oldValue.equals(newValue))) {
                setBackground();
            }
        });

        Timer timer = new Timer(true);

        this.analysis.intervalProperty.addListener((observable, oldValue, newValue) -> {
            logger.debug("New Interval: {}", newValue);
        });

        this.analysis.updateIsRunningProperty.addListener((observable, oldValue, newValue) -> {
            if (this.updateTask != null) {
                try {
                    this.updateTask.cancel();
                } catch (Exception ex) {

                }
            }
            this.updateTask = updateTimerTask();

            if (newValue) {
                logger.info("Start update scheduler: {} sec", this.analysis.updateRate.getValue());
                timer.scheduleAtFixedRate(this.updateTask, 1000, this.analysis.updateRate.getValue() * 1000);
            }
        });

        this.analysis.displayedIntervalProperty.addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                showLoading(true);

                for (Task task : this.runningUpdateTaskList) {
                    task.cancel();
                }
                this.runningUpdateTaskList.clear();

                try {
                    this.widgetList.forEach(widget -> {
                        addWidgetUpdateTask(widget, newValue);
                    });
                    showLoading(false);
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }

        });

        this.analysis.showGridProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                showGrid(newValue);
            }
        });


    }

    private void addWidgetUpdateTask(Widget widget, Interval interval) {
        if (widget == null || interval == null) {
            logger.error("widget is null, this should not happen");
            return;
        }
        Task updateTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    logger.debug("addWidgetUpdateTask: " + widget.typeID());
                    widget.showProgressIndicator(true);
                    widget.update(interval);
                    widget.showProgressIndicator(false);
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
                return null;
            }
        };

        this.runningUpdateTaskList.add(updateTask);
        this.executor.execute(updateTask);
    }

    private void setBackground() {
        try {
            final Image image = this.analysis.imageBoardBackground.getValue();

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
        this.analysis.intervalProperty.setValue(this.analysis.timeFrameProperty.getValue().getInterval(DateTime.now()));

        return this.analysis.intervalProperty.getValue();
    }

    public TimerTask updateTimerTask() {

        return new TimerTask() {
            @Override
            public void run() {
                logger.debug("Starting Update");

                showLoading(true);

                Interval interval = buildInterval();


                DashBoardPane.this.widgetList.parallelStream().forEach(widget -> {
                    addWidgetUpdateTask(widget, interval);
                });
                showLoading(false);
            }
        };
    }


    private void showGrid(boolean show) {
        if (show) {
            DashBoardPane.this.getChildren().addAll(this.visibleGrid);
        } else {
            DashBoardPane.this.getChildren().removeAll(this.visibleGrid);
        }
    }

    /**
     * Add an grid to the pane.
     * <p>
     * TODO: make the grid bigger than the visible view, so the user can zoom out an still has an grid
     *
     * @param xWidth
     * @param height
     */
    public void createGrid(double xWidth, double height) {
        int maxColumns = Double.valueOf(DashBoardPane.this.getWidth() / xWidth).intValue() + 1;
        int maxRows = Double.valueOf(DashBoardPane.this.getHeight() / height).intValue() + 1;
        double opacity = 0.4;
        Double[] strokeDashArray = new Double[]{4d};
        this.xGrids.clear();
        this.yGrids.clear();
        this.visibleGrid.clear();

        /** rows **/
        for (int i = 0; i < maxColumns; i++) {
            double xPos = i * xWidth;
            this.xGrids.add(xPos);


            Line line = new Line();
            line.setStartX(xPos);
            line.setStartY(0.0f);
            line.setEndX(xPos);
            line.setEndY(DashBoardPane.this.getHeight());
            line.getStrokeDashArray().addAll(strokeDashArray);
            line.setOpacity(opacity);
            this.visibleGrid.add(line);

        }

        /** columns **/
        for (int i = 0; i < maxRows; i++) {
            double yPos = i * height;
            this.yGrids.add(yPos);

            Line line = new Line();
            line.setStartX(0);
            line.setStartY(yPos);
            line.setEndX(DashBoardPane.this.getWidth());
            line.setEndY(yPos);
            line.getStrokeDashArray().addAll(strokeDashArray);
            line.setOpacity(opacity);
            this.visibleGrid.add(line);

        }
        showGrid(this.analysis.showGridProperty.getValue());
    }

    public void removeNode(Widget widget) {
        this.widgetList.remove(widget);
    }

    public synchronized void addNode(Widget widget) {
        if (!this.widgetList.contains(widget)) {
            logger.debug("Add widget to pane: {}", widget);
            this.widgetList.add(widget);
            widget.setDashBoard(this);
        }
    }


    public double getNextGridX(double xPos) {
        if (!this.analysis.snapToGridProperty.getValue()) {
            return xPos;
        }
        double c = this.xGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - xPos)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
//        System.out.println("Next xPos: " + c);
        return c;
    }

    public double getNextGridY(double yPos) {
        if (!this.analysis.snapToGridProperty.getValue()) {
            return yPos;
        }
        double c = this.yGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - yPos)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
//        System.out.println("Next yPos: " + c);
        return c;
    }

    public DashBordModel getDashBordAnalysis() {
        return this.analysis;
    }

    private void printChildren() {
        System.out.println("---");
        for (Widget node : this.widgetList) {
            String id = "";

            if (node instanceof Widget) {
                id = ((Widget) node).getId();
            }

            System.out.println("Child: " + id + "  " + node.getClass() + " " + node);
        }
    }

    public void updateChildren() {
        if (!this.isUpdating.get()) {
            this.isUpdating.set(true);
            Platform.runLater(() -> {
                try {
                    logger.debug("update Dashboard");

                    getChildren().setAll(this.widgetList);
                    createGrid(this.analysis.xGridInterval.get(), this.analysis.yGridInterval.get());

                    this.isUpdating.set(false);
                } catch (Exception ex) {
                    logger.error("Thread problem: {}", ex);
                    this.isUpdating.set(false);
                }
            });

        }


    }

}
