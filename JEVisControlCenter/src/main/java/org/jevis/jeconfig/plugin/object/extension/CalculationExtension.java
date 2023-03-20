package org.jevis.jeconfig.plugin.object.extension;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;
import org.jevis.api.*;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.extension.calculation.CalculationViewController;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CalculationExtension implements ObjectEditorExtension {

    private static final String CALC_CLASS_NAME = "Calculation";
    private static final String TITLE = I18n.getInstance().getString("plugin.object.calc.title");
    private static final Logger logger = LogManager.getLogger(CalculationExtension.class);
    private final BorderPane view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _enabledChanged = new SimpleBooleanProperty(false);
    private final JEVisObject _obj;
    private CalculationViewController control;
    private String oldExpression = "";
    private final JEVisSample lastSampleEnabeld = null;
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


        //    JFXButton button = new JFXButton();
        //  button.setText("Calc");
        //ap.getChildren().add(button);

//         = new Pane();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/EditCalculation.fxml"));
        fxmlLoader.setResources(I18n.getInstance().getBundle());
        //fxmlLoader.setRoot();
        //fxmlLoader.setController(new CalculationViewController());
        try {
            JFXButton buttonOutput = new JFXButton(I18n.getInstance().getString("plugin.object.calc.output"));
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

            JFXButton calcNowButton = new JFXButton(I18n.getInstance().getString("plugin.object.calc.recalc"));
            calcNowButton.setOnAction(bEvent -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(I18n.getInstance().getString("plugin.object.calc.recalc"));
                alert.setHeaderText(I18n.getInstance().getString("plugin.object.calc.recalc.question"));
                TopMenu.applyActiveTheme(alert.getDialogPane().getScene());

                GridPane gp = new GridPane();
                gp.setHgap(6);
                gp.setVgap(6);
                final ToggleGroup toggleGroup = new ToggleGroup();

                JFXRadioButton allRadioButton = new JFXRadioButton(I18n.getInstance().getString("plugin.object.report.dialog.period.all"));
                allRadioButton.setSelected(true);
                allRadioButton.setToggleGroup(toggleGroup);

                JFXRadioButton nowRadioButton = new JFXRadioButton(I18n.getInstance().getString("graph.datehelper.referencepoint.now"));
                nowRadioButton.setToggleGroup(toggleGroup);

                JFXRadioButton fromRadioButton = new JFXRadioButton(I18n.getInstance().getString("plugin.graph.dialog.export.from"));
                fromRadioButton.setToggleGroup(toggleGroup);
                JFXDatePicker fromDatePicker = new JFXDatePicker();
                fromDatePicker.setValue(LocalDate.now());
                fromDatePicker.setPrefWidth(120d);
                JFXTimePicker fromTimePicker = new JFXTimePicker();
                fromTimePicker.setValue(LocalTime.now());
                fromTimePicker.setPrefWidth(100d);
                fromTimePicker.setMaxWidth(100d);
                fromTimePicker.set24HourView(true);
                fromTimePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

                gp.add(allRadioButton, 0, 0);
                gp.add(nowRadioButton, 1, 0);
                gp.add(fromRadioButton, 2, 0);
                gp.add(fromDatePicker, 3, 0);
                gp.add(fromTimePicker, 4, 0);

                alert.getDialogPane().setContent(gp);

                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType.equals(ButtonType.OK)) {
                        final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("plugin.object.cleandata.reclean.title") + "...");
                        boolean all = allRadioButton.isSelected();
                        boolean now = nowRadioButton.isSelected();
                        boolean from = fromRadioButton.isSelected();
                        DateTime fromDate = new DateTime(fromDatePicker.getValue().getYear(), fromDatePicker.getValue().getMonthValue(), fromDatePicker.getValue().getDayOfMonth(),
                                fromTimePicker.getValue().getHour(), fromTimePicker.getValue().getMinute(), fromTimePicker.getValue().getSecond(), 0);

                        StringProperty errorMsg = new SimpleStringProperty();
                        logger.debug("Setting default timezone to UTC");
                        TimeZone defaultTimeZone = TimeZone.getDefault();
                        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                        DateTimeZone defaultDateTimeZone = DateTimeZone.getDefault();
                        DateTimeZone.setDefault(DateTimeZone.UTC);

                        Task<Void> set = new Task<Void>() {
                            @Override
                            protected Void call() {
                                try {
                                    boolean wasEnabled = false;
                                    if (_obj.getAttribute("Enabled").hasSample()) {
                                        wasEnabled = _obj.getAttribute("Enabled").getLatestSample().getValueAsBoolean();
                                    }
                                    _obj.getAttribute("Enabled").buildSample(new DateTime(), false).commit();

                                    JEVisClass output = _obj.getDataSource().getJEVisClass("Output");
                                    List<JEVisObject> outputs = _obj.getChildren(output, true);
                                    List<JEVisObject> targets = new ArrayList<>();
                                    for (JEVisObject jeVisObject : outputs) {
                                        try {
                                            JEVisAttribute attribute = jeVisObject.getAttribute("Output");
                                            if (attribute != null && attribute.hasSample()) {
                                                TargetHelper th = new TargetHelper(_obj.getDataSource(), attribute);
                                                targets.addAll(th.getObject());
                                                for (JEVisObject dataObject : th.getObject()) {
                                                    if (all) {
                                                        CommonMethods.deleteAllSamples(dataObject, true, true);
                                                    } else if (from) {
                                                        CommonMethods.deleteAllSamples(dataObject, fromDate, null, true, true);
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            logger.error("Error with output {}:{}", jeVisObject, e);
                                        }
                                    }

                                    CalcJob calcJob;
                                    CalcJobFactory calcJobCreator = new CalcJobFactory();
                                    do {
                                        calcJob = calcJobCreator.getCurrentCalcJob(new SampleHandler(), _obj.getDataSource(), _obj);
                                        calcJob.execute();
                                    } while (!calcJob.hasProcessedAllInputSamples());

                                    if (wasEnabled) {
                                        _obj.getAttribute("Enabled").buildSample(new DateTime(), true).commit();
                                    }

                                    for (JEVisObject jeVisObject : targets) {
                                        for (JEVisObject jeVisObject1 : jeVisObject.getChildren()) {
                                            if (all) {
                                                CommonMethods.processAllCleanData(jeVisObject1, null, null);
                                            } else if (from)
                                                CommonMethods.processAllCleanData(jeVisObject1, fromDate, null);
                                            else {
                                                CommonMethods.processAllCleanDataNoDelete(jeVisObject1);
                                            }
                                        }
                                    }

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    errorMsg.set(ex.getMessage());
                                    this.failed();
                                }
                                return null;
                            }
                        };
                        set.setOnSucceeded(event -> {
                            pForm.getDialogStage().close();
                            restoreTimeZone(defaultTimeZone, defaultDateTimeZone);
                        });

                        set.setOnCancelled(event -> {
                            logger.debug("Setting all multiplier and differential switches cancelled");
                            pForm.getDialogStage().hide();
                            restoreTimeZone(defaultTimeZone, defaultDateTimeZone);
                        });

                        set.setOnFailed(event -> {
                            logger.debug("Setting all multiplier and differential switches failed");
                            pForm.getDialogStage().hide();

                            Alert error = new Alert(Alert.AlertType.ERROR);
                            error.setHeaderText(I18n.getInstance().getString("plugin.object.calc.recalc.title"));
                            TopMenu.applyActiveTheme(error.getDialogPane().getScene());
                            error.setContentText(errorMsg.get());
                            restoreTimeZone(defaultTimeZone, defaultDateTimeZone);
                        });

                        pForm.activateProgressBar(set);
                        pForm.getDialogStage().show();

                        new Thread(set).start();
                    }
                });
            });


            FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL, 8, 12, label, enableButton, labelOutput, buttonOutput);
            if (JEConfig.getExpert()) {
                flowPane.getChildren().add(calcNowButton);
            }


            VBox vbox = new VBox(8, flowPane, editConfigPane);
            view.setCenter(vbox);
        } catch (Exception e) {
            logger.fatal(e);
        }


    }

    private void restoreTimeZone(TimeZone defaultTimeZone, DateTimeZone defaultDateTimeZone) {
        logger.debug("Setting default timezone to old timezone {}", defaultTimeZone.getID());
        TimeZone.setDefault(defaultTimeZone);
        DateTimeZone.setDefault(defaultDateTimeZone);
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
            _changed.setValue(!control.getFormula().equals(oldExpression));
            if (needSave()) {
                String newExpression = control.getFormula();
                JEVisAttribute expression = _obj.getAttribute("Expression");

                JEVisSample newSample = expression.buildSample(new DateTime(), newExpression);
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
