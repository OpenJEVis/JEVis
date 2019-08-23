package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jfxtras.labs.util.event.MouseControlUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashBoardPane;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.joda.time.Interval;

import java.util.UUID;


public abstract class Widget extends Group {
    private static final Logger logger = LogManager.getLogger(Widget.class);
    private final String TYPE = "type";
    private final org.jevis.api.JEVisDataSource jeVisDataSource;
    public WidgetPojo config;
    public Size previewSize = new Size(100, 150);
    private AnchorPane contentRoot = new AnchorPane();
    private AnchorPane editPane = new AnchorPane();
    private UUID uuid = UUID.randomUUID();
    private final DashboardControl control;
    private Size lastSize = new Size(0, 0);
    private BooleanProperty editable = new SimpleBooleanProperty(false);
    private BooleanProperty snapToGrid = new SimpleBooleanProperty(false);

    public Widget(DashboardControl control, WidgetPojo config) {
        super();
        this.control = control;
        this.jeVisDataSource = control.getDataSource();
        if (config != null) {
            this.config = config;
        } else {
            this.config.setType(typeID());
        }
        this.getChildren().add(this.editPane);
        getChildren().add(this.contentRoot);
    }


    public void updateConfig(WidgetPojo config, DashBoardPane parent) {
        this.config = config;

        if (!this.lastSize.equals(this.config.getSize())) {
            setSize(this.config.getSize().getWidth(), this.config.getSize().getHeight());
            makeDragDropOverlay();
            makeWindowForm();
        }
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

        setOnMouseReleased(event -> {
            if (this.snapToGrid.get() && this.editable.get()) {
                layoutXProperty().setValue(parent.getNextGridX(layoutXProperty().longValue()));
                layoutYProperty().setValue(parent.getNextGridY(layoutYProperty().longValue()));
            }
        });
    }


    public void setShadow() {
        Platform.runLater(() -> {

        });
    }

    public void setGlow(boolean glow) {
//        Platform.runLater(() -> {
//            if (glow) {
//                Glow glowEffect = new Glow();
//                glowEffect.setLevel(0.9);
//                InnerShadow is = new InnerShadow();
//                is.setOffsetX(2.0f);
//                is.setOffsetY(2.0f);
//
//                int depth = 30;
//
//                DropShadow borderGlow = new DropShadow();
//                borderGlow.setOffsetY(-0f);
//                borderGlow.setOffsetX(-0f);
//                borderGlow.setColor(Color.BLUE);
//                borderGlow.setWidth(depth);
//                borderGlow.setHeight(depth);
//
//                borderGlow.setInput(is);
//
//                this.setEffect(borderGlow);
//            } else {
//                this.setEffect(null);
//            }
//
//        });


        Platform.runLater(() -> {

            if (glow) {
                this.setEffect(null);

//                FadeTransition animation = new FadeTransition(Duration.millis(150), this);
//                animation.setFromValue(0);
//                animation.setToValue(1);
//                animation.setCycleCount(2);
//                animation.setAutoReverse(false);
//                animation.playFromStart();
//                InnerShadow is = new InnerShadow();
//                is.setOffsetX(2.0f);
//                is.setOffsetY(2.0f);
//                is.setColor(Color.RED);

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

    public void setMoveable(boolean moveable) {
        this.snapToGrid.set(moveable);
    }

    public void setEditable(boolean editable) {
        this.editable.setValue(editable);
    }

    private void makeDragDropOverlay() {
        this.editPane.setVisible(false);
        HBox windowHeader = new HBox();
        Button configButton = new Button("", JEConfig.getImage("Service Manager.png", 16, 16));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(configButton);

        Button deleteButton = new Button("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", 16, 16));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(deleteButton);

        configButton.setOnAction(event -> {
//            config.openConfig();
            this.openConfig();
        });


        Tooltip tooltip = new Tooltip(this.config.getTitle());
        configButton.setTooltip(tooltip);

        deleteButton.setOnAction(event -> {
            Widget.this.setVisible(false);
            this.control.removeNode(Widget.this);

        });

        AnchorPane.setTopAnchor(windowHeader, 0.0);
        AnchorPane.setLeftAnchor(configButton, 0.0);
        AnchorPane.setRightAnchor(windowHeader, 0.0);


        this.editable.addListener((observable, oldValue, newValue) -> {
            configButton.setVisible(newValue);
            this.editPane.setVisible(newValue);

            if (newValue) {
                MouseControlUtil.makeDraggable(this);
            } else {
                this.setOnMouseDragged(null);
                this.setOnMousePressed(null);
            }
        });

        setNodeSize(this.editPane, this.config.getSize().getWidth(), this.config.getSize().getHeight());
        windowHeader.getChildren().addAll(configButton, deleteButton);
        windowHeader.setBackground(new Background(new BackgroundFill(Color.GREY, new CornerRadii(0), new Insets(0, 0, 0, 0))));

        this.editPane.setBackground(new Background(new BackgroundFill(Color.GREY, new CornerRadii(0), new Insets(0, 0, 0, 0))));
        this.editPane.setOpacity(0.5);
        this.editPane.getChildren().addAll(windowHeader);

        EventHandler<MouseEvent> openConfigEvent = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    openConfig();
                }
            }
        };
        windowHeader.setOnMouseClicked(openConfigEvent);
        this.editPane.setOnMouseClicked(openConfigEvent);
    }

    private void setNodeSize(Region node, double width, double height) {
        node.setMaxWidth(width);
        node.setMinWidth(width);
        node.setPrefWidth(width);
        node.setMaxHeight(height);
        node.setMinHeight(height);
        node.setPrefHeight(height);
    }

    private void makeWindowForm() {
        setBackgroundColor(this.config.getBackgroundColor());
    }

    private void setBackgroundColor(Color color) {
        Background bgColor = new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
        this.contentRoot.setBackground(bgColor);
    }

    public abstract ImageView getImagePreview();


    private void setBorder(BorderWidths newValue) {
        this.contentRoot.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, newValue)));
    }

    /**
     * TODO: this function will standardize the size of the widgets
     */
    public void setSize(double width, double height) {
        this.contentRoot.setMinWidth(width);
        this.contentRoot.setMaxWidth(width);
        this.contentRoot.setPrefWidth(width);

        this.contentRoot.setMaxHeight(height);
        this.contentRoot.setMinHeight(height);
        this.contentRoot.setPrefHeight(height);
    }

    public void showProgressIndicator(boolean show) {
//        ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
//        AnchorPane.setTopAnchor(progressIndicator, contentRoot.getHeight() / 2);
//        AnchorPane.setLeftAnchor(progressIndicator, contentRoot.getWidth() / 2);
//
//
//        if (show) {
//
//
//            contentRoot.getChildren().add(progressIndicator);
//
//        } else {
//            contentRoot.getChildren().remove(progressIndicator);
//        }

    }

    public void setGraphic(Node node) {
        this.contentRoot.getChildren().clear();
        this.contentRoot.getChildren().add(node);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);


//        contentRoot.setCenter(node);
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

}
