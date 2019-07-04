package org.jevis.jeconfig.plugin.Dashboard.config2;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.DashboardControl;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.Layouts;
import org.jevis.jeconfig.tool.ToggleSwitchPlus;


public class Navigator {
    private double iconSize = 20;
    final ImageView lockIcon = JEConfig.getImage("eye_visible.png", this.iconSize, this.iconSize);
    final ImageView unlockIcon = JEConfig.getImage("eye_hidden.png", this.iconSize, this.iconSize);


    private final DashboardControl control;


    public Navigator(DashboardControl control) {
        this.control = control;
    }


    public void show() {
        final Stage stage = new Stage(StageStyle.UTILITY);


        stage.setTitle(I18n.getInstance().getString("attribute.editor.title"));
        stage.initModality(Modality.NONE);
        stage.initOwner(JEConfig.getStage());

        AnchorPane root = new AnchorPane();

        root.setStyle("-fx-background-color: orange;");
        Control table = buildTable();
        ToolBar toolBar = buildToolbar();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(table);
        borderPane.setTop(toolBar);


        Layouts.setAnchor(borderPane, 0d);
        root.getChildren().add(borderPane);

        final Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.setWidth(740);
        stage.setHeight(800);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        scene.setFill(Color.TRANSPARENT);
        stage.show();
    }


    private ToolBar buildToolbar() {
        ToolBar toolBar = new ToolBar();
        ToggleSwitchPlus jfxToggleButton = new ToggleSwitchPlus();
        jfxToggleButton.setLabels("Hervorheben", "Hervorheben");//TODO locale


        ToggleButton unlockB = new ToggleButton("", this.unlockIcon);

        unlockB.selectedProperty().bindBidirectional(this.control.highligtProperty);
        unlockB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                unlockB.setGraphic(this.lockIcon);
            } else {
                unlockB.setGraphic(this.unlockIcon);
            }
        });

        toolBar.getItems().addAll(unlockB);

        return toolBar;
    }

    private Control buildTable() {
//        gridPane.setStyle("-fx-background-color: orange;");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

//        List<Widget> widgetList = this.control.getWidgets();
//        System.out.println("Widgets: " + widgetList.size());
        WidgetColumnFactory widgetColumnFactory = new WidgetColumnFactory(this.control);
        TableView table = widgetColumnFactory.buildTable(this.control.getWidgetList());

        scrollPane.setContent(table);
//        table.getItems().setAll(this.control.getWidgets());

        return scrollPane;

    }


}
