package org.jevis.jeconfig.plugin.Dashboard.wizzard;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.jevis.jeconfig.plugin.Dashboard.widget.DonutChart;
import org.jevis.jeconfig.plugin.Dashboard.widget.HighLowWidget;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;

import java.util.ArrayList;
import java.util.List;

public class PageWidgetSelection extends Page {


    public ObjectProperty<Widget> selectedWidgetProperty = new SimpleObjectProperty<>();
    Wizard wizard;
    private Widget selectedWidget = null;
    private Pane selectedWidgetContainer = null;

    public void setWizard(Wizard wizard) {
        this.wizard = wizard;
    }

    @Override
    public Node getNode() {

        List<Widget> widgetList = new ArrayList<>();
        widgetList.add(new DonutChart(wizard.getDataSource()));
        widgetList.add(new HighLowWidget(wizard.getDataSource()));

        AnchorPane root = new AnchorPane();
        final FlowPane widgetListPane = new FlowPane(Orientation.HORIZONTAL, 20, 20);

//        widgetListPane.setStyle("-fx-background-color: blue;");
//        widgetListPane.setPrefWrapLength(300);
        widgetList.forEach(widget -> {

            Button widgetButton = new Button(widget.typeID(), widget.getImagePreview());
            widgetButton.setContentDisplay(ContentDisplay.TOP);

            widgetListPane.getChildren().add(widgetButton);

            widgetButton.setOnAction(event -> {
                System.out.println("Widget event: " + widget + " setVar: ");
//                wizard.selectedWidget.setValue(widget);
                selectedWidgetProperty.setValue(widget);
            });

        });

        AnchorPane.setTopAnchor(widgetListPane, 10.0);
        AnchorPane.setBottomAnchor(widgetListPane, 10.0);
        AnchorPane.setLeftAnchor(widgetListPane, 10.0);
        AnchorPane.setRightAnchor(widgetListPane, 10.0);
        return widgetListPane;

    }
}
