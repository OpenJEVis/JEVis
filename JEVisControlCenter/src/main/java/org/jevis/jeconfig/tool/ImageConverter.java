/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.tool;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.swing.ImageIcon;
import org.jevis.api.JEVisException;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ImageConverter {

    public static ImageView convertToImageView(ImageIcon icon) throws JEVisException {
        java.awt.image.BufferedImage bi = convertToBufferedImage(icon);
        javafx.scene.image.Image fi = convertToFxImage(bi);

        return new ImageView(fi);
    }

    public static ImageView convertToImageView(ImageIcon icon, double w, double h) throws JEVisException {
        java.awt.image.BufferedImage bi = convertToBufferedImage(icon);
        javafx.scene.image.Image fi = convertToFxImage(bi);
        ImageView iv = new ImageView(fi);
        iv.fitHeightProperty().setValue(h);
        iv.fitWidthProperty().setValue(w);
        iv.setSmooth(true);
        return iv;
    }

    public static ImageView convertToImageView(BufferedImage icon, double w, double h) throws JEVisException {
        if (icon == null) {
            return new ImageView();
        }

        Image image = SwingFXUtils.toFXImage(icon, null);
        ImageView iv = new ImageView(image);
        iv.fitHeightProperty().setValue(h);
        iv.fitWidthProperty().setValue(w);
        iv.setSmooth(true);
        return iv;
    }

    public static java.awt.image.BufferedImage convertToBufferedImage(ImageIcon icon) {
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_4BYTE_ABGR_PRE);
        Graphics g = bi.createGraphics();
        // paint the Icon to the BufferedImage.
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return bi;

    }

    public static javafx.scene.image.Image convertToFxImage(java.awt.image.BufferedImage awtImage) {
//        if (Image.impl_isExternalFormatSupported(BufferedImage.class)) {
//            return javafx.scene.image.Image.impl_fromExternalImage(awtImage);
//        } else {
//            return null;
//        }
//        
        return null;
    }

    public static java.awt.image.BufferedImage convertToAwtImage(javafx.scene.image.Image fxImage) {
//        if (Image.impl_isExternalFormatSupported(BufferedImage.class)) {
//            java.awt.image.BufferedImage awtImage = new BufferedImage((int) fxImage.getWidth(), (int) fxImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
//            return (BufferedImage) fxImage.impl_toExternalImage(awtImage);
//        } else {
//            return null;
//        }

        return null;
    }
}
