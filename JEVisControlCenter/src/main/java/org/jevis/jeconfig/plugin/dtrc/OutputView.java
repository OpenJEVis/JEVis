package org.jevis.jeconfig.plugin.dtrc;

import com.ibm.icu.text.NumberFormat;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXTimePicker;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.joda.time.DateTime;
import org.mariuszgromada.math.mxparser.Expression;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class OutputView extends Tab {
    private static final Logger logger = LogManager.getLogger(OutputView.class);
    private static final String NO_RESULT = I18n.getInstance().getString("plugin.dtrc.noresult");
    private final NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
    private final JEVisDataSource ds;
    private final JFXDatePicker startDate = new JFXDatePicker(LocalDate.now());
    private final JFXDatePicker endDate = new JFXDatePicker(LocalDate.now());
    private final JFXTimePicker startTime = new JFXTimePicker(LocalTime.of(0, 0, 0));
    private final JFXTimePicker endTime = new JFXTimePicker(LocalTime.of(23, 59, 59));
    private final TemplateHandler templateHandler;
    private final GridPane gridPane = new GridPane();
    private final SimpleBooleanProperty showInputs = new SimpleBooleanProperty(false);
    private final VBox viewVBox;
    private final FlowPane viewInputs = new FlowPane(4, 4);

    public OutputView(String title, JEVisDataSource ds, TemplateHandler templateHandler) {
        super(title);
        this.ds = ds;
        this.templateHandler = templateHandler;
        setClosable(false);

        gridPane.setPadding(new Insets(4));
        gridPane.setVgap(6);
        gridPane.setHgap(6);

        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        startDate.setPrefWidth(120d);
        endDate.setPrefWidth(120d);

        startTime.setPrefWidth(100d);
        startTime.setMaxWidth(100d);
        startTime.set24HourView(true);
        startTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        endTime.setPrefWidth(100d);
        endTime.setMaxWidth(100d);
        endTime.set24HourView(true);
        endTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        startTime.valueProperty().addListener((observable, oldValue, newValue) -> update());
        endTime.valueProperty().addListener((observable, oldValue, newValue) -> update());
        startDate.valueProperty().addListener((observable, oldValue, newValue) -> update());
        endDate.valueProperty().addListener((observable, oldValue, newValue) -> update());

        IntervalSelector intervalSelector = new IntervalSelector(ds, startDate, startTime, endDate, endTime);

        GridPane datePane = new GridPane();
        datePane.setPadding(new Insets(4));
        datePane.setHgap(6);
        datePane.setVgap(6);

        Label startText = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate") + "  ");
        Label endText = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));

        datePane.add(startText, 0, 0);
        datePane.add(startDate, 1, 0);
        datePane.add(startTime, 2, 0);

        datePane.add(endText, 0, 1);
        datePane.add(endDate, 1, 1);
        datePane.add(endTime, 2, 1);

        HBox dateBox = new HBox(4, intervalSelector, datePane);

        Separator separator1 = new Separator(Orientation.HORIZONTAL);
        separator1.setPadding(new Insets(8, 0, 8, 0));
        Separator separator2 = new Separator(Orientation.HORIZONTAL);
        separator2.setPadding(new Insets(8, 0, 8, 0));

        Label inputsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.view.input"));
        inputsLabel.setPadding(new Insets(8, 0, 8, 0));
        inputsLabel.setFont(new Font(18));

        final Label outputsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.view.output"));
        outputsLabel.setPadding(new Insets(8, 0, 8, 0));
        outputsLabel.setFont(new Font(18));

        viewVBox = new VBox(4,
                dateBox, separator1,
                outputsLabel, gridPane);
        viewVBox.setPadding(new Insets(12));

        setContent(viewVBox);

        showInputs.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                viewVBox.getChildren().setAll(
                        dateBox, separator1,
                        inputsLabel, viewInputs, separator2,
                        outputsLabel, gridPane);
                setContent(viewVBox);
            } else {
                viewVBox.getChildren().setAll(
                        dateBox, separator1,
                        outputsLabel, gridPane);
            }
        });
    }

    public void update() {
        Platform.runLater(() -> gridPane.getChildren().clear());

        DateTime start = new DateTime(startDate.getValue().getYear(), startDate.getValue().getMonthValue(), startDate.getValue().getDayOfMonth(),
                startTime.getValue().getHour(), startTime.getValue().getMinute(), startTime.getValue().getSecond());
        DateTime end = new DateTime(endDate.getValue().getYear(), endDate.getValue().getMonthValue(), endDate.getValue().getDayOfMonth(),
                endTime.getValue().getHour(), endTime.getValue().getMinute(), endTime.getValue().getSecond());

        for (TemplateOutput templateOutput : templateHandler.getRcTemplate().getTemplateOutputs()) {

            Label label = new Label(templateOutput.getName());
            if (templateOutput.getNameBold()) {
                label.setFont(Font.font(label.getFont().getFamily(), FontWeight.BOLD, label.getFont().getSize()));
            }
            Label result = new Label();
            result.setTextAlignment(TextAlignment.RIGHT);
            if (templateOutput.getResultBold()) {
                result.setFont(Font.font(result.getFont().getFamily(), FontWeight.BOLD, result.getFont().getSize()));
            }
            HBox hBox = new HBox(label, result);

            Task<String> task = new Task<String>() {
                @Override
                protected String call() {
                    String result = NO_RESULT;

                    TemplateFormula formula = templateHandler.getRcTemplate().getTemplateFormulas().stream().filter(templateFormula -> templateFormula.getOutput().equals(templateOutput.getVariableName())).findFirst().orElse(null);

                    if (formula != null) {
                        linkInputs(formula, templateHandler.getRcTemplate().getTemplateInputs());
                        String formulaString = formula.getFormula();
                        boolean isText = false;
                        for (TemplateInput templateInput : formula.getInputs()) {
                            try {
                                if (templateInput.getVariableType().equals(InputVariableType.STRING.toString())) {
                                    isText = true;
                                }

                                formulaString = formulaString.replace(templateInput.getVariableName(), templateInput.getValue(ds, start, end));

                            } catch (JEVisException e) {
                                logger.error("Could not get template input value for {}", templateInput.getVariableName(), e);
                            }
                        }

                        if (!isText) {
                            Expression expression = new Expression(formulaString);
                            result = nf.format(expression.calculate()) + " " + templateOutput.getUnit();
                        } else result = formulaString;
                    } else result = "";

                    return result;
                }
            };

            task.setOnSucceeded(event -> Platform.runLater(() -> {
                try {
                    result.setText(task.get());
                } catch (InterruptedException e) {
                    logger.error("InterruptedException", e);
                } catch (ExecutionException e) {
                    logger.error("ExecutionException", e);
                }
            }));

            JEConfig.getStatusBar().addTask(TRCPlugin.class.getSimpleName(), task, null, true);

            Platform.runLater(() -> gridPane.add(hBox, templateOutput.getColumn(), templateOutput.getRow(), templateOutput.getColSpan(), templateOutput.getRowSpan()));
        }
    }

    private void linkInputs(TemplateFormula formula, List<TemplateInput> templateInputs) {
        for (TemplateInput templateInput : formula.getInputs()) {
            templateInputs.stream().filter(input1 -> templateInput.getVariableName().equals(input1.getVariableName())).findFirst().ifPresent(templateInput::clone);
        }
    }

    public void updateViewInputFlowPane() {
        viewInputs.getChildren().clear();

        Map<JEVisClass, List<TemplateInput>> groupedInputsMap = new HashMap<>();
        List<TemplateInput> ungroupedInputs = new ArrayList<>();
        for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
            if (templateInput.getGroup() == null || templateInput.getGroup()) {
                JEVisClass jeVisClass = null;
                try {
                    jeVisClass = ds.getJEVisClass(templateInput.getObjectClass());
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                if (groupedInputsMap.get(jeVisClass) == null) {
                    List<TemplateInput> list = new ArrayList<>();
                    list.add(templateInput);
                    groupedInputsMap.put(jeVisClass, list);
                } else {
                    groupedInputsMap.get(jeVisClass).add(templateInput);
                }
            } else ungroupedInputs.add(templateInput);
        }


        for (Map.Entry<JEVisClass, List<TemplateInput>> templateInput : groupedInputsMap.entrySet()) {
            JEVisClass inputClass = templateInput.getKey();
            List<TemplateInput> groupedInputs = templateInput.getValue();
            String className = null;
            try {
                className = I18nWS.getInstance().getClassName(inputClass);
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            Label label = new Label(className);
            label.setAlignment(Pos.CENTER);
            List<JEVisObject> objects = null;
            try {
                objects = ds.getObjects(ds.getJEVisClass(inputClass.getName()), false);
                List<JEVisObject> filteredObjects = new ArrayList<>();
                String filter = groupedInputs.get(0).getFilter();
                for (JEVisObject jeVisObject : objects) {
                    String objectName = TRCPlugin.getRealName(jeVisObject);

                    if (filter != null && filter.contains(" ")) {
                        String[] result = filter.split(" ");
                        String string = objectName.toLowerCase();
                        boolean[] results = new boolean[result.length];
                        for (int i = 0, resultLength = result.length; i < resultLength; i++) {
                            String value = result[i];
                            String subString = value.toLowerCase();
                            results[i] = string.contains(subString);
                        }

                        boolean allFound = true;
                        for (boolean b : results) {
                            if (!b) {
                                allFound = false;
                                break;
                            }
                        }
                        if (allFound) {
                            filteredObjects.add(jeVisObject);
                        }

                    } else if (filter != null) {
                        String string = objectName.toLowerCase();
                        if (string.contains(filter.toLowerCase())) {
                            filteredObjects.add(jeVisObject);
                        }
                    } else filteredObjects.add(jeVisObject);
                }

                objects = filteredObjects;
            } catch (JEVisException e) {
                logger.error("Could not get JEVisClass {}", className, e);
            }
            JFXComboBox<JEVisObject> objectSelector = new JFXComboBox<>(FXCollections.observableArrayList(objects));
            Callback<ListView<JEVisObject>, ListCell<JEVisObject>> attributeCellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
                @Override
                public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                    return new JFXListCell<JEVisObject>() {
                        @Override
                        protected void updateItem(JEVisObject obj, boolean empty) {
                            super.updateItem(obj, empty);
                            if (obj == null || empty) {
                                setGraphic(null);
                                setText(null);
                            } else {
                                try {
                                    if (!obj.getJEVisClassName().equals("Clean Data")) {
                                        setText(obj.getName());
                                    } else {
                                        setText(TRCPlugin.getRealName(obj));
                                    }
                                } catch (JEVisException e) {
                                    logger.error("Could not get JEVisClass of object {}:{}", obj.getName(), obj.getID(), e);
                                }
                            }
                        }
                    };
                }
            };

            objectSelector.setCellFactory(attributeCellFactory);
            objectSelector.setButtonCell(attributeCellFactory.call(null));

            objectSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    groupedInputs.forEach(templateInput1 -> templateInput1.setObjectID(newValue.getID()));
                    update();
                }
            });

            VBox vBox = new VBox(label);
            vBox.setAlignment(Pos.CENTER);
            HBox templateBox = new HBox(4, vBox, objectSelector);
            viewInputs.getChildren().add(templateBox);

            Platform.runLater(() -> objectSelector.getSelectionModel().selectFirst());
        }
    }

    public void showInputs(boolean show) {
        showInputs.set(show);
    }
}
