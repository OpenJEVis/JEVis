package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.LastValueHandler;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;

public class NumberWidget extends Widget {

    Label textField = new Label("345345,98 kWh");

    public NumberWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);
    }

    @Override
    public void update(WidgetData data, boolean hasNewData) {

    }

    public SampleHandler getSampleHandler() {
        LastValueHandler sampleHandler = new LastValueHandler(getDataSource());
        sampleHandler.setMultiSelect(false);

        //TODO

        return sampleHandler;

    }

    @Override
    public void init() {
        System.out.println("NumberWidget update");
        AnchorPane anchorPane = new AnchorPane();
        VBox vBox = new VBox(12);
        Label nameLabel = new Label("Main Meter");


        nameLabel.setFont(new Font(12));
        textField.setFont(new Font(20));
        textField.setTextFill(Color.CORNFLOWERBLUE);
        nameLabel.setTextFill(Color.GREY);

        vBox.getChildren().addAll(textField, nameLabel);
        vBox.setAlignment(Pos.CENTER);


        anchorPane.getChildren().add(vBox);
        AnchorPane.setRightAnchor(vBox, 20.0);
        AnchorPane.setLeftAnchor(vBox, 20.0);
        AnchorPane.setTopAnchor(vBox, 20.0);
        AnchorPane.setBottomAnchor(vBox, 20.0);

        nameLabel.textFillProperty().bind(config.fontColor);
        textField.textFillProperty().bind(config.fontColorSecondary);
        config.backgroundColor.addListener((observable, oldValue, newValue) -> {
            anchorPane.setBackground(new Background(new BackgroundFill(newValue, cornerRadii, new Insets(0, 0, 0, 0))));
        });
        anchorPane.setBackground(new Background(new BackgroundFill(config.backgroundColor.get(), cornerRadii, new Insets(0, 0, 0, 0))));


        setGraphic(anchorPane);
    }

    @Override
    public String typeID() {
        return "Number Widget";
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/HighLow.png", 100, 100);
    }
}
