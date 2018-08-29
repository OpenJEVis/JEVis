package org.jevis.jeconfig.plugin.dashboard;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.jevis.api.*;
import org.jevis.jeconfig.plugin.dashboard.data.ScadaElementData;

public class LabelElement extends MoveableNode implements SCADAElement {

    private StringProperty titleProperty = new SimpleStringProperty();
    private StringProperty typeProperty = new SimpleStringProperty();
    private DoubleProperty xPositionProperty = new SimpleDoubleProperty();
    private DoubleProperty yPositionProperty = new SimpleDoubleProperty();
//    private DoubleProperty widthProperty = new SimpleDoubleProperty();
//    private DoubleProperty heightProperty = new SimpleDoubleProperty();

    private JEVisAttribute attribute;
    private HBox view = new HBox(8);
    private Label label = new Label();
    private Label text = new Label();

    private ScadaElementData data;
    private SCADAAnalysis analysis;

    public LabelElement(SCADAAnalysis analysis) {
        super();
        super.setContent(view);
        this.analysis = analysis;
    }


    @Override
    public void setData(ScadaElementData data) {
        try {
            JEVisDataSource ds = analysis.getObject().getDataSource();
            JEVisObject targetObj = ds.getObject(data.getObjectID());
            JEVisAttribute targetAtt = targetObj.getAttribute(data.getAttribute());
            attribute = targetAtt;

            xPositionProperty.setValue(data.getxPos());
            yPositionProperty.setValue(data.getyPos());


        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    @Override
    public StringProperty titleProperty() {
        return titleProperty;
    }


    @Override
    public void setAttribute(JEVisAttribute att) {
        attribute = att;
//        label.textProperty().bindBidirectional(titleProperty);
        titleProperty.bindBidirectional(label.textProperty());
    }

    @Override
    public DoubleProperty xPositionProperty() {
        return xPositionProperty;
    }

    @Override
    public DoubleProperty yPositionProperty() {
        return yPositionProperty;
    }


    @Override
    public Node getGraphic() {
        view.getChildren().clear();
        view.getChildren().addAll(label, text);

        text.setTextFill(Color.RED);//DEBUG
        label.setTextFill(Color.BLUE);//DEBUG
        view.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        view.setStyle("-fx-background-color: #00ff00;");


        return this;
    }

    @Override
    public void update() {
        String value = "";
        if (attribute != null) {
            JEVisSample lastSample = attribute.getLatestSample();
            if (lastSample != null) {
                try {
                    value = lastSample.getValueAsString();
                } catch (NullPointerException | JEVisException e) {
                    e.printStackTrace();
                }
            }
        }

        this.text.setText(value);

    }

}
