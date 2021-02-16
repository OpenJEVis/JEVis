package org.jevis.jeconfig.plugin.dashboard;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.HiddenConfig;
import org.jevis.jeconfig.plugin.dashboard.common.DashboardExport;
import org.jevis.jeconfig.plugin.dashboard.config.BackgroundMode;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrames;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardControl {

    private static final Logger logger = LogManager.getLogger(DashboardControl.class);
    private double zoomFactor = 1.0d;
    private final double defaultZoom = 1.0d;
    private final DashBordPlugIn dashBordPlugIn;
    private final ConfigManager configManager;
    private final JEVisDataSource jevisDataSource;
    private TimerTask updateTask;
    private final ObservableList<Task> runningUpdateTaskList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private final ObservableList<Widget> widgetList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private Timer updateTimer = new Timer(true);
    private DashboardPojo activeDashboard;
    private ExecutorService executor;
    private boolean isUpdateRunning = false;
    private java.io.File newBackgroundFile;
    private final Image widgetTaskIcon = JEConfig.getImage("if_dashboard_46791.png");

    private Interval activeInterval = new Interval(new DateTime(), new DateTime());
    private final ObjectProperty<Interval> activeIntervalProperty = new SimpleObjectProperty<>(activeInterval);
    private TimeFrameFactory activeTimeFrame;
    private TimeFrames timeFrames;
    private List<JEVisObject> dashboardObjects = new ArrayList<>();
    public BooleanProperty highlightProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showGridProperty = new SimpleBooleanProperty(false);
    public BooleanProperty editableProperty = new SimpleBooleanProperty(false);
    public BooleanProperty snapToGridProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showWidgetHelpProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showHelpProperty = new SimpleBooleanProperty(false);
    private DashBoardPane dashboardPane = new DashBoardPane();
    private DashBoardToolbar toolBar;
    private String firstLoadedConfigHash = null;
    private final WidgetNavigator widgetNavigator;
    private final boolean fitToParent = false;
    public static double MAX_ZOOM = 3;
    public static double MIN_ZOOM = -0.2;
    public static double zoomSteps = 0.05d;
    public static double fitToScreen = 99;
    public static double fitToWidth = 98;
    public static double fitToHeight = 97;
    public static double YGridSize = 25;
    public static double XGridSize = 25;
    private Image backgroundImage;
    private Timer timer;

    /**
     * we want to keep some changes when switching dashboard, this are the workaround variables
     **/
    private boolean firstDashboard = true;
    private final TimeFrameFactory previusActiveTimeFrame = null;
    private final Interval previusActiveInterval = null;


    public DashboardControl(DashBordPlugIn plugin) {
        this.configManager = new ConfigManager(plugin.getDataSource());
        this.dashBordPlugIn = plugin;
        this.jevisDataSource = plugin.getDataSource();

        //TaskWindow taskWindow = new TaskWindow(runningUpdateTaskList);

        widgetNavigator = new WidgetNavigator(this);
        initTimeFrameFactory();
        this.activeDashboard = this.configManager.createEmptyDashboard();
    }

    /**
     * public ExecutorService getExecutor() {
     * return executor;
     * }
     **/

    public void executeTask(Task task) {
        JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), task, widgetTaskIcon, true);
    }

    public void updateWidget(Widget widget) {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                widget.updateConfig(widget.getConfig());
                widget.updateData(activeInterval);
                super.done();
                return null;
            }
        };
        JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), task, widgetTaskIcon, true);
    }

    private void initTimeFrameFactory() {
        this.timeFrames = new TimeFrames(this.jevisDataSource);
    }

    public TimeFrames getAllTimeFrames() {
        return this.timeFrames;
    }

    public void enableHighlightGlow(boolean disable) {
        highlightProperty.setValue(disable);
        this.widgetList.forEach(widget -> {
            Platform.runLater(() -> {
                try {
                    widget.setGlow(false, highlightProperty.get());
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });

        });
    }


    private void resetDashboard() {
        /** clear old states **/
        //rundataUpdateTasks(false);
        stopAllUpdates();
        this.widgetList.clear();


        /** init default states **/
        this.activeDashboard = this.configManager.createEmptyDashboard();
        setZoomFactor(1);
    }


    public void setDashboardPane(DashBoardPane dashboardPane) {
        this.dashboardPane = dashboardPane;
        ChangeListener<Number> sizeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Size rootSize = dashBordPlugIn.getPluginSize();
                setRootSizeChanged(rootSize.getWidth(), rootSize.getHeight());
                //setRootSizeChanged(dashBordPlugIn.getScrollPane().getWidth(), dashBordPlugIn.getScrollPane().getHeight());
            }
        };
        dashboardPane.widthProperty().addListener(sizeListener);
        dashboardPane.heightProperty().addListener(sizeListener);


    }

    public DashBoardPane getDashboardPane() {
        return this.dashboardPane;
    }

    public void showGrid(boolean showGrid) {
        if (showGrid != showGridProperty.get()) {
            showGridProperty.setValue(showGrid);
            dashboardPane.showGrid(showGridProperty.getValue());
            toolBar.updateView(activeDashboard);
//            dashboardPane.updateView();
        }
    }

    public void setSnapToGrid(boolean snapToGrid) {
        logger.error("setSnapToGrid: " + snapToGrid);
        snapToGridProperty.setValue(snapToGrid);
        toolBar.updateView(activeDashboard);
        /**
         if (snapToGrid != showGridProperty.getValue()) {
         this.showGridProperty.setValue(snapToGrid);
         toolBar.updateView(activeDashboard);
         //            dashboardPane.updateView();
         }
         **/
    }


    public int getNextFreeUUID() {
        final Comparator<Widget> comp = (p1, p2) -> Integer.compare(p1.getConfig().getUuid(), p2.getConfig().getUuid());

        try {
            Widget maxIDWidget = getWidgetList().stream().max(comp).get();
            return maxIDWidget.getConfig().getUuid() + 1;
        } catch (Exception ex) {
            return 1;
        }
    }

    public Widget createNewWidget(WidgetPojo widgetPojo) {
        widgetPojo.setUuid(getNextFreeUUID());
//        System.out.println("createNewWidget: "+widgetPojo.getUuid());
        return configManager.createWidget(this, widgetPojo);
    }

    public void loadFirstDashboard() {
        try {
            loadDashboardObjects();

            JEVisObject userDashboad = getUserSelectedDashboard();

            if (this.dashboardObjects.isEmpty()) {
                selectDashboard(null);
            } else if (userDashboad != null) {
                selectDashboard(userDashboad);
            } else if (!this.dashboardObjects.isEmpty()) {
                selectDashboard(this.dashboardObjects.get(0));
            }

            firstDashboard = false;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }

    public DashboardPojo getActiveDashboard() {
        return this.activeDashboard;
    }


    public void setRootSizeChanged(double width, double height) {
        //System.out.println("-- setRootSizeChanged: w"+width+"  h:"+height);
        setZoomFactor(zoomFactor);
    }

    public void highlightWidgetInView(Widget widget, boolean highlight) {
        if (this.highlightProperty.getValue()) {
            widget.setGlow(highlight, highlightProperty.get());
        }
    }

    public void setDashboardSize(double width, double height) {
        this.activeDashboard.setSize(new Size(height, width));
        this.dashboardPane.loadSetting(activeDashboard);

        this.dashboardPane.showGrid(this.showGridProperty.getValue());
    }

    public void zoomIn() {
        setZoomFactor(zoomFactor + zoomSteps);
    }

    public void zoomOut() {
        setZoomFactor(zoomFactor - zoomSteps);
    }


    public void setZoomFactor(double zoom) {
        /** check if the zoomMode is fine **/
        if (zoom != fitToScreen && zoom != fitToHeight && zoom != fitToWidth) {
            zoom = Precision.round(zoom, 2);
            if (this.zoomFactor < MIN_ZOOM) {
                zoom = MIN_ZOOM;
            } else if (zoom > 90) { /** fix zoomFactor(to-screen etc) beginn at 90 **/
                zoom = MAX_ZOOM;
            } else if (zoom > MAX_ZOOM) {
                zoom = MAX_ZOOM;
            }
        }

        this.zoomFactor = zoom;
        Size parentSize = dashBordPlugIn.getPluginSize();
        double relWidthDiff = parentSize.getWidth() / dashboardPane.getWidth();
        double relHeightDiff = parentSize.getHeight() / dashboardPane.getHeight();

        logger.debug("SetZoom: Factor:{}\nparent: {}/{}\ndashboard: {}/{}\nrel: {}/{}", zoomFactor, parentSize.getWidth(), zoomFactor, parentSize.getHeight(), dashboardPane.getWidth(), dashboardPane.getHeight(), relWidthDiff, relHeightDiff);
        logger.debug("Dashboard in bounds: {}/{}", dashboardPane.getBoundsInParent().getWidth(), dashboardPane.getBoundsInParent().getHeight());

        //        if(dashboardPane.getHeight()<dashboardPane.getBoundsInParent().getHeight() || dashboardPane.getWidth()<dashboardPane.getBoundsInParent().getWidth()){
//            Size size= new Size( dashboardPane.getBoundsInParent().getHeight(),dashboardPane.getBoundsInParent().getWidth());
//            dashboardPane.setSize(size);
//        }

        if (zoomFactor == fitToScreen) {
            dashboardPane.setScale(relWidthDiff, relHeightDiff);
        } else if (zoomFactor == fitToHeight) {
            dashboardPane.setZoom(relHeightDiff);
        } else if (zoomFactor == fitToWidth) {
            dashboardPane.setZoom(relWidthDiff);
        } else { /** manual Zoom **/
            dashboardPane.setZoom(zoomFactor);
        }
        toolBar.updateZoomLevelView(zoomFactor);
        logger.debug("Fine Size: dashboard: {}/{}", dashboardPane.getWidth(), dashboardPane.getHeight());

    }

    public double getZoomFactory() {
        return zoomFactor;
    }

    public JEVisDataSource getDataSource() {
        return this.jevisDataSource;
    }

    private void loadDashboardObjects() {
        try {
            JEVisClass scadaAnalysis = this.getDataSource().getJEVisClass(DashBordPlugIn.CLASS_ANALYSIS);
            this.dashboardObjects = this.getDataSource().getObjects(scadaAnalysis, false);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public ObservableList<JEVisObject> getAllDashboards() {
        ObservableList<JEVisObject> observableList = FXCollections.observableList(this.dashboardObjects);
        DashboardSorter.sortDashboards(this.jevisDataSource, observableList);
        return observableList;
    }

    public void restView() {
        this.dashBordPlugIn.getDashBoardPane().clearView();
        this.widgetList.clear();
    }


    public void createNewDashboard() {
        backgroundImage = null;
        newBackgroundFile = null;
        setSnapToGrid(true);
        showGrid(true);
        setEditable(true);

        selectDashboard(null);
        openWidgetNavigator();
    }

    /**
     * Load an dashboard and view it.
     *
     * @param object
     */
    public void selectDashboard(JEVisObject object) {
        logger.error("selectDashboard: {}", object);
        try {
            /** check if the last dashboard was saved and if not ask user **/
            if (firstLoadedConfigHash != null) {
                String newConfigHash = configManager.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this.configManager.toJson(activeDashboard, this.widgetList));
                if (!firstLoadedConfigHash.equals(newConfigHash)) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setContentText(I18n.getInstance().getString("plugin.dashboard.dialog.changed.text"));
                    alert.setResizable(true);
                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            save();
                        } else {

                        }
                    });
                }
            }
            this.firstLoadedConfigHash = null;
            this.editableProperty.setValue(false);
            this.snapToGridProperty.setValue(true);
            this.backgroundImage = null;
            this.newBackgroundFile = null;

            resetDashboard();
            restartExecutor();
            restView();


            if (object == null) {  /** Create new Dashboard**/
                this.activeDashboard = new DashboardPojo();
                this.activeDashboard.setTitle("New Dashboard");
                this.activeDashboard.setTimeFrame(timeFrames.day());
                Size pluginSize = dashBordPlugIn.getPluginSize();
                pluginSize.setHeight(pluginSize.getHeight() - 10);
                pluginSize.setWidth(pluginSize.getWidth() - 10);
                this.activeDashboard.setSize(pluginSize);
            } else { /** load existing Dashboard**/
                try {
                    this.activeDashboard = this.configManager.loadDashboard(this.configManager.readDashboardFile(object));
                } catch (Exception ex) {
                    dashBordPlugIn.showMessage(I18n.getInstance().getString("plugin.dashboard.load.error.file.content"));
                }
                //this.activeDashboard.setName(object.getName());
                this.activeDashboard.setTitle(object.getName());
            }

            this.activeDashboard.setJevisObject(object);

            this.dashBordPlugIn.getDashBoardPane().updateView();
            this.widgetList.addAll(this.configManager.createWidgets(this, this.activeDashboard.getWidgetList()));
            //this.dashBordPlugIn.setContentSize(this.activeDashboard.getSize().getWidth(), this.activeDashboard.getSize().getHeight());

            updateBackground();

            /** async loading of the background image **/
            if (this.activeDashboard.getDashboardObject() != null) {
                this.configManager.getBackgroundImage(this.activeDashboard.getDashboardObject()).addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        backgroundImage = newValue;
                        updateBackground();
                    }
                });
            }

            /** add widgets to dashboard**/
            this.widgetList.forEach(widget -> {
                this.dashBordPlugIn.getDashBoardPane().addWidget(widget);
            });


            /** inti configuration of widgets **/
            this.widgetList.forEach(widget -> {
                try {
                    widget.updateConfig();
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });

//            if (activeTimeFrame == null) {
            this.activeTimeFrame = activeDashboard.getTimeFrame();
//            }

//            if (activeInterval != null) {
//                setInterval(activeInterval);
//            } else {
            setInterval(this.activeTimeFrame.getInterval(getStartDateByData()));
//            }

            setActiveTimeFrame(activeTimeFrame);

            firstLoadedConfigHash = configManager.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this.configManager.toJson(activeDashboard, this.widgetList));

            Platform.runLater(() -> {
                setZoomFactor(activeDashboard.getZoomFactor());
                toolBar.updateView(activeDashboard);
            });
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Calculate an fitting DateTime based on available Samples.
     * For now we use get maximum of all samples.
     *
     * @return
     */
    private DateTime getStartDateByData() {
        DateTime date = new DateTime().minus(Period.years(1));
        for (Widget widget : this.widgetList) {
            try {
                for (DateTime dateTime : widget.getMaxTimeStamps()) {
                    if (dateTime.isAfter(date)) {
                        date = dateTime;
                    }
                }
            } catch (Exception ex) {
                logger.error("Widget '{}' getStartDate error: {}", widget.getId(), ex);
            }
        }
        logger.debug("calculated max TS: {}", date);
        return date;
    }

    public void openWidgetNavigator() {

        widgetNavigator.show();
    }

    public void setDefaultZoom(double zoomFactor) {
        activeDashboard.setZoomFactor(zoomFactor);
    }

    public void setEditable(boolean editable) {
        this.editableProperty.setValue(editable);
//        setSnapToGrid(true);
        showGrid(editable);
        this.widgetList.forEach(widget -> {
            Platform.runLater(() -> {
                try {
                    widget.setEditable(editable);
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });

        });
    }

    public void reload() {
        loadDashboardObjects();
        this.jevisDataSource.reloadAttribute(this.activeDashboard.getDashboardObject());
        initTimeFrameFactory();
        selectDashboard(this.activeDashboard.getDashboardObject());
    }

    public void setActiveTimeFrame(TimeFrameFactory activeTimeFrame) {
        logger.error("SetTimeFrameFactory to: {}", activeTimeFrame.getID());
        this.activeTimeFrame = activeTimeFrame;
        this.setInterval(activeTimeFrame.getInterval(activeInterval.getStart()));
        this.toolBar.updateView(activeDashboard);
    }

    public void registerToolBar(DashBoardToolbar toolbar) {
        this.toolBar = toolbar;
    }

    public TimeFrameFactory getActiveTimeFrame() {
        return this.activeTimeFrame;
    }

    public void setPrevInteval() {
        try {
            Interval nextInterval = this.activeTimeFrame.previousPeriod(this.activeInterval, 1);
            if (nextInterval.getStart().isBeforeNow()) {
                setInterval(nextInterval);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void setNextInterval() {
        try {
            Interval nextInterval = this.activeTimeFrame.nextPeriod(this.activeInterval, 1);
            if (nextInterval.getStart().isBeforeNow()) {
                setInterval(nextInterval);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void restartExecutor() {
        try {
            runningUpdateTaskList.clear();
            if (this.executor != null) {
                this.executor.shutdownNow();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        this.executor = Executors.newFixedThreadPool(HiddenConfig.DASH_THREADS);

    }

    public void setInterval(Interval interval) {
        try {
            logger.error("SetInterval to: {}", interval);
            this.activeInterval = interval;
            activeIntervalProperty.setValue(activeInterval);//workaround
            runDataUpdateTasks(false);

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public ObjectProperty<Interval> getActiveIntervalProperty() {
        return activeIntervalProperty;
    }

    public void requestViewUpdate(Widget widget) {
        logger.debug("requestViewUpdate: {}", widget.getConfig().getTitle());
//        widget.updateData(getInterval());
        try {
            widget.updateConfig(widget.getConfig());
        } catch (Exception ex) {
            logger.error(ex);
        }
//        widget.updateConfig();
    }

    public Interval getInterval() {
        return this.activeInterval;
    }

    public void switchUpdating() {
        this.isUpdateRunning = !this.isUpdateRunning;
        if (this.isUpdateRunning) {
            runDataUpdateTasks(isUpdateRunning);
        } else {
            stopAllUpdates();
        }
//        rundataUpdateTasks(!this.isUpdateRunning);
    }

    private void removeNode(Widget widget) {
        this.widgetList.remove(widget);
    }


    private void stopAllUpdates() {
        try {
            this.isUpdateRunning = false;

            JEConfig.getStatusBar().stopTasks(DashBordPlugIn.class.getName());
            /**
             if (this.updateTask != null) {
             try {
             this.updateTask.cancel();
             } catch (Exception ex) {

             }
             }
             this.updateTimer.cancel();
             if (this.executor != null) this.executor.shutdownNow();

             */
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void runDataUpdateTasks(boolean reStartUpdateDaemon) {
        this.isUpdateRunning = reStartUpdateDaemon;

        stopAllUpdates();

        this.updateTimer = new Timer(true);
        //this.executor = Executors.newFixedThreadPool(HiddenConfig.DASH_THREADS);
//        this.totalUpdateJobs.setValue(0);
//        this.finishUpdateJobs.setValue(0);

        this.runningUpdateTaskList.clear();

        for (Widget widget : this.widgetList) {
            if (!widget.isStatic()) {
                Platform.runLater(() -> {
                    try {
                        widget.showProgressIndicator(true);
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                });
            }
        }


        logger.debug("Update Interval: {}", activeInterval);
        this.updateTask = new TimerTask() {
            @Override
            public void run() {
                logger.debug("Starting Update");
                JEConfig.getStatusBar().startProgressJob("Dashboard"
                        , DashboardControl.this.widgetList.stream().filter(wiget -> !wiget.isStatic()).count()
                        , I18n.getInstance().getString("plugin.dashboard.message.startupdate"));
                try {
//                    totalUpdateJobs.setValue(DashboardControl.this.widgetList.stream().filter(wiget -> !wiget.isStatic()).count());
                    for (Widget widget : DashboardControl.this.widgetList) {
                        if (!widget.isStatic()) {
                            //addWidgetUpdateTask(widget, activeInterval);
                            Task<Object> updateTask = addWidgetUpdateTask(widget, activeInterval);
                            //runningUpdateTaskList.add(updateTask);
                            //JEConfig.getStatusBar().addTask(updateTask,widgetTaskIcon);
                            //executor.submit(updateTask);
                            JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), updateTask, widgetTaskIcon, true);
                        }
                    }

                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        };


        if (reStartUpdateDaemon) {
            this.dashBordPlugIn.getDashBoardToolbar().setUpdateRunning(reStartUpdateDaemon);
            logger.info("Start updateData scheduler: {} sec", this.activeDashboard.getUpdateRate());
            this.updateTimer.scheduleAtFixedRate(this.updateTask, 1000, this.activeDashboard.getUpdateRate() * 1000);
        } else {
            this.updateTimer.schedule(this.updateTask, 1000);
        }

    }


    private Task<Object> addWidgetUpdateTask(Widget widget, Interval interval) {
        /**
         if (widget == null || interval == null) {
         logger.error("widget is null, this should not happen");
         return;
         }
         **/


        Task<Object> updateTask = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                try {
                    logger.debug("addWidgetUpdateTask: " + widget);
                    Platform.runLater(() -> this.updateTitle("Updating Widget [" + widget.typeID() + "" + widget.getConfig().getUuid() + "] " + widget.getConfig().getTitle() + "'"));

                    if (!widget.isStatic()) {
                        widget.updateData(interval);
                        logger.debug("updateData done: " + widget);
//                        finishUpdateJobs.setValue(finishUpdateJobs.getValue() + 1);
                    }

                    this.succeeded();
                    logger.debug("task done: " + widget);
                } catch (Exception ex) {
                    this.failed();
                    logger.error("Widget update error: [{}]", widget.getConfig().getUuid(), ex);
                    ex.printStackTrace();
                } finally {
                    this.done();
                    JEConfig.getStatusBar().progressProgressJob("Dashboard", 1
                            , I18n.getInstance().getString("plugin.dashboard.message.finishedwidget") + " " + widget.getConfig().getUuid());
                }
                return null;
            }
        };
        return updateTask;
    }

    private synchronized boolean allJobsDone(List<Task> futures) {
        boolean allDone = true;
        Iterator<Task> itr = futures.iterator();
        while (itr.hasNext()) {
            if (!itr.next().isDone()) {
                allDone = false;
            }
        }

        return allDone;

    }

    public ObservableList<Widget> getWidgetList() {
        return this.widgetList;
    }


    public void addWidget(Widget widget) {
        try {
            widget.init();
            widget.updateConfig(widget.getConfig());
            this.widgetList.add(widget);
            this.dashboardPane.addWidget(widget);
            widget.updateData(this.activeInterval);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public synchronized void removeAllWidgets(Collection<Widget> elements) {
        elements.forEach(widget -> {
            widget.setVisible(false);
        });
        this.widgetList.removeAll(elements);
        this.dashboardPane.removeAllWidgets(elements);
    }

    public synchronized void removeWidget(Widget widget) {
        try {
            widget.setVisible(false);
            this.widgetList.remove(widget);
            this.dashboardPane.removeWidget(widget);
        } catch (Exception ex) {
            logger.error(ex);
        }

    }

    public List<Widget> getWidgets() {
        return this.widgetList;
    }

    public void save() {
        try {
            this.configManager.openSaveUnder(this.activeDashboard, this.widgetList, this.newBackgroundFile);
            loadDashboardObjects();
            this.toolBar.updateDashboardList(getAllDashboards(), this.activeDashboard);
            firstLoadedConfigHash = configManager.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this.configManager.toJson(activeDashboard, this.widgetList));
        } catch (Exception ex) {
            logger.error(ex);
        }

    }

    /**
     * Ask and set the background image.
     * <p>
     * TODO: allow revert if the does not save
     */
    public void startWallpaperSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Pictures", "*.png", "*.gif", "*.jpg", "*.bmp"));
        File newBackground = fileChooser.showOpenDialog(JEConfig.getStage());
        if (newBackground != null) {
            try {
                BufferedImage bufferedImage = ImageIO.read(newBackground);
                javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
//                JEVisFile jeVisFile = new JEVisFileImp(newBackground.getName(), newBackground);
//                JEVisAttribute attdebug =  getActiveDashboard().getDashboardObject().getAttribute("Background");
//                JEVisSample jeVisSample = getActiveDashboard().getDashboardObject().getAttribute("Background").buildSample(DateTime.now(), jeVisFile);
//                jeVisSample.commit();
                this.newBackgroundFile = newBackground;
                this.backgroundImage = fxImage;
                updateBackground();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void updateBackground() {
        List<BackgroundFill> fillList = new ArrayList<>();
        List<BackgroundImage> bgImageList = new ArrayList<>();
        BackgroundFill bgFill = new BackgroundFill(activeDashboard.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY);

        if (backgroundImage != null) {
            BackgroundRepeat backgroundRepeat = BackgroundRepeat.NO_REPEAT;
            BackgroundSize backgroundSize = new BackgroundSize(backgroundImage.getWidth(), backgroundImage.getHeight(), false, false, false, false);

            switch (activeDashboard.getBackgroundMode()) {
                case BackgroundMode.defaultMode:
                    backgroundRepeat = BackgroundRepeat.NO_REPEAT;
                    break;
                case BackgroundMode.stretch:
                    backgroundSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true);
                    break;
                case BackgroundMode.repeat:
                    backgroundRepeat = BackgroundRepeat.REPEAT;
                    break;
            }

            BackgroundImage bgImage = new BackgroundImage(backgroundImage, backgroundRepeat,
                    backgroundRepeat, BackgroundPosition.DEFAULT, backgroundSize);
            bgImageList.add(bgImage);
        }

        fillList.add(bgFill);

        setBackground(new Background(fillList, bgImageList));

    }

    public void setBackground(Background background) {
        Platform.runLater(() -> {
            try {
                this.dashBordPlugIn.getDashBoardPane().setBackground(background);
            } catch (Exception ex) {
                logger.error(ex);
            }
        });
    }

    private JEVisObject getUserSelectedDashboard() {
        JEVisObject currentUserObject = null;
        try {
            currentUserObject = this.jevisDataSource.getCurrentUser().getUserObject();
            JEVisAttribute userSelectedDashboard = currentUserObject.getAttribute("Start dashboard");
            if (userSelectedDashboard != null) {
                TargetHelper th = new TargetHelper(this.jevisDataSource, userSelectedDashboard);
                if (th.getObject() != null && !th.getObject().isEmpty()) {
                    return th.getObject().get(0);
                }
            }
        } catch (Exception e) {
            logger.error("Could not get Start dashboard from user.");
        }

        return null;
    }

    private String intervalToString() {
        return getInterval().getStart().toString("yyyyMMdd") + "_" + getInterval().getEnd().toString("yyyyMMdd");
    }

    public void toPNG() {
        DashboardExport exporter = new DashboardExport();
        exporter.toPDF(dashboardPane, activeDashboard.getTitle() + "_" + intervalToString());
    }


    public void toPDF() {
        DashboardExport exporter = new DashboardExport();
        exporter.toPDF(dashboardPane, activeDashboard.getTitle() + "_" + intervalToString());
    }

    public void toggleTooltip() {
        this.showHelpProperty.set(!this.showHelpProperty.get());
        toolBar.showTooltips(showHelpProperty.get());
    }

    public void hideAllToolTips() {
        toolBar.hideToolTips();

        if (showWidgetHelpProperty.get()) {
            showWidgetTooltips(false);
        }

        if (showHelpProperty.get()) {
            toggleTooltip();
        }
        toolBar.updateView(activeDashboard);
    }

    public void toggleWidgetTooltips() {
        showWidgetTooltips(!showWidgetHelpProperty.get());
    }

    public void showWidgetTooltips(boolean show) {
        for (Widget widget : getWidgets()) {
            try {
                if (!widget.getTt().getText().equals("")) {
                    if (widget.getTt().isShowing() != show) {
                        if (widget.getTt().isShowing()) Platform.runLater(() -> widget.getTt().hide());
                        else {
                            Bounds sceneBounds = widget.localToScene(widget.getBoundsInLocal());

                            double x = sceneBounds.getMinX() + 2;
                            double y = sceneBounds.getMinY() + 4;

                            Platform.runLater(() -> widget.getTt().show(widget, x, y));
                        }
                    }


                }
            } catch (Exception ex) {
                logger.error("Error while showing tooltip for: {}", widget);
            }
        }
        showWidgetHelpProperty.set(show);

    }


}
