package org.jevis.jeconfig.application.Chart.ChartElements;


import javafx.application.Platform;
import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TableSerie extends XYChartSerie {
    private static final Logger logger = LogManager.getLogger(TableSerie.class);

    public TableSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        super(singleRow, hideShowIcons);
    }

    public void generateSeriesFromSamples() throws JEVisException {
        timeStampFromFirstSample = DateTime.now();
        timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);
        tableEntry = new TableEntry(getTableEntryName());
        this.serie.setName(getTableEntryName());

        List<JEVisSample> samples = singleRow.getSamples();

        serie.getData().clear();

        int samplesSize = samples.size();

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
        List<MultiAxisChart.Data<Number, Number>> dataList = new ArrayList<>();
        for (JEVisSample sample : samples) {
            try {

                DateTime dateTime = sample.getTimestamp();
                Long timestamp = dateTime.getMillis();

                MultiAxisChart.Data<Number, Number> data = new MultiAxisChart.Data<>();
                data.setXValue(timestamp);
                data.setYValue(0);
                data.setExtraValue(yAxis);

                data.setNode(null);

                dataList.add(data);

                sampleMap.put(dateTime, sample);

            } catch (JEVisException e) {

            }
        }

        Platform.runLater(() -> {
            serie.getData().setAll(dataList);
            JEConfig.getStatusBar().progressProgressJob(GraphPluginView.JOB_NAME, 1, "Finished Serie");
        });

    }

    public void setDataNodeColor(MultiAxisChart.Data<Number, Number> data) {
    }

    public Node generateNode(JEVisSample sample) throws JEVisException {
        return null;
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
        return super.getUnit();
    }

    public TreeMap<DateTime, JEVisSample> getSampleMap() {
        return sampleMap;
    }
}
