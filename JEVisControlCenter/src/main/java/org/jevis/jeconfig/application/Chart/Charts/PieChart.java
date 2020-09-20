package org.jevis.jeconfig.application.Chart.Charts;

import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class PieChart implements Chart {
    private static final Logger logger = LogManager.getLogger(PieChart.class);
    private final Integer chartId;
    private final Boolean showRawData;
    private final Boolean showSum;
    private String chartName;
    private String unit;
    private final AnalysisDataModel analysisDataModel;
    private final List<ChartDataRow> chartDataRows;
    private final Boolean hideShowIcons;
    private List<javafx.scene.chart.PieChart.Data> series = new ArrayList<>();
    private PieChartExtended pieChart;
    private final List<Color> hexColors = new ArrayList<>();
    private DateTime valueForDisplay;
    private final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Region pieChartRegion;
    private Period period;
    private final ChartType chartType = ChartType.PIE;
    private boolean legendMode = false;

    private final ChartSettingsFunction chartSettingsFunction = new ChartSettingsFunction() {
        @Override
        public void applySetting(javafx.scene.chart.Chart chart) {

        }
    };
    private final List<String> seriesNames = new ArrayList<>();

    public PieChart(AnalysisDataModel analysisDataModel, List<ChartDataRow> chartDataRows, ChartSetting chartSetting) {
        this.analysisDataModel = analysisDataModel;
        this.chartDataRows = chartDataRows;
        this.showRawData = analysisDataModel.getShowRawData();
        this.showSum = analysisDataModel.getShowSum();
        this.hideShowIcons = analysisDataModel.getShowIcons();
        this.chartName = chartSetting.getName();
        this.chartId = chartSetting.getId();

        double totalJob = chartDataRows.size();

        if (showRawData) {
            totalJob *= 4;
        }

        if (showSum) {
            totalJob += 1;
        }

        JEConfig.getStatusBar().startProgressJob(XYChart.JOB_NAME, totalJob, I18n.getInstance().getString("plugin.graph.message.startupdate"));

        init();
    }

    private void init() {
        List<Double> listSumsPiePieces = new ArrayList<>();
        List<String> listTableEntryNames = new ArrayList<>();

        if (chartDataRows != null && chartDataRows.size() > 0) {
            unit = UnitManager.getInstance().format(chartDataRows.get(0).getUnit());
            if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");
            period = chartDataRows.get(0).getAttribute().getDisplaySampleRate();
        }

        hexColors.clear();
        if (chartDataRows != null) {
            for (ChartDataRow singleRow : chartDataRows) {
                if (!singleRow.getSelectedcharts().isEmpty()) {
                    ChartDataRow clonedModel = singleRow.clone();
                    clonedModel.setAggregationPeriod(AggregationPeriod.NONE);
                    Double sumPiePiece = 0d;
                    QuantityUnits qu = new QuantityUnits();
                    boolean isQuantity = qu.isQuantityUnit(clonedModel.getUnit());
                    boolean isSummable = qu.isSumCalculable(clonedModel.getUnit());

                    List<JEVisSample> samples = new ArrayList<>();
                    if (singleRow.hasForecastData()) {
                        samples = clonedModel.getForecastSamples();
                    } else {
                        samples = clonedModel.getSamples();
                    }
                    if (!isQuantity && isSummable) {
                        List<JEVisSample> scaledSamples = new ArrayList<>();

                        JEVisUnit sumUnit = qu.getSumUnit(clonedModel.getUnit());
                        String outputUnit = UnitManager.getInstance().format(sumUnit).replace("·", "");
                        if (outputUnit.equals("")) outputUnit = sumUnit.getLabel();

                        String inputUnit = UnitManager.getInstance().format(clonedModel.getUnit()).replace("·", "");
                        if (inputUnit.equals("")) inputUnit = clonedModel.getUnit().getLabel();

                        ChartUnits cu = new ChartUnits();
                        Double finalFactor = cu.scaleValue(inputUnit, outputUnit);
                        samples.forEach(sample -> {
                            try {
                                JEVisSample smp = new VirtualSample(sample.getTimestamp(), sample.getValueAsDouble() * finalFactor);
                                smp.setNote(sample.getNote());
                                scaledSamples.add(smp);
                            } catch (Exception e) {
                                try {
                                    logger.error("Error in sample: " + sample.getTimestamp() + " : " + sample.getValue());
                                } catch (Exception e1) {
                                    logger.fatal(e1);
                                }
                            }
                        });

                        samples = scaledSamples;
                    }

                    int samplecount = samples.size();
                    for (JEVisSample sample : samples) {
                        try {
                            sumPiePiece += sample.getValueAsDouble();
                        } catch (JEVisException e) {
                            logger.error(e);
                        }
                    }

                    if (!isQuantity && !isSummable) {
                        sumPiePiece = sumPiePiece / samplecount;
                    }

                    listSumsPiePieces.add(sumPiePiece);
                    if (!listTableEntryNames.contains(clonedModel.getObject().getName()) && !singleRow.hasForecastData()) {
                        listTableEntryNames.add(clonedModel.getObject().getName());
                    } else if (!listTableEntryNames.contains(clonedModel.getObject().getName()) && singleRow.hasForecastData()) {
                        listTableEntryNames.add(clonedModel.getObject().getName() + " - " + I18n.getInstance().getString("plugin.graph.chart.forecast.title"));
                    } else {
                        listTableEntryNames.add(clonedModel.getObject().getName() + " " + chartDataRows.indexOf(singleRow));
                    }
                    if (!singleRow.hasForecastData()) {
                        hexColors.add(ColorHelper.toColor(clonedModel.getColor()));
                    } else {
                        hexColors.add(ColorHelper.toColor(ColorHelper.colorToBrighter(singleRow.getColor())));
                    }
                }
            }
        }

        Double whole = 0d;
        List<Double> listPercentages = new ArrayList<>();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        for (Double d : listSumsPiePieces) whole += d;
        for (Double d : listSumsPiePieces) {
            if (d > 0) {
                listPercentages.add(d / whole);
            } else {
                listPercentages.add(0d);
            }
        }

        series = new ArrayList<>();
        seriesNames.clear();
        for (String name : listTableEntryNames) {
            QuantityUnits qu = new QuantityUnits();
            JEVisUnit currentUnit = chartDataRows.get(listTableEntryNames.indexOf(name)).getUnit();
            String currentUnitString = "";
            if (qu.isQuantityUnit(currentUnit)) currentUnitString = getUnit(currentUnit);
            else currentUnitString = getUnit(qu.getSumUnit(currentUnit));
//            String seriesName = name + " - " + nf.format(listSumsPiePieces.get(listTableEntryNames.indexOf(name)))
            String seriesName = nf.format(listSumsPiePieces.get(listTableEntryNames.indexOf(name)))
                    + " " + currentUnitString
                    + " (" + nf.format(listPercentages.get(listTableEntryNames.indexOf(name)) * 100) + "%)";

            javafx.scene.chart.PieChart.Data data = new javafx.scene.chart.PieChart.Data(seriesName, listSumsPiePieces.get(listTableEntryNames.indexOf(name)));
            series.add(data);
            seriesNames.add(name);

            JEConfig.getStatusBar().progressProgressJob(XYChart.JOB_NAME, 1, I18n.getInstance().getString("graph.progress.finishedserie") + " " + name);
        }

        if (pieChart == null) {
            pieChart = new PieChartExtended(FXCollections.observableArrayList(series));
        } else pieChart.setData(FXCollections.observableArrayList(series));

        pieChart.setTitle(chartName);
        pieChart.setLegendVisible(false);
        pieChart.applyCss();

        pieChart.setTitle(chartName);
        pieChart.setLegendVisible(true);
        pieChart.setLegendSide(Side.BOTTOM);
        pieChart.setLabelsVisible(true);

        applyColors();

        chartSettingsFunction.applySetting(pieChart);

    }

    public void addToolTipText() {
        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 24 arial;");

        for (final javafx.scene.chart.PieChart.Data data : series) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED, //--> is null weil noch nicht da
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent e) {
                            caption.setTranslateX(e.getSceneX());
                            caption.setTranslateY(e.getSceneY());
                            caption.setText(data.getPieValue() + "%");
                        }
                    });
        }
    }

    @Override
    public void setRegion(Region region) {
        pieChartRegion = region;
    }

    @Override
    public List<ChartDataRow> getChartDataRows() {
        return chartDataRows;
    }

    @Override
    public String getChartName() {
        return chartName;
    }

    @Override
    public void setTitle(String s) {
        chartName = s;
    }

    @Override
    public Integer getChartId() {
        return null;
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay) {

    }

    @Override
    public void updateTableZoom(double lowerBound, double upperBound) {
        Double lb = lowerBound * 1000d;
        Double ub = upperBound * 1000d;
        DateTime start = new DateTime(lb.longValue());
        DateTime end = new DateTime(ub.longValue());
        if (chartDataRows != null) {
            List<Double> listSumsPiePieces = new ArrayList<>();
            List<String> listTableEntryNames = new ArrayList<>();

            for (ChartDataRow singleRow : chartDataRows) {
                if (!singleRow.getSelectedcharts().isEmpty()) {
                    singleRow.setSelectedStart(start);
                    singleRow.setSelectedEnd(end);
                    singleRow.setSomethingChanged(true);

                    Double sumPiePiece = 0d;
                    QuantityUnits qu = new QuantityUnits();
                    boolean isQuantity = qu.isQuantityUnit(singleRow.getUnit());
                    boolean isSummable = qu.isSumCalculable(singleRow.getUnit());

                    List<JEVisSample> samples = singleRow.getSamples();
                    if (!isQuantity && isSummable) {

                        JEVisUnit sumUnit = qu.getSumUnit(singleRow.getUnit());
                        String outputUnit = UnitManager.getInstance().format(sumUnit).replace("·", "");
                        if (outputUnit.equals("")) outputUnit = sumUnit.getLabel();

                        String inputUnit = UnitManager.getInstance().format(singleRow.getUnit()).replace("·", "");
                        if (inputUnit.equals("")) inputUnit = singleRow.getUnit().getLabel();

                        ChartUnits cu = new ChartUnits();
                        Double finalFactor = cu.scaleValue(inputUnit, outputUnit);
                        samples.forEach(sample -> {
                            try {
                                sample.setValue(sample.getValueAsDouble() * finalFactor);
                            } catch (Exception e) {
                                try {
                                    logger.error("Error in sample: " + sample.getTimestamp() + " : " + sample.getValue());
                                } catch (Exception e1) {
                                    logger.fatal(e1);
                                }
                            }
                        });
                    }

                    int samplecount = samples.size();
                    for (JEVisSample sample : samples) {
                        try {
                            sumPiePiece += sample.getValueAsDouble();
                        } catch (JEVisException e) {
                            logger.error(e);
                        }
                    }

                    if (!isQuantity && !isSummable) {
                        sumPiePiece = sumPiePiece / samplecount;
                    }

                    listSumsPiePieces.add(sumPiePiece);
                    if (!listTableEntryNames.contains(singleRow.getObject().getName())) {
                        listTableEntryNames.add(singleRow.getObject().getName());
                    } else {
                        listTableEntryNames.add(singleRow.getObject().getName() + " " + chartDataRows.indexOf(singleRow));
                    }
                }

                Double whole = 0d;
                List<Double> listPercentages = new ArrayList<>();
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(2);
                for (Double d : listSumsPiePieces) whole += d;
                for (Double d : listSumsPiePieces) listPercentages.add(d / whole);

                for (String name : listTableEntryNames) {
                    QuantityUnits qu = new QuantityUnits();
                    JEVisUnit currentUnit = chartDataRows.get(listTableEntryNames.indexOf(name)).getUnit();
                    String currentUnitString = "";
                    if (qu.isQuantityUnit(currentUnit)) currentUnitString = getUnit(currentUnit);
                    else currentUnitString = getUnit(qu.getSumUnit(currentUnit));

                    String seriesName = nf.format(listSumsPiePieces.get(listTableEntryNames.indexOf(name)))
                            + " " + currentUnitString
                            + " (" + nf.format(listPercentages.get(listTableEntryNames.indexOf(name)) * 100) + " %)";

                    Platform.runLater(() -> {
                        pieChart.getData().get(listTableEntryNames.indexOf(name)).setName(seriesName);
                        if (listTableEntryNames.indexOf(name) == listTableEntryNames.size() - 1) {
                            makeCustomLegend();
                        }
                    });
                    Platform.runLater(() -> pieChart.getData().get(listTableEntryNames.indexOf(name)).setPieValue(listSumsPiePieces.get(listTableEntryNames.indexOf(name))));
                }
            }
        }

    }

    @Override
    public void applyColors() {

        for (int i = 0; i < hexColors.size(); i++) {

            Color currentColor = hexColors.get(i);
            String hexColor = ColorHelper.toRGBCode(currentColor);
            String preIdent = ".default-color" + i;
            Node node = pieChart.lookup(preIdent + ".chart-pie");

            if (node != null) {
                node.setStyle("-fx-pie-color: " + hexColor + ";");
            }
        }

        makeCustomLegend();
    }

    public void makeCustomLegend() {
        ObservableList<Legend.LegendItem> items = pieChart.getPieLegend().getItems();
        for (int j = 0; j < items.size(); j++) {
            Legend.LegendItem legendItem = items.get(j);
            int finalI = j;
            Platform.runLater(() -> {
                legendItem.setSymbol(new Rectangle(8, 8, hexColors.get(finalI)));
                legendItem.setText(seriesNames.get(finalI));
            });
        }

        AlphanumComparator ac = new AlphanumComparator();
        Platform.runLater(() -> items.sort((o1, o2) -> ac.compare(o1.getText(), o2.getText())));
    }


    @Override
    public ObservableList<TableEntry> getTableData() {
        return tableData;
    }

    @Override
    public Period getPeriod() {
        return period;
    }

    @Override
    public void setPeriod(Period period) {
        this.period = period;
    }

    @Override
    public de.gsi.chart.Chart getChart() {
        return null;
    }

    @Override
    public ChartType getChartType() {
        return chartType;
    }

    @Override
    public Region getRegion() {
        return pieChart;
    }

    public String getUnit(JEVisUnit jeVisUnit) {

        String unit = "";
        if (jeVisUnit != null) {
            unit = UnitManager.getInstance().format(jeVisUnit);
            if (unit.equals("")) unit = jeVisUnit.getLabel();
        }

        if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

        return unit;
    }

    public void setLegendMode(boolean enable) {
        legendMode = enable;
    }
}
