package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jfxtras.labs.util.event.MouseControlUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.DashBoardPane;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordModel;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;
import org.joda.time.Interval;

import java.util.UUID;


public abstract class Widget extends Group {
    private static final Logger logger = LogManager.getLogger(Widget.class);
    private final String TYPE = "type";
    private final org.jevis.api.JEVisDataSource jeVisDataSource;
    public WidgetConfig config = new WidgetConfig("Generic");
    public Size previewSize = new Size(100, 150);
    public SampleHandler dataHandler;
    private DashBoardPane dashBoard;
    private AnchorPane contentRoot = new AnchorPane();
    private AnchorPane editPane = new AnchorPane();
    private UUID uuid = UUID.randomUUID();

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

//    public void setConfig(WidgetConfig config) {
//        logger.error("Widget.setConfig()");
//        ObjectMapper mapper = new ObjectMapper();//.enable(SerializationFeature.INDENT_OUTPUT);
//        try {
//            logger.info("Widget.SetConfig: {}", mapper.writeValueAsString(config));
//        } catch (JsonProcessingException e) {
////            e.printStackTrace();
//        }
//        this.config = config;
//
//    }


    public JEVisDataSource getDataSource() {
        return jeVisDataSource;
    }

    public UUID getUUID() {
        return uuid;
    }


    private void makeDragDropOverlay() {
        HBox windowHeader = new HBox();
        Button configButton = new Button("", JEConfig.getImage("Service Manager.png", 18, 18));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(configButton);

        Button deleteButton = new Button("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", 18, 18));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(deleteButton);

        configButton.setOnAction(event -> {
            config.openConfig();
        });

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
//        windowHeader.setOpacity(0.5);
        editPane.setBackground(new Background(new BackgroundFill(Color.GREY, new CornerRadii(0), new Insets(0, 0, 0, 0))));
        editPane.setOpacity(0.5);
        config.size.addListener((observable, oldValue, newValue) -> {
            setNodeSize(editPane, newValue.getWidth(), newValue.getHeight());
        });
        setNodeSize(editPane, config.size.getValue().getWidth(), config.size.getValue().getHeight());

        editPane.getChildren().addAll(windowHeader);


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
        Rectangle background = new Rectangle();
        background.setX(0);
        background.setY(0);
        background.setWidth(config.size.getValue().getWidth());
        background.setHeight(config.size.getValue().getHeight());
        background.setArcWidth(0);
        background.setArcHeight(0);
//        background.setStroke(Color.BLACK);
//        background.setFill(dashBoard.getDashBordAnalysis().colorWidgetPlugin.getValue());
        background.setFill(config.backgroundColor.getValue());
//        background.setOpacity(dashBoard.getDashBordAnalysis().opacityWidgetPlugin.getValue());
        config.size.addListener((observable, oldValue, newValue) -> {
            background.setWidth(newValue.getWidth());
            background.setHeight(newValue.getHeight());
        });

        config.backgroundColor.addListener((observable, oldValue, newValue) -> {
//            donut.setBackgroundColor(newValue);
            background.setFill(newValue);
        });

        Background bgColor = new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY));


        contentRoot.setBackground(bgColor);

//        this.getChildren().add(background);
    }

    public abstract ImageView getImagePreview();

    public DashBordModel getAnalysis() {
        return dashBoard.getDashBordAnalysis();
    }

    public void setDashBoard(DashBoardPane parent) {
        System.out.println("setDashBoard()");
        this.dashBoard = parent;

        layoutXProperty().bindBidirectional(config.xPosition);
        layoutYProperty().bindBidirectional(config.yPosition);

        makeWindowForm();
        getChildren().add(contentRoot);
        makeDragDropOverlay();


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

        getAnalysis().editProperty.addListener((observable, oldValue, newValue) -> {
            System.out.println("Set Edit: " + newValue);
            editPane.setVisible(newValue);
        });
        editPane.setVisible(getAnalysis().editProperty.get());

        setOnMouseReleased(event -> {
            if (getAnalysis().snapToGridProperty.getValue()) {
                layoutXProperty().setValue(parent.getNextGridX(layoutXProperty().longValue()));
                layoutYProperty().setValue(parent.getNextGridY(layoutYProperty().longValue()));
            }
        });


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


}
