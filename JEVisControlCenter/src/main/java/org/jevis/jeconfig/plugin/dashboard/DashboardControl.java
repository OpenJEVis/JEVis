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
import org.jevis.jeconfig.plugin.dashboard.slideshow.SlideshowControl;
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
import java.util.*;

/**
 * Central lifecycle coordinator for the Dashboard plugin.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Loading, saving and switching between dashboard configurations via {@link ConfigManager}</li>
 *   <li>Scheduling periodic data updates for all widgets via an internal {@link Timer}</li>
 *   <li>Managing zoom, background image, grid, and edit-mode state</li>
 *   <li>Providing delegated access to widget selection operations through {@link WidgetSelectionController}</li>
 * </ul>
 *
 * <p>All bulk-selection operations ({@code setSelectedWidgets}, {@code moveSelected}, {@code fontSizeSelected}, etc.)
 * are delegated to {@link WidgetSelectionController}. Scheduling operations should eventually be extracted
 * to a dedicated {@code DashboardUpdateScheduler}.
 */
public class DashboardControl {

    private static final Logger logger = LogManager.getLogger(DashboardControl.class);
    public static double MAX_ZOOM = 3;
    public static double MIN_ZOOM = -0.2;
    public static double zoomSteps = 0.05d;
    public static double fitToScreen = 99;
    public static double fitToWidth = 98;
    public static double fitToHeight = 97;
    public static double YGridSize = 25;
    public static double XGridSize = 25;
    private final DashBordPlugIn dashBordPlugIn;
    private final ConfigManager configManager;
    private final JEVisDataSource jevisDataSource;
    private final ObservableList<Widget> widgetList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private final DashboardUpdateScheduler updateScheduler = new DashboardUpdateScheduler(this);
    private final Image widgetTaskIcon = JEConfig.getImage("if_dashboard_46791.png");
    private final WidgetNavigator widgetNavigator;
    public BooleanProperty highlightProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showGridProperty = new SimpleBooleanProperty(false);
    public BooleanProperty editableProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showSideEditorProperty = new SimpleBooleanProperty(true);
    public BooleanProperty snapToGridProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showWidgetHelpProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showHelpProperty = new SimpleBooleanProperty(false);
    public ObjectProperty<Side> configSideProperty = new SimpleObjectProperty<>(Side.RIGHT);
    public BooleanProperty customWorkdayProperty = new SimpleBooleanProperty(true);
    private double zoomFactor = 1.0d;
    private DashboardPojo activeDashboard;
    private java.io.File newBackgroundFile;
    private SideConfigPanel sideConfigPanel;
    private Interval activeInterval = new Interval(new DateTime(), new DateTime());
    private final ObjectProperty<Interval> activeIntervalProperty = new SimpleObjectProperty<>(activeInterval);
    private TimeFrame activeTimeFrame;
    private List<JEVisObject> dashboardObjects = new ArrayList<>();
    private final WidgetSelectionController selectionController = new WidgetSelectionController(this);
    private DashBoardPane dashboardPane = new DashBoardPane();
    private DashBoardToolbar toolBar;
    private String firstLoadedConfigHash = null;
    private Image backgroundImage;
    private TimeFrameFactory timeFrameFactory;
    /**
     * we want to keep some changes when switching dashboard, these are the workaround variables
     **/
    private boolean firstDashboard = true;
    private WorkDays workDays;
    private SlideshowControl slideshowControl = null;


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
        Platform.runLater(() -> this.widgetList.forEach(widget -> {
            try {
                widget.setGlow(false, highlightProperty.get());
            } catch (Exception ex) {
                logger.error("Failed to set glow on widget '{}'", widget.getConfig().getUuid(), ex);
            }
        }));
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

    public DashBoardPane getDashboardPane() {
        return this.dashboardPane;
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

    public void showGrid(boolean showGrid) {
        if (showGrid != showGridProperty.get()) {
            showGridProperty.setValue(showGrid);
            dashboardPane.showGrid(showGridProperty.getValue());
            toolBar.updateView(activeDashboard);
//            dashboardPane.updateView();
        }
    }

    public void setSnapToGrid(boolean snapToGrid) {
        logger.debug("setSnapToGrid: " + snapToGrid);
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
        logger.debug("setCustomWorkday: " + customWorkday);

        customWorkdayProperty.setValue(customWorkday);
        toolBar.updateView(activeDashboard);

        getWidgets().forEach(widget -> {
            if (widget instanceof DataModelWidget) {
                DataModelWidget dataModelWidget = (DataModelWidget) widget;
                dataModelWidget.setCustomWorkday(customWorkdayProperty.get());
            }
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

    /**
     * Creates a new widget instance from a configuration POJO, assigns it the next free UUID,
     * and returns it. The widget is NOT yet added to the widget list; call {@link #addWidget(Widget)} for that.
     *
     * @param widgetPojo the configuration template; its UUID will be set by this method
     * @return the newly created widget, or {@code null} if the type is unknown
     */
    public Widget createNewWidget(WidgetPojo widgetPojo) {
        logger.debug("createNewWidget: {}", widgetPojo);
        widgetPojo.setUuid(getNextFreeUUID());
        return configManager.createWidget(this, widgetPojo);
    }

    public void loadFirstDashboard() {
        try {
            loadDashboardObjects();
            JEVisObject userDashboard = getUserSelectedDashboard();

            if (userDashboard != null && userDashboard.getJEVisClassName().equals("Dashboard Collection")) {
                try {
                    slideshowControl = new SlideshowControl(userDashboard, this);
                    if (slideshowControl.isAutoplay()) {
                        slideshowControl.start();
                        firstDashboard = false;
                    } else {
                        selectDashboard(slideshowControl.getFirstDashboard());
                    }
                } catch (Exception ex) {
                    logger.error("Dashboard Collection could not be loaded", ex);
                }

            } else if (this.dashboardObjects.isEmpty()) {
                selectDashboard(null);
            } else if (userDashboard != null) {
                selectDashboard(userDashboard);
            } else if (!this.dashboardObjects.isEmpty()) {
                selectDashboard(this.dashboardObjects.get(0));
            }

            firstDashboard = false;
        } catch (Exception ex) {
            logger.error("Failed to load first dashboard", ex);
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

    /**
     * Returns the plugin host. Used by {@link WidgetSelectionController} for UI callbacks.
     */
    DashBordPlugIn getDashBordPlugIn() {
        return dashBordPlugIn;
    }

    private void loadDashboardObjects() {
        try {
            JEVisClass dashboards = this.getDataSource().getJEVisClass(DashBordPlugIn.CLASS_ANALYSIS);
            this.dashboardObjects = this.getDataSource().getObjects(dashboards, false);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void deleteDashboard() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.dashboard.dia.delete.title"));
        alert.setContentText(I18n.getInstance().getString("plugin.dashboard.dia.delete.content"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.dashboard.dia.delete.header"));

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    JEVisObject toDeleteObj = this.activeDashboard.getDashboardObject();
                    toDeleteObj.delete();
                    loadDashboardObjects();
                    if (this.dashboardObjects.isEmpty()) {
                        selectDashboard(null);
                    } else {
                        selectDashboard(this.dashboardObjects.get(0));
                    }
                } catch (Exception e) {
                    logger.error("Failed to delete dashboard", e);
                }
            }
        });

    }

    public SlideshowControl getSlideshowControl() {
        return slideshowControl;
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
     * Loads and displays a dashboard from the given JEVis object.
     *
     * <p>If there are unsaved changes on the currently active dashboard the user is prompted to save.
     * Passing {@code null} creates a blank "New Dashboard" with default settings.
     *
     * @param object the JEVis object representing the dashboard to load, or {@code null} to create a new one
     */
    public void selectDashboard(JEVisObject object) {
        logger.debug("selectDashboard: {}", object);
        stopAllUpdates();
        try {
            /* check if the last dashboard was saved and if not ask user */
            if (firstLoadedConfigHash != null) {
                String newConfigHash = computeDashboardHash();
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
            this.selectionController.clearSelection();
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
                    this.workDays = new WorkDays(object);
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
            this.widgetList.forEach(widget -> this.dashBordPlugIn.getDashBoardPane().addWidget(widget));


            /** init configuration of widgets **/
            this.widgetList.forEach(widget -> {
                try {
                    widget.updateConfig();
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });


            this.activeTimeFrame = activeDashboard.getTimeFrame();
            if (this.activeTimeFrame == null) {
                logger.warn("Dashboard '{}' has no timeframe configured, defaulting to day", activeDashboard.getTitle());
                this.activeTimeFrame = timeFrameFactory.day();
            }

            firstLoadedConfigHash = computeDashboardHash();

            setZoomFactor(activeDashboard.getZoomFactor());

            setActiveTimeFrame(activeTimeFrame);

            sideConfigPanel = new SideConfigPanel(this);
            this.dashBordPlugIn.getWidgetControlPane().setContent(sideConfigPanel);

        } catch (Exception ex) {
            logger.error("Failed to select dashboard", ex);
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
        Platform.runLater(() -> this.widgetList.forEach(widget -> {
            try {
                widget.setEditable(editable);
            } catch (Exception ex) {
                logger.error("Failed to set editable on widget '{}'", widget.getConfig().getUuid(), ex);
            }
        }));
        if (!editable) {
            selectionController.clearSelection();
            selectionController.updateHighlightSelected();
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

    public void setActiveTimeFrame(TimeFrame activeTimeFrame) {
        logger.debug("SetTimeFrameFactory to: {}", activeTimeFrame.getID());
        this.activeTimeFrame = activeTimeFrame;
        DateTime start = activeInterval.getStart();
        if (workDays != null && workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart()) && activeInterval.toDuration().getStandardDays() > 1) {
            start = start.plusDays(1);
        }

        Interval interval = activeTimeFrame.getInterval(start, false);
        setInterval(interval);

        this.toolBar.updateView(activeDashboard);
    }

    public void registerToolBar(DashBoardToolbar toolbar) {
        this.toolBar = toolbar;
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

    /**
     * Sets the active time interval and triggers a one-shot data update for all widgets.
     *
     * @param interval the new time interval to display; must not be {@code null}
     */
    public void setInterval(Interval interval) {
        try {
            logger.debug("------------------ SetInterval to: {} ------------------", interval);

            this.activeInterval = interval;
            activeIntervalProperty.setValue(activeInterval);//workaround
            runDataUpdateTasks(true);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void switchUpdating() {
        logger.debug("switchUpdating");

        if (this.updateScheduler.isUpdateRunning()) {
            if (slideshowControl != null) {
                slideshowControl.stop();
            } else {
                this.updateScheduler.stopAllUpdates();
            }
        } else {
            if (slideshowControl != null) {
                slideshowControl.start();
            } else {
                this.updateScheduler.runDataUpdateTasks(false);
            }
        }

        this.dashBordPlugIn.getDashBoardToolbar().setUpdateRunning(updateScheduler.isUpdateRunning());
    }

    public void setUpdateRunning(boolean updateRunning) {
        this.updateScheduler.setUpdateRunning(updateRunning);
    }

    private void removeNode(Widget widget) {
        this.widgetList.remove(widget);
    }

    /**
     * Stops all running update tasks. Delegated to {@link DashboardUpdateScheduler}.
     */
    void stopAllUpdates() {
        this.updateScheduler.stopAllUpdates();
    }

    /**
     * Starts widget data updates. Delegated to {@link DashboardUpdateScheduler}.
     *
     * @param runOnce {@code true} to execute once immediately; {@code false} for repeating schedule
     */
    public void runDataUpdateTasks(boolean runOnce) {
        this.updateScheduler.runDataUpdateTasks(runOnce);
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

    /**
     * Computes a compact JSON snapshot of the current dashboard state used for dirty-detection.
     * Uses compact (non-pretty-print) serialization for performance.
     */
    private String computeDashboardHash() {
        try {
            return this.configManager.toJson(activeDashboard, this.widgetList).toString();
        } catch (Exception ex) {
            logger.error("Failed to compute dashboard hash", ex);
            return "";
        }
    }

    public void save() {
        try {
            this.configManager.openSaveUnder(this.activeDashboard, this.widgetList, this.newBackgroundFile);
            loadDashboardObjects();
            this.toolBar.updateDashboardList(getAllDashboards(), this.activeDashboard);
            firstLoadedConfigHash = computeDashboardHash();
        } catch (Exception ex) {
            logger.error("Failed to save dashboard", ex);
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
                logger.error("Failed to load background image from file", e);
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

    // -------------------------------------------------------------------------
    // Selection — all delegated to WidgetSelectionController
    // -------------------------------------------------------------------------

    public List<Widget> getSelectedWidgets() {
        return selectionController.getSelectedWidgets();
    }

    public void setSelectedWidgets(List<Widget> widgets) {
        selectionController.setSelectedWidgets(widgets);
    }

    public void addToWidgetSelection(List<Widget> widgetsToToggle) {
        selectionController.addToWidgetSelection(widgetsToToggle);
    }

    public void setSelectedWidget(Widget widget) {
        selectionController.setSelectedWidget(widget);
    }

    public void setSelectAllFromType(Widget widget) {
        selectionController.setSelectAllFromType(widget);
    }

    public void moveSelected(double up, double down, double left, double right) {
        selectionController.moveSelected(up, down, left, right);
    }

    public void layerSelected(int layer) {
        selectionController.layerSelected(layer);
    }

    public void fgColorSelected(Color color) {
        selectionController.fgColorSelected(color);
    }

    public void bgColorSelected(Color color) {
        selectionController.bgColorSelected(color);
    }

    public void sizeSelected(double width, double height) {
        selectionController.sizeSelected(width, height);
    }

    public void positionSelected(double xpos, double ypos) {
        selectionController.positionSelected(xpos, ypos);
    }

    public void equalizeDataModel() {
        selectionController.equalizeDataModel();
    }

    public void shadowSelected(boolean shadows) {
        selectionController.shadowSelected(shadows);
    }

    public void showValueSelected(boolean showValue) {
        selectionController.showValueSelected(showValue);
    }

    public void alignSelected(Pos pos) {
        selectionController.alignSelected(pos);
    }

    public void fontSizeSelected(double size) {
        selectionController.fontSizeSelected(size);
    }

    public void fontWeightSelected(FontWeight fontWeight) {
        selectionController.fontWeightSelected(fontWeight);
    }

    public void fontPostureSelected(FontPosture fontPosture) {
        selectionController.fontPostureSelected(fontPosture);
    }

    public void fontUnderlinedSelected(Boolean underlined) {
        selectionController.fontUnderlinedSelected(underlined);
    }

    public void setWidgetTitle(String name) {
        selectionController.setWidgetTitle(name);
    }

    public void decimalsSelected(int size) {
        selectionController.decimalsSelected(size);
    }

    /**
     * Updates the config panel to reflect the current selection state.
     */
    void showConfig() {
        List<Widget> selected = selectionController.getSelectedWidgets();
        if (sideConfigPanel != null) {
            if (!selected.isEmpty()) {
                sideConfigPanel.setLastSelectedWidget(Iterables.getLast(selected));
            } else {
                sideConfigPanel.setLastSelectedWidget(null);
            }
        }

        if (editableProperty.get()) {
            this.dashBordPlugIn.showWidgetControlPane(showSideEditorProperty.get());
        } else {
            this.dashBordPlugIn.showWidgetControlPane(false);
        }
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
