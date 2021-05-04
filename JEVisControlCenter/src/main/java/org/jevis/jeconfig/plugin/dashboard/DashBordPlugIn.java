package org.jevis.jeconfig.plugin.dashboard;

//import com.itextpdf.text.Document;
//import com.itextpdf.text.pdf.PdfWriter;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.HiddenSidesPane;
import org.controlsfx.control.NotificationPane;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.tool.Layouts;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class DashBordPlugIn implements Plugin {

    private static final Logger logger = LogManager.getLogger(DashBordPlugIn.class);
    public static String CLASS_ANALYSIS = "Dashboard Analysis", CLASS_ANALYSIS_DIR = "Analyses Directory", ATTRIBUTE_DATA_MODEL_FILE = "Data Model File", ATTRIBUTE_DATA_MODEL = "Data Model", ATTRIBUTE_BACKGROUND = "Background";
    public static String PLUGIN_NAME = "Dashboard Plugin";
    private static final double scrollBarSize = 18;
    private final StringProperty nameProperty = new SimpleStringProperty("Dashboard");
    private boolean isInitialized = false;
    private final StringProperty uuidProperty = new SimpleStringProperty("Dashboard");
    private final NotificationPane notificationPane;


    private final DashboardControl dashboardControl;
    private JEVisDataSource jeVisDataSource;
    private final DashBoardPane dashBoardPane;
    private final DashBoardToolbar toolBar;
    private final AnchorPane rootPane = new AnchorPane();
    private final StackPane dialogPane = new StackPane(rootPane);
    private final HiddenSidesPane hiddenSidesPane = new HiddenSidesPane(dialogPane, new Region(), new Region(), new Region(), new Region());
    private final ScrollPane scrollPane = new ScrollPane();
    /**
     * pane which gets the zoomed size of the dashboard so the layout of the ScrollPane is ok
     * Group() is not working because of Chart problems
     */
    private final Pane zoomPane = new Pane();


    public DashBordPlugIn(JEVisDataSource ds, String name) {
        logger.debug("init DashBordPlugIn");
        this.rootPane.setStyle("-fx-background-color: blue;");

        this.nameProperty.setValue(name);
        this.jeVisDataSource = ds;


        this.dashboardControl = new DashboardControl(this);
        this.toolBar = new DashBoardToolbar(this.dashboardControl);
        this.dashBoardPane = new DashBoardPane(this.dashboardControl);
        this.dashboardControl.setDashboardPane(dashBoardPane);
//        this.workaround.getChildren().add(dashBoardPane);
//        this.scrollPane.setContent(workaround);
//        workaround.setAutoSizeChildren(true);
//        this.scrollPane.setContent(dashBoardPane);


        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        zoomPane.getChildren().add(dashBoardPane);
        this.scrollPane.setContent(zoomPane);

        Layouts.setAnchor(this.zoomPane, 0d);
        Layouts.setAnchor(this.scrollPane, 0d);
        Layouts.setAnchor(this.rootPane, 0d);


        this.rootPane.getChildren().setAll(this.scrollPane);

        notificationPane = new NotificationPane(dialogPane);
//        notificationPane.setStyle("-fx-background-color: red;");
        notificationPane.setStyle("-fx-focus-color: transparent;");
        rootPane.setStyle("-fx-focus-color: transparent;");
        notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);


        ChangeListener<Number> sizeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Size size = getPluginSize();
                DashBordPlugIn.this.dashboardControl.setRootSizeChanged(size.getWidth(), size.getHeight());
            }
        };
        this.scrollPane.widthProperty().addListener(sizeListener);
        this.scrollPane.heightProperty().addListener(sizeListener);
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);


        dashBoardPane.boundsInParentProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                /** Minus 10pix to account for the common use case of shadows **/
                //Layouts.setSize(zoomPane,dashBoardPane.getBoundsInParent().getWidth()-10,dashBoardPane.getBoundsInParent().getHeight()-10);
                Layouts.setSize(zoomPane, dashBoardPane.getBoundsInParent().getWidth(), dashBoardPane.getBoundsInParent().getHeight());
            });
        });
    }

    public Pane getZoomPane() {
        return zoomPane;
    }


    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public void showMessage(String message) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Platform.runLater(() -> notificationPane.show(message));

        executor.schedule(new Runnable() {
            @Override
            public void run() {
                notificationPane.hide();
            }
        }, 5, TimeUnit.SECONDS);

    }


    public void setPluginSize(double height, double width) {
        scrollPane.setMinWidth(width);
        scrollPane.setMinHeight(height);
    }


    public Size getPluginSize() {
        logger.debug("getPluginSize in bounds: {}/{}", rootPane.getBoundsInParent().getWidth(), rootPane.getBoundsInParent().getHeight());

        return new Size(rootPane.getBoundsInParent().getHeight() - scrollBarSize, rootPane.getBoundsInParent().getWidth() - scrollBarSize);
    }

    public DashBoardToolbar getDashBoardToolbar() {
        return this.toolBar;
    }

    @Override
    public String getClassName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getName() {
        return this.nameProperty.getValue();
    }

    @Override
    public void setName(String name) {
        this.nameProperty.setValue(name);
    }

    @Override
    public StringProperty nameProperty() {
        return this.nameProperty;
    }

    @Override
    public String getUUID() {
        return this.uuidProperty.getValue();
    }

    @Override
    public void setUUID(String id) {
        this.uuidProperty.setValue(id);
    }

    @Override
    public String getToolTip() {
        return this.nameProperty.getValue();
    }

    @Override
    public StringProperty uuidProperty() {
        return this.uuidProperty;
    }

    @Override
    public Node getMenu() {
        return new Pane();
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
            case Constants.Plugin.Command.NEW:
            case Constants.Plugin.Command.RELOAD:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Node getToolbar() {
        return this.toolBar;
    }

    @Override
    public void updateToolbar() {

    }

    @Override
    public JEVisDataSource getDataSource() {
        return this.jeVisDataSource;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {
        this.jeVisDataSource = ds;
    }

    @Override
    public void handleRequest(int cmdType) {
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
                this.dashboardControl.save();
                break;
            case Constants.Plugin.Command.RELOAD:
                dashboardControl.reload();
                break;
            case Constants.Plugin.Command.HELP:
                dashboardControl.toggleTooltip();
                break;
            default:
                logger.error("unknown PluginCommand: {}", cmdType);
                break;
        }
    }

    @Override
    public Node getContentNode() {
        return hiddenSidesPane;
        //return notificationPane;
//        return this.rootPane;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("if_dashboard_46791.png", 20, 20);
    }

    @Override
    public void fireCloseEvent() {
    }


    @Override
    public void setHasFocus() {
        if (!this.isInitialized) {
            this.isInitialized = true;
            this.dashboardControl.loadFirstDashboard();
            logger.debug("DashBordPlugIn focus.size: {}/{} {}/{} ", this.rootPane.getWidth(), this.rootPane.getHeight(), this.scrollPane.getWidth(), this.scrollPane.getHeight());

        }
    }

    @Override
    public void lostFocus() {
        dashboardControl.hideAllToolTips();
    }

    public DashBoardPane getDashBoardPane() {
        return this.dashBoardPane;
    }


    @Override
    public void openObject(Object object) {
        if (object instanceof JEVisObject) {
            JEVisObject dashboardObject = (JEVisObject) object;
            dashboardControl.selectDashboard(dashboardObject);
        }

    }

    @Override
    public int getPrefTapPos() {
        return 1;
    }


    public StackPane getDialogPane() {
        return dialogPane;
    }


    public HiddenSidesPane getHiddenSidesPane() {
        return hiddenSidesPane;
    }

    public void showConfig(Node node) {

        hiddenSidesPane.setRight(node);
        hiddenSidesPane.setPinnedSide(Side.RIGHT);
    }
}
