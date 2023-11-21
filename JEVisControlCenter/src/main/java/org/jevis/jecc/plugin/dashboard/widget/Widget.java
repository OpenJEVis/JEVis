package org.jevis.jecc.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.jevis.jecc.plugin.dashboard.config2.JsonNames;
import org.jevis.jecc.plugin.dashboard.config2.Size;
import org.jevis.jecc.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jecc.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jecc.tool.DragResizeMod;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


public abstract class Widget extends Region {
    private static final Logger logger = LogManager.getLogger(Widget.class);
    public final DashboardControl control;
    private final String TYPE = "type";
    private final org.jevis.api.JEVisDataSource jeVisDataSource;
    private final Size size = new Size(100, 150);
    private final AnchorPane contentRoot = new AnchorPane();
    private final AnchorPane editPane = new AnchorPane();
    private final AnchorPane alertPane = new AnchorPane();
    private final StackPane loadingPane = new StackPane();
    private final BooleanProperty editable = new SimpleBooleanProperty(false);
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final Label label = new Label();
    private final Tooltip tt = new Tooltip("");
    private final DragResizeMod.OnDragResizeEventListener onDragResizeEventListener = DragResizeMod.defaultListener;
    public WidgetPojo config;
    public Size previewSize = new Size(28, 28);
    protected DataModelDataHandler sampleHandler;
    double widthCache;
    double heightCache;

    public Widget(DashboardControl control, WidgetPojo config) {
        super();
        logger.debug("new Widget with config");
        this.control = control;
        this.jeVisDataSource = control.getDataSource();

        if (config != null) {
            this.config = config;
        } else {
            this.config = createDefaultConfig();
        }


        initLayout();
        setCacheHint(CacheHint.QUALITY);
        setCache(true);
    }

    @Override
    protected double computePrefWidth(double height) {
        return widthCache != 0 ? widthCache : super.computePrefWidth(height);
    }

/*
    public Widget() {
        super();
        this.config = new WidgetPojo();
        this.config.setUuid(-1);
        this.control = null;
        this.jeVisDataSource = null;
    }
*/

    @Override
    protected double computePrefHeight(double width) {
        return heightCache != 0 ? heightCache : super.computePrefHeight(width);

    }

    /**
     * creates a new Widget with a default configuration
     **/
    /*
    public Widget(DashboardControl control) {
        super();
        logger.debug("new Widget without config");
        this.control = control;
        this.jeVisDataSource = control.getDataSource();
        this.config = createDefaultConfig();

        initLayout();
        setCacheHint(CacheHint.QUALITY);
        setCache(true);
    }
*/
    public DashboardControl getControl() {
        return this.control;
    }

    private void initLayout() {
        logger.debug("initLayout() {}", config.getTitle());
        makeDragDropOverlay();
        makeWindowForm();
        /** disabled because the animation eats a lot of performance for bigger Dashboards**/
        //makeLoadingOverlay();
        makeAlertOverlay();

        this.getChildren().addAll(
                this.contentRoot,
                this.alertPane,
                this.editPane
                //this.loadingPane
        );
        onDragResizeEventListener.setDashBoardPane(control.getDashboardPane());
        DragResizeMod.makeResizable(this, onDragResizeEventListener);

        this.layoutYProperty().addListener((observable, oldValue, newValue) -> {
            this.config.setyPosition(newValue.doubleValue());
        });

        this.layoutXProperty().addListener((observable, oldValue, newValue) -> {
            this.config.setxPosition(newValue.doubleValue());
        });


        setOnMouseClicked(event -> {
            if ((event.getButton() == MouseButton.PRIMARY) && (event.getClickCount() == 1)) {
                if (event.isShiftDown()) {
                    debug();
                    event.consume();
                }
            }
        });


        updateConfig(this.config);
        logger.debug("initLayout() done {}", config.getTitle());
    }

    abstract public void debug();

    abstract public WidgetPojo createDefaultConfig();


    public void updateConfig(WidgetPojo config) {
        logger.debug("updateConfig: '{}',{},{}", config.getTitle(), config.hashCode(), (config != null));
        this.config = config;

        setNodeSize(this.config.getSize().getWidth(), this.config.getSize().getHeight());
        setBorder(this.config.getBorderSize());
        layoutXProperty().set(this.config.getxPosition());
        layoutYProperty().set(this.config.getyPosition());


        if (this.config.getShowShadow()) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(5.0);
            dropShadow.setOffsetX(3.0);
            dropShadow.setOffsetY(3.0);
            dropShadow.setColor(Color.BLACK);
            this.contentRoot.setEffect(dropShadow);
        } else {
            this.contentRoot.setEffect(null);
        }

        if (!config.getTooltip().equals("")) {
            tt.setText(config.getTooltip());
            Tooltip.install(this, this.tt);
        } else {
            tt.setText("");
            Tooltip.uninstall(this, this.tt);
        }

        try {
            setBackgroundColor(this.config.getBackgroundColor());
            //updateConfig();

        } catch (Exception ex) {
            logger.debug(ex);
        }
    }


    public void setGlow(boolean glow, boolean ishighlightModus) {

        Platform.runLater(() -> {
//            logger.debug("[{}] setGlow: {},{}", getConfig().getUuid(), glow, ishighlightModus);
            this.setEffect(null);

            if (glow) {
                DropShadow borderGlow = new DropShadow();
                borderGlow.setOffsetY(-0f);
                borderGlow.setOffsetX(-0f);
                borderGlow.setColor(Color.BLUE);
                borderGlow.setWidth(30);
                borderGlow.setHeight(30);

                this.setEffect(borderGlow);
            } else if (ishighlightModus) {

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


    public void setEditable(boolean editable) {
//        logger.debug("Widget setEditable {}", editable);
        onDragResizeEventListener.resizeableProperty().setValue(editable);
        this.editPane.setVisible(editable);
        this.editable.setValue(editable);
    }

    public void showAlertOverview(boolean show, String message) {
        Platform.runLater(() -> {
            this.alertPane.setVisible(show);
            Tooltip tooltip = new Tooltip(message);
            label.setTooltip(tooltip);
        });

    }

    private void makeAlertOverlay() {
        this.alertPane.setVisible(false);
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        ImageView alert = ControlCenter.getImage("Warning-icon.png", 16d, 16d);
        label.setGraphic(alert);
        this.alertPane.getChildren().add(label);
        AnchorPane.setLeftAnchor(alert, 8d);
        AnchorPane.setTopAnchor(alert, 8d);
    }

    /**
     * @deprecated
     */
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
        this.editPane.setOpacity(0.0);//0.7


        final ContextMenu contextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Delete", ControlCenter.getImage("if_trash_(delete)_16x16_10030.gif", 18, 18));
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Widget.this.control.removeWidget(Widget.this);
            }
        });
        MenuItem configItem = new MenuItem("Open Configuration", ControlCenter.getImage("Service Manager.png", 18, 18));
        configItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Widget.this.openConfig();
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
            }
        });

        MenuItem layerUPItem = new MenuItem("Layer UP", ControlCenter.getImage("arrow_up.png", 18, 18));
        layerUPItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Widget.this.getConfig().setLayer(Widget.this.getConfig().getLayer() + 1);
                    //debugLayers();
                    control.redrawDashboardPane();
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
            }
        });


        MenuItem layerDownItem = new MenuItem("Layer Down", ControlCenter.getImage("arrow_down.png", 18, 18));
        layerDownItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Widget.this.getConfig().setLayer(Widget.this.getConfig().getLayer() - 1);
                    control.redrawDashboardPane();
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
            }
        });

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(0, 5, 0, 5));
        //Label typeLabel = new Label("Type:");
        Label idLabel = new Label("ID:");
        //gridPane.add(typeLabel, 0, 0);
        gridPane.add(idLabel, 0, 1);
        //gridPane.add(new Label(typeID()), 1, 0);
        gridPane.add(new Label(getConfig().getUuid() + ""), 1, 1);

        CustomMenuItem infoMenuItem = new CustomMenuItem(gridPane);
        infoMenuItem.setHideOnClick(false);
        infoMenuItem.setDisable(true);

        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();


        contextMenu.getItems().addAll(infoMenuItem, separatorMenuItem, configItem, layerUPItem, layerDownItem, new SeparatorMenuItem(), delete);

        //this.editPane.setMouseTransparent(false);
        this.editPane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                try {
                    Widget.this.openConfig();
                    event.consume();
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
            }

            if ((event.getButton() == MouseButton.PRIMARY) && (event.getClickCount() == 1)) {
                if (event.isControlDown()) {
                    ArrayList arrayList = new ArrayList<>();
                    arrayList.add(this);
                    control.addToWidgetSelection(arrayList);
                    event.consume();
                } else if (event.isAltDown()) {
                    logger.debug("Is alt selected: " + this);
                    control.setSelectAllFromType(this);
                    event.consume();
                } else {
                    control.setSelectedWidget(this);
                    event.consume();
                }
            }


            if (event.getButton().equals(MouseButton.SECONDARY)) {
                if (event.isShiftDown()) {
                    /* debug help to show json */
                    try {
                        Alert.AlertType alertAlertType;
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.initModality(Modality.NONE);
                        alert.setResizable(true);
                        alert.setTitle("Widget: " + getConfig().getTitle());

                        ObjectMapper objectMapper = new ObjectMapper();
                        TextArea textArea = new TextArea(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.toNode()));
                        textArea.setPrefColumnCount(40);
                        textArea.setPrefRowCount(20);
                        textArea.setEditable(false);
                        textArea.setWrapText(true);
                        alert.getDialogPane().setContent(textArea);
                        alert.show();
                        event.consume();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                } else {
                    contextMenu.show(this.editPane, event.getScreenX(), event.getScreenY());
                    event.consume();
                }

            }

        });


    }

    private void debugLayers() {
        logger.debug("Layers:");
        control.getWidgets().stream().sorted((o1, o2) -> o1.getConfig().getLayer().compareTo(o2.getConfig().getLayer())).forEach(widget -> {
            logger.debug("L: " + widget.getConfig().getLayer() + "  " + widget.getConfig().getTitle());
        });
    }

    public void setNodeSize(double width, double height) {

        logger.debug("setNodeSize: old w:{}/h:{}  new w:{}/h:{}", this.getWidth(), this.getHeight(), width, height);
        logger.debug("setNodePos : old x:{}/y:{}", this.getLayoutX(), this.getLayoutY());

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

            size.setHeight(height);
            size.setWidth(width);
            this.config.setSize(size);
            /** performance workaround, update y/x if the drag&Drop event moves and resize at the same time **/
            this.config.setxPosition(getLayoutX());
            this.config.setyPosition(getLayoutY());
        });
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

    public void showProgressIndicator(boolean show) {
        Platform.runLater(() -> {
            try {
                if (show) {
                    loadingPane.setVisible(true);
                } else {
                    logger.debug("Hide loading: widget: {}", getConfig().getUuid());
                    loadingPane.setVisible(false);
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        });


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
     * returns true if the content is static and does not need an updateData()
     *
     * @return
     */
    public abstract boolean isStatic();


    /**
     * @return
     */
    public abstract List<DateTime> getMaxTimeStamps();

    /**
     * Init will be called ones if the the widget will be created
     */
    public abstract void init();


    /**
     * Unique ID of this Widget for CSS
     */
    public abstract String typeID();

    public void openConfig() {
    }


    public abstract ObjectNode toNode();

    public ObjectNode createDefaultNode() {
        ObjectMapper mapper = new ObjectMapper();


        ObjectNode dashBoardNode = mapper.createObjectNode();
        dashBoardNode
                .put(JsonNames.Widget.UUID, this.config.getUuid())
                .put(JsonNames.Widget.TYPE, typeID())
                .put(JsonNames.Widget.TITLE, this.config.getTitle())
                .put(JsonNames.Widget.TOOLTIP, this.config.getTooltip())
                .put(JsonNames.Widget.BACKGROUND_COLOR, this.config.getBackgroundColor().toString())
                .put(JsonNames.Widget.FONT_COLOR, this.config.getFontColor().toString())
                .put(JsonNames.Widget.FONT_SIZE, this.config.getFontSize())
                .put(JsonNames.Widget.FONT_WEIGHT, this.config.getFontWeight().toString())
                .put(JsonNames.Widget.FONT_POSTURE, this.config.getFontPosture().toString())
                .put(JsonNames.Widget.FONT_UNDERLINED, this.config.getFontUnderlined())
                .put(JsonNames.Widget.TITLE_POSITION, this.config.getTitlePosition().toString())
                .put(JsonNames.Widget.BORDER_SIZE, this.config.getBorderSize().getTop())
                .put(JsonNames.Widget.SHOW_SHADOW, this.config.getShowShadow())
                .put(JsonNames.Widget.LAYER, this.config.getLayer())
                .put(JsonNames.Widget.WIDTH, this.config.getSize().getWidth())
                .put(JsonNames.Widget.HEIGHT, this.config.getSize().getHeight())
                .put(JsonNames.Widget.DECIMALS, this.config.getDecimals())
                .put(JsonNames.Widget.SHOW_VALUE, this.config.getShowValue())
                .put(JsonNames.Widget.X_POS, this.config.getxPosition())
                .put(JsonNames.Widget.Y_POS, this.config.getyPosition())
                .put(JsonNames.Widget.FIXED_TIMEFRAME, this.config.isFixedTimeframe());


        return dashBoardNode;
    }

    public Tooltip getTt() {
        return tt;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Widget otherObject) {
            if (otherObject.getConfig() != null && this.getConfig() != null) {
                return (otherObject.getConfig().getUuid() == this.getConfig().getUuid());
            }
        }

        return false;
    }

    @Override
    public Widget clone() {
        try {
            logger.debug("Clone Widget: " + config);
            ObjectNode json = this.toNode();
            WidgetPojo newConfig = new WidgetPojo(json);
            Widget newWidget = Widgets.createWidget(typeID(), control, newConfig);
            return newWidget;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public Interval getCurrentInterval(Interval interval) {
        if (sampleHandler != null) {
            sampleHandler.setInterval(interval);
            return new Interval(sampleHandler.getDuration().getStart(), sampleHandler.getDuration().getEnd());
        } else {
            return null;
        }


    }

}
