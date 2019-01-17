package org.jevis.jeconfig.plugin.graph.view;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GraphExportImage {
    private static final Logger logger = LogManager.getLogger(GraphExportImage.class);
    private final GraphDataModel model;
    private File destinationFile;
    private DateTime minDate = null;
    private DateTime maxDate = null;
    private String formatName;

    public GraphExportImage(GraphDataModel model) {
        this.model = model;
        this.setDates();

        String formattedName = model.getCurrentAnalysis().getName().replaceAll(" ", "_");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CSV File Destination");
        DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");
        fileChooser.setInitialFileName(formattedName + I18n.getInstance().getString("plugin.graph.dialog.export.from")
                + fmtDate.print(minDate) + I18n.getInstance().getString("plugin.graph.dialog.export.to")
                + fmtDate.print(maxDate) + "_" + fmtDate.print(new DateTime()) + ".png");

        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showSaveDialog(JEConfig.getStage());
        if (file != null) {
            String fileName = file.getName();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, file.getName().length());
            destinationFile = file;
            formatName = fileExtension;
        }
    }

    public void export(BorderPane border) throws IOException {

        WritableImage image = border.snapshot(new SnapshotParameters(), null);

        ImageIO.write(SwingFXUtils.fromFXImage(image, null), formatName, destinationFile);
    }

    private void setDates() {
        for (ChartDataModel mdl : model.getSelectedData()) {
            DateTime startNow = mdl.getSelectedStart();
            DateTime endNow = mdl.getSelectedEnd();
            if (minDate == null || startNow.isBefore(minDate)) minDate = startNow;
            if (maxDate == null || endNow.isAfter(maxDate)) maxDate = endNow;
        }
    }
}
