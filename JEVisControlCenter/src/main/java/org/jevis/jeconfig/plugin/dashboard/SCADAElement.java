package org.jevis.jeconfig.plugin.dashboard;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import org.jevis.api.JEVisAttribute;
import org.jevis.jeconfig.plugin.dashboard.data.ScadaElementData;

/**
 * The interface Scada element.
 */
public interface SCADAElement {


    void setData(ScadaElementData data);

    StringProperty titleProperty();

    void setAttribute(JEVisAttribute att);

    DoubleProperty xPositionProperty();

    DoubleProperty yPositionProperty();

    Node getGraphic();

    void update();


}
