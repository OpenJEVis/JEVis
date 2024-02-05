package org.jevis.jecc.dialog;

import com.ibm.icu.text.NumberFormat;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.Chart.Charts.Chart;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.jevis.jecc.plugin.charts.BaseLoad;
import org.jevis.jecc.plugin.charts.BaseLoadSetting;
import org.jevis.jecc.plugin.charts.BaseLoadTable;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseLoadDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(BaseLoadDialog.class);
    final NumberFormat nf = NumberFormat.getNumberInstance();

    public BaseLoadDialog(JEVisDataSource ds, BaseLoadSetting settings, HashMap<Integer, Chart> model) {
        setTitle(I18n.getInstance().getString("plugin.graph.baseloaddialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.graph.baseloaddialog.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        this.nf.setMinimumFractionDigits(model.values().stream().findFirst().get().getChartModel().getMinFractionDigits());
        this.nf.setMaximumFractionDigits(model.values().stream().findFirst().get().getChartModel().getMaxFractionDigits());

        List<BaseLoad> data = new ArrayList<>();
        int repeatType = settings.getRepeatType();

        for (Map.Entry<Integer, Chart> entry : model.entrySet()) {
            Integer integer = entry.getKey();
            Chart chart = entry.getValue();
            for (ChartDataRow chartDataRow : chart.getChartDataRows()) {
                BaseLoad baseLoad = new BaseLoad(chartDataRow.getName());

                JEVisAttribute attribute = chartDataRow.getAttribute();
                List<JEVisSample> baseLoadSamples = new ArrayList<>();

                double baseLoadSum = 0.0;
                if (repeatType == 0) {
                    baseLoadSamples.addAll(attribute.getSamples(settings.getBaseLoadStart(), settings.getBaseLoadEnd()));
                    for (JEVisSample jeVisSample : baseLoadSamples) {
                        try {
                            baseLoadSum += jeVisSample.getValueAsDouble();
                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                } else {
                    List<JEVisSample> baseSamples = attribute.getSamples(settings.getResultStart(), settings.getResultEnd());

                    for (JEVisSample jeVisSample : baseSamples) {
                        try {
                            DateTime ts = jeVisSample.getTimestamp();
                            switch (repeatType) {
                                case 1:
                                    //day
                                    if (ts.getHourOfDay() >= settings.getBaseLoadStart().getHourOfDay() && ts.getMinuteOfHour() >= settings.getBaseLoadStart().getMinuteOfHour() && ts.getSecondOfMinute() >= settings.getBaseLoadStart().getSecondOfMinute()
                                            && ts.getHourOfDay() <= settings.getBaseLoadEnd().getHourOfDay() && ts.getMinuteOfHour() <= settings.getBaseLoadEnd().getMinuteOfHour() && ts.getSecondOfMinute() <= settings.getBaseLoadEnd().getSecondOfMinute()) {
                                        baseLoadSum += jeVisSample.getValueAsDouble();
                                        baseLoadSamples.add(jeVisSample);
                                    }
                                    break;
                                case 2:
                                    //week
                                    if (ts.getDayOfWeek() >= settings.getBaseLoadStart().getDayOfWeek() && ts.getHourOfDay() >= settings.getBaseLoadStart().getHourOfDay() && ts.getMinuteOfHour() >= settings.getBaseLoadStart().getMinuteOfHour() && ts.getSecondOfMinute() >= settings.getBaseLoadStart().getSecondOfMinute()
                                            && ts.getDayOfWeek() <= settings.getBaseLoadEnd().getDayOfWeek() && ts.getHourOfDay() <= settings.getBaseLoadEnd().getHourOfDay() && ts.getMinuteOfHour() <= settings.getBaseLoadEnd().getMinuteOfHour() && ts.getSecondOfMinute() <= settings.getBaseLoadEnd().getSecondOfMinute()) {
                                        baseLoadSum += jeVisSample.getValueAsDouble();
                                        baseLoadSamples.add(jeVisSample);
                                    }
                                    break;
                                case 3:
                                    //month
                                    if (ts.getDayOfMonth() >= settings.getBaseLoadStart().getDayOfMonth() && ts.getDayOfWeek() >= settings.getBaseLoadStart().getDayOfWeek() && ts.getHourOfDay() >= settings.getBaseLoadStart().getHourOfDay() && ts.getMinuteOfHour() >= settings.getBaseLoadStart().getMinuteOfHour() && ts.getSecondOfMinute() >= settings.getBaseLoadStart().getSecondOfMinute()
                                            && ts.getDayOfMonth() <= settings.getBaseLoadEnd().getDayOfMonth() && ts.getDayOfWeek() <= settings.getBaseLoadEnd().getDayOfWeek() && ts.getHourOfDay() <= settings.getBaseLoadEnd().getHourOfDay() && ts.getMinuteOfHour() <= settings.getBaseLoadEnd().getMinuteOfHour() && ts.getSecondOfMinute() <= settings.getBaseLoadEnd().getSecondOfMinute()) {
                                        baseLoadSum += jeVisSample.getValueAsDouble();
                                        baseLoadSamples.add(jeVisSample);
                                    }
                                    break;
                                case 4:
                                    //year
                                    if (ts.getMonthOfYear() >= settings.getBaseLoadStart().getMonthOfYear() && ts.getDayOfMonth() >= settings.getBaseLoadStart().getDayOfMonth() && ts.getDayOfWeek() >= settings.getBaseLoadStart().getDayOfWeek() && ts.getHourOfDay() >= settings.getBaseLoadStart().getHourOfDay() && ts.getMinuteOfHour() >= settings.getBaseLoadStart().getMinuteOfHour() && ts.getSecondOfMinute() >= settings.getBaseLoadStart().getSecondOfMinute()
                                            && ts.getMonthOfYear() <= settings.getBaseLoadEnd().getMonthOfYear() && ts.getDayOfMonth() <= settings.getBaseLoadEnd().getDayOfMonth() && ts.getDayOfWeek() <= settings.getBaseLoadEnd().getDayOfWeek() && ts.getHourOfDay() <= settings.getBaseLoadEnd().getHourOfDay() && ts.getMinuteOfHour() <= settings.getBaseLoadEnd().getMinuteOfHour() && ts.getSecondOfMinute() <= settings.getBaseLoadEnd().getSecondOfMinute()) {
                                        baseLoadSum += jeVisSample.getValueAsDouble();
                                        baseLoadSamples.add(jeVisSample);
                                    }
                                    break;

                            }
                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                }

                if (!baseLoadSamples.isEmpty()) {
                    baseLoadSum = baseLoadSum / baseLoadSamples.size();
                }

                chartDataRow.setSelectedStart(settings.getResultStart());
                chartDataRow.setSelectedEnd(settings.getResultEnd());

                List<JEVisSample> samples = chartDataRow.getSamples();

                double min = Double.MAX_VALUE;
                double max = -Double.MAX_VALUE;
                double avg = 0.0;
                double sum = 0.0;
                long zeroCount = 0;

                for (JEVisSample sample : samples) {
                    try {
                        Double currentValue = sample.getValueAsDouble();
                        currentValue -= baseLoadSum;

                        min = Math.min(min, currentValue);
                        max = Math.max(max, currentValue);
                        sum += currentValue;

                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }

                if (chartDataRow.getManipulationMode().equals(ManipulationMode.CUMULATE)) {
                    avg = max / samples.size();
                    sum = max;
                }

                if (!chartDataRow.getManipulationMode().equals(ManipulationMode.CUMULATE) && samples.size() > 0) {
                    avg = sum / (samples.size() - zeroCount);
                }

                QuantityUnits qu = new QuantityUnits();
                boolean isQuantity = qu.isQuantityUnit(chartDataRow.getUnit());

                if (min == Double.MAX_VALUE || samples.size() == 0) {
                    baseLoad.setMin("- " + chartDataRow.getUnit());
                } else {
                    baseLoad.setMin(nf.format(min) + " " + chartDataRow.getUnit());
                }

                if (max == -Double.MAX_VALUE || samples.size() == 0) {
                    baseLoad.setMax("- " + chartDataRow.getUnit());
                } else {
                    baseLoad.setMax(nf.format(max) + " " + chartDataRow.getUnit());
                }

                if (samples.size() == 0) {
                    baseLoad.setAvg("- " + chartDataRow.getUnit());
                    baseLoad.setSum("- " + chartDataRow.getUnit());
                } else {

                    baseLoad.setAvg(nf.format(avg) + " " + chartDataRow.getUnit());

                    if (isQuantity) {
                        baseLoad.setSum(nf.format(sum) + " " + chartDataRow.getUnit());
                    } else {
                        if (qu.isSumCalculable(chartDataRow.getUnit()) && chartDataRow.getManipulationMode().equals(ManipulationMode.NONE)) {
                            try {
                                JEVisUnit sumUnit = qu.getSumUnit(chartDataRow.getUnit());
                                ChartUnits cu = new ChartUnits();

                                Period currentPeriod = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
                                Period rawPeriod = CleanDataObject.getPeriodForDate(attribute.getObject(), samples.get(0).getTimestamp());

                                double newScaleFactor = cu.scaleValue(rawPeriod, chartDataRow.getUnit().toString(), currentPeriod, sumUnit.toString());
                                JEVisUnit inputUnit = attribute.getInputUnit();
                                JEVisUnit sumUnitOfInputUnit = qu.getSumUnit(inputUnit);

                                if (qu.isDiffPrefix(sumUnitOfInputUnit, sumUnit)) {
                                    sum = sum * newScaleFactor / chartDataRow.getTimeFactor();
                                } else {
                                    sum = sum / chartDataRow.getScaleFactor() / chartDataRow.getTimeFactor();
                                }

                                baseLoad.setSum(nf.format(sum) + " " + chartDataRow.getUnit());

                            } catch (Exception e) {
                                logger.error("Couldn't calculate periods");
                                baseLoad.setSum("- " + chartDataRow.getUnit());
                            }
                        } else {
                            baseLoad.setSum("- " + chartDataRow.getUnit());
                        }
                    }
                }

                data.add(baseLoad);
            }
        }

        BaseLoadTable baseLoadTable = new BaseLoadTable(FXCollections.observableList(data));

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, baseLoadTable, separator);

        okButton.setOnAction(event -> {
            try {


            } catch (Exception e) {
                logger.error(e);
            }
            close();
        });

        cancelButton.setOnAction(event -> close());

        getDialogPane().setContent(vBox);
    }

}
