package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
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
import org.jevis.jeconfig.plugin.Dashboard.DashBoardPane;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordModel;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfigEditor;
import org.joda.time.Interval;

import java.util.UUID;


public abstract class Widget extends Group {
    private static final Logger logger = LogManager.getLogger(Widget.class);
    private final String TYPE = "type";
    private final org.jevis.api.JEVisDataSource jeVisDataSource;
    public WidgetConfig config = new WidgetConfig("Generic");
    public Size previewSize = new Size(100, 150);
    private DashBoardPane dashBoard;
    private AnchorPane contentRoot = new AnchorPane();
    private AnchorPane editPane = new AnchorPane();
    private UUID uuid = UUID.randomUUID();
    private ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);

    public Widget(JEVisDataSource jeVisDataSource) {
        super();
        this.jeVisDataSource = jeVisDataSource;
    }

    public Widget(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        super();
        this.jeVisDataSource = jeVisDataSource;
        if (config != null) {
            this.config = config;
        } else {
            this.config.setType(typeID());
        }

    }


    public WidgetConfig getConfig() {
        return config;
    }


    public JEVisDataSource getDataSource() {
        return jeVisDataSource;
    }

    public UUID getUUID() {
        return uuid;
    }


    private void makeDragDropOverlay() {
        HBox windowHeader = new HBox();
        Button configButton = new Button("", JEConfig.getImage("Service Manager.png", 16, 16));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(configButton);

        Button deleteButton = new Button("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", 16, 16));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(deleteButton);

        configButton.setOnAction(event -> {
//            config.openConfig();
            this.openConfig();
        });


        Tooltip tooltip = new Tooltip(config.title.getValue());
        configButton.setTooltip(tooltip);

        deleteButton.setOnAction(event -> {
            dashBoard.removeNode(Widget.this);
            Widget.this.setVisible(false);
        });

        AnchorPane.setTopAnchor(windowHeader, 0.0);
        AnchorPane.setLeftAnchor(configButton, 0.0);
        AnchorPane.setRightAnchor(windowHeader, 0.0);

        editPane.visibleProperty().bindBidirectional(configButton.visibleProperty());


        windowHeader.getChildren().addAll(configButton, deleteButton);
        windowHeader.setBackground(new Background(new BackgroundFill(Color.GREY, new CornerRadii(0), new Insets(0, 0, 0, 0))));

        editPane.setBackground(new Background(new BackgroundFill(Color.GREY, new CornerRadii(0), new Insets(0, 0, 0, 0))));
        editPane.setOpacity(0.5);
        config.size.addListener((observable, oldValue, newValue) -> {
            setNodeSize(editPane, newValue.getWidth(), newValue.getHeight());
        });
        setNodeSize(editPane, config.size.getValue().getWidth(), config.size.getValue().getHeight());

        editPane.getChildren().addAll(windowHeader);

        EventHandler<MouseEvent> openConfigEvent = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    openConfig();
                }
            }
        };
        windowHeader.setOnMouseClicked(openConfigEvent);
        editPane.setOnMouseClicked(openConfigEvent);

        this.getChildren().add(editPane);
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
        setBackgroundColor(config.backgroundColor.getValue());
        config.backgroundColor.addListener((observable, oldValue, newValue) -> {
            setBackgroundColor(newValue);
        });

    }

    private void setBackgroundColor(Color color) {
        Background bgColor = new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
        contentRoot.setBackground(bgColor);
    }

    public abstract ImageView getImagePreview();

    public DashBordModel getAnalysis() {
        return dashBoard.getDashBordAnalysis();
    }

    public void setDashBoard(DashBoardPane parent) {
        this.dashBoard = parent;

        layoutXProperty().bindBidirectional(config.xPosition);
        layoutYProperty().bindBidirectional(config.yPosition);

        makeWindowForm();
        getChildren().add(contentRoot);
        makeDragDropOverlay();


        if (config.showShadow.getValue()) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(5.0);
            dropShadow.setOffsetX(3.0);
            dropShadow.setOffsetY(3.0);
            dropShadow.setColor(Color.BLACK);
//        dropShadow.setColor(Color.color(0.4, 0.5, 0.5));
            contentRoot.setEffect(dropShadow);
        }


        getAnalysis().editProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                MouseControlUtil.makeDraggable(this);
            } else {
                this.setOnMouseDragged(null);
                this.setOnMousePressed(null);
            }

        });

        setSize(config.size.get().getWidth(), config.size.get().getHeight());
        config.size.addListener((observable, oldValue, newValue) -> {
            setSize(newValue.getWidth(), newValue.getHeight());
        });

        setBorder(config.borderSize.get());
        config.borderSize.addListener((observable, oldValue, newValue) -> {
            setBorder(newValue);
        });

        getAnalysis().editProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                editPane.setVisible(newValue);
            }
        });
        editPane.setVisible(getAnalysis().editProperty.get());

        setOnMouseReleased(event -> {
            if (getAnalysis().snapToGridProperty.getValue()) {
                layoutXProperty().setValue(parent.getNextGridX(layoutXProperty().longValue()));
                layoutYProperty().setValue(parent.getNextGridY(layoutYProperty().longValue()));
            }
        });


    }

    private void setBorder(BorderWidths newValue) {
        this.contentRoot.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, newValue)));
    }

    /**
     * TODO: this function will standardize the size of the widgets
     */
    public void setSize(double width, double height) {
        contentRoot.setMinWidth(width);
        contentRoot.setMaxWidth(width);
        contentRoot.setPrefWidth(width);

        contentRoot.setMaxHeight(height);
        contentRoot.setMinHeight(height);
        contentRoot.setPrefHeight(height);
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
        contentRoot.getChildren().clear();
        contentRoot.getChildren().add(node);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);


//        contentRoot.setCenter(node);
    }

    /**
     * This function will be called if a new Value arrives
     */
    public abstract void update(Interval interval);

    /**
     * Init will be called ones if the the widget will be created
     */
    public abstract void init();


    /**
     * Unique ID of this Widget
     */
    public abstract String typeID();

    public void openConfig() {
        WidgetConfigEditor widgetConfigEditor = new WidgetConfigEditor(config);
//        widgetConfigEditor.show();
    }


}
