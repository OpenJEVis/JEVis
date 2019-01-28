package org.jevis.jeconfig.application.Chart.ChartElements;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.List;
import java.util.TreeMap;

public class XYChartSerie {
    private static final Logger logger = LogManager.getLogger(XYChartSerie.class);
    Integer yAxis;
    ObservableList<MultiAxisChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
    MultiAxisChart.Series<Number, Number> serie;
    TableEntry tableEntry;
    private DateTime timeStampFromFirstSample = DateTime.now();
    private DateTime timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);
    ChartDataModel singleRow;
    Boolean hideShowIcons;
    TreeMap<Double, JEVisSample> sampleMap;

    public XYChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        this.singleRow = singleRow;
        this.yAxis = singleRow.getAxis();
        this.hideShowIcons = hideShowIcons;
        this.serie = new MultiAxisChart.Series<>(getTableEntryName(), seriesData);

        generateSeriesFromSamples();
    }

    public void generateSeriesFromSamples() throws JEVisException {
        tableEntry = new TableEntry(getTableEntryName());
        this.serie.setName(getTableEntryName());

        tableEntry.setColor(singleRow.getColor());

        List<JEVisSample> samples = singleRow.getSamples();
        JEVisUnit unit = singleRow.getUnit();

        seriesData.clear();

        int samplesSize = samples.size();
//        int seriesDataSize = seriesData.size();
//
//        if (samplesSize < seriesDataSize) {
//            seriesData.subList(samplesSize, seriesDataSize).clear();
//        } else if (samplesSize > seriesDataSize) {
//            for (int i = seriesDataSize; i < samplesSize; i++) {
//                seriesData.add(new MultiAxisChart.Data<>());
//            }
//        }

        if (samplesSize > 0) {
            try {

                if (samples.get(0).getTimestamp().isBefore(getTimeStampFromFirstSample()))
                    setTimeStampFromFirstSample(samples.get(0).getTimestamp());

                if (samples.get(samples.size() - 1).getTimestamp().isAfter(getTimeStampFromLastSample()))
                    setTimeStampFromLastSample(samples.get(samples.size() - 1).getTimestamp());

            } catch (Exception e) {
                logger.error("Couldn't get timestamps from samples. " + e);
            }
        }

        sampleMap = new TreeMap<>();

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double avg = 0.0;
        Double sum = 0.0;

        for (JEVisSample sample : samples) {
            try {
//                int index = samples.indexOf(sample);

                DateTime dateTime = sample.getTimestamp();
                Double currentValue = sample.getValueAsDouble();

                min = Math.min(min, currentValue);
                max = Math.max(max, currentValue);
                sum += currentValue;

                Long timestamp = dateTime.getMillis();

//                MultiAxisChart.Data<Number, Number> data = seriesData.get(index);
                MultiAxisChart.Data<Number, Number> data = new MultiAxisChart.Data<>();
                data.setXValue(timestamp);
                data.setYValue(currentValue);
                data.setExtraValue(yAxis);
                data.setNode(generateNode(sample));
                seriesData.add(data);

                sampleMap.put(timestamp.doubleValue(), sample);

            } catch (JEVisException e) {

            }
        }

        QuantityUnits qu = new QuantityUnits();
        boolean isQuantity = qu.isQuantityUnit(unit);

        if (samples.size() > 0)
            avg = sum / samples.size();

        NumberFormat nf_out = NumberFormat.getNumberInstance();
        nf_out.setMaximumFractionDigits(2);
        nf_out.setMinimumFractionDigits(2);

        if (min == Double.MAX_VALUE || samples.size() == 0) {
            tableEntry.setMin("- " + unit);
        } else {
            tableEntry.setMin(nf_out.format(min) + " " + unit);
        }

        if (max == Double.MIN_VALUE || samples.size() == 0) {
            tableEntry.setMax("- " + unit);
        } else {
            tableEntry.setMax(nf_out.format(max) + " " + unit);
        }

        if (samples.size() == 0) {
            tableEntry.setAvg("- " + unit);
            tableEntry.setSum("- " + unit);
        } else {
            tableEntry.setAvg(nf_out.format(avg) + " " + unit);
            if (isQuantity) {
                tableEntry.setSum(nf_out.format(sum) + " " + unit);
            } else tableEntry.setSum("- " + unit);
        }
    }

    public Node generateNode(JEVisSample sample) throws JEVisException {
        Note note = new Note(sample.getNote());

        if (note.getNote() != null && hideShowIcons) {
            note.getNote().setVisible(true);
            return note.getNote();
        } else {
            Rectangle rect = new Rectangle(0, 0);
            rect.setFill(singleRow.getColor());
            rect.setVisible(false);
            return rect;
        }
    }


    public MultiAxisChart.Series getSerie() {
        return serie;
    }

    public TableEntry getTableEntry() {
        return tableEntry;
    }

    public DateTime getTimeStampFromFirstSample() {
        return this.timeStampFromFirstSample;
    }

    public void setTimeStampFromFirstSample(DateTime timeStampFromFirstSample) {
        this.timeStampFromFirstSample = timeStampFromFirstSample;
    }

    public DateTime getTimeStampFromLastSample() {
        return this.timeStampFromLastSample;
    }

    public void setTimeStampFromLastSample(DateTime timeStampFromLastSample) {
        this.timeStampFromLastSample = timeStampFromLastSample;
    }

    public ChartDataModel getSingleRow() {
        return singleRow;
    }

    public void setSingleRow(ChartDataModel singleRow) {
        this.singleRow = singleRow;
        this.yAxis = singleRow.getAxis();
    }

    public String getTableEntryName() {
        return singleRow.getObject().getName();
    }

    public String getUnit() {

        String unit = UnitManager.getInstance().format(singleRow.getUnit());

        if (unit.equals("")) unit = singleRow.getUnit().getLabel();
        if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

        return unit;
    }

    public TreeMap<Double, JEVisSample> getSampleMap() {
        return sampleMap;
    }
}
