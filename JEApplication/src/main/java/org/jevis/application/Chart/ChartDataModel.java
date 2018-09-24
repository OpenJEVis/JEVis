package org.jevis.application.Chart;

import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.application.Chart.ChartElements.TableEntry;
import org.jevis.application.jevistree.plugin.ChartPlugin;
import org.jevis.commons.dataprocessing.BasicProcess;
import org.jevis.commons.dataprocessing.BasicProcessOption;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.ProcessOptions;
import org.jevis.commons.dataprocessing.function.AggrigatorFunction;
import org.jevis.commons.dataprocessing.function.InputFunction;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChartDataModel {


    private TableEntry tableEntry;
    private String _title;
    private DateTime _selectedStart;
    private DateTime _selectedEnd;
    private JEVisObject _object;
    private JEVisAttribute _attribute;
    private Color _color = Color.LIGHTBLUE;
    private boolean _selected = false;
    private ChartPlugin.AGGREGATION aggregation = ChartPlugin.AGGREGATION.None;
    private JEVisObject _dataProcessorObject = null;
    private List<JEVisSample> samples = new ArrayList<>();
    private TreeMap<Double, JEVisSample> sampleMap = new TreeMap<>();
    private boolean _somethingChanged = true;
    private JEVisUnit _unit;
    private List<String> _selectedCharts = new ArrayList<>();
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ChartDataModel.class);


    public ChartDataModel() {
    }

    public JEVisUnit getUnit() {
        try {
            if (_unit == null) {
                if (getAttribute() != null) {
                    _unit = getAttribute().getDisplayUnit();
                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ChartPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        return _unit;
    }

    public void setUnit(JEVisUnit _unit) {
        _somethingChanged = true;
        this._unit = _unit;
    }

    public List<JEVisSample> getSamples() {

        if (_somethingChanged) {
            _somethingChanged = false;
            samples = new ArrayList<>();

            try {
                JEVisDataSource ds = _object.getDataSource();
                Process aggregate = null;
                switch (aggregation) {
                    case None:
                        break;
                    case Hourly:
                        aggregate = new BasicProcess();
                        aggregate.setJEVisDataSource(ds);
                        aggregate.setID("Dynamic");
                        aggregate.setFunction(new AggrigatorFunction());
                        aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.hours(1).toString()));
                        break;
                    case Daily:
                        aggregate = new BasicProcess();
                        aggregate.setJEVisDataSource(ds);
                        aggregate.setID("Dynamic");
                        aggregate.setFunction(new AggrigatorFunction());
                        aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.days(1).toString()));
                        break;
                    case Monthly:
                        aggregate = new BasicProcess();
                        aggregate.setJEVisDataSource(ds);
                        aggregate.setID("Dynamic");
                        aggregate.setFunction(new AggrigatorFunction());
                        aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(1).toString()));
                        break;
                    case Weekly:
                        aggregate = new BasicProcess();
                        aggregate.setJEVisDataSource(ds);
                        aggregate.setID("Dynamic");
                        aggregate.setFunction(new AggrigatorFunction());
                        aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.weeks(1).toString()));
                        break;
                    case Yearly:
                        aggregate = new BasicProcess();
                        aggregate.setJEVisDataSource(ds);
                        aggregate.setID("Dynamic");
                        aggregate.setFunction(new AggrigatorFunction());
                        aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.years(1).toString()));
                        break;
                }

                if (getDataProcessor() != null) {
                    _dataProcessorObject = getDataProcessor();
                    _attribute = _dataProcessorObject.getAttribute("Value");
                } else _attribute = _object.getAttribute("Value");

                if (aggregate != null) {
                    Process input = new BasicProcess();
                    input.setJEVisDataSource(ds);
                    input.setID("Dynamic Input");
                    input.setFunction(new InputFunction());

                    input.getOptions().add(new BasicProcessOption(InputFunction.ATTRIBUTE_ID, _attribute.getName()));
                    input.getOptions().add(new BasicProcessOption(InputFunction.OBJECT_ID, _attribute.getObject().getID() + ""));
                    aggregate.setSubProcesses(Arrays.asList(input));
                    samples.addAll(factorizeSamples(aggregate.getResult()));
                } else {
                    samples.addAll(factorizeSamples(getAttribute().getSamples(getSelectedStart(), getSelectedEnd())));
                }

            } catch (Exception ex) {
                //TODO: exception handling
                logger.error("", ex);
            }
        }

        System.gc();

        return samples;
    }

    public void setSamples(List<JEVisSample> samples) {
        this.samples = samples;
    }

    private List<JEVisSample> factorizeSamples(List<JEVisSample> inputList) throws JEVisException {
        if (_unit != null) {
            final String outputUnit = UnitManager.getInstance().formate(_unit);
            final String inputUnit = UnitManager.getInstance().formate(_attribute.getDisplayUnit());
            Double factor = 1.0;
            switch (outputUnit) {
                case "W":
                    switch (inputUnit) {
                        case "kW":
                            factor = 1000d / 1d;
                            break;
                        case "MW":
                            factor = 1000000d / 1d;
                            break;
                        case "GW":
                            factor = 1000000000d / 1d;
                            break;
                        case "Wh":
                            factor = 4d / 1d;
                            break;
                        case "kWh":
                            factor = 4d / 1000d;
                            break;
                        case "MWh":
                            factor = 4d / 1000000d;
                            break;
                        case "GWh":
                            factor = 4d / 1000000000d;
                            break;
                    }
                    break;
                case "kW":
                    switch (inputUnit) {
                        case "W":
                            factor = 1d / 1000;
                            break;
                        case "MW":
                            factor = 1d / 1000d;
                            break;
                        case "GW":
                            factor = 1d / 1000000d;
                            break;
                        case "Wh":
                            factor = 4000d / 1d;
                            break;
                        case "kWh":
                            factor = 4d / 1d;
                            break;
                        case "MWh":
                            factor = 4d / 1000d;
                            break;
                        case "GWh":
                            factor = 4d / 1000000d;
                            break;
                    }
                    break;
                case "MW":
                    switch (inputUnit) {
                        case "W":
                            factor = 1d / 1000000d;
                            break;
                        case "kW":
                            factor = 1d / 1000d;
                            break;
                        case "GW":
                            factor = 1000d;
                            break;
                        case "Wh":
                            factor = 4d / 1000000d;
                            break;
                        case "kWh":
                            factor = 4d / 1000d;
                            break;
                        case "MWh":
                            factor = 4d / 1d;
                            break;
                        case "GWh":
                            factor = 4000d / 1d;
                            break;
                    }
                    break;
                case "GW":
                    switch (inputUnit) {
                        case "W":
                            factor = 1d / 1000000000d;
                            break;
                        case "kW":
                            factor = 1d / 1000000d;
                            break;
                        case "MW":
                            factor = 1d / 1000d;
                            break;
                        case "Wh":
                            factor = 4d / 1000000000d;
                            break;
                        case "kWh":
                            factor = 4d / 1000000d;
                            break;
                        case "MWh":
                            factor = 4d / 1000d;
                            break;
                        case "GWh":
                            factor = 4d / 1d;
                            break;
                    }
                    break;
                case "Wh":
                    switch (inputUnit) {
                        case "kWh":
                            factor = 1000d;
                            break;
                        case "MWh":
                            factor = 1000000d / 1d;
                            break;
                        case "GWh":
                            factor = 1000000000d / 1d;
                            break;
                        case "W":
                            factor = 1 / 4d;
                            break;
                        case "kW":
                            factor = 1000d / 4d;
                            break;
                        case "MW":
                            factor = 1000000d / 4d;
                            break;
                        case "GW":
                            factor = 1000000000d / 4d;
                            break;
                    }
                    break;
                case "kWh":
                    switch (inputUnit) {
                        case "Wh":
                            factor = 1d / 1000d;
                            break;
                        case "MWh":
                            factor = 1000d;
                            break;
                        case "GWh":
                            factor = 1000000d;
                            break;
                        case "W":
                            factor = 1000d / 4d;
                            break;
                        case "kW":
                            factor = 1d / 4d;
                            break;
                        case "MW":
                            factor = 1d / 4000d;
                            break;
                        case "GW":
                            factor = 1d / 4000000d;
                            break;
                    }
                    break;
                case "MWh":
                    switch (inputUnit) {
                        case "Wh":
                            factor = 1d / 1000000d;
                            break;
                        case "kWh":
                            factor = 1d / 1000d;
                            break;
                        case "GWh":
                            factor = 1000d;
                            break;
                        case "W":
                            factor = 1d / 4000000d;
                            break;
                        case "kW":
                            factor = 1d / 4000d;
                            break;
                        case "MW":
                            factor = 1d / 4d;
                            break;
                        case "GW":
                            factor = 1000d / 4d;
                            break;
                    }
                    break;
                case "GWh":
                    switch (inputUnit) {
                        case "Wh":
                            factor = 1d / 1000000000d;
                            break;
                        case "kWh":
                            factor = 1d / 1000000d;
                            break;
                        case "MWh":
                            factor = 1d / 1000d;
                            break;
                        case "W":
                            factor = 1d / 4000000000d;
                            break;
                        case "kW":
                            factor = 1d / 4000000d;
                            break;
                        case "MW":
                            factor = 1d / 4000d;
                            break;
                        case "GW":
                            factor = 1d / 4d;
                            break;
                    }
                    break;
                case "L":
                    switch (inputUnit) {
                        case "m³":
                            factor = 1000d;
                            break;
                    }
                    break;
                case "m³":
                    switch (inputUnit) {
                        case "L":
                            factor = 1d / 1000d;
                            break;
                    }
                    break;
                case "kg":
                    switch (inputUnit) {
                        case "t":
                            factor = 1000d;
                            break;
                    }
                    break;
                case "t":
                    switch (inputUnit) {
                        case "kg":
                            factor = 1d / 1000d;
                            break;
                    }
                    break;
                default:
                    break;
            }

            Double finalFactor = factor;
            inputList.forEach(sample -> {
                try {
                    sample.setValue(sample.getValueAsDouble() * finalFactor);
                } catch (Exception e) {
                    try {
                        logger.error("Error in sample: " + sample.getTimestamp() + " : " + sample.getValue()
                                + " of attribute: " + getAttribute().getName()
                                + " of object: " + getObject().getName() + ":" + getObject().getID());
                    } catch (Exception e1) {
                    }
                }
            });

            return inputList;
        } else return inputList;
    }

    public TableEntry getTableEntry() {
        return tableEntry;
    }

    public void setTableEntry(TableEntry tableEntry) {
        this.tableEntry = tableEntry;
    }

    public TreeMap<Double, JEVisSample> getSampleMap() {
        return sampleMap;
    }

    public void setSampleMap(TreeMap<Double, JEVisSample> sampleMap) {
        this.sampleMap = sampleMap;
    }

    public JEVisObject getDataProcessor() {
        return _dataProcessorObject;
    }

    public void setDataProcessor(JEVisObject _dataProcessor) {
        _somethingChanged = true;
        this._dataProcessorObject = _dataProcessor;
    }

    public ChartPlugin.AGGREGATION getAggregation() {
        return aggregation;
    }

    public void setAggregation(ChartPlugin.AGGREGATION aggregation) {
        _somethingChanged = true;
        this.aggregation = aggregation;
    }

    public boolean getSelected() {

        return _selected;
    }

    public void setSelected(boolean selected) {

        _somethingChanged = true;
        _selected = selected;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String _title) {
        this._title = _title;
    }

    public DateTime getSelectedStart() {

        if (_selectedStart != null) {
            return _selectedStart;
        } else if (getAttribute() != null) {
            DateTime dt = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), 1, 0, 0, 0, 0);
            dt = dt.minusMonths(1);
            DateTime timeStampFromFirstSample = getAttribute().getTimestampFromFirstSample();

            if (timeStampFromFirstSample.isBefore(dt)) _selectedStart = dt;
            else _selectedStart = timeStampFromFirstSample;

            return _selectedStart;
        } else {
            return null;
        }
    }

    public void setSelectedStart(DateTime selectedStart) {
        if (_selectedEnd == null || !_selectedEnd.equals(selectedStart)) {
            _somethingChanged = true;
        }
        this._selectedStart = selectedStart;
    }

    public DateTime getSelectedEnd() {
        if (_selectedEnd != null) {
            return _selectedEnd;
        } else if (getAttribute() != null) {
            _selectedEnd = getAttribute().getTimestampFromLastSample();
            return _selectedEnd;
        } else {
            return null;
        }
    }

    public void setSelectedEnd(DateTime selectedEnd) {
        if (_selectedEnd == null || !_selectedEnd.equals(selectedEnd)) {
            _somethingChanged = true;
        }
        this._selectedEnd = selectedEnd;
    }

    public JEVisObject getObject() {
        return _object;
    }

    public void setObject(JEVisObject _object) {
        this._object = _object;
    }

    public JEVisAttribute getAttribute() {
        if (_attribute == null) {
            try {
                JEVisAttribute attribute = null;
                if (getObject().getJEVisClassName().equals("Data") || getObject().getJEVisClassName().equals("Clean Data")) {
                    if (_dataProcessorObject == null) attribute = getObject().getAttribute("Value");
                    else attribute = getDataProcessor().getAttribute("Value");

                    _attribute = attribute;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return _attribute;
    }

    public void setAttribute(JEVisAttribute _attribute) {
        this._attribute = _attribute;
    }

    public Color getColor() {
        return _color;
    }

    public void setColor(Color _color) {
        this._color = _color;
    }

    public boolean isSelectable() {
        return getAttribute() != null && getAttribute().hasSample();
    }


    public List<String> get_selectedCharts() {
        return _selectedCharts;
    }

    public void set_selectedCharts(List<String> _selectedCharts) {

        _somethingChanged = true;
        this._selectedCharts = _selectedCharts;
    }

    public void set_somethingChanged(boolean _somethingChanged) {
        this._somethingChanged = _somethingChanged;
    }

}
