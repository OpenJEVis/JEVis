package org.jevis.jeconfig.plugin.charts;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class GraphExportImage {
    private static final Logger logger = LogManager.getLogger(GraphExportImage.class);
    private final AnalysisDataModel model;
    private File destinationFile;
    private DateTime minDate = null;
    private DateTime maxDate = null;
    private String formatName;
    private final FileChooser fileChooser;

    public GraphExportImage(AnalysisDataModel model) {
        this.model = model;
        this.setDates();

        String formattedName = model.getCurrentAnalysis().getName().replaceAll(" ", "_");
        fileChooser = new FileChooser();
        fileChooser.setTitle("Image File Destination");
        DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");

        if (JEConfig.getLastPath() != null) {
            File file = JEConfig.getLastPath();
            if (file.exists() && file.canRead()) {
                fileChooser.setInitialDirectory(file);
            }
        }

        fileChooser.setInitialFileName(
                formattedName + "_"
                        + I18n.getInstance().getString("plugin.graph.dialog.export.from") + "_"
                        + fmtDate.print(minDate) + "_" + I18n.getInstance().getString("plugin.graph.dialog.export.to") + "_"
                        + fmtDate.print(maxDate) + "_" + I18n.getInstance().getString("plugin.graph.dialog.export.created") + "_"
                        + fmtDate.print(new DateTime()));

        FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("Portable Network Graphics Files (*.png)", ".png");
        FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("Joint Photographic Experts Group Files (*.jpg)", ".jpg");
        fileChooser.getExtensionFilters().addAll(pngFilter, jpgFilter);
        fileChooser.setSelectedExtensionFilter(pngFilter);

        File file = fileChooser.showSaveDialog(JEConfig.getStage());
        if (file != null) {
            String fileExtension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0);
            if (!file.getAbsolutePath().contains(fileExtension)) {
                destinationFile = new File(file + fileExtension);
            } else {
                destinationFile = file;
            }
            formatName = fileExtension;
            JEConfig.setLastPath(file);
        }
    }

    public void export(VBox vBox) {

        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setTransform(new Scale(2, 2));
        snapshotParameters.setFill(Color.WHITESMOKE);

        Platform.runLater(() -> {
            WritableImage image = vBox.snapshot(snapshotParameters, null);

            try {
                String extension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0).substring(1);

                if (extension.equals("jpg")) {
                    PixelGrabber pg = new PixelGrabber(SwingFXUtils.fromFXImage(image, null), 0, 0, -1, -1, true);
                    pg.grabPixels();
                    int width = pg.getWidth(), height = pg.getHeight();

                    int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
                    ColorModel RGB_OPAQUE = new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);

                    DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
                    WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
                    BufferedImage bi = new BufferedImage(RGB_OPAQUE, raster, false, null);

                    ImageIO.write(bi, extension, destinationFile);
                } else {

                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), extension, destinationFile);
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Error: could not export to file.", e);
            }

            Platform.runLater(() -> {
                JEConfig.getStage().setMaximized(true);
//                double height = JEConfig.getStage().getHeight();
//                double width = JEConfig.getStage().getWidth();
//                JEConfig.getStage().setWidth(0);
//                JEConfig.getStage().setHeight(0);
//                JEConfig.getStage().setHeight(height);
//                JEConfig.getStage().setWidth(width);
            });
        });
    }

    private void setDates() {
        for (ChartDataRow mdl : model.getSelectedData()) {
            DateTime startNow = mdl.getSelectedStart();
            DateTime endNow = mdl.getSelectedEnd();
            if (minDate == null || startNow.isBefore(minDate)) minDate = startNow;
            if (maxDate == null || endNow.isAfter(maxDate)) maxDate = endNow;
        }
    }

    public File getDestinationFile() {
        return destinationFile;
    }
}
