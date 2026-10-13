package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.tool.SvgImages;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class ImageConfig {

    private static final Logger logger = LogManager.getLogger(ImageConfig.class);

    private final String JSON_IMAGE_ID = "objectID";
    private Long objectID = -1L;

    final DashboardControl dashboardControl;

    public ImageConfig(DashboardControl control) {
        this(control, null);
    }

    public Long getObjectID() {
        return objectID;
    }

    public ImageConfig(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;

        if (jsonNode != null) {
            objectID = jsonNode.get(JSON_IMAGE_ID).asLong(-1L);
        }
    }


    private class ImageTab extends Tab implements ConfigTab {
        ImageConfig limit;

        public ImageTab(String text, ImageConfig limit) {
            super(text);
            this.limit = limit;
        }

        @Override
        public void commitChanges() {
            //TODO;
        }
    }

    public void setObjectID(Long objectID) {
        this.objectID = objectID;
    }

    private static Label errorLabel(Throwable t) {
        Label label = new Label(SvgImages.describeError(t));
        label.setWrapText(true);
        label.setMaxWidth(400);
        label.setStyle("-fx-text-fill: firebrick;");
        return label;
    }

    public Tab getConfigTab() {
        ImageTab tab = new ImageTab(I18n.getInstance().getString("plugin.dashboard.imagewidget.tab")
                , this);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(8);


        Label limitTypeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.imagewidget.file"));
        Label fileSizeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.imagewidget.filesize"));
        Label fileSizeField = new Label();

        Button fileButton = new Button();
        fileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter pictures = new FileChooser.ExtensionFilter("Pictures", "*.png", "*.gif", "*.jpg", "*.jpeg", "*.bmp", "*.svg");
            fileChooser.getExtensionFilters().add(pictures);
            fileChooser.setSelectedExtensionFilter(pictures);
            File newBackground = fileChooser.showOpenDialog(JEConfig.getStage());
            if (newBackground != null) {
                try {
                    //BufferedImage bufferedImage = ImageIO.read(newBackground);
                    //javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

                    JEVisClass fileClass = dashboardControl.getDataSource().getJEVisClass("File");
                    JEVisObject fileObject = getFileObject();

                    if (fileObject == null) {
                        /* no image yet, or the stored one was deleted: create a new file object */
                        fileObject = dashboardControl.getActiveDashboard().getDashboardObject().buildObject(FilenameUtils.removeExtension(newBackground.getName()), fileClass);
                        fileObject.commit();
                        objectID = fileObject.getID();
                    }


                    JEVisFile jeVisFile = new JEVisFileImp(newBackground.getName(), newBackground);
                    JEVisSample jeVisSample = fileObject.getAttribute("File").buildSample(DateTime.now(), jeVisFile);
                    jeVisSample.commit();

                    fileSizeField.setText(jeVisFile.getBytes().length + " bytes");

                    fileButton.setGraphic(createPreview(jeVisFile));

                } catch (Exception e) {
                    logger.error("Failed to upload image file {}", newBackground, e);
                    fileButton.setGraphic(errorLabel(e));
                }

            }

        });

        JEVisFile currentFile = getCurrentFile();
        if (currentFile != null) {
            fileButton.setGraphic(createPreview(currentFile));
            fileSizeField.setText(currentFile.getBytes().length + " bytes");
        } else {
            fileButton.setGraphic(new Label(I18n.getInstance().getString("plugin.dashboard.imagewidget.filenew")));
        }

        gridPane.addRow(0, fileSizeLabel, fileSizeField);
        gridPane.addRow(1, limitTypeLabel, fileButton);
        //gridPane.add(new Separator(Orientation.HORIZONTAL_TOP_LEFT), 0, 2, 2, 1);
        //gridPane.add(editorPane, 0, 3, 2, 1);


        tab.setContent(gridPane);
        return tab;
    }

    /**
     * @return the configured file object, or {@code null} if none is set or it does not exist anymore
     * (deleted, or not readable for the current user)
     */
    public JEVisObject getFileObject() {
        if (objectID == null || objectID < 1) {
            return null;
        }
        try {
            JEVisObject object = dashboardControl.getDataSource().getObject(objectID);
            if (object == null || object.getAttribute("File") == null) {
                logger.warn("Image object {} of the image widget does not exist", objectID);
                return null;
            }
            return object;
        } catch (Exception ex) {
            logger.warn("Could not load image object {}", objectID, ex);
            return null;
        }
    }

    /**
     * @return the latest image file, or {@code null} if there is none
     */
    public JEVisFile getCurrentFile() {
        JEVisObject object = getFileObject();
        if (object == null) {
            return null;
        }
        try {
            JEVisSample sample = object.getAttribute("File").getLatestSample();
            return sample != null ? sample.getValueAsFile() : null;
        } catch (Exception ex) {
            logger.warn("Could not load image file of object {}", objectID, ex);
            return null;
        }
    }

    /**
     * @return the preview of the image, or a label with the reason if the image can not be shown
     */
    private Node createPreview(JEVisFile file) {
        try {
            Image image;
            if (SvgImages.isSvg(file.getFilename(), file.getBytes())) {
                image = SvgImages.render(file.getBytes(), 200, 200);
            } else {
                InputStream in = new ByteArrayInputStream(file.getBytes());
                image = new Image(in);
                if (image.isError()) {
                    throw new IllegalArgumentException("Unsupported image format: " + file.getFilename(), image.getException());
                }
            }
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(200);
            return imageView;
        } catch (Throwable t) {
            logger.error("Failed to show image {}", file.getFilename(), t);
            return errorLabel(t);
        }
    }

    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
        dataNode.put(JSON_IMAGE_ID, objectID.toString());

        return dataNode;
    }


}