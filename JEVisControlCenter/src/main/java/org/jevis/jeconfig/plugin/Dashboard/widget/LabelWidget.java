package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfigProperty;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.NullSampleHandel;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;

import java.util.ArrayList;
import java.util.List;

public class LabelWidget extends Widget {

    private HBox root = new HBox();
    private Label textLabel = new Label("blub test");


    public LabelWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);
    }

    @Override
    public SampleHandler getSampleHandler() {
        return new NullSampleHandel(getDataSource());
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/DonutChart.png", previewSize.getHeight(), previewSize.getWidth());
    }

    @Override
    public void update(WidgetData data, boolean hasNewData) {

    }

    @Override
    public void setBackgroundColor(Color color) {
        Background colorBackground = new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
        root.setBackground(colorBackground);
        textLabel.setBackground(colorBackground);
    }

    @Override
    public void setTitle(String text) {

    }

    @Override
    public void setFontColor(Color color) {
        System.out.println("SetFont Color: " + color.toString());
        textLabel.setTextFill(color);

    }

    @Override
    public void setCustomFont(Font font) {
        textLabel.setFont(font);
    }


    @Override
    public void init() {
        if (isInitialized.getValue()) {
            return;
        }
//        textLabel.setMinWidth(100);

        System.out.println("init");
        root.getChildren().add(textLabel);
        HBox.setHgrow(textLabel, Priority.ALWAYS);
//        getChildren().add(textLabel);


        StringProperty textProperty = new SimpleStringProperty("");
        textProperty.addListener((observable, oldValue, newValue) -> {
            System.out.println("label text changes: " + newValue);
            Platform.runLater(() -> {
                textLabel.setText(newValue);
            });

        });

        String category = "Label Widget";

        List<WidgetConfigProperty> propertyList = new ArrayList<>();
        propertyList.add(new WidgetConfigProperty<String>("Widget.Text", category, "Text", "", textProperty));
        config.addAdditionalSetting(propertyList);


        addCommonConfigListeners();
        applyCannonConfig();
        isInitialized.setValue(true);

        setGraphic(root);
    }


    @Override
    public String typeID() {
        return "Label";
    }
}
