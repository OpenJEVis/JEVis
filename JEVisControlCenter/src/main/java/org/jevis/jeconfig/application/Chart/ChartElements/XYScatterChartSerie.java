package org.jevis.jeconfig.application.Chart.ChartElements;


import javafx.scene.Node;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.application.Chart.ChartDataModel;

public class XYScatterChartSerie extends XYChartSerie {
    public XYScatterChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        super(singleRow, hideShowIcons);
    }

    @Override
    public Node generateNode(JEVisSample sample) throws JEVisException {
        return null;
    }
}
