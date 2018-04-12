/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.sample;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class AttributeStatesExtension implements SampleEditorExtension {

    private final static String TITEL = "Overview";
    private final BorderPane _view = new BorderPane();
    private JEVisAttribute _att;
    private List<JEVisSample> _samples;
    private boolean _dataChanged = true;

    public AttributeStatesExtension(JEVisAttribute att) {
        _att = att;
    }

    private void buildGui(JEVisAttribute att, List<JEVisSample> samples) throws JEVisException {
        Label lName = new Label("Name:");
        Label lType = new Label("Type:");
        Label lUnit = new Label("Unit:");
        Label lSampleRate = new Label("Sample Rate:");
        Label lDataSize = new Label("Data Size:");
        Label lSCount = new Label("Total Sample Count:");
        Label lFirst = new Label("First Sample:");
        Label lLast = new Label("Lastest Sample:");
        Label lMinValue = new Label("Smallest Value:");
        Label lMaxValue = new Label("Biggest Value:");
        Label lAVGValue = new Label("Average Value:");

        Label name = new Label();
        Label type = new Label();
        Label unit = new Label();
        Label dataSize = new Label();
        Label sampleRate = new Label();
        Label sCount = new Label();
        Label first = new Label();
        Label last = new Label();
        Label minValue = new Label();
        Label maxValue = new Label();
        Label avgValue = new Label();

        name.setMinWidth(300);

        GridPane gp = new GridPane();
        gp.setStyle("-fx-background-color: transparent;");
//        gp.setStyle("-fx-background-color: #E2E2E2;");
        gp.setPadding(new Insets(10));
        gp.setHgap(7);
        gp.setVgap(7);

        int y = 0;
//        gp.add(lName, 0, y);
//        gp.add(name, 1, y);
//        y++;
        gp.add(lType, 0, y);
        gp.add(type, 1, y);
        y++;
        gp.add(lUnit, 0, y);
        gp.add(unit, 1, y);
        y++;
        gp.add(lSampleRate, 0, y);
        gp.add(sampleRate, 1, y);
        y++;
        gp.add(lSCount, 0, y);
        gp.add(sCount, 1, y);
        y++;
        gp.add(lDataSize, 0, y);
        gp.add(dataSize, 1, y);
        y++;
        gp.add(lFirst, 0, y);
        gp.add(first, 1, y);
        y++;
        gp.add(lLast, 0, y);
        gp.add(last, 1, y);
        y++;
        gp.add(lMinValue, 0, y);
        gp.add(minValue, 1, y);
        y++;
        gp.add(lMaxValue, 0, y);
        gp.add(maxValue, 1, y);
        y++;
        gp.add(lAVGValue, 0, y);
        gp.add(avgValue, 1, y);

        name.setText(att.getName());
        type.setText(att.getType().getName());
        unit.setText(att.getDisplayUnit().toString());
        sampleRate.setText(att.getInputSampleRate().toString());
        sCount.setText(att.getSampleCount() + "");

        if (att.hasSample()) {
            JEVisSample minS = null;
            JEVisSample maxS = null;
            first.setText(att.getTimestampFromFirstSample().toString());
            last.setText(att.getTimestampFromLastSample().toString());
            double total = 0;
            for (JEVisSample sample : samples) {
                if (minS == null || minS.getTimestamp().isAfter(sample.getTimestamp())) {
                    minS = sample;
                }
                if (maxS == null || maxS.getTimestamp().isBefore(sample.getTimestamp())) {
                    maxS = sample;
                }
                if (att.getType().getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE
                        || att.getType().getPrimitiveType() == JEVisConstants.PrimitiveType.LONG) {
                    total += sample.getValueAsDouble();
                }

            }
            if (minS != null && maxS != null) {
                maxValue.setText(maxS.getValue() + "  at (" + maxS.getTimestamp().toString() + ")");
                minValue.setText(minS.getValue() + "  at (" + minS.getTimestamp().toString() + ")");
                avgValue.setText((total / samples.size()) + "");

                //
                int dbAVG = 100;//byte from DB, this value is not fix and only an avg
                dataSize.setText(((dbAVG * samples.size()) / 1048576) + " MB, per Sample ~" + dbAVG + " bytes");
            }
        }

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(gp);
//        _view.getChildren().setAll(scroll);
        _view.setCenter(scroll);
//        _view.setCenter(buildChart(samples));
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
    public String getTitel() {
        return TITEL;
    }

    @Override
    public void setSamples(final JEVisAttribute att, final List<JEVisSample> samples) {
        _samples = samples;
        _att = att;
        _dataChanged = true;
    }

    @Override
    public void update() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (_dataChanged) {
                    try {

                        buildGui(_att, _att.getAllSamples());
                        _dataChanged = false;
                    } catch (JEVisException ex) {
                        Logger.getLogger(AttributeStatesExtension.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        });
    }

    private XYChart buildChart(final List<JEVisSample> samples) {

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(true);

        xAxis.setTickUnit(1);
        xAxis.setTickMarkVisible(true);
        xAxis.setTickLength(1);
//        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
//
//            @Override
//            public String toString(Number t) {
//
//                DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd    HH:mm:ss");
//                try {
////                    Number index = (t.doubleValue() - 1.5);
//                    Number index = t.doubleValue();
////                    if (index.intValue() < 0) {
////                        index = 0;
////                    }
////                    if (index.intValue() > 100) {
////                        System.out.println(" is bigger 100");
////                    }
//                    System.out.println("convert Major value: " + t.toString() + "=" + index);
//                    return fmtDate.print(samples.get(index.intValue()).getTimestamp());
////                return fmtDate.print(new DateTime(t.longValue()));
//                } catch (Exception ex) {
//                    System.out.println("error");
//                }
//                return t.toString();
//            }
//
//            @Override
//            public Number fromString(String string) {
//                System.out.println("from string: " + string);
//                return 200;
//            }
//        });

        final DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd    HH:mm:ss");
        xAxis.setMinorTickCount(1);
        xAxis.setMinorTickLength(1);
        xAxis.setMinorTickVisible(true);
        xAxis.setTickLabelRotation(75d);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {

            @Override
            public String toString(Number t) {

                try {
//                    System.out.println("number: " + t);
                    //TODO: replace this DIRTY workaround. For this i will come in the DevHell
                    //NOTE: the axis is % based, java 1.8 has an dateAxe use this if we migrate to it
                    int index = samples.size() / 100 * t.intValue();
                    return fmtDate.print(samples.get(index).getTimestamp());
//                    return fmtDate.print(samples.get(t.intValue()).getTimestamp());
                } catch (Exception ex) {
                    System.out.println("error: " + ex);
                    return "";
                }

            }

            @Override
            public Number fromString(String string) {
                System.out.println("from string: " + string);
                return 200;
            }
        });

//        xAxis.setLabel("Month");
        final LineChart<Number, Number> lineChart = new LineChart(xAxis, yAxis);
//        final BarChart<String, Number> lineChart = new BarChart(xAxis, yAxis);

        String titel = String.format("");

        lineChart.setTitle(titel);
//        lineChart.setAnimated(true);
        lineChart.setLegendVisible(false);
        lineChart.setCache(true);

        XYChart.Series series1 = new XYChart.Series();

//        DateTimeFormatter fmttime = DateTimeFormat.forPattern("E HH:mm:ss");
//        DateTimeFormatter fmttime2 = DateTimeFormat.forPattern("E HH:mm:ss");
        int pos = 0;

        for (JEVisSample sample : samples) {
            try {
////                String datelabel = "";
////                if (pos == 0 || samples.size() == pos || pos % 10 == 0) {
////                    datelabel = fmtDate.print(sample.getTimestamp());
////                }

//                String datelabel = fmtDate.print(sample.getTimestamp());
                series1.getData().add(new XYChart.Data((Number) pos, sample.getValueAsDouble()));

//                System.out.println("pos1: " + pos + " sample=" + sample);
                if (yAxis.getLowerBound() > sample.getValueAsDouble()) {
                    yAxis.setLowerBound(sample.getValueAsDouble() * 0.9d);
                }

                pos++;
//                series1.getData().add(new XYChart.Data(pos + "", sample.getValueAsDouble()));
            } catch (Exception ex) {
                Logger.getLogger(SampleEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        yAxis.setLowerBound(10d);

//        int size = 22 * samples.size();
//        int size = 15 * samples.size();
//        lineChart.setPrefWidth(size);
        lineChart.setPrefWidth(720);
        lineChart.getData().addAll(series1);

        return lineChart;
    }

    @Override
    public boolean sendOKAction() {
        return false;
    }

}
