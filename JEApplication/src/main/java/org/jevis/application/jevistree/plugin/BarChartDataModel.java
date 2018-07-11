package org.jevis.application.jevistree.plugin;

import javafx.scene.paint.Color;
import org.jevis.api.*;
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

public class BarChartDataModel {


    private TableEntry tableEntry;
    private String _title;
    private DateTime _selectedStart;
    private DateTime _selectedEnd;
    private JEVisObject _object;
    private JEVisAttribute _attribute;
    private Color _color = Color.LIGHTBLUE;
    private boolean _selected = false;
    private Process _task = null;
    private BarchartPlugin.AGGREGATION aggregation = BarchartPlugin.AGGREGATION.None;
    private JEVisObject _dataProcessorObject = null;
    private List<JEVisSample> samples = new ArrayList<>();
    private TreeMap<Double, JEVisSample> sampleMap = new TreeMap<>();
    private boolean _somethingChanged = true;
    private JEVisUnit _unit;

    public BarChartDataModel() {
    }

    public JEVisUnit getUnit() {
        try {
            if (_unit == null) {
                if (getAttribute() != null) {
                    _unit = getAttribute().getDisplayUnit();
                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(BarchartPlugin.class.getName()).log(Level.SEVERE, null, ex);
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
                if (aggregation == BarchartPlugin.AGGREGATION.None) {

                } else if (aggregation == BarchartPlugin.AGGREGATION.Daily) {
                    aggregate = new BasicProcess();
                    aggregate.setJEVisDataSource(ds);
                    aggregate.setID("Dynamic");
                    aggregate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.days(1).toString());
                    aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.days(1).toString()));
                } else if (aggregation == BarchartPlugin.AGGREGATION.Monthly) {
                    aggregate = new BasicProcess();
                    aggregate.setJEVisDataSource(ds);
                    aggregate.setID("Dynamic");
                    aggregate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.months(1).toString());
                    aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(1).toString()));
                } else if (aggregation == BarchartPlugin.AGGREGATION.Weekly) {
                    aggregate = new BasicProcess();
                    aggregate.setJEVisDataSource(ds);
                    aggregate.setID("Dynamic");
                    aggregate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.weeks(1).toString());
                    aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.weeks(1).toString()));
                } else if (aggregation == BarchartPlugin.AGGREGATION.Yearly) {
//                        System.out.println("year.....  " + Period.years(1).toString());
                    aggregate = new BasicProcess();
                    aggregate.setJEVisDataSource(ds);
                    aggregate.setID("Dynamic");
                    aggregate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.years(1).toString());
                    aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.years(1).toString()));
                }

//                    Process dataPorceessor = null;
                if (getDataProcessor() != null) {
//                        dataPorceessor = ProcessChains.getProcessChain(getDataProcessor());
                    _dataProcessorObject = getDataProcessor();
                    _attribute = _dataProcessorObject.getAttribute("Value");
                }
//                    if (dataPorceessor == null) {
                if (aggregate != null) {
                    Process input = new BasicProcess();
                    input.setJEVisDataSource(ds);
                    input.setID("Dynamic Input");
                    input.setFunction(new InputFunction());

                    input.getOptions().add(new BasicProcessOption(InputFunction.ATTRIBUTE_ID, _attribute.getName()));
                    input.getOptions().add(new BasicProcessOption(InputFunction.OBJECT_ID, _attribute.getObject().getID() + ""));
//                            input.getOptions().put(InputFunction.ATTRIBUTE_ID, getAttribute().getName());
//                            input.getOptions().put(InputFunction.OBJECT_ID, getAttribute().getObject().getID() + "");
                    aggregate.setSubProcesses(Arrays.asList(input));
                    samples.addAll(factorizeSamples(aggregate.getResult()));
                } else {

                    samples.addAll(factorizeSamples(getAttribute().getSamples(getSelectedStart(), getSelectedEnd())));
//                            samples.addAll(getAttribute().getAllSamples());
                }

//                    } else if (aggrigate != null) {
//                        aggrigate.setSubProcesses(Arrays.asList(dataPorceessor));
//                        samples.addAll(aggrigate.getResult());
//                    } else {
//                        samples.addAll(dataPorceessor.getResult());
//                    }

            } catch (Exception ex) {
                //TODO: exeption handling
                ex.printStackTrace();
            }
        }

        return samples;
    }

    private List<JEVisSample> factorizeSamples(List<JEVisSample> inputList) throws JEVisException {
        if (_unit != null) {
            List<JEVisSample> outputList = new ArrayList<>();
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
                            factor = 1000000 / 1d;
                            break;
                        case "GW":
                            factor = 1000000000 / 1d;
                            break;
                        case "Wh":
                            factor = 4 / 1d;
                            break;
                        case "kWh":
                            factor = 4d / 1000d;
                            break;
                        case "MWh":
                            factor = 4 / 1000000d;
                            break;
                        case "GWh":
                            factor = 4 / 1000000000d;
                            break;
                    }
                    break;
                case "kW":
                    switch (inputUnit) {
                        case "W":
                            factor = 1d / 1000;
                            break;
                        case "MW":
                            factor = 1 / 1000d;
                            break;
                        case "GW":
                            factor = 1 / 1000000d;
                            break;
                        case "Wh":
                            factor = 4000 / 1d;
                            break;
                        case "kWh":
                            factor = 4d / 1d;
                            break;
                        case "MWh":
                            factor = 4 / 1000d;
                            break;
                        case "GWh":
                            factor = 4 / 1000000d;
                            break;
                    }
                    break;
                case "MW":
                    switch (inputUnit) {
                        case "W":
                            factor = 1 / 1000000d;
                            break;
                        case "kW":
                            factor = 1 / 1000d;
                            break;
                        case "GW":
                            factor = 1000d;
                            break;
                        case "Wh":
                            factor = 4 / 1000000d;
                            break;
                        case "kWh":
                            factor = 4d / 1000d;
                            break;
                        case "MWh":
                            factor = 4 / 1d;
                            break;
                        case "GWh":
                            factor = 4000 / 1d;
                            break;
                    }
                    break;
                case "GW":
                    switch (inputUnit) {
                        case "W":
                            factor = 1 / 1000000000d;
                            break;
                        case "kW":
                            factor = 1 / 1000000d;
                            break;
                        case "MW":
                            factor = 1 / 1000d;
                            break;
                        case "Wh":
                            factor = 4 / 1000000000d;
                            break;
                        case "kWh":
                            factor = 4d / 1000000d;
                            break;
                        case "MWh":
                            factor = 4 / 1000d;
                            break;
                        case "GWh":
                            factor = 4 / 1d;
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
                            factor = 1 / 1000.0;
                            break;
                        case "MWh":
                            factor = 1000.0;
                            break;
                        case "GWh":
                            factor = 1000000.0;
                            break;
                        case "W":
                            factor = 1000.0 / 4.0;
                            break;
                        case "kW":
                            factor = 1d / 4.0;
                            break;
                        case "MW":
                            factor = 1d / 4000d;
                            break;
                        case "GW":
                            factor = 1 / 4000000d;
                            break;
                    }
                    break;
                case "MWh":
                    switch (inputUnit) {
                        case "Wh":
                            factor = 1 / 1000000d;
                            break;
                        case "kWh":
                            factor = 1 / 1000d;
                            break;
                        case "GWh":
                            factor = 1000d;
                            break;
                        case "W":
                            factor = 1 / 4000000d;
                            break;
                        case "kW":
                            factor = 1 / 4000d;
                            break;
                        case "MW":
                            factor = 1 / 4d;
                            break;
                        case "GW":
                            factor = 1000 / 4d;
                            break;
                    }
                    break;
                case "GWh":
                    switch (inputUnit) {
                        case "Wh":
                            factor = 1 / 1000000000d;
                            break;
                        case "kWh":
                            factor = 1 / 1000000d;
                            break;
                        case "MWh":
                            factor = 1 / 1000d;
                            break;
                        case "W":
                            factor = 1d / 4000000000d;
                            break;
                        case "kW":
                            factor = 1d / 4000000d;
                            break;
                        case "MW":
                            factor = 1 / 4000d;
                            break;
                        case "GW":
                            factor = 1 / 4d;
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
                            factor = 1 / 1000d;
                            break;
                    }
                    break;
                default:
                    break;
            }

            for (JEVisSample sample : inputList) {
                JEVisSample newSample = sample;
                newSample.setValue(sample.getValueAsDouble() * factor);
                outputList.add(newSample);
            }
            return outputList;
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
        _somethingChanged = true;
        return _dataProcessorObject;
    }

    public void setDataProcessor(JEVisObject _dataProcessor) {
        this._dataProcessorObject = _dataProcessor;
    }

    public BarchartPlugin.AGGREGATION getAggregation() {
        return aggregation;
    }

    public void setAggregation(BarchartPlugin.AGGREGATION aggregation) {
        _somethingChanged = true;
        this.aggregation = aggregation;
    }

    public boolean getSelected() {

        return _selected;
    }

    public void setSelected(boolean selected) {
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
            _selectedStart = getAttribute().getTimestampFromFirstSample();
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
                if (getObject().getJEVisClassName().equals("Data") || getObject().getJEVisClassName().equals("Clean Data")) {
                    JEVisAttribute values = getObject().getAttribute("Value");
                    _attribute = values;
                }
            } catch (Exception ex) {
                Logger.getLogger(BarchartPlugin.class.getName()).log(Level.SEVERE, null, ex);
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

    public void set_somethingChanged(boolean _somethingChanged) {
        this._somethingChanged = _somethingChanged;
    }
}
