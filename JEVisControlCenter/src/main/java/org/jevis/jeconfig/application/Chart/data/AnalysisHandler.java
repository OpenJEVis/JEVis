package org.jevis.jeconfig.application.Chart.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;

/**
 * Serialises and deserialises a {@link DataModel} to/from JSON, and loads/saves
 * analysis configurations stored as {@link org.jevis.api.JEVisFile} samples on a
 * JEVis analysis object.
 * <p>
 * The JSON format is owned by this class — field names used in
 * {@link #toJsonNode(DataModel)} must match the Jackson property names on
 * {@link DataModel} / {@link ChartModel} / {@link ChartData} for round-tripping to work.
 * <p>
 * Backwards compatibility with the pre-JSON "old model" format is handled via
 * {@link #convertOldToNew(AnalysisDataModel, DataModel)}.
 *
 * @see DataModel
 * @see ChartModel
 * @see ChartData
 */
public class AnalysisHandler {

    public final static String TYPE = "AnalysisHandler";
    public static final String ANALYSIS_FILE_ATTRIBUTE_NAME = "Analysis File";
    private static final Logger logger = LogManager.getLogger(AnalysisHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();

    public AnalysisHandler() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void jsonToModel(JsonNode jsonNode, DataModel dataModel) {
        try {
            this.mapper.readerForUpdating(dataModel).treeToValue(jsonNode, DataModel.class);
        } catch (Exception e) {
            logger.error("Could not parse json model", e);
        }
    }

    /**
     * Serialises a {@link DataModel} to a Jackson {@link JsonNode}.
     * <p>
     * The resulting node can be written to a JSON file via Jackson's
     * {@code ObjectMapper.writerWithDefaultPrettyPrinter()} and later
     * deserialised back by passing it to {@link #jsonToModel(JsonNode, DataModel)}.
     *
     * @param dataModel the model to serialise
     * @return a JSON object node representing the full analysis configuration
     */
    public JsonNode toJsonNode(DataModel dataModel) {

        ObjectNode dataHandlerNode = JsonNodeFactory.instance.objectNode();

        ArrayNode chartModels = JsonNodeFactory.instance.arrayNode();

        for (ChartModel chartModel : dataModel.getChartModels()) {
            ObjectNode chartModelNode = JsonNodeFactory.instance.objectNode();

            ArrayNode chartDataList = JsonNodeFactory.instance.arrayNode();

            for (ChartData chartData : chartModel.getChartData()) {
                ObjectNode chartDataNode = JsonNodeFactory.instance.objectNode();

                chartDataNode.put("id", chartData.getId());
                chartDataNode.put("attributeString", chartData.getAttributeString());

                if (chartData.getUnit() != null) {
                    chartDataNode.put("unitPrefix", chartData.getUnit().getPrefix().toString());
                    chartDataNode.put("unitFormula", chartData.getUnit().getFormula());
                    chartDataNode.put("unitLabel", chartData.getUnit().getLabel());
                }

                chartDataNode.put("name", chartData.getName());

                if (chartData.getColor() != null) {
                    chartDataNode.put("color", chartData.getColor().toString());
                }

                chartDataNode.put("axis", chartData.getAxis());
                chartDataNode.put("calculation", chartData.isCalculation());
                chartDataNode.put("calculationId", chartData.getCalculationId());

                if (chartData.getBubbleType() != null) {
                    chartDataNode.put("bubbleType", chartData.getBubbleType().toString());
                }

                if (chartData.getChartType() != null) {
                    chartDataNode.put("chartType", chartData.getChartType().toString());
                }

                if (chartData.getAggregationPeriod() != null) {
                    chartDataNode.put("aggregationPeriod", chartData.getAggregationPeriod().toString());
                }

                if (chartData.getManipulationMode() != null) {
                    chartDataNode.put("manipulationMode", chartData.getManipulationMode().toString());
                }

                chartDataNode.put("decimalDigits", chartData.getDecimalDigits());

                chartDataNode.put("css", chartData.getCss());

                chartDataNode.put("intervalStart", chartData.getIntervalStart());
                chartDataNode.put("intervalEnd", chartData.getIntervalEnd());
                chartDataNode.put("intervalEnabled", chartData.isIntervalEnabled());

                chartDataList.add(chartDataNode);
            }

            chartModelNode.put("chartId", chartModel.getChartId());
            chartModelNode.put("chartName", chartModel.getChartName());

            if (chartModel.getChartType() != null) {
                chartModelNode.put("chartType", chartModel.getChartType().toString());
            }

            chartModelNode.put("minFractionDigits", chartModel.getMinFractionDigits());
            chartModelNode.put("maxFractionDigits", chartModel.getMaxFractionDigits());
            chartModelNode.put("groupingInterval", chartModel.getGroupingInterval());
            chartModelNode.put("height", chartModel.getHeight());

            if (chartModel.getColorMapping() != null) {
                chartModelNode.put("colorMapping", chartModel.getColorMapping().toString());
            }

            if (chartModel.getOrientation() != null) {
                chartModelNode.put("orientation", chartModel.getOrientation().toString());
            }

            if (chartModel.getDayStart() != null) {
                chartModelNode.put("dayStart", chartModel.getDayStart().toString());
            }
            if (chartModel.getDayEnd() != null) {
                chartModelNode.put("dayEnd", chartModel.getDayEnd().toString());
            }

            chartModelNode.put("filterEnabled", chartModel.isFilterEnabled());
            chartModelNode.put("fixYAxisToZero", chartModel.isFixYAxisToZero());
            chartModelNode.put("coloringEnabled", chartModel.isColoringEnabled());
            chartModelNode.put("showColumnSums", chartModel.isShowColumnSums());
            chartModelNode.put("showRowSums", chartModel.isShowRowSums());

            if (chartModel.getxAxisTitle() != null) {
                chartModelNode.put("xAxisTitle", chartModel.getxAxisTitle());
            }

            if (chartModel.getyAxisTitle() != null) {
                chartModelNode.put("yAxisTitle", chartModel.getyAxisTitle());
            }

            chartModelNode.set("chartData", chartDataList);

            chartModels.add(chartModelNode);

        }

        dataHandlerNode.put("type", TYPE);
        dataHandlerNode.put("autoSize", dataModel.isAutoSize());
        dataHandlerNode.put("chartsPerScreen", dataModel.getChartsPerScreen());
        dataHandlerNode.put("horizontalPies", dataModel.getHorizontalPies());
        dataHandlerNode.put("horizontalTables", dataModel.getHorizontalTables());
        dataHandlerNode.put("forcedInterval", dataModel.getForcedInterval());

        dataHandlerNode.set("chartModels", chartModels);

        return dataHandlerNode;

    }

    /**
     * Loads the analysis configuration stored on {@code analysisObject} into {@code dataModel}.
     * <p>
     * If the object carries an {@value #ANALYSIS_FILE_ATTRIBUTE_NAME} sample, the latest JSON
     * file is parsed and merged into {@code dataModel} using Jackson's
     * {@code readerForUpdating}. Otherwise the legacy attribute-based format is read via
     * {@link AnalysisDataModel} and converted with {@link #convertOldToNew}.
     * <p>
     * Always calls {@link DataModel#reset()} first to avoid stale state from a previous load.
     *
     * @param analysisObject the JEVis analysis object to load from
     * @param dataModel      the target model; its state is fully replaced
     */
    public void loadDataModel(JEVisObject analysisObject, DataModel dataModel) {
        dataModel.reset();

        try {
            JEVisAttribute analysisFileAttribute = analysisObject.getAttribute(ANALYSIS_FILE_ATTRIBUTE_NAME);
            if (analysisFileAttribute != null && analysisFileAttribute.hasSample()) {
                JEVisSample latestSample = analysisFileAttribute.getLatestSample();
                if (latestSample == null) return;
                JEVisFile file = latestSample.getValueAsFile();
                JsonNode jsonNode = mapper.readTree(file.getBytes());
                jsonToModel(jsonNode, dataModel);

                for (ChartModel chartModel : dataModel.getChartModels()) {
                    for (ChartData chartData : chartModel.getChartData()) {
                        try {
                            JEVisObject object = analysisObject.getDataSource().getObject(chartData.getId());
                            chartData.setObjectName(object);
                        } catch (Exception ignored) {
                        }
                    }
                }
            } else {
                AnalysisDataModel analysisDataModel = new AnalysisDataModel(analysisObject.getDataSource(), analysisObject);
                analysisDataModel.getSelectedData();

                convertOldToNew(analysisDataModel, dataModel);
            }
        } catch (Exception e) {
            logger.error("Could not read template file from object {}", analysisObject);
        }
    }

    /**
     * Persists the current {@link DataModel} as a JSON file sample on {@code analysisObject}.
     * <p>
     * The model is serialised via {@link #toJsonNode(DataModel)}, written to a
     * {@link org.jevis.commons.JEVisFileImp} whose filename includes the current timestamp,
     * and committed as a new sample on the {@value #ANALYSIS_FILE_ATTRIBUTE_NAME} attribute.
     *
     * @param analysisObject the target JEVis object to write to; no-op if {@code null}
     * @param dataModel      the model to persist
     */
    public void saveDataModel(JEVisObject analysisObject, DataModel dataModel) {

        try {
            JsonNode analysisNode = toJsonNode(dataModel);

            if (analysisObject != null) {
                JEVisAttribute analysisFileAttribute = analysisObject.getAttribute(ANALYSIS_FILE_ATTRIBUTE_NAME);
                JEVisFileImp jsonFile = new JEVisFileImp(
                        analysisObject.getLocalName(I18n.getInstance().getLocale().getLanguage()) + "_" + DateTime.now().toString("yyyyMMddHHmmss") + ".json"
                        , this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(analysisNode).getBytes(StandardCharsets.UTF_8));
                JEVisSample newSample = analysisFileAttribute.buildSample(new DateTime(), jsonFile);
                newSample.commit();
            }
        } catch (Exception e) {
            logger.error("Could not write template file to object {}", analysisObject);
        }
    }

    /**
     * Converts a legacy {@link AnalysisDataModel} (attribute-based) to the current
     * {@link DataModel} (JSON-based) format.
     * <p>
     * Called automatically by {@link #loadDataModel} when no JSON analysis file is found
     * on the analysis object.
     *
     * @param oldModel  the legacy model read from JEVis attributes
     * @param dataModel the target new-format model to populate
     */
    private void convertOldToNew(AnalysisDataModel oldModel, DataModel dataModel) {

        dataModel.setAutoSize(oldModel.getAutoResize());
        dataModel.setChartsPerScreen(oldModel.getChartsPerScreen().intValue());
        dataModel.setHorizontalPies(oldModel.getHorizontalPies().intValue());
        dataModel.setHorizontalTables(oldModel.getHorizontalTables().intValue());

        for (ChartSetting listSetting : oldModel.getCharts().getListSettings()) {
            ChartModel chartModel = new ChartModel();

            chartModel.setChartId(listSetting.getId());
            chartModel.setChartName(listSetting.getName());
            chartModel.setChartType(listSetting.getChartType());
            chartModel.setHeight(listSetting.getHeight());
            chartModel.setGroupingInterval(listSetting.getGroupingInterval());
            chartModel.setMinFractionDigits(listSetting.getMinFractionDigits());
            chartModel.setMaxFractionDigits(listSetting.getMaxFractionDigits());
            chartModel.setColorMapping(listSetting.getColorMapping());
            chartModel.setOrientation(listSetting.getOrientation());

            for (ChartDataRow chartDataRow : oldModel.getSelectedData()) {
                if (chartDataRow.getSelectedCharts().contains(listSetting.getId())) {
                    ChartData chartData = new ChartData();
                    chartData.setId(chartDataRow.getObject().getID());
                    chartData.setObjectName(chartDataRow.getObject());

                    if (chartDataRow.getDataProcessor() != null) {
                        chartData.setId(chartDataRow.getDataProcessor().getID());
                        chartData.setObjectName(chartDataRow.getDataProcessor());
                    }

                    chartData.setAttributeString(chartDataRow.getAttribute().getName());

                    chartData.setCalculation(chartDataRow.isCalculation());
                    if (chartDataRow.isCalculation()) {
                        chartData.setCalculationId(chartDataRow.getCalculationObject().getID());
                    }

                    chartData.setName(chartDataRow.getName());
                    chartData.setChartType(chartDataRow.getChartType());
                    chartData.setBubbleType(chartDataRow.getBubbleType());
                    chartData.setColor(chartDataRow.getColor());
                    chartData.setUnit(chartDataRow.getUnit());
                    chartData.setAxis(chartDataRow.getAxis());
                    chartModel.getChartData().add(chartData);
                }
            }

            dataModel.getChartModels().add(chartModel);
        }
    }

    public void restoreDataModel(DataModel dataModel, JEVisObject currentAnalysis) {
        loadDataModel(currentAnalysis, dataModel);
    }
}
