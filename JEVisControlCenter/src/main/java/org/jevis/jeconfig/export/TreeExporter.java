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
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class TreeExporter {

    private static final Logger logger = LogManager.getLogger(TreeExporter.class);
    private static final String IMPORTER_REVISION = "2026-10-05-adaptive-upload-v7";
    private static final String EXPORTER_REVISION = "2026-10-05-adaptive-upload-v7";
    private static final int BUFFER_SIZE = 4096;
    private static final int SAMPLE_IMPORT_CHUNK_SIZE = 5000;
    private static final String FILE_DATE_FORMAT = "yyyyMMddHHmmss";
    private static final String FILE_DATE_FORMAT_WITH_MILLIS = "yyyyMMddHHmmssSSS";
    private static final String RELATIONSHIPS_FILE = "relationships.json";
    private static final String EXPORT_MANIFEST_FILE = "export-manifest.json";

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

    private void showProcessReport(ProcessReport report, Throwable taskFailure) {
        if (taskFailure != null && report.errors == 0) {
            report.error(report.operation + " wurde abgebrochen", taskFailure);
        }

        boolean failed = taskFailure != null;
        boolean hasProblems = report.errors > 0 || report.warnings > 0;
        Alert.AlertType type = failed ? Alert.AlertType.ERROR
                : hasProblems ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(type);
        alert.setTitle("JEVis " + report.operation);
        alert.setHeaderText(failed
                ? report.operation + " fehlgeschlagen"
                : report.errors > 0 ? report.operation + " mit Fehlern abgeschlossen"
                  : report.warnings > 0 ? report.operation + " mit Warnungen abgeschlossen"
                    : report.operation + " erfolgreich abgeschlossen");

        long seconds = Math.max(0, (System.currentTimeMillis() - report.startedAt) / 1000);
        String expected = report.expectedObjects > 0 ? " / " + report.expectedObjects : "";
        alert.setContentText(
                "Datei: " + report.sourceOrTarget + "\n"
                        + "Dauer: " + seconds + " s\n"
                        + "Objekte: " + report.objects + expected + "\n"
                        + "Attribute: " + report.attributes + "\n"
                        + "Samples: " + report.samples + "\n"
                        + "Datei-Samples: " + report.fileSamples + "\n"
                        + "Beziehungen: " + report.relationships + "\n"
                        + "Übersprungen: " + report.skipped + "\n"
                        + "Warnungen: " + report.warnings + "\n"
                        + "Fehler: " + report.errors);

        if (!report.details.isEmpty()) {
            String detailText = String.join(System.lineSeparator(), report.details);
            if (report.omittedDetails > 0) {
                detailText += System.lineSeparator() + "... " + report.omittedDetails
                        + " weitere Meldungen (siehe Logdatei)";
            }
            TextArea textArea = new TextArea(detailText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            GridPane detailPane = new GridPane();
            detailPane.setMaxWidth(Double.MAX_VALUE);
            detailPane.add(textArea, 0, 0);
            alert.getDialogPane().setExpandableContent(detailPane);
            alert.getDialogPane().setExpanded(hasProblems);
        }
        alert.showAndWait();
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
     *   <li><b>Relationships</b>: If the archive contains {@value #RELATIONSHIPS_FILE}, exported
     *       functional and access-control relationships are recreated via {@link #importRelationships}.
     *       Missing the file is silently ignored for backward compatibility with older archives.
     *       Note: {@code PASSWORD_PBKDF2} attributes are intentionally excluded from export, so
     *       imported User objects will have no password — administrators must reset them manually.</li>
     * </ol>
     *
     * @param file   the {@code .jex} archive to import
     * @param parent the JEVis object that will be the parent of all imported root objects
     * @return a Task that performs the import; must be submitted to a thread or executor
     */
    public Task<Void> importFromFile(File file, JEVisObject parent) {
        final ProcessReport report = new ProcessReport("Import", file);
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    logger.info("==========================================");
                    logger.info("importFromFile: {} parent: {}", file, parent);
                    logger.info("TreeExporter importer revision: {}", IMPORTER_REVISION);

                    StringProperty messages = new SimpleStringProperty();
                    messages.addListener((observable, oldValue, newValue) -> updateMessage(newValue));

                    Path tmpDir = Files.createTempDirectory("import").toAbsolutePath();

                    ZipFile zipFile = new ZipFile(file);
                    Enumeration zipFileEntries = zipFile.entries();

                    while (zipFileEntries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                        Path destinationPath = tmpDir.resolve(entry.getName()).normalize();
                        if (!destinationPath.startsWith(tmpDir)) {
                            throw new IOException("Unsafe ZIP entry outside import directory: " + entry.getName());
                        }
                        File destFile = destinationPath.toFile();
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
                    Set<Long> archiveObjectIds = collectArchiveObjectIds(tmpDir);
                    report.expectedObjects = archiveObjectIds.size();
                    validateExportManifest(tmpDir, archiveObjectIds.size());

                    readTmpFilesToJEVis(messages, tmpDir, parent, createdObjects, targets,
                            fileAttributes, longTargets, report);
                    if (createdObjects.size() != archiveObjectIds.size()) {
                        logger.warn("Object import incomplete: {} of {} archive objects were created. See earlier errors for the first rejected class or object.",
                                createdObjects.size(), archiveObjectIds.size());
                        report.warning("Nicht alle Objekte wurden erstellt: " + createdObjects.size()
                                + " von " + archiveObjectIds.size());
                    } else {
                        logger.info("Created all {} objects from archive", createdObjects.size());
                    }

                    updateTargetAttributes(createdObjects, targets, report);
                    updateTargetLongAttributes(createdObjects, longTargets, report);
                    updateTargetsInFiles(parent.getDataSource(), createdObjects, archiveObjectIds,
                            fileAttributes, report);
                    importRelationships(parent.getDataSource(), tmpDir, createdObjects, archiveObjectIds, report);

                    logger.info("All Done");
                } catch (Exception ex) {
                    logger.error("Failed extracting files from archive.", ex);
                    report.error("Import wurde abgebrochen", ex);
                    throw ex;
                }

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                showProcessReport(report, null);
            }

            @Override
            protected void failed() {
                super.failed();
                showProcessReport(report, getException());
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                showProcessReport(report,
                        new java.util.concurrent.CancellationException("Import wurde abgebrochen"));
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
     * @param archiveObjectIds IDs represented by object files in the archive; failed archive
     *                         objects must not fall back to a coincidentally equal target-system ID
     * @param fileAttributes all non-target attributes collected during import; only those with
     *                       recognized names are processed
     * @param report         accumulates remapping results, warnings and errors for the UI summary
     */
    private void updateTargetsInFiles(JEVisDataSource ds,
                                      Map<Long, JEVisObject> createdObjects,
                                      Set<Long> archiveObjectIds,
                                      List<JEVisAttribute> fileAttributes,
                                      ProcessReport report) throws JEVisException, IOException {
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
                            report.warning("ChartData-ID " + oldId + " in Analysis File von Objekt "
                                    + fileAttribute.getObject().getID() + " konnte nicht aufgelöst werden");
                            report.skipped++;
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
                                report.warning("Calculation-ID " + oldCalcId + " in Analysis File von Objekt "
                                        + fileAttribute.getObject().getID() + " konnte nicht aufgelöst werden");
                                report.skipped++;
                            }
                        }
                    }
                }

                analysisHandler.saveDataModel(fileAttribute.getObject(), dataModel);

            } else if (fileAttribute.getName().equals(JC.DashboardAnalysis.a_DataModelFile)) {
                try {
                    JEVisSample latestSample = fileAttribute.getLatestSample();
                    if (latestSample == null) {
                        report.warning("Dashboard " + fileAttribute.getObject().getID()
                                + " besitzt nach dem Import kein Data-Model-Sample");
                        continue;
                    }

                    JEVisFile file = latestSample.getValueAsFile();
                    if (file == null || file.getBytes() == null || file.getBytes().length == 0) {
                        report.error("Data Model File von Dashboard "
                                + fileAttribute.getObject().getID() + " enthält keine Daten", null);
                        continue;
                    }

                    JsonNode jsonNode = mapper.readTree(file.getBytes());
                    Map<Long, Long> resolvedIds = new HashMap<>();
                    Set<Long> warnedUnresolvedIds = new HashSet<>();
                    int remapped = remapDashboardReferences(jsonNode, null, ds, createdObjects,
                            archiveObjectIds, resolvedIds, warnedUnresolvedIds, report,
                            fileAttribute.getObject().getID());
                    report.info("Dashboard " + fileAttribute.getObject().getID() + ": "
                            + remapped + " Objekt-ID-Referenzen umgesetzt");

                    JEVisFileImp jsonFile = new JEVisFileImp(
                            file.getFilename(),
                            mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(jsonNode)
                    );
                    JEVisSample newSample = fileAttribute.buildSample(new DateTime(), jsonFile);
                    newSample.commit();
                } catch (Exception e) {
                    logger.error("Failed to remap Dashboard Data Model File for object {}",
                            fileAttribute.getObject().getID(), e);
                    report.error("Dashboard Data Model File von Objekt "
                            + fileAttribute.getObject().getID() + " konnte nicht umgesetzt werden", e);
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
                                report.warning("Template-ID " + oldTemplateId + " in Objekt "
                                        + fileAttribute.getObject().getID() + " konnte nicht aufgelöst werden");
                                report.skipped++;
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
                                    report.warning("TemplateInput-ID " + oldId + " in Objekt "
                                            + fileAttribute.getObject().getID() + " konnte nicht aufgelöst werden");
                                    report.skipped++;
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
                                    report.warning("Accounting-Ziel '" + output.getTarget()
                                            + "' konnte nicht umgesetzt werden");
                                    report.skipped++;
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
                    report.error("Template File von Objekt " + fileAttribute.getObject().getID()
                            + " konnte nicht umgesetzt werden", e);
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
                                        report.warning("SCADA-Objekt-ID " + oldId + " in Objekt "
                                                + fileAttribute.getObject().getID() + " konnte nicht aufgelöst werden");
                                        report.skipped++;
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
                    report.error("SCADA Data Model von Objekt " + fileAttribute.getObject().getID()
                            + " konnte nicht umgesetzt werden", e);
                }
            }
        }
    }

    /**
     * Rewrites JEVis object references in a dashboard JSON tree without touching unrelated IDs
     * such as widget UUIDs or TimeFrameWidget selected-widget IDs.
     *
     * <p>Supported dashboard formats:
     * <ul>
     *   <li>Current AnalysisHandler data series: {@code chartData[].id} and
     *       {@code chartData[].calculationId}</li>
     *   <li>Legacy SimpleDataHandler rows: {@code objectID}, {@code cleanObjectID} and
     *       {@code calculationID}</li>
     *   <li>Dashboard links and image/file references: {@code dashboardObject} and
     *       {@code objectID}</li>
     *   <li>NetGraph and Sankey rows, including Sankey {@code children}</li>
     * </ul>
     * Numeric JSON values remain numeric and string values remain strings.
     */
    private int remapDashboardReferences(JsonNode node,
                                         String containerName,
                                         JEVisDataSource ds,
                                         Map<Long, JEVisObject> createdObjects,
                                         Set<Long> archiveObjectIds,
                                         Map<Long, Long> resolvedIds,
                                         Set<Long> warnedUnresolvedIds,
                                         ProcessReport report,
                                         long dashboardObjectId) {
        if (node == null) return 0;

        int remapped = 0;
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<String> fieldNames = new ArrayList<>();
            objectNode.fieldNames().forEachRemaining(fieldNames::add);

            for (String fieldName : fieldNames) {
                JsonNode value = objectNode.get(fieldName);
                if (isDashboardReferenceField(fieldName, containerName)) {
                    remapped += remapDashboardReference(objectNode, fieldName, value, ds,
                            createdObjects, archiveObjectIds, resolvedIds, warnedUnresolvedIds,
                            report, dashboardObjectId);
                } else if ("children".equals(fieldName)
                        && "sankeyDataRows".equals(containerName) && value != null && value.isArray()) {
                    remapped += remapDashboardReferenceArray((ArrayNode) value, ds, createdObjects,
                            archiveObjectIds, resolvedIds, warnedUnresolvedIds, report,
                            dashboardObjectId, "sankeyDataRows[].children");
                }

                remapped += remapDashboardReferences(value, fieldName, ds, createdObjects,
                        archiveObjectIds, resolvedIds, warnedUnresolvedIds, report,
                        dashboardObjectId);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                remapped += remapDashboardReferences(child, containerName, ds, createdObjects,
                        archiveObjectIds, resolvedIds, warnedUnresolvedIds, report,
                        dashboardObjectId);
            }
        }
        return remapped;
    }

    private boolean isDashboardReferenceField(String fieldName, String containerName) {
        if ("objectID".equals(fieldName)
                || "cleanObjectID".equals(fieldName)
                || "calculationID".equals(fieldName)
                || "calculationId".equals(fieldName)
                || "dashboardObject".equals(fieldName)) {
            return true;
        }

        return "id".equals(fieldName)
                && ("chartData".equals(containerName)
                || "netGraphDataRows".equals(containerName)
                || "sankeyDataRows".equals(containerName));
    }

    private int remapDashboardReference(ObjectNode parent,
                                        String fieldName,
                                        JsonNode value,
                                        JEVisDataSource ds,
                                        Map<Long, JEVisObject> createdObjects,
                                        Set<Long> archiveObjectIds,
                                        Map<Long, Long> resolvedIds,
                                        Set<Long> warnedUnresolvedIds,
                                        ProcessReport report,
                                        long dashboardObjectId) {
        if (value == null || (!value.isNumber() && !value.isTextual())) return 0;
        long oldId = value.asLong(-1);
        if (oldId <= 0) return 0;

        long newId = resolveIdForRelationship(ds, createdObjects, archiveObjectIds, resolvedIds, oldId);
        if (newId <= 0) {
            warnUnresolvedDashboardReference(oldId, fieldName, dashboardObjectId,
                    warnedUnresolvedIds, report);
            report.skipped++;
            return 0;
        }

        if (value.isTextual()) {
            parent.put(fieldName, Long.toString(newId));
        } else {
            parent.put(fieldName, newId);
        }
        return newId != oldId ? 1 : 0;
    }

    private int remapDashboardReferenceArray(ArrayNode values,
                                             JEVisDataSource ds,
                                             Map<Long, JEVisObject> createdObjects,
                                             Set<Long> archiveObjectIds,
                                             Map<Long, Long> resolvedIds,
                                             Set<Long> warnedUnresolvedIds,
                                             ProcessReport report,
                                             long dashboardObjectId,
                                             String fieldName) {
        int remapped = 0;
        for (int i = 0; i < values.size(); i++) {
            JsonNode value = values.get(i);
            if (value == null || (!value.isNumber() && !value.isTextual())) continue;
            long oldId = value.asLong(-1);
            if (oldId <= 0) continue;

            long newId = resolveIdForRelationship(ds, createdObjects, archiveObjectIds,
                    resolvedIds, oldId);
            if (newId <= 0) {
                warnUnresolvedDashboardReference(oldId, fieldName, dashboardObjectId,
                        warnedUnresolvedIds, report);
                report.skipped++;
                continue;
            }

            values.set(i, value.isTextual()
                    ? JsonNodeFactory.instance.textNode(Long.toString(newId))
                    : JsonNodeFactory.instance.numberNode(newId));
            if (newId != oldId) remapped++;
        }
        return remapped;
    }

    private void warnUnresolvedDashboardReference(long oldId,
                                                  String fieldName,
                                                  long dashboardObjectId,
                                                  Set<Long> warnedUnresolvedIds,
                                                  ProcessReport report) {
        if (warnedUnresolvedIds.add(oldId)) {
            String text = "Dashboard " + dashboardObjectId + ": Referenz " + fieldName
                    + " mit alter Objekt-ID " + oldId + " konnte nicht aufgelöst werden";
            logger.warn(text);
            report.warning(text);
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
    private void updateTargetAttributes(Map<Long, JEVisObject> createdObjects,
                                        Map<JEVisAttribute, JsonNode> targets,
                                        ProcessReport report) {
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
                    report.error("Target-Sample konnte nicht aufgebaut werden: " + jSample, ex);
                    report.skipped++;
                }
            }

            try {
                if (!jeVisSamples.isEmpty()) {
                    addSamplesInChunks(jeVisAttribute, jeVisSamples);
                    report.samples += jeVisSamples.size();
                }
            } catch (Exception e) {
                logger.error(e);
                report.error("Target-Samples für Objekt " + jeVisAttribute.getObject().getID()
                        + ", Attribut '" + jeVisAttribute.getName() + "' konnten nicht gespeichert werden", e);
                report.skipped += jeVisSamples.size();
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
    private void updateTargetLongAttributes(Map<Long, JEVisObject> createdObjects,
                                            Map<JEVisAttribute, JsonNode> longTargets,
                                            ProcessReport report) {
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
                        report.warning("Ziel-ID " + oldId + " für Attribut '"
                                + jeVisAttribute.getName() + "' konnte nicht aufgelöst werden");
                        report.skipped++;
                    }
                } catch (Exception ex) {
                    logger.error("Error remapping BASIC_TARGET_LONG sample: {}", jSample, ex);
                    report.error("Long-Target-Sample konnte nicht umgesetzt werden: " + jSample, ex);
                    report.skipped++;
                }
            }

            try {
                if (!samples.isEmpty()) {
                    addSamplesInChunks(jeVisAttribute, samples);
                    report.samples += samples.size();
                }
            } catch (Exception e) {
                logger.error("Failed to commit BASIC_TARGET_LONG samples for attribute '{}'",
                        jeVisAttribute.getName(), e);
                report.error("Long-Target-Samples für Attribut '" + jeVisAttribute.getName()
                        + "' konnten nicht gespeichert werden", e);
                report.skipped += samples.size();
            }
        }
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
                                     Map<JEVisAttribute, JsonNode> longTargets,
                                     ProcessReport report) {
        try {
            Set<Path> objectFiles = listObjectFiles(directory);

            for (Path objectPath : objectFiles) {
                try {
                    JsonNode jsonObjectNode = mapper.readTree(objectPath.toFile());

                    logger.info("Create Object: {} [{}]", jsonObjectNode.get(OBJECT_NAME), jsonObjectNode.get(OBJECT_CLASS));
                    message.setValue("Create Object " + jsonObjectNode.get(OBJECT_NAME) + "[" + jsonObjectNode.get(OBJECT_CLASS) + "]");

                    JEVisClass objClass = parent.getDataSource().getJEVisClass(jsonObjectNode.get(OBJECT_CLASS).asText());

                    if (objClass == null) {
                        logger.error("Class does not exist, skipping object file: {}", objectPath);
                        report.error("Klasse fehlt; Objektdatei wird übersprungen: " + objectPath, null);
                        report.skipped++;
                        continue;
                    }

                    if (!parent.getAllowedChildrenClasses().contains(objClass)) {
                        logger.error("Class '{}' is not allowed under '{}' (object file: {})",
                                objClass.getName(), parent.getJEVisClassName(), objectPath);
                        report.error("Klasse '" + objClass.getName() + "' ist unter '"
                                + parent.getJEVisClassName() + "' nicht erlaubt: " + objectPath, null);
                        report.skipped++;
                        continue;
                    }

                    JEVisObject jeVisObject = parent.buildObject(jsonObjectNode.get(OBJECT_NAME).asText(), objClass);
                    jeVisObject.commit();
                    Thread.sleep(500);

                    createdObjects.put(
                            Long.parseLong(FilenameUtils.removeExtension(objectPath.getFileName().toString()).substring(2)),
                            jeVisObject
                    );
                    report.objects++;

                    if (jsonObjectNode.get(OBJECT_LANG) != null && jsonObjectNode.get(OBJECT_LANG).isArray()) {
                        for (JsonNode jsonNode1 : jsonObjectNode.get(OBJECT_LANG)) {
                            jsonNode1.fieldNames().forEachRemaining(s -> jeVisObject.setLocalName(s, jsonNode1.get(s).asText()));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to import object file {}. Other objects will still be attempted.", objectPath, e);
                    report.error("Objektdatei konnte nicht importiert werden: " + objectPath, e);
                    report.skipped++;
                }
            }

            // Build the complete object tree before importing attributes. Previously, an exception
            // in one attribute prevented this recursion and silently dropped the whole subtree.
            Set<Path> objectFolders = listObjectFolders(directory);

            for (Path objectFolderPath : objectFolders) {
                try {
                    long oldObjectId = Long.parseLong(objectFolderPath.getFileName().toString());
                    JEVisObject correspondingJEVisObject = createdObjects.get(oldObjectId);

                    if (correspondingJEVisObject == null) {
                        logger.error("Could not find created parent object for imported folder id {}. Subtree {} cannot be imported.",
                                oldObjectId, objectFolderPath);
                        report.error("Übergeordnetes Objekt " + oldObjectId
                                + " fehlt; Teilbaum wird übersprungen: " + objectFolderPath, null);
                        report.skipped++;
                        continue;
                    }

                    readTmpFilesToJEVis(message, objectFolderPath, correspondingJEVisObject,
                            createdObjects, targets, fileAttributes, longTargets, report);
                } catch (Exception e) {
                    logger.error("Failed to import object subtree {}. Other subtrees will still be attempted.",
                            objectFolderPath, e);
                    report.error("Teilbaum konnte nicht importiert werden: " + objectFolderPath, e);
                    report.skipped++;
                }
            }

            Set<Path> attributeFiles = listAttributeFiles(directory);

            for (Path attributePath : attributeFiles) {
                try {
                    JsonNode jsonAttributeNode = mapper.readTree(attributePath.toFile());
                    String attributeFileString = FilenameUtils
                            .removeExtension(Paths.get(attributePath.getFileName().toString()).getFileName().toString())
                            .replaceFirst("a_", "");
                    int indexOf = attributeFileString.indexOf("_");
                    long oldObjectId = Long.parseLong(attributeFileString.substring(0, indexOf));

                    JEVisObject correspondingJEVisObject = createdObjects.get(oldObjectId);
                    if (correspondingJEVisObject == null) {
                        logger.error("Could not find created object for imported id {} (attribute file: {})",
                                oldObjectId, attributePath);
                        report.error("Objekt " + oldObjectId + " für Attributdatei fehlt: " + attributePath, null);
                        report.skipped++;
                        continue;
                    }

                    JsonNode attributeNameNode = jsonAttributeNode.get(ATTRIBUTE_NAME);
                    if (attributeNameNode == null || attributeNameNode.asText().trim().isEmpty()) {
                        logger.error("Attribute file {} has no valid '{}' field", attributePath, ATTRIBUTE_NAME);
                        report.error("Attributdatei enthält keinen gültigen Namen: " + attributePath, null);
                        report.skipped++;
                        continue;
                    }

                    String attributeName = attributeNameNode.asText();
                    logger.info("Creating Attribute: {}", attributeName);
                    JEVisAttribute jevisAttribute = correspondingJEVisObject.getAttribute(attributeName);
                    if (jevisAttribute == null) {
                        logger.warn("Attribute '{}' from {} does not exist on imported object {}:{} and will be skipped",
                                attributeName, attributePath, correspondingJEVisObject.getName(), correspondingJEVisObject.getID());
                        report.warning("Attribut '" + attributeName + "' existiert am Zielobjekt nicht: " + attributePath);
                        report.skipped++;
                        continue;
                    }

                    if (jsonAttributeNode.get(ATTRIBUTE_UNIT) != null) {
                        JsonNode unit = jsonAttributeNode.get(ATTRIBUTE_UNIT);
                        String unitString = mapper.writeValueAsString(unit);
                        JEVisUnitImp jevUnitImp = new JEVisUnitImp(mapper.readValue(unitString, org.jevis.commons.ws.json.JsonUnit.class));
                        jevisAttribute.setInputUnit(jevUnitImp);
                        jevisAttribute.setDisplayUnit(jevUnitImp);
                    }

                    if (jsonAttributeNode.get(ATTRIBUTE_RATE) != null) {
                        jevisAttribute.setInputSampleRate(Period.parse(jsonAttributeNode.get(ATTRIBUTE_RATE).asText()));
                        jevisAttribute.setDisplaySampleRate(Period.parse(jsonAttributeNode.get(ATTRIBUTE_RATE).asText()));
                    }

                    jevisAttribute.commit();
                    Thread.sleep(500);
                    report.attributes++;

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
                                JsonNode noteNode = jSample.get(NOTE);
                                JEVisSample sample = jevisAttribute.buildSample(
                                        dateTime,
                                        jSample.get(SAMPLE_VALUE).asText(),
                                        noteNode != null && !noteNode.isNull() ? noteNode.asText() : ""
                                );
                                jeVisSamples.add(sample);
                            } catch (Exception ex) {
                                logger.error("Error while creating sample from {} in {}", jSample, attributePath, ex);
                                report.error("Sample konnte nicht aufgebaut werden in " + attributePath, ex);
                                report.skipped++;
                            }
                        }

                        if (!jeVisSamples.isEmpty()) {
                            addSamplesInChunks(jevisAttribute, jeVisSamples);
                            report.samples += jeVisSamples.size();
                        }
                    }

                    fileAttributes.add(jevisAttribute);
                } catch (Exception e) {
                    logger.error("Failed to import attribute file {}. Other attributes and subtrees remain unaffected.",
                            attributePath, e);
                    report.error("Attributdatei konnte nicht importiert werden: " + attributePath, e);
                    report.skipped++;
                }
            }

            Set<Path> folderPaths = listFileFolders(directory);

            for (Path folderPath : folderPaths) {
                try {
                    String objectString = folderPath.getFileName().toString().substring(2);
                    String folderName = Paths.get(objectString).getFileName().toString();
                    int indexOf = folderName.indexOf("_");
                    long oldObjectId = Long.parseLong(folderName.substring(0, indexOf));
                    String attributeString = folderName.substring(indexOf + 1);

                    JEVisObject correspondingJEVisObject = createdObjects.get(oldObjectId);
                    if (correspondingJEVisObject == null) {
                        logger.error("Could not find created object for imported id {} (file attribute folder: {})",
                                oldObjectId, folderPath);
                        report.error("Objekt " + oldObjectId + " für Datei-Attribut fehlt: " + folderPath, null);
                        report.skipped++;
                        continue;
                    }

                    JEVisAttribute jevisAttribute = correspondingJEVisObject.getAttribute(attributeString);
                    if (jevisAttribute == null) {
                        logger.warn("File attribute '{}' from {} does not exist on imported object {}:{} and will be skipped",
                                attributeString, folderPath, correspondingJEVisObject.getName(), correspondingJEVisObject.getID());
                        report.warning("Datei-Attribut '" + attributeString
                                + "' existiert am Zielobjekt nicht: " + folderPath);
                        report.skipped++;
                        continue;
                    }
                    report.attributes++;

                    List<JEVisSample> fileSamples = new ArrayList<>();
                    Set<Path> fileDateFolders = listFileDateTimeFolders(folderPath);

                    for (Path fileDateFolderPath : fileDateFolders) {
                        try {
                            File[] files = fileDateFolderPath.toFile().listFiles();
                            if (files == null) {
                                continue;
                            }

                            for (File listFile : files) {
                                DateTime dateTime = parseFileSampleTimestamp(
                                        fileDateFolderPath.getFileName().toString());
                                JEVisFile jeVisFile = new JEVisFileImp(listFile.getName(), listFile);
                                fileSamples.add(jevisAttribute.buildSample(dateTime, jeVisFile));
                            }
                        } catch (Exception e) {
                            logger.error("Failed to import file samples from {}", fileDateFolderPath, e);
                            report.error("Datei-Samples konnten nicht gelesen werden: " + fileDateFolderPath, e);
                            report.skipped++;
                        }
                    }

                    if (!fileSamples.isEmpty()) {
                        addSamplesInChunks(jevisAttribute, fileSamples);
                        report.fileSamples += fileSamples.size();
                    }
                    // Register FILE attributes for content-level ID remapping (e.g. Analysis File,
                    // Data Model File, Template File) — these go through updateTargetsInFiles().
                    fileAttributes.add(jevisAttribute);
                } catch (Exception e) {
                    logger.error("Failed to import file attribute folder {}. Other attributes and subtrees remain unaffected.",
                            folderPath, e);
                    report.error("Datei-Attributordner konnte nicht importiert werden: " + folderPath, e);
                    report.skipped++;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to read import directory {}", directory, e);
            report.error("Importverzeichnis konnte nicht gelesen werden: " + directory, e);
        }
    }

    /**
     * Returns every object ID represented by an {@code o_<id>.json} entry in the archive.
     * This lets relationship import distinguish an external reference from an archive object
     * whose creation failed, avoiding pointless HTTP lookups for the latter.
     */
    private Set<Long> collectArchiveObjectIds(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("o_") && name.endsWith(".json"))
                    .map(name -> name.substring(2, name.length() - 5))
                    .map(value -> {
                        try {
                            return Long.parseLong(value);
                        } catch (NumberFormatException e) {
                            logger.warn("Ignoring object file with invalid id: o_{}.json", value);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
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

    private void validateExportManifest(Path root, int actualObjectFiles) throws IOException {
        Path manifestPath = root.resolve(EXPORT_MANIFEST_FILE);
        if (!Files.exists(manifestPath)) {
            logger.info("No {} in archive; importing legacy export without completeness metadata",
                    EXPORT_MANIFEST_FILE);
            return;
        }

        JsonNode manifest = mapper.readTree(manifestPath.toFile());
        boolean complete = manifest.path("complete").asBoolean(false);
        long expected = manifest.path("objectsExpected").asLong(-1);
        long written = manifest.path("objectsWritten").asLong(-1);
        if (!complete || expected < 0 || written != expected || actualObjectFiles != written) {
            throw new IOException("Export manifest validation failed: complete=" + complete
                    + ", expected=" + expected + ", written=" + written
                    + ", objectFiles=" + actualObjectFiles);
        }
        logger.info("Validated export manifest: {} complete objects, {} scalar samples, {} file samples, {} relationships",
                written,
                manifest.path("samplesWritten").asLong(0),
                manifest.path("fileSamplesWritten").asLong(0),
                manifest.path("relationshipsWritten").asLong(0));
    }

    /**
     * Persists samples in bounded requests. A complete historic Value attribute can contain
     * hundreds of thousands of samples; sending it as one JSON request can exceed the proxy or
     * web-service request limit and previously resulted in the whole attribute being lost.
     */
    private void addSamplesInChunks(JEVisAttribute attribute, List<JEVisSample> samples) throws JEVisException {
        if (samples == null || samples.isEmpty()) {
            return;
        }

        int total = samples.size();
        logger.info("Importing {} samples into object {} attribute '{}' in chunks of at most {}",
                total, attribute.getObject().getID(), attribute.getName(), SAMPLE_IMPORT_CHUNK_SIZE);

        for (int from = 0; from < total; from += SAMPLE_IMPORT_CHUNK_SIZE) {
            int to = Math.min(from + SAMPLE_IMPORT_CHUNK_SIZE, total);
            addSampleChunkWithRetry(attribute, samples, from, to, total);
        }
    }

    /**
     * Uploads one range and recursively halves it when the reverse proxy reports HTTP 413.
     * The effective request-size limit may differ between JEWebService installations, so a
     * fixed number of samples alone is not reliable.
     */
    private void addSampleChunkWithRetry(JEVisAttribute attribute,
                                         List<JEVisSample> samples,
                                         int from,
                                         int to,
                                         int total) throws JEVisException {
        try {
            attribute.addSamples(samples.subList(from, to));
            logger.info("Imported samples {}-{} of {} into object {} attribute '{}'",
                    from + 1, to, total, attribute.getObject().getID(), attribute.getName());
        } catch (JEVisException e) {
            int size = to - from;
            if (e.getCode() == 413 && size > 1) {
                int middle = from + size / 2;
                logger.warn("Sample request for object {} attribute '{}' with {} samples was too large; retrying as {} and {} samples",
                        attribute.getObject().getID(), attribute.getName(), size,
                        middle - from, to - middle);
                addSampleChunkWithRetry(attribute, samples, from, middle, total);
                addSampleChunkWithRetry(attribute, samples, middle, to, total);
                return;
            }

            throw new JEVisException("Failed importing samples " + (from + 1) + "-" + to
                    + " of " + total + " into object " + attribute.getObject().getID()
                    + " attribute '" + attribute.getName() + "'", e.getCode(), e);
        }
    }

    public Set<Path> listFileDateTimeFolders(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(path -> Files.isDirectory(path) && isFileSampleTimestamp(path.getFileName().toString()))
                    .collect(Collectors.toSet());
        }
    }

    private boolean isFileSampleTimestamp(String value) {
        try {
            parseFileSampleTimestamp(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
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

    private DateTime parseFileSampleTimestamp(String value) {
        if (value != null && value.length() == FILE_DATE_FORMAT_WITH_MILLIS.length()) {
            return DateTime.parse(value, DateTimeFormat.forPattern(FILE_DATE_FORMAT_WITH_MILLIS));
        }
        return DateTime.parse(value, DateTimeFormat.forPattern(FILE_DATE_FORMAT));
    }

    private void extractFile(InputStream zipIn, String filePath) throws IOException {
        try (InputStream input = zipIn;
             BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(bytesIn)) != -1) {
                output.write(bytesIn, 0, read);
            }
        }
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
     *   <li>{@value #RELATIONSHIPS_FILE} containing every non-structural relationship touching an
     *       exported object, including LINK, data-flow, ownership, membership, and role relations.</li>
     *   <li>{@value #EXPORT_MANIFEST_FILE} containing completeness counters and the format version.</li>
     * </ul>
     * {@code PASSWORD_PBKDF2} attributes are intentionally excluded from export.
     *
     * @param file    output file path for the {@code .jex} archive
     * @param objects root objects to export; all their descendants are included recursively
     * @return a Task that performs the export; must be submitted to a thread or executor
     */
    public Task<Void> exportToFileTask(File file, List<JEVisObject> objects) {
        final ProcessReport report = new ProcessReport("Export", file);
        final ExportStats stats = new ExportStats();
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (file == null) {
                    throw new IllegalArgumentException("Export file must not be null");
                }
                if (objects == null || objects.isEmpty()) {
                    throw new IllegalArgumentException("At least one object must be selected for export");
                }
                if (objects.get(0) == null) {
                    throw new IllegalArgumentException("Export root must not be null");
                }
                JEVisDataSource exportDataSource = objects.get(0).getDataSource();
                for (JEVisObject root : objects) {
                    if (root == null || root.getDataSource() != exportDataSource) {
                        throw new IllegalArgumentException("All export roots must belong to the same data source");
                    }
                }

                Path target = file.toPath().toAbsolutePath();
                Path targetDirectory = target.getParent();
                Files.createDirectories(targetDirectory);
                String prefix = target.getFileName().toString();
                if (prefix.length() < 3) {
                    prefix = "jex" + prefix;
                }
                Path temporaryFile = Files.createTempFile(targetDirectory, prefix + ".", ".part");

                try {
                    StringProperty message = new SimpleStringProperty();
                    message.addListener((observable, oldValue, newValue) -> updateMessage(newValue));
                    logger.info("TreeExporter exporter revision: {}", EXPORTER_REVISION);

                    Map<Long, List<JEVisObject>> childrenMap = buildChildrenMap(objects);
                    List<JEVisObject> allObjects = flattenTree(objects, childrenMap);
                    report.expectedObjects = allObjects.size();

                    AtomicReference<Integer> jobNo = new AtomicReference<>(0);
                    int jobCount = allObjects.size();

                    try (OutputStream outputStream = Files.newOutputStream(temporaryFile);
                         ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                        writeZipOutputStream(zipOutputStream, objects, "", message, jobNo, jobCount,
                                childrenMap, new HashSet<>(), stats, report);
                        stats.relationships = exportRelationships(zipOutputStream, allObjects);
                        writeExportManifest(zipOutputStream, objects, allObjects.size(), stats);
                    }

                    if (stats.objects != allObjects.size()) {
                        throw new IOException("Export validation failed: expected " + allObjects.size()
                                + " objects but wrote " + stats.objects);
                    }

                    moveCompletedExport(temporaryFile, target);
                    report.copyExportStats(stats);
                    if (stats.skippedPasswords > 0) {
                        report.info(stats.skippedPasswords
                                + " Passwort-Attribute wurden absichtlich nicht exportiert");
                    }
                    logger.info("Export complete: {} objects, {} attributes, {} scalar samples, {} file samples, {} relationships -> {}",
                            stats.objects, stats.attributes, stats.samples, stats.fileSamples,
                            stats.relationships, target);
                } catch (Exception ex) {
                    report.copyExportStats(stats);
                    try {
                        Files.deleteIfExists(temporaryFile);
                    } catch (IOException cleanupError) {
                        ex.addSuppressed(cleanupError);
                    }
                    logger.error("Export failed. The existing target file was not replaced: {}", target, ex);
                    report.error("Export fehlgeschlagen; eine vorhandene Zieldatei wurde nicht ersetzt", ex);
                    throw ex;
                }

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                showProcessReport(report, null);
            }

            @Override
            protected void failed() {
                super.failed();
                showProcessReport(report, getException());
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                showProcessReport(report,
                        new java.util.concurrent.CancellationException("Export wurde abgebrochen"));
            }
        };
    }

    /**
     * Walks {@code roots} and all their descendants exactly once, fetching each object's children
     * a single time via {@link JEVisObject#getChildren()} and caching the result. Used so that both
     * the job-count/relationship export and the actual ZIP write share one tree walk instead of two,
     * A failure listing any object's children aborts the export so that a partial archive can never
     * replace an existing valid export.
     *
     * @param roots the top-level objects being exported
     * @return map of object ID to its direct children
     */
    private Map<Long, List<JEVisObject>> buildChildrenMap(List<JEVisObject> roots) throws JEVisException {
        Map<Long, List<JEVisObject>> map = new HashMap<>();
        Deque<JEVisObject> queue = new ArrayDeque<>(roots);

        while (!queue.isEmpty()) {
            JEVisObject current = queue.poll();
            if (map.containsKey(current.getID())) {
                continue;
            }

            try {
                List<JEVisObject> children = current.getChildren();
                if (children == null) {
                    children = Collections.emptyList();
                }
                map.put(current.getID(), children);
                queue.addAll(children);
            } catch (Exception e) {
                throw new JEVisException("Could not list children of object " + current.getID()
                        + ". Export aborted to avoid an incomplete archive.", 8236360, e);
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
        Set<Long> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            JEVisObject current = queue.poll();
            if (!visited.add(current.getID())) {
                continue;
            }
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
                                      Map<Long, List<JEVisObject>> childrenMap,
                                      Set<Long> writtenObjectIds,
                                      ExportStats stats,
                                      ProcessReport report) throws Exception {
        for (JEVisObject object : objects) {
            if (!writtenObjectIds.add(object.getID())) {
                logger.warn("Skipping duplicate or cyclic object reference for id {}", object.getID());
                continue;
            }

            jobNo.set(jobNo.get() + 1);
            message.set("Prepare Export Job [" + jobNo.get() + "/" + jobCount + "] object: ["
                    + object.getID() + "] " + object.getName());

            logger.debug("Exporting object: {}:{}", object.getName(), object.getID());

            ZipEntry objectZipEntry = new ZipEntry(folder + "o_" + object.getID() + ".json");
            zipOutputStream.putNextEntry(objectZipEntry);
            try {
                mapper.writeValue(zipOutputStream, toJson(object));
            } finally {
                zipOutputStream.closeEntry();
            }
            stats.objects++;

            // Link objects expose their target's attributes. Exporting those attributes under the
            // link would duplicate target data; the LINK relationship is exported separately.
            if (!"Link".equals(object.getJEVisClassName())) {
                for (JEVisAttribute jeVisAttribute : object.getAttributes()) {
                    logger.debug("Exporting attribute {} of object {}:{}.",
                            jeVisAttribute.getName(), object.getName(), object.getID());

                    int primitiveType = jeVisAttribute.getPrimitiveType();
                    if (primitiveType == JEVisConstants.PrimitiveType.PASSWORD_PBKDF2) {
                        stats.skippedPasswords++;
                        continue;
                    }

                    String attributeName = validateZipPathSegment(jeVisAttribute.getName(), "attribute name");
                    if (primitiveType != JEVisConstants.PrimitiveType.FILE) {
                        ZipEntry attributeZipEntry = new ZipEntry(folder + "a_"
                                + object.getID() + "_" + attributeName + ".json");
                        zipOutputStream.putNextEntry(attributeZipEntry);
                        try {
                            stats.samples += writeAttributeJson(zipOutputStream, jeVisAttribute);
                        } finally {
                            zipOutputStream.closeEntry();
                        }
                        stats.attributes++;
                    } else if (jeVisAttribute.hasSample()) {
                        List<JEVisSample> allSamples = jeVisAttribute.getAllSamples();
                        if (allSamples == null) {
                            throw new IOException("Sample query returned null for FILE attribute "
                                    + jeVisAttribute.getName() + " on object " + object.getID());
                        }
                        int fileNo = 0;
                        for (JEVisSample sample : allSamples) {
                            fileNo++;
                            if (allSamples.size() > 1) {
                                message.set("Prepare Export Job [" + jobNo.get() + "/" + jobCount + "] object: ["
                                        + object.getID() + "] " + object.getName()
                                        + " — attachment " + fileNo + "/" + allSamples.size());
                            }

                            String exportedFilename = sample.getValueAsString();
                            JEVisFile sampleFile = sample.getValueAsFile();
                            if (sampleFile == null || sampleFile.getBytes() == null) {
                                logger.warn("Retrying missing file content for '{}' at {} on object {} attribute '{}'",
                                        exportedFilename, sample.getTimestamp(), object.getID(), jeVisAttribute.getName());
                                sampleFile = sample.getValueAsFile();
                            }

                            String filename = sampleFile != null && sampleFile.getFilename() != null
                                    && !sampleFile.getFilename().trim().isEmpty()
                                    ? sampleFile.getFilename() : exportedFilename;
                            byte[] bytes = sampleFile != null ? sampleFile.getBytes() : null;
                            if (filename == null || filename.trim().isEmpty() || bytes == null) {
                                String description = "Datei-Sample '" + String.valueOf(filename)
                                        + "' vom " + sample.getTimestamp() + " bei Objekt " + object.getID()
                                        + ", Attribut '" + jeVisAttribute.getName()
                                        + "' hat keinen abrufbaren Inhalt und wurde übersprungen";
                                logger.warn(description);
                                report.warning(description);
                                report.skipped++;
                                continue;
                            }

                            ZipEntry sampleFileZipEntry = new ZipEntry(folder + "a_"
                                    + object.getID() + "_" + attributeName
                                    + "/" + sample.getTimestamp().toString(FILE_DATE_FORMAT_WITH_MILLIS)
                                    + "/" + sanitizeFileName(filename));
                            zipOutputStream.putNextEntry(sampleFileZipEntry);
                            try {
                                zipOutputStream.write(bytes);
                            } finally {
                                zipOutputStream.closeEntry();
                            }
                            stats.fileSamples++;
                        }
                        stats.attributes++;
                    }
                }
            }

            String newFolder = folder + object.getID() + "/";
            List<JEVisObject> children = childrenMap.getOrDefault(object.getID(), Collections.emptyList());
            writeZipOutputStream(zipOutputStream, children, newFolder, message, jobNo, jobCount,
                    childrenMap, writtenObjectIds, stats, report);
        }
    }

    /**
     * Collects every non-structural relationship touching an exported object and writes it to
     * {@value #RELATIONSHIPS_FILE}. PARENT is represented by the ZIP directory hierarchy and
     * DELETED_PARENT is intentionally excluded.
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
    private long exportRelationships(ZipOutputStream zipOutputStream, List<JEVisObject> allObjects) throws Exception {
        List<JsonRelationship> relationships = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (JEVisObject object : allObjects) {
            for (JEVisRelationship rel : object.getRelationships()) {
                // The directory hierarchy already represents PARENT. DELETED_PARENT describes
                // trash/history state and must not be recreated in a normal import.
                if (rel.getType() == JEVisConstants.ObjectRelationship.PARENT
                        || rel.getType() == JEVisConstants.ObjectRelationship.DELETED_PARENT) {
                    continue;
                }

                String key = rel.getStartID() + "_" + rel.getEndID() + "_" + rel.getType();
                if (seen.add(key)) {
                    JsonRelationship jsonRelationship = new JsonRelationship();
                    jsonRelationship.setFrom(rel.getStartID());
                    jsonRelationship.setTo(rel.getEndID());
                    jsonRelationship.setType(rel.getType());
                    relationships.add(jsonRelationship);
                }
            }
        }

        ZipEntry relEntry = new ZipEntry(RELATIONSHIPS_FILE);
        zipOutputStream.putNextEntry(relEntry);
        try {
            mapper.writeValue(zipOutputStream, relationships);
        } finally {
            zipOutputStream.closeEntry();
        }
        logger.info("Exported {} relationships to {}", relationships.size(), RELATIONSHIPS_FILE);
        return relationships.size();
    }

    private void writeExportManifest(ZipOutputStream zipOutputStream,
                                     List<JEVisObject> roots,
                                     int expectedObjects,
                                     ExportStats stats) throws IOException {
        ObjectNode manifest = mapper.createObjectNode();
        manifest.put("formatVersion", 2);
        manifest.put("exporterRevision", EXPORTER_REVISION);
        manifest.put("created", new DateTime().toString());
        manifest.put("complete", stats.objects == expectedObjects);
        manifest.put("objectsExpected", expectedObjects);
        manifest.put("objectsWritten", stats.objects);
        manifest.put("attributesWritten", stats.attributes);
        manifest.put("samplesWritten", stats.samples);
        manifest.put("fileSamplesWritten", stats.fileSamples);
        manifest.put("relationshipsWritten", stats.relationships);
        manifest.put("passwordAttributesSkipped", stats.skippedPasswords);
        ArrayNode rootIds = manifest.putArray("rootObjectIds");
        for (JEVisObject root : roots) {
            rootIds.add(root.getID());
        }

        ZipEntry manifestEntry = new ZipEntry(EXPORT_MANIFEST_FILE);
        zipOutputStream.putNextEntry(manifestEntry);
        try {
            mapper.writeValue(zipOutputStream, manifest);
        } finally {
            zipOutputStream.closeEntry();
        }
    }

    private void moveCompletedExport(Path temporaryFile, Path target) throws IOException {
        try {
            Files.move(temporaryFile, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporaryFile, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String validateZipPathSegment(String value, String description) throws IOException {
        if (value == null || value.trim().isEmpty()
                || value.contains("/") || value.contains("\\") || value.contains("..")) {
            throw new IOException("Unsafe " + description + " for ZIP entry: " + value);
        }
        return value;
    }

    private String sanitizeFileName(String filename) throws IOException {
        String normalized = filename.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String baseName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        baseName = baseName.replace("..", "_").replaceAll("[\\p{Cntrl}]", "_").trim();
        if (baseName.isEmpty()) {
            throw new IOException("Unsafe or empty file name in FILE sample: " + filename);
        }
        return baseName;
    }

    /**
     * Post-import phase D: recreates functional and access-control relationships from
     * {@value #RELATIONSHIPS_FILE} in the extracted archive.
     *
     * <p>Resolution strategy (in priority order):
     * <ol>
     *   <li>Check {@code createdObjects} — the object was part of this import and received a new ID.</li>
     *   <li>If the ID belongs to an archive object whose creation failed, return unresolved without
     *       querying the target server with the obsolete ID.</li>
     *   <li>For external references only, fall back to {@code ds.getObject(oldId)} — the object already exists on the target
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
     * @param archiveObjectIds all object IDs represented by object files in the archive
     */
    private void importRelationships(JEVisDataSource ds, Path tmpDir,
                                     Map<Long, JEVisObject> createdObjects,
                                     Set<Long> archiveObjectIds,
                                     ProcessReport report) {
        Path relFile = tmpDir.resolve(RELATIONSHIPS_FILE);
        if (!Files.exists(relFile)) {
            logger.info("No {} in archive — skipping relationship import", RELATIONSHIPS_FILE);
            report.info("Das Archiv enthält keine " + RELATIONSHIPS_FILE
                    + " (bei älteren Exporten normal)");
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
            Map<Long, Long> resolvedIds = new HashMap<>();
            Set<Long> warnedUnresolvedIds = new HashSet<>();

            for (int i = 0; i < relationships.size(); i++) {
                JsonRelationship rel = relationships.get(i);
                try {
                    long newFrom = resolveIdForRelationship(ds, createdObjects, archiveObjectIds,
                            resolvedIds, rel.getFrom());
                    long newTo = resolveIdForRelationship(ds, createdObjects, archiveObjectIds,
                            resolvedIds, rel.getTo());

                    if (newFrom > 0 && newTo > 0) {
                        JEVisRelationship newRel = ds.buildRelationship(newFrom, newTo, rel.getType());
                        if (newRel != null) {
                            created++;
                            consecutiveFailures = 0;
                        } else {
                            logger.warn("Failed to recreate relationship: from={} to={} type={}",
                                    newFrom, newTo, rel.getType());
                            skipped++;
                            report.warning("Beziehung konnte nicht erstellt werden: "
                                    + newFrom + " -> " + newTo + " (Typ " + rel.getType() + ")");
                            consecutiveFailures++;
                        }
                    } else {
                        if (newFrom <= 0 && warnedUnresolvedIds.add(rel.getFrom())) {
                            logger.warn("Cannot resolve relationship endpoint {}{}",
                                    rel.getFrom(), archiveObjectIds.contains(rel.getFrom())
                                            ? " because its object file could not be imported"
                                            : " because it does not exist on the target system");
                        }
                        if (newTo <= 0 && warnedUnresolvedIds.add(rel.getTo())) {
                            logger.warn("Cannot resolve relationship endpoint {}{}",
                                    rel.getTo(), archiveObjectIds.contains(rel.getTo())
                                            ? " because its object file could not be imported"
                                            : " because it does not exist on the target system");
                        }
                        logger.debug("Skipping unresolvable relationship: from={} (resolved={}) to={} (resolved={}) type={}",
                                rel.getFrom(), newFrom, rel.getTo(), newTo, rel.getType());
                        skipped++;
                        report.warning("Beziehung mit nicht auflösbarem Endpunkt übersprungen: "
                                + rel.getFrom() + " -> " + rel.getTo() + " (Typ " + rel.getType() + ")");
                    }
                } catch (Exception e) {
                    logger.error("Failed to recreate relationship {}", rel, e);
                    skipped++;
                    report.error("Beziehung konnte nicht erstellt werden: " + rel, e);
                    consecutiveFailures++;
                }

                if (consecutiveFailures >= FAILURE_ABORT_THRESHOLD) {
                    int remaining = relationships.size() - (i + 1);
                    skipped += remaining;
                    logger.error("Aborting relationship import after {} consecutive failures — the target server may not support the relationship API (older JEWebService version?). {} relationships were not attempted.",
                            consecutiveFailures, remaining);
                    report.error("Beziehungsimport nach " + consecutiveFailures
                            + " aufeinanderfolgenden Fehlern abgebrochen; " + remaining
                            + " Beziehungen wurden nicht versucht", null);
                    break;
                }
            }

            logger.info("Relationship import complete: {} created, {} skipped", created, skipped);
            report.relationships += created;
            report.skipped += skipped;
            ds.updateAccessControl();
        } catch (Exception e) {
            logger.error("Failed to load or apply {}", RELATIONSHIPS_FILE, e);
            report.error(RELATIONSHIPS_FILE + " konnte nicht geladen oder angewendet werden", e);
        }
    }

    /**
     * Resolves an old (exported) object ID to the current system's object ID.
     *
     * <p>Tries {@code createdObjects} first (object was re-created during import). An ID represented
     * by an object file but missing from that map is known to have failed import and is not looked up
     * under its obsolete ID. Only external references fall back to a live datasource lookup.
     *
     * @param ds             live datasource for fallback lookup
     * @param createdObjects mapping of old IDs to newly created objects
     * @param archiveObjectIds all IDs represented by object files in the archive
     * @param resolvedIds    per-import cache, including failed resolutions ({@code -1})
     * @param oldId          the object ID as it appeared in the export
     * @return the resolved ID on the current system, or {@code -1} if the object cannot be found
     */
    private long resolveIdForRelationship(JEVisDataSource ds,
                                          Map<Long, JEVisObject> createdObjects,
                                          Set<Long> archiveObjectIds,
                                          Map<Long, Long> resolvedIds,
                                          long oldId) {
        Long cached = resolvedIds.get(oldId);
        if (cached != null) return cached;

        JEVisObject obj = createdObjects.get(oldId);
        if (obj != null) {
            resolvedIds.put(oldId, obj.getID());
            return obj.getID();
        }

        // The ID belongs to this archive but was not created. A lookup by the old ID on the
        // target server cannot resolve it and only produces a misleading HTTP 404 response.
        if (archiveObjectIds.contains(oldId)) {
            resolvedIds.put(oldId, -1L);
            return -1L;
        }

        try {
            obj = ds.getObject(oldId);
            if (obj != null) {
                resolvedIds.put(oldId, obj.getID());
                return obj.getID();
            }
        } catch (Exception ignored) {
        }
        resolvedIds.put(oldId, -1L);
        return -1L;
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
    private long writeAttributeJson(ZipOutputStream out, JEVisAttribute attribute) throws Exception {
        logger.info("Writing attribute {} of object {}:{}",
                attribute.getName(), attribute.getObject().getName(), attribute.getObject().getID());

        JsonGenerator gen = mapper.getFactory().createGenerator(out);
        gen.setCodec(mapper);
        gen.writeStartObject();
        gen.writeStringField(ATTRIBUTE_NAME, attribute.getName());
        long writtenSamples = 0;

        if (attribute.getInputSampleRate() != null) {
            gen.writeStringField(ATTRIBUTE_RATE, attribute.getInputSampleRate().toString());
        }

        if (attribute.getInputUnit() != null) {
            gen.writeObjectField(ATTRIBUTE_UNIT, JsonFactory.buildUnit(attribute.getInputUnit()));
        }

        if (attribute.hasSample()) {
            List<JEVisSample> allSamples = attribute.getAllSamples();
            if (allSamples == null) {
                throw new IOException("Sample query returned null for attribute "
                        + attribute.getName() + " on object " + attribute.getObjectID());
            }

            logger.info("Found {} samples on attribute {}. Writing samples.",
                    allSamples.size(), attribute.getName());

            gen.writeArrayFieldStart(ATTRIBUTE_SAMPLES);
            if (isScalarPrimitiveType(attribute.getPrimitiveType())) {
                for (JEVisSample jeVisSample : allSamples) {
                    gen.writeStartObject();
                    gen.writeStringField(SAMPLE_TS, jeVisSample.getTimestamp().toString());
                    gen.writeStringField(SAMPLE_VALUE, jeVisSample.getValueAsString());
                    String note = jeVisSample.getNote();
                    gen.writeStringField(NOTE, note != null ? note : "");
                    gen.writeEndObject();
                    writtenSamples++;
                }
            }
            gen.writeEndArray();
        }

        gen.writeEndObject();
        gen.close();
        return writtenSamples;
    }

    public ObjectNode toJson(JEVisObject object) throws JEVisException {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put(OBJECT_NAME, object.getName());

        logger.info("Created object {}", object.getName());

        objectNode.put(OBJECT_CLASS, object.getJEVisClassName());
        ArrayNode arrayNode = objectNode.putArray(OBJECT_LANG);

        for (Map.Entry<String, String> entry : object.getLocalNameList().entrySet()) {
            String lang = entry.getKey();
            String translatedName = entry.getValue();

            ObjectNode langNode = JsonNodeFactory.instance.objectNode();
            langNode.put(lang, translatedName);
            arrayNode.add(langNode);
        }

        return objectNode;
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

    private static final class ExportStats {
        private long objects;
        private long attributes;
        private long samples;
        private long fileSamples;
        private long relationships;
        private long skippedPasswords;
    }

    private static final class ProcessReport {
        private static final int MAX_DETAILS = 250;
        private final String operation;
        private final String sourceOrTarget;
        private final long startedAt = System.currentTimeMillis();
        private final List<String> details = new ArrayList<>();
        private long expectedObjects;
        private long objects;
        private long attributes;
        private long samples;
        private long fileSamples;
        private long relationships;
        private long skipped;
        private long warnings;
        private long errors;
        private long omittedDetails;

        private ProcessReport(String operation, File file) {
            this.operation = operation;
            this.sourceOrTarget = file == null ? "" : file.getAbsolutePath();
        }

        private void info(String text) {
            addDetail("INFO", text, null);
        }

        private void warning(String text) {
            warnings++;
            addDetail("WARNUNG", text, null);
        }

        private void error(String text, Throwable throwable) {
            errors++;
            addDetail("FEHLER", text, throwable);
        }

        private void addDetail(String level, String text, Throwable throwable) {
            if (details.size() >= MAX_DETAILS) {
                omittedDetails++;
                return;
            }
            StringBuilder line = new StringBuilder(level).append(": ").append(text);
            if (throwable != null && throwable.getMessage() != null
                    && !throwable.getMessage().trim().isEmpty()) {
                line.append(" (").append(throwable.getMessage()).append(')');
            }
            details.add(line.toString());
        }

        private void copyExportStats(ExportStats stats) {
            objects = stats.objects;
            attributes = stats.attributes;
            samples = stats.samples;
            fileSamples = stats.fileSamples;
            relationships = stats.relationships;
            skipped = stats.skippedPasswords;
        }
    }
}
