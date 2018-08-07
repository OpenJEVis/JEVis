package org.jevis.jeconfig.plugin.object.extension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.controlsfx.control.ToggleSwitch;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.attribute.BooleanValueEditor;
import org.jevis.jeconfig.plugin.object.extension.calculation.CalculationViewController;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CalculationExtension implements ObjectEditorExtension {

    public static final String CALC_CLASS_NAME = "Calculation";
    private static final String TITLE = I18n.getInstance().getString("plugin.object.calc.title");
    private final org.apache.logging.log4j.Logger log = LogManager.getLogger(CalculationExtension.class);
    private final BorderPane view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _enabledChanged = new SimpleBooleanProperty(false);
    private JEVisObject _obj;
    private CalculationViewController contol;
    private String oldExpression = "";
    private JEVisSample lastSampleEnabeld = null;
    private JEVisSample _newSampleEnabeld = null;

    public CalculationExtension(JEVisObject _obj) {
        this._obj = _obj;
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        Boolean isCalcObject = false;
        try {
            isCalcObject = obj.getJEVisClassName().equals(CALC_CLASS_NAME);
        } catch (JEVisException e) {
            log.error("Could not get object type" + e.getLocalizedMessage());
        }
        return isCalcObject;
    }


    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void setVisible() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 0, 20, 20));
        gridPane.setHgap(7);
        gridPane.setVgap(7);

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(gridPane);

        AnchorPane ap = new AnchorPane();

        //    Button button = new Button();
        //  button.setText("Calc");
        //ap.getChildren().add(button);

//         = new Pane();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/EditCalculation.fxml"));
        fxmlLoader.setResources(I18n.getInstance().getBundle());
        //fxmlLoader.setRoot();
        //fxmlLoader.setController(new CalculationViewController());
        try {
            final Pane editConfigPane = fxmlLoader.load();
            contol = fxmlLoader.<CalculationViewController>getController();
            contol.setData(_obj);

            JEVisAttribute aExprsssion = _obj.getAttribute("Expression");
            JEVisSample lastValue = aExprsssion.getLatestSample();

            if (lastValue != null) {
                System.out.println("LastSample: " + lastValue.getTimestamp() + " " + lastValue.getValueAsString());
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
                        enableButton.setText(I18n.getInstance().getString("button.toggle.activate"));
                    } else {
                        enableButton.setText(I18n.getInstance().getString("button.toggle.deactivate"));
                    }


                } else {
                    enableButton.setSelected(false);//TODO: get default Value
                    enableButton.setText(I18n.getInstance().getString("button.toggle.deactivate"));
                }


                enableButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                        try {
                            _newSampleEnabeld = enabled.buildSample(new DateTime(), enableButton.isSelected());
                            if (t1) {
                                enableButton.setText(I18n.getInstance().getString("button.toggle.activate"));
                                editConfigPane.setDisable(false);
                            } else {
                                enableButton.setText(I18n.getInstance().getString("button.toggle.deactivate"));
                                editConfigPane.setDisable(true);
                            }
                            _enabledChanged.setValue(true);
                        } catch (Exception ex) {
                            Logger.getLogger(BooleanValueEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Label label = new Label("Aktiviert:");

            FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL,8,12,label,enableButton);
            VBox vbox = new VBox(8, flowPane, editConfigPane);
            ap.getChildren().addAll(vbox);

        } catch (Exception e) {
            e.printStackTrace();
        }


        view.setCenter(new ScrollPane(ap));

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
            _changed.setValue(!contol.getFormel().equals(oldExpression));
            if (needSave()) {
                String newExpression = contol.getFormel();
                JEVisAttribute aExprsssion = _obj.getAttribute("Expression");

                JEVisSample newSample = aExprsssion.buildSample(new DateTime(), newExpression);
                newSample.commit();
                oldExpression = newExpression;
                _changed.setValue(false);


                _newSampleEnabeld.commit();
                _enabledChanged.setValue(false);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }
}
