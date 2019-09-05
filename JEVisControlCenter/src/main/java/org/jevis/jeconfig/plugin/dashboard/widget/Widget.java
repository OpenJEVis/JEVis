package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.tool.DragResizeMod;
import org.joda.time.Interval;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;


public abstract class Widget extends Region {
    private static final Logger logger = LogManager.getLogger(Widget.class);
    private final String TYPE = "type";
    private final org.jevis.api.JEVisDataSource jeVisDataSource;
    public WidgetPojo config;
    public Size previewSize = new Size(100, 150);
    private AnchorPane contentRoot = new AnchorPane();
    private AnchorPane editPane = new AnchorPane();
    private StackPane loadingPane = new StackPane();
    private UUID uuid = UUID.randomUUID();
    private final DashboardControl control;
    private BooleanProperty editable = new SimpleBooleanProperty(false);
    private ProgressIndicator progressIndicator = new ProgressIndicator();

//    private BooleanProperty snapToGrid = new SimpleBooleanProperty(false);

    private DragResizeMod.OnDragResizeEventListener onDragResizeEventListener = DragResizeMod.defaultListener;

    double widthCache;
    double heightCache;

    @Override
    protected double computePrefWidth(double height) {
        return widthCache != 0 ? widthCache : super.computePrefWidth(height);
    }

    @Override
    protected double computePrefHeight(double width) {
        return heightCache != 0 ? heightCache : super.computePrefHeight(width);

    }

    public Widget(DashboardControl control, WidgetPojo config) {
        super();
        logger.debug("new Widget with config");
        this.control = control;
        this.jeVisDataSource = control.getDataSource();
        this.config = config;

        initLayout();
    }

    /**
     * creates an new Widget with an default configuration
     **/
    public Widget(DashboardControl control) {
        super();
        logger.debug("new Widget without config");
        this.control = control;
        this.jeVisDataSource = control.getDataSource();
        this.config = createDefaultConfig();

        initLayout();
    }

    private void initLayout() {
        logger.debug("initLayout() {}", config.getTitle());
        this.getChildren().add(this.contentRoot);
        this.getChildren().add(this.editPane);
        this.getChildren().add(loadingPane);
        onDragResizeEventListener.setDashBoardPane(control.getDashboardPane());
        DragResizeMod.makeResizable(this, onDragResizeEventListener);
//        DragResizeMod.makeResizable(this.contentRoot, onDragResizeEventListener);

        updateConfig(this.config);
    }

    abstract public WidgetPojo createDefaultConfig();


    public void updateConfig(WidgetPojo config) {
        logger.debug("updateConfig: '{}',{},{}", config.getTitle(), config.hashCode(), (config != null));
        this.config = config;

        setNodeSize(this.config.getSize().getWidth(), this.config.getSize().getHeight());
        makeDragDropOverlay();
        makeWindowForm();
        makeLoadingOverlay();
        setBorder(this.config.getBorderSize());
        layoutXProperty().set(this.config.getxPosition());
        layoutYProperty().set(this.config.getyPosition());

        if (this.config.getShowShadow()) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(5.0);
            dropShadow.setOffsetX(3.0);
            dropShadow.setOffsetY(3.0);
            dropShadow.setColor(Color.BLACK);
//        dropShadow.setColor(Color.color(0.4, 0.5, 0.5));
            this.contentRoot.setEffect(dropShadow);
        } else {
            this.contentRoot.setEffect(null);
        }

    }


    public void setGlow(boolean glow) {

        Platform.runLater(() -> {

            this.setEffect(null);
            if (glow) {

                DropShadow borderGlow = new DropShadow();
                borderGlow.setOffsetY(-0f);
                borderGlow.setOffsetX(-0f);
                borderGlow.setColor(Color.BLUE);
                borderGlow.setWidth(30);
                borderGlow.setHeight(30);

                this.setEffect(borderGlow);
            } else {


                GaussianBlur gaussianBlur = new GaussianBlur();
                gaussianBlur.setRadius(5d);
                this.setEffect(gaussianBlur);

            }
        });

    }

    public WidgetPojo getConfig() {
        return this.config;
    }


    public JEVisDataSource getDataSource() {
        return this.jeVisDataSource;
    }

    public UUID getUUID() {
        return this.uuid;
    }


    public void setEditable(boolean editable) {
        logger.debug("Widget setEditable {}", editable);
        onDragResizeEventListener.resizeableProperty().setValue(editable);

        this.editPane.setVisible(editable);
        this.editable.setValue(editable);
    }


    private void makeLoadingOverlay() {
        this.loadingPane.setVisible(false);
        progressIndicator.setMaxSize(100, 100);
        this.loadingPane.getChildren().add(progressIndicator);
        StackPane.setAlignment(progressIndicator, Pos.CENTER);
        this.loadingPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0), new Insets(0, 0, 0, 0))));
        this.loadingPane.setOpacity(0.4);
    }

    private void makeDragDropOverlay() {
        this.editPane.setVisible(false);

        this.editPane.setBackground(new Background(new BackgroundFill(Color.GREY, new CornerRadii(0), new Insets(0, 0, 0, 0))));
        this.editPane.setOpacity(0.7);

        final ContextMenu contextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Delete", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", 18, 18));
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Widget.this.control.removeWidget(Widget.this);
            }
        });
        MenuItem configItem = new MenuItem("Open Configuration", JEConfig.getImage("Service Manager.png", 18, 18));
        configItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Widget.this.openConfig();
            }
        });

        contextMenu.getItems().addAll(configItem, delete);

        this.editPane.setOnMouseClicked(event -> {
//            if (event.getButton().equals(MouseButton.PRIMARY)) {
//                if (event.getClickCount() == 2) {
//                    openConfig();
//                }
//            }
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                contextMenu.show(this.editPane, event.getScreenX(), event.getScreenY());
            }
        });


    }


    public void setNodeSize(double width, double height) {
        logger.debug("setNodeSize: old w:{}/h:{}  new w:{}/h:{}", this.getWidth(), this.getHeight(), width, height);

        Platform.runLater(() -> {


            this.contentRoot.setMaxHeight(height);
            this.contentRoot.setMinHeight(height);
            this.contentRoot.setPrefHeight(height);

            this.contentRoot.setMinWidth(width);
            this.contentRoot.setMinWidth(width);
            this.contentRoot.setPrefWidth(width);


            this.editPane.setMaxHeight(height);
            this.editPane.setMinHeight(height);
            this.editPane.setPrefHeight(height);

            this.editPane.setMinWidth(width);
            this.editPane.setMinWidth(width);
            this.editPane.setPrefWidth(width);


            this.loadingPane.setMaxHeight(height);
            this.loadingPane.setMinHeight(height);
            this.loadingPane.setPrefHeight(height);

            this.loadingPane.setMinWidth(width);
            this.loadingPane.setMinWidth(width);
            this.loadingPane.setPrefWidth(width);

            setMaxWidth(width);
            setMinWidth(width);
            setPrefWidth(width);

            setMaxHeight(height);
            setMinHeight(height);
            setPrefHeight(height);

        });


//        this.editPane.setMaxHeight(height);
//        this.editPane.setMinHeight(height);
//        this.editPane.setPrefHeight(height);
    }

    private void makeWindowForm() {
        setBackgroundColor(this.config.getBackgroundColor());
    }

    private void setBackgroundColor(Color color) {
//        color = Color.LIGHTBLUE;//test
        Background bgColor = new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
        this.contentRoot.setBackground(bgColor);
    }

    public abstract ImageView getImagePreview();


    private void setBorder(BorderWidths newValue) {
        this.contentRoot.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, newValue)));
    }


    public void showProgressIndicator(boolean show) {
        if (show) {
            loadingPane.setVisible(true);
        } else {
            loadingPane.setVisible(false);
        }

    }

    public void setGraphic(Node node) {
        this.contentRoot.getChildren().clear();
        this.contentRoot.getChildren().add(node);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);


    }

    /**
     * This function will be called if a new Value arrives
     */
    public abstract void updateData(Interval interval);

    public abstract void updateLayout();

    public abstract void updateConfig();

    /**
     * Init will be called ones if the the widget will be created
     */
    public abstract void init();


    /**
     * Unique ID of this Widget
     */
    public abstract String typeID();

    public void openConfig() {
//        WidgetConfigEditor widgetConfigEditor = new WidgetConfigEditor(this.config);
//        widgetConfigEditor.show();
    }


    public abstract ObjectNode toNode();

    public ObjectNode createDefaultNode() {
        ObjectMapper mapper = new ObjectMapper();


        ObjectNode dashBoardNode = mapper.createObjectNode();
        dashBoardNode
                .put(JsonNames.Widget.TYPE, typeID())
                .put(JsonNames.Widget.TITLE, this.config.getTitle())
                .put(JsonNames.Widget.BACKGROUND_COLOR, this.config.getBackgroundColor().toString())
                .put(JsonNames.Widget.FONT_COLOR, this.config.getFontColor().toString())
                .put(JsonNames.Widget.FONT_SIZE, this.config.getFontSize())
                .put(JsonNames.Widget.TITLE_POSITION, this.config.getTitlePosition().toString())
                .put(JsonNames.Widget.BORDER_SIZE, this.config.getBorderSize().getTop())
                .put(JsonNames.Widget.WIDTH, this.config.getSize().getWidth())
                .put(JsonNames.Widget.HEIGHT, this.config.getSize().getHeight())
                .put(JsonNames.Widget.X_POS, this.config.getxPosition())
                .put(JsonNames.Widget.Y_POS, this.config.getyPosition());

        return dashBoardNode;
    }

    @Override
    public Widget clone() {
//        Class cls = Class.forName(this.getcl);

        try {
            Widget newWidget = (Widget) this.getClass().getDeclaredConstructor(DashboardControl.class).newInstance(control);
            //TODO clone WidgetPojo
            return newWidget;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

}
