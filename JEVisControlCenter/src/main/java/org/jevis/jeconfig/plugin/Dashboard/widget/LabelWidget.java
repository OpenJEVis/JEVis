package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.NullSampleHandel;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;

public class LabelWidget extends Widget {

    private Label textLabel = new Label();
    private StringProperty textProperty = new SimpleStringProperty("");


    public LabelWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);
    }

    @Override
    public SampleHandler getSampleHandler() {
        return new NullSampleHandel(getDataSource());
    }

    @Override
    public ImageView getImagePreview() {
        return null;
    }

    @Override
    public void update(WidgetData data, boolean hasNewData) {

    }

    @Override
    public void init() {

        HBox root = new HBox();
        root.getChildren().add(textLabel);

        getChildren().add(root);

        config.backgroundColor.addListener((observable, oldValue, newValue) -> {
//            tile.setBackgroundColor(newValue);
            Background colorBackground = new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY));
            root.setBackground(colorBackground);
        });

        config.fontColor.addListener((observable, oldValue, newValue) -> {
            textLabel.setTextFill(newValue);
        });


    }

    @Override
    public String typeID() {
        return null;
    }
}
