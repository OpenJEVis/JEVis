package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.Interval;

import java.util.List;

public class TitleWidget extends Widget {

    public static String WIDGET_ID = "Title";
    private final Label label = new Label();

    public TitleWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource, new WidgetConfig(WIDGET_ID));
    }

    public TitleWidget(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        super(jeVisDataSource, config);
    }


    @Override
    public void update(Interval interval) {
        //if config changed
        if (config.hasChanged("")) {
            Background bgColor = new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY));
            label.setBackground(bgColor);
            label.setTextFill(config.fontColor.getValue());
            label.setText(config.title.getValue());
            label.setFont(new Font(config.fontSize.getValue()));
            label.setAlignment(config.titlePosition.getValue());

        }


    }

    public void applyColors(PieChart chart, List<Color> colors) {
        for (int i = 0; i < colors.size(); i++) {
            try {
                Color currentColor = colors.get(i);
                String hexColor = toRGBCode(currentColor);
                String preIdent = ".default-color" + i;
                Node node = chart.lookup(preIdent + ".chart-pie");
                node.setStyle("-fx-pie-color: " + hexColor + ";");
            } catch (Exception ex) {
            }
        }
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    @Override
    public void init() {
        label.setText(config.title.getValue());
        label.setPadding(new Insets(0, 8, 0, 8));
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(label);
        Layouts.setAnchor(label, 0);
        setGraphic(anchorPane);
    }


    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/DonutWidget.png", previewSize.getHeight(), previewSize.getWidth());
    }
}
