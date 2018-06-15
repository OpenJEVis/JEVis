/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeapi.ws;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class to load an Zipfile with all classicons from the JEVisWebservice.
 *
 * This Class is a temporary solution until the permanent class caching is
 * implemented.
 *
 * @deprecated this is just an tmp solution
 * @author fs
 */
public class ClassIconHandler {

    private List<File> files = new ArrayList<>();
    private boolean fileExists = false;
    private File tmpDir;
    private final Logger logger = LogManager.getLogger(ClassIconHandler.class);
    File zipFile;

    public ClassIconHandler(File tmpDir) {
        this.tmpDir = tmpDir;

        zipFile = new File(tmpDir + "/classicons.zip");
        fileExists = zipFile.canRead();

        zipFile.deleteOnExit();
        logger.debug("zipFile: {}   {}", zipFile, zipFile.canRead());
    }

    public void readStream(InputStream input) throws IOException {
        logger.info("ReadStream");
        tmpDir.mkdirs();

        OutputStream outputStream = new FileOutputStream(zipFile);

        int read = 0;
        byte[] bytes = new byte[1024];

        while ((read = input.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }
        unZipIt(tmpDir, zipFile);

    }

    public boolean fileExists() {
        return fileExists;
    }

    public Map<String, BufferedImage> getClassIcon() throws Exception {
        Map<String, BufferedImage> map = new HashMap<>();
        for (File file : tmpDir.listFiles()) {
            if (file.getName().contains(".png")) {
                map.put(file.getName().replaceAll(".png", ""), ImageIO.read(file));
            }
        }
        return map;
    }
    
    public BufferedImage getClassIcon(String name) throws Exception {
        for (File file : tmpDir.listFiles()) {
            if (file.getName().equals(name + ".png")) {
                return ImageIO.read(file);
            }
        }
        return null;
    }

    public void unZipIt(File folder, File zipFile) {
//        List<File> files = new ArrayList<>();

        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            if (!folder.exists()) {
                folder.mkdir();
            }
            folder.deleteOnExit();

            //get the zip file content
            ZipInputStream zis
                    = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(folder + File.separator + fileName);
                newFile.deleteOnExit();
//                files.add(newFile);

//                System.out.println("file unzip : " + newFile.getAbsoluteFile());
                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

//                files.add(newFile);
                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
