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
package org.jevis.jecc.sample;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTimeZone;

import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class AttributeStatesExtension implements SampleEditorExtension {
    private static final Logger logger = LogManager.getLogger(AttributeStatesExtension.class);

    private final static String TITLE = I18n.getInstance().getString("plugin.object.attribute.overview.header");
    private final BorderPane _view = new BorderPane();
    private JEVisAttribute _att;
    private List<JEVisSample> _samples;
    private boolean _dataChanged = true;

    public AttributeStatesExtension(JEVisAttribute att) {
        _att = att;
    }

    private void buildGui(JEVisAttribute att) throws JEVisException {
        Label lName = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.name"));
        Label lType = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.type"));
//        Label lUnit = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.unit"));
        Label lSampleRate = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.samplerate"));
        Label lDataSize = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.datasize"));
        Label lSCount = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.totalsamplecount"));
        Label lFirst = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.firstsample"));
        Label lLast = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.latestsample"));
//        Label lMinValue = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.smallestvalue"));
//        Label lMaxValue = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.biggestvalue"));
//        Label lAVGValue = new Label(I18n.getInstance().getString("plugin.object.attribute.overview.averagevalue"));

        Label name = new Label();
        Label type = new Label();
//        Label unit = new Label();
        Label dataSize = new Label();
        Label sampleRate = new Label();
        Label sCount = new Label();
        Label first = new Label();
        Label last = new Label();
//        Label minValue = new Label();
//        Label maxValue = new Label();
//        Label avgValue = new Label();

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
//        gp.add(lUnit, 0, y);
//        gp.add(unit, 1, y);
//        y++;
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
//        gp.add(lMinValue, 0, y);
//        gp.add(minValue, 1, y);
//        y++;
//        gp.add(lMaxValue, 0, y);
//        gp.add(maxValue, 1, y);
//        y++;
//        gp.add(lAVGValue, 0, y);
//        gp.add(avgValue, 1, y);

        name.setText(att.getName());
        type.setText(att.getType().getName());
//        unit.setText(att.getDisplayUnit().toString());
        sampleRate.setText(att.getInputSampleRate().toString());
        sCount.setText(att.getSampleCount() + "");
        int dbAVG = 100;//byte from DB, this value is not fix and only an avg
        try {
            if (att.getTimestampOfFirstSample() != null) {
                first.setText(att.getTimestampOfFirstSample().toString());
            }
            if (att.getTimestampOfFirstSample() != null) {
                last.setText(att.getTimestampOfLastSample().toString());
            }


            dataSize.setText(((dbAVG * att.getSampleCount()) / 1048576) + " MB, per Sample ~" + dbAVG + " bytes");
        } catch (Exception ex) {

        }


        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(gp);
        _view.setCenter(scroll);
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (_dataChanged) {
                    try {
                        buildGui(_att);
                        _dataChanged = false;
                    } catch (Exception ex) {
                        logger.fatal(ex);
                    }

                }
            }
        });
    }


    @Override
    public boolean sendOKAction() {
        return false;
    }

}
