/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.sample;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.Charts.LineChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SampleGraphExtension implements SampleEditorExtension {
    private static final Logger logger = LogManager.getLogger(SampleGraphExtension.class);
    private final static String TITLE = "Graph";
    private final BorderPane _view = new BorderPane();
    private JEVisAttribute _att;
    private List<JEVisSample> _samples;
    private boolean _dataChanged = true;

    public SampleGraphExtension(JEVisAttribute att) {
        _att = att;
    }

    private void buildGui(JEVisAttribute obj, List<JEVisSample> samples) {

        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: transparent");

        ChartDataModel chartDataModel = null;
        try {
            chartDataModel = new ChartDataModel(obj.getDataSource());
        } catch (JEVisException e) {
            logger.error("Could not get data source from object: " + e);
        }

        if (chartDataModel != null) {
            try {
                if (obj.getObject().getJEVisClassName().equals("Data")) chartDataModel.setObject(obj.getObject());
                else if (obj.getObject().getJEVisClassName().equals("Clean Data")) {
                    chartDataModel.setDataProcessor(obj.getObject());
                    chartDataModel.setObject(obj.getObject().getParents().get(0));
                } else {
                    chartDataModel.setObject(obj.getObject());
                }
            } catch (JEVisException e) {

            }

            List<Integer> list = new ArrayList<>();
            list.add(0);
            chartDataModel.setSelectedCharts(list);
            chartDataModel.setAttribute(obj);
            chartDataModel.setSamples(samples);
            chartDataModel.setColor(Color.BLUE);
            chartDataModel.setSomethingChanged(false);

            List<ChartDataModel> chartDataModelList = new ArrayList<>();
            chartDataModelList.add(chartDataModel);

            LineChart lc = new LineChart(chartDataModelList, false, false, false, ManipulationMode.NONE, 0, "");
            lc.setRegion(lc.getJfxChartUtil().setupZooming((MultiAxisChart<?, ?>) lc.getChart()));

            bp.setCenter(lc.getRegion());
            _view.setCenter(bp);
        }
    }

    @Override
    public boolean isForAttribute(JEVisAttribute obj) {
        return true;
    }

    @Override
    public Node getView() {
        return _view;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void setSamples(final JEVisAttribute att, final List<JEVisSample> samples) {
        _samples = samples;
        _att = att;
        _dataChanged = true;
    }

    @Override
    public void disableEditing(boolean disable) {
        //TODO
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            if (_dataChanged) {
                if (_samples != null && !_samples.isEmpty()) buildGui(_att, _samples);
                _dataChanged = false;
            }
        });
    }

    @Override
    public boolean sendOKAction() {
        return false;
    }
}
