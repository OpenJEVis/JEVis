package org.jevis.jeconfig.application.Chart.ChartElements;


import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.application.Chart.data.ChartDataModel;

public class XYScatterChartSerie extends XYChartSerie {
    public XYScatterChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        super(singleRow, hideShowIcons, false);
    }

    @Override
    public String generateNote(JEVisSample sample) throws JEVisException {
        return null;
    }

    @Override
    public void setDataNodeColor(MultiAxisChart.Data<Number, Number> data) {

    }
}
