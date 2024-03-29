package org.jevis.jeconfig.application.Chart.ChartElements;


import de.gsi.dataset.spi.DoubleDataSet;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.XYChart;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.joda.time.DateTime;

import java.util.List;
import java.util.TreeMap;

public class TableSerie extends XYChartSerie {
    private static final Logger logger = LogManager.getLogger(TableSerie.class);

    public TableSerie(ChartModel chartModelSetting, ChartDataRow singleRow, Boolean hideShowIcons) throws JEVisException {
        super(chartModelSetting, singleRow, hideShowIcons, false);
    }

    public void generateSeriesFromSamples() throws JEVisException {
        timeStampOfFirstSample = DateTime.now();
        timeStampOfLastSample = new DateTime(1990, 1, 1, 0, 0, 0);
        tableEntry = new TableEntry(getTableEntryName());
        this.valueDataSet.setName(getTableEntryName());

        List<JEVisSample> samples = singleRow.getSamples();

        valueDataSet.clearData();

        int samplesSize = samples.size();

        if (samplesSize > 0) {
            try {

                if (samples.get(0).getTimestamp().isBefore(getTimeStampOfFirstSample()))
                    setTimeStampOfFirstSample(samples.get(0).getTimestamp());

                if (samples.get(samples.size() - 1).getTimestamp().isAfter(getTimeStampOfLastSample()))
                    setTimeStampOfLastSample(samples.get(samples.size() - 1).getTimestamp());

            } catch (Exception e) {
                logger.error("Couldn't get timestamps from samples. " + e);
            }
        }

        sampleMap = new TreeMap<>();

        for (JEVisSample sample : samples) {
            try {

                DateTime dateTime = sample.getTimestamp();

                sampleMap.put(dateTime, sample);

            } catch (JEVisException e) {

            }
        }

        Platform.runLater(() -> {
            JEConfig.getStatusBar().progressProgressJob(XYChart.JOB_NAME, 1, FINISHED_SERIE);
        });

    }

    public String generateNote(JEVisSample sample) throws JEVisException {
        return null;
    }


    public DoubleDataSet getValueDataSet() {
        return valueDataSet;
    }

    public TableEntry getTableEntry() {
        return tableEntry;
    }

    public DateTime getTimeStampOfFirstSample() {
        return this.timeStampOfFirstSample;
    }

    public void setTimeStampOfFirstSample(DateTime timeStampOfFirstSample) {
        this.timeStampOfFirstSample = timeStampOfFirstSample;
    }

    public DateTime getTimeStampOfLastSample() {
        return this.timeStampOfLastSample;
    }

    public void setTimeStampOfLastSample(DateTime timeStampOfLastSample) {
        this.timeStampOfLastSample = timeStampOfLastSample;
    }

    public ChartDataRow getSingleRow() {
        return singleRow;
    }

    public void setSingleRow(ChartDataRow singleRow) {
        this.singleRow = singleRow;
        this.yAxis = singleRow.getAxis();
    }

    public String getUnit() {
        return super.getUnit();
    }

    public TreeMap<DateTime, JEVisSample> getSampleMap() {
        return sampleMap;
    }
}
