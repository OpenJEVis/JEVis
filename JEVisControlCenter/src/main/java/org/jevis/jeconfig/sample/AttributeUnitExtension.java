/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.sample;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.unit.SamplingRateUI;
import org.jevis.jeconfig.plugin.unit.UnitSelectUI;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTimeZone;

import java.util.List;

/**
 * @author fs
 */
public class AttributeUnitExtension implements SampleEditorExtension {

    private final static String TITLE = I18n.getInstance().getString("attribute.editor.unit.title");
    private final BorderPane _view = new BorderPane();
    private JEVisAttribute att;
    private UnitSelectUI iuUnit;
    private UnitSelectUI ouUnit;

    public AttributeUnitExtension(JEVisAttribute att) {
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
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void setSamples(JEVisAttribute att, List<JEVisSample> samples) {

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
        _view.getChildren().clear();
        try {

            //legend
            final Label l_prefixL = new Label(I18n.getInstance().getString("attribute.editor.unit.prefix"));
            final Label l_unitL = new Label(I18n.getInstance().getString("attribute.editor.unit.unit"));
            final Label l_example = new Label(I18n.getInstance().getString("attribute.editor.unit.symbol"));
            final Label l_SampleRate = new Label(I18n.getInstance().getString("attribute.editor.unit.samplingrate"));

            final Label l_dbUnit = new Label(I18n.getInstance().getString("attribute.editor.unit.meteringunit"));
            final Label l_displayUnit = new Label(I18n.getInstance().getString("attribute.editor.unit.diplayunit"));


            iuUnit = new UnitSelectUI(att.getDataSource(), att.getInputUnit());
            ouUnit = new UnitSelectUI(att.getDataSource(), att.getDisplayUnit());
            SamplingRateUI iuRate = new SamplingRateUI(att.getInputSampleRate());
            SamplingRateUI ouRate = new SamplingRateUI(att.getDisplaySampleRate());

            Button applyToRight = new Button("", JEConfig.getImage("right.png", 12, 12));
            Button applyToLeft = new Button("", JEConfig.getImage("left.png", 12, 12));

            applyToRight.setOnAction(event -> {
                try {
                    att.setDisplayUnit(att.getInputUnit());
                    att.setDisplaySampleRate(att.getInputSampleRate());

                    ouUnit.setUnit(att.getInputUnit());
                    ouUnit.getUnitButton().setText(att.getInputUnit().getFormula());
                    ouUnit.getPrefixBox().getSelectionModel().select(
                            UnitManager.getInstance().getPrefix(att.getInputUnit().getPrefix()));
                    ouUnit.getSymbolField().setText(iuUnit.getSymbolField().getText());

                    ouRate.samplingRateProperty().setValue(att.getInputSampleRate());
                    ouRate.getSelectionModel().select(att.getInputSampleRate());


                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            });

            applyToLeft.setOnAction(event -> {
                try {
                    att.setInputUnit(att.getDisplayUnit());
                    att.setInputSampleRate(att.getDisplaySampleRate());

                    iuUnit.setUnit(att.getDisplayUnit());
                    iuUnit.getUnitButton().setText(att.getDisplayUnit().getFormula());
                    iuUnit.getPrefixBox().getSelectionModel().select(
                            UnitManager.getInstance().getPrefix(att.getDisplayUnit().getPrefix()));
                    iuUnit.getSymbolField().setText(ouUnit.getSymbolField().getText());

                    iuRate.samplingRateProperty().setValue(att.getDisplaySampleRate());
                    iuRate.getSelectionModel().select(att.getDisplaySampleRate());

                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            });

            iuRate.samplingRateProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    att.setInputSampleRate(iuRate.samplingRateProperty().getValue());
                } catch (Exception ex) {
                    ex.printStackTrace();//TODO
                }
            });

            ouRate.samplingRateProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    att.setDisplaySampleRate(ouRate.samplingRateProperty().getValue());
                } catch (Exception ex) {
                    ex.printStackTrace();//TODO
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
            gp.add(l_displayUnit, 5, row, 1, 1);

//            row++;
//            gp.add(new Separator(Orientation.HORIZONTAL), 1, row, 2, 1);
            Separator sep = new Separator(Orientation.VERTICAL);
            sep.setOpacity(0.3);
            gp.add(sep, 2, 1, 1, 5);
            Separator sep2 = new Separator(Orientation.VERTICAL);
            sep2.setOpacity(0.3);
            gp.add(sep2, 4, 1, 1, 5);

            row++;
            gp.add(l_prefixL, 0, row, 1, 1);
            gp.add(iuUnit.getPrefixBox(), 1, row, 1, 1);
            gp.add(applyToRight, 3, row, 1, 1);
            gp.add(ouUnit.getPrefixBox(), 5, row, 1, 1);

            row++;
            gp.add(l_unitL, 0, row, 1, 1);
            gp.add(iuUnit.getUnitButton(), 1, row, 1, 1);
            gp.add(ouUnit.getUnitButton(), 5, row, 1, 1);

            row++;
            gp.add(l_example, 0, row, 1, 1);
            gp.add(iuUnit.getSymbolField(), 1, row, 1, 1);
            gp.add(applyToLeft, 3, row, 1, 1);
            gp.add(ouUnit.getSymbolField(), 5, row, 1, 1);

            row++;
            gp.add(l_SampleRate, 0, row, 1, 1);
            gp.add(iuRate, 1, row, 1, 1);
            gp.add(ouRate, 5, row, 1, 1);

            iuUnit.getPrefixBox().setPrefWidth(95);
            ouUnit.getPrefixBox().setPrefWidth(95);
            iuUnit.getUnitButton().setPrefWidth(95);
            ouUnit.getUnitButton().setPrefWidth(95);
            iuUnit.getSymbolField().setPrefWidth(95);
            ouUnit.getSymbolField().setPrefWidth(95);

            GridPane.setVgrow(hSep, Priority.ALWAYS);
            _view.setCenter(gp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean sendOKAction() {
        try {
            if (iuUnit != null) {
                att.setInputUnit(iuUnit.getUnit());
            }
            if (ouUnit != null) {
                att.setDisplayUnit(ouUnit.getUnit());
            }
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
