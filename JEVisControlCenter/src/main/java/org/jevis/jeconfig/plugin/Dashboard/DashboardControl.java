package org.jevis.jeconfig.plugin.Dashboard;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.CommonDialogs;
import org.jevis.jeconfig.dialog.HiddenConfig;
import org.jevis.jeconfig.plugin.Dashboard.config2.ConfigManager;
import org.jevis.jeconfig.plugin.Dashboard.config2.DashboardPojo;
import org.jevis.jeconfig.plugin.Dashboard.config2.DashboardSorter;
import org.jevis.jeconfig.plugin.Dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.Dashboard.timeframe.TimeFrames;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardControl {


    private static final Logger logger = LogManager.getLogger(DashboardControl.class);
    private double zoomFactor = 1.0d;
    private final DashBordPlugIn dashBordPlugIn;
    private final ConfigManager configManager;
    private boolean editable = false;
    private final JEVisDataSource jevisDataSource;
    private TimerTask updateTask;
    private final ObservableList<Task> runningUpdateTaskList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private ObservableList<Widget> widgetList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
//    private List<Widget> widgetList = Collections.synchronizedList(new ArrayList<>());

    private Timer updateTimer = new Timer(true);
    private DashboardPojo activeDashboard;
    private ExecutorService executor;
    private boolean isUpdateRunning = false;
    private java.io.File newBackgroundFile;
    private Interval activeInterval = new Interval(new DateTime(), new DateTime());
    private TimeFrameFactory activeTimeFrame;
    private final TimeFrames timeFrames;
    private List<JEVisObject> dashboardObjects = new ArrayList<>();
    private boolean fitToParent = false;
    public BooleanProperty highligtProperty = new SimpleBooleanProperty(false);
    private TimeFrameFactory defaultTimeFrame = null;

    public DashboardControl(DashBordPlugIn plugin) {
        this.configManager = new ConfigManager(plugin.getDataSource());
        this.dashBordPlugIn = plugin;
        this.jevisDataSource = plugin.getDataSource();
        this.activeDashboard = this.configManager.createEmptyDashboard();
        this.timeFrames = new TimeFrames(this.jevisDataSource);
        this.activeTimeFrame = this.timeFrames.day();


        this.highligtProperty.addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.widgetList.forEach(widget -> {
                    widget.setGlow(false);
//                    highlightWidgetInView(widget, false);
                });
            }
        });


    }


    public void loadFirstDashboard() {
        try {
            loadDashboardObjects();

            JEVisObject userDasboad = getUserSelectedDashboard();
            if (userDasboad != null) {
                selectDashboard(userDasboad);
            } else if (!this.dashboardObjects.isEmpty()) {
                selectDashboard(this.dashboardObjects.get(0));
            } else {


            }

//            JEVisObject firstObject = this.jevisDataSource.getObject(3808l);
//            selectDashboard(firstObject);
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }

    public DashboardPojo getActiveDashboard() {
        return this.activeDashboard;
    }


    public void setRootSizeChanged(double width, double height) {
        if (this.fitToParent && width != 0.0 && height != 0.0) {
            this.dashBordPlugIn.getDashBoardPane().zoomToParent(width, height);
        }

    }

    public void highlightWidgetInView(Widget widget, boolean highlight) {
        if (this.highligtProperty.getValue()) {
            widget.setGlow(highlight);
        }
    }


    public void zoomIn() {
        if (this.zoomFactor < 3) {
            this.zoomFactor = this.zoomFactor + 0.05d;
        }
        this.zoomFactor = this.zoomFactor + 0.1d;
        this.dashBordPlugIn.getDashBoardPane().setZoom(this.zoomFactor);

    }

    public void zoomOut() {
        if (this.zoomFactor > -0.2) {
            this.zoomFactor = this.zoomFactor - 0.05d;
        }
        this.dashBordPlugIn.getDashBoardPane().setZoom(this.zoomFactor);
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

    /**
     * Load an dashboard and view it.
     *
     * @param object
     */
    public void selectDashboard(JEVisObject object) {
        logger.error("selectDashboard: {}", object);
        try {

            restartExecutor();
            restView();

            this.activeDashboard = this.configManager.loadDashboard(this.configManager.readDashboardFile(object));
            this.activeDashboard.setJevisObject(object);

            this.dashBordPlugIn.getDashBoardPane().updateView();
            this.widgetList.addAll(this.configManager.createWidgets(this, this.activeDashboard.getWidgetList()));

            this.dashBordPlugIn.setContentSize(this.activeDashboard.getSize().getWidth(), this.activeDashboard.getSize().getHeight());

            this.configManager.getBackgroundImage(this.activeDashboard.getDashboardObject()).addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    this.dashBordPlugIn.getDashBoardPane().setBackgroundImage(newValue);
                }
            });

            this.widgetList.forEach(widget -> {
                widget.updateConfig();
                this.dashBordPlugIn.getDashBoardPane().addWidget(widget);
            });
            this.widgetList.forEach(widget -> {
                widget.updateConfig(widget.getConfig(), this.dashBordPlugIn.getDashBoardPane());
            });
            this.widgetList.forEach(widget -> {
                addWidgetUpdateTask(widget, this.getInterval());
            });

            this.dashBordPlugIn.getDashBoardToolbar().updateView(this.activeDashboard);
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }

    public void setEditable(boolean editable) {
        this.editable = editable;

        this.dashBordPlugIn.editProperty.setValue(this.editable);
    }

    public void reload() {
        this.jevisDataSource.reloadAttribute(this.activeDashboard.getDashboardObject());
        selectDashboard(this.activeDashboard.getDashboardObject());
    }

    public void setActiveTimeFrame(TimeFrameFactory activeTimeFrame) {
        this.activeTimeFrame = activeTimeFrame;
        setInterval(this.activeInterval);
    }

    public TimeFrameFactory getActiveTimeFrame() {
        return this.activeTimeFrame;
    }

    public void setPrevInteval() {
        Interval nextInterval = this.activeTimeFrame.previousPeriod(this.activeInterval, 1);
        if (nextInterval.getStart().isBeforeNow()) {
            setInterval(nextInterval);
        }
    }

    public void setNextInterval() {
        Interval nextInterval = this.activeTimeFrame.nextPeriod(this.activeInterval, 1);
        if (nextInterval.getStart().isBeforeNow()) {
            setInterval(nextInterval);
        }
    }

    private void restartExecutor() {
        if (this.executor != null) {
            this.executor.shutdownNow();
        }

        this.executor = Executors.newFixedThreadPool(HiddenConfig.DASH_THREADS);
    }

    public void setInterval(Interval interval) {
        startUpdating(false);
        this.activeInterval = interval;

        restartExecutor();

        DashboardControl.this.widgetList.parallelStream().forEach(widget -> {
            addWidgetUpdateTask(widget, interval);
        });

    }


    public void requestViewUpdate(Widget widget) {
//        widget.updateData(getInterval());
        widget.updateConfig(widget.getConfig(), this.dashBordPlugIn.getDashBoardPane());
        widget.updateConfig();
    }

    public Interval getInterval() {
        return this.activeInterval;
    }

    public void switchUpdating() {
        startUpdating(!this.isUpdateRunning);
    }

    public void removeNode(Widget widget) {
        this.widgetList.remove(widget);
    }

    public void startUpdating(boolean run) {
        this.isUpdateRunning = run;
        if (this.updateTask != null) {
            try {
                this.updateTask.cancel();
            } catch (Exception ex) {

            }
        }
        this.updateTimer.cancel();
        if (this.executor != null) this.executor.shutdownNow();

        this.executor = Executors.newFixedThreadPool(HiddenConfig.DASH_THREADS);
        this.updateTask = new TimerTask() {
            @Override
            public void run() {
                logger.debug("Starting Update");

                Interval interval = DashboardControl.this.activeDashboard.getTimeFrame().getInterval(DateTime.now());

                DashboardControl.this.widgetList.parallelStream().forEach(widget -> {
                    addWidgetUpdateTask(widget, interval);
                });
            }
        };

        if (run) {
            this.dashBordPlugIn.getDashBoardToolbar().setUpdateRunning(run);
            logger.info("Start updateData scheduler: {} sec", this.activeDashboard.getUpdateRate());
            this.updateTimer.scheduleAtFixedRate(this.updateTask, 1000, this.activeDashboard.getUpdateRate() * 1000);
        }

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
                    widget.updateData(interval);
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

    public ObservableList<Widget> getWidgetList() {
        return this.widgetList;
    }

    public List<Widget> getWidgets() {
        return this.widgetList;
    }

    public void save() {
        //if(needsave){

        //update

        if (this.activeDashboard.getNew()) {//new TODO
            NewAnalyseDialog newAnalyseDialog = new NewAnalyseDialog();
            try {

                NewAnalyseDialog.Response response = newAnalyseDialog.show((Stage) this.dashBordPlugIn.getDashBoardPane().getScene().getWindow(), this.jevisDataSource);
                if (response == NewAnalyseDialog.Response.YES) {
                    JEVisClass analisisDirClass = this.jevisDataSource.getJEVisClass(DashBordPlugIn.CLASS_ANALYSIS_DIR);
                    List<JEVisObject> analisisDir = this.jevisDataSource.getObjects(analisisDirClass, true);
                    JEVisClass analisisClass = this.jevisDataSource.getJEVisClass(DashBordPlugIn.CLASS_ANALYSIS);


                    JEVisObject newObject = newAnalyseDialog.getParent().buildObject(newAnalyseDialog.getCreateName(), analisisClass);
//                newObject.commit();//TODO
                    selectDashboard(newObject);

                    if (this.newBackgroundFile != null) {
                        this.configManager.setBackgroundImage(this.activeDashboard.getDashboardObject(), this.newBackgroundFile);
                    }

                }
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }


        } else {//update
            try {
                this.configManager.saveDashboard(this.activeDashboard, this.widgetList);
            } catch (Exception ex) {
                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.copy.error.title"),
                        I18n.getInstance().getString("dashboard.save.error"), null, ex);
            }

        }


    }

    public void setDefaultTimeFrame(TimeFrameFactory timeFrame) {
        this.defaultTimeFrame = timeFrame;
    }

    public void startWallpaperSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Pictures", "*.png", "*.gif", "*.jpg", "*.bmp"));
        File newBackground = fileChooser.showOpenDialog(JEConfig.getStage());
        if (newBackground != null) {
            try {
                BufferedImage bufferedImage = ImageIO.read(newBackground);
                javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
//                this.newBackgroundImage = fxImage;
                this.newBackgroundFile = newBackground;
                setWallpaper(fxImage);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void setWallpaper(Image image) {
        logger.error("setWallpaper: {}/{} {}", image.getHeight(), image.getWidth(), image);
        final BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);

        final BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
        final Background background = new Background(backgroundImage);

        Platform.runLater(() -> {
            this.dashBordPlugIn.getDashBoardPane().setBackground(background);
        });

    }


    /**
     * Opens the new dashboard Wizard
     * <p>
     * TODO: implement
     */
    public void startWizard() {
        PopupWindow popupWindow = new Popup();
        popupWindow.show((Stage) this.dashBordPlugIn.getDashBoardPane().getScene().getWindow(), 0, 50);

        Stage newWidget = new Stage();
        newWidget.initStyle(StageStyle.UNDECORATED);
        FlowPane flowPane = new FlowPane();
        flowPane.getChildren().addAll(new JFXButton("Test"), new JFXButton("blub"));
        Scene newScene = new Scene(flowPane);

//        Bounds boundsInScreen = this.dashBordPlugIn.localToScene(this.getBoundsInLocal());
//        System.out.println("Bounds: " + boundsInScreen);
//
//        System.out.println("B1: " + newWidgetButton.getBoundsInLocal());
//        System.out.println("B2: " + newWidgetButton.getBoundsInParent());
//        System.out.println("B3: " + newWidgetButton.layoutBoundsProperty().get());
////            boundsInScreen = newWidget.setScene(newScene);
//        newWidget.setAlwaysOnTop(true);
//        newWidget.initOwner(JEConfig.getStage());
//        newWidget.setX(boundsInScreen.getMaxX());
//        newWidget.setY(boundsInScreen.getMaxY());
//        newWidget.show();

//            Wizard wizzard = new Wizard(JEConfig.getDataSource());
//            Optional<Widget> newWidget = wizzard.show(null);
//
//            if (newWidget.isPresent()) {
//                dashBordPlugIn.addWidget(newWidget.get().getConfig());
//            }

    }

    private JEVisObject getUserSelectedDashboard() {
        JEVisObject currentUserObject = null;
        try {
            currentUserObject = this.jevisDataSource.getCurrentUser().getUserObject();
            JEVisAttribute userSelectedDashboard = currentUserObject.getAttribute("Start Dashboard");
            if (userSelectedDashboard != null) {
                TargetHelper th = new TargetHelper(this.jevisDataSource, userSelectedDashboard);
                if (th.getObject() != null && !th.getObject().isEmpty()) {
                    return th.getObject().get(0);
                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get Start Dashboard from user.");
        }

        return null;
    }

}
