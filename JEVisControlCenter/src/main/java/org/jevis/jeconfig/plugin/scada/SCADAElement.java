package org.jevis.jeconfig.plugin.scada;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.jevis.api.JEVisAttribute;
import org.jevis.application.jevistree.UserSelection;
import org.jevis.jeconfig.plugin.scada.data.ScadaElementData;

/**
 * The interface Scada element.
 */
public interface SCADAElement {


    ScadaElementData getData();

    void setData(ScadaElementData data);

    StringProperty titleProperty();

    void setAttribute(JEVisAttribute att);

    DoubleProperty xPositionProperty();

    DoubleProperty yPositionProperty();

    Node getGraphic();

    void update();

    UserSelection getUserSeclection();

    double getRelativeXPos();

    double getRelativeYPos();

    void setParent(Pane parent);

    BooleanProperty movableProperty();

    void openConfig();
}
