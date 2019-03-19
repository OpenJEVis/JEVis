package org.jevis.jeconfig.application.Chart.ChartElements;


import javafx.scene.Node;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;

public class XYScatterChartSerie extends XYChartSerie {
    public XYScatterChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        super(singleRow, hideShowIcons);
    }

    @Override
    public Node generateNode(JEVisSample sample) throws JEVisException {
        return null;
    }

    @Override
    public void setDataNodeColor(MultiAxisChart.Data<Number, Number> data) {

    }
}
