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
import org.jevis.jeconfig.plugin.Dashboard.widget.*;

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
        widgetList.add(new NumberWidget(wizard.getDataSource()));
        widgetList.add(new StockWidget(wizard.getDataSource()));
        widgetList.add(new LabelWidget(wizard.getDataSource()));

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

    @Override
    public boolean isSkipable() {
        return false;
    }
}
