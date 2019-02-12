package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import jfxtras.labs.util.event.MouseControlUtil;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.DashBoardPane;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordAnalysis;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;

import java.util.UUID;

public abstract class Widget extends Group {

    private final org.jevis.api.JEVisDataSource jeVisDataSource;
    public WidgetConfig config = new WidgetConfig();
    public CornerRadii cornerRadii = new CornerRadii(0);
    public Size previewSize = new Size(100, 150);
    public BooleanProperty noDataInPeriodProperty = new SimpleBooleanProperty(false);
    public BooleanProperty isInitialized = new SimpleBooleanProperty(false);
    private DashBoardPane dashBoard;
    private AnchorPane contentRoot = new AnchorPane();
    private AnchorPane editPane = new AnchorPane();
    private UUID uuid = UUID.randomUUID();

    public Widget(JEVisDataSource jeVisDataSource) {
        super();
        this.jeVisDataSource = jeVisDataSource;
    }

    public BooleanProperty getNoDataInPeriodProperty() {
        return noDataInPeriodProperty;
    }

    public abstract SampleHandler getSampleHandler();

    public abstract void setBackgroundColor(Color color);

    public abstract void setTitle(String text);

    public abstract void setFontColor(Color color);

    public abstract void setCustomFont(Font font);


//    public abstract List<WidgetConfigProperty> getAdditionalSetting();

    public JEVisDataSource getDataSource() {
        return jeVisDataSource;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void applyCannonConfig() {
        setBackgroundColor(config.backgroundColor.getValue());
        setFontColor(config.fontColor.getValue());
        setTitle(config.title.getValue());
        setCustomFont(config.font.getValue());
    }

    public void addCommonConfigListeners() {
        config.backgroundColor.addListener((observable, oldValue, newValue) -> {
            setBackgroundColor(newValue);
        });
        config.fontColor.addListener((observable, oldValue, newValue) -> {
            setFontColor(newValue);
        });

        config.title.addListener((observable, oldValue, newValue) -> {
            setTitle(newValue);
        });

        config.font.addListener((observable, oldValue, newValue) -> {
            setCustomFont(newValue);
        });

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

//        AnchorPane.setTopAnchor(configButton, 6.0);
//        AnchorPane.setRightAnchor(configButton, 6.0);
//
//        AnchorPane.setTopAnchor(configButton, 6.0);
//        AnchorPane.setLeftAnchor(configButton, 6.0);

        AnchorPane.setTopAnchor(windowHeader, 0.0);
        AnchorPane.setLeftAnchor(configButton, 0.0);
        AnchorPane.setRightAnchor(windowHeader, 0.0);

        editPane.visibleProperty().bindBidirectional(configButton.visibleProperty());


        windowHeader.getChildren().addAll(configButton, deleteButton);
        windowHeader.setBackground(new Background(new BackgroundFill(Color.GREY, new CornerRadii(0), new Insets(0, 0, 0, 0))));
//        windowHeader.setOpacity(0.5);
        editPane.setBackground(new Background(new BackgroundFill(Color.GREY, new CornerRadii(0), new Insets(0, 0, 0, 0))));
        editPane.setOpacity(0.8);
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
        background.setFill(dashBoard.getDashBordAnalysis().colorWidgetPlugin.getValue());
//        background.setOpacity(dashBoard.getDashBordAnalysis().opacityWidgetPlugin.getValue());
        config.size.addListener((observable, oldValue, newValue) -> {
            background.setWidth(newValue.getWidth());
            background.setHeight(newValue.getHeight());
        });

        config.backgroundColor.addListener((observable, oldValue, newValue) -> {
//            tile.setBackgroundColor(newValue);
            background.setFill(newValue);
        });


        this.getChildren().add(background);
    }

    public abstract ImageView getImagePreview();

    public DashBordAnalysis getAnalysis() {
        return dashBoard.getDashBordAnalysis();
    }

    public void setDashBoard(DashBoardPane parent) {
        this.dashBoard = parent;

        config.position.addListener((observable, oldValue, newValue) -> {
            setLayoutX(newValue.getxPos());
            setLayoutY(newValue.getyPos());
        });
        setLayoutX(config.position.get().getxPos());
        setLayoutY(config.position.get().getyPos());

        makeWindowForm();
        getChildren().add(contentRoot);
        makeDragDropOverlay();
        MouseControlUtil.makeDraggable(this);

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

    public void setConfig(WidgetConfig config) {
        this.config = config;
    }

    /**
     *
     *
     * @param data
     */

    /**
     * This function will be called if a new Value arrives
     *
     * @param data       latest data, can be the same as the last update
     * @param hasNewData is true if the data changed since the last update
     */
    public abstract void update(WidgetData data, boolean hasNewData);

    /**
     * Init will be called ones if the the widget will be created
     */
    public abstract void init();


    /**
     * Unique ID of this Widget
     */
    public abstract String typeID();


}
