/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;

/**
 * This class can read and write .jcfp files.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisClassPackageManager {

    private File file;
    private JEVisDataSource ds;

    public JEVisClassPackageManager(String jcfp, JEVisDataSource ds) {
        System.out.println("readFiless");
        file = new File(jcfp);
        this.ds = ds;
        if (file.exists()) {
            System.out.println("file exists");
        }
    }

    public boolean importIntoJEVis(JEVisDataSource ds) {
        Map<String, JsonJEVisClass> classes = new HashMap<>();
        Map<String, Image> icons = new HashMap<>();

        try {
            FileInputStream fis = new FileInputStream(file);
            try (ZipInputStream zis = new ZipInputStream(fis)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {

                    try {
                        System.out.println("File: " + entry.getName());
                        if (entry.getName().endsWith(".jcf")) {
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            StringBuilder sb = new StringBuilder();
                            for (int c = zis.read(); c != -1; c = zis.read()) {
                                sb.append((char) c);
                            }
                            JsonJEVisClass newJClass = gson.fromJson(sb.toString(), JsonJEVisClass.class);
                            classes.put(newJClass.getName(), newJClass);
                            System.out.println("new Class: " + newJClass.getName());
                        } else if (entry.getName().endsWith(".icon")) {
                            System.out.println("is icon");
                            Image newImage = ImageIO.read(zis);
                            icons.put(entry.getName(), newImage);
                        }
                    } catch (IOException ex) {
                        System.out.println("exeption while reading file, skip to next: " + ex.getMessage());
                    }

                }
            }
            checkContent(classes, icons);

        } catch (IOException ioe) {
            System.out.println("Error creating zip file: " + ioe);
            return false;
        }
        return true;
    }

    private boolean classExists(JEVisDataSource ds, Map<String, JsonJEVisClass> classes, String jclass) {
        if (classes.containsKey(jclass)) {
            return true;
        } else {
            try {
                if (ds.getJEVisClass(jclass) != null) {
                    return true;
                }
            } catch (JEVisException ex) {
                Logger.getLogger(JEVisClassPackageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public static BufferedImage imageToBufferedImage(Image im) {
        BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics bg = bi.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
    }

    private List<JEVisClass> buildClasses(JEVisDataSource ds, Map<String, JsonJEVisClass> classes, Map<String, Image> icons) {
        List<JEVisClass> jclasses = new ArrayList<>();
        for (Map.Entry<String, JsonJEVisClass> entry : classes.entrySet()) {
            try {
                if (ds.getJEVisClass(entry.getKey()) == null) {
                    JEVisClass newClass = ds.buildClass(entry.getKey());
                }

                JEVisClass dbClass = ds.getJEVisClass(entry.getKey());
                dbClass.setDescription(entry.getValue().getDescription());
                dbClass.setDescription(entry.getValue().getDescription());

                if (icons.containsKey(entry.getKey())) {
//                    ImageIcon ii = new ImageIcon(icons.get(entry.getKey()));
                    dbClass.setIcon(imageToBufferedImage(icons.get(entry.getKey())));
                }

                for (JsonType type : entry.getValue().getTypes()) {
                    if (dbClass.getType(type.getName()) == null) {
                        JEVisType newType = dbClass.buildType(type.getName());
                        newType.setPrimitiveType(type.getPrimitiveType());
                        newType.setGUIDisplayType(type.getGUIDisplayType());
                        newType.setGUIPosition(type.getGUIPosition());
                        newType.commit();
                    }
                }
                jclasses.add(dbClass);

            } catch (Exception ex) {
                System.out.println("Faild to create class: " + ex);
            }
        }
        return jclasses;
    }

    private void buildRelationships(JEVisDataSource ds, Map<String, JsonJEVisClass> classes) {
        for (Map.Entry<String, JsonJEVisClass> entry : classes.entrySet()) {

        }
    }

    public boolean checkContent(Map<String, JsonJEVisClass> classes, Map<String, Image> icons) {
//        for (JsonJEVisClass jclass : classes) {
        for (Map.Entry<String, JsonJEVisClass> entry : classes.entrySet()) {
            System.out.println("Check class: " + entry.getKey());
            for (JsonRelationship rel : entry.getValue().getRelationships()) {

                if (classExists(ds, classes, rel.getFrom())) {
                    System.out.println("From class is ok");
                } else {
                    System.out.println("From class is NOT ok");
                }
                if (classExists(ds, classes, rel.getTo())) {
                    System.out.println("To class is ok");
                } else {
                    System.out.println("To class is NOT ok");
                }

            }
        }

        return false;
    }

    public void setContent(List<JEVisClass> classes) {
        List<ZIPContent> content = new ArrayList<>();
        for (JEVisClass jc : classes) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonJEVisClass jsclass = new JsonJEVisClass(jc);
            String json = gson.toJson(jsclass);
            try {

                BufferedImage bIcon = jc.getIcon();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bIcon, "gif", baos);
                baos.flush();
                byte[] imageInByte = baos.toByteArray();
                baos.close();

                content.add(new ZIPContent(json.getBytes(), jc.getName(), ZIPContent.TYPE.CLASS));
                content.add(new ZIPContent(imageInByte, jc.getName(), ZIPContent.TYPE.ICON));

            } catch (JEVisException ex) {
                Logger.getLogger(JEVisClassPackageManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(JEVisClassPackageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        addZIPContentToFile(file, content);
    }

    private void addZIPContentToFile(File zipFile, List<ZIPContent> contents) {

        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            try (ZipOutputStream zos = new ZipOutputStream(fos)) {
                for (ZIPContent zip : contents) {
                    zos.putNextEntry(new ZipEntry(zip.getName()));
                    zos.write(zip.getData());
                    zos.closeEntry();
                }
            }

        } catch (IOException ioe) {
            System.out.println("Error creating zip file: " + ioe);
        }
    }

}
