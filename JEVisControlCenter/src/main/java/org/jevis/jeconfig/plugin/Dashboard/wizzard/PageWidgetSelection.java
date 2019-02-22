package org.jevis.jeconfig.plugin.Dashboard.wizzard;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widgets;

public class PageWidgetSelection extends Page {


    public ObjectProperty<Widget> selectedWidgetProperty = new SimpleObjectProperty<>();
    Wizard wizard;
    private Widget selectedWidget = null;
    private Pane selectedWidgetContainer = null;
    private DashBordPlugIn dashBordPlugIn;

    public PageWidgetSelection() {
    }

    public void setWizard(Wizard wizard) {
        this.wizard = wizard;
    }


    @Override
    public Node getNode() {


//        final FlowPane widgetListPane = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        final TilePane widgetListPane = new TilePane(Orientation.HORIZONTAL, 20, 20);
        widgetListPane.setMaxWidth(Region.USE_COMPUTED_SIZE);

//        widgetListPane.setStyle("-fx-background-color: blue;");
//        widgetListPane.setPrefWrapLength(300);
        Widgets.getAvabableWidgets(JEConfig.getDataSource()).forEach(widget -> {

            Button widgetButton = new Button(widget.typeID(), widget.getImagePreview());
            widgetButton.setContentDisplay(ContentDisplay.TOP);

            widgetListPane.getChildren().add(widgetButton);

            widgetButton.setOnAction(event -> {
                System.out.println("Widget event: " + widget + " setVar: ");
//                wizard.selectedWidget.setValue(widget);
                selectedWidgetProperty.setValue(widget);
            });

        });


        ScrollPane scrollPane = new ScrollPane(widgetListPane);
        scrollPane.setStyle("-fx-background-color:transparent;");
        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            widgetListPane.setMaxWidth(newValue.doubleValue() - 5);
        });

        AnchorPane.setTopAnchor(scrollPane, 10.0);
        AnchorPane.setBottomAnchor(scrollPane, 10.0);
        AnchorPane.setLeftAnchor(scrollPane, 10.0);
        AnchorPane.setRightAnchor(scrollPane, 10.0);
//        AnchorPane.setTopAnchor(widgetListPane, 10.0);
//        AnchorPane.setBottomAnchor(widgetListPane, 10.0);
//        AnchorPane.setLeftAnchor(widgetListPane, 10.0);
//        AnchorPane.setRightAnchor(widgetListPane, 10.0);
        return scrollPane;

    }

    @Override
    public boolean isSkipable() {
        return false;
    }
}
