package org.jevis.application.Chart;

import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.application.Chart.ChartElements.TableEntry;
import org.jevis.commons.dataprocessing.AggregationMode;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.SampleGenerator;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ChartDataModel {
    private static final Logger logger = LogManager.getLogger(ChartDataModel.class);

    private TableEntry tableEntry;
    private String _title;
    private DateTime _selectedStart;
    private DateTime _selectedEnd;
    private JEVisObject _object;
    private JEVisAttribute _attribute;
    private Color _color = Color.LIGHTBLUE;
    private boolean _selected = false;
    private AggregationPeriod aggregationPeriod = AggregationPeriod.NONE;
    private JEVisObject _dataProcessorObject = null;
    private List<JEVisSample> samples = new ArrayList<>();
    private TreeMap<Double, JEVisSample> sampleMap = new TreeMap<>();
    private boolean _somethingChanged = true;
    private JEVisUnit _unit;
    private List<String> _selectedCharts = new ArrayList<>();

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
            logger.fatal(ex);
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
            if (getSelectedStart().isBefore(getSelectedEnd())) {
                try {

                    if (getDataProcessor() != null) {
                        _dataProcessorObject = getDataProcessor();
                        _attribute = _dataProcessorObject.getAttribute("Value");
                    } else _attribute = _object.getAttribute("Value");


                    SampleGenerator sg;
                    if (aggregationPeriod.equals(AggregationPeriod.NONE))
                        sg = new SampleGenerator(_attribute.getDataSource(), _attribute.getObject(), _attribute, getSelectedStart(),
                                getSelectedEnd(), AggregationMode.NONE, aggregationPeriod);
                    else
                        sg = new SampleGenerator(_attribute.getDataSource(), _attribute.getObject(), _attribute, getSelectedStart(),
                                getSelectedEnd(), AggregationMode.TOTAL, aggregationPeriod);

                    samples = sg.generateSamples();
                    samples = sg.getAggregatedSamples(samples);
                    samples = factorizeSamples(samples);


                } catch (Exception ex) {
                    //TODO: exception handling
                    logger.error("", ex);
                }
            } else {
                if (getDataProcessor() != null) {
                    logger.error("No interval between timestamps for object {}:{}. The end instant must be greater the start. ",
                            getDataProcessor().getName(), getDataProcessor().getID());
                } else {
                    logger.error("No interval between timestamps for object {}:{}. The end instant must be greater the start. ",
                            getObject().getName(), getObject().getID());
                }
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
                case "bar":
                    switch (inputUnit) {
                        case "atm":
                            factor = 1d / 1.01325;
                            break;
                    }
                    break;
                case "atm":
                    switch (inputUnit) {
                        case "bar":
                            factor = 1.01325 / 1d;
                            break;
                    }
                    break;
                case "m³/s":
                    switch (inputUnit) {
                        case "m³/min":
                            factor = 1d / 60D;
                            break;
                        case "m³/h":
                            factor = 1d / 3600d;
                            break;
                        case "l/s":
                            factor = 1d / 1000d;
                            break;
                        case "l/min":
                            factor = 1d / 60000d;
                            break;
                        case "l/h":
                            factor = 1d / 3600000;
                            break;
                    }
                    break;
                case "m³/min":
                    switch (inputUnit) {
                        case "m³/s":
                            factor = 60d;
                            break;
                        case "m³/h":
                            factor = 1d / 60d;
                            break;
                        case "l/s":
                            factor = 1000d * 60d;
                            break;
                        case "l/min":
                            factor = 1000d;
                            break;
                        case "l/h":
                            factor = 1000d / 60d;
                            break;
                    }
                    break;
                case "m³/h":
                    switch (inputUnit) {
                        case "m³/s":
                            factor = 60d * 60d;
                            break;
                        case "m³/min":
                            factor = 1d / 60d;
                            break;
                        case "l/s":
                            factor = 1000d / 3600d;
                            break;
                        case "l/min":
                            factor = 1000d / 60d;
                            break;
                        case "l/h":
                            factor = 1000d;
                            break;
                    }
                    break;
                case "l/s":
                    switch (inputUnit) {
                        case "m³/s":
                            //TODO: finish factors
                            break;
                        case "m³/min":
                            break;
                        case "m³/h":
                            break;
                        case "l/min":
                            break;
                        case "l/h":
                            break;
                    }
                    break;
                case "l/min":
                    switch (inputUnit) {
                        case "m³/s":
                            break;
                        case "m³/min":
                            break;
                        case "m³/h":
                            break;
                        case "l/s":
                            break;
                        case "l/h":
                            break;
                    }
                    break;
                case "l/h":
                    switch (inputUnit) {
                        case "m³/s":
                            break;
                        case "m³/min":
                            break;
                        case "m³/h":
                            break;
                        case "l/s":
                            break;
                        case "l/min":
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
                        logger.fatal(e1);
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

    public AggregationPeriod getAggregationPeriod() {
        return aggregationPeriod;
    }

    public void setAggregationPeriod(AggregationPeriod aggregationPeriod) {
        _somethingChanged = true;
        this.aggregationPeriod = aggregationPeriod;
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
            DateTime timeStampFromLastSample = getAttribute().getTimestampFromLastSample();
            if (timeStampFromLastSample != null) {
                timeStampFromLastSample = timeStampFromLastSample.minusDays(7);

                DateTime timeStampFromFirstSample = getAttribute().getTimestampFromFirstSample();
                if (timeStampFromFirstSample != null) {
                    if (timeStampFromFirstSample.isBefore(timeStampFromLastSample))
                        _selectedStart = timeStampFromLastSample;
                } else _selectedStart = timeStampFromFirstSample;

            } else {
                return null;
            }

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
            DateTime timeStampFromLastSample = getAttribute().getTimestampFromLastSample();
            if (timeStampFromLastSample == null) _selectedEnd = DateTime.now();
            else _selectedEnd = timeStampFromLastSample;
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
                String jevisClassName = getObject().getJEVisClassName();
                if (jevisClassName.equals("Data") || jevisClassName.equals("Clean Data")) {
                    if (_dataProcessorObject == null) attribute = getObject().getAttribute("Value");
                    else attribute = getDataProcessor().getAttribute("Value");

                    _attribute = attribute;
                }
            } catch (Exception ex) {
                logger.fatal(ex);
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


    public List<String> getSelectedcharts() {
        return _selectedCharts;
    }

    public void setSelectedCharts(List<String> selectedCharts) {

        _somethingChanged = true;
        this._selectedCharts = selectedCharts;
    }

    public void setSomethingChanged(boolean _somethingChanged) {
        this._somethingChanged = _somethingChanged;
    }

    @Override
    public String toString() {
        return "ChartDataModel{" +

                " _title='" + _title + '\'' +
                ", _selectedStart=" + _selectedStart +
                ", _selectedEnd=" + _selectedEnd +
                ", _object=" + _object +
                ", _attribute=" + _attribute +
                ", _color=" + _color +
                ", _selected=" + _selected +
                ", _somethingChanged=" + _somethingChanged +
                ", _unit=" + _unit +
                ", _selectedCharts=" + _selectedCharts +
                ", tableEntry=" + tableEntry +
                '}';
    }
}
