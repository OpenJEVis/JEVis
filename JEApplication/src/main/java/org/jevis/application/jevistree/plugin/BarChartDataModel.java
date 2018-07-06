package org.jevis.application.jevistree.plugin;

import javafx.scene.paint.Color;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.BasicProcess;
import org.jevis.commons.dataprocessing.BasicProcessOption;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.ProcessOptions;
import org.jevis.commons.dataprocessing.function.AggrigatorFunction;
import org.jevis.commons.dataprocessing.function.InputFunction;
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
            if (getAttribute() != null) {
                return getAttribute().getDisplayUnit();
            }

//            return _unit;
        } catch (JEVisException ex) {
            Logger.getLogger(BarchartPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void setUnit(JEVisUnit _unit) {
        _somethingChanged = true;
        this._unit = _unit;
    }

    public List<JEVisSample> getSamples() {
        System.out.println("getSamples()");

        if (_somethingChanged) {
            _somethingChanged = false;
            samples = new ArrayList<>();

            try {
                JEVisDataSource ds = _object.getDataSource();
                Process aggrigate = null;
                if (aggregation == BarchartPlugin.AGGREGATION.None) {

                } else if (aggregation == BarchartPlugin.AGGREGATION.Daily) {
                    aggrigate = new BasicProcess();
                    aggrigate.setJEVisDataSource(ds);
                    aggrigate.setID("Dynamic");
                    aggrigate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.days(1).toString());
                    aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.days(1).toString()));
                } else if (aggregation == BarchartPlugin.AGGREGATION.Monthly) {
                    aggrigate = new BasicProcess();
                    aggrigate.setJEVisDataSource(ds);
                    aggrigate.setID("Dynamic");
                    aggrigate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.months(1).toString());
                    aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(1).toString()));
                } else if (aggregation == BarchartPlugin.AGGREGATION.Weekly) {
                    aggrigate = new BasicProcess();
                    aggrigate.setJEVisDataSource(ds);
                    aggrigate.setID("Dynamic");
                    aggrigate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.weeks(1).toString());
                    aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.weeks(1).toString()));
                } else if (aggregation == BarchartPlugin.AGGREGATION.Yearly) {
//                        System.out.println("year.....  " + Period.years(1).toString());
                    aggrigate = new BasicProcess();
                    aggrigate.setJEVisDataSource(ds);
                    aggrigate.setID("Dynamic");
                    aggrigate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.years(1).toString());
                    aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.years(1).toString()));
                }

//                    Process dataPorceessor = null;
                if (getDataProcessor() != null) {
//                        dataPorceessor = ProcessChains.getProcessChain(getDataProcessor());
                    _object = getDataProcessor();
                    _attribute = _object.getAttribute("Value");
                }
//                    if (dataPorceessor == null) {
                if (aggrigate != null) {
                    Process input = new BasicProcess();
                    input.setJEVisDataSource(ds);
                    input.setID("Dynamic Input");
                    input.setFunction(new InputFunction());

                    input.getOptions().add(new BasicProcessOption(InputFunction.ATTRIBUTE_ID, _attribute.getName()));
                    input.getOptions().add(new BasicProcessOption(InputFunction.OBJECT_ID, _attribute.getObject().getID() + ""));
//                            input.getOptions().put(InputFunction.ATTRIBUTE_ID, getAttribute().getName());
//                            input.getOptions().put(InputFunction.OBJECT_ID, getAttribute().getObject().getID() + "");
                    aggrigate.setSubProcesses(Arrays.asList(input));
                    samples.addAll(aggrigate.getResult());
                } else {
                    samples.addAll(getAttribute().getSamples(getSelectedStart(), getSelectedEnd()));
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
        System.out.println("is selectec: " + _object.getName() + "   unit: " + getUnit());
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

//            if (_selectedStart != null && getAttribute() != null) {
//                System.out.print("-");
////                System.out.println("getSelectedStart1 " + getAttribute().getTimestampFromFirstSample());
//                return getAttribute().getTimestampFromFirstSample();
//            }
//            System.out.print(".");
////            System.out.println("getSelectedStart2 " + _selectedStart);
//            return _selectedStart;
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

//            if (_selectedEnd != null && getAttribute() != null) {
//                return getAttribute().getTimestampFromLastSample();
//            }
//
//            return _selectedEnd;
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
//            System.out.println("new DataModel: " + _object);
        this._object = _object;
    }

    public JEVisAttribute getAttribute() {

        if (_attribute == null) {
//                System.out.println("att is null");
            try {
                if (getObject().getJEVisClassName().equals("Data") || getObject().getJEVisClassName().equals("Clean Data")) {
                    JEVisAttribute values = getObject().getAttribute("Value");
                    _attribute = values;
                }
//                    return values;
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
