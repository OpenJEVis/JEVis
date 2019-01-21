package org.jevis.jeconfig.application.Chart.ChartElements;


import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;

public class XYScatterChartSerie extends XYChartSerie {
    public XYScatterChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        super(singleRow, hideShowIcons);
    }

    @Override
    public void generateNode(JEVisSample sample, MultiAxisChart.Data<Number, Number> data) throws JEVisException {
    }
}
