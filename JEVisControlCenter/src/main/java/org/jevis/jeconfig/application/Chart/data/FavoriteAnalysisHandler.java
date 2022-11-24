package org.jevis.jeconfig.application.Chart.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class FavoriteAnalysisHandler {

    public final static String TYPE = "FavoriteAnalysisHandler";
    public static final String FAVORITE_ANALYSES_FILE_ATTRIBUTE_NAME = "Favorite Analyses";
    private static final Logger logger = LogManager.getLogger(FavoriteAnalysisHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private final SimpleObjectProperty<FavoriteAnalyses> favoriteAnalyses = new SimpleObjectProperty<>(new FavoriteAnalyses());

    public FavoriteAnalysisHandler() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void jsonToModel(JsonNode jsonNode) {
        try {
            this.mapper.readerForUpdating(getFavoriteAnalyses()).treeToValue(jsonNode, FavoriteAnalyses.class);
        } catch (Exception e) {
            logger.error("Could not parse json model", e);
        }
    }

    public JsonNode toJsonNode() {

        FavoriteAnalyses favoriteAnalysesModel = getFavoriteAnalyses();

        ObjectNode dataHandlerNode = JsonNodeFactory.instance.objectNode();

        ArrayNode favoriteAnalyses = JsonNodeFactory.instance.arrayNode();

        for (FavoriteAnalysis favoriteAnalysis : favoriteAnalysesModel.getFavoriteAnalyses()) {
            ObjectNode favoriteAnalysisModel = JsonNodeFactory.instance.objectNode();

            favoriteAnalysisModel.put("id", favoriteAnalysis.getId());
            favoriteAnalysisModel.put("name", favoriteAnalysis.getName());
            favoriteAnalysisModel.put("timeFrame", favoriteAnalysis.getTimeFrame().toString());
            if (favoriteAnalysis.getTimeFrame() == TimeFrame.CUSTOM) {
                favoriteAnalysisModel.put("start", favoriteAnalysis.getStart());
                favoriteAnalysisModel.put("end", favoriteAnalysis.getEnd());
            }

            favoriteAnalysisModel.put("aggregationPeriod", favoriteAnalysis.getAggregationPeriod().toString());
            favoriteAnalysisModel.put("manipulationMode", favoriteAnalysis.getManipulationMode().toString());

            favoriteAnalyses.add(favoriteAnalysisModel);
        }

        dataHandlerNode.put("type", TYPE);

        dataHandlerNode.set("favoriteAnalyses", favoriteAnalyses);

        return dataHandlerNode;

    }

    public void loadDataModel() {
        getFavoriteAnalyses().reset();

        try {
            JEVisObject userObject = JEConfig.getDataSource().getCurrentUser().getUserObject();

            JEVisAttribute favoriteAnalysesFileAttribute = userObject.getAttribute(FAVORITE_ANALYSES_FILE_ATTRIBUTE_NAME);
            if (favoriteAnalysesFileAttribute.hasSample()) {
                JEVisFile file = favoriteAnalysesFileAttribute.getLatestSample().getValueAsFile();
                JsonNode jsonNode = mapper.readTree(file.getBytes());
                jsonToModel(jsonNode);
            }
        } catch (Exception e) {
            logger.error("Could not read favorite analyses file", e);
        }
    }

    public void saveDataModel() {

        try {
            JsonNode analysisNode = toJsonNode();
            JEVisObject userObject = JEConfig.getDataSource().getCurrentUser().getUserObject();
            JEVisAttribute favoriteAnalysisFileAttribute = userObject.getAttribute(FAVORITE_ANALYSES_FILE_ATTRIBUTE_NAME);
            JEVisFileImp jsonFile = new JEVisFileImp(
                    userObject.getLocalName(I18n.getInstance().getLocale().getLanguage()) + "_" + DateTime.now().toString("yyyyMMddHHmmss") + ".json"
                    , this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(analysisNode).getBytes(StandardCharsets.UTF_8));
            JEVisSample newSample = favoriteAnalysisFileAttribute.buildSample(new DateTime(), jsonFile);
            newSample.commit();

        } catch (Exception e) {
            logger.error("Could not write favorite analyses file", e);
        }
    }

    public FavoriteAnalyses getFavoriteAnalyses() {
        return favoriteAnalyses.get();
    }

    public void setFavoriteAnalyses(FavoriteAnalyses favoriteAnalyses) {
        this.favoriteAnalyses.set(favoriteAnalyses);
    }

    public List<FavoriteAnalysis> getFavoriteAnalysesList() {
        return favoriteAnalyses.get().getFavoriteAnalyses();
    }

    public SimpleObjectProperty<FavoriteAnalyses> favoriteAnalysesProperty() {
        return favoriteAnalyses;
    }
}
