/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.drivermanagment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisException;
import org.jevis.commons.json.JsonFactory;
import org.jevis.commons.json.JsonJEVisClass;
import org.jevis.commons.json.JsonRelationship;

/**
 * Very basic JEVisClass exporter. The implemnetation will be replaced in the
 * future.
 *
 * @author Florian Simon
 */
public class ClassExporter {

    public static final String DIR_CLASSES = "Classes";
    public static final String DIR_ICONS = "Icons";
    public static final String DIR_RELATIONSHIPS = "Relationships";

    public ClassExporter(File targetFile, List<JEVisClass> classes) {

        List<File> files = new ArrayList<>();

        for (JEVisClass jclass : classes) {
            try {
                files.addAll(filesForClass(jclass));

            } catch (JEVisException ex) {
                Logger.getLogger(ClassExporter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ClassExporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            files.addAll(relationshipsfilesForClass(classes));
        } catch (JEVisException ex) {
            Logger.getLogger(ClassExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClassExporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        writeFile(targetFile, files);
//        writeFile(targetFile, typeFiles);

    }

    private List<File> relationshipsfilesForClass(List<JEVisClass> jclasses) throws JEVisException, IOException {
        String tmpdir = System.getProperty("java.io.tmpdir");
        List<File> classFiles = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
        String jsonString = gson.toJson(allJson, new TypeToken<List<JsonRelationship>>() {
        }.getType());

        File relFodler = new File(tmpdir + "/" + DIR_RELATIONSHIPS + "/");
        if (!relFodler.exists()) {
            relFodler.mkdirs();
        }
        relFodler.deleteOnExit();

        FileOutputStream outputStream;
        File temp = new File(tmpdir + "/" + DIR_RELATIONSHIPS + "/relationships.json");
        System.out.println("File name: " + temp);

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
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonJEVisClass jsonClass = JsonFactory.buildJEVisClassWithType(jclass);

        String s = gson.toJson(jsonClass);

        File classFodler = new File(tmpdir + "/" + DIR_CLASSES);
        if (!classFodler.exists()) {
            classFodler.mkdirs();
        }
        classFodler.deleteOnExit();

        FileOutputStream outputStream;
        File temp = new File(tmpdir + "/" + DIR_CLASSES + "/" + jclass.getName() + ".json");
        System.out.println("File name: " + temp);

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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToZipFile(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {

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
}
