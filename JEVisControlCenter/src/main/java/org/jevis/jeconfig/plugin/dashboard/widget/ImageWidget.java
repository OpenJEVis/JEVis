package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.ImageConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ImageWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(ImageWidget.class);
    public static String WIDGET_ID = "Image";
    private BorderPane rootPane = new BorderPane();
    private ImageConfig imageConfig;
    public static String IMAGE_NODE_NAME = "image";
    private ImageView imageView;
    private JEVisFile imageFile;


    public ImageWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        this.setId(WIDGET_ID + UUID.randomUUID());
    }

    @Override
    public void debug() {

    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.imagewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 5, control.getActiveDashboard().xGridInterval * 5));
        widgetPojo.setShowShadow(true);
        widgetPojo.setBackgroundColor(Color.WHITESMOKE);
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
        logger.debug("UpdateConfig");
        Platform.runLater(() -> {
            try {

                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                rootPane.setBackground(bgColor);

                if (this.imageConfig.getObjectID() > 1) {
                    JEVisAttribute imageAttribute = getDataSource().getObject(this.imageConfig.getObjectID()).getAttribute("File");

                    if (imageAttribute != null) {
                        JEVisSample imageSample = imageAttribute.getLatestSample();

                        JEVisFile imageFile = imageSample.getValueAsFile();
                        InputStream in = new ByteArrayInputStream(imageFile.getBytes());
                        imageView.setImage(new Image(in));
                    }

                }
                updateImage();
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
        rootPane.setBackground(null);
        imageView = new ImageView();
        imageView.setPreserveRatio(false);
        rootPane.setCenter(imageView);
        imageView.fitWidthProperty().bind(rootPane.widthProperty());
        imageView.fitHeightProperty().bind(rootPane.heightProperty());
        setGraphic(rootPane);


        try {
            this.imageConfig = new ImageConfig(this.control, this.config.getConfigNode(IMAGE_NODE_NAME));
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }

    }


    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(null);

        if (imageConfig != null) {
            widgetConfigDialog.addTab(imageConfig.getConfigTab());
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
        if (imageConfig != null) {
            dashBoardNode.set(IMAGE_NODE_NAME, imageConfig.toJSON());
        }

        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ImageWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


    private void updateImage() {
        Platform.runLater(() -> {
            if (imageConfig != null) {
                rootPane.getChildren().setAll(imageView);
            }
        });

    }


}
