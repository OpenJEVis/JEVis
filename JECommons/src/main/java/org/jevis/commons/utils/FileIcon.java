package org.jevis.commons.utils;

import org.jevis.api.JEVisFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class FileIcon {


    public static BufferedImage getIcon(JEVisFile file) throws IOException {
//        BufferedImage bImage = ImageIO.read(new File("sample.jpg"));
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ImageIO.write(bImage, "jpg", bos );
        byte[] data = file.getBytes();
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        BufferedImage bImage2 = ImageIO.read(bis);
//        ImageIO.write(bImage2, "jpg", new File("output.jpg"));

        return bImage2;
    }
}
