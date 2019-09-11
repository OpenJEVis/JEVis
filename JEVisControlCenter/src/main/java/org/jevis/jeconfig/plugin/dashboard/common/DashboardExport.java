package org.jevis.jeconfig.plugin.dashboard.common;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.JEConfig;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Export an Dashboard to an PDF file.
 * NOTE: does not work with the current java8 version we are using, because of an endless loop in the java screenshot function
 */
public class DashboardExport {

    private static final Logger logger = LogManager.getLogger(DashboardExport.class);

    public DashboardExport() {
    }


    public void toPDF(Node node, String name) {
        /** disabled in dependency, takes 5 mb and does not work for now because of ChartFX**/
        try {
            logger.info("start- converting to pdf");

            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
//            Interval interval = dashboardisplayedIntervalProperty.getValue();
//            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
//            String intervalString = fmt.print(interval.getStart()) + "_" + fmt.print(interval.getEnd());
            fileChooser.setInitialFileName(name + ".pdf");

            File file = fileChooser.showSaveDialog(JEConfig.getStage());

            if (file != null) {
                logger.info("target file: {}", file);
                final SnapshotParameters spa = new SnapshotParameters();
                //                final WritableImage image = new WritableImage((int) dashBoardPane.getWidth(), (int) dashBoardPane.getHeight());

                logger.info("Start writing screenshot");
                //                WritableImage wImage = dashBoardPane.snapshot(spa, image);
                WritableImage wImage = node.snapshot(new SnapshotParameters(), null);
                logger.info("Done screenshot");
                ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
                ImageIO.write(SwingFXUtils.fromFXImage(wImage, null), "png", byteOutput);
                logger.info("Convert 1 Done");
//                com.itextpdf.text.Image graph = com.itextpdf.text.Image.getInstance(byteOutput.toByteArray());
                logger.info("Convert 2 Done");
//                Document document = new Document();
//                logger.info("Document start");
//                PdfWriter.getInstance(document, new FileOutputStream(file));
//                document.open();
//                logger.info("doc open");
//                document.add(graph);
//                logger.info("doc screenshot add done");
//                document.close();
//                logger.info("doc done done");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /****/
    }
}
