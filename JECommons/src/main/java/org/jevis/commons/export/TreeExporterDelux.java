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
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonUnit;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TreeExporterDelux {


    private String OBJECT_NAME = "name";
    private String OBJECT_CLASS = "class";
    private String OBJECT_CHILD = "children";
    private String OBJECT_LANG = "lang";
    private String OBJECT_ATTRIBUTES = "attributes";
    private String ATTRIBUTE_NAME = "attribute";
    private String ATTRIBUTE_ID = "object";
    private String ATTRIBUTE_UNIT = "unit";
    private String ATTRIBUTE_SAMPLES = "samples";
    private String ATTRIBUTE_RATE = "sampleRate";
    private String SAMPLE_TS = "t";
    private String SAMPLE_VALUE = "v";
    private static final Logger logger = LogManager.getLogger(TreeExporterDelux.class);
    private ObjectMapper mapper = new ObjectMapper();

    public TreeExporterDelux() {
    }


    public void createObject(JsonNode jsonNode, JEVisObject parent, StringProperty message) {
        try {
            System.out.println("Create Object: " + jsonNode.get(OBJECT_NAME) + "[" + jsonNode.get(OBJECT_CLASS) + "]");
            message.setValue("Create Object " + jsonNode.get(OBJECT_NAME) + "[" + jsonNode.get(OBJECT_CLASS) + "]");
            JEVisClass objClass = parent.getDataSource().getJEVisClass(jsonNode.get(OBJECT_CLASS).asText());
            if (objClass == null) {
                logger.error("Class does not exist, skiping to next: {}", jsonNode.get(OBJECT_CLASS));
                return;
            }

            if (!parent.getAllowedChildrenClasses().contains(objClass)) {
                logger.error("Class '{}' is not allowed under: '{}'", objClass.getName(), parent.getJEVisClassName());
                return;
            }
            JEVisObject newJEVisObject = parent.buildObject(jsonNode.get(OBJECT_NAME).asText(), objClass);
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


                                    System.out.println("sdfs: " + jevUnitImp);
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
                        logger.error("Create Child: {} under {}", jchild, newJEVisObject);
                        createObject(jchild, newJEVisObject, message);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("Error while creating Object: {}", jsonNode, ex);
        }
        return;
    }

    public Task importFromFile(File file, JEVisObject parent) throws IOException {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    logger.error("==========================================");
                    logger.error("importFromFile: {} parent: {}", file, parent);
                    JsonNode jsonNodes = mapper.readTree(file);
                    StringProperty messages = new SimpleStringProperty();
                    messages.addListener((observable, oldValue, newValue) -> {
                        updateMessage(newValue);
                    });

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

        return task;


    }


    public ObjectNode buildObjectNode(JEVisObject obj, StringProperty message, int counter, int total) throws Exception {
        ObjectNode jNode = toJson(obj);
        counter++;
        message.set("Prepare Export Job [" + counter + "/" + total + "] object: [" + obj.getID() + "] " + obj.getName() + "");

        ArrayNode jAttributes = jNode.putArray(OBJECT_ATTRIBUTES);
        obj.getAttributes().forEach(jeVisAttribute -> {
            try {
                //message.set("Export Attribute: " + obj.getName() + "-" + jeVisAttribute.getName());
                jAttributes.add(toJson(jeVisAttribute));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ArrayNode jChildren = jNode.putArray(OBJECT_CHILD);
        for (JEVisObject child : obj.getChildren()) {
            try {
                jChildren.add(buildObjectNode(child, message, counter, total));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return jNode;
    }

    public int countObjects(JEVisObject obj) {
        int i = 0;
        try {
            for (JEVisObject object : obj.getChildren()) {
                i += 1;
                i += countObjects(object);
            }
        } catch (Exception ex) {
        }
        return i;
    }

    public Task exportToFileTask(File file, List<JEVisObject> objects) {

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    StringProperty message = new SimpleStringProperty();
                    message.addListener((observable, oldValue, newValue) -> {
                        updateMessage(newValue);
                    });
                    List<ObjectNode> jObjects = new ArrayList<>();
                    int jobCount = 0;


                    for (JEVisObject obj : objects) {
                        try {
                            jobCount += countObjects(obj) + 1;

                            ObjectNode jNode = buildObjectNode(obj, message, new Integer(0), jobCount);
                            jObjects.add(jNode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                    try {
                        writer.writeValue(file, jObjects);
                    } catch (IOException e) {
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

        return task;


    }


    public ObjectNode toJson(JEVisAttribute jeVisAttribute) throws Exception {


        ObjectNode attributeNode = mapper.createObjectNode();

        attributeNode
                .put(ATTRIBUTE_NAME, jeVisAttribute.getName())
        ;
        if (jeVisAttribute.getInputSampleRate() != null) {
            attributeNode.put(ATTRIBUTE_RATE, jeVisAttribute.getInputSampleRate().toString());
        }
        if (jeVisAttribute.getInputUnit() != null) {
            JsonUnit junit = JsonFactory.buildUnit(jeVisAttribute.getInputUnit());
            //JsonTools.objectMapper().writeValueAsString(junit);
            attributeNode.putPOJO(ATTRIBUTE_UNIT, junit);
            System.out.println("?????? " + junit.toString());
            //attributeNode.put(ATTRIBUTE_UNIT, jeVisAttribute.getInputUnit().toJSON());
        }

        if (jeVisAttribute.hasSample()) {
            ArrayNode jSamples = attributeNode.putArray(ATTRIBUTE_SAMPLES);
            jeVisAttribute.getAllSamples().forEach(jeVisSample -> {
                try {
                    ObjectNode sampleNode = mapper.createObjectNode();

                    sampleNode
                            .put(SAMPLE_TS, jeVisSample.getTimestamp().toString())
                            .put(SAMPLE_VALUE, jeVisSample.getValueAsString())
                    ;
                    jSamples.add(sampleNode);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }


        return attributeNode;

    }


    public ObjectNode toJson(JEVisObject object) throws Exception {


        ObjectNode dashBoardNode = mapper.createObjectNode();
        dashBoardNode
                .put(OBJECT_NAME, object.getName())
                .put(OBJECT_CLASS, object.getJEVisClassName())
        ;

        ArrayNode jnames = dashBoardNode.putArray(OBJECT_LANG);

        object.getLocalNameList().forEach((s, s2) -> {
            try {
                ObjectNode langNode = mapper.createObjectNode();
                langNode.put(s, s2);
                jnames.add(langNode);
            } catch (Exception ex) {
                logger.error("Error while exporting languge: {} ", s, ex);
            }
        });


        return dashBoardNode;

    }

}
