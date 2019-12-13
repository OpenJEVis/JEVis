package org.jevis.jeconfig.plugin.meters;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class ImagePrintable implements Printable {

    private double x, y, width;

    private int orientation;

    private BufferedImage image;

    public ImagePrintable(PrinterJob printJob, BufferedImage image) {
        PageFormat pageFormat = printJob.defaultPage();
        this.x = pageFormat.getImageableX();
        this.y = pageFormat.getImageableY();
        this.width = pageFormat.getImageableWidth();
        this.orientation = pageFormat.getOrientation();
        this.image = image;
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex)
            throws PrinterException {
        if (pageIndex == 0) {
            int pWidth = 0;
            int pHeight = 0;
            if (orientation == PageFormat.PORTRAIT) {
                pWidth = (int) Math.min(width, image.getWidth());
                pHeight = pWidth * image.getHeight() / image.getWidth();
            } else {
                pHeight = (int) Math.min(width, image.getHeight());
                pWidth = pHeight * image.getWidth() / image.getHeight();
            }
            g.drawImage(image, (int) x, (int) y, pWidth, pHeight, null);
            return PAGE_EXISTS;
        } else {
            return NO_SUCH_PAGE;
        }
    }

}