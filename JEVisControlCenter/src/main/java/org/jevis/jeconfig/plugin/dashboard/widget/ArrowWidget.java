package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
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
    private final Label label = new Label();
    private AnchorPane anchorPane = new AnchorPane();

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
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 1, control.getActiveDashboard().xGridInterval * 4));
        widgetPojo.setShowShadow(false);
        widgetPojo.setBackgroundColor(Color.TRANSPARENT);
        widgetPojo.setFontColorSecondary(Color.BLACK);

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
//                anchorPane.setEffect(null);
//                label.setEffect(null);
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

        anchorPane.heightProperty().addListener(observable -> updateConfig());
        anchorPane.widthProperty().addListener(observable -> updateConfig());

        //Layouts.setAnchor(this.label, 0);


//        anchorPane.setEffect(null);
//        this.setEffect(null);//Workaround

    }


    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(null);

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
//        dashBoardNode
//                .set(JsonNames.Widget.DATA_HANDLER_NODE, this.sampleHandler.toJsonNode());
        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/TitleWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


    private void updateArrow() {
        Platform.runLater(() -> {
            anchorPane.getChildren().setAll(drawArrowPath());
            //anchorPane.getChildren().setAll(drawArrowPath(0, anchorPane.getHeight() / 4, anchorPane.getWidth() - 5, anchorPane.getHeight() / 4, anchorPane.getHeight()));
        });

    }

    private Path drawArrowPath() {
        Path path = new Path();
        path.strokeProperty().bind(path.fillProperty());
        path.setFill(this.config.getFontColorSecondary());

        //x = Width
        //y = Height

        double partY = anchorPane.getHeight() / 4;
        double startX = anchorPane.getWidth();
        double startY = anchorPane.getHeight() / 2;
        double Y2 = startY - partY;
        double x2 = anchorPane.getWidth() - 5;

        path.getElements().add(new MoveTo(startX, startY));
        path.getElements().add(new LineTo(startX, Y2));
        path.getElements().add(new LineTo(0, Y2));
        path.getElements().add(new LineTo(0, startY + partY));
        path.getElements().add(new LineTo(startX, startY + partY));
        path.getElements().add(new MoveTo(startX, startY));


        return path;
    }

    private Path drawArrowPath(double startX, double startY, double endX, double endY, double arrowHeadSize) {
        logger.error("drawArrowPath: {},{},{},{},{}", startX, startY, endX, endY, arrowHeadSize);
        Path path = new Path();

        path.strokeProperty().bind(path.fillProperty());
        //Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
        path.setFill(this.config.getFontColorSecondary());

        //Line
        path.getElements().add(new MoveTo(startX, startY));
        path.getElements().add(new LineTo(endX, endY));

        path.getElements().add(new LineTo(endX + endX, endY));
        path.getElements().add(new LineTo(startX + startX, startY));
        path.getElements().add(new LineTo(startX, startY));

        //ArrowHead
        double angle = Math.atan2((endY - startY), (endX - startX)) - Math.PI / 2.0;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        //point1
        double x1 = (-1.0 / 2.0 * cos + Math.sqrt(3) / 2 * sin) * arrowHeadSize + endX;
        double y1 = (-1.0 / 2.0 * sin - Math.sqrt(3) / 2 * cos) * arrowHeadSize + endY;
        //point2
        double x2 = (1.0 / 2.0 * cos + Math.sqrt(3) / 2 * sin) * arrowHeadSize + endX;
        double y2 = (1.0 / 2.0 * sin - Math.sqrt(3) / 2 * cos) * arrowHeadSize + endY;

        path.getElements().add(new LineTo(x1, y1));
        path.getElements().add(new LineTo(x2, y2));
        path.getElements().add(new LineTo(endX, endY));
        return path;
    }

}
