package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jfxtras.labs.util.event.MouseControlUtil;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.DashBoardPane;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordAnalysis;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;

public abstract class Widget extends Group {

    public WidgetConfig config;
    public CornerRadii cornerRadii = new CornerRadii(30);
    private DashBoardPane dashBoard;
    private AnchorPane contentRoot = new AnchorPane();
    private AnchorPane editPane = new AnchorPane();


    public Widget() {
        super();
    }


    private void makeDragDropOverlay() {

        Button configButton = new Button("", JEConfig.getImage("Service Manager.png", 18, 18));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(configButton);

        configButton.setOnAction(event -> {
            config.openConfig();
        });

        AnchorPane.setTopAnchor(configButton, 6.0);
        AnchorPane.setRightAnchor(configButton, 6.0);

        editPane.visibleProperty().bindBidirectional(configButton.visibleProperty());

        editPane.setBackground(new Background(new BackgroundFill(Color.GREY, cornerRadii, new Insets(0, 0, 0, 0))));
        editPane.setOpacity(0.8);
        editPane.setMaxWidth(config.size.getValue().getWidth());
        editPane.setMinWidth(config.size.getValue().getWidth());
        editPane.setPrefWidth(config.size.getValue().getWidth());
        editPane.setMaxHeight(config.size.getValue().getHeight());
        editPane.setMinHeight(config.size.getValue().getHeight());
        editPane.setPrefHeight(config.size.getValue().getHeight());
//        editPane.getChildren().add(dragDropOverlay);
        editPane.getChildren().add(configButton);


        this.getChildren().add(editPane);
    }

    private void makeWindowForm() {
        Rectangle background = new Rectangle();
        background.setX(0);
        background.setY(0);
        background.setWidth(config.size.getValue().getWidth());
        background.setHeight(config.size.getValue().getHeight());
        background.setArcWidth(30);
        background.setArcHeight(30);
        background.setStroke(Color.BLACK);
        background.setFill(dashBoard.getDashBordAnalysis().colorWidgetPlugin.getValue());
//        background.setOpacity(dashBoard.getDashBordAnalysis().opacityWidgetPlugin.getValue());
        config.size.addListener((observable, oldValue, newValue) -> {
            background.setWidth(newValue.getWidth());
            background.setHeight(newValue.getHeight());
        });

        this.getChildren().add(background);
    }

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

        layoutXProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue) && getAnalysis().snapToGridProperty.getValue()) {
                layoutXProperty().setValue(parent.getNextGridX(newValue.longValue()));
            }
        });

        layoutYProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue) && getAnalysis().snapToGridProperty.getValue()) {
                layoutYProperty().setValue(parent.getNextGridY(newValue.longValue()));
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
