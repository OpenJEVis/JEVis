package org.jevis.jeconfig.application.Chart.data;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.ws.json.JsonUnit;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.joda.time.DateTime;

import javax.measure.MetricPrefix;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

public class ChartData {
    private static final Logger logger = LogManager.getLogger(ChartData.class);
    private final SimpleLongProperty id = new SimpleLongProperty(this, "id", -1);
    private final SimpleStringProperty attributeString = new SimpleStringProperty(this, "attribute", "");
    private final SimpleStringProperty unitPrefix = new SimpleStringProperty(this, "unitPrefix", new JEVisUnitImp(Unit.ONE).getPrefix().toString());
    private final SimpleStringProperty unitLabel = new SimpleStringProperty(this, "unitLabel", new JEVisUnitImp(Unit.ONE).getLabel());
    private final SimpleStringProperty unitFormula = new SimpleStringProperty(this, "unitFormula", new JEVisUnitImp(Unit.ONE).getFormula());
    private final SimpleObjectProperty<JEVisUnit> unit = new SimpleObjectProperty<>(this, "unit", new JEVisUnitImp(Unit.ONE));
    private final SimpleObjectProperty<JEVisObject> objectName = new SimpleObjectProperty<>(this, "objectName");
    private final SimpleStringProperty name = new SimpleStringProperty(this, "name", "");
    private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<Color>(this, "color", Color.LIGHTBLUE);
    private final SimpleIntegerProperty axis = new SimpleIntegerProperty(this, "axis", 0);
    private final SimpleBooleanProperty calculation = new SimpleBooleanProperty(this, "calculation", false);
    private final SimpleLongProperty calculationId = new SimpleLongProperty(this, "calculationId", -1);
    private final SimpleObjectProperty<BubbleType> bubbleType = new SimpleObjectProperty<>(this, "bubbleType", BubbleType.NONE);
    private final SimpleObjectProperty<ChartType> chartType = new SimpleObjectProperty<>(this, "chartType", ChartType.AREA);
    private final SimpleObjectProperty<AggregationPeriod> aggregationPeriod = new SimpleObjectProperty<>(this, "aggregationPeriod", AggregationPeriod.NONE);
    private final SimpleObjectProperty<ManipulationMode> manipulationMode = new SimpleObjectProperty<>(this, "manipulationMode", ManipulationMode.NONE);
    private final SimpleBooleanProperty intervalEnabled = new SimpleBooleanProperty(this, "intervalEnabled", false);
    private final SimpleStringProperty intervalStart = new SimpleStringProperty(this, "intervalStart", DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toString());
    private final SimpleStringProperty intervalEnd = new SimpleStringProperty(this, "intervalEnd", DateTime.now().toString());
    private final SimpleStringProperty css = new SimpleStringProperty(this, "css", "");

    public ChartData() {
    }

    public long getId() {
        return id.get();
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public SimpleLongProperty idProperty() {
        return id;
    }

    public String getAttributeString() {
        return attributeString.get();
    }

    public void setAttributeString(String attributeString) {
        this.attributeString.set(attributeString);
    }

    public SimpleStringProperty attributeStringProperty() {
        return attributeString;
    }

    public String getUnitPrefix() {
        return unitPrefix.get();
    }

    public void setUnitPrefix(String unitPrefix) {
        this.unitPrefix.set(unitPrefix);
    }

    public SimpleStringProperty unitPrefixProperty() {
        return unitPrefix;
    }

    public String getUnitLabel() {
        return unitLabel.get();
    }

    public void setUnitLabel(String unitLabel) {
        this.unitLabel.set(unitLabel);
    }

    public SimpleStringProperty unitLabelProperty() {
        return unitLabel;
    }

    public String getUnitFormula() {
        return unitFormula.get();
    }

    public void setUnitFormula(String unitFormula) {
        this.unitFormula.set(unitFormula);
    }

    public SimpleStringProperty unitFormulaProperty() {
        return unitFormula;
    }

    public JEVisUnit getUnit() {
        JEVisUnitImp jeVisUnitImp = new JEVisUnitImp(getUnitFormula(), getUnitLabel(), getUnitPrefix());
        unit.set(jeVisUnitImp);
        logger.debug("ChartData {} Unit from prefix {} and formula {} and label {} results in {}", getName(), getUnitPrefix(), getUnitFormula(), getUnitLabel(), unit.get());
        return jeVisUnitImp;
    }

    public void setUnit(JEVisUnit unit) {
        unitPrefix.set(unit.getPrefix().toString());
        unitFormula.set(unit.getFormula());
        unitLabel.set(unit.getLabel());
    }

    public void setUnit(String unitString) {
        UnitFormat unitFormat = UnitFormat.getInstance();
        try {
            Unit u = (Unit) unitFormat.parseObject(unitString);
            JEVisUnitImp jeVisUnitImp = new JEVisUnitImp(u);
            this.unit.set(jeVisUnitImp);
            this.unitPrefix.set(jeVisUnitImp.getPrefix().toString());
            this.unitFormula.set(jeVisUnitImp.getFormula());
            this.unitLabel.set(jeVisUnitImp.getLabel());
        } catch (Exception e) {
            String prefixFromUnit = getPrefixFromUnit(unitString);
            JsonUnit jsonUnit = new JsonUnit();
            jsonUnit.setLabel(unitString);
            jsonUnit.setFormula(unitString);
            jsonUnit.setPrefix(prefixFromUnit);

            JEVisUnitImp jeVisUnitImp = new JEVisUnitImp(jsonUnit);
            this.unit.set(jeVisUnitImp);
            this.unitPrefix.set(jeVisUnitImp.getPrefix().toString());
            this.unitFormula.set(jeVisUnitImp.getFormula());
            this.unitLabel.set(jeVisUnitImp.getLabel());
        }
    }

    private String getPrefixFromUnit(String unitString) {

        if (unitString.length() > 1) {
            if (unitString.equals("m²") || unitString.equals("m³") || unitString.equals("min")) return "";

            String sub = unitString.substring(0, 1);
            MetricPrefix prefixFromShort = UnitManager.getInstance().getPrefixFromShort(sub);

            if (prefixFromShort != null) {
                return prefixFromShort.toString();
            } else return "";
        } else return "";
    }

    public JEVisObject getObjectName() {
        return objectName.get();
    }

    public void setObjectName(JEVisObject objectName) {
        this.objectName.set(objectName);
    }

    public void setObjectName(String objectNameString) {
        this.objectName.set(null);
    }

    public SimpleObjectProperty<JEVisObject> objectNameProperty() {
        return objectName;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public Color getColor() {
        return color.get();
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public void setColor(String colorString) {
        this.color.set(Color.web(colorString));
    }

    public SimpleObjectProperty<Color> colorProperty() {
        return color;
    }

    public int getAxis() {
        return axis.get();
    }

    public void setAxis(int axis) {
        this.axis.set(axis);
    }

    public SimpleIntegerProperty axisProperty() {
        return axis;
    }

    public boolean isCalculation() {
        return calculation.get();
    }

    public void setCalculation(boolean calculation) {
        this.calculation.set(calculation);
    }

    public SimpleBooleanProperty calculationProperty() {
        return calculation;
    }

    public long getCalculationId() {
        return calculationId.get();
    }

    public void setCalculationId(long calculationId) {
        this.calculationId.set(calculationId);
    }

    public SimpleLongProperty calculationIdProperty() {
        return calculationId;
    }

    public BubbleType getBubbleType() {
        return bubbleType.get();
    }

    public void setBubbleType(BubbleType bubbleType) {
        this.bubbleType.set(bubbleType);
    }

    public void setBubbleType(String bubbleTypeString) {
        this.bubbleType.set(BubbleType.parseBubbleType(bubbleTypeString));
    }

    public SimpleObjectProperty<BubbleType> bubbleTypeProperty() {
        return bubbleType;
    }

    public ChartType getChartType() {
        return chartType.get();
    }

    public void setChartType(ChartType chartType) {
        this.chartType.set(chartType);
    }

    public void setChartType(String chartTypeString) {
        this.chartType.set(ChartType.parseChartType(chartTypeString));
    }

    public SimpleObjectProperty<ChartType> chartTypeProperty() {
        return chartType;
    }

    public AggregationPeriod getAggregationPeriod() {
        return aggregationPeriod.get();
    }

    public void setAggregationPeriod(AggregationPeriod aggregationPeriod) {
        this.aggregationPeriod.set(aggregationPeriod);
    }

    public void setAggregationPeriod(String aggregationPeriodString) {
        this.aggregationPeriod.set(AggregationPeriod.parseAggregation(aggregationPeriodString));
    }

    public SimpleObjectProperty<AggregationPeriod> aggregationPeriodProperty() {
        return aggregationPeriod;
    }

    public ManipulationMode getManipulationMode() {
        return manipulationMode.get();
    }

    public void setManipulationMode(ManipulationMode manipulationMode) {
        this.manipulationMode.set(manipulationMode);
    }

    public void setManipulationMode(String manipulationModeString) {
        this.manipulationMode.set(ManipulationMode.parseManipulation(manipulationModeString));
    }

    public SimpleObjectProperty<ManipulationMode> manipulationModeProperty() {
        return manipulationMode;
    }

    public String getIntervalStart() {
        return intervalStart.get();
    }

    public void setIntervalStart(String intervalStart) {
        this.intervalStart.set(intervalStart);
    }

    public void setIntervalStart(DateTime intervalStart) {
        this.intervalStart.set(intervalStart.toString());
    }

    public DateTime getIntervalStartDateTime() {
        return new DateTime(intervalStart.get());
    }

    public SimpleStringProperty intervalStartProperty() {
        return intervalStart;
    }

    public String getIntervalEnd() {
        return intervalEnd.get();
    }

    public void setIntervalEnd(String intervalEnd) {
        this.intervalEnd.set(intervalEnd);
    }

    public void setIntervalEnd(DateTime intervalEnd) {
        this.intervalEnd.set(intervalEnd.toString());
    }

    public DateTime getIntervalEndDateTime() {
        return new DateTime(intervalEnd.get());
    }

    public SimpleStringProperty intervalEndProperty() {
        return intervalEnd;
    }

    public boolean isIntervalEnabled() {
        return intervalEnabled.get();
    }

    public void setIntervalEnabled(boolean intervalEnabled) {
        this.intervalEnabled.set(intervalEnabled);
    }

    public SimpleBooleanProperty intervalEnabledProperty() {
        return intervalEnabled;
    }

    public String getCss() {
        return css.get();
    }

    public void setCss(String css) {
        this.css.set(css);
    }

    public SimpleStringProperty cssProperty() {
        return css;
    }

    @Override
    public ChartData clone() {
        ChartData newData = new ChartData();

        newData.setId(this.getId());
        newData.setAttributeString(this.getAttributeString());
        newData.setUnit(this.getUnit());
        newData.setObjectName(this.getObjectName());
        newData.setName(this.getName());
        newData.setColor(this.getColor());
        newData.setAxis(this.getAxis());
        newData.setCalculation(this.isCalculation());
        newData.setCalculationId(this.getCalculationId());
        newData.setBubbleType(this.getBubbleType());
        newData.setChartType(this.getChartType());
        newData.setAggregationPeriod(this.getAggregationPeriod());
        newData.setManipulationMode(this.getManipulationMode());
        newData.setIntervalEnabled(this.isIntervalEnabled());
        newData.setIntervalStart(this.getIntervalStart());
        newData.setIntervalEnd(this.getIntervalEnd());
        newData.setCss(this.getCss());

        return newData;
    }
}
