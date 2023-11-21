package org.jevis.jecc.plugin.action.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.ChartTools;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.joda.time.DateTime;

import java.util.List;

public class ConsumptionData {

    private static final Logger logger = LogManager.getLogger(ConsumptionData.class);
    @Expose
    @SerializedName("After")
    public final SimpleDoubleProperty after = new SimpleDoubleProperty(0.0);
    @Expose
    @SerializedName("Before")
    public final SimpleDoubleProperty actual = new SimpleDoubleProperty(0.0);
    @Expose
    @SerializedName("Diff")
    public final SimpleDoubleProperty diff = new SimpleDoubleProperty(0.0);

    @Expose
    @SerializedName("Object")
    public final SimpleLongProperty dataObject = new SimpleLongProperty(0L);

    @Expose
    @SerializedName("Calculation")
    public final SimpleLongProperty calcObject = new SimpleLongProperty(0L);

    @Expose
    @SerializedName("Before From Date")
    public final SimpleObjectProperty<DateTime> beforeFromDate = new SimpleObjectProperty<DateTime>(null);

    @Expose
    @SerializedName("Before Until Date")
    public final SimpleObjectProperty<DateTime> beforeUntilDate = new SimpleObjectProperty<DateTime>(null);

    @Expose
    @SerializedName("After From Date")
    public final SimpleObjectProperty<DateTime> afterFromDate = new SimpleObjectProperty<DateTime>(null);
    public final SimpleBooleanProperty isManual = new SimpleBooleanProperty(true);
    @Expose
    @SerializedName("After Until Date")
    public final SimpleObjectProperty<DateTime> afterUntilDate = new SimpleObjectProperty<DateTime>(null);
    @Expose
    @SerializedName("Data Source")
    public final SimpleStringProperty jevisLink = new SimpleStringProperty("EnPI Link",
            I18n.getInstance().getString("plugin.action.enpilink"), "");
    private final SimpleStringProperty unit = new SimpleStringProperty("kWh");
    public boolean isEnPI = false;
    private ActionData actionData = null;

    public ConsumptionData() {
    }

    /**
     * TODO Move this function to some better place
     *
     * @param ds
     * @param consumptionData
     * @return
     */
    public static JEVisObject getSourceObject(JEVisDataSource ds, ConsumptionData consumptionData) {
        JEVisObject obj = EmptyObject.getInstance();
        try {
            try {
                if (consumptionData.jevisLinkProperty().get().isEmpty()) {
                } else {
                    Long id = Long.parseLong(consumptionData.jevisLinkProperty().get());
                    if (id.equals(EmptyObject.getInstance().getID())) {
                        obj = EmptyObject.getInstance();
                    } else if (id.equals(FreeObject.getInstance().getID())) {
                        obj = FreeObject.getInstance();
                    } else {
                        obj = ds.getObject(id);
                    }
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }

            return obj;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    public static String getObjectName(JEVisDataSource ds, ConsumptionData consumptionData) {
        return getSourceObject(ds, consumptionData).getName();

    }

    private boolean isManual() {
        return dataObject.get() == 0;
    }

    public String getUnit() {
        return unit.get();
    }

    public SimpleStringProperty unitProperty() {
        return unit;
    }

    public void update() {
        actual.addListener((observableValue, number, t1) -> diff.setValue(calcDiff(actual.getValue(), after.getValue())));
        after.addListener((observableValue, number, t1) -> diff.setValue(calcDiff(actual.getValue(), after.getValue())));

        if (isEnPI) {
            // System.out.println("jevisLink.get(): " + jevisLink.get());
            //updateEnPIData();
        } else {

        }
    }

    public void updateEnPIData() {
        try {
            logger.debug("updateEnPIData");

            JEVisDataSource ds = ControlCenter.getDataSource();

            if (getSourceObject(ds, this).getID() > 0) {
                JEVisObject targetObj = getSourceObject(ds, this);

                logger.debug("this.targetObj:" + targetObj);

                try {
                    unit.setValue(targetObj.getAttribute("Unit").getLatestSample().getValueAsString());
                } catch (Exception ex) {
                    logger.error("no Unit set in Data Object", ex, ex);
                }

                JEVisUnit unit = targetObj.getAttribute("Value").getDisplayUnit();
                double before = calcEnpi(targetObj, unit,
                        beforeFromDate.get(),
                        beforeUntilDate.get());

                double after = calcEnpi(targetObj, unit,
                        afterFromDate.get(),
                        afterUntilDate.get());

                this.actual.set(before);
                this.after.set(after);
            }


        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    private double calcDiff(double before, double after) {
        return before - after;
    }

    private Double calcEnpi(JEVisObject dataObj, JEVisUnit unit, DateTime from, DateTime until) {
        try {
            //System.out.println("ENPI changed: " + dataObj);
            ChartDataRow chartDataRow = new ChartDataRow(ControlCenter.getDataSource());
            chartDataRow.setId(dataObj.getID());
            chartDataRow.setCalculationId(ChartTools.isObjectCalculated(dataObj));
            chartDataRow.setCalculation(true);
            chartDataRow.setSelectedStart(from);
            chartDataRow.setSelectedEnd(until);
            chartDataRow.setAggregationPeriod(AggregationPeriod.NONE);
            chartDataRow.setManipulationMode(ManipulationMode.NONE);
            chartDataRow.setAbsolute(true);
            // JEVisUnit unit = unit;//dataObj.getAttribute("Value").getDisplayUnit();
            chartDataRow.setUnit(unit);
            //System.out.println("Get Data");
            List<JEVisSample> samples = chartDataRow.getSamples();

            for (JEVisSample jeVisSample : samples) {
                try {
                    //System.out.println("Sample: " + jeVisSample);
                    return jeVisSample.getValueAsDouble();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Double(0);
    }

    public void setEnPI(boolean enPI) {
        isEnPI = enPI;
    }

    public SimpleDoubleProperty afterProperty() {
        return after;
    }

    public SimpleDoubleProperty actualProperty() {
        return actual;
    }

    public SimpleDoubleProperty diffProperty() {
        return diff;
    }

    public SimpleLongProperty dataObjectProperty() {
        return dataObject;
    }

    public SimpleLongProperty calcObjectProperty() {
        return calcObject;
    }

    public SimpleObjectProperty<DateTime> beforeFromDateProperty() {
        return beforeFromDate;
    }

    public SimpleObjectProperty<DateTime> beforeUntilDateProperty() {
        return beforeUntilDate;
    }

    public SimpleObjectProperty<DateTime> afterFromDateProperty() {
        return afterFromDate;
    }

    public SimpleObjectProperty<DateTime> afterUntilDateProperty() {
        return afterUntilDate;
    }

    public ActionData actionData() {
        return actionData;
    }

    public SimpleBooleanProperty isManualProperty() {
        return isManual;
    }

    public SimpleStringProperty jevisLinkProperty() {
        return jevisLink;
    }

    private void addActionData(ActionData actionData) {
        this.actionData = actionData;
    }


}
