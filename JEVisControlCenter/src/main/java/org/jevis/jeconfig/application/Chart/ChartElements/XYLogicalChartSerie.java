package org.jevis.jeconfig.application.Chart.ChartElements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.XYChart;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.joda.time.DateTime;
import tech.units.indriya.AbstractUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class XYLogicalChartSerie extends XYChartSerie {
    private static final Logger logger = LogManager.getLogger(XYLogicalChartSerie.class);

    public XYLogicalChartSerie(ChartModel chartModelSetting, ChartDataRow singleRow, Boolean hideShowIcons) throws JEVisException {
        super(chartModelSetting, singleRow, hideShowIcons, false);
    }

    @Override
    public void generateSeriesFromSamples() throws JEVisException {

        this.tableEntry = new TableEntry(getTableEntryName());
        this.valueDataSet.setName(getTableEntryName());
        this.valueDataSet.setStyle("strokeColor=" + ColorHelper.toRGBCode(singleRow.getColor()) + "; fillColor= " + ColorHelper.toRGBCode(singleRow.getColor()) + ";strokeDashPattern=0");

        this.tableEntry.setColor(singleRow.getColor());

        List<JEVisSample> samples = singleRow.getSamples();
        List<JEVisSample> modifiedList = getModifiedList(samples);

        this.valueDataSet.clearData();

        if (modifiedList.size() > 0) {
            try {

                if (modifiedList.get(0).getTimestamp().isBefore(getTimeStampOfFirstSample()))
                    setTimeStampOfFirstSample(modifiedList.get(0).getTimestamp());

                if (modifiedList.get(modifiedList.size() - 1).getTimestamp().isAfter(getTimeStampOfLastSample()))
                    setTimeStampOfLastSample(modifiedList.get(modifiedList.size() - 1).getTimestamp());

            } catch (Exception e) {
                logger.error("Couldn't get timestamps from samples. ", e);
            }
        }

        sampleMap = new TreeMap<>();
        int noteIndex = 0;
        for (JEVisSample sample : modifiedList) {
            try {
//                int index = samples.indexOf(sample);

                DateTime dateTime = sample.getTimestamp();
                Double value = sample.getValueAsDouble();

                minValue.minCheck(dateTime, value);
                maxValue.maxCheck(dateTime, value);

                long timestamp = dateTime.getMillis();

                sampleMap.put(sample.getTimestamp(), sample);
                valueDataSet.add(timestamp / 1000d, value);

                String noteString = generateNote(sample);
                if (noteString != null && showIcons) {
                    noteDataSet.add(timestamp, value);
                    noteDataSet.addDataLabel(noteIndex, noteString);
                    noteDataSet.addDataStyle(noteIndex, "strokeColor=" + ColorHelper.toRGBCode(singleRow.getColor()) + "; fillColor= " + ColorHelper.toRGBCode(singleRow.getColor()) + ";strokeDashPattern=0");
                    noteIndex++;
                }

            } catch (JEVisException e) {

            }
        }

        updateTableEntry(modifiedList, new JEVisUnitImp(AbstractUnit.ONE), getMinValue(), getMaxValue(), 0, false);

        JEConfig.getStatusBar().progressProgressJob(XYChart.JOB_NAME, 1, FINISHED_SERIE);
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
                            JEVisSample virtualSample = new VirtualSample(start, smp.getValueAsDouble());
                            virtualSample.setNote("auto");
                            modifiedList.add(virtualSample);
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
                    if (newSample != null) {
                        newSample.setNote("auto");
                        modifiedList.add(newSample);
                    }
                }
                if (smp.getNote() == null || smp.getNote().equals("")) {
                    smp.setNote("auto");
                }
                modifiedList.add(smp);
                lastValue = currentValue;
                lastProcessedSample = smp;
            }

            if (lastProcessedSample != null && !lastProcessedSample.getTimestamp().equals(end)) {
                JEVisSample virtualSample = new VirtualSample(end, lastValue);
                virtualSample.setNote("auto");
                modifiedList.add(virtualSample);
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

            JEVisSample virtualSampleStart = new VirtualSample(start, lastSample.getValueAsDouble());
            modifiedList.add(virtualSampleStart);
            JEVisSample virtualSampleEnd = new VirtualSample(end, lastSample.getValueAsDouble());
            modifiedList.add(virtualSampleEnd);

        }
        return modifiedList;
    }
}
