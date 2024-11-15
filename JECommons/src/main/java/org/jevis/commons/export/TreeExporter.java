package org.jevis.commons.export;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.constants.GUIConstants;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonUnit;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class TreeExporter {


    private static final Logger logger = LogManager.getLogger(TreeExporter.class);
    private static final int BUFFER_SIZE = 4096;
    private final static String FILE_DATE_FORMAT = "yyyyMMddHHmmss";
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
    private final String NOTE = "n";
    private final String SAMPLE_VALUE = "v";
    private final ObjectMapper mapper = new ObjectMapper();

    public TreeExporter() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    }

    public Task<Void> importFromFile(File file, JEVisObject parent) {
        return new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    logger.info("==========================================");
                    logger.info("importFromFile: {} parent: {}", file, parent);

                    StringProperty messages = new SimpleStringProperty();
                    messages.addListener((observable, oldValue, newValue) -> updateMessage(newValue));

                    Path tmpDir = Files.createTempDirectory("import").toAbsolutePath();

                    ZipFile zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
                    // iterates over entries in the zip file
                    while (zipFileEntries.hasMoreElements()) {
                        ZipEntry entry = zipFileEntries.nextElement();

                        File destFile = new File(tmpDir.toFile(), entry.getName());
                        File destinationParent = destFile.getParentFile();

                        // create the parent directory structure if needed
                        destinationParent.mkdirs();

                        if (!entry.isDirectory()) {
                            // if the entry is a file, extracts it
                            extractFile(zipFile.getInputStream(entry), destFile.getAbsolutePath());
                        } else {
                            // if the entry is a directory, make the directory
                            File dir = new File(destFile.getAbsolutePath());
                            dir.mkdirs();
                        }
                    }
                    zipFile.close();

                    Map<JEVisAttribute, JsonNode> targets = new HashMap<>();
                    Map<Long, JEVisObject> createdObjects = new HashMap<>();
                    List<JEVisAttribute> fileAttributes = new ArrayList<>();

                    readTmpFilesToJEVis(messages, tmpDir, parent, createdObjects, targets, fileAttributes);

                    updateTargetAttributes(createdObjects, targets);

                    updateTargetsInFiles(createdObjects, fileAttributes);

                    logger.info("All Done");


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

    private void updateTargetsInFiles(Map<Long, JEVisObject> createdObjects, List<JEVisAttribute> fileAttributes) throws JEVisException, IOException {
        for (JEVisAttribute fileAttribute : fileAttributes) {

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            if (fileAttribute.getName().equals(JC.Analysis.a_DataModel) || fileAttribute.getName().equals(JC.DashboardAnalysis.a_DataModelFile)) {
                JEVisSample latestSample = fileAttribute.getLatestSample();
                if (latestSample != null) {
                    JEVisFile file = fileAttribute.getLatestSample().getValueAsFile();

                    JsonNode jsonNode = mapper.readTree(file.getBytes());
                    String jsonNodePrettyString = jsonNode.toPrettyString();

                    for (JsonNode id : jsonNode.findValues("id")) {
                        JEVisObject jeVisObject = createdObjects.get(id);
                        String oldValue = "\"id\" : " + id;
                        String newValue = "\"id\" : " + jeVisObject.getID();
                        jsonNodePrettyString.replaceAll(oldValue, newValue);
                    }

                    JEVisFileImp jsonFile = new JEVisFileImp(
                            file.getFilename(), jsonNodePrettyString.getBytes(StandardCharsets.UTF_8));
                    JEVisSample newSample = fileAttribute.buildSample(new DateTime(), jsonFile);
                    newSample.commit();
                }
            }
        }
    }

    private void updateTargetAttributes(Map<Long, JEVisObject> createdObjects, Map<JEVisAttribute, JsonNode> targets) {

        for (Map.Entry<JEVisAttribute, JsonNode> entry : targets.entrySet()) {
            JEVisAttribute jeVisAttribute = entry.getKey();
            JsonNode jsonNode = entry.getValue();
            List<JEVisSample> jeVisSamples = new ArrayList<>();

            for (JsonNode jSample : jsonNode) {
                try {
                    DateTime dateTime = DateTime.parse(jSample.get(SAMPLE_TS).asText());
                    String text = jSample.get(SAMPLE_VALUE).asText();
                    List<String> targetStrings = new ArrayList<>();

                    if (text.contains(TargetHelper.MULTI_SELECT_SEPARATOR)) {
                        targetStrings.addAll(TargetHelper.multiSelectStringToList(text));
                    } else {
                        targetStrings.add(text);
                    }

                    StringBuilder newTarget = new StringBuilder();
                    for (int i = 0; i < targetStrings.size(); i++) {
                        if (i > 0) newTarget.append(";");

                        String target = targetStrings.get(i);
                        int index = target.indexOf(":");
                        Long oldId = Long.parseLong(target.substring(0, index));
                        String attributeString = target.substring(index + 1);

                        newTarget.append(createdObjects.get(oldId).getID()).append(":").append(attributeString);
                    }

                    JEVisSample sample = jeVisAttribute.buildSample(dateTime, newTarget.toString(), jSample.get(NOTE).asText());
                    jeVisSamples.add(sample);
                } catch (Exception ex) {
                    logger.error("Error while creating Sample: {}", jSample, ex);
                }
            }

            try {
                jeVisAttribute.addSamples(jeVisSamples);
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    private void readTmpFilesToJEVis(StringProperty message, Path directory, JEVisObject parent, Map<Long, JEVisObject> createdObjects, Map<JEVisAttribute, JsonNode> targets, List<JEVisAttribute> fileAttributes) {
        try {

            Set<Path> objectFiles = listObjectFiles(directory);

            for (Path objectPath : objectFiles) {
                JsonNode jsonObjectNode = mapper.readTree(objectPath.toFile());

                logger.info("Create Object: {} [{}]", jsonObjectNode.get(OBJECT_NAME), jsonObjectNode.get(OBJECT_CLASS));

                message.setValue("Create Object " + jsonObjectNode.get(OBJECT_NAME) + "[" + jsonObjectNode.get(OBJECT_CLASS) + "]");
                JEVisClass objClass = parent.getDataSource().getJEVisClass(jsonObjectNode.get(OBJECT_CLASS).asText());

                if (objClass == null) {
                    logger.error("Class does not exist, skipping to next: {}", jsonObjectNode.get(OBJECT_CLASS));
                    continue;
                }

                if (!parent.getAllowedChildrenClasses().contains(objClass)) {
                    logger.error("Class '{}' is not allowed under: '{}'", objClass.getName(), parent.getJEVisClassName());
                    continue;
                }

                JEVisObject jeVisObject = parent.buildObject(jsonObjectNode.get(OBJECT_NAME).asText(), objClass);
                jeVisObject.commit();
                Thread.sleep(500);

                createdObjects.put(Long.parseLong(FilenameUtils.removeExtension(objectPath.getFileName().toString()).substring(2)), jeVisObject);

                if (jsonObjectNode.get(OBJECT_LANG).isArray()) {
                    for (JsonNode jsonNode1 : jsonObjectNode.get(OBJECT_LANG)) {
                        jsonNode1.fieldNames().forEachRemaining(s -> jeVisObject.setLocalName(s, jsonNode1.get(s).asText()));
                    }
                }
            }

            Set<Path> attributeFiles = listAttributeFiles(directory);

            for (Path attributePath : attributeFiles) {
                JsonNode jsonAttributeNode = mapper.readTree(attributePath.toFile());
                String attributeFileString = FilenameUtils.removeExtension(Paths.get(attributePath.getFileName().toString()).getFileName().toString()).replaceFirst("a_", "");
                int indexOf = attributeFileString.indexOf("_");
                Long oldObjectId = Long.parseLong(attributeFileString.substring(0, indexOf));
                JEVisObject correspondingJEVisObject = createdObjects.get(oldObjectId);

                logger.info("Creating Attribute: {}", jsonAttributeNode.get(ATTRIBUTE_NAME));
                JEVisAttribute jevisAttribute = correspondingJEVisObject.getAttribute(jsonAttributeNode.get(ATTRIBUTE_NAME).asText());

                if (jsonAttributeNode.get(ATTRIBUTE_UNIT) != null) {
                    try {
                        JsonNode unit = jsonAttributeNode.get(ATTRIBUTE_UNIT);
                        String unitString = mapper.writeValueAsString(unit);
                        JEVisUnitImp jevUnitImp = new JEVisUnitImp(mapper.readValue(unitString, org.jevis.commons.ws.json.JsonUnit.class));

                        jevisAttribute.setInputUnit(jevUnitImp);
                        jevisAttribute.setDisplayUnit(jevUnitImp);
                    } catch (Exception ex) {
                        logger.error("Unit Error: ", ex);
                    }
                }
                if (jsonAttributeNode.get(ATTRIBUTE_RATE) != null) {
                    try {
                        jevisAttribute.setInputSampleRate(Period.parse(jsonAttributeNode.get(ATTRIBUTE_RATE).asText()));
                        jevisAttribute.setDisplaySampleRate(Period.parse(jsonAttributeNode.get(ATTRIBUTE_RATE).asText()));
                    } catch (Exception e) {
                        logger.error("Rate Error: ", e);
                    }
                }
                jevisAttribute.commit();
                Thread.sleep(500);

                JsonNode jSamples = jsonAttributeNode.get(ATTRIBUTE_SAMPLES);
                if (jSamples != null && jSamples.isArray()) {
                    List<JEVisSample> jeVisSamples = new ArrayList<>();

                    JEVisType type = jevisAttribute.getType();
                    String guiDisplayType = type.getGUIDisplayType();

                    if (guiDisplayType != null && (guiDisplayType.equals(GUIConstants.TARGET_OBJECT.getId()) || guiDisplayType.equals(GUIConstants.TARGET_ATTRIBUTE.getId()))) {
                        targets.put(jevisAttribute, jSamples);
                        continue;
                    }

                    for (JsonNode jSample : jSamples) {
                        try {
                            DateTime dateTime = DateTime.parse(jSample.get(SAMPLE_TS).asText());
                            JEVisSample sample = jevisAttribute.buildSample(dateTime, jSample.get(SAMPLE_VALUE).asText(), jSample.get(NOTE).asText());
                            jeVisSamples.add(sample);
                        } catch (Exception ex) {
                            logger.error("Error while creating Sample: {}", jSample, ex);
                        }
                    }

                    if (!jeVisSamples.isEmpty()) {
                        jevisAttribute.addSamples(jeVisSamples);
                    }

                }

                fileAttributes.add(jevisAttribute);
            }

            Set<Path> folderPaths = listFileFolders(directory);

            for (Path folderPath : folderPaths) {
                String objectString = folderPath.getFileName().toString().substring(2);

                String folderName = Paths.get(objectString).getFileName().toString();
                int indexOf = folderName.indexOf("_");

                Long oldObjectId = Long.parseLong(folderName.substring(0, indexOf));
                String attributeString = folderName.substring(indexOf + 1);

                JEVisObject correspondingJEVisObject = createdObjects.get(oldObjectId);
                JEVisAttribute jevisAttribute = correspondingJEVisObject.getAttribute(attributeString);
                List<JEVisSample> fileSamples = new ArrayList<>();

                Set<Path> fileDateFolders = listFileDateTimeFolders(folderPath);
                for (Path fileDateFolderPath : fileDateFolders) {
                    try {
                        for (File listFile : fileDateFolderPath.toFile().listFiles()) {
                            DateTime dateTime = DateTime.parse(fileDateFolderPath.getFileName().toString(), DateTimeFormat.forPattern(FILE_DATE_FORMAT));
                            JEVisFile jeVisFile = new JEVisFileImp(listFile.getName(), listFile);
                            fileSamples.add(jevisAttribute.buildSample(dateTime, jeVisFile));
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }

                jevisAttribute.addSamples(fileSamples);
            }

            Set<Path> objectFolders = listObjectFolders(directory);

            for (Path objectFolderPath : objectFolders) {
                Long oldObjectId = Long.parseLong(objectFolderPath.getFileName().toString());

                JEVisObject correspondingJEVisObject = createdObjects.get(oldObjectId);

                readTmpFilesToJEVis(message, objectFolderPath, correspondingJEVisObject, createdObjects, targets, fileAttributes);
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

    public Set<Path> listObjectFiles(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(file -> !Files.isDirectory(file) && file.getFileName().toString().startsWith("o"))
                    .collect(Collectors.toSet());
        }
    }

    public Set<Path> listAttributeFiles(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(file -> !Files.isDirectory(file) && file.getFileName().toString().startsWith("a"))
                    .collect(Collectors.toSet());
        }
    }

    public Set<Path> listObjectFolders(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(path -> {
                        Long l = null;
                        try {
                            l = Long.parseLong(path.getFileName().toString());
                        } catch (Exception ignored) {
                        }
                        return Files.isDirectory(path) && l != null;
                    })
                    .collect(Collectors.toSet());
        }
    }

    public Set<Path> listFileFolders(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(path -> Files.isDirectory(path) && path.getFileName().toString().startsWith("a"))
                    .collect(Collectors.toSet());
        }
    }

    public Set<Path> listFileDateTimeFolders(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(path -> Files.isDirectory(path)
                            && DateTime.parse(path.getFileName().toString(), DateTimeFormat.forPattern(FILE_DATE_FORMAT)) != null)
                    .collect(Collectors.toSet());
        }
    }

    private void extractFile(InputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
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

                    AtomicReference<Integer> jobNo = new AtomicReference<>(0);
                    int jobCount = allObjects.size();

                    OutputStream outputStream = Files.newOutputStream(file.toPath());
                    ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
                    writeZipOutputStream(zipOutputStream, objects, "", message, jobNo, jobCount);
                    zipOutputStream.close();
                    outputStream.close();

                    succeeded();
                } catch (Exception ex) {
                    logger.error(ex);
                    failed();
                } finally {
                    done();
                }
                return null;
            }
        };
    }

    private void writeZipOutputStream(ZipOutputStream zipOutputStream, List<JEVisObject> objects, String folder, StringProperty message, AtomicReference<Integer> jobNo, int jobCount) throws Exception {
        for (JEVisObject object : objects) {
            jobNo.set(jobNo.get() + 1);
            message.set("Prepare Export Job [" + jobNo.get() + "/" + jobCount + "] object: [" + object.getID() + "] " + object.getName());
            ZipEntry objectZipEntry = new ZipEntry(folder + "o_" + object.getID() + ".json");
            zipOutputStream.putNextEntry(objectZipEntry);
            ObjectNode objectNode = toJson(object);
            mapper.writeValue(zipOutputStream, objectNode);

            for (JEVisAttribute jeVisAttribute : object.getAttributes()) {

                if (jeVisAttribute.getPrimitiveType() != JEVisConstants.PrimitiveType.FILE && jeVisAttribute.getPrimitiveType() != JEVisConstants.PrimitiveType.PASSWORD_PBKDF2) {
                    ZipEntry attributeZipEntry = new ZipEntry(folder + "a_" + object.getID() + "_" + jeVisAttribute.getName() + ".json");
                    zipOutputStream.putNextEntry(attributeZipEntry);
                    ObjectNode attributeNode = toJson(jeVisAttribute);
                    mapper.writeValue(zipOutputStream, attributeNode);
                } else if (jeVisAttribute.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
                    if (jeVisAttribute.hasSample()) {
                        List<JEVisSample> allSamples = jeVisAttribute.getAllSamples();
                        for (JEVisSample sample : allSamples) {
                            JEVisFile sampleValueAsFile = sample.getValueAsFile();
                            ZipEntry sampleFileZipEntry = new ZipEntry(folder + "a_" + object.getID() + "_" + jeVisAttribute.getName() + "/" + sample.getTimestamp().toString(FILE_DATE_FORMAT) + "/" + sampleValueAsFile.getFilename());
                            zipOutputStream.putNextEntry(sampleFileZipEntry);
                            zipOutputStream.write(sampleValueAsFile.getBytes());
                        }
                    }
                }
            }

            String newFolder = folder + object.getID() + "/";
            writeZipOutputStream(zipOutputStream, object.getChildren(), newFolder, message, jobNo, jobCount);
        }
    }

    public ObjectNode toJson(JEVisAttribute jeVisAttribute) throws Exception {
        ObjectNode attributeNode = JsonNodeFactory.instance.objectNode();
        attributeNode.put(ATTRIBUTE_NAME, jeVisAttribute.getName());
        logger.info("Created attribute {} from object {}", jeVisAttribute.getName(), jeVisAttribute.getObjectID());

        try {
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
                List<JEVisSample> allSamples = jeVisAttribute.getAllSamples();
                logger.info("Found {} samples on attribute {}. Creating samples.", allSamples.size(), jeVisAttribute.getName());
                for (JEVisSample jeVisSample : allSamples) {
                    if (jeVisAttribute.getPrimitiveType() == JEVisConstants.PrimitiveType.BOOLEAN
                            || jeVisAttribute.getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE
                            || jeVisAttribute.getPrimitiveType() == JEVisConstants.PrimitiveType.LONG
                            || jeVisAttribute.getPrimitiveType() == JEVisConstants.PrimitiveType.SELECTION
                            || jeVisAttribute.getPrimitiveType() == JEVisConstants.PrimitiveType.MULTI_SELECTION
                            || jeVisAttribute.getPrimitiveType() == JEVisConstants.PrimitiveType.STRING) {
                        try {
                            ObjectNode sampleNode = mapper.createObjectNode();

                            sampleNode.put(SAMPLE_TS, jeVisSample.getTimestamp().toString());
                            sampleNode.put(SAMPLE_VALUE, jeVisSample.getValueAsString());
                            sampleNode.put(NOTE, jeVisSample.getNote());

                            jSamples.add(sampleNode);
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    } else if (jeVisAttribute.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
                        // TODO File Export
                    } else if (jeVisAttribute.getPrimitiveType() == JEVisConstants.PrimitiveType.PASSWORD_PBKDF2) {
                        // TODO Password Export
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return attributeNode;
    }


    public ObjectNode toJson(JEVisObject object) {

        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put(OBJECT_NAME, object.getName());
        logger.info("Created object {}", object.getName());

        try {
            objectNode.put(OBJECT_CLASS, object.getJEVisClassName());

            ArrayNode arrayNode = objectNode.putArray(OBJECT_LANG);

            for (Map.Entry<String, String> entry : object.getLocalNameList().entrySet()) {
                try {
                    String lang = entry.getKey();
                    String translatedName = entry.getValue();

                    ObjectNode langNode = JsonNodeFactory.instance.objectNode();
                    langNode.put(lang, translatedName);
                    arrayNode.add(langNode);
                } catch (Exception ex) {
                    logger.error("Error while exporting language: {} ", entry.getKey(), ex);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return objectNode;

    }
}
