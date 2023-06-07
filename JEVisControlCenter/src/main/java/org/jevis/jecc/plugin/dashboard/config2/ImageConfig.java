package org.jevis.jecc.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.geometry.Insets;
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
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class ImageConfig {

    private static final Logger logger = LogManager.getLogger(ImageConfig.class);
    final DashboardControl dashboardControl;
    private final String JSON_IMAGE_ID = "objectID";
    private Long objectID = -1l;

    public ImageConfig(DashboardControl control) {
        this(control, null);
    }

    public ImageConfig(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;

        if (jsonNode != null) {
            objectID = jsonNode.get(JSON_IMAGE_ID).asLong(-1l);
        }
    }

    public Long getObjectID() {
        return objectID;
    }

    public void setObjectID(Long objectID) {
        this.objectID = objectID;
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
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Pictures", "*.png", "*.gif", "*.jpg", "*.bmp"));
            File newBackground = fileChooser.showOpenDialog(ControlCenter.getStage());
            if (newBackground != null) {
                try {
                    //BufferedImage bufferedImage = ImageIO.read(newBackground);
                    //javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

                    JEVisClass fileClass = dashboardControl.getDataSource().getJEVisClass("File");
                    JEVisObject fileObject;

                    if (objectID >= 1) {
                        fileObject = dashboardControl.getDataSource().getObject(objectID);
                    } else {
                        fileObject = dashboardControl.getActiveDashboard().getDashboardObject().buildObject(FilenameUtils.removeExtension(newBackground.getName()), fileClass);
                        fileObject.commit();
                        objectID = fileObject.getID();
                    }


                    JEVisFile jeVisFile = new JEVisFileImp(newBackground.getName(), newBackground);
                    JEVisSample jeVisSample = fileObject.getAttribute("File").buildSample(DateTime.now(), jeVisFile);
                    jeVisSample.commit();

                    fileSizeField.setText(jeVisFile.getBytes().length + " bytes");

                    InputStream in = new ByteArrayInputStream(jeVisFile.getBytes());
                    ImageView imageView = new ImageView(new Image(in));
                    imageView.setPreserveRatio(true);
                    imageView.setFitHeight(200);
                    fileButton.setGraphic(imageView);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });

        if (objectID > 1) {
            try {
                JEVisAttribute imageAttribute = dashboardControl.getDataSource().getObject(objectID).getAttribute("File");
                InputStream in = new ByteArrayInputStream(imageAttribute.getLatestSample().getValueAsFile().getBytes());
                ImageView imageView = new ImageView(new Image(in));
                imageView.setPreserveRatio(true);
                imageView.setFitHeight(200);
                fileButton.setGraphic(imageView);
                fileSizeField.setText(imageAttribute.getLatestSample().getValueAsFile().getBytes().length + " bytes");
            } catch (Exception ex) {
                logger.error(ex, ex);
            }
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

    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
        dataNode.put(JSON_IMAGE_ID, objectID.toString());

        return dataNode;
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


}