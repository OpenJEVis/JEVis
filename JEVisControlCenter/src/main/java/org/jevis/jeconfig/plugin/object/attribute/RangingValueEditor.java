package org.jevis.jeconfig.plugin.object.attribute;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.RangingValue;
import org.jevis.commons.object.plugin.RangingValues;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.joda.time.DateTime;

public class RangingValueEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(RangingValueEditor.class);
    private final static String TYPE = "RangingValuesHandler";
    private final StackPane dialogContainer;
    private final JEVisAttribute attribute;
    private final SimpleBooleanProperty valueChanged = new SimpleBooleanProperty(this, "valueChanged", false);
    private final HBox box = new HBox(12);
    private final ObjectMapper mapper = new ObjectMapper();
    private final SimpleObjectProperty<RangingValues> rangingValues = new SimpleObjectProperty<>(new RangingValues());
    private final DoubleValidator dv = DoubleValidator.getInstance();
    private JEVisSample lastSample;
    private JEVisSample newSample;
    private boolean initialized = false;

    public RangingValueEditor(StackPane dialogContainer, JEVisAttribute attribute) {
        this.dialogContainer = dialogContainer;
        this.attribute = attribute;
        this.lastSample = attribute.getLatestSample();

        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        initGui();
    }

    private void initGui() {
        JFXButton openConfig = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.openconfig"));
        openConfig.setOnAction(action -> {
            try {
                show();
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });

        box.getChildren().setAll(openConfig);

        initialized = true;
    }

    public void jsonToModel(String jsonString) {
        try {
            JsonNode jsonNode = this.mapper.readTree(jsonString);
            this.mapper.readerForUpdating(getRangingValues()).treeToValue(jsonNode, RangingValues.class);
        } catch (Exception e) {
            logger.error("Could not parse json model", e);
        }
    }

    private JsonNode toJsonNode() {
        RangingValues rangingValuesModel = getRangingValues();

        ObjectNode dataHandlerNode = JsonNodeFactory.instance.objectNode();

        ArrayNode rangingValues = JsonNodeFactory.instance.arrayNode();

        for (RangingValue rangingValue : rangingValuesModel.getRangingValues()) {
            ObjectNode rangingValueNode = JsonNodeFactory.instance.objectNode();

            rangingValueNode.put("from", rangingValue.getFrom());
            rangingValueNode.put("to", rangingValue.getTo());
            rangingValueNode.put("value", rangingValue.getValue());

            rangingValues.add(rangingValueNode);
        }

        dataHandlerNode.put("type", TYPE);

        dataHandlerNode.set("rangingValues", rangingValues);

        return dataHandlerNode;
    }

    private void show() throws JEVisException {
        if (lastSample != null && !lastSample.getValueAsString().isEmpty()) {
            try {
                jsonToModel(lastSample.getValueAsString());
            } catch (Exception e) {
                logger.error("Could not parse Json: {}", lastSample.getValueAsString(), e);
            }
        } else {
            createDefaultConfig();
        }

        TabPane tabPane = new TabPane();

        JFXDialog dialog = new JFXDialog();
        dialog.setDialogContainer(dialogContainer);
        dialog.setTransitionType(JFXDialog.DialogTransition.NONE);
        //dialog.setHeight(450);
        //dialog.setWidth(620);

        for (RangingValue rangingValue : rangingValues.get().getRangingValues()) {
            Tab newTab = new Tab(String.valueOf(rangingValues.get().getRangingValues().indexOf(rangingValue)));
            newTab.setOnClosed(event -> this.rangingValues.get().getRangingValues().remove(rangingValue));
            fillTab(newTab, rangingValue);

            tabPane.getTabs().add(newTab);
        }

        Tab addTab = new Tab();
        addTab.setGraphic(JEConfig.getSVGImage(Icon.PLUS, 12, 12));
        addTab.setClosable(false);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == addTab) {
                RangingValue rangingValue = new RangingValue();
                this.rangingValues.get().getRangingValues().add(rangingValue);
                Tab addedTab = new Tab(String.valueOf(rangingValues.get().getRangingValues().indexOf(rangingValue)));
                addedTab.setOnClosed(event -> this.rangingValues.get().getRangingValues().remove(rangingValue));
                fillTab(addedTab, rangingValue);

                tabPane.getTabs().add(tabPane.getTabs().size() - 1, addedTab);
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
            }
        });

        tabPane.getTabs().add(addTab);

        final JFXButton ok = new JFXButton(I18n.getInstance().getString("newobject.ok"));
        ok.setDefaultButton(true);
        final JFXButton cancel = new JFXButton(I18n.getInstance().getString("newobject.cancel"));
        cancel.setCancelButton(true);

        HBox buttonBar = new HBox(6, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(12));

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, tabPane, separator, buttonBar);
        dialog.setContent(vBox);

        ok.setOnAction(event -> {
            try {
                JsonNode rangingValuesNode = toJsonNode();
                String jsonString = this.mapper.writeValueAsString(rangingValuesNode);
                newSample = attribute.buildSample(new DateTime(), jsonString);
                setValueChanged(true);
                commit();

            } catch (Exception e) {
                logger.error("Could not write ranging values to JEVis System", e);
            }
            dialog.close();
        });

        cancel.setOnAction(event -> dialog.close());

        dialog.show();
    }

    private void fillTab(Tab tab, RangingValue rangingValue) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(5);

        Label fromLabel = new Label(I18n.getInstance().getString("plugin.graph.dialog.export.from"));
        Label toLabel = new Label(I18n.getInstance().getString("plugin.graph.dialog.export.to"));
        Label valueLabel = new Label(I18n.getInstance().getString("plugin.graph.table.value"));

        JFXTextField fromField = new JFXTextField();
        JFXTextField toField = new JFXTextField();
        JFXTextField valueField = new JFXTextField();

        fromField.setText(String.valueOf(rangingValue.getFrom()));
        toField.setText(String.valueOf(rangingValue.getFrom()));
        valueField.setText(String.valueOf(rangingValue.getFrom()));

        fromField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String parsedDefaultValue = dv.validate(newValue, I18n.getInstance().getLocale()).toString();
                rangingValue.setFrom(Double.parseDouble(parsedDefaultValue));
            } catch (Exception e) {
                fromField.setText(oldValue);
            }
        });

        toField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String parsedDefaultValue = dv.validate(newValue, I18n.getInstance().getLocale()).toString();
                rangingValue.setTo(Double.parseDouble(parsedDefaultValue));
            } catch (Exception e) {
                fromField.setText(oldValue);
            }
        });

        valueField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String parsedDefaultValue = dv.validate(newValue, I18n.getInstance().getLocale()).toString();
                rangingValue.setValue(Double.parseDouble(parsedDefaultValue));
            } catch (Exception e) {
                fromField.setText(oldValue);
            }
        });

        ColumnConstraints column1 = new ColumnConstraints(100, 150, 800, Priority.SOMETIMES, HPos.LEFT, false);
        ColumnConstraints column2 = new ColumnConstraints(200, 300, 800, Priority.ALWAYS, HPos.LEFT, true);
        gridPane.getColumnConstraints().addAll(column1, column2);

        int row = 0;
        gridPane.add(fromLabel, 0, row);
        gridPane.add(fromField, 1, row);
        row++;

        gridPane.add(toLabel, 0, row);
        gridPane.add(toField, 1, row);
        row++;

        gridPane.add(valueLabel, 0, row);
        gridPane.add(valueField, 1, row);

        tab.setContent(gridPane);
    }


    private void createDefaultConfig() {
        getRangingValues().reset();
        getRangingValues().getRangingValues().add(new RangingValue());
    }

    @Override
    public boolean hasChanged() {
        return valueChanged.get();
    }

    @Override
    public void commit() throws JEVisException {
        if (hasChanged() && newSample != null) {
            //TODO: check if type is ok, maybe better at input time
            logger.debug("Commit: {}", newSample.getValueAsString());
            newSample.commit();
            lastSample = newSample;
            newSample = null;
            setValueChanged(false);
        }
    }

    @Override
    public Node getEditor() {
        try {
            if (!initialized) {
                initGui();
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }

        return box;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return valueChanged;
    }

    @Override
    public void setReadOnly(boolean canRead) {

    }

    @Override
    public JEVisAttribute getAttribute() {
        return attribute;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void update() {

    }

    public void setValueChanged(boolean valueChanged) {
        this.valueChanged.set(valueChanged);
    }

    public RangingValues getRangingValues() {
        return rangingValues.get();
    }

    public void setRangingValues(RangingValues rangingValues) {
        this.rangingValues.set(rangingValues);
    }

    public SimpleObjectProperty<RangingValues> rangingValuesProperty() {
        return rangingValues;
    }
}
