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

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.AxisLabelOverlapPolicy;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

    private void buildGui(JEVisAttribute attribute, List<JEVisSample> samples) {

        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: transparent");

        try {
            if (attribute.getObject().getJEVisClassName().equals("Data") || attribute.getObject().getJEVisClassName().equals("Clean Data")) {

                DateTime firstTS = null;
                DateTime lastTS = null;
                DoubleDataSet dataSet = new DoubleDataSet(attribute.getObject().getName());
                for (JEVisSample jeVisSample : samples) {
                    try {
                        dataSet.add(jeVisSample.getTimestamp().getMillis() / 1000.0, jeVisSample.getValueAsDouble());

                        if (samples.indexOf(jeVisSample) == 0) {
                            firstTS = jeVisSample.getTimestamp();
                        } else if (samples.indexOf(jeVisSample) == samples.size() - 1) {
                            lastTS = jeVisSample.getTimestamp();
                        }
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }

                DateTimeFormatter dtfOutLegend = DateTimeFormat.forPattern("EE. dd.MM.yyyy HH:mm");
                String overall = String.format("%s %s %s",
                        dtfOutLegend.print(firstTS),
                        I18n.getInstance().getString("plugin.graph.chart.valueaxis.until"),
                        dtfOutLegend.print(lastTS));

                final DefaultNumericAxis xAxis1 = new DefaultNumericAxis(I18n.getInstance().getString("plugin.graph.chart.dateaxis.title") + " " + overall, null);
                xAxis1.setOverlapPolicy(AxisLabelOverlapPolicy.SKIP_ALT);
                final DefaultNumericAxis yAxis1 = new DefaultNumericAxis(null, null);

                final XYChart chart = new XYChart(xAxis1, yAxis1);
                chart.legendVisibleProperty().set(false);
                chart.getPlugins().add(new Zoomer());
                //            chart.getPlugins().add(new EditAxis());

                DataPointTooltip dataPointTooltip = new DataPointTooltip();
                try {
                    dataPointTooltip.setPickingDistance(attribute.getDisplaySampleRate().toStandardDuration().getMillis() / 1000.0);
                } catch (Exception ignored) {
                }

                chart.getPlugins().add(dataPointTooltip);
                // set them false to make the plot faster
                chart.setAnimated(false);

                xAxis1.setAutoRangeRounding(false);
                // xAxis1.invertAxis(true); TODO: bug inverted time axis crashes when zooming
                xAxis1.setTimeAxis(true);
                yAxis1.setAutoRangeRounding(true);

                ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
                renderer.setPolyLineStyle(LineStyle.AREA);
                renderer.getDatasets().add(dataSet);

                chart.getRenderers().add(renderer);

                bp.setCenter(chart);
                _view.setCenter(bp);
            }
        } catch (JEVisException e) {
            e.printStackTrace();
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
    public void setDateTimeZone(DateTimeZone dateTimeZone) {

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
