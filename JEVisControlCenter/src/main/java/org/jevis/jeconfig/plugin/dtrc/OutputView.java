package org.jevis.jeconfig.plugin.dtrc;

import com.ibm.icu.text.NumberFormat;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXTimePicker;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
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
import org.jevis.jeconfig.plugin.accounting.SelectionTemplate;
import org.joda.time.DateTime;
import org.mariuszgromada.math.mxparser.Expression;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private final SimpleBooleanProperty showInputs = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty showDatePicker = new SimpleBooleanProperty(true);
    private final VBox viewVBox;
    private final GridPane viewInputs = new GridPane();
    private SelectionTemplate selectionTemplate;
    private final IntervalSelector intervalSelector;
    private final ConcurrentHashMap<String, Double> resultMap = new ConcurrentHashMap<>();

    public OutputView(String title, JEVisDataSource ds, TemplateHandler templateHandler) {
        super(title);
        this.ds = ds;
        this.templateHandler = templateHandler;
        setClosable(false);

        gridPane.setPadding(new Insets(4));
        gridPane.setVgap(6);
        gridPane.setHgap(6);

        viewInputs.setPadding(new Insets(4));
        viewInputs.setVgap(6);
        viewInputs.setHgap(6);

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


        ChangeListener<LocalTime> updateTimeListener = new ChangeListener<LocalTime>() {
            @Override
            public void changed(ObservableValue<? extends LocalTime> observable, LocalTime oldValue, LocalTime newValue) {
                requestUpdate();
            }
        };
        startTime.valueProperty().addListener(updateTimeListener);
        endTime.valueProperty().addListener(updateTimeListener);
        ChangeListener<? super LocalDate> updateDateListener = new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                requestUpdate();
            }
        };
        startDate.valueProperty().addListener(updateDateListener);
        endDate.valueProperty().addListener(updateDateListener);

        intervalSelector = new IntervalSelector(ds, startDate, startTime, endDate, endTime);

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
                inputsLabel, viewInputs, separator2,
                outputsLabel, gridPane);
        viewVBox.setPadding(new Insets(12));

        setContent(viewVBox);

        showDatePicker.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                dateBox.getChildren().setAll(intervalSelector, datePane);
            } else {
                dateBox.getChildren().setAll(intervalSelector);
            }
        });

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

    public DateTime getStart() {
        return new DateTime(startDate.getValue().getYear(), startDate.getValue().getMonthValue(), startDate.getValue().getDayOfMonth(),
                startTime.getValue().getHour(), startTime.getValue().getMinute(), startTime.getValue().getSecond());
    }

    public DateTime getEnd() {
        return new DateTime(endDate.getValue().getYear(), endDate.getValue().getMonthValue(), endDate.getValue().getDayOfMonth(),
                endTime.getValue().getHour(), endTime.getValue().getMinute(), endTime.getValue().getSecond());
    }

    public void requestUpdate() {

        Platform.runLater(() -> gridPane.getChildren().clear());

        DateTime start = getStart();
        DateTime end = getEnd();

        //Sort outputs by formula
        List<TemplateOutput> templateOutputs = templateHandler.getRcTemplate().getTemplateOutputs();
        templateOutputs.sort((o1, o2) -> {
            TemplateFormula formulaO1 = null;
            for (TemplateFormula formula : templateHandler.getRcTemplate().getTemplateFormulas()) {
                if (formula.getOutput().equals(o1.getVariableName())) {
                    formulaO1 = formula;
                    break;
                }
            }
            TemplateFormula formulaO2 = null;
            for (TemplateFormula templateFormula : templateHandler.getRcTemplate().getTemplateFormulas()) {
                if (templateFormula.getOutput().equals(o2.getVariableName())) {
                    formulaO2 = templateFormula;
                    break;
                }
            }
            if (formulaO1 != null && formulaO2 == null) {
                return 1;
            } else if (formulaO1 == null && formulaO2 != null) {
                return -1;
            } else if (formulaO1 != null && formulaO2 != null) {
                List<TemplateInput> formulaInputsO1 = new ArrayList<>();
                for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
                    if (formulaO1.getInputIds().contains(templateInput.getId()) && templateInput.getVariableType() != null && templateInput.getVariableType().equals(InputVariableType.FORMULA.toString())) {
                        formulaInputsO1.add(templateInput);
                    }
                }
                List<TemplateInput> formulaInputsO2 = new ArrayList<>();
                for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
                    if (formulaO1.getInputIds().contains(templateInput.getId()) && templateInput.getVariableType() != null && templateInput.getVariableType().equals(InputVariableType.FORMULA.toString())) {
                        formulaInputsO2.add(templateInput);
                    }
                }

                return Integer.compare(formulaInputsO1.size(), formulaInputsO2.size());
            }
            return -1;
        });

        for (TemplateOutput templateOutput : templateOutputs) {
            if (!templateOutput.getSeparator()) {
                Label label = new Label(templateOutput.getName());
                if (templateOutput.getNameBold()) {
                    label.setFont(Font.font(label.getFont().getFamily(), FontWeight.BOLD, label.getFont().getSize()));
                }
                Label result = new Label();
                result.setTextAlignment(TextAlignment.RIGHT);
                result.setAlignment(Pos.CENTER_RIGHT);
                if (templateOutput.getResultBold()) {
                    result.setFont(Font.font(result.getFont().getFamily(), FontWeight.BOLD, result.getFont().getSize()));
                }
                HBox hBox = new HBox(label, result);

                if (templateOutput.getName() == null || templateOutput.getName().equals("")) {
                    hBox.setAlignment(Pos.CENTER_RIGHT);
                }

                if (templateOutput.getVariableName() == null || templateOutput.getVariableName().equals("")) {
                    if (templateOutput.getColSpan() > 1) {
                        hBox.setAlignment(Pos.CENTER);
                    }
                }

                Task<String> task = new Task<String>() {
                    @Override
                    protected String call() {
                        String result = NO_RESULT;

                        TemplateFormula formula = templateHandler.getRcTemplate().getTemplateFormulas().stream().filter(templateFormula -> templateFormula.getOutput().equals(templateOutput.getId())).findFirst().orElse(null);

                        if (formula != null) {
                            String formulaString = formula.getFormula();
                            boolean isText = false;
                            for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
                                if (formula.getInputIds().contains(templateInput.getId())) {
                                    try {
                                        if (templateInput.getVariableType().equals(InputVariableType.STRING.toString())) {
                                            isText = true;
                                        }

                                        if (!templateInput.getVariableType().equals(InputVariableType.FORMULA.toString())) {
                                            formulaString = formulaString.replace(templateInput.getVariableName(), templateInput.getValue(ds, start, end));
                                        } else {
                                            Double d = resultMap.get(templateInput.getVariableName());
                                            if (d != null) {
                                                formulaString = formulaString.replace(templateInput.getVariableName(), d.toString());
                                            }
                                        }

                                    } catch (JEVisException e) {
                                        logger.error("Could not get template input value for {}", templateInput.getVariableName(), e);
                                    }
                                }
                            }

                            if (!isText) {
                                try {
                                    Expression expression = new Expression(formulaString);
                                    Double calculate = expression.calculate();
                                    if (!calculate.isNaN()) {
                                        resultMap.put(formula.getName(), calculate);
                                    }
                                    result = nf.format(calculate) + " " + templateOutput.getUnit();
                                    succeeded();
                                } catch (Exception e) {
                                    logger.error("Error in formula {}", formula.getName(), e);
                                }
                            } else {
                                result = formulaString;
                                succeeded();
                            }
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

                JEConfig.getStatusBar().addTask(OutputView.class.getSimpleName(), task, null, true);

                Platform.runLater(() -> gridPane.add(hBox, templateOutput.getColumn(), templateOutput.getRow(), templateOutput.getColSpan(), templateOutput.getRowSpan()));
            } else {
                Separator separator = new Separator();
                if (templateOutput.getColSpan() > 1) {
                    separator.setPadding(new Insets(8, 0, 8, 0));
                    separator.setOrientation(Orientation.HORIZONTAL);
                }
                if (templateOutput.getRowSpan() > 1) {
                    separator.setPadding(new Insets(0, 8, 0, 8));
                    separator.setOrientation(Orientation.VERTICAL);
                }

                Platform.runLater(() -> gridPane.add(separator, templateOutput.getColumn(), templateOutput.getRow(), templateOutput.getColSpan(), templateOutput.getRowSpan()));
            }
        }
    }

    public void updateViewInputFlowPane() {
        viewInputs.getChildren().clear();
        if (selectionTemplate != null) {
            templateHandler.getRcTemplate().getTemplateInputs().stream().filter(templateInput -> !selectionTemplate.getSelectedInputs().contains(templateInput)).forEach(templateInput -> selectionTemplate.getSelectedInputs().add(templateInput));
        }

        Map<JEVisClass, List<TemplateInput>> groupedInputsMap = new HashMap<>();
        List<TemplateInput> ungroupedInputs = new ArrayList<>();
        for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
            if (((templateInput.getGroup() == null || templateInput.getGroup()) && templateInput.getVariableType() != null) && !templateInput.getVariableType().equals(InputVariableType.FORMULA.toString())) {
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
            } else if (templateInput.getVariableType() != null && !templateInput.getVariableType().equals(InputVariableType.FORMULA.toString())) {
                ungroupedInputs.add(templateInput);
            }
        }

        int row = 0;
        int column = 0;
        for (TemplateInput ungroupedInput : ungroupedInputs) {
            int index = ungroupedInputs.indexOf(ungroupedInput);
            Label label = new Label(I18nWS.getInstance().getClassName(ungroupedInput.getObjectClass()));
            label.setAlignment(Pos.CENTER_LEFT);
            VBox labelBox = new VBox(label);
            labelBox.setAlignment(Pos.CENTER);

            if (index == 4 || (index > 4 && index % 4 == 0)) {
                column = 0;
                row++;
            }

            try {
                JFXComboBox<JEVisObject> objectSelector = createObjectSelector(Collections.singletonList(ds.getObject(ungroupedInput.getObjectID())));

                if (selectionTemplate != null && selectionTemplate.getSelectedInputs().contains(ungroupedInput)) {
                    TemplateInput found = null;
                    for (TemplateInput templateInput : selectionTemplate.getSelectedInputs()) {
                        if (templateInput.equals(ungroupedInput))
                            found = templateInput;
                        break;
                    }
                    if (found != null) {
                        try {
                            JEVisObject selectedObject = ds.getObject(found.getObjectID());
                            objectSelector.getSelectionModel().select(selectedObject);
                        } catch (JEVisException e) {
                            logger.error("Could not get object {}", found.getVariableName());
                            objectSelector.getSelectionModel().selectFirst();
                        }
                    } else {
                        objectSelector.getSelectionModel().selectFirst();
                    }
                } else {
                    objectSelector.getSelectionModel().selectFirst();
                }

                if (objectSelector.getSelectionModel().getSelectedItem() != null) {
                    ungroupedInput.setObjectID(objectSelector.getSelectionModel().getSelectedItem().getID());
                }

                objectSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(oldValue)) {
                        requestUpdate();
                    }
                });

                int finalColumn = column;
                int finalRow = row;
                Platform.runLater(() -> {
                    removeNode(finalRow, finalColumn, viewInputs);
                    removeNode(finalRow, finalColumn + 1, viewInputs);
                    viewInputs.add(labelBox, finalColumn, finalRow);
                    viewInputs.add(objectSelector, finalColumn + 1, finalRow);
                });
                column += 2;
            } catch (JEVisException e) {
                logger.error("Could not get object selector for template input {}", ungroupedInput, e);
            }
        }

        int idx = 0;
        for (Map.Entry<JEVisClass, List<TemplateInput>> templateInput : groupedInputsMap.entrySet()) {
            if (idx == 4 || (idx > 4 && idx % 4 == 0)) {
                column = 0;
                row++;
            }
            idx++;
            JEVisClass inputClass = templateInput.getKey();
            List<TemplateInput> groupedInputs = templateInput.getValue();
            String className = null;
            try {
                className = I18nWS.getInstance().getClassName(inputClass);
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            Label label = new Label(className);
            label.setAlignment(Pos.CENTER_LEFT);
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
            JFXComboBox<JEVisObject> objectSelector = createObjectSelector(objects);

            if (selectionTemplate != null && selectionTemplate.getSelectedInputs().contains(groupedInputs.get(0))) {
                TemplateInput found = selectionTemplate.getSelectedInputs().stream().filter(ti -> ti.equals(groupedInputs.get(0))).findFirst().orElse(null);
                if (found != null) {
                    try {
                        JEVisObject selectedObject = ds.getObject(found.getObjectID());
                        objectSelector.getSelectionModel().select(selectedObject);
                    } catch (JEVisException e) {
                        logger.error("Could not get object {}", found.getVariableName());
                        objectSelector.getSelectionModel().selectFirst();
                    }
                } else {
                    objectSelector.getSelectionModel().selectFirst();
                }
            } else {
                objectSelector.getSelectionModel().selectFirst();
            }

            if (objectSelector.getSelectionModel().getSelectedItem() != null) {
                groupedInputs.forEach(templateInput1 -> templateInput1.setObjectID(objectSelector.getSelectionModel().getSelectedItem().getID()));
            }

            objectSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    groupedInputs.forEach(templateInput1 -> templateInput1.setObjectID(newValue.getID()));
                    requestUpdate();
                }
            });

            int finalColumn1 = column;
            int finalRow1 = row;
            Platform.runLater(() -> {
                removeNode(finalRow1, finalColumn1, viewInputs);
                removeNode(finalRow1, finalColumn1 + 1, viewInputs);
                viewInputs.add(label, finalColumn1, finalRow1);
                viewInputs.add(objectSelector, finalColumn1 + 1, finalRow1);
            });
            column += 2;
        }
    }

    public void removeNode(final int row, final int column, GridPane gridPane) {
        ObservableList<Node> children = gridPane.getChildren();
        for (Node node : children) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                gridPane.getChildren().remove(node);
                break;
            }
        }
    }

    private JFXComboBox<JEVisObject> createObjectSelector(List<JEVisObject> objects) {
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

        return objectSelector;
    }

    public void showInputs(boolean show) {
        showInputs.set(show);
    }

    public void showDatePicker(boolean show) {
        showDatePicker.set(show);
    }

    public GridPane getViewInputs() {
        return viewInputs;
    }

    public void setSelectionTemplate(SelectionTemplate selectionTemplate) {
        this.selectionTemplate = selectionTemplate;
    }

    public IntervalSelector getIntervalSelector() {
        return intervalSelector;
    }
}
