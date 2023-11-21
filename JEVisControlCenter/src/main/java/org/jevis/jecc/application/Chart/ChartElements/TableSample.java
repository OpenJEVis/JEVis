package org.jevis.jecc.application.Chart.ChartElements;

import com.ibm.icu.text.NumberFormat;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.jeapi.ws.JEVisObjectWS;
import org.joda.time.DateTime;

import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TableSample {
    final private DateTime timeStamp;
    private final List<String> columnValues = new ArrayList<>();
    private final List<Double> columnNumbers = new ArrayList<>();
    private final List<Long> columnNumbersSize = new ArrayList<>();
    private final List<XYChartSerie> chartSeries = new ArrayList<>();
    private final List<JEVisUnit> units = new ArrayList<>();
    private final List<NumberFormat> numberFormats = new ArrayList<>();
    private final List<Boolean> isCalculation = new ArrayList<>();
    private final List<JEVisObject> calculationObjects = new ArrayList<>();
    private final UUID uuid;
    private String columnIdentifier = "";

    public TableSample(DateTime timeStamp, int columns) {
        this.timeStamp = timeStamp;
        this.uuid = UUID.randomUUID();

        for (int i = 0; i < columns; i++) {
            columnValues.add("");
            columnNumbers.add(0d);
            columnNumbersSize.add(0L);
            numberFormats.add(NumberFormat.getInstance());
            isCalculation.add(false);
            calculationObjects.add(new JEVisObjectWS(null, new JsonObject()));
            chartSeries.add(new XYChartSerie());

            units.add(new JEVisUnitImp(Unit.ONE));
        }
    }

    public DateTime getTimeStamp() {
        return timeStamp;
    }

    public List<String> getColumnValues() {
        return columnValues;
    }

    public List<Double> getColumnNumbers() {
        return columnNumbers;
    }

    public List<Long> getColumnNumbersSize() {
        return columnNumbersSize;
    }

    public List<NumberFormat> getNumberFormats() {
        return numberFormats;
    }

    public List<Boolean> isCalculation() {
        return isCalculation;
    }

    public List<JEVisObject> getCalculationObjects() {
        return calculationObjects;
    }

    public List<JEVisUnit> getUnits() {
        return units;
    }

    public String getColumnIdentifier() {
        return columnIdentifier;
    }

    public void setColumnIdentifier(String columnIdentifier) {
        this.columnIdentifier = columnIdentifier;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<XYChartSerie> getChartSeries() {
        return chartSeries;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TableSample otherObj) {
            return otherObj.getUuid().equals(this.getUuid()) && otherObj.getTimeStamp().equals(this.getTimeStamp());
        }
        return false;
    }
}
