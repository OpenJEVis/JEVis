package org.jevis.jeconfig.application.Chart.ChartElements;

import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class XYLogicalChartSerie extends XYChartSerie {
    private static final Logger logger = LogManager.getLogger(XYLogicalChartSerie.class);

    public XYLogicalChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        super(singleRow, hideShowIcons);
    }

    @Override
    public void generateSeriesFromSamples() throws JEVisException {
        setMinValue(Double.MAX_VALUE);
        setMaxValue(-Double.MAX_VALUE);

        tableEntry = new TableEntry(getTableEntryName());
        this.serie.setName(getTableEntryName());

        tableEntry.setColor(singleRow.getColor());

        List<JEVisSample> samples = singleRow.getSamples();
        List<JEVisSample> modifiedList = getModifiedList(samples);

        serie.getData().clear();

//        int samplesSize = samples.size();
//        int seriesDataSize = serie.getData().size();
//
//        if (samplesSize < seriesDataSize) {
//            serie.getData().subList(samplesSize, seriesDataSize).clear();
//        } else if (samplesSize > seriesDataSize) {
//            for (int i = seriesDataSize; i < samplesSize; i++) {
//                serie.getData().add(new MultiAxisChart.Data<>());
//            }
//        }

        if (modifiedList.size() > 0) {
            try {

                if (modifiedList.get(0).getTimestamp().isBefore(getTimeStampFromFirstSample()))
                    setTimeStampFromFirstSample(modifiedList.get(0).getTimestamp());

                if (modifiedList.get(modifiedList.size() - 1).getTimestamp().isAfter(getTimeStampFromLastSample()))
                    setTimeStampFromLastSample(modifiedList.get(modifiedList.size() - 1).getTimestamp());

            } catch (Exception e) {
                logger.error("Couldn't get timestamps from samples. " + e);
            }
        }

        sampleMap = new TreeMap<>();
        for (JEVisSample sample : modifiedList) {
            try {
//                int index = samples.indexOf(sample);

                DateTime dateTime = sample.getTimestamp();
                Double value = sample.getValueAsDouble();

                setMinValue(Math.min(minValue, value));
                setMaxValue(Math.max(maxValue, value));

                Long timestamp = dateTime.getMillis();

//                MultiAxisChart.Data<Number, Number> data = serie.getData().get(index);
                MultiAxisChart.Data<Number, Number> data = new MultiAxisChart.Data<>(timestamp, value);
                data.setXValue(timestamp);
                data.setYValue(value);
                data.setExtraValue(yAxis);
                data.setExtraValue(yAxis);

                data.setNode(null);
                Note note = new Note(sample);

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
                serie.getData().add(data);

            } catch (JEVisException e) {

            }
        }

    }

    private List<JEVisSample> getModifiedList(List<JEVisSample> samples) throws JEVisException {
        List<JEVisSample> modifiedList = new ArrayList<>();
        JEVisSample lastSample = singleRow.getAttribute().getLatestSample();
        List<JEVisSample> allSamples = singleRow.getAttribute().getAllSamples();
        DateTime start = singleRow.getSelectedStart();
        DateTime end = singleRow.getSelectedEnd();
        Double lastValue = null;

        if (!samples.isEmpty()) {
            JEVisSample checkStart = samples.get(0);
            if (checkStart != null) {
                for (int i = allSamples.size() - 1; i > -1; i--) {
                    JEVisSample smp = allSamples.get(i);
                    if (smp.getTimestamp().isBefore(start)) {
                        if (!start.equals(checkStart.getTimestamp())) {
                            modifiedList.add(new VirtualSample(start, smp.getValueAsDouble()));
                            lastValue = smp.getValueAsDouble();
                        }
                        break;
                    }
                }
            }

            JEVisSample lastProcessedSample = null;
            for (JEVisSample smp : samples) {
                Double currentValue = smp.getValueAsDouble();
                DateTime currentTimeStamp = smp.getTimestamp();

                if (!currentValue.equals(lastValue)) {
                    JEVisSample newSample = null;
                    if (lastValue != null) {
                        newSample = new VirtualSample(currentTimeStamp.minus(1), lastValue);
                    } else {
                        for (int i1 = allSamples.size() - 1; i1 > -1; i1--) {
                            JEVisSample sample = allSamples.get(i1);
                            if (sample.getTimestamp().isBefore(currentTimeStamp)) {
                                newSample = new VirtualSample(currentTimeStamp.minus(1), sample.getValueAsDouble());
                            }
                        }
                    }
                    modifiedList.add(newSample);
                }
                modifiedList.add(smp);
                lastValue = currentValue;
                lastProcessedSample = smp;
            }

            if (lastProcessedSample != null && !lastProcessedSample.getTimestamp().equals(end)) {
                modifiedList.add(new VirtualSample(end, lastValue));
            }

//            JEVisSample checkEnd = samples.get(samples.size() - 1);
//            if (checkStart != null && checkEnd != checkStart) {
//                for (int i = allSamples.size() - 1; i > -1; i--) {
//                    JEVisSample smp = allSamples.get(i);
//                    if (smp.getTimestamp().isBefore(end)) {
//                        modifiedList.add(new VirtualSample(end, smp.getValueAsDouble()));
//                        break;
//                    }
//                }
//            }
        } else {

            modifiedList.add(new VirtualSample(start, lastSample.getValueAsDouble()));
            modifiedList.add(new VirtualSample(end, lastSample.getValueAsDouble()));

        }
        return modifiedList;
    }
}
