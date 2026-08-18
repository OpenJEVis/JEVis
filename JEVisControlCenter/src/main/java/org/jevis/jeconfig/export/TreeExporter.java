package org.jevis.jeconfig.export;

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
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.jeconfig.application.Chart.data.AnalysisHandler;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.application.Chart.data.DataModel;
import org.jevis.jeconfig.plugin.accounting.AccountingTemplateHandler;
import org.jevis.jeconfig.plugin.accounting.SelectionTemplate;
import org.jevis.jeconfig.plugin.scada.SCADAPlugin;
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
    private static final String FILE_DATE_FORMAT = "yyyyMMddHHmmss";
    private static final String RELATIONSHIPS_FILE = "relationships.json";

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

    /**
     * Creates a JavaFX {@link Task} that imports a previously exported {@code .jex} archive into the
     * JEVis tree as children of {@code parent}.
     *
     * <p>The import runs four post-processing phases after all objects and their attribute samples
     * have been created, to remap old object IDs (from the export system) to the new IDs assigned
     * during import:
     * <ol>
     *   <li><b>String target attributes</b> ({@link GUIConstants#TARGET_OBJECT} /
     *       {@link GUIConstants#TARGET_ATTRIBUTE}): TargetHelper strings of the form
     *       {@code "objectId:attributeName"} are resolved via {@link #updateTargetAttributes}.</li>
     *   <li><b>Long target attributes</b> ({@link GUIConstants#BASIC_TARGET_LONG}): Raw Long
     *       object IDs stored in attributes with the "Target Selector" display type are resolved
     *       via {@link #updateTargetLongAttributes}.</li>
     *   <li><b>File-embedded IDs</b>: Specific file attributes whose content references object
     *       IDs by JSON key are post-processed by {@link #updateTargetsInFiles}. Covered types:
     *       Analysis File, Dashboard Data Model File, Accounting Template File, SCADA Data Model.</li>
     *   <li><b>Relationships</b>: If the archive contains {@value #RELATIONSHIPS_FILE}, access-control
     *       relationships (OWNER, MEMBER_*, ROLE_*) are recreated via {@link #importRelationships}.
     *       Missing the file is silently ignored for backward compatibility with older archives.
     *       Note: {@code PASSWORD_PBKDF2} attributes are intentionally excluded from export, so
     *       imported User objects will have no password — administrators must reset them manually.</li>
     * </ol>
     *
     * @param file   the {@code .jex} archive to import
     * @param parent the JEVis object that will be the parent of all imported root objects
     * @return a Task that performs the import; must be submitted to a thread or executor
     */
    public Task importFromFile(File file, JEVisObject parent) {
        return new Task() {
            @Override
            protected Void call() {
                try {
                    logger.info("==========================================");
                    logger.info("importFromFile: {} parent: {}", file, parent);

                    StringProperty messages = new SimpleStringProperty();
                    messages.addListener((observable, oldValue, newValue) -> updateMessage(newValue));

                    Path tmpDir = Files.createTempDirectory("import").toAbsolutePath();

                    ZipFile zipFile = new ZipFile(file);
                    Enumeration zipFileEntries = zipFile.entries();

                    while (zipFileEntries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                        File destFile = new File(tmpDir.toFile(), entry.getName());
                        File destinationParent = destFile.getParentFile();

                        destinationParent.mkdirs();

                        if (!entry.isDirectory()) {
                            extractFile(zipFile.getInputStream(entry), destFile.getAbsolutePath());
                        } else {
                            File dir = new File(destFile.getAbsolutePath());
                            dir.mkdirs();
                        }
                    }
                    zipFile.close();

                    Map<JEVisAttribute, JsonNode> targets = new HashMap<>();
                    Map<Long, JEVisObject> createdObjects = new HashMap<>();
                    List<JEVisAttribute> fileAttributes = new ArrayList<>();
                    Map<JEVisAttribute, JsonNode> longTargets = new HashMap<>();

                    readTmpFilesToJEVis(messages, tmpDir, parent, createdObjects, targets, fileAttributes, longTargets);

                    updateTargetAttributes(createdObjects, targets);
                    updateTargetLongAttributes(createdObjects, longTargets);
                    updateTargetsInFiles(parent.getDataSource(), createdObjects, fileAttributes);
                    importRelationships(parent.getDataSource(), tmpDir, createdObjects);

                    logger.info("All Done");
                    succeeded();
                } catch (Exception ex) {
                    failed();
                    logger.error("Failed extracting files from archive.", ex);
                } finally {
                    done();
                }

                return null;
            }
        };
    }

    /**
     * Post-import phase C: remaps object IDs embedded inside the content of specific file and
     * string attributes whose format is known to contain JEVis object ID references.
     *
     * <p>Handled attribute types:
     * <ul>
     *   <li>{@link JC.Analysis#a_AnalysisFile} — remaps {@code id} and {@code calculationId} in each
     *       {@link ChartData} of every chart model in the analysis.</li>
     *   <li>{@link JC.DashboardAnalysis#a_DataModelFile} — remaps JSON keys {@code "id"},
     *       {@code "calculationId"}, {@code "dashboardObject"}, and {@code "objectID"} in the
     *       dashboard data model JSON.</li>
     *   <li>{@link JC.AccountingConfiguration#a_TemplateFile} — remaps {@code objectID} on each
     *       {@code TemplateInput}, the {@code templateSelection} ID, and TargetHelper target strings
     *       on linked {@code TemplateOutput} entries.</li>
     *   <li>SCADA {@code "Data Model"} STRING attribute — remaps {@code "objectID"} values embedded
     *       in the JSON stored as the attribute's string value.</li>
     * </ul>
     *
     * <p>For all cases, if an old ID is not present in {@code createdObjects} (it was a cross-tree
     * reference to an object that already exists on the target system), a live datasource lookup is
     * attempted as a fallback before logging a warning and skipping the entry.
     *
     * @param ds             live datasource used for cross-tree ID fallback lookups
     * @param createdObjects mapping of old (export) object IDs to newly created {@link JEVisObject}s
     * @param fileAttributes all non-target attributes collected during import; only those with
     *                       recognized names are processed
     */
    private void updateTargetsInFiles(JEVisDataSource ds, Map<Long, JEVisObject> createdObjects, List<JEVisAttribute> fileAttributes) throws JEVisException, IOException {
        for (JEVisAttribute fileAttribute : fileAttributes) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            if (fileAttribute.getName().equals(JC.Analysis.a_AnalysisFile)) {
                AnalysisHandler analysisHandler = new AnalysisHandler();
                DataModel dataModel = new DataModel();
                analysisHandler.loadDataModel(fileAttribute.getObject(), dataModel);

                for (ChartModel chartModel : dataModel.getChartModels()) {
                    for (ChartData chartData : chartModel.getChartData()) {
                        // Remap primary data object ID
                        long oldId = chartData.getId();
                        JEVisObject resolved = resolveObject(ds, createdObjects, oldId);
                        if (resolved != null) {
                            chartData.setId(resolved.getID());
                        } else {
                            logger.warn("Cannot resolve ChartData id {} in Analysis File of object {}",
                                    oldId, fileAttribute.getObject().getID());
                        }

                        // Remap calculation object ID when the series uses a formula
                        if (chartData.isCalculation() && chartData.getCalculationId() > 0) {
                            long oldCalcId = chartData.getCalculationId();
                            JEVisObject resolvedCalc = resolveObject(ds, createdObjects, oldCalcId);
                            if (resolvedCalc != null) {
                                chartData.setCalculationId(resolvedCalc.getID());
                            } else {
                                logger.warn("Cannot resolve calculationId {} in Analysis File of object {}",
                                        oldCalcId, fileAttribute.getObject().getID());
                            }
                        }
                    }
                }

                analysisHandler.saveDataModel(fileAttribute.getObject(), dataModel);

            } else if (fileAttribute.getName().equals(JC.DashboardAnalysis.a_DataModelFile)) {
                JEVisSample latestSample = fileAttribute.getLatestSample();

                if (latestSample != null) {
                    JEVisFile file = latestSample.getValueAsFile();

                    if (file != null && file.getBytes() != null && file.getBytes().length > 0) {
                        JsonNode jsonNode = mapper.readTree(file.getBytes());
                        String json = jsonNode.toPrettyString();

                        // Remap "id" — primary data object references in chart data
                        for (JsonNode id : jsonNode.findValues("id")) {
                            JEVisObject obj = resolveObject(ds, createdObjects, id.asLong());
                            if (obj != null) {
                                json = json.replace("\"id\" : " + id, "\"id\" : " + obj.getID());
                            }
                        }

                        // Remap "calculationId" — formula/calculation object references
                        for (JsonNode calcId : jsonNode.findValues("calculationId")) {
                            long oldCalcId = calcId.asLong(-1);
                            if (oldCalcId > 0) {
                                JEVisObject obj = resolveObject(ds, createdObjects, oldCalcId);
                                if (obj != null) {
                                    json = json.replace("\"calculationId\" : " + oldCalcId,
                                            "\"calculationId\" : " + obj.getID());
                                }
                            }
                        }

                        // Remap "dashboardObject" — DashboardLinkerNode references to other dashboards
                        for (JsonNode dashObj : jsonNode.findValues("dashboardObject")) {
                            long oldDashId = dashObj.asLong(-1);
                            if (oldDashId > 0) {
                                JEVisObject obj = resolveObject(ds, createdObjects, oldDashId);
                                if (obj != null) {
                                    json = json.replace("\"dashboardObject\" : " + oldDashId,
                                            "\"dashboardObject\" : " + obj.getID());
                                }
                            }
                        }

                        // Remap "objectID" — ImageConfig file-object references (stored as JSON string)
                        for (JsonNode objIdNode : jsonNode.findValues("objectID")) {
                            long oldObjId = objIdNode.asLong(-1);
                            if (oldObjId > 0) {
                                JEVisObject obj = resolveObject(ds, createdObjects, oldObjId);
                                if (obj != null) {
                                    // ImageConfig serializes the Long as a JSON string value
                                    json = json.replace("\"objectID\" : \"" + oldObjId + "\"",
                                            "\"objectID\" : \"" + obj.getID() + "\"");
                                    // Handle numeric form used by other widgets
                                    json = json.replace("\"objectID\" : " + oldObjId,
                                            "\"objectID\" : " + obj.getID());
                                }
                            }
                        }

                        JEVisFileImp jsonFile = new JEVisFileImp(
                                file.getFilename(),
                                json.getBytes(StandardCharsets.UTF_8)
                        );
                        JEVisSample newSample = fileAttribute.buildSample(new DateTime(), jsonFile);
                        newSample.commit();
                    }
                }

            } else if (fileAttribute.getName().equals(JC.AccountingConfiguration.a_TemplateFile)) {
                // Covers both JC.AccountingConfiguration and JC.ResultCalculationTemplate,
                // which both use the same attribute name and JSON format.
                try {
                    AccountingTemplateHandler handler = new AccountingTemplateHandler();
                    handler.setTemplateObject(fileAttribute.getObject());
                    SelectionTemplate template = handler.getSelectionTemplate();

                    if (template != null) {
                        // Remap the selected template definition object
                        Long oldTemplateId = template.getTemplateSelection();
                        if (oldTemplateId != null && oldTemplateId > 0) {
                            JEVisObject resolved = resolveObject(ds, createdObjects, oldTemplateId);
                            if (resolved != null) {
                                template.setTemplateSelection(resolved.getID());
                            } else {
                                logger.warn("Cannot resolve templateSelection id {} in Template File of object {}",
                                        oldTemplateId, fileAttribute.getObject().getID());
                            }
                        }

                        // Remap objectID on each selected data input
                        for (org.jevis.jeconfig.plugin.dtrc.TemplateInput input : template.getSelectedInputs()) {
                            Long oldId = input.getObjectID();
                            if (oldId != null && oldId > 0) {
                                JEVisObject resolved = resolveObject(ds, createdObjects, oldId);
                                if (resolved != null) {
                                    input.setObjectID(resolved.getID());
                                } else {
                                    logger.warn("Cannot resolve TemplateInput objectID {} in Template File of object {}",
                                            oldId, fileAttribute.getObject().getID());
                                }
                            }
                        }

                        // Remap TargetHelper target strings on linked outputs
                        for (org.jevis.jeconfig.plugin.dtrc.TemplateOutput output : template.getLinkedOutputs()) {
                            if (Boolean.TRUE.equals(output.getLink()) && output.getTarget() != null
                                    && !output.getTarget().isEmpty()) {
                                try {
                                    output.setTarget(resolveTargetValue(createdObjects, fileAttribute, output.getTarget()));
                                } catch (Exception e) {
                                    logger.warn("Cannot remap accounting output target '{}' on object {}",
                                            output.getTarget(), fileAttribute.getObject().getID(), e);
                                }
                            }
                        }

                        handler.setSelectionTemplate(template);
                        byte[] updatedBytes = objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsBytes(handler.toJsonNode());
                        JEVisFileImp updatedFile = new JEVisFileImp("template.json", updatedBytes);
                        JEVisSample newSample = fileAttribute.buildSample(new DateTime(), updatedFile);
                        newSample.commit();
                    }
                } catch (Exception e) {
                    logger.error("Failed to remap Template File for object {}",
                            fileAttribute.getObject().getID(), e);
                }

            } else if (fileAttribute.getName().equals(SCADAPlugin.ATTRIBUTE_DATA_MODEL)
                    && fileAttribute.getObject().getJEVisClassName().equals(SCADAPlugin.CLASS_SCADA_ANALYSIS)) {
                // SCADA "Data Model" is a STRING attribute whose value is JSON containing objectID fields
                try {
                    JEVisSample latestSample = fileAttribute.getLatestSample();
                    if (latestSample != null) {
                        String jsonString = latestSample.getValueAsString();
                        if (jsonString != null && !jsonString.isEmpty()) {
                            JsonNode rootNode = objectMapper.readTree(jsonString);
                            String json = rootNode.toPrettyString();
                            boolean modified = false;

                            for (JsonNode objIdNode : rootNode.findValues("objectID")) {
                                long oldId = objIdNode.asLong(-1);
                                if (oldId > 0) {
                                    JEVisObject resolved = resolveObject(ds, createdObjects, oldId);
                                    if (resolved != null) {
                                        json = json.replace("\"objectID\" : " + oldId,
                                                "\"objectID\" : " + resolved.getID());
                                        modified = true;
                                    } else {
                                        logger.warn("Cannot resolve SCADA objectID {} in Data Model of object {}",
                                                oldId, fileAttribute.getObject().getID());
                                    }
                                }
                            }

                            if (modified) {
                                JEVisSample newSample = fileAttribute.buildSample(new DateTime(), json);
                                newSample.commit();
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to remap SCADA Data Model for object {}",
                            fileAttribute.getObject().getID(), e);
                }
            }
        }
    }

    /**
     * Post-import phase B: remaps TargetHelper string values in attributes with the
     * {@link GUIConstants#TARGET_OBJECT} or {@link GUIConstants#TARGET_ATTRIBUTE} display type.
     *
     * <p>TargetHelper strings have the form {@code "objectId:attributeName"} or a semicolon-separated
     * list for multi-select attributes. Each object ID is resolved first from {@code createdObjects}
     * (an object that was re-created during this import), then via a live datasource lookup for
     * cross-tree references that already exist on the target system.
     *
     * @param createdObjects mapping of old (export) object IDs to newly created {@link JEVisObject}s
     * @param targets        deferred target attributes and their exported sample JSON nodes
     */
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
                        if (i > 0) {
                            newTarget.append(TargetHelper.MULTI_SELECT_SEPARATOR);
                        }

                        newTarget.append(resolveTargetValue(createdObjects, jeVisAttribute, targetStrings.get(i)));
                    }

                    JEVisSample sample = jeVisAttribute.buildSample(
                            dateTime,
                            newTarget.toString(),
                            jSample.get(NOTE).asText()
                    );
                    jeVisSamples.add(sample);
                } catch (Exception ex) {
                    logger.error("Error while creating Target sample: {}", jSample, ex);
                }
            }

            try {
                if (!jeVisSamples.isEmpty()) {
                    jeVisAttribute.addSamples(jeVisSamples);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    /**
     * Post-import phase B2: remaps raw Long object IDs stored in attributes with the
     * {@link GUIConstants#BASIC_TARGET_LONG} ("Target Selector") display type.
     *
     * <p>These attributes (e.g. "Source Id" on JEVis Channel objects) hold a single Long value that
     * is a JEVis object ID. Each old ID is resolved first from {@code createdObjects}, then via a
     * live datasource lookup for cross-tree references. Entries that cannot be resolved are logged
     * and skipped.
     *
     * @param createdObjects mapping of old (export) object IDs to newly created {@link JEVisObject}s
     * @param longTargets    deferred BASIC_TARGET_LONG attributes and their exported sample JSON nodes
     */
    private void updateTargetLongAttributes(Map<Long, JEVisObject> createdObjects, Map<JEVisAttribute, JsonNode> longTargets) {
        for (Map.Entry<JEVisAttribute, JsonNode> entry : longTargets.entrySet()) {
            JEVisAttribute jeVisAttribute = entry.getKey();
            JsonNode jSamples = entry.getValue();
            List<JEVisSample> samples = new ArrayList<>();

            for (JsonNode jSample : jSamples) {
                try {
                    DateTime ts = DateTime.parse(jSample.get(SAMPLE_TS).asText());
                    long oldId = jSample.get(SAMPLE_VALUE).asLong();

                    JEVisObject resolved = createdObjects.get(oldId);
                    if (resolved == null) {
                        try {
                            resolved = jeVisAttribute.getObject().getDataSource().getObject(oldId);
                        } catch (Exception ignored) {
                        }
                    }

                    if (resolved != null) {
                        samples.add(jeVisAttribute.buildSample(ts, resolved.getID(),
                                jSample.get(NOTE).asText()));
                    } else {
                        logger.warn("Cannot resolve BASIC_TARGET_LONG id {} for attribute '{}' on object {}",
                                oldId, jeVisAttribute.getName(), jeVisAttribute.getObject().getID());
                    }
                } catch (Exception ex) {
                    logger.error("Error remapping BASIC_TARGET_LONG sample: {}", jSample, ex);
                }
            }

            try {
                if (!samples.isEmpty()) {
                    jeVisAttribute.addSamples(samples);
                }
            } catch (Exception e) {
                logger.error("Failed to commit BASIC_TARGET_LONG samples for attribute '{}'",
                        jeVisAttribute.getName(), e);
            }
        }
    }

    /**
     * Resolves a TargetHelper string from the exported format (old object ID) to the format valid
     * on the target system (new object ID).
     *
     * <p>The TargetHelper format is {@code "objectId:attributeName"}, or just {@code "objectId"} if
     * the attribute name defaults to {@code "Value"}. Resolution tries {@code createdObjects} first
     * (the object was re-created during this import), then falls back to a live datasource lookup
     * for objects that already exist on the target system with the same numeric ID.
     *
     * @param createdObjects  mapping of old (export) IDs to newly created objects
     * @param targetAttribute the attribute being resolved; used to obtain the datasource for fallback
     * @param rawTarget       the TargetHelper string from the export, e.g. {@code "12345:Value"}
     * @return the TargetHelper string with the ID updated to the target system's ID
     * @throws JEVisException           if the object cannot be resolved on the target system
     * @throws IllegalArgumentException if {@code rawTarget} is null or empty
     */
    private String resolveTargetValue(Map<Long, JEVisObject> createdObjects,
                                      JEVisAttribute targetAttribute,
                                      String rawTarget) throws JEVisException {
        if (rawTarget == null || rawTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty target value");
        }

        String target = rawTarget.trim();
        int index = target.indexOf(":");

        long objectId;
        String attributeName;

        if (index >= 0) {
            objectId = Long.parseLong(target.substring(0, index).trim());
            attributeName = target.substring(index + 1).trim();
        } else {
            objectId = Long.parseLong(target);
            attributeName = "Value";
        }

        if (attributeName == null || attributeName.isEmpty()) {
            attributeName = "Value";
        }

        JEVisObject remappedObject = createdObjects.get(objectId);
        if (remappedObject != null) {
            return remappedObject.getID() + ":" + attributeName;
        }

        JEVisObject existingObject = targetAttribute
                .getObject()
                .getDataSource()
                .getObject(objectId);

        if (existingObject != null) {
            return existingObject.getID() + ":" + attributeName;
        }

        throw new JEVisException("Cannot resolve target object id: " + objectId, 145415);
    }

    /**
     * Reads the extracted ZIP contents from {@code directory} recursively and creates JEVis objects
     * and attribute samples on the server.
     *
     * <p>Three types of entries are processed per directory level:
     * <ol>
     *   <li><b>Object JSON files</b> ({@code o_<id>.json}): a new JEVis object is created under
     *       {@code parent}; the mapping {@code oldId → newObject} is stored in {@code createdObjects}.</li>
     *   <li><b>Attribute JSON files</b> ({@code a_<id>_<attrName>.json}): non-FILE, non-PASSWORD
     *       attribute metadata and samples are applied to the corresponding newly created object.
     *       Attributes with display types {@link GUIConstants#TARGET_OBJECT},
     *       {@link GUIConstants#TARGET_ATTRIBUTE}, or {@link GUIConstants#BASIC_TARGET_LONG} are
     *       deferred to {@code targets} or {@code longTargets} for ID remapping after all objects
     *       are created. All other attributes are added to {@code fileAttributes} for potential
     *       content-level remapping in {@link #updateTargetsInFiles}.</li>
     *   <li><b>Attribute directories</b> ({@code a_<id>_<attrName>/}): FILE-type attribute samples
     *       are reconstructed from the contained timestamp sub-directories and files. These attributes
     *       are also added to {@code fileAttributes} so {@link #updateTargetsInFiles} can post-process
     *       their content (e.g. Analysis File, Data Model File, Template File).</li>
     * </ol>
     * Numeric sub-directories ({@code <id>/}) trigger recursive calls for child objects.
     *
     * @param message        property used to push status messages to the UI task
     * @param directory      current directory to process
     * @param parent         JEVis object under which new objects at this level are created
     * @param createdObjects accumulates old-ID → new-object mappings across all recursive calls
     * @param targets        accumulates deferred TARGET_OBJECT / TARGET_ATTRIBUTE attribute samples
     * @param fileAttributes accumulates all non-deferred attributes for {@link #updateTargetsInFiles}
     * @param longTargets    accumulates deferred BASIC_TARGET_LONG attribute samples
     */
    private void readTmpFilesToJEVis(StringProperty message,
                                     Path directory,
                                     JEVisObject parent,
                                     Map<Long, JEVisObject> createdObjects,
                                     Map<JEVisAttribute, JsonNode> targets,
                                     List<JEVisAttribute> fileAttributes,
                                     Map<JEVisAttribute, JsonNode> longTargets) {
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

                createdObjects.put(
                        Long.parseLong(FilenameUtils.removeExtension(objectPath.getFileName().toString()).substring(2)),
                        jeVisObject
                );

                if (jsonObjectNode.get(OBJECT_LANG) != null && jsonObjectNode.get(OBJECT_LANG).isArray()) {
                    for (JsonNode jsonNode1 : jsonObjectNode.get(OBJECT_LANG)) {
                        jsonNode1.fieldNames().forEachRemaining(s -> jeVisObject.setLocalName(s, jsonNode1.get(s).asText()));
                    }
                }
            }

            Set<Path> attributeFiles = listAttributeFiles(directory);

            for (Path attributePath : attributeFiles) {
                JsonNode jsonAttributeNode = mapper.readTree(attributePath.toFile());
                String attributeFileString = FilenameUtils
                        .removeExtension(Paths.get(attributePath.getFileName().toString()).getFileName().toString())
                        .replaceFirst("a_", "");
                int indexOf = attributeFileString.indexOf("_");
                Long oldObjectId = Long.parseLong(attributeFileString.substring(0, indexOf));

                JEVisObject correspondingJEVisObject = createdObjects.get(oldObjectId);
                if (correspondingJEVisObject == null) {
                    logger.error("Could not find created object for imported id {}", oldObjectId);
                    continue;
                }

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

                    if (guiDisplayType != null) {
                        if (guiDisplayType.equals(GUIConstants.TARGET_OBJECT.getId())
                                || guiDisplayType.equals(GUIConstants.TARGET_ATTRIBUTE.getId())) {
                            targets.put(jevisAttribute, jSamples);
                            continue;
                        }
                        if (guiDisplayType.equals(GUIConstants.BASIC_TARGET_LONG.getId())) {
                            longTargets.put(jevisAttribute, jSamples);
                            continue;
                        }
                    }

                    for (JsonNode jSample : jSamples) {
                        try {
                            DateTime dateTime = DateTime.parse(jSample.get(SAMPLE_TS).asText());
                            JEVisSample sample = jevisAttribute.buildSample(
                                    dateTime,
                                    jSample.get(SAMPLE_VALUE).asText(),
                                    jSample.get(NOTE).asText()
                            );
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
                        File[] files = fileDateFolderPath.toFile().listFiles();
                        if (files == null) {
                            continue;
                        }

                        for (File listFile : files) {
                            DateTime dateTime = DateTime.parse(
                                    fileDateFolderPath.getFileName().toString(),
                                    DateTimeFormat.forPattern(FILE_DATE_FORMAT)
                            );
                            JEVisFile jeVisFile = new JEVisFileImp(listFile.getName(), listFile);
                            fileSamples.add(jevisAttribute.buildSample(dateTime, jeVisFile));
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }

                jevisAttribute.addSamples(fileSamples);
                // Register FILE attributes for content-level ID remapping (e.g. Analysis File,
                // Data Model File, Template File) — these go through updateTargetsInFiles().
                fileAttributes.add(jevisAttribute);
            }

            Set<Path> objectFolders = listObjectFolders(directory);

            for (Path objectFolderPath : objectFolders) {
                Long oldObjectId = Long.parseLong(objectFolderPath.getFileName().toString());
                JEVisObject correspondingJEVisObject = createdObjects.get(oldObjectId);

                if (correspondingJEVisObject == null) {
                    logger.error("Could not find created parent object for imported folder id {}", oldObjectId);
                    continue;
                }

                readTmpFilesToJEVis(message, objectFolderPath, correspondingJEVisObject, createdObjects, targets, fileAttributes, longTargets);
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
        int read;

        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }

        bos.close();
    }

    /**
     * Creates a JavaFX {@link Task} that exports the given objects and all their descendants into a
     * {@code .jex} ZIP archive at {@code file}.
     *
     * <p>The archive contains:
     * <ul>
     *   <li>One {@code o_<id>.json} per object with its name, class, and localized names.</li>
     *   <li>One {@code a_<id>_<attrName>.json} per non-FILE, non-PASSWORD attribute with metadata
     *       and all samples.</li>
     *   <li>One {@code a_<id>_<attrName>/<timestamp>/<filename>} entry per FILE attribute sample.</li>
     *   <li>An optional {@value #RELATIONSHIPS_FILE} in the archive root containing OWNER,
     *       MEMBER_*, and ROLE_* relationships for all exported objects (see
     *       {@link #exportRelationships}).</li>
     * </ul>
     * {@code PASSWORD_PBKDF2} attributes are intentionally excluded from export.
     *
     * @param file    output file path for the {@code .jex} archive
     * @param objects root objects to export; all their descendants are included recursively
     * @return a Task that performs the export; must be submitted to a thread or executor
     */
    public Task exportToFileTask(File file, List<JEVisObject> objects) {
        return new Task() {
            @Override
            protected Void call() {
                try {
                    StringProperty message = new SimpleStringProperty();
                    message.addListener((observable, oldValue, newValue) -> updateMessage(newValue));

                    if (!objects.isEmpty()) {
                        try {
                            // Bulk-load every attribute for every object in one request, so the
                            // per-object getAttributes() calls below hit a warm cache instead of
                            // each triggering their own network round trip.
                            objects.get(0).getDataSource().getAttributes();
                        } catch (Exception e) {
                            logger.warn("Could not bulk-preload attributes; falling back to per-object loading", e);
                        }
                    }

                    Map<Long, List<JEVisObject>> childrenMap = buildChildrenMap(objects);
                    List<JEVisObject> allObjects = flattenTree(objects, childrenMap);

                    AtomicReference<Integer> jobNo = new AtomicReference<>(0);
                    int jobCount = allObjects.size();

                    OutputStream outputStream = Files.newOutputStream(file.toPath());
                    ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

                    writeZipOutputStream(zipOutputStream, objects, "", message, jobNo, jobCount, childrenMap);
                    exportRelationships(zipOutputStream, allObjects);

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

    /**
     * Walks {@code roots} and all their descendants exactly once, fetching each object's children
     * a single time via {@link JEVisObject#getChildren()} and caching the result. Used so that both
     * the job-count/relationship export and the actual ZIP write share one tree walk instead of two,
     * and so a failure listing one object's children doesn't abort the whole export — just that
     * object's subtree is logged as incomplete and treated as childless.
     *
     * @param roots the top-level objects being exported
     * @return map of object ID to its direct children
     */
    private Map<Long, List<JEVisObject>> buildChildrenMap(List<JEVisObject> roots) {
        Map<Long, List<JEVisObject>> map = new HashMap<>();
        Deque<JEVisObject> queue = new ArrayDeque<>(roots);

        while (!queue.isEmpty()) {
            JEVisObject current = queue.poll();
            if (map.containsKey(current.getID())) {
                continue;
            }

            try {
                List<JEVisObject> children = current.getChildren();
                map.put(current.getID(), children);
                queue.addAll(children);
            } catch (Exception e) {
                logger.error("Could not list children of object {}:{} — its subtree will be incomplete in this export",
                        current.getName(), current.getID(), e);
                map.put(current.getID(), Collections.emptyList());
            }
        }

        return map;
    }

    /**
     * Flattens {@code roots} and all descendants (per {@code childrenMap}) into a single list,
     * roots first, matching the traversal order {@link #buildChildrenMap} used to populate the map.
     */
    private List<JEVisObject> flattenTree(List<JEVisObject> roots, Map<Long, List<JEVisObject>> childrenMap) {
        List<JEVisObject> result = new ArrayList<>();
        Deque<JEVisObject> queue = new ArrayDeque<>(roots);

        while (!queue.isEmpty()) {
            JEVisObject current = queue.poll();
            result.add(current);
            queue.addAll(childrenMap.getOrDefault(current.getID(), Collections.emptyList()));
        }

        return result;
    }

    private void writeZipOutputStream(ZipOutputStream zipOutputStream,
                                      List<JEVisObject> objects,
                                      String folder,
                                      StringProperty message,
                                      AtomicReference<Integer> jobNo,
                                      int jobCount,
                                      Map<Long, List<JEVisObject>> childrenMap) {
        for (JEVisObject object : objects) {
            try {
                jobNo.set(jobNo.get() + 1);

                message.set("Prepare Export Job [" + jobNo.get() + "/" + jobCount + "] object: ["
                        + object.getID() + "] " + object.getName());

                logger.debug("Exporting object: {}:{}", object.getName(), object.getID());

                ZipEntry objectZipEntry = new ZipEntry(folder + "o_" + object.getID() + ".json");
                zipOutputStream.putNextEntry(objectZipEntry);
                ObjectNode objectNode = toJson(object);
                mapper.writeValue(zipOutputStream, objectNode);

                for (JEVisAttribute jeVisAttribute : object.getAttributes()) {
                    try {
                        logger.debug("Exporting attribute {} of object {}:{}.",
                                jeVisAttribute.getName(), object.getName(), object.getID());

                        if (jeVisAttribute.getPrimitiveType() != JEVisConstants.PrimitiveType.FILE
                                && jeVisAttribute.getPrimitiveType() != JEVisConstants.PrimitiveType.PASSWORD_PBKDF2) {
                            ZipEntry attributeZipEntry = new ZipEntry(folder + "a_"
                                    + object.getID() + "_" + jeVisAttribute.getName() + ".json");
                            zipOutputStream.putNextEntry(attributeZipEntry);

                            writeAttributeJson(zipOutputStream, jeVisAttribute);
                        } else if (jeVisAttribute.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
                            if (jeVisAttribute.hasSample()) {
                                List<JEVisSample> allSamples = jeVisAttribute.getAllSamples();

                                logger.debug("Found {} file samples for attribute {} of object {}:{}. Writing to export file...",
                                        allSamples.size(), jeVisAttribute.getName(), object.getName(), object.getID());

                                int fileNo = 0;
                                for (JEVisSample sample : allSamples) {
                                    fileNo++;
                                    try {
                                        if (allSamples.size() > 1) {
                                            message.set("Prepare Export Job [" + jobNo.get() + "/" + jobCount + "] object: ["
                                                    + object.getID() + "] " + object.getName()
                                                    + " — attachment " + fileNo + "/" + allSamples.size());
                                        }

                                        JEVisFile sampleValueAsFile = sample.getValueAsFile();

                                        if (sampleValueAsFile == null
                                                || sampleValueAsFile.getFilename() == null
                                                || sampleValueAsFile.getFilename().trim().isEmpty()) {
                                            logger.warn("Skipping unreadable file sample at {} for attribute {} of object {}:{} (file or filename missing)",
                                                    sample.getTimestamp(), jeVisAttribute.getName(), object.getName(), object.getID());
                                            continue;
                                        }

                                        if (sampleValueAsFile.getBytes() == null) {
                                            logger.warn("Skipping file sample {} at {} for attribute {} of object {}:{} (file content missing)",
                                                    sampleValueAsFile.getFilename(), sample.getTimestamp(), jeVisAttribute.getName(), object.getName(), object.getID());
                                            continue;
                                        }

                                        ZipEntry sampleFileZipEntry = new ZipEntry(folder + "a_"
                                                + object.getID() + "_" + jeVisAttribute.getName()
                                                + "/" + sample.getTimestamp().toString(FILE_DATE_FORMAT)
                                                + "/" + sampleValueAsFile.getFilename().trim());
                                        zipOutputStream.putNextEntry(sampleFileZipEntry);
                                        zipOutputStream.write(sampleValueAsFile.getBytes());
                                    } catch (Exception e) {
                                        logger.error("Failed to write file sample at {} for attribute {} of object {}:{}",
                                                sample.getTimestamp(), jeVisAttribute.getName(), object.getName(), object.getID(), e);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to write attribute {} of object {}:{}",
                                jeVisAttribute.getName(), object.getName(), object.getID(), e);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to write object {}:{}", object.getName(), object.getID(), e);
            }

            // Recurse into children regardless of whether this object's own write above succeeded,
            // so a transient failure on one object doesn't silently drop its entire subtree from
            // the export. Children were already fetched once into childrenMap up front.
            String newFolder = folder + object.getID() + "/";
            List<JEVisObject> children = childrenMap.getOrDefault(object.getID(), Collections.emptyList());
            writeZipOutputStream(zipOutputStream, children, newFolder, message, jobNo, jobCount, childrenMap);
        }
    }

    /**
     * Collects access-control relationships for all exported objects and writes them as
     * {@value #RELATIONSHIPS_FILE} into the ZIP archive root.
     *
     * <p>Three categories of relationships are exported:
     * <ul>
     *   <li>{@link JEVisConstants.ObjectRelationship#OWNER} (100) — for every exported object,
     *       recording which groups have access to it.</li>
     *   <li>{@code MEMBER_READ..MEMBER_DELETE} (101–105) — for every exported object of class
     *       {@link JEVisConstants.Class#USER}, recording group memberships and their permission level.</li>
     *   <li>{@code ROLE_MEMBER..ROLE_DELETE} (200–205) — for every exported object of class
     *       {@link JC.UserRole#name}, recording which users belong to the role and which groups
     *       the role has access to.</li>
     * </ul>
     *
     * <p>Older importers that do not understand {@value #RELATIONSHIPS_FILE} will simply ignore the
     * entry, ensuring backward compatibility of the archive format.
     *
     * <p>Note: {@code PASSWORD_PBKDF2} attributes are never exported. Users imported from this
     * archive will have no password and require a manual password reset.
     *
     * @param zipOutputStream the open ZIP stream to write to (must not be closed by this method)
     * @param allObjects      flat list of every exported JEVis object (root and all descendants)
     */
    private void exportRelationships(ZipOutputStream zipOutputStream, List<JEVisObject> allObjects) {
        try {
            List<JsonRelationship> relationships = new ArrayList<>();
            Set<String> seen = new HashSet<>();

            for (JEVisObject object : allObjects) {
                try {
                    // OWNER: every object → the groups that can access it
                    collectRelationships(relationships, seen, object,
                            JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD);

                    String className = object.getJEVisClassName();

                    // MEMBER_*: User objects → the groups they belong to (with permission type)
                    if (JEVisConstants.Class.USER.equals(className)) {
                        for (int type : new int[]{
                                JEVisConstants.ObjectRelationship.MEMBER_READ,
                                JEVisConstants.ObjectRelationship.MEMBER_WRITE,
                                JEVisConstants.ObjectRelationship.MEMBER_EXECUTE,
                                JEVisConstants.ObjectRelationship.MEMBER_CREATE,
                                JEVisConstants.ObjectRelationship.MEMBER_DELETE}) {
                            collectRelationships(relationships, seen, object, type,
                                    JEVisConstants.Direction.FORWARD);
                        }
                    }

                    // ROLE_*: User Role objects → their user members and group access
                    if (JC.UserRole.name.equals(className)) {
                        for (int type : new int[]{
                                JEVisConstants.ObjectRelationship.ROLE_MEMBER,
                                JEVisConstants.ObjectRelationship.ROLE_READ,
                                JEVisConstants.ObjectRelationship.ROLE_WRITE,
                                JEVisConstants.ObjectRelationship.ROLE_EXECUTE,
                                JEVisConstants.ObjectRelationship.ROLE_CREATE,
                                JEVisConstants.ObjectRelationship.ROLE_DELETE}) {
                            collectRelationships(relationships, seen, object, type,
                                    JEVisConstants.Direction.FORWARD);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to collect relationships for object {}:{}",
                            object.getName(), object.getID(), e);
                }
            }

            ZipEntry relEntry = new ZipEntry(RELATIONSHIPS_FILE);
            zipOutputStream.putNextEntry(relEntry);
            mapper.writeValue(zipOutputStream, relationships);
            logger.info("Exported {} relationships to {}", relationships.size(), RELATIONSHIPS_FILE);
        } catch (Exception e) {
            logger.error("Failed to export relationships", e);
        }
    }

    /**
     * Collects all relationships of the given {@code type} and {@code direction} from {@code object}
     * into {@code list}, deduplicating via {@code seen}.
     *
     * @param list      target list to add collected relationships to
     * @param seen      set of {@code "from_to_type"} keys used for deduplication
     * @param object    the JEVis object whose relationships are queried
     * @param type      the relationship type constant from {@link JEVisConstants.ObjectRelationship}
     * @param direction {@link JEVisConstants.Direction#FORWARD} or {@link JEVisConstants.Direction#BACKWARD}
     */
    private void collectRelationships(List<JsonRelationship> list, Set<String> seen,
                                      JEVisObject object, int type, int direction) throws JEVisException {
        for (JEVisRelationship rel : object.getRelationships(type, direction)) {
            String key = rel.getStartID() + "_" + rel.getEndID() + "_" + rel.getType();
            if (seen.add(key)) {
                JsonRelationship jr = new JsonRelationship();
                jr.setFrom(rel.getStartID());
                jr.setTo(rel.getEndID());
                jr.setType(rel.getType());
                list.add(jr);
            }
        }
    }

    /**
     * Post-import phase D: recreates access-control relationships from {@value #RELATIONSHIPS_FILE}
     * in the extracted archive.
     *
     * <p>Resolution strategy (in priority order):
     * <ol>
     *   <li>Check {@code createdObjects} — the object was part of this import and received a new ID.</li>
     *   <li>Fall back to {@code ds.getObject(oldId)} — the object already exists on the target
     *       system with the same numeric ID (e.g. a cross-tree reference to a shared group).</li>
     * </ol>
     *
     * <p>If {@value #RELATIONSHIPS_FILE} is absent in the archive, the method returns immediately
     * without error, ensuring backward compatibility with pre-relationship {@code .jex} files.
     *
     * <p>After all relationships are created, {@link JEVisDataSource#updateAccessControl()} is called
     * to flush the server-side ACL cache, making permissions effective immediately.
     *
     * <p>Note: {@code PASSWORD_PBKDF2} attributes are not included in the export. Imported User
     * objects will have no password — administrators must reset passwords after import.
     *
     * @param ds             live datasource used to create relationships and flush the ACL cache
     * @param tmpDir         directory into which the archive was extracted
     * @param createdObjects mapping of old export IDs to newly created {@link JEVisObject}s
     */
    private void importRelationships(JEVisDataSource ds, Path tmpDir, Map<Long, JEVisObject> createdObjects) {
        Path relFile = tmpDir.resolve(RELATIONSHIPS_FILE);
        if (!Files.exists(relFile)) {
            logger.info("No {} in archive — skipping relationship import", RELATIONSHIPS_FILE);
            return;
        }

        try {
            List<JsonRelationship> relationships = mapper.readValue(
                    relFile.toFile(),
                    mapper.getTypeFactory().constructCollectionType(List.class, JsonRelationship.class));

            logger.info("Importing {} relationships", relationships.size());
            int created = 0;
            int skipped = 0;
            int consecutiveFailures = 0;
            final int FAILURE_ABORT_THRESHOLD = 5;

            for (int i = 0; i < relationships.size(); i++) {
                JsonRelationship rel = relationships.get(i);
                try {
                    long newFrom = resolveIdForRelationship(ds, createdObjects, rel.getFrom());
                    long newTo = resolveIdForRelationship(ds, createdObjects, rel.getTo());

                    if (newFrom > 0 && newTo > 0) {
                        JEVisRelationship newRel = ds.buildRelationship(newFrom, newTo, rel.getType());
                        if (newRel != null) {
                            created++;
                            consecutiveFailures = 0;
                        } else {
                            logger.warn("Failed to recreate relationship: from={} to={} type={}",
                                    newFrom, newTo, rel.getType());
                            skipped++;
                            consecutiveFailures++;
                        }
                    } else {
                        logger.warn("Skipping unresolvable relationship: from={} (resolved={}) to={} (resolved={}) type={}",
                                rel.getFrom(), newFrom, rel.getTo(), newTo, rel.getType());
                        skipped++;
                    }
                } catch (Exception e) {
                    logger.error("Failed to recreate relationship {}", rel, e);
                    skipped++;
                    consecutiveFailures++;
                }

                if (consecutiveFailures >= FAILURE_ABORT_THRESHOLD) {
                    int remaining = relationships.size() - (i + 1);
                    skipped += remaining;
                    logger.error("Aborting relationship import after {} consecutive failures — the target server may not support the relationship API (older JEWebService version?). {} relationships were not attempted.",
                            consecutiveFailures, remaining);
                    break;
                }
            }

            logger.info("Relationship import complete: {} created, {} skipped", created, skipped);
            ds.updateAccessControl();
        } catch (Exception e) {
            logger.error("Failed to load or apply {}", RELATIONSHIPS_FILE, e);
        }
    }

    /**
     * Resolves an old (exported) object ID to the current system's object ID.
     *
     * <p>Tries {@code createdObjects} first (object was re-created during import), then falls back
     * to a live datasource lookup (object already exists on the target system).
     *
     * @param ds             live datasource for fallback lookup
     * @param createdObjects mapping of old IDs to newly created objects
     * @param oldId          the object ID as it appeared in the export
     * @return the resolved ID on the current system, or {@code -1} if the object cannot be found
     */
    private long resolveIdForRelationship(JEVisDataSource ds, Map<Long, JEVisObject> createdObjects, long oldId) {
        JEVisObject obj = createdObjects.get(oldId);
        if (obj != null) return obj.getID();
        try {
            obj = ds.getObject(oldId);
            if (obj != null) return obj.getID();
        } catch (Exception ignored) {
        }
        return -1L;
    }

    /**
     * Resolves an old (exported) object ID to the corresponding {@link JEVisObject} on the current
     * system.
     *
     * <p>Tries {@code createdObjects} first (object was re-created during import with a new ID),
     * then falls back to a live datasource lookup for objects that already exist on the target
     * system (cross-tree references).
     *
     * @param ds             live datasource for fallback lookup
     * @param createdObjects mapping of old IDs to newly created objects
     * @param oldId          the object ID as it appeared in the export
     * @return the resolved {@link JEVisObject}, or {@code null} if not found on either path
     */
    private JEVisObject resolveObject(JEVisDataSource ds, Map<Long, JEVisObject> createdObjects, long oldId) {
        JEVisObject obj = createdObjects.get(oldId);
        if (obj != null) return obj;
        try {
            return ds.getObject(oldId);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Returns whether {@code primitiveType} is one of the scalar types whose samples are exported
     * as plain JSON values (timestamp/value/note). FILE and PASSWORD_PBKDF2 attributes are handled
     * elsewhere ({@link #writeZipOutputStream} writes FILE samples as separate ZIP entries;
     * PASSWORD_PBKDF2 is never exported).
     */
    private boolean isScalarPrimitiveType(int primitiveType) {
        return primitiveType == JEVisConstants.PrimitiveType.BOOLEAN
                || primitiveType == JEVisConstants.PrimitiveType.DOUBLE
                || primitiveType == JEVisConstants.PrimitiveType.LONG
                || primitiveType == JEVisConstants.PrimitiveType.SELECTION
                || primitiveType == JEVisConstants.PrimitiveType.MULTI_SELECTION
                || primitiveType == JEVisConstants.PrimitiveType.STRING;
    }

    /**
     * Writes a non-FILE, non-PASSWORD attribute (metadata + all samples) directly onto {@code out}
     * as a single streamed JSON object, instead of building an in-memory Jackson tree first. For
     * attributes with long sample histories this avoids holding every sample as both a
     * {@link JEVisSample} and a duplicate JSON-tree node in memory at once.
     *
     * @param out       the open ZIP stream, positioned at the attribute's entry
     * @param attribute the attribute to serialize
     */
    private void writeAttributeJson(ZipOutputStream out, JEVisAttribute attribute) throws IOException {
        logger.info("Writing attribute {} of object {}:{}",
                attribute.getName(), attribute.getObject().getName(), attribute.getObject().getID());

        JsonGenerator gen = mapper.getFactory().createGenerator(out);
        gen.setCodec(mapper);
        gen.writeStartObject();
        gen.writeStringField(ATTRIBUTE_NAME, attribute.getName());

        try {
            if (attribute.getInputSampleRate() != null) {
                gen.writeStringField(ATTRIBUTE_RATE, attribute.getInputSampleRate().toString());
            }

            if (attribute.getInputUnit() != null) {
                gen.writeObjectField(ATTRIBUTE_UNIT, JsonFactory.buildUnit(attribute.getInputUnit()));
            }

            if (attribute.hasSample()) {
                List<JEVisSample> allSamples = attribute.getAllSamples();

                logger.info("Found {} samples on attribute {}. Writing samples.",
                        allSamples.size(), attribute.getName());

                gen.writeArrayFieldStart(ATTRIBUTE_SAMPLES);
                if (isScalarPrimitiveType(attribute.getPrimitiveType())) {
                    for (JEVisSample jeVisSample : allSamples) {
                        try {
                            gen.writeStartObject();
                            gen.writeStringField(SAMPLE_TS, jeVisSample.getTimestamp().toString());
                            gen.writeStringField(SAMPLE_VALUE, jeVisSample.getValueAsString());
                            gen.writeStringField(NOTE, jeVisSample.getNote());
                            gen.writeEndObject();
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                }
                gen.writeEndArray();
            }
        } catch (Exception e) {
            logger.error(e);
        }

        gen.writeEndObject();
        gen.flush();
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
