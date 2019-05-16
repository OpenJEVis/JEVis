package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.List;

public class ValueWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(ValueWidget.class);
    public static String WIDGET_ID = "Value";
    private final Label label = new Label();
    private NumberFormat nf = NumberFormat.getInstance();
    private DataModelDataHandler sampleHandler;
    private StringProperty labelText = new SimpleStringProperty("n.a.");

    public ValueWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource, new WidgetConfig(WIDGET_ID));
    }

    public ValueWidget(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        super(jeVisDataSource, config);
    }


    @Override
    public void update(Interval interval) {
        logger.debug("Value.Update: {}", interval);
        Platform.runLater(() -> {
            label.setText(I18n.getInstance().getString("plugin.dashboard.loading"));
        });


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

        labelText.setValue("n.a.");

        try {
            ChartDataModel dataModel = sampleHandler.getDataModel().get(0);
            List<JEVisSample> results;

            String unit = dataModel.getUnitLabel();


            results = dataModel.getSamples();

//            if (dataModel.getEnPI()) {
//                System.out.println("is EnpI: " + dataModel.getEnPI());
//                CalcJobFactory calcJobCreator = new CalcJobFactory();
//
//                CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(
//                        new SampleHandler(), dataModel.getObject().getDataSource(), dataModel.getCalculationObject(),
//                        dataModel.getSelectedStart(), dataModel.getSelectedEnd(), true);
//
//                results = calcJob.getResults();
//
//            } else {
//                results = dataModel.getSamples();
//            }

            if (!results.isEmpty()) {
                labelText.setValue((nf.format(DataModelDataHandler.getTotal(results))) + " " + unit);
            }


        } catch (Exception ex) {
            logger.error(ex);
        }

        Platform.runLater(() -> {
            label.setText(labelText.getValue());
        });


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
