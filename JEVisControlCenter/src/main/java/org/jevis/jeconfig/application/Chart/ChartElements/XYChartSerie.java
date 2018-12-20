package org.jevis.jeconfig.application.Chart.ChartElements;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.List;
import java.util.TreeMap;

public class XYChartSerie implements Serie {
    private static final Logger logger = LogManager.getLogger(XYChartSerie.class);
    private ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
    private XYChart.Series<Number, Number> serie;
    private TableEntry tableEntry;
    private DateTime timeStampFromFirstSample = DateTime.now();
    private DateTime timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);
    private ChartDataModel singleRow;
    private Boolean hideShowIcons;
    private List<JEVisSample> samples;
    private TreeMap<Double, JEVisSample> sampleMap;

    public XYChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        this.singleRow = singleRow;
        this.hideShowIcons = hideShowIcons;
        this.serie = new XYChart.Series<>(getTableEntryName(), seriesData);

//        boolean isQuantitiy = false;

//        JEVisObject dp = singleRow.getDataProcessor();
//        if (Objects.nonNull(dp)) {
//            try {
//                if (Objects.nonNull(dp.getAttribute("Value is a Quantity"))) {
//                    if (Objects.nonNull(dp.getAttribute("Value is a Quantity").getLatestSample())) {
//                        if (dp.getAttribute("Value is a Quantity").getLatestSample().getValueAsBoolean()) {
//
//                            isQuantitiy = true;
//                        }
//                    }
//                }
//            } catch (JEVisException e) {
//                logger.error("Error: could not data processor attribute", e);
//            }
//        }

//        QuantityUnits qu = new QuantityUnits();
//        if (qu.get().contains(singleRow.getUnit())) isQuantitiy = true;


        generateSeriesFromSamples();
    }

    public void generateSeriesFromSamples() throws JEVisException {
        tableEntry = new TableEntry(getTableEntryName());

        if (!singleRow.getManipulationMode().equals(ManipulationMode.NONE)) tableEntry.setColor(singleRow.getColor());
        else tableEntry.setColor(singleRow.getColor().darker());

        samples = singleRow.getSamples();

        seriesData.clear();
        if (samples.size() > 0) {
            try {

                if (samples.get(0).getTimestamp().isBefore(getTimeStampFromFirstSample()))
                    setTimeStampFromFirstSample(samples.get(0).getTimestamp());

                if (samples.get(samples.size() - 1).getTimestamp().isAfter(getTimeStampFromLastSample()))
                    setTimeStampFromLastSample(samples.get(samples.size() - 1).getTimestamp());

            } catch (Exception e) {
                logger.error("Couldn't get timestamps from samples. " + e);
            }
        }

        sampleMap = new TreeMap<Double, JEVisSample>();
        for (JEVisSample sample : samples) {
            try {
                DateTime dateTime = sample.getTimestamp();
                Double value = sample.getValueAsDouble();
                Long timestamp = dateTime.getMillis();

                XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(timestamp, value);

                Note note = new Note(sample.getNote());

                if (note.getNote() != null && hideShowIcons) {
                    note.getNote().setVisible(true);
                    data.setNode(note.getNote());
                } else {
                    Rectangle rect = new Rectangle(0, 0);
                    rect.setFill(singleRow.getColor());
                    rect.setVisible(false);
                    data.setNode(rect);
                }


                sampleMap.put((double) sample.getTimestamp().getMillis(), sample);
                seriesData.add(data);

            } catch (JEVisException e) {

            }
        }

//        if (isQuantitiy) {
        calcTableValues(tableEntry, samples, getUnit());
//        }
    }


    public XYChart.Series getSerie() {
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
    }

    public String getTableEntryName() {
        String tableEntryName = singleRow.getObject().getName();

//        if (!singleRow.getManipulationMode().equals(ManipulationMode.NONE))
//            tableEntryName += " (" + singleRow.getManipulationMode().toString() + ")";

        return tableEntryName;
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
