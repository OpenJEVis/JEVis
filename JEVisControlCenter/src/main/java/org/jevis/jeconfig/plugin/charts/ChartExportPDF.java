package org.jevis.jeconfig.plugin.charts;

import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.FileNames;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.application.Chart.data.DataModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ChartExportPDF {
    private static final Logger logger = LogManager.getLogger(ChartExportPDF.class);
    private final DataModel model;
    private final boolean directPrint;
    private final String formattedName;
    private File destinationFile;
    private DateTime minDate = null;
    private DateTime maxDate = null;
    private FileChooser fileChooser;

    public ChartExportPDF(DataModel model, String analysisName, boolean directPrint) {
        this.model = model;
        this.directPrint = directPrint;
        this.formattedName = FileNames.fixName(analysisName);
        this.setDates();
        if (!directPrint) {
            fileChooser = new FileChooser();
            fileChooser.setTitle("PDF File Destination");
            DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");

            if (JEConfig.getLastPath() != null) {
                File file = JEConfig.getLastPath();

                try {
                    if (file.exists() && file.canRead()) {
                        file.getCanonicalPath();
                        fileChooser.setInitialDirectory(file);
                    }
                } catch (IOException e) {
                    logger.error("Error while accessing last path", e);
                }
            }

            fileChooser.setInitialFileName(
                    formattedName + "_"
                            + I18n.getInstance().getString("plugin.graph.dialog.export.from") + "_"
                            + fmtDate.print(minDate) + "_" + I18n.getInstance().getString("plugin.graph.dialog.export.to") + "_"
                            + fmtDate.print(maxDate));

            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", ".pdf");
            fileChooser.getExtensionFilters().addAll(pdfFilter);
            fileChooser.setSelectedExtensionFilter(pdfFilter);

            File file = fileChooser.showSaveDialog(JEConfig.getStage());
            if (file != null) {
                String fileExtension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0);
                if (!file.getAbsolutePath().contains(fileExtension)) {
                    destinationFile = new File(file + fileExtension);
                } else {
                    destinationFile = file;
                }
                JEConfig.setLastPath(file);
            }
        } else {
            String tmpFileName = UUID.randomUUID().toString();
            try {
                Path temp = Files.createTempFile(tmpFileName, ".pdf");
                destinationFile = temp.toFile();
            } catch (IOException e) {
                logger.error("Could not create temp file", e);
            }
        }
    }

    public byte[] export(VBox vBox) {

        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setTransform(new Scale(2, 2));
        snapshotParameters.setFill(Color.WHITESMOKE);

        Platform.runLater(() -> {
            WritableImage image = vBox.snapshot(snapshotParameters, null);

            try {
                OutputStream fileStream = new FileOutputStream(destinationFile);

                Document document = new Document();
                PdfWriter.getInstance(document, fileStream);

                document.open();
                document.setPageSize(PageSize.A4.rotate());
                document.addAuthor("JEVis");
                document.addTitle(formattedName);
                document.addCreationDate();

                String tmpFileName = UUID.randomUUID().toString();
                Path temp = Files.createTempFile(tmpFileName, ".png");
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", temp.toFile());
                Image img = Image.getInstance(temp.toFile().toURI().toURL());

                PdfPTable table = new PdfPTable(1);
                table.setWidthPercentage(100);

                Font CUSTOM_FONT_HEADER = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD | Font.UNDERLINE);
                Font CUSTOM_FONT_TEXT = new Font(Font.FontFamily.TIMES_ROMAN, 11);

                Paragraph headerParagraph = new Paragraph(formattedName);
                headerParagraph.setFont(CUSTOM_FONT_HEADER);

                PdfPCell cellTitle = new PdfPCell(headerParagraph);
                cellTitle.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellImage = new PdfPCell(img, true);
                cellImage.setBorder(Rectangle.NO_BORDER);
                cellImage.setPadding(5);

                Paragraph datePara = new Paragraph(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(new DateTime()));
                datePara.setFont(CUSTOM_FONT_TEXT);
                PdfPCell cellFooter = new PdfPCell(datePara);
                cellFooter.setBorder(Rectangle.NO_BORDER);

                table.addCell(cellTitle);
                table.addCell(cellImage);
                table.addCell(cellFooter);

                document.add(table);
                document.close();

                if (!directPrint) {
                    try {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(destinationFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else print();

            } catch (IOException | DocumentException e) {
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


        return null;
    }

    private void print() {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        try {
            PDDocument document = PDDocument.load(destinationFile);
            printerJob.setPageable(new PDFPageable(document));
            if (printerJob.printDialog()) {
                printerJob.print();
            }
        } catch (IOException | PrinterException e) {
            logger.error("Could not print temp file", e);
        }

    }

    private void setDates() {
        model.getChartModels().forEach(chart -> {
            for (ChartData mdl : chart.getChartData()) {
                DateTime startNow = mdl.getIntervalStartDateTime();
                DateTime endNow = mdl.getIntervalEndDateTime();
                if (minDate == null || startNow.isBefore(minDate)) minDate = startNow;
                if (maxDate == null || endNow.isAfter(maxDate)) maxDate = endNow;
            }
        });
    }

    public File getDestinationFile() {
        return destinationFile;
    }
}
