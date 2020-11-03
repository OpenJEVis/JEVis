package org.jevis.jeconfig.plugin.object.extension;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.LongValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.plugin.charts.GraphPluginView;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.attribute.*;
import org.jevis.jeconfig.tool.ToggleSwitchPlus;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class CleanDataExtension implements ObjectEditorExtension {

    private static final String CLEAN_DATA_CLASS_NAME = "Clean Data";
    private static final String TITLE = I18n.getInstance().getString("plugin.object.cleandata.title");
    private static final Logger logger = LogManager.getLogger(CleanDataExtension.class);
    private final BorderPane view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private JEVisObject _obj;
    private List<JEVisAttribute> changedAttributes = new ArrayList<>();
    private JEVisAttribute conversionToDifferentialAttribute;
    private JEVisAttribute enabledAttribute;
    private JEVisAttribute limitsEnabledAttribute;
    private JEVisAttribute gapFillingEnabledAttribute;
    private JEVisAttribute alarmEnabledAttribute;
    private JEVisAttribute periodAlignmentAttribute;
    private JEVisAttribute periodOffsetAttribute;
    private JEVisAttribute valueIsAQuantityAttribute;
    private JEVisAttribute valueMultiplierAttribute;
    private JEVisAttribute valueOffsetAttribute;
    private JEVisAttribute valueAttribute;
    private JEVisAttribute counterOverflowAttribute;
    private ToggleSwitchPlus conversionToDifferential;
    private ToggleSwitchPlus enabled;
    private ToggleSwitchPlus limitsEnabled;
    private ToggleSwitchPlus gapsEnabled;
    private ToggleSwitchPlus alarmEnabled;
    private ToggleSwitchPlus periodAlignment;
    private JFXTextField periodOffset;
    private ToggleSwitchPlus valueIsAQuantity;
    private JFXTextField valueMultiplier;
    private JFXTextField valueOffset;
    private JFXTextField counterOverflow;
    private TimeStampEditor conversionToDifferentialTimeStampEditor;
    private TimeStampEditor valueMultiplierTimeStampEditor;
    private CleanDataObject cleanDataObject;
    private boolean changedValueMultiplier = false;
    private boolean changedConversionToDifferential = false;
    private JEVisAttribute gapFillingConfigAttribute;
    private JEVisAttribute limitsConfigurationAttribute;
    private NumberFormat nf = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());

    public CleanDataExtension(JEVisObject _obj) {
        this._obj = _obj;
        this.nf.setMinimumFractionDigits(2);
        this.nf.setMaximumFractionDigits(2);
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        boolean isCleanDataObject = false;
        try {
            isCleanDataObject = obj.getJEVisClassName().equals(CLEAN_DATA_CLASS_NAME);
        } catch (JEVisException e) {
            logger.error("Could not get object type" + e.getLocalizedMessage());
        }
        return isCleanDataObject;
    }


    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void showHelp(boolean show) {

    }

    @Override
    public void setVisible() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 0, 20, 20));
        gridPane.setHgap(7);
        gridPane.setVgap(7);

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(gridPane);

        try {
            cleanDataObject = new CleanDataObject(_obj, new ObjectHandler(_obj.getDataSource()));
        } catch (JEVisException e) {
            logger.error("Could not get Clean Data Object.");
        }

        try {
            buildGUI(cleanDataObject);
        } catch (JEVisException e) {
            logger.error("Could not build GUI.");
        }
    }

    private void buildGUI(CleanDataObject cleanDataObject) throws JEVisException {

        cleanDataObject.getAttributes();
        conversionToDifferentialAttribute = cleanDataObject.getConversionToDifferentialAttribute();
        enabledAttribute = cleanDataObject.getEnabledAttribute();
        limitsEnabledAttribute = cleanDataObject.getLimitsEnabledAttribute();
        limitsConfigurationAttribute = cleanDataObject.getLimitsConfigurationAttribute();
        gapFillingEnabledAttribute = cleanDataObject.getGapFillingEnabledAttribute();
        gapFillingConfigAttribute = cleanDataObject.getGapFillingConfigAttribute();
        alarmEnabledAttribute = cleanDataObject.getAlarmEnabledAttribute();
        JEVisAttribute alarmConfigAttribute = cleanDataObject.getAlarmConfigAttribute();
        JEVisAttribute alarmLogAttribute = cleanDataObject.getAlarmLogAttribute();
        periodAlignmentAttribute = cleanDataObject.getPeriodAlignmentAttribute();
        periodOffsetAttribute = cleanDataObject.getPeriodOffsetAttribute();
        valueIsAQuantityAttribute = cleanDataObject.getValueIsAQuantityAttribute();
        valueMultiplierAttribute = cleanDataObject.getValueMultiplierAttribute();
        valueOffsetAttribute = cleanDataObject.getValueOffsetAttribute();
        valueAttribute = cleanDataObject.getValueAttribute();
        counterOverflowAttribute = cleanDataObject.getCounterOverflowAttribute();

        JEVisSample conversionToDifferentialLastSample = conversionToDifferentialAttribute.getLatestSample();
        JEVisSample enabledLastSample = enabledAttribute.getLatestSample();
        JEVisSample limitsEnabledLastSample = limitsEnabledAttribute.getLatestSample();
        JEVisSample limitsConfigurationLastSample = limitsConfigurationAttribute.getLatestSample();
        JEVisSample gapFillingEnabledLastSample = gapFillingEnabledAttribute.getLatestSample();
        JEVisSample gapFillingConfigLastSample = gapFillingConfigAttribute.getLatestSample();
        JEVisSample alarmEnabledLastSample = alarmEnabledAttribute.getLatestSample();
        JEVisSample alarmConfigLastSample = alarmConfigAttribute.getLatestSample();
        JEVisSample alarmLogLastSample = alarmLogAttribute.getLatestSample();
        JEVisSample periodAlignmentLastSample = periodAlignmentAttribute.getLatestSample();
        JEVisSample periodOffsetLastSample = periodOffsetAttribute.getLatestSample();
        JEVisSample valueIsAQuantityLastSample = valueIsAQuantityAttribute.getLatestSample();
        JEVisSample valueMultiplierLastSample = valueMultiplierAttribute.getLatestSample();
        JEVisSample valueOffsetLastSample = valueOffsetAttribute.getLatestSample();
        JEVisSample valueLastSample = valueAttribute.getLatestSample();
        JEVisSample counterOverflowLastSample = counterOverflowAttribute.getLatestSample();


        /**
         *  Conversion to Differential
         */
        Label nameConversionToDifferential = new Label(I18nWS.getInstance().getAttributeName(conversionToDifferentialAttribute));
        Tooltip ttConversionToDifferential = new Tooltip(I18nWS.getInstance().getAttributeDescription(conversionToDifferentialAttribute));
        if (!ttConversionToDifferential.getText().isEmpty()) {
            nameConversionToDifferential.setTooltip(ttConversionToDifferential);
        }
        AttributeAdvSettingDialogButton advSettingDialogButtonConversionToDifferential = new AttributeAdvSettingDialogButton(conversionToDifferentialAttribute);
        conversionToDifferentialTimeStampEditor = new TimeStampEditor(conversionToDifferentialAttribute);
        conversionToDifferential = new ToggleSwitchPlus();
        if (conversionToDifferentialLastSample != null) {
            conversionToDifferential.setSelected(conversionToDifferentialLastSample.getValueAsBoolean());
        }

        /**
         *  Enabled
         */
        Label nameEnabled = new Label(I18nWS.getInstance().getAttributeName(enabledAttribute));
        Tooltip ttEnabled = new Tooltip(I18nWS.getInstance().getAttributeDescription(enabledAttribute));
        if (!ttEnabled.getText().isEmpty()) {
            nameEnabled.setTooltip(ttConversionToDifferential);
        }
        AttributeAdvSettingDialogButton advSettingDialogButtonEnabled = new AttributeAdvSettingDialogButton(enabledAttribute);
        enabled = new ToggleSwitchPlus();
        if (enabledLastSample != null) {
            enabled.setSelected(enabledLastSample.getValueAsBoolean());
        }

        /**
         *  Limits
         */
        Label nameLimitsEnabled = new Label(I18nWS.getInstance().getAttributeName(limitsEnabledAttribute));
        Tooltip ttLimitsEnabled = new Tooltip(I18nWS.getInstance().getAttributeDescription(limitsEnabledAttribute));
        if (!ttLimitsEnabled.getText().isEmpty()) {
            nameLimitsEnabled.setTooltip(ttLimitsEnabled);
        }
        limitsEnabled = new ToggleSwitchPlus();
        if (limitsEnabledLastSample != null) {
            limitsEnabled.setSelected(limitsEnabledLastSample.getValueAsBoolean());
        }

        Label nameLimitsConfiguration = new Label(I18nWS.getInstance().getAttributeName(limitsConfigurationAttribute));
        Tooltip ttLimitsConfiguration = new Tooltip(I18nWS.getInstance().getAttributeDescription(limitsConfigurationAttribute));
        if (!ttLimitsConfiguration.getText().isEmpty()) {
            nameLimitsConfiguration.setTooltip(ttLimitsConfiguration);
        }
        LimitEditor limitsConfiguration = new LimitEditor(limitsConfigurationAttribute);

        /**
         *  Gaps
         */
        Label nameGapsEnabled = new Label(I18nWS.getInstance().getAttributeName(gapFillingEnabledAttribute));
        Tooltip ttGapsEnabled = new Tooltip(I18nWS.getInstance().getAttributeDescription(gapFillingEnabledAttribute));
        if (!ttGapsEnabled.getText().isEmpty()) {
            nameGapsEnabled.setTooltip(ttGapsEnabled);
        }
        gapsEnabled = new ToggleSwitchPlus();
        if (gapFillingEnabledLastSample != null) {
            gapsEnabled.setSelected(gapFillingEnabledLastSample.getValueAsBoolean());
        }

        Label nameGapsConfiguration = new Label(I18nWS.getInstance().getAttributeName(gapFillingConfigAttribute));
        Tooltip ttGapsConfiguration = new Tooltip(I18nWS.getInstance().getAttributeDescription(gapFillingConfigAttribute));
        if (!ttGapsConfiguration.getText().isEmpty()) {
            nameGapsConfiguration.setTooltip(ttGapsConfiguration);
        }
        GapFillingEditor gapsConfiguration = new GapFillingEditor(gapFillingConfigAttribute);

        /**
         *  Alarm
         */
        Label nameAlarmEnabled = new Label(I18nWS.getInstance().getAttributeName(alarmEnabledAttribute));
        Tooltip ttAlarmEnabled = new Tooltip(I18nWS.getInstance().getAttributeDescription(alarmEnabledAttribute));
        if (!ttAlarmEnabled.getText().isEmpty()) {
            nameAlarmEnabled.setTooltip(ttAlarmEnabled);
        }
        alarmEnabled = new ToggleSwitchPlus();
        if (alarmEnabledLastSample != null) {
            alarmEnabled.setSelected(alarmEnabledLastSample.getValueAsBoolean());
        }

        Label nameAlarmConfiguration = new Label(I18nWS.getInstance().getAttributeName(alarmConfigAttribute));
        Tooltip ttAlarmConfiguration = new Tooltip(I18nWS.getInstance().getAttributeDescription(alarmConfigAttribute));
        if (!ttAlarmConfiguration.getText().isEmpty()) {
            nameAlarmConfiguration.setTooltip(ttAlarmConfiguration);
        }
        AlarmEditor alarmConfiguration = new AlarmEditor(alarmConfigAttribute);

        Label nameAlarmLog = new Label(I18nWS.getInstance().getAttributeName(alarmLogAttribute));
        Tooltip ttAlarmLog = new Tooltip(I18nWS.getInstance().getAttributeDescription(alarmLogAttribute));
        if (!ttAlarmLog.getText().isEmpty()) {
            nameAlarmLog.setTooltip(ttAlarmLog);
        }
        AttributeAdvSettingDialogButton advSettingDialogButtonAlarmLog = new AttributeAdvSettingDialogButton(alarmLogAttribute);
        TimeStampEditor alarmLogTimeStamp = new TimeStampEditor(alarmLogAttribute);
        alarmLogTimeStamp.getEditor().setDisable(true);
        JFXTextField alarmLog = new JFXTextField();
        alarmLog.setDisable(true);
        if (alarmLogLastSample != null) {
            alarmLog.setText(alarmLogLastSample.getValueAsDouble().toString());
        }

        /**
         *  Period Alignment
         */
        Label namePeriodAlignment = new Label(I18nWS.getInstance().getAttributeName(periodAlignmentAttribute));
        Tooltip ttPeriodAlignment = new Tooltip(I18nWS.getInstance().getAttributeDescription(periodAlignmentAttribute));
        if (!ttPeriodAlignment.getText().isEmpty()) {
            namePeriodAlignment.setTooltip(ttPeriodAlignment);
        }
        AttributeAdvSettingDialogButton advSettingDialogButtonPeriodAlignment = new AttributeAdvSettingDialogButton(periodAlignmentAttribute);
        periodAlignment = new ToggleSwitchPlus();
        if (periodAlignmentLastSample != null) {
            periodAlignment.setSelected(periodAlignmentLastSample.getValueAsBoolean());
        }

        /**
         *  Period Offset
         */
        Label namePeriodOffset = new Label(I18nWS.getInstance().getAttributeName(periodOffsetAttribute));
        Tooltip ttPeriodOffset = new Tooltip(I18nWS.getInstance().getAttributeDescription(periodOffsetAttribute));
        if (!ttPeriodOffset.getText().isEmpty()) {
            namePeriodOffset.setTooltip(ttPeriodOffset);
        }
        AttributeAdvSettingDialogButton advSettingDialogButtonPeriodOffset = new AttributeAdvSettingDialogButton(periodOffsetAttribute);
        periodOffset = new JFXTextField();
        if (periodOffsetLastSample != null) {
            periodOffset.setText(periodOffsetLastSample.getValueAsLong().toString());
        }

        /**
         *  Value is a Quantity
         */
        Label nameValueIsAQuantity = new Label(I18nWS.getInstance().getAttributeName(valueIsAQuantityAttribute));
        Tooltip ttValueIsAQuantity = new Tooltip(I18nWS.getInstance().getAttributeDescription(valueIsAQuantityAttribute));
        if (!ttValueIsAQuantity.getText().isEmpty()) {
            nameValueIsAQuantity.setTooltip(ttValueIsAQuantity);
        }
        AttributeAdvSettingDialogButton advSettingDialogButtonValueIsAQuantity = new AttributeAdvSettingDialogButton(valueIsAQuantityAttribute);
        valueIsAQuantity = new ToggleSwitchPlus();
        if (valueIsAQuantityLastSample != null) {
            valueIsAQuantity.setSelected(valueIsAQuantityLastSample.getValueAsBoolean());
        }

        /**
         *  Value Multiplier
         */
        Label nameValueMultiplier = new Label(I18nWS.getInstance().getAttributeName(valueMultiplierAttribute));
        Tooltip ttValueMultiplier = new Tooltip(I18nWS.getInstance().getAttributeDescription(valueMultiplierAttribute));
        if (!ttValueMultiplier.getText().isEmpty()) {
            nameValueMultiplier.setTooltip(ttValueMultiplier);
        }
        AttributeAdvSettingDialogButton advSettingDialogButtonValueMultiplier = new AttributeAdvSettingDialogButton(valueMultiplierAttribute);
        valueMultiplierTimeStampEditor = new TimeStampEditor(valueMultiplierAttribute);
        valueMultiplier = new JFXTextField();

        if (valueMultiplierLastSample != null) {
            valueMultiplier.setText(nf.format(valueMultiplierLastSample.getValueAsDouble()));
        }

        /**
         *  Value
         */
        Label nameValue = new Label(I18nWS.getInstance().getAttributeName(valueAttribute));
        Tooltip ttValue = new Tooltip(I18nWS.getInstance().getAttributeDescription(valueAttribute));
        if (!ttValue.getText().isEmpty()) {
            nameValue.setTooltip(ttValue);
        }
        AttributeAdvSettingDialogButton advSettingDialogButtonValue = new AttributeAdvSettingDialogButton(valueAttribute);
        TimeStampEditor valueTimeStamp = new TimeStampEditor(valueAttribute);
        valueTimeStamp.getEditor().setDisable(true);
        JFXTextField value = new JFXTextField();
        JFXTextField unitValue = new JFXTextField();
        JFXButton valueInChartsButton = new JFXButton("", JEConfig.getImage("1415314386_Graph.png", 20, 20));
        try {
            DateHelper dateHelper = new DateHelper(DateHelper.TransformType.TODAY);
            WorkDays workDays = new WorkDays(valueAttribute.getObject());
            dateHelper.setStartTime(workDays.getWorkdayStart());
            dateHelper.setEndTime(workDays.getWorkdayEnd());

            AnalysisRequest analysisRequest = new AnalysisRequest(valueAttribute.getObject(),
                    AggregationPeriod.NONE,
                    ManipulationMode.NONE,
                    new AnalysisTimeFrame(TimeFrame.TODAY),
                    dateHelper.getStartDate(), dateHelper.getEndDate());
            analysisRequest.setAttribute(valueAttribute);
            valueInChartsButton.setOnAction(event -> JEConfig.openObjectInPlugin(GraphPluginView.PLUGIN_NAME, analysisRequest));
        } catch (Exception e) {
            logger.error("Could not create value in charts button: " + e);
        }
        value.setDisable(true);
        if (valueLastSample != null) {
            value.setText(nf.format(valueLastSample.getValueAsDouble()));
        }
        if (valueAttribute.getDisplayUnit() != null && !valueAttribute.getInputUnit().getLabel().isEmpty()) {
            unitValue.setText(UnitManager.getInstance().format(valueAttribute.getDisplayUnit().getLabel()));
        } else {
            unitValue.setText(UnitManager.getInstance().format(valueAttribute.getInputUnit().getLabel()));
        }
        unitValue.setDisable(true);

        /**
         *  Value Offset
         */
        Label nameValueOffset = new Label(I18nWS.getInstance().getAttributeName(valueOffsetAttribute));
        Tooltip ttValueOffset = new Tooltip(I18nWS.getInstance().getAttributeDescription(valueOffsetAttribute));
        if (!ttValueOffset.getText().isEmpty()) {
            nameValueOffset.setTooltip(ttValueOffset);
        }
        AttributeAdvSettingDialogButton advSettingDialogButtonValueOffset = new AttributeAdvSettingDialogButton(valueOffsetAttribute);
        valueOffset = new JFXTextField();
        if (valueOffsetLastSample != null) {
            valueOffset.setText(nf.format(valueOffsetLastSample.getValueAsDouble()));
        }

        /**
         *  Counter Overflow
         */
        Label nameCounterOverflow = new Label(I18nWS.getInstance().getAttributeName(counterOverflowAttribute));
        Tooltip ttCounterOverflow = new Tooltip(I18nWS.getInstance().getAttributeDescription(counterOverflowAttribute));
        if (!ttCounterOverflow.getText().isEmpty()) {
            nameCounterOverflow.setTooltip(ttCounterOverflow);
        }
        AttributeAdvSettingDialogButton advSettingDialogButtonCounterOverflow = new AttributeAdvSettingDialogButton(counterOverflowAttribute);
        counterOverflow = new JFXTextField();
        if (counterOverflowLastSample != null) {
            counterOverflow.setText(nf.format(counterOverflowLastSample.getValueAsDouble()));
        }

        setupListener(conversionToDifferentialTimeStampEditor, conversionToDifferential,
                enabled,
                limitsEnabled,
                limitsConfiguration,
                gapsEnabled,
                gapsConfiguration,
                alarmEnabled,
                alarmConfiguration,
                alarmLog,
                periodAlignment,
                periodOffset,
                valueIsAQuantity,
                valueMultiplierTimeStampEditor,
                valueMultiplier,
                valueOffset,
                valueTimeStamp,
                value,
                counterOverflow);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: transparent");
        scrollPane.setMaxSize(10000, 10000);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(7);
        gridPane.setVgap(7);
        gridPane.setPadding(new Insets(4, 4, 4, 4));

        enabled.setAlignment(Pos.CENTER_LEFT);
        value.setAlignment(Pos.CENTER_RIGHT);
        unitValue.setAlignment(Pos.CENTER_LEFT);
        unitValue.setPrefWidth(40);
        unitValue.setEditable(false);
        valueMultiplier.setAlignment(Pos.CENTER_RIGHT);
        valueOffset.setAlignment(Pos.CENTER_RIGHT);
        valueIsAQuantity.setAlignment(Pos.CENTER_LEFT);
        counterOverflow.setAlignment(Pos.CENTER_RIGHT);
        conversionToDifferential.setAlignment(Pos.CENTER_LEFT);
        periodAlignment.setAlignment(Pos.CENTER_LEFT);
        periodOffset.setAlignment(Pos.CENTER_RIGHT);
        alarmLog.setAlignment(Pos.CENTER_RIGHT);

        int row = 0;
        int colSpan = 1;

        gridPane.add(nameEnabled, 0, row);
        gridPane.add(advSettingDialogButtonEnabled, 1, row);
        gridPane.add(enabled, 2, row, colSpan, 1);
        row++;
        gridPane.add(nameValue, 0, row);
        gridPane.add(advSettingDialogButtonValue, 1, row);
        gridPane.add(value, 2, row);
        gridPane.add(unitValue, 3, row);
        gridPane.add(valueInChartsButton, 4, row);
        gridPane.add(valueTimeStamp.getEditor(), 5, row);
        row++;
        gridPane.add(nameValueMultiplier, 0, row);
        gridPane.add(advSettingDialogButtonValueMultiplier, 1, row);
        gridPane.add(valueMultiplier, 2, row);
        gridPane.add(valueMultiplierTimeStampEditor.getEditor(), 5, row);

        row++;
        gridPane.add(nameValueOffset, 0, row);
        gridPane.add(advSettingDialogButtonValueOffset, 1, row);
        gridPane.add(valueOffset, 2, row, colSpan, 1);
        row++;

        gridPane.add(nameValueIsAQuantity, 0, row);
        gridPane.add(advSettingDialogButtonValueIsAQuantity, 1, row);
        gridPane.add(valueIsAQuantity, 2, row, colSpan, 1);
        row++;
        gridPane.add(nameCounterOverflow, 0, row);
        gridPane.add(advSettingDialogButtonCounterOverflow, 1, row);
        gridPane.add(counterOverflow, 2, row, colSpan, 1);
        row++;

        gridPane.add(nameConversionToDifferential, 0, row);
        gridPane.add(advSettingDialogButtonConversionToDifferential, 1, row);
        gridPane.add(conversionToDifferential, 2, row);
        gridPane.add(conversionToDifferentialTimeStampEditor.getEditor(), 5, row);

        row++;
        gridPane.add(namePeriodAlignment, 0, row);
        gridPane.add(advSettingDialogButtonPeriodAlignment, 1, row);
        gridPane.add(periodAlignment, 2, row, colSpan, 1);
        row++;
        gridPane.add(namePeriodOffset, 0, row);
        gridPane.add(advSettingDialogButtonPeriodOffset, 1, row);
        gridPane.add(periodOffset, 2, row, colSpan, 1);
        row++;

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(10, 0, 10, 0));
        gridPane.add(separator, 0, row, 5, 1);
        row++;

        gridPane.add(nameGapsConfiguration, 0, row);
        gridPane.add(gapsEnabled, 2, row, colSpan, 1);
        gridPane.add(gapsConfiguration.getEditor(), 3, row, 3, 1);
        row++;
        gridPane.add(nameLimitsConfiguration, 0, row);
        gridPane.add(limitsEnabled, 2, row, colSpan, 1);
        gridPane.add(limitsConfiguration.getEditor(), 3, row, 3, 1);
        row++;
        gridPane.add(nameAlarmConfiguration, 0, row);
        gridPane.add(alarmEnabled, 2, row, colSpan, 1);
        gridPane.add(alarmConfiguration.getEditor(), 3, row, 3, 1);
        row++;
        gridPane.add(nameAlarmLog, 0, row);
        gridPane.add(advSettingDialogButtonAlarmLog, 1, row);
        gridPane.add(alarmLog, 2, row, colSpan, 1);
        gridPane.add(alarmLogTimeStamp.getEditor(), 5, row);
        row++;


        scrollPane.setContent(gridPane);
        view.setCenter(scrollPane);
    }

    private void setupListener(TimeStampEditor conversionToDifferentialTimeStampEditor, ToggleSwitchPlus conversionToDifferential,
                               ToggleSwitchPlus enabled, ToggleSwitchPlus limitsEnabled, LimitEditor limitsConfiguration,
                               ToggleSwitchPlus gapsEnabled, GapFillingEditor gapsConfiguration, ToggleSwitchPlus alarmEnabled,
                               AlarmEditor alarmConfiguration, JFXTextField alarmLog, ToggleSwitchPlus periodAlignment,
                               JFXTextField periodOffset, ToggleSwitchPlus valueIsAQuantity, TimeStampEditor valueMultiplierTimeStampEditor,
                               JFXTextField valueMultiplier, JFXTextField valueOffset, TimeStampEditor valueTimeStampEditor,
                               JFXTextField value, JFXTextField counterOverflow) {

        conversionToDifferentialTimeStampEditor.getValueChangedProperty().addListener((observable, oldValue, newValue) -> {
            _changed.set(true);
            if (!changedAttributes.contains(conversionToDifferentialAttribute)) {
                changedAttributes.add(conversionToDifferentialAttribute);
            }
        });

        conversionToDifferential.selectedProperty().addListener((observable, oldValue, newValue) -> {
            _changed.set(true);
            if (!changedAttributes.contains(conversionToDifferentialAttribute)) {
                changedAttributes.add(conversionToDifferentialAttribute);
                changedConversionToDifferential = true;
            }
        });

        enabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
            _changed.set(true);
            if (!changedAttributes.contains(enabledAttribute)) {
                changedAttributes.add(enabledAttribute);
            }
        });

        limitsEnabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
            _changed.set(true);
            if (!changedAttributes.contains(limitsEnabledAttribute)) {
                changedAttributes.add(limitsEnabledAttribute);
            }
        });

        gapsEnabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
            _changed.set(true);
            if (!changedAttributes.contains(gapFillingEnabledAttribute)) {
                changedAttributes.add(gapFillingEnabledAttribute);
            }
        });

        alarmEnabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
            _changed.set(true);
            if (!changedAttributes.contains(alarmEnabledAttribute)) {
                changedAttributes.add(alarmEnabledAttribute);
            }
        });

        periodAlignment.selectedProperty().addListener((observable, oldValue, newValue) -> {
            _changed.set(true);
            if (!changedAttributes.contains(periodAlignmentAttribute)) {
                changedAttributes.add(periodAlignmentAttribute);
            }
        });

        periodOffset.textProperty().addListener((observable, oldValue, newValue) -> {
            LongValidator validator = LongValidator.getInstance();
            try {
                long parsedValue = validator.validate(newValue, I18n.getInstance().getLocale());
                _changed.set(true);
                if (!changedAttributes.contains(periodOffsetAttribute)) {
                    changedAttributes.add(periodOffsetAttribute);
                }
            } catch (Exception e) {
                periodOffset.setText(oldValue);
            }
        });

        valueIsAQuantity.selectedProperty().addListener((observable, oldValue, newValue) -> {
            _changed.set(true);
            if (!changedAttributes.contains(valueIsAQuantityAttribute)) {
                changedAttributes.add(valueIsAQuantityAttribute);
            }
        });

        valueMultiplierTimeStampEditor.getValueChangedProperty().addListener((observable, oldValue, newValue) -> {
            _changed.set(true);
            if (!changedAttributes.contains(valueMultiplierAttribute)) {
                changedAttributes.add(valueMultiplierAttribute);
            }
        });

        valueMultiplier.textProperty().addListener((observable, oldValue, newValue) -> {
            DoubleValidator validator = DoubleValidator.getInstance();
            try {
                double parsedValue = validator.validate(newValue, I18n.getInstance().getLocale());
                _changed.set(true);
                if (!changedAttributes.contains(valueMultiplierAttribute)) {
                    changedAttributes.add(valueMultiplierAttribute);
                    changedValueMultiplier = true;
                }
            } catch (Exception e) {
                valueMultiplier.setText(oldValue);
            }
        });

//        value.focusedProperty().addListener((observable, oldValue, newValue) -> {
//            _changed.set(true);
//            if (!changedAttributes.contains(valueAttribute)) {
//                changedAttributes.add(valueAttribute);
//            }
//        });

        valueOffset.textProperty().addListener((observable, oldValue, newValue) -> {
            DoubleValidator validator = DoubleValidator.getInstance();
            try {
                double parsedValue = validator.validate(newValue, I18n.getInstance().getLocale());
                _changed.set(true);
                if (!changedAttributes.contains(valueOffsetAttribute)) {
                    changedAttributes.add(valueOffsetAttribute);
                }
            } catch (Exception e) {
                valueOffset.setText(oldValue);
            }
        });

        counterOverflow.textProperty().addListener((observable, oldValue, newValue) -> {
            DoubleValidator validator = DoubleValidator.getInstance();
            try {
                double parsedValue = validator.validate(newValue, I18n.getInstance().getLocale());
                _changed.set(true);
                if (!changedAttributes.contains(counterOverflowAttribute)) {
                    changedAttributes.add(counterOverflowAttribute);
                }
            } catch (Exception e) {
                counterOverflow.setText(oldValue);
            }
        });
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public boolean needSave() {
        return _changed.getValue();
    }

    @Override
    public void dismissChanges() {
        _changed.setValue(false);
    }

    @Override
    public boolean save() {
        try {
            if (needSave()) {

                List<JEVisAttribute> savedAttributes = new ArrayList<>();
                for (JEVisAttribute attribute : changedAttributes) {
                    if (attribute.equals(conversionToDifferentialAttribute)) {
                        DateTime oldDateTime = conversionToDifferentialTimeStampEditor.getOriginalDateTime();
                        DateTime newDateTime = conversionToDifferentialTimeStampEditor.getDateTime();
                        List<JEVisSample> oldSamples = conversionToDifferentialAttribute.getSamples(oldDateTime, oldDateTime);
                        if (oldSamples.isEmpty()) {
                            JEVisSample newSample = conversionToDifferentialAttribute.buildSample(newDateTime, conversionToDifferential.isSelected());
                            newSample.commit();
                        } else {
                            if (!changedConversionToDifferential) {
                                conversionToDifferentialAttribute.deleteSamplesBetween(oldDateTime, oldDateTime);
                            }
                            JEVisSample newSample = conversionToDifferentialAttribute.buildSample(newDateTime, conversionToDifferential.isSelected());
                            newSample.commit();
                        }

                        savedAttributes.add(conversionToDifferentialAttribute);
                    } else if (attribute.equals(enabledAttribute)) {
                        JEVisSample newSample = enabledAttribute.buildSample(DateTime.now(), enabled.isSelected());
                        newSample.commit();
                        savedAttributes.add(enabledAttribute);
                    } else if (attribute.equals(limitsEnabledAttribute)) {
                        JEVisSample newSample = limitsEnabledAttribute.buildSample(DateTime.now(), limitsEnabled.isSelected());
                        newSample.commit();
                        savedAttributes.add(limitsEnabledAttribute);
                        if (limitsEnabled.isSelected() && !limitsConfigurationAttribute.hasSample()) {
                            List<JsonLimitsConfig> defaultConfig = LimitEditor.createDefaultConfig();
                            JEVisSample newConfigSample = limitsConfigurationAttribute.buildSample(DateTime.now(), defaultConfig.toString());
                            newConfigSample.commit();
                        }
                    } else if (attribute.equals(gapFillingEnabledAttribute)) {
                        JEVisSample newSample = gapFillingEnabledAttribute.buildSample(DateTime.now(), gapsEnabled.isSelected());
                        newSample.commit();
                        savedAttributes.add(gapFillingEnabledAttribute);
                        if (gapsEnabled.isSelected() && !gapFillingConfigAttribute.hasSample()) {
                            List<JsonGapFillingConfig> defaultConfig = GapFillingEditor.createDefaultConfig();
                            JEVisSample newConfigSample = gapFillingConfigAttribute.buildSample(DateTime.now(), defaultConfig.toString());
                            newConfigSample.commit();
                        }
                    } else if (attribute.equals(alarmEnabledAttribute)) {
                        JEVisSample newSample = alarmEnabledAttribute.buildSample(DateTime.now(), alarmEnabled.isSelected());
                        newSample.commit();
                        savedAttributes.add(alarmEnabledAttribute);
                    } else if (attribute.equals(periodAlignmentAttribute)) {
                        JEVisSample newSample = periodAlignmentAttribute.buildSample(DateTime.now(), periodAlignment.isSelected());
                        newSample.commit();
                        savedAttributes.add(periodAlignmentAttribute);
                    } else if (attribute.equals(periodOffsetAttribute)) {
                        LongValidator validator = LongValidator.getInstance();
                        Long value = validator.validate(periodOffset.getText(), I18n.getInstance().getLocale());
                        JEVisSample newSample = periodOffsetAttribute.buildSample(DateTime.now(), value);
                        newSample.commit();
                        savedAttributes.add(periodOffsetAttribute);
                    } else if (attribute.equals(valueIsAQuantityAttribute)) {
                        JEVisSample newSample = valueIsAQuantityAttribute.buildSample(DateTime.now(), valueIsAQuantity.isSelected());
                        newSample.commit();
                        savedAttributes.add(valueIsAQuantityAttribute);
                    } else if (attribute.equals(valueMultiplierAttribute)) {
                        DateTime oldDateTime = valueMultiplierTimeStampEditor.getOriginalDateTime();
                        DateTime newDateTime = valueMultiplierTimeStampEditor.getDateTime();
                        List<JEVisSample> oldSamples = valueMultiplierAttribute.getSamples(oldDateTime, oldDateTime);
                        DoubleValidator validator = DoubleValidator.getInstance();
                        Double value = validator.validate(valueMultiplier.getText(), I18n.getInstance().getLocale());
                        if (oldSamples.isEmpty()) {
                            JEVisSample newSample = valueMultiplierAttribute.buildSample(newDateTime, value);
                            newSample.commit();
                        } else {
                            if (!changedValueMultiplier) {
                                valueMultiplierAttribute.deleteSamplesBetween(oldDateTime, oldDateTime);
                            }
                            JEVisSample newSample = valueMultiplierAttribute.buildSample(newDateTime, value);
                            newSample.commit();
                        }
                        savedAttributes.add(valueMultiplierAttribute);
                    } else if (attribute.equals(valueOffsetAttribute)) {
                        DoubleValidator validator = DoubleValidator.getInstance();
                        Double value = validator.validate(valueOffset.getText(), I18n.getInstance().getLocale());
                        JEVisSample newSample = valueOffsetAttribute.buildSample(DateTime.now(), value);
                        newSample.commit();
                        savedAttributes.add(valueOffsetAttribute);
                    } else if (attribute.equals(counterOverflowAttribute)) {
                        DoubleValidator validator = DoubleValidator.getInstance();
                        Double value = validator.validate(counterOverflow.getText(), I18n.getInstance().getLocale());
                        JEVisSample newSample = counterOverflowAttribute.buildSample(DateTime.now(), value);
                        newSample.commit();
                        savedAttributes.add(counterOverflowAttribute);
                    }
                }


                changedAttributes.removeAll(savedAttributes);
                _changed.setValue(false);
                Platform.runLater(() -> {
                    try {
                        cleanDataObject.reloadAttributes();
                        buildGUI(cleanDataObject);
                    } catch (JEVisException e) {
                        logger.error("Could not reload.");
                    }
                });
            }
            return true;
        } catch (Exception ex) {
            logger.fatal("Could not save attributes", ex);
        }
        return false;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }
}
