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
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.plugin.charts.Values;
import org.jevis.jeconfig.plugin.charts.ValuesSetting;
import org.jevis.jeconfig.plugin.charts.ValuesTable;
import org.jevis.jeconfig.sample.DaySchedule;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class ValuesDialog extends JFXDialog {
    private static final Logger logger = LogManager.getLogger(ValuesDialog.class);
    final NumberFormat nf = NumberFormat.getNumberInstance();

    public ValuesDialog(StackPane dialogContainer, ValuesSetting settings, AnalysisDataModel model) {
        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);

        this.nf.setMinimumFractionDigits(model.getCharts().getListSettings().get(0).getMinFractionDigits());
        this.nf.setMaximumFractionDigits(model.getCharts().getListSettings().get(0).getMaxFractionDigits());

        List<Values> data = new ArrayList<>();

        for (ChartDataRow dataRow : model.getSelectedData()) {
            Values values = new Values(dataRow.getTitle());

            JEVisAttribute attribute = dataRow.getAttribute();
            List<JEVisSample> samples = dataRow.getSamples();

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

            if (dataRow.getManipulationMode().equals(ManipulationMode.CUMULATE)) {
                avg = max / sampleCount;
                sum = max;
            }

            if (!dataRow.getManipulationMode().equals(ManipulationMode.CUMULATE) && sampleCount > 0) {
                avg = sum / (sampleCount - zeroCount);
            }

            QuantityUnits qu = new QuantityUnits();
            boolean isQuantity = qu.isQuantityUnit(dataRow.getUnit());

            if (min == Double.MAX_VALUE || sampleCount == 0) {
                values.setMin("- " + dataRow.getUnit());
            } else {
                values.setMin(nf.format(min) + " " + dataRow.getUnit());
            }

            if (max == -Double.MAX_VALUE || sampleCount == 0) {
                values.setMax("- " + dataRow.getUnit());
            } else {
                values.setMax(nf.format(max) + " " + dataRow.getUnit());
            }

            if (sampleCount == 0) {
                values.setMin("- " + dataRow.getUnit());
                values.setMax("- " + dataRow.getUnit());
                values.setAvg("- " + dataRow.getUnit());
                values.setSum("- " + dataRow.getUnit());
                values.setCount("-");
                values.setZeros("-");
            } else {

                values.setAvg(nf.format(avg) + " " + dataRow.getUnit());
                values.setCount("" + sampleCount);
                values.setZeros("" + zeroCount);

                if (isQuantity) {
                    values.setSum(nf.format(sum) + " " + dataRow.getUnit());
                } else {
                    if (qu.isSumCalculable(dataRow.getUnit()) && dataRow.getManipulationMode().equals(ManipulationMode.NONE)) {
                        try {
                            JEVisUnit sumUnit = qu.getSumUnit(dataRow.getUnit());
                            ChartUnits cu = new ChartUnits();
                            double newScaleFactor = cu.scaleValue(dataRow.getUnit().toString(), sumUnit.toString());
                            JEVisUnit inputUnit = attribute.getInputUnit();
                            JEVisUnit sumUnitOfInputUnit = qu.getSumUnit(inputUnit);

                            if (qu.isDiffPrefix(sumUnitOfInputUnit, sumUnit)) {
                                sum = sum * newScaleFactor / dataRow.getTimeFactor();
                            } else {
                                sum = sum / dataRow.getScaleFactor() / dataRow.getTimeFactor();
                            }

                            values.setSum(nf.format(sum) + " " + dataRow.getUnit());

                        } catch (Exception e) {
                            logger.error("Couldn't calculate periods");
                            values.setSum("- " + dataRow.getUnit());
                        }
                    } else {
                        values.setSum("- " + dataRow.getUnit());
                    }
                }
            }

            data.add(values);
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
