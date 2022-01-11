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
import org.jevis.jeconfig.plugin.charts.BaseLoad;
import org.jevis.jeconfig.plugin.charts.BaseLoadSetting;
import org.jevis.jeconfig.plugin.charts.BaseLoadTable;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class BaseLoadDialog extends JFXDialog {
    private static final Logger logger = LogManager.getLogger(BaseLoadDialog.class);
    final NumberFormat nf = NumberFormat.getNumberInstance();

    public BaseLoadDialog(StackPane dialogContainer, BaseLoadSetting settings, AnalysisDataModel model) {
        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);

        this.nf.setMinimumFractionDigits(model.getCharts().getListSettings().get(0).getMinFractionDigits());
        this.nf.setMaximumFractionDigits(model.getCharts().getListSettings().get(0).getMaxFractionDigits());

        List<BaseLoad> data = new ArrayList<>();
        int repeatType = settings.getRepeatType();

        for (ChartDataRow dataRow : model.getSelectedData()) {
            BaseLoad baseLoad = new BaseLoad(dataRow.getTitle());

            JEVisAttribute attribute = dataRow.getAttribute();
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

            dataRow.setSelectedStart(settings.getResultStart());
            dataRow.setSelectedEnd(settings.getResultEnd());

            List<JEVisSample> samples = dataRow.getSamples();

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

            if (dataRow.getManipulationMode().equals(ManipulationMode.CUMULATE)) {
                avg = max / samples.size();
                sum = max;
            }

            if (!dataRow.getManipulationMode().equals(ManipulationMode.CUMULATE) && samples.size() > 0) {
                avg = sum / (samples.size() - zeroCount);
            }

            QuantityUnits qu = new QuantityUnits();
            boolean isQuantity = qu.isQuantityUnit(dataRow.getUnit());

            if (min == Double.MAX_VALUE || samples.size() == 0) {
                baseLoad.setMin("- " + dataRow.getUnit());
            } else {
                baseLoad.setMin(nf.format(min) + " " + dataRow.getUnit());
            }

            if (max == -Double.MAX_VALUE || samples.size() == 0) {
                baseLoad.setMax("- " + dataRow.getUnit());
            } else {
                baseLoad.setMax(nf.format(max) + " " + dataRow.getUnit());
            }

            if (samples.size() == 0) {
                baseLoad.setAvg("- " + dataRow.getUnit());
                baseLoad.setSum("- " + dataRow.getUnit());
            } else {

                baseLoad.setAvg(nf.format(avg) + " " + dataRow.getUnit());

                if (isQuantity) {
                    baseLoad.setSum(nf.format(sum) + " " + dataRow.getUnit());
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

                            baseLoad.setSum(nf.format(sum) + " " + dataRow.getUnit());

                        } catch (Exception e) {
                            logger.error("Couldn't calculate periods");
                            baseLoad.setSum("- " + dataRow.getUnit());
                        }
                    } else {
                        baseLoad.setSum("- " + dataRow.getUnit());
                    }
                }
            }

            data.add(baseLoad);
        }

        BaseLoadTable baseLoadTable = new BaseLoadTable(FXCollections.observableList(data));

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
