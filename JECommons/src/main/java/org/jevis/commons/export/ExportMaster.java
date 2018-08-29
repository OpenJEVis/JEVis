package org.jevis.commons.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jevis.api.*;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class ExportMaster {


    Map<String, MetaObject> metaObjects = new HashMap<>();
    Map<String, JsonRelationship> relationships = new HashMap<>();
    HashSet<String> structure = new HashSet<>();
    List<Long> root = new ArrayList<>();
    List<Long> rootParents = new ArrayList<>();

    Map<String, String> keymatching = new HashMap<>();

    List<Integer> backlist = Arrays.asList(100, 101, 102, 103, 104, 105);
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));


    public void ExportMaster(File outputFile) {

    }

    private String getKey(JsonRelationship rel) {
//        System.out.println("Rel: "+rel.getFrom() + ":" + rel.getTo());
        return rel.getFrom() + ":" + rel.getTo();//rel.getType()+
    }

    public List<JsonObject> buildJson(List<JEVisObject> objs, boolean includeChildren) {
        List<JsonObject> result = new ArrayList<>();
        for (JEVisObject obj : objs) {
            try {
                JsonObject jsonO = JsonFactory.buildObject(obj, false);
                jsonO.setAttributes(new ArrayList<>());
                for (JEVisAttribute att : obj.getAttributes()) {
                    JsonAttribute jsonA = JsonFactory.buildAttribute(att);
                    jsonO.getAttributes().add(jsonA);

                    JEVisSample lastValue = att.getLatestSample();
                    if (lastValue != null) {
                        jsonA.setLatestValue(JsonFactory.buildSample(lastValue, att.getPrimitiveType()));
                    }
                }
                result.add(jsonO);
                if (includeChildren) {
                    result.addAll(buildJson(obj.getChildren(), includeChildren));
                }


                List<JsonRelationship> jsonRels = JsonFactory.buildRelationship(obj.getRelationships());
                for (JsonRelationship rel : jsonRels) {
                    if (rel.getType() == JEVisConstants.ObjectRelationship.PARENT) {
                        if (!rootParents.contains(rel.getTo())) {
                            String key = getKey(rel);
                            //                                System.out.println("add new Key:" +key);
                            structure.add(key);
                        }


                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public void setObject(List<JEVisObject> objects, boolean includeChildren) {
        objects.forEach(object -> {
            try {
                root.add(object.getID());
                if (object.getParents() != null && !object.getParents().isEmpty()) {
                    rootParents.add(object.getParents().get(0).getID());
                }

            } catch (JEVisException ex) {
                ex.printStackTrace();
            }
        });

        List<JsonObject> jsonObjects = buildJson(objects, includeChildren);


        for (JsonObject obj : jsonObjects) {
            MetaObject mo = new MetaObject(obj);
            metaObjects.put(mo.getKey(), mo);
        }


        validateObjects();
    }

    public void export(File outputfile) throws IOException {
        //zip Files...

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("Export object count: " + metaObjects.size());
        for (Map.Entry<String, MetaObject> mo : metaObjects.entrySet()) {
            MetaObject metaObject = mo.getValue();
//            File newFile = new File(tmpDir.getAbsolutePath() + File.pathSeparator + mo.getKey() + ".json");
            File newFile = new File(outputfile.getAbsolutePath() + File.separatorChar + mo.getKey() + ".json");
            System.out.println("write File: " + newFile);
            try (Writer writer = new FileWriter(newFile)) {
//                Gson gson = new GsonBuilder().create();
                gson.toJson(mo.getValue(), writer);
            }

            //write Samples
            if (mo.getValue().getMode() == MetaObject.Mode.ALL_DATA) {
//                for(JsonAttribute att: metaObject.getObject().getAttributes()){
//                    List<JsonSample> list =
//                }


            }


        }


        for (String key : structure) {
            System.out.println("Rel: " + key);
        }


    }


    public void createTemplate(JEVisObject parent) {
        System.out.println("Create first level");
        root.forEach(rootID -> {

            MetaObject mo = metaObjects.get(rootID.toString());
            System.out.println("Build: " + mo.getObject().getName());

            buildChildren(mo.getObject().getId() + "");

        });
    }


//    public void exportToFiles() throws IOException {
//        try (Writer writer = new FileWriter("Output.json")) {
//            Gson gson = new GsonBuilder().create();
//            gson.toJson(users, writer);
//        }
//    }

    public void importStruckture() {
        //if TYPE.Uniqe
        //JsonReader reader = new JsonReader(new FileReader(jsonFile));


    }

    private void zip() {
//        String fileZip = "compressed.zip";
//        byte[] buffer = new byte[1024];
//        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
//        ZipEntry zipEntry = zis.getNextEntry();
//        while(zipEntry != null){
//            String fileName = zipEntry.getName();
//            File newFile = new File("unzipTest/" + fileName);
//            FileOutputStream fos = new FileOutputStream(newFile);
//            int len;
//            while ((len = zis.read(buffer)) > 0) {
//                fos.write(buffer, 0, len);
//            }
//            fos.close();
//            zipEntry = zis.getNextEntry();
//        }
//        zis.closeEntry();
    }

    public void buildChildren(String parentID) {
        structure.forEach(key -> {
//            System.out.println("("+parentID+") ? "+key);
            String[] keys = key.split(":");
            if (keys[1].equals(parentID)) {
                MetaObject mo = metaObjects.get(keys[0]);
                System.out.println("--> Build child: " + mo.getObject().getName());
                buildChildren(mo.getKey());
            }

        });
    }


    public boolean validateObjects() {
        System.out.println("Validate");

        System.out.println("=Root=");
        root.forEach(root -> System.out.println(root.toString()));

        System.out.println("Parents: ");
        for (String key : structure) {
//            System.out.println("- key: "+key);
            try {
                String[] keys = key.split(":");
                if (metaObjects.containsKey(keys[0])) {
//                    System.out.println("Key: " + keys[0] + " is OK");
                } else {
                    System.out.println("Key: " + keys[0] + " is NOK");
                }

                if (metaObjects.containsKey(keys[1])) {
//                    System.out.println("Key: " + keys[1] + " is OK");
                } else {
                    System.out.println("Key: " + keys[1] + " is NOK");
                }
            } catch (Exception ex) {
                System.out.println("Key error: " + key);
                ex.printStackTrace();
            }

        }

//        for(Map.Entry<String,MetaObject> entry:metaObjects.entrySet()){
//            if(metaObjects.containsKey(String.valueOf(entry.getValue().getObject().getParent()))){
//                System.out.println("Key: "+entry.getKey()+"  -> Valid Parent");
//            }else{
//                System.out.println("Key: "+entry.getKey()+"  -> NOT Valid Parent");
//            }
//        }

        //check if this obj has an valid parent
        return true;
    }


}
