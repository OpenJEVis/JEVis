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
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GraphExportImage {
    private static final Logger logger = LogManager.getLogger(GraphExportImage.class);
    private final AnalysisDataModel model;
    private File destinationFile;
    private DateTime minDate = null;
    private DateTime maxDate = null;
    private String formatName;

    public GraphExportImage(AnalysisDataModel model) {
        this.model = model;
        this.setDates();

        String formattedName = model.getCurrentAnalysis().getName().replaceAll(" ", "_");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Image File Destination");
        DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");

        if (JEConfig.getLastPath() != null) {
            File file = JEConfig.getLastPath();
            if (file.exists() && file.canRead()) {
                fileChooser.setInitialDirectory(file);
            }
        }

        fileChooser.setInitialFileName(formattedName + I18n.getInstance().getString("plugin.graph.dialog.export.from")
                + fmtDate.print(minDate) + I18n.getInstance().getString("plugin.graph.dialog.export.to")
                + fmtDate.print(maxDate) + "_" + fmtDate.print(new DateTime()));

        FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("Portable Network Graphics Files (*.png)", ".png");
        FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("Joint Photographic Experts Group Files (*.jpg)", ".jpg");
        fileChooser.getExtensionFilters().addAll(pngFilter, jpgFilter);
        fileChooser.setSelectedExtensionFilter(pngFilter);

        File file = fileChooser.showSaveDialog(JEConfig.getStage());
        if (file != null) {
            String fileExtension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0);
            destinationFile = new File(file + fileExtension);
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
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), ".png", destinationFile);
            } catch (IOException e) {
                logger.error("Error: could not export to file.", e);
            }
        });
    }

    private void setDates() {
        for (ChartDataModel mdl : model.getSelectedData()) {
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
