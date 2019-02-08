package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.LastValueHandler;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;

import java.util.concurrent.atomic.AtomicReference;

public class ObjectWidget extends Widget {

    GridPane root = new GridPane();

    public ObjectWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);
    }

    public SampleHandler getSampleHandler() {
        LastValueHandler sampleHandler = new LastValueHandler(getDataSource());
        sampleHandler.setMultiSelect(false);


        return sampleHandler;

    }

    @Override
    public void update(WidgetData data, boolean hasNewData) {

        try {
            root.getChildren().clear();
            AtomicReference<Integer> row = new AtomicReference<>(0);

            JEConfig.getDataSource().getObject(777l).getAttributes().forEach(jeVisAttribute -> {
                row.set(row.get() + 1);

                Label attributeName = new Label(jeVisAttribute.getName() + ":");
                JEVisSample sample = jeVisAttribute.getLatestSample();
                String value = "";
                if (sample != null) {
                    try {
                        value = sample.getValueAsString();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                Label attributeValue = new Label(value);
                root.add(attributeName, 0, row.get());
                root.add(attributeValue, 1, row.get());
            });
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        setGraphic(root);
    }

    @Override
    public void init() {

    }

    @Override
    public String typeID() {
        return "Object Widget";
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/HighLow.png", 100, 100);
    }

}
