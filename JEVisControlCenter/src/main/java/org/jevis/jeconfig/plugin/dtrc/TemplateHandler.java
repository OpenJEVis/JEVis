package org.jevis.jeconfig.plugin.dtrc;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.ArrayList;
import java.util.List;

public class TemplateHandler {
    public final static String TYPE = "SimpleDataHandler";
    private static final Logger logger = LogManager.getLogger(TemplateHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private RCTemplate rcTemplate;
    private JEVisObject templateObject;
    private String Title;

    public TemplateHandler() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public RCTemplate getRcTemplate() {
        if (rcTemplate == null && templateObject != null) {

        }

        return rcTemplate;
    }

    public void setRcTemplate(RCTemplate rcTemplate) {
        this.rcTemplate = rcTemplate;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void jsonToModel(JsonNode jsonNode) {
        try {
            this.rcTemplate = this.mapper.treeToValue(jsonNode, RCTemplate.class);

            removeOldStyleFormulaInputs();

            createFormulaInputs();

            checkFormulaInputs();
        } catch (JsonProcessingException e) {
            logger.error("Could not parse json model", e);
        }
    }

    private void checkFormulaInputs() {

        for (TemplateFormula templateFormula : getRcTemplate().getTemplateFormulas()) {
            List<String> inputIdsToRemove = new ArrayList<>();
            for (String inputId : templateFormula.getInputIds()) {
                boolean found = getRcTemplate().getTemplateFormulas().stream().anyMatch(formula -> inputId.equals(formula.getId()));

                if (!found) {
                    found = getRcTemplate().getTemplateInputs().stream().anyMatch(input -> inputId.equals(input.getId()));
                }

                if (!found) {
                    inputIdsToRemove.add(inputId);
                    logger.debug("Could not find input with Id {} in formula {} with formula of {}. Will be removed on save.", inputId, templateFormula.getName(), templateFormula.getFormula());
                }
            }

            templateFormula.getInputIds().removeAll(inputIdsToRemove);
        }
    }

    private void removeOldStyleFormulaInputs() {
        List<TemplateInput> inputsToRemove = new ArrayList<>();
        for (TemplateInput templateInput : getRcTemplate().getTemplateInputs()) {
            if (templateInput.getVariableType().equals(InputVariableType.FORMULA.toString())) {
                inputsToRemove.add(templateInput);
            }
        }

        getRcTemplate().getTemplateInputs().removeAll(inputsToRemove);
    }

    private void createFormulaInputs() {
        for (TemplateFormula templateFormula : getRcTemplate().getTemplateFormulas()) {
            TemplateInput formulaInput = new TemplateInput();
            formulaInput.setVariableName(templateFormula.getName());
            formulaInput.setTemplateFormula(templateFormula.getId());
            formulaInput.setVariableType(InputVariableType.FORMULA.toString());
            getRcTemplate().getTemplateFormulaInputs().add(formulaInput);
        }
    }

    public JsonNode toJsonNode() {
        ObjectNode dataHandlerNode = JsonNodeFactory.instance.objectNode();

        ArrayNode templateInputsArrayNode = JsonNodeFactory.instance.arrayNode();
        this.rcTemplate.getTemplateInputs().forEach(templateInput -> {

            ObjectNode inputNode = JsonNodeFactory.instance.objectNode();
            inputNode.put("objectClass", templateInput.getObjectClass());
            inputNode.put("id", templateInput.getId());
            inputNode.put("quantity", templateInput.isQuantity());
            inputNode.put("attributeName", templateInput.getAttributeName());
            inputNode.put("variableName", templateInput.getVariableName());
            inputNode.put("variableType", templateInput.getVariableType());
            inputNode.put("templateFormula", templateInput.getTemplateFormula());
            inputNode.put("dependency", templateInput.getDependency());
            inputNode.put("filter", templateInput.getFilter());
            inputNode.put("group", templateInput.getGroup());
            inputNode.put("timeRestrictionEnabled", templateInput.getTimeRestrictionEnabled());
            inputNode.put("fixedTimeFrame", templateInput.getFixedTimeFrame());
            inputNode.put("reducingTimeFrame", templateInput.getReducingTimeFrame());

            templateInputsArrayNode.add(inputNode);
        });

        ArrayNode templateOutputsArrayNode = JsonNodeFactory.instance.arrayNode();
        this.rcTemplate.getTemplateOutputs().forEach(templateOutput -> {

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("id", templateOutput.getId());
            outputNode.put("name", templateOutput.getName());
            outputNode.put("nameBold", templateOutput.getNameBold());
            outputNode.put("variableName", templateOutput.getVariableName());
            outputNode.put("tooltip", templateOutput.getTooltip());
            outputNode.put("showTooltip", templateOutput.getShowTooltip());
            outputNode.put("resultBold", templateOutput.getResultBold());
            outputNode.put("unit", templateOutput.getUnit());
            outputNode.put("showLabel", templateOutput.getShowLabel());
            outputNode.put("showAnalysisLink", templateOutput.getShowAnalysisLink());
            outputNode.put("link", templateOutput.getLink());
            outputNode.put("column", templateOutput.getColumn());
            outputNode.put("row", templateOutput.getRow());
            outputNode.put("colSpan", templateOutput.getColSpan());
            outputNode.put("rowSpan", templateOutput.getRowSpan());
            outputNode.put("separator", templateOutput.getSeparator());

            templateOutputsArrayNode.add(outputNode);
        });

        ArrayNode formulasArrayNode = JsonNodeFactory.instance.arrayNode();
        this.rcTemplate.getTemplateFormulas().forEach(templateFormula -> {

            ObjectNode formulaNode = JsonNodeFactory.instance.objectNode();
            formulaNode.put("id", templateFormula.getId());
            formulaNode.put("name", templateFormula.getName());
            formulaNode.put("formula", templateFormula.getFormula());
            formulaNode.put("output", templateFormula.getOutput());
            formulaNode.put("timeRestrictionEnabled", templateFormula.getTimeRestrictionEnabled());
            formulaNode.put("fixedTimeFrame", templateFormula.getFixedTimeFrame());
            formulaNode.put("reducingTimeFrame", templateFormula.getReducingTimeFrame());

            ArrayNode inputIdsArrayNode = JsonNodeFactory.instance.arrayNode();
            templateFormula.getInputIds().forEach(id -> inputIdsArrayNode.add(JsonNodeFactory.instance.textNode(id)));

            formulaNode.set("inputIds", inputIdsArrayNode);

            formulasArrayNode.add(formulaNode);
        });

        ObjectNode intervalConfiguration = mapper.valueToTree(this.rcTemplate.getIntervalSelectorConfiguration());

        dataHandlerNode.set("templateInputs", templateInputsArrayNode);
        dataHandlerNode.set("templateOutputs", templateOutputsArrayNode);
        dataHandlerNode.set("templateFormulas", formulasArrayNode);
        dataHandlerNode.set("intervalSelectorConfiguration", intervalConfiguration);

        dataHandlerNode.set("type", JsonNodeFactory.instance.textNode(TYPE));

        return dataHandlerNode;

    }

    public JEVisObject getTemplateObject() {
        return templateObject;
    }

    public void setTemplateObject(JEVisObject templateObject) {
        this.templateObject = templateObject;
        this.setTitle(templateObject.getName());
        try {
            JEVisAttribute templateFileAttribute = templateObject.getAttribute("Template File");
            if (templateFileAttribute.hasSample()) {
                JEVisFile file = templateFileAttribute.getLatestSample().getValueAsFile();
                JsonNode jsonNode = mapper.readTree(file.getBytes());
                jsonToModel(jsonNode);
            }
        } catch (Exception e) {
            logger.error("Could not read template file from object {}", templateObject);
        }
    }
}
