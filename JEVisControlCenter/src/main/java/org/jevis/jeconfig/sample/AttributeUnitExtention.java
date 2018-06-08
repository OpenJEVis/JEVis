/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.application.jevistree.plugin.AttributeSettingsDialog;
import org.jevis.application.unit.SampleRateNode;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.unit.SamplingRateUI;
import org.jevis.jeconfig.plugin.unit.UnitSelectUI;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.Period;

import java.util.List;

/**
 * @author fs
 */
public class AttributeUnitExtention implements SampleEditorExtension {

    private final static String TITEL = I18n.getInstance().getString("attribute.editor.unit.title");
    private final BorderPane _view = new BorderPane();
    private JEVisAttribute att;
    Stage owner = JEConfig.getStage();
    private AttributeSettingsDialog.Response response = AttributeSettingsDialog.Response.CANCEL;
    private JEVisAttribute _attribute;
    private UnitSelectUI upDisplay;
    private UnitSelectUI upInput;
    private SampleRateNode _inputSampleRate;
    private SampleRateNode _displaySampleRate;

    public AttributeUnitExtention(JEVisAttribute att) {
        this.att = att;
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
    public void setSamples(JEVisAttribute att, List<JEVisSample> samples) {

    }

    @Override
    public void update() {
        _view.getChildren().clear();
        try {

            //legend
            final Label l_prefixL = new Label(I18n.getInstance().getString("attribute.editor.unit.prefix"));
            final Label l_unitL = new Label(I18n.getInstance().getString("attribute.editor.unit.unit"));
            final Label l_example = new Label(I18n.getInstance().getString("attribute.editor.unit.symbol"));
            final Label l_SampleRate = new Label(I18n.getInstance().getString("attribute.editor.unit.samplingrate"));

            final Label l_dbUnit = new Label(I18n.getInstance().getString("attribute.editor.unit.meteringunit"));
            final Label l_displayUnit = new Label(I18n.getInstance().getString("attribute.editor.unit.diplayunit"));

            _displaySampleRate = new SampleRateNode(att.getDisplaySampleRate());
            _inputSampleRate = new SampleRateNode(att.getInputSampleRate());

            UnitSelectUI iuUnit = new UnitSelectUI(att.getDataSource(), att.getInputUnit());
            UnitSelectUI ouUnit = new UnitSelectUI(att.getDataSource(), att.getDisplayUnit());
            SamplingRateUI iuRate = new SamplingRateUI(att.getInputSampleRate());
            SamplingRateUI ouRate = new SamplingRateUI(att.getDisplaySampleRate());

            iuUnit.unitProperty().addListener(new ChangeListener<JEVisUnit>() {
                @Override
                public void changed(ObservableValue<? extends JEVisUnit> observable, JEVisUnit oldValue, JEVisUnit newValue) {
                    try {
                        att.setInputUnit(iuUnit.unitProperty().getValue());
                    } catch (JEVisException jex) {
                        jex.printStackTrace();
                    }
                }
            });

            ouUnit.unitProperty().addListener(new ChangeListener<JEVisUnit>() {
                @Override
                public void changed(ObservableValue<? extends JEVisUnit> observable, JEVisUnit oldValue, JEVisUnit newValue) {
                    try {
                        att.setDisplayUnit(ouUnit.unitProperty().getValue());
                    } catch (JEVisException jex) {
                        jex.printStackTrace();
                    }
                }
            });


            iuRate.sampleingRateProperty().addListener(new ChangeListener<Period>() {
                @Override
                public void changed(ObservableValue<? extends Period> observable, Period oldValue, Period newValue) {
                    try {
                        att.setInputSampleRate(newValue);
                    } catch (Exception ex) {
                        ex.printStackTrace();//TODO
                    }
                }
            });

            ouRate.sampleingRateProperty().addListener(new ChangeListener<Period>() {
                @Override
                public void changed(ObservableValue<? extends Period> observable, Period oldValue, Period newValue) {
                    try {
                        att.setDisplaySampleRate(newValue);
                    } catch (Exception ex) {
                        ex.printStackTrace();//TODO
                    }
                }
            });
//            HBox iuSepUnit = buildTitelPane("Metering Unit");
//            HBox ioSepRate = buildTitelPane("Defaultdisplay Unit");
            Separator hSep = new Separator(Orientation.VERTICAL);

            GridPane gp = new GridPane();
            gp.setHgap(12);
            gp.setVgap(5);
            gp.setPadding(new Insets(10, 10, 10, 10));

            int row = 1;
            //                   x  Y  x+ y+
            gp.add(new Region(), 0, row, 1, 1);
            gp.add(l_dbUnit, 1, row, 1, 1);
            gp.add(l_displayUnit, 3, row, 1, 1);

//            row++;
//            gp.add(new Separator(Orientation.HORIZONTAL), 1, row, 2, 1);
            Separator sep = new Separator(Orientation.VERTICAL);
            sep.setOpacity(0.3);
            gp.add(sep, 2, 1, 1, 5);

            row++;
            gp.add(l_prefixL, 0, row, 1, 1);
            gp.add(iuUnit.getPrefixBox(), 1, row, 1, 1);
            gp.add(ouUnit.getPrefixBox(), 3, row, 1, 1);

            row++;
            gp.add(l_unitL, 0, row, 1, 1);
            gp.add(iuUnit.getUnitButton(), 1, row, 1, 1);
            gp.add(ouUnit.getUnitButton(), 3, row, 1, 1);

            row++;
            gp.add(l_example, 0, row, 1, 1);
            gp.add(iuUnit.getLabelField(), 1, row, 1, 1);
            gp.add(ouUnit.getLabelField(), 3, row, 1, 1);

            row++;
            gp.add(l_SampleRate, 0, row, 1, 1);
            gp.add(iuRate, 1, row, 1, 1);
            gp.add(ouRate, 3, row, 1, 1);

            iuUnit.getPrefixBox().setPrefWidth(95);
            ouUnit.getPrefixBox().setPrefWidth(95);
            iuUnit.getUnitButton().setPrefWidth(95);
            ouUnit.getUnitButton().setPrefWidth(95);
            iuUnit.getLabelField().setPrefWidth(95);
            ouUnit.getLabelField().setPrefWidth(95);

            GridPane.setVgrow(hSep, Priority.ALWAYS);
            _view.setCenter(gp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean sendOKAction() {
        try {
            att.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;

    }

    private HBox buildTitelPane(String title) {
        HBox box = new HBox(5);
        box.setPadding(new Insets(5));
        box.setAlignment(Pos.BASELINE_CENTER);
        Label titleLabel = new Label(title);
        Separator sepLeft = new Separator(Orientation.HORIZONTAL);
        Separator sepRight = new Separator(Orientation.HORIZONTAL);
        box.getChildren().addAll(sepLeft, titleLabel, sepRight);
        HBox.setHgrow(sepLeft, Priority.ALWAYS);
        HBox.setHgrow(sepRight, Priority.ALWAYS);
        return box;
    }

}
