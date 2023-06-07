package org.jevis.jecc.plugin.dashboard.common;

import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.FileNames;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Export an Dashboard to an PDF file.
 * NOTE: does not work with the current java8 version we are using, because of an endless loop in the java screenshot function
 */
public class DashboardExport {

    private static final Logger logger = LogManager.getLogger(DashboardExport.class);

    public DashboardExport() {
    }


    public void toPDF(DashboardControl control, String fileName) {
        try {
            String title = control.getActiveDashboard().getTitle();
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", ".pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialFileName(FileNames.fixName(fileName) + ".pdf");
            Document document = new Document();
            File file = fileChooser.showSaveDialog(ControlCenter.getStage());
            if (file != null) {
                OutputStream fileStream = new FileOutputStream(file);
                PdfWriter.getInstance(document, fileStream);


                document.setPageSize(PageSize.A4.rotate());
                document.addAuthor("JEVis");
                document.addTitle(control.getActiveDashboard().getTitle());
                document.addCreationDate();
                document.open();
//berichtsperiode

                WritableImage image = control.getDashboardPane().snapshot(new SnapshotParameters(), null);
                String tmpFileName = UUID.randomUUID().toString();
                Path temp = Files.createTempFile(tmpFileName, ".png");
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", temp.toFile());
                Image img = Image.getInstance(temp.toFile().toURI().toURL());

                PdfPTable table = new PdfPTable(1);
                table.setWidthPercentage(100);


                Font CUSTOM_FONT_HEADER = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL | Font.UNDERLINE);
                Font CUSTOM_FONT_TEXT = new Font(Font.FontFamily.HELVETICA, 8);

                Paragraph headerParagraph = new Paragraph(title, CUSTOM_FONT_HEADER);
                headerParagraph.setAlignment(Paragraph.ALIGN_LEFT);
                //headerParagraph.setFont(CUSTOM_FONT_HEADER);

                PdfPCell cellTitle = new PdfPCell(headerParagraph);
                cellTitle.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellImage = new PdfPCell(img, true);
                cellImage.setBorder(Rectangle.NO_BORDER);
                cellImage.setPadding(5);


                DateTimeFormatter dTF = DateTimeFormat.forPattern("yyyy.MM.dd HH:mm");
                /*
                String footer = String.format("%s: %s, %s: %s %s %s",
                        I18n.getInstance().getString("dashboard.pdf.creatdate"),
                        dTF.print(new DateTime()),
                        I18n.getInstance().getString("dashboard.pdf.duration"),
                        dTF.print(control.getInterval().getStart()),
                        I18n.getInstance().getString("dashboard.pdf.until"),
                        dTF.print(control.getInterval().getEnd())
                );
                */

                String footer = String.format("%s: %s, %s: %s %s",
                        I18n.getInstance().getString("dashboard.pdf.createdate"),
                        dTF.print(new DateTime()),
                        I18n.getInstance().getString("dashboard.pdf.duration"),
                        control.getActiveTimeFrame().getListName(),
                        control.getActiveTimeFrame().format(control.getInterval())

                );


                //controller.getActiveTimeFrame().format(controller.getInterval())
                //controller.getActiveTimeFrame().getListName
                Paragraph datePara = new Paragraph(footer, CUSTOM_FONT_TEXT);
                //datePara.setFont(CUSTOM_FONT_TEXT);
                datePara.setAlignment(Paragraph.ALIGN_LEFT);
                PdfPCell cellFooter = new PdfPCell(datePara);
                cellFooter.setBorder(Rectangle.NO_BORDER);

                table.addCell(cellTitle);
                table.addCell(cellImage);
                table.addCell(cellFooter);

                document.add(table);
                document.close();
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void toPNG(Node node, String name) {
        /** disabled in dependency, takes 5 mb and does not work for now because of ChartFX**/
        try {
            logger.info("start- converting to pdf");

            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(extFilter);
//            Interval interval = dashboardisplayedIntervalProperty.getValue();
//            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
//            String intervalString = fmt.print(interval.getStart()) + "_" + fmt.print(interval.getEnd());
            fileChooser.setInitialFileName(name + ".png");

            File file = fileChooser.showSaveDialog(ControlCenter.getStage());

            if (file != null) {
                logger.info("target file: {}", file);
                WritableImage image = node.snapshot(new SnapshotParameters(), null);

                logger.info("Start writing screenshot");
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
//                    Desktop.getDesktop().open(file);

                } catch (Exception e) {
                    // TODO: handle exception here
                }

//                //                WritableImage wImage = dashBoardPane.snapshot(spa, image);
//                WritableImage wImage = node.snapshot(new SnapshotParameters(), null);
//                logger.info("Done screenshot");
//                ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
//                ImageIO.write(SwingFXUtils.fromFXImage(wImage, null), "png", byteOutput);
//                logger.info("Convert 1 Done");
////                com.itextpdf.text.Image graph = com.itextpdf.text.Image.getInstance(byteOutput.toByteArray());
//                logger.info("Convert 2 Done");
////                Document document = new Document();
////                logger.info("Document start");
////                PdfWriter.getInstance(document, new FileOutputStream(file));
////                document.open();
////                logger.info("doc open");
////                document.add(graph);
////                logger.info("doc screenshot add done");
////                document.close();
////                logger.info("doc done done");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /****/
    }
}
