package org.jevis.jeconfig.plugin.accounting;

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
import org.jevis.jeconfig.plugin.dtrc.InputVariableType;

public class AccountingTemplateHandler {
    public final static String TYPE = "SimpleDataHandler";
    private static final Logger logger = LogManager.getLogger(AccountingTemplateHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private SelectionTemplate selectionTemplate;
    private JEVisObject templateObject;
    private String Title;

    public AccountingTemplateHandler() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public SelectionTemplate getSelectionTemplate() {
        if (selectionTemplate == null && templateObject != null) {
            logger.warn("Could not read json file, selection template is null");
            selectionTemplate = new SelectionTemplate();
        }

        return selectionTemplate;
    }

    public void setSelectionTemplate(SelectionTemplate selectionTemplate) {
        this.selectionTemplate = selectionTemplate;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void jsonToModel(JsonNode jsonNode) {
        try {
            this.selectionTemplate = this.mapper.treeToValue(jsonNode, SelectionTemplate.class);
        } catch (JsonProcessingException e) {
            logger.error("Could not parse json model", e);
        }
    }

    public JsonNode toJsonNode() {
        ObjectNode dataHandlerNode = JsonNodeFactory.instance.objectNode();

        ArrayNode templateInputsArrayNode = JsonNodeFactory.instance.arrayNode();
        this.selectionTemplate.getSelectedInputs().forEach(templateInput -> {
            if (templateInput.getVariableType() != null && !templateInput.getVariableType().equals(InputVariableType.FORMULA.toString())) {
                ObjectNode inputNode = JsonNodeFactory.instance.objectNode();
                inputNode.put("objectClass", templateInput.getObjectClass());
                inputNode.put("id", templateInput.getId());
                inputNode.put("attributeName", templateInput.getAttributeName());
                inputNode.put("variableName", templateInput.getVariableName());
                inputNode.put("variableType", templateInput.getVariableType());
                inputNode.put("filter", templateInput.getFilter());
                inputNode.put("group", templateInput.getGroup());
                inputNode.put("objectID", templateInput.getObjectID());

                templateInputsArrayNode.add(inputNode);
            }
        });

        dataHandlerNode.set("selectedInputs", templateInputsArrayNode);
        dataHandlerNode.set("templateSelection", JsonNodeFactory.instance.numberNode(selectionTemplate.getTemplateSelection()));

        dataHandlerNode.set("type", JsonNodeFactory.instance.textNode(TYPE));

        dataHandlerNode.set("contractNumber", JsonNodeFactory.instance.textNode(selectionTemplate.getContractNumber()));
        dataHandlerNode.set("contractType", JsonNodeFactory.instance.textNode(selectionTemplate.getContractType()));
        dataHandlerNode.set("marketLocationNumber", JsonNodeFactory.instance.textNode(selectionTemplate.getMarketLocationNumber()));
        dataHandlerNode.set("contractDate", JsonNodeFactory.instance.textNode(selectionTemplate.getContractDate()));
        dataHandlerNode.set("firstRate", JsonNodeFactory.instance.textNode(selectionTemplate.getFirstRate()));
        dataHandlerNode.set("periodOfNotice", JsonNodeFactory.instance.textNode(selectionTemplate.getPeriodOfNotice()));
        dataHandlerNode.set("contractStart", JsonNodeFactory.instance.textNode(selectionTemplate.getContractStart()));
        dataHandlerNode.set("contractEnd", JsonNodeFactory.instance.textNode(selectionTemplate.getContractEnd()));

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
