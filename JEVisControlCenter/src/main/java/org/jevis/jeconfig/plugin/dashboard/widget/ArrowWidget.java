package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.ArrowConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ArrowWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(ArrowWidget.class);
    public static String WIDGET_ID = "Arrow";
    private AnchorPane anchorPane = new AnchorPane();
    private ArrowConfig arrowConfig;
    public static String ARROW_NODE_NAME = "arrow";

    public enum ARROW_ORIENTATION {
        LEFT_RIGHT,
        RIGHT_LEFT,
        TOP_BOTTOM,
        BOTTOM_TOP
    }

    public enum SHAPE {
        ARROW,
        LINE,
    }


    public ArrowWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        this.setId(WIDGET_ID + UUID.randomUUID());
    }

    @Override
    public void debug() {

    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.titlewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 1, control.getActiveDashboard().xGridInterval * 5));
        widgetPojo.setShowShadow(true);
        widgetPojo.setBackgroundColor(Color.TRANSPARENT);
        widgetPojo.setFontColorSecondary(Color.BLACK);
        widgetPojo.setBorderSize(BorderWidths.EMPTY);

        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {
        logger.error("UpdateConfig");
        Platform.runLater(() -> {
            try {

                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                anchorPane.setBackground(bgColor);
                updateArrow();
                /**
                 this.label.setBackground(bgColor);
                 this.label.setTextFill(this.config.getFontColor());
                 this.label.setText(this.config.getTitle());
                 this.label.setFont(new Font(this.config.getFontSize()));
                 this.label.setPrefWidth(this.config.getSize().getWidth());
                 this.label.setAlignment(this.config.getTitlePosition());

                 anchorPane.getChildren().setAll(drawArrowPath(0, 20, 50, 20, 5));
                 **/
            } catch (Exception ex) {
                logger.error(ex);
            }
        });
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public List<DateTime> getMaxTimeStamps() {
        return new ArrayList<>();
    }


    @Override
    public void init() {
        //this.label.setPadding(new Insets(0, 8, 0, 8));
        anchorPane.setBackground(null);
        setGraphic(anchorPane);


        try {
            this.arrowConfig = new ArrowConfig(this.control, this.config.getConfigNode(ARROW_NODE_NAME));
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        if (arrowConfig == null) {
            logger.error("Limit is null make new: " + config.getUuid());
            this.arrowConfig = new ArrowConfig(this.control);
        }
        anchorPane.heightProperty().addListener(observable -> updateConfig());
        anchorPane.widthProperty().addListener(observable -> updateConfig());

    }


    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(null);

        if (arrowConfig != null) {
            widgetConfigDialog.addTab(arrowConfig.getConfigTab());
        }

        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);

            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ObjectNode toNode() {

        ObjectNode dashBoardNode = super.createDefaultNode();
        if (arrowConfig != null) {
            dashBoardNode.set(ARROW_NODE_NAME, arrowConfig.toJSON());
        }

        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ArrowWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


    private void updateArrow() {
        Platform.runLater(() -> {
            if (arrowConfig != null) {
                anchorPane.getChildren().setAll(drawArrowPath(anchorPane.getWidth(), anchorPane.getHeight(), arrowConfig.getOrientation(), arrowConfig.getShape()));
            }
        });

    }


    private Pane drawArrowPath(double parentWidth, double parentHeight, ARROW_ORIENTATION orientation, SHAPE shape) {
        if (orientation.equals(ARROW_ORIENTATION.BOTTOM_TOP) || orientation.equals(ARROW_ORIENTATION.TOP_BOTTOM)) {

            double orgParentHeight = parentHeight;
            double orgParentWidth = parentWidth;
            parentWidth = orgParentHeight;
            parentHeight = orgParentWidth;
        }

        double arrowSize = shape == SHAPE.ARROW ? parentHeight : 0;


        double yQuarter = parentHeight / 4;
        double xStart = 0;
        double yStart = yQuarter;
        double xWidth = parentWidth + 1 - arrowSize;//-1 to remove gap
        double yHeight = yQuarter * 2;
        double arrowPeak = parentHeight / 2;

        Rectangle rectangle = new Rectangle(xStart, yStart, xWidth, yHeight);
        rectangle.setFill(this.config.getFontColor());


        Pane arrow = new Pane();

        switch (shape) {
            case LINE:
                arrow.getChildren().addAll(rectangle);
                break;
            case ARROW:
                Polygon polygon = new Polygon();
                polygon.strokeProperty().bind(polygon.fillProperty());
                polygon.setFill(this.config.getFontColor());
                polygon.getPoints().addAll(new Double[]{
                        parentWidth, arrowPeak,
                        parentWidth - arrowSize, parentHeight,
                        parentWidth - arrowSize, 0d});
                arrow.getChildren().addAll(rectangle, polygon);
        }

        //arrow.getChildren().addAll(rectangle, polygon);

        switch (orientation) {
            case BOTTOM_TOP:
                arrow.getTransforms().add(new Rotate(-90));
                arrow.setTranslateX(0);
                arrow.setTranslateY(parentWidth);
                break;
            case LEFT_RIGHT:
                //Default
                break;
            case RIGHT_LEFT:
                arrow.getTransforms().add(new Rotate(180));
                arrow.setTranslateX(parentWidth);
                arrow.setTranslateY(parentHeight);
                break;
            case TOP_BOTTOM:
                arrow.getTransforms().add(new Rotate(90));
                arrow.setTranslateX(parentHeight);
                break;
        }


        return arrow;
    }

}
