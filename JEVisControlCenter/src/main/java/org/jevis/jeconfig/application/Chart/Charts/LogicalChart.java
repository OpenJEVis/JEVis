package org.jevis.jeconfig.application.Chart.Charts;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartElements.Note;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.XYLogicalChartSerie;
import org.jevis.jeconfig.application.Chart.LogicalYAxisStringConverter;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.NumberFormat;
import java.util.List;
import java.util.TreeMap;

public class LogicalChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(LogicalChart.class);

    public LogicalChart(AnalysisDataModel analysisDataModel, List<ChartDataModel> chartDataModels, Integer chartId, String chartName) {
        super(analysisDataModel, chartDataModels, chartId, chartName);
    }

    @Override
    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataModel singleRow) throws JEVisException {
        XYLogicalChartSerie serie = new XYLogicalChartSerie(singleRow, showIcons);
        setMinValue(Math.min(minValue, serie.getMinValue()));
        setMaxValue(Math.max(maxValue, serie.getMaxValue()));

        getHexColors().add(ColorHelper.toColor(singleRow.getColor()));

        /**
         * check if timestamps are in serie
         */

        if (serie.getTimeStampFromFirstSample().isBefore(timeStampOfFirstSample.get())) {
            timeStampOfFirstSample.set(serie.getTimeStampFromFirstSample());
            changedBoth[0] = true;
        }

        if (serie.getTimeStampFromLastSample().isAfter(timeStampOfLastSample.get())) {
            timeStampOfLastSample.set(serie.getTimeStampFromLastSample());
            changedBoth[1] = true;
        }

        /**
         * check if theres a manipulation for changing the x axis values into duration instead of concrete timestamps
         */

        checkManipulation(singleRow);
        return serie;
    }

    @Override
    public void addSeriesToChart() {
        for (XYChartSerie xyChartSerie : xyChartSerieList) {
            chart.getDatasets().add(xyChartSerie.getValueDataSet());
            tableData.add(xyChartSerie.getTableEntry());
        }
    }

    @Override
    public void applyColors() {

        for (int i = 0; i < getHexColors().size(); i++) {
            Color currentColor = getHexColors().get(i);
            String hexColor = ColorHelper.toRGBCode(currentColor);
            String preIdent = ".default-color" + i;
            Node node = getChart().lookup(preIdent + ".chart-series-area-fill");
            Node nodew = getChart().lookup(preIdent + ".chart-series-area-line");

            if (node != null) {
                node.setStyle("-fx-fill: " + hexColor + ";");
            }
            if (nodew != null) {
                nodew.setStyle("-fx-stroke: " + hexColor + "; -fx-stroke-width: 2px; ");
            }
        }
    }

    @Override
    public void generateYAxis() {
        super.generateYAxis();

//      y1Axis.setLowerBound(0d);
//      y1Axis.setUpperBound(1d);
        y1Axis.setTickUnit(1d);
        y1Axis.setMinorTickVisible(false);
        y1Axis.setTickLabelFormatter(new LogicalYAxisStringConverter());
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay) {
        Point2D mouseCoordinates = null;
        if (mouseEvent != null) mouseCoordinates = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        Double x = null;
        if (valueForDisplay == null) {

//            x = ((MultiAxisChart) getChart()).getXAxis().sceneToLocal(Objects.requireNonNull(mouseCoordinates)).getX();
//
//            valueForDisplay = ((DateAxis) ((MultiAxisChart) getChart()).getXAxis()).getDateTimeForDisplay(x);

        }
        if (valueForDisplay != null) {
            setValueForDisplay(valueForDisplay);
            DateTime finalValueForDisplay = valueForDisplay;
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);

            xyChartSerieList.parallelStream().forEach(serie -> {
                try {

                    TableEntry tableEntry = serie.getTableEntry();
                    TreeMap<DateTime, JEVisSample> sampleTreeMap = serie.getSampleMap();

                    DateTime nearest = sampleTreeMap.lowerKey(finalValueForDisplay);

                    JEVisSample sample = sampleTreeMap.get(nearest);
                    Double valueAsDouble = sample.getValueAsDouble();
                    Note formattedNote = new Note(sample, serie.getSingleRow().getNoteSamples().get(sample.getTimestamp()));
                    String formattedDouble = nf.format(valueAsDouble);

                    if (!asDuration) {
                        Platform.runLater(() -> tableEntry.setDate(nearest
                                .toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss"))));
                    } else {
                        Platform.runLater(() -> tableEntry.setDate((nearest.getMillis() -
                                timeStampOfFirstSample.get().getMillis()) / 1000 / 60 / 60 + " h"));
                    }
                    Platform.runLater(() -> tableEntry.setNote(formattedNote.getNoteAsString()));
                    String unit = serie.getUnit();

                    if (!sample.getNote().contains("Zeros")) {
                        Platform.runLater(() -> tableEntry.setValue(formattedDouble + " " + unit));
                    } else Platform.runLater(() -> tableEntry.setValue("- " + unit));

                } catch (Exception ex) {
                }

            });
        }
    }
}
