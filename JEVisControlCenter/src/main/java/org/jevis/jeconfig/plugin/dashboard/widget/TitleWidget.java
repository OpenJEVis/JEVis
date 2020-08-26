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
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TitleWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(TitleWidget.class);
    public static String WIDGET_ID = "Title";
    private final Label label = new Label();
    private AnchorPane anchorPane = new AnchorPane();

    public TitleWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        this.setId(WIDGET_ID+ UUID.randomUUID());
    }

    @Override
    public void debug() {

    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.titlewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval*1,control.getActiveDashboard().xGridInterval*4));

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
        logger.debug("UpdateConfig");
        Platform.runLater(() -> {
            try {

                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                this.label.setBackground(bgColor);
                this.label.setTextFill(this.config.getFontColor());
                this.label.setText(this.config.getTitle());
                this.label.setFont(new Font(this.config.getFontSize()));
                this.label.setPrefWidth(this.config.getSize().getWidth());
                this.label.setAlignment(this.config.getTitlePosition());
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
        this.label.setPadding(new Insets(0, 8, 0, 8));
        anchorPane.setBackground(null);
        anchorPane.getChildren().add(this.label);
        Layouts.setAnchor(this.label, 0);

        setGraphic(anchorPane);
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
}
