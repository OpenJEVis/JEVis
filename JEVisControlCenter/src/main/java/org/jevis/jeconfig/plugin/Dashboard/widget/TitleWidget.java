package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.Dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.Interval;

public class TitleWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(TitleWidget.class);
    public static String WIDGET_ID = "Title";
    private final Label label = new Label();

    public TitleWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }


    @Override
    public void updateData(Interval interval) {
        logger.debug("Update titleWidget: {}", this.config.getTitle());
        Platform.runLater(() -> {
            Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
            this.label.setBackground(bgColor);
            this.label.setTextFill(this.config.getFontColor());
            this.label.setText(this.config.getTitle());
            this.label.setFont(new Font(this.config.getFontSize()));
            this.label.setPrefWidth(this.config.getSize().getWidth());

            this.label.setAlignment(this.config.getTitlePosition());

//            switch (this.config.getTitlePosition()) {
//                case TOP_LEFT:
//                case BOTTOM_LEFT:
//                case CENTER_LEFT:
//                case BASELINE_LEFT:
//                    System.out.println("Case 1");
//                    this.label.setTextAlignment(TextAlignment.LEFT);
//                    break;
//                case CENTER:
//                case BASELINE_CENTER:
//                case BOTTOM_CENTER:
//                case TOP_CENTER:
//                    System.out.println("case 2");
//                    this.label.setTextAlignment(TextAlignment.CENTER);
//                    break;
//                case TOP_RIGHT:
//                case BOTTOM_RIGHT:
//                case CENTER_RIGHT:
//                case BASELINE_RIGHT:
//                    System.out.println("case 3");
//                    this.label.setTextAlignment(TextAlignment.RIGHT);
//                    break;

//
//            }

//            System.out.println("TestPOs: " + this.config.getTitlePosition().toString());
//            if (this.config.getTitlePosition().toString().contains("LEFT")) {
//                this.label.setTextAlignment(TextAlignment.LEFT);
//            } else if (this.config.getTitlePosition().toString().contains("CENTER")) {
//                this.label.setTextAlignment(TextAlignment.CENTER);
//            } else if (this.config.getTitlePosition().toString().contains("RIGHT")) {
//                this.label.setTextAlignment(TextAlignment.RIGHT);
//            }


//            this.label.setTextAlignment(TextAlignment.LEFT);
//            this.label.setText(this.config.getTitle());
        });
    }

    @Override
    public void updateLayout() {


    }

    @Override
    public void updateConfig() {

    }


    @Override
    public void init() {
        this.label.setPadding(new Insets(0, 8, 0, 8));
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
        return JEConfig.getImage("widget/TitleWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }
}
