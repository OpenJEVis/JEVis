package org.jevis.jeconfig.plugin.object.extension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.extension.calculation.CalculationViewController;
import org.joda.time.DateTime;

public class CalculationExtension implements ObjectEditorExtension {

    private static final String CALC_CLASS_NAME = "Calculation";
    private static final String TITLE = I18n.getInstance().getString("plugin.object.calc.title");
    private static final Logger logger = LogManager.getLogger(CalculationExtension.class);
    private final BorderPane view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _enabledChanged = new SimpleBooleanProperty(false);
    private JEVisObject _obj;
    private CalculationViewController control;
    private String oldExpression = "";
    private JEVisSample lastSampleEnabeld = null;
    private JEVisSample _newSampleEnabled = null;

    public CalculationExtension(JEVisObject _obj) {
        this._obj = _obj;
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        boolean isCalcObject = false;
        try {
            isCalcObject = obj.getJEVisClassName().equals(CALC_CLASS_NAME);
        } catch (JEVisException e) {
            logger.error("Could not get object type" + e.getLocalizedMessage());
        }
        return isCalcObject;
    }


    @Override
    public void showHelp(boolean show) {

    }

    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void setVisible() {


        //    Button button = new Button();
        //  button.setText("Calc");
        //ap.getChildren().add(button);

//         = new Pane();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/EditCalculation.fxml"));
        fxmlLoader.setResources(I18n.getInstance().getBundle());
        //fxmlLoader.setRoot();
        //fxmlLoader.setController(new CalculationViewController());
        try {
            Button buttonOutput = new Button(I18n.getInstance().getString("plugin.object.calc.output"));
            final ScrollPane editConfigPane = fxmlLoader.load();
            control = fxmlLoader.getController();
            control.setData(_obj, buttonOutput);

            JEVisAttribute aExprsssion = _obj.getAttribute("Expression");
            JEVisSample lastValue = aExprsssion.getLatestSample();

            if (lastValue != null) {
                logger.info("LastSample: " + lastValue.getTimestamp() + " " + lastValue.getValueAsString());
                oldExpression = lastValue.getValueAsString();
            }

            ToggleSwitch enableButton = new ToggleSwitch();
            enableButton.setPrefWidth(65);

            try {
                JEVisAttribute enabled = _obj.getAttribute("Enabled");
                JEVisSample lsample = enabled.getLatestSample();

                if (lsample != null) {
                    boolean selected = lsample.getValueAsBoolean();
                    editConfigPane.disableProperty().setValue(!selected);
                    enableButton.setSelected(selected);
                    enableButton.selectedProperty().setValue(selected);
//            _field.setSelected(selected);//TODO: get default Value
                    if (selected) {
                        enableButton.setText(I18n.getInstance().getString("extension.calc.button.toggle.activate"));
                    } else {
                        enableButton.setText(I18n.getInstance().getString("extension.calc.button.toggle.deactivate"));
                    }


                } else {
                    enableButton.setSelected(false);//TODO: get default Value
                    enableButton.setText(I18n.getInstance().getString("extension.calc.button.toggle.deactivate"));
                }


                enableButton.selectedProperty().addListener((ov, t, t1) -> {
                    try {
                        _newSampleEnabled = enabled.buildSample(new DateTime(), enableButton.isSelected());
                        if (t1) {
                            enableButton.setText(I18n.getInstance().getString("extension.calc.button.toggle.activate"));
                            editConfigPane.setDisable(false);
                        } else {
                            enableButton.setText(I18n.getInstance().getString("extension.calc.button.toggle.deactivate"));
                            editConfigPane.setDisable(true);
                        }
                        _enabledChanged.setValue(true);
                    } catch (Exception ex) {
                        logger.fatal(ex);
                    }
                });

            } catch (Exception ex) {
                logger.fatal(ex);
            }

            Label label = new Label(I18n.getInstance().getString("plugin.scada.element.setting.label.lowerlimit.enable"));
            Label labelOutput = new Label(I18n.getInstance().getString("plugin.object.calc.output"));

            FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL, 8, 12, label, enableButton, labelOutput, buttonOutput);

            VBox vbox = new VBox(8, flowPane, editConfigPane);
//            ap.getChildren().addAll(vbox);
            view.setCenter(vbox);
        } catch (Exception e) {
            logger.fatal(e);
        }


    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public boolean needSave() {
        return _changed.getValue() || _enabledChanged.getValue();
//        return _changed.getValue();
    }

    @Override
    public void dismissChanges() {
        _changed.setValue(false);
    }

    @Override
    public boolean save() {
        try {
            _changed.setValue(!control.getFormel().equals(oldExpression));
            if (needSave()) {
                String newExpression = control.getFormel();
                JEVisAttribute aExprsssion = _obj.getAttribute("Expression");

                JEVisSample newSample = aExprsssion.buildSample(new DateTime(), newExpression);
                newSample.commit();
                oldExpression = newExpression;
                _changed.setValue(false);


                _newSampleEnabled.commit();
                _enabledChanged.setValue(false);
            }
            return true;
        } catch (Exception ex) {
            logger.fatal(ex);
        }
        return false;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }
}
