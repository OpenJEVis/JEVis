package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.Dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.Interval;

import java.util.List;

public class OpenAnalysisWidget extends Widget {

    public static String WIDGET_ID = "Open Analysis";
    private final JFXButton label = new JFXButton();

    public OpenAnalysisWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    @Override
    public void updateData(Interval interval) {
        Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
        this.label.setBackground(bgColor);
        this.label.setTextFill(this.config.getFontColor());
        this.label.setText(this.config.getTitle());
        this.label.setFont(new Font(this.config.getFontSize()));
        this.label.setAlignment(this.config.getTitlePosition());


    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {

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
//        label.setPadding(new Insets(0, 8, 0, 8));
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(this.label);
        Layouts.setAnchor(this.label, 0);
        setGraphic(anchorPane);
    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/DonutWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


}
