/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.drivermanagment;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisException;
import org.jevis.commons.json.JsonFactory;
import org.jevis.commons.json.JsonJEVisClass;
import org.jevis.commons.json.JsonRelationship;
import org.jevis.commons.json.JsonTools;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Very basic JEVisClass exporter. The implemnetation will be replaced in the
 * future.
 *
 * @author Florian Simon
 */
public class ClassExporter {
    private static final Logger logger = LogManager.getLogger(ClassExporter.class);

    public static final String DIR_CLASSES = "Classes";
    public static final String DIR_ICONS = "Icons";
    public static final String DIR_RELATIONSHIPS = "Relationships";

    public ClassExporter(File targetFile, List<JEVisClass> classes) {

        List<File> files = new ArrayList<>();

        for (JEVisClass jclass : classes) {
            try {
                files.addAll(filesForClass(jclass));

            } catch (JEVisException ex) {
                logger.fatal(ex);
            } catch (IOException ex) {
                logger.fatal(ex);
            }
        }
        try {
            files.addAll(relationshipsfilesForClass(classes));
        } catch (JEVisException ex) {
            logger.fatal(ex);
        } catch (IOException ex) {
            logger.fatal(ex);
        }

        writeFile(targetFile, files);
//        writeFile(targetFile, typeFiles);

    }

    public static void addToZipFile(File file, ZipOutputStream zos) throws IOException {

        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(file.getParentFile().getName() + "/" + file.getName());
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }

    private List<File> relationshipsfilesForClass(List<JEVisClass> jclasses) throws JEVisException, IOException {
        String tmpdir = System.getProperty("java.io.tmpdir");
        List<File> classFiles = new ArrayList<>();
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        List<JEVisClassRelationship> allRel = new ArrayList<>();
        List<JsonRelationship> allJson = new ArrayList<>();

        for (JEVisClass jclass : jclasses) {
            boolean exists = false;
            for (JEVisClassRelationship rel : jclass.getRelationships()) {
                for (JEVisClassRelationship relOld : allRel) {
                    if (rel.getStart().getName().equals(relOld.getStart().getName())
                            && rel.getEnd().getName().equals(relOld.getEnd().getName())
                            && rel.getType() == relOld.getType()) {
                        //is allready in List
                        exists = true;
                    }
                }
                if (!exists) {
                    allJson.add(new JsonRelationship(rel));
                }

            }
        }

        for (JEVisClassRelationship rel : allRel) {
            allJson.add(new JsonRelationship(rel));
        }
//        String jsonString = gson.toJson(allJson, new TypeToken<List<JsonRelationship>>() {
//        }.getType());
        String jsonString = JsonTools.prettyObjectMapper().writeValueAsString(allJson);

        File relFodler = new File(tmpdir + "/" + DIR_RELATIONSHIPS + "/");
        if (!relFodler.exists()) {
            relFodler.mkdirs();
        }
        relFodler.deleteOnExit();

        FileOutputStream outputStream;
        File temp = new File(tmpdir + "/" + DIR_RELATIONSHIPS + "/relationships.json");
        logger.info("File name: " + temp);

        outputStream = new FileOutputStream(temp);
        outputStream.write(jsonString.getBytes());
        outputStream.close();
        classFiles.add(temp);
        temp.deleteOnExit();

        return classFiles;
    }

    private List<File> filesForClass(JEVisClass jclass) throws JEVisException, IOException {
        String tmpdir = System.getProperty("java.io.tmpdir");
        List<File> classFiles = new ArrayList<>();
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonJEVisClass jsonClass = JsonFactory.buildJEVisClassWithType(jclass);

        String s = JsonTools.prettyObjectMapper().writeValueAsString(jsonClass);

        File classFodler = new File(tmpdir + "/" + DIR_CLASSES);
        if (!classFodler.exists()) {
            classFodler.mkdirs();
        }
        classFodler.deleteOnExit();

        FileOutputStream outputStream;
        File temp = new File(tmpdir + "/" + DIR_CLASSES + "/" + jclass.getName() + ".json");
        logger.info("File name: " + temp);

        outputStream = new FileOutputStream(temp);
        outputStream.write(s.getBytes());
        outputStream.close();
        classFiles.add(temp);
        temp.deleteOnExit();

        File iconFodler = new File(tmpdir + "/" + DIR_ICONS + "/");
        if (!iconFodler.exists()) {
            iconFodler.mkdirs();
        }
        iconFodler.deleteOnExit();

        if (jclass.getIcon() != null) {
            File tempIcon = new File(tmpdir + "/" + DIR_ICONS + "/" + jclass.getName() + ".png");
            ImageIO.write(jclass.getIcon(), "png", tempIcon);
            classFiles.add(tempIcon);
            tempIcon.deleteOnExit();
        }

        return classFiles;
    }

    private void writeFile(File targetfile, List<File> files) {
        try {
            FileOutputStream fos = new FileOutputStream(targetfile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : files) {
                addToZipFile(file, zos);
            }

            zos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            logger.fatal(e);
        } catch (IOException e) {
            logger.fatal(e);
        }
    }
}
