package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.TreeHelper;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.ImageConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.tool.SvgImages;
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
    private final BorderPane rootPane = new BorderPane();
    private ImageConfig imageConfig;
    public static String IMAGE_NODE_NAME = "image";
    private ImageView imageView;
    private final PauseTransition svgResizeDelay = new PauseTransition(Duration.millis(300));
    private volatile byte[] svgBytes;


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

                JEVisFile imageFile = this.imageConfig.getCurrentFile();
                if (imageFile != null) {
                    if (SvgImages.isSvg(imageFile.getFilename(), imageFile.getBytes())) {
                        svgBytes = imageFile.getBytes();
                        renderSvg();
                    } else {
                        svgBytes = null;
                        InputStream in = new ByteArrayInputStream(imageFile.getBytes());
                        imageView.setImage(new Image(in));
                    }
                }
                updateImage();
            } catch (Exception ex) {
                logger.error("Failed to load image of widget {}", this.config.getTitle(), ex);
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

        /* vector images are rendered at the widget size, render again when the size changes */
        svgResizeDelay.setOnFinished(event -> renderSvg());
        rootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (svgBytes != null) svgResizeDelay.playFromStart();
        });
        rootPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (svgBytes != null) svgResizeDelay.playFromStart();
        });


        try {
            this.imageConfig = new ImageConfig(this.control, this.config.getConfigNode(IMAGE_NODE_NAME));
        } catch (Exception ex) {
            logger.error("Failed to load image widget config node", ex);
        }

    }

    public void restImageConfig() {
        try {
            JEVisObject originalIconObj = getDataSource().getObject(this.imageConfig.getObjectID());
            TreeHelper.copyObjectUnder(
                    originalIconObj,
                    originalIconObj.getParents().get(0),
                    "Copy_" + originalIconObj.getName(),
                    true,
                    true,
                    false);

            JEVisObject copyObject = Iterables.getLast(originalIconObj.getParents().get(0).getChildren());
            if (copyObject.getJEVisClassName().equals("File")) {
                this.imageConfig.setObjectID(copyObject.getID());
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
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


    /**
     * Renders the SVG in the background at the current widget size.
     */
    private void renderSvg() {
        final byte[] svg = svgBytes;
        final double width = rootPane.getWidth();
        final double height = rootPane.getHeight();
        if (svg == null) {
            return;
        }

        Thread renderThread = new Thread(() -> {
            try {
                Image image = SvgImages.render(svg, width, height);
                Platform.runLater(() -> {
                    if (svg == svgBytes) {
                        imageView.setImage(image);
                        rootPane.setCenter(imageView);
                    }
                });
            } catch (Throwable t) {
                logger.error("Failed to render SVG image", t);
                Platform.runLater(() -> {
                    if (svg == svgBytes) {
                        Label error = new Label(SvgImages.describeError(t));
                        error.setWrapText(true);
                        error.setStyle("-fx-text-fill: firebrick;");
                        rootPane.setCenter(error);
                    }
                });
            }
        }, "ImageWidget-SVG");
        renderThread.setDaemon(true);
        renderThread.start();
    }

    private void updateImage() {
        Platform.runLater(() -> {
            if (imageConfig != null) {
                rootPane.getChildren().setAll(imageView);
            }
        });

    }


}
