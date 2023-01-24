package org.jevis.jeconfig.dialog;

import com.ibm.icu.text.NumberFormat;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jeconfig.application.Chart.Charts.Chart;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.plugin.charts.Values;
import org.jevis.jeconfig.plugin.charts.ValuesSetting;
import org.jevis.jeconfig.plugin.charts.ValuesTable;
import org.jevis.jeconfig.sample.DaySchedule;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValuesDialog extends JFXDialog {
    private static final Logger logger = LogManager.getLogger(ValuesDialog.class);
    final NumberFormat nf = NumberFormat.getNumberInstance();

    public ValuesDialog(StackPane dialogContainer, JEVisDataSource ds, ValuesSetting settings, HashMap<Integer, Chart> model) {
        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);

        this.nf.setMinimumFractionDigits(model.values().stream().findFirst().get().getChartModel().getMinFractionDigits());
        this.nf.setMaximumFractionDigits(model.values().stream().findFirst().get().getChartModel().getMaxFractionDigits());

        List<Values> data = new ArrayList<>();

        for (Map.Entry<Integer, Chart> entry : model.entrySet()) {
            Integer integer = entry.getKey();
            Chart chart = entry.getValue();
            for (ChartDataRow chartDataRow : chart.getChartDataRows()) {
                Values values = new Values(chartDataRow.getName());

                JEVisAttribute attribute = chartDataRow.getAttribute();
                List<JEVisSample> samples = chartDataRow.getSamples();

                double min = Double.MAX_VALUE;
                double max = -Double.MAX_VALUE;
                double avg = 0.0;
                double sum = 0.0;
                long zeroCount = 0;
                long sampleCount = 0;

                for (JEVisSample sample : samples) {
                    try {
                        DateTime timestamp = sample.getTimestamp();

                        if (DaySchedule.dateCheck(timestamp, settings.getDaySchedule()) == settings.isInside()) {
                            Double currentValue = sample.getValueAsDouble();

                            min = Math.min(min, currentValue);
                            max = Math.max(max, currentValue);
                            sum += currentValue;

                            sampleCount++;
                            if (currentValue.equals(0d)) zeroCount++;
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }

                if (chartDataRow.getManipulationMode().equals(ManipulationMode.CUMULATE)) {
                    avg = max / sampleCount;
                    sum = max;
                }

                if (!chartDataRow.getManipulationMode().equals(ManipulationMode.CUMULATE) && sampleCount > 0) {
                    avg = sum / (sampleCount - zeroCount);
                }

                QuantityUnits qu = new QuantityUnits();
                boolean isQuantity = qu.isQuantityUnit(chartDataRow.getUnit());

                if (min == Double.MAX_VALUE || sampleCount == 0) {
                    values.setMin("- " + chartDataRow.getUnit());
                } else {
                    values.setMin(nf.format(min) + " " + chartDataRow.getUnit());
                }

                if (max == -Double.MAX_VALUE || sampleCount == 0) {
                    values.setMax("- " + chartDataRow.getUnit());
                } else {
                    values.setMax(nf.format(max) + " " + chartDataRow.getUnit());
                }

                if (sampleCount == 0) {
                    values.setMin("- " + chartDataRow.getUnit());
                    values.setMax("- " + chartDataRow.getUnit());
                    values.setAvg("- " + chartDataRow.getUnit());
                    values.setSum("- " + chartDataRow.getUnit());
                    values.setCount("-");
                    values.setZeros("-");
                } else {

                    values.setAvg(nf.format(avg) + " " + chartDataRow.getUnit());
                    values.setCount("" + sampleCount);
                    values.setZeros("" + zeroCount);

                    if (isQuantity) {
                        values.setSum(nf.format(sum) + " " + chartDataRow.getUnit());
                    } else {
                        if (qu.isSumCalculable(chartDataRow.getUnit()) && chartDataRow.getManipulationMode().equals(ManipulationMode.NONE)) {
                            try {
                                JEVisUnit sumUnit = qu.getSumUnit(chartDataRow.getUnit());
                                ChartUnits cu = new ChartUnits();
                                double newScaleFactor = cu.scaleValue(chartDataRow.getUnit().toString(), sumUnit.toString());
                                JEVisUnit inputUnit = attribute.getInputUnit();
                                JEVisUnit sumUnitOfInputUnit = qu.getSumUnit(inputUnit);

                                if (qu.isDiffPrefix(sumUnitOfInputUnit, sumUnit)) {
                                    sum = sum * newScaleFactor / chartDataRow.getTimeFactor();
                                } else {
                                    sum = sum / chartDataRow.getScaleFactor() / chartDataRow.getTimeFactor();
                                }

                                values.setSum(nf.format(sum) + " " + sumUnit);

                            } catch (Exception e) {
                                logger.error("Couldn't calculate periods");
                                values.setSum("- " + chartDataRow.getUnit());
                            }
                        } else {
                            values.setSum("- " + chartDataRow.getUnit());
                        }
                    }
                }

                data.add(values);

            }
        }

        ValuesTable baseLoadTable = new ValuesTable(FXCollections.observableList(data));

        final JFXButton ok = new JFXButton(I18n.getInstance().getString("newobject.ok"));
        ok.setDefaultButton(true);
        final JFXButton cancel = new JFXButton(I18n.getInstance().getString("newobject.cancel"));
        cancel.setCancelButton(true);

        HBox buttonBar = new HBox(6, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(12));

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, baseLoadTable, separator, buttonBar);

        ok.setOnAction(event -> {
            try {


            } catch (Exception e) {
                logger.error(e);
            }
            close();
        });

        cancel.setOnAction(event -> close());

        setContent(vBox);
    }

}
