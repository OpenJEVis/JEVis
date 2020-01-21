package org.jevis.jeconfig.plugin.dashboard;

//import com.itextpdf.text.Document;
//import com.itextpdf.text.pdf.PdfWriter;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private StringProperty nameProperty = new SimpleStringProperty("Dashboard");
    private StringProperty uuidProperty = new SimpleStringProperty("Dashboard");
    private boolean isInitialized = false;
    private NotificationPane notificationPane;
    private AnchorPane rootPane = new AnchorPane();


    private final DashboardControl dashboardControl;
    private JEVisDataSource jeVisDataSource;
    private final DashBoardPane dashBoardPane;
    private final DashBoardToolbar toolBar;
    private ScrollPane scrollPane = new ScrollPane();

    public DashBordPlugIn(JEVisDataSource ds, String name) {
        logger.debug("init DashBordPlugIn");
        this.rootPane.setStyle("-fx-background-color: blue;");

        this.nameProperty.setValue(name);
        this.jeVisDataSource = ds;


        this.dashboardControl = new DashboardControl(this);
        this.toolBar = new DashBoardToolbar(this.dashboardControl);
        this.dashBoardPane = new DashBoardPane(this.dashboardControl);
        this.dashboardControl.setDashboardPane(dashBoardPane);
        this.scrollPane.setContent(this.dashBoardPane);

        Layouts.setAnchor(this.scrollPane, 0d);
        Layouts.setAnchor(this.rootPane, 0d);


        this.rootPane.getChildren().setAll(this.scrollPane);

        notificationPane = new NotificationPane(rootPane);
//        notificationPane.setStyle("-fx-background-color: red;");
        notificationPane.setStyle("-fx-focus-color: transparent;");
        rootPane.setStyle("-fx-focus-color: transparent;");
        notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);


        ChangeListener<Number> sizeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //DashBordPlugIn.this.dashboardControl.setRootSizeChanged(DashBordPlugIn.this.scrollPane.getWidth(), DashBordPlugIn.this.scrollPane.getHeight());

                Size size = getPluginSize();
                DashBordPlugIn.this.dashboardControl.setRootSizeChanged(size.getWidth(), size.getHeight());
            }
        };
        this.scrollPane.widthProperty().addListener(sizeListener);
        this.scrollPane.heightProperty().addListener(sizeListener);

        //dashBoardPane.getChildren().add(new BorderPane());

    }

    public ScrollPane getScrollPane(){
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


    public Size getPluginSize() {
        return new Size(rootPane.getHeight(), rootPane.getWidth());
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
        System.out.println();
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
                return true;
            case Constants.Plugin.Command.DELETE:
                return false;
            case Constants.Plugin.Command.EXPAND:
                return false;
            case Constants.Plugin.Command.NEW:
                return false;
            case Constants.Plugin.Command.RELOAD:
                return true;
            case Constants.Plugin.Command.ADD_TABLE:
                return false;
            case Constants.Plugin.Command.EDIT_TABLE:
                return false;
            case Constants.Plugin.Command.CREATE_WIZARD:
                return false;
            case Constants.Plugin.Command.FIND_OBJECT:
                return false;
            case Constants.Plugin.Command.PASTE:
                return false;
            case Constants.Plugin.Command.COPY:
                return false;
            case Constants.Plugin.Command.CUT:
                return false;
            case Constants.Plugin.Command.FIND_AGAIN:
                return false;
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
                return;
            case Constants.Plugin.Command.RELOAD:
                dashboardControl.reload();
                return;
            default:
                logger.error("unknown PluginCommand: {}" + cmdType);
                return;
        }
    }

    @Override
    public Node getContentNode() {
        return notificationPane;
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


}
