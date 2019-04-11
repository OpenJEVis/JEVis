package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisLineChart;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.List;

public class ValueWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(ValueWidget.class);
    public static String WIDGET_ID = "Value";
    private final Label label = new Label();
    private NumberFormat nf = NumberFormat.getInstance();
    private DataModelDataHandler sampleHandler;

    public ValueWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource, new WidgetConfig(WIDGET_ID));
    }

    public ValueWidget(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        super(jeVisDataSource, config);
    }


    @Override
    public void update(Interval interval) {
        logger.debug("Value.Update: {}", interval);

        sampleHandler.setInterval(interval);
        sampleHandler.update();

        //if config changed
        if (config.hasChanged("")) {
            Platform.runLater(() -> {
                Background bgColor = new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY));
                label.setBackground(bgColor);
                label.setTextFill(config.fontColor.getValue());

                label.setContentDisplay(ContentDisplay.CENTER);
            });

            nf.setMinimumFractionDigits(config.decimals.getValue());
            nf.setMaximumFractionDigits(config.decimals.getValue());
        }


        Platform.runLater(() -> {
            try {
                ChartDataModel dataModel = sampleHandler.getDataModel().get(0);
                List<JEVisSample> results;

                String unit = dataModel.getUnitLabel();


                if (dataModel.getEnPI()) {
                    CalcJobFactory calcJobCreator = new CalcJobFactory();

                    CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), dataModel.getObject().getDataSource(), dataModel.getCalculationObject(),
                            dataModel.getSelectedStart(), dataModel.getSelectedEnd(), true);

                    results = calcJob.getResults();

                } else {
                    results = dataModel.getSamples();
                }

                if (!results.isEmpty()) {
                    label.setText(nf.format(results.get(results.size() - 1).getValueAsDouble()) + " " + unit);
                }


            } catch (Exception ex) {
                label.setText("-");
                logger.error(ex);
//                ex.printStackTrace();
            }

        });

    }

    private void setChartLabel(MultiAxisLineChart chart, Color newValue) {
        chart.getY1Axis().setTickLabelFill(newValue);
        chart.getXAxis().setTickLabelFill(newValue);

        chart.getXAxis().setLabel("");
        chart.getY1Axis().setLabel("");
    }


    @Override
    public void init() {

        sampleHandler = new DataModelDataHandler(getDataSource(), config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
        sampleHandler.setMultiSelect(false);
        label.setPadding(new Insets(0, 8, 0, 8));
        setGraphic(label);
    }


    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ValueWidget.png", previewSize.getHeight(), previewSize.getWidth());
    }
}
