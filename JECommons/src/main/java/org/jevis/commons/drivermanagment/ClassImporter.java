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
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FilenameUtils;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;
import org.jevis.commons.json.JsonFactory;
import org.jevis.commons.json.JsonJEVisClass;
import org.jevis.commons.json.JsonRelationship;
import org.jevis.commons.json.JsonType;

/**
 * Very basic JEVisClass importer. This implementation will be replaced in the
 * future.
 *
 * @author Florian Simon
 */
public class ClassImporter {

    private JEVisDataSource ds;
    private boolean delete = false;

    public ClassImporter(JEVisDataSource ds) {
        this.ds = ds;

    }

    public void setDeleteExisting(boolean delete) {
        this.delete = delete;
    }

    public void importFiles(List<File> files) {
//        System.out.println("Import Files:");
//        for (File f : files) {
//            System.out.println("File: " + f.getName());
//        }

        List<JsonJEVisClass> classes = new ArrayList<>();
//        List<JsonRelationship> rel = new ArrayList<>();
        List<File> icons = new ArrayList<>();
        List<File> relationshipsFiles = new ArrayList<>();

        for (File file : files) {
            try {
//                System.out.println("FilenameUtils: " + FilenameUtils.getExtension(file.getName()));
//                System.out.println("Import File: " + file);
//                System.out.println("Parent1: " + file.getParentFile().getName());
//                System.out.println("Parent2: " + file.getParent());

                if (file.getParentFile() != null) {

                    switch (file.getParentFile().getName()) {

                        case ClassExporter.DIR_CLASSES:
//                            System.out.println("is Class dir");
//                            for (File subFiles : file.listFiles()) {
                            if (FilenameUtils.isExtension(file.getName(), "json")) {
//                                System.out.println("Classjoin: " + file);
                                classes.add(importClass(file));
                            }
//                            }
                            break;
                        case ClassExporter.DIR_ICONS:
//                            System.out.println("is File");
//                            for (File subFiles : file.listFiles()) {
                            if (FilenameUtils.isExtension(file.getName(), "png")) {
                                icons.add(file);
                            }
//                            }
                            break;
                        case ClassExporter.DIR_RELATIONSHIPS:
//                            System.out.println("is Relationship");
//                            for (File subFiles : file.listFiles()) {
                            if (FilenameUtils.isExtension(file.getName(), "json")) {
                                relationshipsFiles.add(file);
                            }
//                            }
                            break;
                        default:
                            break;
                    }

                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(ClassImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        List<JEVisClass> newClasses = new ArrayList<>();
        List<JsonJEVisClass> faildClasses = new ArrayList<>();
        System.out.println("\n###### Build Class ######");
        for (JsonJEVisClass myclass : classes) {
//            System.out.println("\n[Build Class]: " + myclass.getName() + "\n\t " + myclass);
            try {
                newClasses.add(buildClass(myclass, getIconForClass(myclass.getName(), icons)));

            } catch (JEVisException ex) {
                faildClasses.add(myclass);

                System.out.println("-->[ERROR] Cound not build class: " + myclass.getName());
                ex.printStackTrace();

//                Logger.getLogger(ClassImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

//        System.out.println("Created JEVisClasses:");
//        for (JEVisClass okclass : newClasses) {
//            try {
//                System.out.println("--> " + okclass.getName());
//            } catch (Exception ex) {
//
//            }
//        }
        if (!faildClasses.isEmpty()) {
            System.out.println("\nJEVisClasses which are not created");
            for (JsonJEVisClass fail : faildClasses) {
                try {
                    System.out.println("--> " + fail);
                } catch (Exception ex) {

                }
            }
        }

        if (!relationshipsFiles.isEmpty()) {
            for (File relFile : relationshipsFiles) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(relFile));
                    Gson gson = new Gson();
                    List<JsonRelationship> jRel = gson.fromJson(br, new TypeToken<List<JsonRelationship>>() {
                    }.getType());
                    List<JEVisClassRelationship> newRelationships = buildRelationship(jRel);
                } catch (Exception ex) {
                    System.out.println("-->[ERROR] Error while building Relationships: " + ex);
                }
            }

        }

    }

    private List<JEVisClassRelationship> buildRelationship(List<JsonRelationship> rels) {
        System.out.println("###### Build Relationships ######");
        List<JEVisClassRelationship> newRelasionships = new ArrayList<>();

        List<JsonRelationship> notImportet = new ArrayList<>();

        for (JsonRelationship rel : rels) {
            try {
//                System.out.print("\n[Build relationship]:\n\t " + rel + "\t");

                JEVisClass fromObject = ds.getJEVisClass(rel.getFrom());
                JEVisClass toObject = ds.getJEVisClass(rel.getTo());

                boolean exists = false;
                for (JEVisClassRelationship dbRel : fromObject.getRelationships()) {
                    if (dbRel.getStart().equals(fromObject) && dbRel.getEnd().equals(toObject)) {
                        exists = true;
                    }
                }

                if (!exists) {
//                    System.out.println("CI.buildRelationship: f=" + fromObject + "  t=" + toObject);
                    newRelasionships.add(fromObject.buildRelationship(toObject, rel.getType(), JEVisConstants.Direction.FORWARD));
                }

            } catch (Exception ex) {
                notImportet.add(rel);
//                System.out.println("-->[ERROR] Invalid classrealationship: " + rel);
//                Logger.getLogger(ClassImporter.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        System.out.println("\nCreated Relationships");
        for (JEVisClassRelationship crel : newRelasionships) {
            try {
                System.out.println("--> [" + crel.getType() + "]  " + crel.getStart().getName() + "  -->  " + crel.getEnd().getName());
            } catch (JEVisException jex) {
                System.out.println("--> [ERROR] Unknow error in the createt relationship: " + jex);
            }
        }

        if (!notImportet.isEmpty()) {
            System.out.println("\nRelationships which are not importet:");
            for (JsonRelationship rel : notImportet) {
                try {
                    System.out.println(rel.toString());

                    JEVisClass fromObject = ds.getJEVisClass(rel.getFrom());
                    JEVisClass toObject = ds.getJEVisClass(rel.getTo());

                    if (fromObject == null) {
                        System.out.println("--> [ERROR] Missing from class relationship: " + rel.getFrom());
                    }
                    if (toObject == null) {
                        System.out.println("--> [ERROR] Missing to class relationship: " + rel.getTo());
                    }
                } catch (Exception ex) {
                    System.out.println("--> [ERROR] Unknow error: " + ex);
                }
            }
        }

        return newRelasionships;

    }

    private List<JEVisType> buildTypes(JEVisClass jclass, List<JsonType> types) {
//        System.out.println("###### Build Types ######");
        List<JEVisType> dbTypes = new ArrayList<>();
        List<JsonType> faildType = new ArrayList<>();

        for (JsonType type : types) {
            try {
//                System.out.println("\n[Build Type]: " + type.getName() + "  \n\t " + type);

                JEVisType newType = null;
                if (jclass.getType(type.getName()) == null) {
                    newType = jclass.buildType(type.getName());
//                    System.out.println("create");
//                    dbTypes.add(newType);
                } else {
//                    System.out.println("exist alleady, update");
                    newType = jclass.getType(type.getName());
//                    dbTypes.add(newType);
                }

                if (newType != null) {
                    newType.setPrimitiveType(type.getPrimitiveType());
                    newType.setGUIDisplayType(type.getGUIDisplayType());

                    newType.commit();
                    dbTypes.add(newType);

                } else {
                    faildType.add(type);
//                    System.out.println("ERROR: type was not created");
                }

            } catch (Exception ex) {
                faildType.add(type);
//                System.out.println("-->[ERROR] cant build type: " + ex);
//                Logger.getLogger(ClassImporter.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        System.out.println("-- Types:");
        for (JEVisType type : dbTypes) {
            try {
                System.out.println("----> " + type.getName());
            } catch (Exception ex) {

            }
        }

        if (!faildType.isEmpty()) {
            System.out.println("Failed Types:");
            for (JsonType fail : faildType) {
                try {
                    System.out.println("----> " + fail);
                } catch (Exception ex) {

                }
            }
        }

        return dbTypes;

    }

    private File getIconForClass(String className, List<File> files) {
        for (File file : files) {
            if (file.getName().contains(className + ".png")) {
                return file;
            }
        }

        return null;
    }

    private JEVisClass buildClass(JsonJEVisClass jclass, File icon) throws JEVisException {

        JEVisClass dbClass = null;
        if (ds.getJEVisClass(jclass.getName()) != null) {
//            System.out.println("Class allready exis, updatet: " + jclass.getName());

            if (this.delete) {
                ds.getJEVisClass(jclass.getName()).delete();
//                System.out.println("delete " + jclass.getName());
            } else {
                dbClass = ds.getJEVisClass(jclass.getName());
            }

        }
        if (dbClass == null) {
//            System.out.println("build new class: " + jclass.getName());
            dbClass = ds.buildClass(jclass.getName());
        }

        dbClass.setDescription(jclass.getDescription());
        dbClass.setUnique(jclass.getUnique());

        if (icon != null) {
            dbClass.setIcon(icon);
        }
//        System.out.println("Commit class");
        dbClass.commit();

        System.out.println("\n--> New JEVisClass: " + dbClass.getName());

        buildTypes(dbClass, jclass.getTypes());

        return dbClass;

    }

    private Comparator<JsonJEVisClass> getComperator(final List<JsonJEVisClass> allClassInImport) {
        return new Comparator<JsonJEVisClass>() {

            @Override
            public int compare(JsonJEVisClass o1, JsonJEVisClass o2) {
//                System.out.print("Compare: " + o1.getName() + " - " + o2.getName() + " = ");
                boolean o1Parentless = false;
                boolean o2Parentless = false;
                if (o1.getInheritance() == null || o1.getInheritance().isEmpty() || o1.getInheritance().equals("null")) {
                    o1Parentless = true;
                }
                if (o2.getInheritance() == null || o2.getInheritance().isEmpty() || o2.getInheritance().equals("null")) {
                    o2Parentless = true;
                }

                if (o1Parentless == true && o2Parentless == true) {
//                    System.out.println(" 0");
                    return 0;
                } else if (o1Parentless == true && o2Parentless == false) {
//                    System.out.println(" -1");
                    return -1;
                } else if (o1Parentless == false && o2Parentless == true) {
//                    System.out.println(" 1");
                    return 1;
                } else if (o1Parentless == false && o2Parentless == false) {
                    if (o1.getName().equalsIgnoreCase(o2.getInheritance())) {
//                        System.out.println(" -1-2");
                        return -1;
                    } else if (o2.getName().equalsIgnoreCase(o1.getInheritance())) {
//                        System.out.println(" 1-2");
                        return 1;
                    }

                    List<JsonJEVisClass> o2Parents = getParents(allClassInImport, o2, new ArrayList<JsonJEVisClass>());
                    List<JsonJEVisClass> o1Parents = getParents(allClassInImport, o1, new ArrayList<JsonJEVisClass>());

//                    for (JsonJEVisClass o2p : o2Parents) {
//                        System.out.print("  o2p:" + o2p.getName());
//                    }
//                    for (JsonJEVisClass o1p : o1Parents) {
//                        System.out.print("  o1p:" + o1p.getName());
//                    }
                    if (o2Parents.contains(o1)) {
//                        System.out.println(" 1-3");
                        return 1;
                    } else if (o1Parents.contains(o2)) {
//                        System.out.println(" -1-3");
                        return -1;
                    } else {
//                        System.out.println(" 0-3");
                        return 0;
                    }
                }

//                System.out.println("fallback 0");
                return 0;
            }
        };
    }

    private List<JsonJEVisClass> getParents(List<JsonJEVisClass> allInImport, JsonJEVisClass jclass, List<JsonJEVisClass> parents) {
        if (jclass.getInheritance() == null || jclass.getInheritance().isEmpty()) {
            return parents;
        }

        JsonJEVisClass parent = null;

        for (JsonJEVisClass jsonC : allInImport) {
            if (jsonC.equals(jclass.getInheritance())) {
                parent = jsonC;
                parents.add(jsonC);
                System.out.println("Inheretd class is in Import JobList: " + jsonC);
                return getParents(allInImport, jsonC, parents);
            }
        }

        JEVisClass inDBClass = null;
        try {
            inDBClass = this.ds.getJEVisClass(jclass.getInheritance());
        } catch (JEVisException ex) {
            Logger.getLogger(ClassImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (inDBClass != null) {
            try {
                System.out.println("Class is in DB: " + inDBClass.getName());
            } catch (JEVisException ex) {
                Logger.getLogger(ClassImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                JsonJEVisClass dbJson = JsonFactory.buildJEVisClassComplete(inDBClass);
                parents.add(dbJson);
                if (inDBClass.getInheritance() != null) {
                    return getParents(allInImport, dbJson, parents);
                }
                return parents;
            } catch (JEVisException ex) {
                Logger.getLogger(ClassImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return parents;
    }

    public JsonJEVisClass importClass(File jsonFile) throws FileNotFoundException {
        Gson gson = new Gson();

        BufferedReader br = new BufferedReader(new FileReader(jsonFile));

        JsonJEVisClass jsonclass = gson.fromJson(br, JsonJEVisClass.class);

        return jsonclass;

    }

    public List<File> unZipIt(String outputFolder, File zipFile) {
        List<File> files = new ArrayList<>();

        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis
                    = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                newFile.deleteOnExit();
                files.add(newFile);

//                System.out.println("file unzip : " + newFile.getAbsoluteFile());
                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return files;
    }

}
