package org.jevis.jecc.application.resource;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;

public class PDFModel {
    private static final Logger logger = LogManager.getLogger(PDFModel.class);

    private PDDocument document;
    private PDFRenderer renderer;

    public PDFModel() {
    }

    public PDFModel(byte[] bytes) {
        setBytes(bytes);
    }

    public void setBytes(byte[] bytes) {
        try {
            document = PDDocument.load(bytes);
            renderer = new PDFRenderer(document);
        } catch (IOException ex) {
            throw new UncheckedIOException("PDDocument throws IOException bytes=" + bytes, ex);
        }
    }

    public int numPages() {
        return document.getPages().getCount();
    }

    public ImageView getImage(int pageNumber, double zoom) {
        Image image = getImage(pageNumber);

        ImageView imageView = new ImageView(image);
        imageView.setScaleX(zoom);
        imageView.setScaleY(zoom);

        return imageView;
    }

    private Image getImage(int pageNumber) {
        BufferedImage pageImage;
        try {
            pageImage = renderer.renderImageWithDPI(pageNumber, 300);
        } catch (IOException ex) {
            throw new UncheckedIOException("PDFRenderer throws IOException", ex);
        }

        return SwingFXUtils.toFXImage(pageImage, null);
    }
}