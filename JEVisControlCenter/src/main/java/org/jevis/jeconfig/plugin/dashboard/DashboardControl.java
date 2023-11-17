package org.jevis.jeconfig.plugin.dashboard;

import com.google.common.collect.Iterables;
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
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.plugin.dashboard.common.DashboardExport;
import org.jevis.jeconfig.plugin.dashboard.config.BackgroundMode;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.jevis.jeconfig.tool.ScreenSize;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

public class DashboardControl {

    private static final Logger logger = LogManager.getLogger(DashboardControl.class);
    private double zoomFactor = 1.0d;
    private final double defaultZoom = 1.0d;
    private final DashBordPlugIn dashBordPlugIn;
    private final ConfigManager configManager;
    private final JEVisDataSource jevisDataSource;
    private final ObservableList<Widget> widgetList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private final Timer updateTimer = new Timer(true);
    private DashboardPojo activeDashboard;
    private boolean isUpdateRunning = false;
    private java.io.File newBackgroundFile;
    private final Image widgetTaskIcon = JEConfig.getImage("if_dashboard_46791.png");
    private SideConfigPanel sideConfigPanel;
    private Interval activeInterval = new Interval(new DateTime(), new DateTime());
    private final ObjectProperty<Interval> activeIntervalProperty = new SimpleObjectProperty<>(activeInterval);
    private final TimeFrame previousActiveTimeFrame = null;
    private TimeFrame activeTimeFrame;
    private List<JEVisObject> dashboardObjects = new ArrayList<>();
    private List<Widget> selectedWidgets = new ArrayList<>();
    public BooleanProperty highlightProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showGridProperty = new SimpleBooleanProperty(false);
    public BooleanProperty editableProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showSideEditorProperty = new SimpleBooleanProperty(true);
    public BooleanProperty snapToGridProperty = new SimpleBooleanProperty(false);
    private final Interval previousActiveInterval = null;
    public BooleanProperty showWidgetHelpProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showHelpProperty = new SimpleBooleanProperty(false);
    public ObjectProperty<Side> configSideProperty = new SimpleObjectProperty<>(Side.RIGHT);
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
    public BooleanProperty customWorkdayProperty = new SimpleBooleanProperty(true);
    private TimeFrameFactory timeFrameFactory;
    /**
     * we want to keep some changes when switching dashboard, these are the workaround variables
     **/
    private boolean firstDashboard = true;
    private TimerTask updateTask;
    private WorkDays wd;


    public DashboardControl(DashBordPlugIn plugin) {
        this.configManager = new ConfigManager(plugin.getDataSource());
        this.dashBordPlugIn = plugin;
        this.jevisDataSource = plugin.getDataSource();

        //TaskWindow taskWindow = new TaskWindow(runningUpdateTaskList);

        widgetNavigator = new WidgetNavigator(this);
        initTimeFrames();
        this.activeDashboard = this.configManager.createEmptyDashboard();
    }

    public void updateWidget(Widget widget) {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                logger.debug("---- Control.updateWidgets");
                widget.updateConfig(widget.getConfig());
                widget.updateConfig();
                widget.updateData(activeInterval);

                super.done();
                return null;
            }
        };
        JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), task, widgetTaskIcon, true);
    }

    private void initTimeFrames() {
        this.timeFrameFactory = new TimeFrameFactory(this.jevisDataSource);
    }

    public TimeFrameFactory getAllTimeFrames() {
        return this.timeFrameFactory;
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
        this.activeInterval = new Interval(new DateTime(), new DateTime());
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

    public void setCustomWorkday(boolean customWorkday) {
        logger.error("setCustomWorkday: " + customWorkday);
        customWorkdayProperty.setValue(customWorkday);
        toolBar.updateView(activeDashboard);

        getWidgets().forEach(widget -> {
            if (widget instanceof DataModelWidget) {
                DataModelWidget dataModelWidget = (DataModelWidget) widget;
                dataModelWidget.setCustomWorkday(customWorkdayProperty.get());
            }
            //widget.updateData(activeInterval);
        });
        setInterval(activeInterval);


    }

    public int getNextFreeUUID() {
        final Comparator<Widget> comp = Comparator.comparingInt(p -> p.getConfig().getUuid());

        try {
            Widget maxIDWidget = getWidgetList().stream().max(comp).get();
            return maxIDWidget.getConfig().getUuid() + 1;
        } catch (Exception ex) {
            return 1;
        }
    }

    public Widget createNewWidget(WidgetPojo widgetPojo) {
        System.out.println("Contol.createnewWidgets: " + widgetPojo);

        System.out.println("---- newWidgetS.getSe...");
        try {
            Class<?> clazz = Class.forName("org.jevis.jeconfig.plugin.dashboard.widget.TitleWidget");
            Constructor<?> ctor = clazz.getConstructor(DashboardControl.class);
            Object object = ctor.newInstance(this);
            Widget widget = (Widget) object;
            System.out.println("hmmmmmmmm: " + widget.getControl());
        } catch (Exception exception) {
            exception.printStackTrace();
        }


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

        /*
        if the zoom level is to one of the dynamic sizes start at 100%.
        There might be a better ways to calculate a better factor
         */
        if (zoomFactor >= fitToHeight) {
            zoomFactor = 1;
        }

        setZoomFactor(zoomFactor + zoomSteps);
    }

    public void zoomOut() {
         /*
        if the zoom level is to one of the dynamic sizes start at 100%.
        There might be a better ways to calculate a better factor
         */
        if (zoomFactor >= fitToHeight) {
            zoomFactor = 1;
        }
        setZoomFactor(zoomFactor - zoomSteps);
    }


    public void setZoomFactor(double zoom) {
        /** check if the zoomMode is fine **/
        if (zoom != fitToScreen && zoom != fitToHeight && zoom != fitToWidth) {
            zoom = Precision.round(zoom, 2);
            if (this.zoomFactor < MIN_ZOOM) {
                zoom = MIN_ZOOM;
            } else if (zoom > 90) { /** fix zoomFactor(to-screen etc) begin at 90 **/
                zoom = MAX_ZOOM;
            } else if (zoom > MAX_ZOOM) {
                zoom = MAX_ZOOM;
            }
        }

        this.zoomFactor = zoom;
        Size parentSize = dashBordPlugIn.getPluginSize();
        double relWidthDiff = parentSize.getWidth() / dashboardPane.getWidth();
        double relHeightDiff = parentSize.getHeight() / dashboardPane.getHeight();

        logger.debug("SetZoom: Factor:{}\nparent: {}/{}\ndashboard: {}/{}\nrel: {}/{}", zoomFactor, parentSize.getWidth(), parentSize.getHeight(), dashboardPane.getWidth(), dashboardPane.getHeight(), relWidthDiff, relHeightDiff);
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

    public void deleteDashboard() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.dashboard.dia.delete.title"));
        alert.setContentText(I18n.getInstance().getString("plugin.dashboard.dia.delete.content"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.dashboard.dia.delete.header"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                JEVisObject toDeleteObj = this.activeDashboard.getDashboardObject();
                toDeleteObj.delete();
                loadDashboardObjects();
                selectDashboard(this.dashboardObjects.get(0));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public ObservableList<JEVisObject> getAllDashboards() {
        ObservableList<JEVisObject> observableList = FXCollections.observableList(this.dashboardObjects);
        DashboardSorter.sortDashboards(this.jevisDataSource, observableList);
        return observableList;
    }

    public void resetView() {
        this.dashBordPlugIn.getDashBoardPane().clearView();
        this.widgetList.clear();
    }


    public void createNewDashboard() {
        backgroundImage = null;
        newBackgroundFile = null;
        setSnapToGrid(true);
        showGrid(true);

        selectDashboard(null);
        setEditable(true);
        openWidgetNavigator();
    }

    /**
     * Load an dashboard and view it.
     *
     * @param object
     */
    public void selectDashboard(JEVisObject object) {
        logger.debug("selectDashboard: {}", object);
        stopAllUpdates();
        try {
            /* check if the last dashboard was saved and if not ask user */
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
            /* reset controls */
            this.firstLoadedConfigHash = null;
            this.editableProperty.setValue(false);
            this.snapToGridProperty.setValue(true);
            this.backgroundImage = null;
            this.newBackgroundFile = null;
            this.selectedWidgets = new ArrayList<>();
            this.showGridProperty.setValue(false);


            showConfig();//if list is empty=reset
            resetDashboard();
            resetView();

            if (object == null) {  /* Create new Dashboard*/
                this.activeDashboard = new DashboardPojo();
                this.activeDashboard.setTitle("New Dashboard");
                this.activeDashboard.setTimeFrame(timeFrameFactory.day());

                /* Now we use 1920 x 1080 (minus the boarders) as default for all */
                Size pluginSize = new Size(886.0, 1863.0);
                //Size pluginSize = dashBordPlugIn.getPluginSize();

                pluginSize.setHeight(pluginSize.getHeight() - 10);
                pluginSize.setWidth(pluginSize.getWidth() - 10);
                this.activeDashboard.setSize(pluginSize);
            } else { /* load existing Dashboard*/
                try {
                    this.activeDashboard = this.configManager.loadDashboard(this.configManager.readDashboardFile(object));
                    this.wd = new WorkDays(object);
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

            /** add widgets to dashboard **/
            this.widgetList.forEach(widget -> {
                this.dashBordPlugIn.getDashBoardPane().addWidget(widget);
            });


            /** init configuration of widgets **/
            this.widgetList.forEach(widget -> {
                //if (!widget.getId().equals(TimeFrameWidget.WIDGET_ID)) {
                try {
                    widget.updateConfig();
                } catch (Exception ex) {
                    logger.error(ex);
                }
                // }
            });


            this.activeTimeFrame = activeDashboard.getTimeFrame();
            //setInterval(this.activeTimeFrame.getInterval(getStartDateByData()));
            setActiveTimeFrame(activeTimeFrame);

            firstLoadedConfigHash = configManager.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this.configManager.toJson(activeDashboard, this.widgetList));

            Platform.runLater(() -> {
                setZoomFactor(activeDashboard.getZoomFactor());
                toolBar.updateView(activeDashboard);
            });

            sideConfigPanel = new SideConfigPanel(this);

            this.dashBordPlugIn.getWidgetControlPane().setContent(sideConfigPanel);
            this.toolBar.updateView(activeDashboard);


        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Calculate a fitting DateTime based on available Samples.
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

    public void redrawDashboardPane() {
        dashboardPane.redrawWidgets(this.widgetList);
        dashboardPane.showGrid(showGridProperty.getValue());
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
        if (!editable) {
            selectedWidgets.clear();

            updateHighlightSelected();
        }

        showConfig();

        this.toolBar.updateView(activeDashboard);
    }

    public void reload() {
        loadDashboardObjects();
        this.jevisDataSource.reloadAttribute(this.activeDashboard.getDashboardObject());
        selectDashboard(this.activeDashboard.getDashboardObject());
    }

    public TimeFrame getActiveTimeFrame() {
        return this.activeTimeFrame;
    }

    public void registerToolBar(DashBoardToolbar toolbar) {
        this.toolBar = toolbar;
    }

    public void setActiveTimeFrame(TimeFrame activeTimeFrame) {
        logger.debug("SetTimeFrameFactory to: {}", activeTimeFrame.getID());
        this.activeTimeFrame = activeTimeFrame;
        DateTime start = activeInterval.getStart();
        if (wd != null && wd.getWorkdayEnd().isBefore(wd.getWorkdayStart()) && activeInterval.toDuration().getStandardDays() > 1) {
            start = start.plusDays(1);
        }

        this.setInterval(activeTimeFrame.getInterval(start, false));
        this.toolBar.updateView(activeDashboard);
    }

    public void setPrevInterval() {
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

    public void setInterval(Interval interval) {
        try {
            logger.debug("------------------ SetInterval to: {} ------------------", interval);

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
        logger.error("switchUpdating");
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
            logger.debug("stopAllUpdates: " + JEConfig.getStatusBar().getTaskList().size());
            this.isUpdateRunning = false;

            JEConfig.getStatusBar().stopTasks(DashBordPlugIn.class.getName());
            this.updateTask.cancel();
        } catch (NullPointerException nex) {
            logger.debug(nex, nex);
        } catch (Exception ex) {
            logger.error("Error while stoping running task", ex);
        }
    }

    public void runDataUpdateTasks(boolean reStartUpdateDaemon) {
        logger.debug("Restart Update Tasks: daemon: {}", reStartUpdateDaemon);
        this.isUpdateRunning = reStartUpdateDaemon;

        stopAllUpdates();

        for (Widget widget : this.widgetList) {
            if (!widget.isStatic()) {
                Platform.runLater(() -> {
                    try {
                        widget.showProgressIndicator(true);
                    } catch (Exception ex) {
                        logger.error("Show ProgressIndicator for: {}", widget.getConfig().getTitle(), ex);
                    }
                });
            }
        }

        logger.debug("Update Interval: {}", activeInterval);

        updateTask = new TimerTask() {
            @Override
            public void run() {
                logger.error("Starting Updates");
                JEConfig.getStatusBar().startProgressJob("Dashboard"
                        , DashboardControl.this.widgetList.stream().filter(wiget -> !wiget.isStatic()).count()
                        , I18n.getInstance().getString("plugin.dashboard.message.startupdate"));
                try {
                    List<Widget> objects = new ArrayList<>();

                    for (Widget widget : DashboardControl.this.widgetList) {
                        if (!widget.isStatic()) {
                            if (objects.contains(widget)) {
                                logger.warn("    --- warning duplicate widget update: {}-{}", widget.getConfig().getTitle(), widget.getConfig().getType());
                            } else {
                                objects.add(widget);
                                Task<Object> updateTask = addWidgetUpdateTask(widget, activeInterval);
                                JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), updateTask, widgetTaskIcon, true);
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Error while adding widgets", ex);
                }
            }
        };

        if (reStartUpdateDaemon) {
            this.dashBordPlugIn.getDashBoardToolbar().setUpdateRunning(true);
            logger.info("Start updateData scheduler: {} sec", this.activeDashboard.getUpdateRate());
            this.updateTimer.scheduleAtFixedRate(updateTask, 1000, this.activeDashboard.getUpdateRate() * 1000);
        } else {
            this.dashBordPlugIn.getDashBoardToolbar().setUpdateRunning(false);
            this.updateTimer.schedule(updateTask, 0);
        }

    }


    private Task<Object> addWidgetUpdateTask(Widget widget, Interval interval) {
        /**
         if (widget == null || interval == null) {
         logger.error("widget is null, this should not happen");
         return;
         }
         **/

        return new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                try {
                    logger.debug("addWidgetUpdateTask: '{}'  - Interval: {}", widget.getConfig().getTitle(), interval);
                    Platform.runLater(() -> this.updateTitle(I18n.getInstance().getString("plugin.dashboard.message.updatingwidget")
                            + " [" + widget.typeID() + widget.getConfig().getUuid() + "] " + widget.getConfig().getTitle() + "'"));
                    if (!widget.isStatic()) {
                        widget.updateData(interval);
                        logger.debug("updateData done: '{}:{}'", widget.getConfig().getTitle(), widget.getConfig().getUuid());
                    }

                    this.succeeded();
                    logger.debug("task done: {}:{}", widget.getConfig().getTitle(), widget.getConfig().getUuid());
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
            if (widget instanceof DataModelWidget) {
                DataModelWidget dataModelWidget = (DataModelWidget) widget;
                dataModelWidget.setCustomWorkday(customWorkdayProperty.get());
            }
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
        exporter.toPNG(dashboardPane, activeDashboard.getTitle() + "_" + intervalToString());
    }


    public void toPDF() {
        DashboardExport exporter = new DashboardExport();
        exporter.toPDF(this, activeDashboard.getTitle() + "_" + intervalToString());

        ScreenSize.fixScreenshotLayout(JEConfig.getStage());

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

    public void showLoadDialog() {
        LoadDashboardDialog loadDashboardDialog = new LoadDashboardDialog(jevisDataSource, this);
        loadDashboardDialog.show();
        loadDashboardDialog.setOnCloseRequest(event -> {
            if (loadDashboardDialog.getResponse() == Response.NEW) {
                createNewDashboard();
            } else if (loadDashboardDialog.getResponse() == Response.LOAD) {

                selectDashboard(loadDashboardDialog.getSelectedDashboard());
                setActiveTimeFrame(loadDashboardDialog.getTimeFrameFactory());
                setInterval(loadDashboardDialog.getSelectedInterval());

            }
        });
    }

    /**
     * ----------------------------------------------------------------
     * Selection function below.
     * TODO: move this function to an better place
     */

    public List<Widget> getSelectedWidgets() {
        return selectedWidgets;
    }

    public void addToWidgetSelection(List<Widget> widgets) {
        widgets.forEach(widget -> {
            if (selectedWidgets.contains(widget)) {
                selectedWidgets.remove(widget);
            } else {
                selectedWidgets.addAll(widgets);
            }
        });
        updateHighlightSelected();
        showConfig();
    }

    public void setSelectedWidget(Widget widget) {
        List<Widget> selected = new ArrayList<>();
        selected.add(widget);
        setSelectedWidgets(selected);
    }

    public void setSelectAllFromType(Widget widget) {
        List<Widget> selected = new ArrayList<>();
        widgetList.forEach(widget1 -> {
            if (widget.getConfig().getType().equals(widget1.getConfig().getType())) {
                selected.add(widget1);
            }
        });
        setSelectedWidgets(selected);
    }


    public void setSelectedWidgets(List<Widget> widgets) {
        if (this.editableProperty.get()) {
            selectedWidgets.clear();
            selectedWidgets.addAll(widgets);
            updateHighlightSelected();
            /* dashboard need focus so the key events work*/
            dashBordPlugIn.getScrollPane().requestFocus();
            showConfig();
        }
    }


    private void updateHighlightSelected() {
        for (Widget widget : widgetList) {
            widget.setGlow(selectedWidgets.contains(widget), false);
        }
    }


    public void moveSelected(double up, double down, double left, double right) {
        selectedWidgets.forEach(widget -> {
            if (up > 0) {
                widget.getConfig().setyPosition(widget.getConfig().getyPosition() - up);
            } else if (down > 0) {
                widget.getConfig().setyPosition(widget.getConfig().getyPosition() + down);
            } else if (left > 0) {
                widget.getConfig().setxPosition(widget.getConfig().getxPosition() - left);
            } else if (right > 0) {
                widget.getConfig().setxPosition(widget.getConfig().getxPosition() + right);
            }

            requestViewUpdate(widget);
        });
    }

    public void layerSelected(int layer) {
        selectedWidgets.forEach(widget -> {
            //System.out.println("Widget set layer to: " + widget.getConfig().getUuid() + " " + layer);
            widget.getConfig().setLayer(layer);
        });
        redrawDashboardPane();
    }

    public void fgColorSelected(Color color) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setFontColor(color);
            widget.updateConfig();
        });
        redrawDashboardPane();
    }

    public void bgColorSelected(Color color) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setBackgroundColor(color);
            widget.updateConfig();
        });
    }

    public void sizeSelected(double width, double height) {
        selectedWidgets.forEach(widget -> {
            Size size = widget.getConfig().getSize();
            if (width > 0) {
                size.setWidth(width);
            }
            if (height > 0) {
                size.setHeight(height);
            }

            widget.getConfig().setSize(size);
            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    public void positionSelected(double xpos, double ypos) {
        selectedWidgets.forEach(widget -> {
            if (xpos > -1) {
                widget.getConfig().setxPosition(xpos);
            }
            if (ypos > -1) {
                widget.getConfig().setyPosition(ypos);
            }

            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    public void equalizeDataModel() {
        Widget lastWidget = Iterables.getLast(getSelectedWidgets());
        if (lastWidget instanceof DataModelWidget) {
            getSelectedWidgets().forEach(widget -> {
                if (widget instanceof DataModelWidget && !widget.equals(lastWidget)) {
                    //System.out.println("Is DataModelWidget: " + widget.getConfig().getUuid());
                    ((DataModelWidget) widget).setDataHandler(((DataModelWidget) lastWidget).getDataHandler());
                    widget.updateConfig();
                    requestViewUpdate(widget);
                    widget.updateData(activeInterval);
                }
            });
        }
    }

    public void shadowSelected(boolean shadows) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setShowShadow(shadows);
            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    public void showValueSelected(boolean showValue) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setShowValue(showValue);
            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    public void alignSelected(Pos pos) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setTitlePosition(pos);
            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    public void fontSizeSelected(double size) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setFontSize(size);
            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    public void fontWeightSelected(FontWeight fontWeight) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setFontWeight(fontWeight);
            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    public void fontPostureSelected(FontPosture fontPosture) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setFontPosture(fontPosture);
            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    public void fontUnderlinedSelected(Boolean underlined) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setFontUnderlined(underlined);
            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    public void setWidgetTitle(String name) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setTitle(name);
            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    public void decimalsSelected(int size) {
        selectedWidgets.forEach(widget -> {
            widget.getConfig().setDecimals(size);
            widget.updateConfig();
            requestViewUpdate(widget);
        });
    }

    private void showConfig() {

        if (sideConfigPanel != null) {
            if (!selectedWidgets.isEmpty()) {
                sideConfigPanel.setLastSelectedWidget(Iterables.getLast(selectedWidgets));
            } else {
                sideConfigPanel.setLastSelectedWidget(null);
            }
        }


        if (editableProperty.get()) {
            this.dashBordPlugIn.showWidgetControlPane(showSideEditorProperty.get());
        } else {
            this.dashBordPlugIn.showWidgetControlPane(false);
        }


        /**
         if (selectedWidgets.isEmpty()) {
         //dashBordPlugIn.getHiddenSidesPane().setPinnedSide(null);
         this.dashBordPlugIn.showWidgetControlPane(false);
         } else {
         //configPanePos(configSideProperty.get(), sideConfigPanel);
         sideConfigPanel.setLastSelectedWidget(Iterables.getLast(selectedWidgets));
         this.dashBordPlugIn.showWidgetControlPane(true);
         }
         */
    }

    public void configPanePos(Side pos, Node node) {
        configSideProperty.setValue(pos);

        if (pos.equals(Side.LEFT)) {
            dashBordPlugIn.getHiddenSidesPane().setRight(null);
            dashBordPlugIn.getHiddenSidesPane().setLeft(node);
        } else if (pos.equals(Side.RIGHT)) {
            dashBordPlugIn.getHiddenSidesPane().setLeft(null);
            dashBordPlugIn.getHiddenSidesPane().setRight(node);
        }
        dashBordPlugIn.getHiddenSidesPane().setPinnedSide(pos);
    }

    public ObjectProperty<Side> getConfigSideProperty() {
        return configSideProperty;
    }
}
