package org.jevis.commons.export;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(ExportMaster.class);

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
//        logger.info("Rel: "+rel.getFrom() + ":" + rel.getTo());
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
                            //                                logger.info("add new Key:" +key);
                            structure.add(key);
                        }


                    }
                }

            } catch (Exception ex) {
                logger.fatal(ex);
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
                logger.fatal(ex);
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

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        logger.info("Export object count: {}", metaObjects.size());
        for (Map.Entry<String, MetaObject> mo : metaObjects.entrySet()) {
            MetaObject metaObject = mo.getValue();
//            File newFile = new File(tmpDir.getAbsolutePath() + File.pathSeparator + mo.getKey() + ".json");
            File newFile = new File(outputfile.getAbsolutePath() + File.separatorChar + mo.getKey() + ".json");
            logger.info("write File: {}", newFile);
            try (Writer writer = new FileWriter(newFile)) {
//                Gson gson = new GsonBuilder().create();
                objectMapper.writeValue(writer, mo.getValue());
//                gson.toJson(mo.getValue(), writer);
            }

            //write Samples
            if (mo.getValue().getMode() == MetaObject.Mode.ALL_DATA) {
//                for(JsonAttribute att: metaObject.getObject().getAttributes()){
//                    List<JsonSample> list =
//                }


            }


        }


        for (String key : structure) {
            logger.info("Rel: {}", key);
        }


    }


    public void createTemplate(JEVisObject parent) {
        logger.info("Create first level");
        root.forEach(rootID -> {

            MetaObject mo = metaObjects.get(rootID.toString());
            logger.info("Build: {}", mo.getObject().getName());

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
//            logger.info("("+parentID+") ? "+key);
            String[] keys = key.split(":");
            if (keys[1].equals(parentID)) {
                MetaObject mo = metaObjects.get(keys[0]);
                logger.info("--> Build child: {}", mo.getObject().getName());
                buildChildren(mo.getKey());
            }

        });
    }


    public boolean validateObjects() {
        logger.info("Validate");

        logger.info("=Root=");
        root.forEach(root -> logger.info(root.toString()));

        logger.info("Parents: ");
        for (String key : structure) {
//            logger.info("- key: "+key);
            try {
                String[] keys = key.split(":");
                if (metaObjects.containsKey(keys[0])) {
//                    logger.info("Key: " + keys[0] + " is OK");
                } else {
                    logger.info("Key: {} is NOK", keys[0]);
                }

                if (metaObjects.containsKey(keys[1])) {
//                    logger.info("Key: " + keys[1] + " is OK");
                } else {
                    logger.info("Key: {} is NOK", keys[1]);
                }
            } catch (Exception ex) {
                logger.info("Key error: {}", key);
                logger.fatal(ex);
            }

        }

//        for(Map.Entry<String,MetaObject> entry:metaObjects.entrySet()){
//            if(metaObjects.containsKey(String.valueOf(entry.getValue().getObject().getParent()))){
//                logger.info("Key: "+entry.getKey()+"  -> Valid Parent");
//            }else{
//                logger.info("Key: "+entry.getKey()+"  -> NOT Valid Parent");
//            }
//        }

        //check if this obj has an valid parent
        return true;
    }


}
