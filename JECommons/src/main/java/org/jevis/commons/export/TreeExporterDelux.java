package org.jevis.commons.export;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonUnit;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TreeExporterDelux {


    private static final Logger logger = LogManager.getLogger(TreeExporterDelux.class);
    private final String OBJECT_NAME = "name";
    private final String OBJECT_CLASS = "class";
    private final String OBJECT_CHILD = "children";
    private final String OBJECT_LANG = "lang";
    private final String OBJECT_ATTRIBUTES = "attributes";
    private final String ATTRIBUTE_NAME = "attribute";
    private final String ATTRIBUTE_ID = "object";
    private final String ATTRIBUTE_UNIT = "unit";
    private final String ATTRIBUTE_SAMPLES = "samples";
    private final String ATTRIBUTE_RATE = "sampleRate";
    private final String SAMPLE_TS = "t";
    private final String SAMPLE_VALUE = "v";
    private final ObjectMapper mapper = new ObjectMapper();

    public TreeExporterDelux() {
    }


    public void createObject(JsonNode jsonNode, JEVisObject parent, StringProperty message) {
        try {
            logger.error("Create Object: {} [{}]", jsonNode.get(OBJECT_NAME), jsonNode.get(OBJECT_CLASS));
            message.setValue("Create Object " + jsonNode.get(OBJECT_NAME) + "[" + jsonNode.get(OBJECT_CLASS) + "]");
            JEVisClass objClass = parent.getDataSource().getJEVisClass(jsonNode.get(OBJECT_CLASS).asText());
            if (objClass == null) {
                logger.error("Class does not exist, skipping to next: {}", jsonNode.get(OBJECT_CLASS));
                return;
            }

            if (!parent.getAllowedChildrenClasses().contains(objClass)) {
                logger.error("Class '{}' is not allowed under: '{}'", objClass.getName(), parent.getJEVisClassName());
                return;
            }
            JEVisObject newJEVisObject = parent.buildObject(jsonNode.get(OBJECT_NAME).asText(), objClass);

            if (jsonNode.get(OBJECT_LANG).isArray()) {
                for (JsonNode jsonNode1 : jsonNode.get(OBJECT_LANG)) {
                    jsonNode1.fieldNames().forEachRemaining(s -> {
                        newJEVisObject.setLocalName(s, jsonNode1.get(s).asText());
                    });
                }
            }


            newJEVisObject.commit();
            JsonNode jAttributes = jsonNode.get(OBJECT_ATTRIBUTES);
            logger.error("Attribute Count: {}", jAttributes.size());
            if (jAttributes != null && jAttributes.isArray()) {
                for (JsonNode jAttribute : jAttributes) {
                    try {
                        logger.error("Create Attribute: {}", jAttribute.get(ATTRIBUTE_NAME));
                        JEVisAttribute jevisAttribute = newJEVisObject.getAttribute(jAttribute.get(ATTRIBUTE_NAME).asText());
                        if (newJEVisObject != null) {
                            if (jAttribute.get(ATTRIBUTE_UNIT) != null) {
                                try {
                                    JsonNode unit = jAttribute.get(ATTRIBUTE_UNIT);
                                    /**
                                     String formula = unit.get("formula").asText("");
                                     String label = unit.get("label").asText("");
                                     String prefix = unit.get("prefix").asText("NONE");
                                     JEVisUnitImp test = new JEVisUnitImp(formula, label, prefix);
                                     System.out.println("test:" + test);
                                     **/
                                    String unitString = mapper.writeValueAsString(unit);
                                    JEVisUnitImp jevUnitImp = new JEVisUnitImp(mapper.readValue(unitString, org.jevis.commons.ws.json.JsonUnit.class));

                                    jevisAttribute.setInputUnit(jevUnitImp);
                                    jevisAttribute.setDisplayUnit(jevUnitImp);
                                } catch (Exception ex) {

                                    logger.error("Unit Error: ", ex);
                                }
                            }
                            if (jAttribute.get(ATTRIBUTE_RATE) != null) {
                                jevisAttribute.setInputSampleRate(Period.parse(jAttribute.get(ATTRIBUTE_RATE).asText()));
                                jevisAttribute.setDisplaySampleRate(Period.parse(jAttribute.get(ATTRIBUTE_RATE).asText()));
                            }
                            jevisAttribute.commit();

                            JsonNode jSamples = jAttribute.get(ATTRIBUTE_SAMPLES);
                            //System.out.println("Samples to create: " + jSamples);
                            if (jSamples != null && jSamples.isArray()) {
                                logger.error("Create Samples: {}", jSamples.size());
                                List<JEVisSample> jeVisSamples = new ArrayList<>();
                                for (JsonNode jSample : jSamples) {
                                    try {
                                        //System.out.println("Sample: " + jSample);
                                        DateTime dateTime = DateTime.parse(jSample.get(SAMPLE_TS).asText());
                                        JEVisSample sample = jevisAttribute.buildSample(dateTime, jSample.get(SAMPLE_VALUE).asText());
                                        jeVisSamples.add(sample);
                                        //System.out.println("jevsample: " + sample);
                                    } catch (Exception ex) {
                                        logger.error("Error while creating Sample: {}", jSample, ex);
                                    }
                                }
                                if (!jeVisSamples.isEmpty()) {
                                    logger.error("Import samples: {}", jeVisSamples.size());
                                    jevisAttribute.addSamples(jeVisSamples);
                                }
                            }
                        }

                    } catch (Exception ex) {
                        logger.error("Error while creating Attribute: {}", jAttribute, ex);
                    }
                }
            }

            JsonNode jChildren = jsonNode.get(OBJECT_CHILD);
            if (jChildren != null && jChildren.isArray()) {
                for (JsonNode jchild : jChildren) {
                    try {
                        logger.debug("Create Child: {} under {}", jchild, newJEVisObject);
                        createObject(jchild, newJEVisObject, message);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("Error while creating Object: {}", jsonNode, ex);
        }
    }

    public Task<Void> importFromFile(File file, JEVisObject parent) {
        return new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    logger.error("==========================================");
                    logger.error("importFromFile: {} parent: {}", file, parent);
                    JsonNode jsonNodes = mapper.readTree(file);
                    StringProperty messages = new SimpleStringProperty();
                    messages.addListener((observable, oldValue, newValue) -> updateMessage(newValue));

                    for (JsonNode jsonNode : jsonNodes) {
                        createObject(jsonNode, parent, messages);
                    }

                    logger.error("All Done");


                    succeeded();
                } catch (Exception ex) {
                    failed();
                } finally {
                    done();
                }
                return null;
            }
        };
    }

    public Task<Void> exportToFileTask(File file, List<JEVisObject> objects) {
        return new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    StringProperty message = new SimpleStringProperty();
                    message.addListener((observable, oldValue, newValue) -> updateMessage(newValue));

                    List<ObjectNode> jObjects = new ArrayList<>();
                    List<JEVisObject> allObjects = new ArrayList<>();

                    for (JEVisObject object : objects) {
                        allObjects.addAll(CommonMethods.getAllChildrenRecursive(object));
                    }

                    int jobCount = allObjects.size();

                    for (JEVisObject obj : objects) {
                        try {

                            ObjectNode jNode = buildObjectNode(allObjects, obj, message, jobCount);
                            jObjects.add(jNode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                    try {
                        writer.writeValue(file, jObjects);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    succeeded();
                } catch (Exception ex) {
                    failed();
                } finally {
                    done();
                }
                return null;
            }
        };
    }

    public ObjectNode buildObjectNode(List<JEVisObject> allObjects, JEVisObject obj, StringProperty message, int total) throws Exception {
        ObjectNode jNode = toJson(obj);
        message.set("Prepare Export Job [" + allObjects.indexOf(obj) + "/" + total + "] object: [" + obj.getID() + "] " + obj.getName() + "");

        ArrayNode jAttributes = jNode.putArray(OBJECT_ATTRIBUTES);
        for (JEVisAttribute jeVisAttribute : obj.getAttributes()) {
            try {
                //message.set("Export Attribute: " + obj.getName() + "-" + jeVisAttribute.getName());
                jAttributes.add(toJson(jeVisAttribute));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArrayNode jChildren = jNode.putArray(OBJECT_CHILD);
        for (JEVisObject child : obj.getChildren()) {
            try {
                jChildren.add(buildObjectNode(allObjects, child, message, total));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return jNode;
    }

    public ObjectNode toJson(JEVisAttribute jeVisAttribute) throws Exception {

        ObjectNode attributeNode = mapper.createObjectNode();

        attributeNode.put(ATTRIBUTE_NAME, jeVisAttribute.getName());

        if (jeVisAttribute.getInputSampleRate() != null) {
            attributeNode.put(ATTRIBUTE_RATE, jeVisAttribute.getInputSampleRate().toString());
        }

        if (jeVisAttribute.getInputUnit() != null) {
            JsonUnit junit = JsonFactory.buildUnit(jeVisAttribute.getInputUnit());
            //JsonTools.objectMapper().writeValueAsString(junit);
            attributeNode.putPOJO(ATTRIBUTE_UNIT, junit);
            //attributeNode.put(ATTRIBUTE_UNIT, jeVisAttribute.getInputUnit().toJSON());
        }

        if (jeVisAttribute.hasSample()) {
            ArrayNode jSamples = attributeNode.putArray(ATTRIBUTE_SAMPLES);
            for (JEVisSample jeVisSample : jeVisAttribute.getAllSamples()) {
                try {
                    ObjectNode sampleNode = mapper.createObjectNode();

                    sampleNode.put(SAMPLE_TS, jeVisSample.getTimestamp().toString()).put(SAMPLE_VALUE, jeVisSample.getValueAsString());
                    jSamples.add(sampleNode);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return attributeNode;
    }


    public ObjectNode toJson(JEVisObject object) throws Exception {


        ObjectNode dashBoardNode = mapper.createObjectNode();
        dashBoardNode.put(OBJECT_NAME, object.getName()).put(OBJECT_CLASS, object.getJEVisClassName());

        ArrayNode jnames = dashBoardNode.putArray(OBJECT_LANG);

        for (Map.Entry<String, String> entry : object.getLocalNameList().entrySet()) {
            String lang = entry.getKey();
            String translatedName = entry.getValue();
            try {
                ObjectNode langNode = mapper.createObjectNode();
                langNode.put(lang, translatedName);
                jnames.add(langNode);
            } catch (Exception ex) {
                logger.error("Error while exporting language: {} ", lang, ex);
            }
        }

        return dashBoardNode;

    }

    public void zipFiles(File zipFileName, File directory) throws IOException {
        File[] files = directory.listFiles();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            for (File jsonFile : files) {
                if (jsonFile.isFile() && jsonFile.getName().endsWith(".json")) {
                    ZipEntry zipEntry = new ZipEntry(jsonFile.getName());
                    zos.putNextEntry(zipEntry);
                    //zos.write(objectMapper.writeValueAsBytes(simplePojo));
                    zos.closeEntry();
                }
            }
        }
    }

}
