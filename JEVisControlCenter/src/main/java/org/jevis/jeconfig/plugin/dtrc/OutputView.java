package org.jevis.jeconfig.plugin.dtrc;

import com.ibm.icu.text.NumberFormat;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartTools;
import org.jevis.jeconfig.application.Chart.data.ValueWithDateTime;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.control.AnalysisLinkButton;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.type.GUIConstants;
import org.jevis.jeconfig.dialog.EnterDataDialog;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.accounting.SelectionTemplate;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.object.attribute.AttributeEditor;
import org.jevis.jeconfig.plugin.object.attribute.RangingValueEditor;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mariuszgromada.math.mxparser.Expression;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OutputView extends Tab {
    private static final Logger logger = LogManager.getLogger(OutputView.class);
    private static final String NO_RESULT = I18n.getInstance().getString("plugin.dtrc.noresult");
    private static final Insets INSETS = new Insets(12);
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
    private final IntervalSelector intervalSelector;
    private final ConcurrentHashMap<String, Double> resultMap = new ConcurrentHashMap<>();
    private final AlphanumComparator alphanumComparator = new AlphanumComparator();
    private final ObjectRelations objectRelations;
    private final GridPane headerGP = new GridPane();
    private final HBox dateBox;
    private final DateTimeFormatter dtfOutLegend = DateTimeFormat.forPattern("EE. dd.MM.yyyy HH:mm");
    private StackPane contractsDialogContainer;
    private StackPane viewDialogContainer;
    private SelectionTemplate selectionTemplate;
    private GridPane contractsGP;
    private Label timeframeField;
    private double fontSize = 12d;

    public OutputView(String title, JEVisDataSource ds, TemplateHandler templateHandler) {
        super(title);
        this.contractsDialogContainer = new StackPane();
        setContent(contractsDialogContainer);
        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);
        this.templateHandler = templateHandler;
        setClosable(false);

        gridPane.setPadding(INSETS);
        gridPane.setVgap(6);
        gridPane.setHgap(6);

        headerGP.setPadding(INSETS);
        headerGP.setVgap(6);
        headerGP.setHgap(6);

        viewInputs.setPadding(INSETS);
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

        intervalSelector = new IntervalSelector(ds, startDate, startTime, endDate, endTime);
        intervalSelector.getTimeFactoryBox().getItems().clear();
        TimeFrameFactory timeFrameFactory = new TimeFrameFactory(ds);
        intervalSelector.getTimeFactoryBox().getItems().addAll(timeFrameFactory.getReduced());
        intervalSelector.updateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                requestUpdate();
                intervalSelector.setUpdate(false);
            }
        });

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

        dateBox = new HBox(4, intervalSelector, datePane);

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

        final Label billLabel = new Label(I18n.getInstance().getString("plugin.accounting.invoice"));
        billLabel.setPadding(new Insets(8, 12, 8, 12));
        billLabel.setFont(new Font(18));

        viewVBox = new VBox(4,
                dateBox, separator1,
                inputsLabel, viewInputs, separator2,
                outputsLabel, gridPane);
        viewVBox.setPadding(new Insets(12));

        ScrollPane scrollPane = new ScrollPane(viewVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        contractsDialogContainer.getChildren().add(scrollPane);

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
                if (!contractsDialogContainer.getChildren().contains(viewVBox)) {
                    contractsDialogContainer.getChildren().add(viewVBox);
                }
            } else {
                viewVBox.getChildren().setAll(headerGP, billLabel, gridPane);
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

        if (templateHandler.getRcTemplate() == null) return;

        Platform.runLater(() -> gridPane.getChildren().clear());
        resultMap.clear();

        DateTime start = getStart();
        DateTime end = getEnd();

        //Sort outputs by formula
        List<TemplateOutput> templateOutputs = templateHandler.getRcTemplate().getTemplateOutputs();
        final List<TemplateFormula> templateFormulas = templateHandler.getRcTemplate().getTemplateFormulas();
        final Map<String, Boolean> intervalConfiguration = templateHandler.getRcTemplate().getIntervalSelectorConfiguration();

        List<TemplateOutput> separatorOutputs = new ArrayList<>();

        List<TemplateOutput> noInputOutputs = new ArrayList<>();
        List<TemplateOutput> singleInputFormulaOutputs = new ArrayList<>();
        List<TemplateOutput> multiInputFormulaOutputs = new ArrayList<>();

        for (TemplateOutput output : templateOutputs) {
            if (output.getSeparator()) {
                separatorOutputs.add(output);
            } else {
                TemplateFormula templateFormula = templateFormulas.stream().filter(formula -> formula.getOutput().equals(output.getId())).findFirst().orElse(null);

                if (templateFormula != null) {
                    boolean foundFormulaInput = templateFormulas.stream().anyMatch(otherFormula -> templateFormula.getInputIds().contains(otherFormula.getId()));

                    if (!foundFormulaInput) {
                        singleInputFormulaOutputs.add(output);
                    } else {
                        multiInputFormulaOutputs.add(output);
                    }
                } else {
                    noInputOutputs.add(output);
                }
            }
        }

        logger.debug("Order of multi input formula outputs before sorting:");
        if (logger.isDebugEnabled()) {
            for (TemplateOutput output : multiInputFormulaOutputs) {
                templateFormulas.forEach(templateFormula -> {
                    if (templateFormula.getOutput().equals(output.getId())) {
                        logger.debug(templateFormula.getName());
                    }
                });
            }
        }

        sortMultiInputFormulaOutputs(multiInputFormulaOutputs, templateOutputs);

        logger.debug("Order of formula outputs after sorting:");
        if (logger.isDebugEnabled()) {
            for (TemplateOutput output : multiInputFormulaOutputs) {
                templateFormulas.forEach(templateFormula -> {
                    if (templateFormula.getOutput().equals(output.getId())) {
                        logger.debug(templateFormula.getName());
                    }
                });
            }
        }

        if (timeframeField != null) {
            String overall = String.format("%s %s %s",
                    dtfOutLegend.print(start),
                    I18n.getInstance().getString("plugin.graph.chart.valueaxis.until"),
                    dtfOutLegend.print(end));

            Platform.runLater(() -> timeframeField.setText(overall));
        }

        List<TemplateOutput> sortedList = new ArrayList<>();
        sortedList.addAll(singleInputFormulaOutputs);
        sortedList.addAll(multiInputFormulaOutputs);

        createOutputs(separatorOutputs);
        createOutputs(noInputOutputs);
        createOutputs(sortedList);

        TimeFrame lastSelectedTimeFrame = intervalSelector.getTimeFactoryBox().getSelectionModel().getSelectedItem();
        List<TimeFrame> inactiveTimeFrames = new ArrayList<>();
        for (TimeFrame item : intervalSelector.getTimeFactoryBox().getItems()) {
            Boolean aBoolean = intervalConfiguration.get(item.getID());
            if (aBoolean == null || !aBoolean) {
                inactiveTimeFrames.add(item);
            }
        }
        TimeFrameFactory timeFrameFactory = new TimeFrameFactory(ds);
        for (TimeFrame timeFrame : timeFrameFactory.getReduced()) {
            if (!intervalSelector.getTimeFactoryBox().getItems().contains(timeFrame)) {
                intervalSelector.getTimeFactoryBox().getItems().add(timeFrame);
            }
        }
        intervalSelector.getTimeFactoryBox().getItems().removeAll(inactiveTimeFrames);

        if (intervalSelector.getTimeFactoryBox().getItems().contains(lastSelectedTimeFrame)) {
            intervalSelector.getTimeFactoryBox().getSelectionModel().select(lastSelectedTimeFrame);
        } else {
            intervalSelector.getTimeFactoryBox().getSelectionModel().selectFirst();
        }
    }

    private List<TemplateFormula> getDependencies(TemplateOutput templateOutput, List<TemplateOutput> multiInputFormulaOutputs) {

        List<TemplateFormula> dependencies = new ArrayList<>();
        TemplateFormula formula = templateHandler.getRcTemplate().getTemplateFormulas().stream().filter(templateFormula -> templateFormula.getOutput().equals(templateOutput.getId())).findFirst().orElse(null);
        if (formula != null) {
            for (TemplateOutput otherOutput : multiInputFormulaOutputs) {
                TemplateFormula otherFormula = templateHandler.getRcTemplate().getTemplateFormulas().stream().filter(templateFormula -> templateFormula.getOutput().equals(otherOutput.getId())).findFirst().orElse(null);
                if (otherFormula != null && formula.getInputIds().contains(otherFormula.getId())) {
                    dependencies.add(otherFormula);

                    dependencies.addAll(getDependencies(otherOutput, multiInputFormulaOutputs));
                }
            }
        }

        return dependencies;
    }

    public void sortMultiInputFormulaOutputs(List<TemplateOutput> multiInputFormulaOutputs, List<TemplateOutput> allFormulaOutputs) {
        Map<TemplateOutput, List<TemplateFormula>> map = new HashMap<>();
        for (TemplateOutput output : multiInputFormulaOutputs) {
            List<TemplateFormula> dependencies = getDependencies(output, allFormulaOutputs);

            map.put(output, dependencies);
        }

        logger.debug("Formula dependencies of outputs:");
        if (logger.isDebugEnabled()) {
            for (Map.Entry<TemplateOutput, List<TemplateFormula>> entry : map.entrySet()) {
                TemplateOutput templateOutput = entry.getKey();
                List<TemplateFormula> templateFormulas = entry.getValue();

                StringBuilder stringBuilder = new StringBuilder();
                TemplateFormula formula = templateHandler.getRcTemplate().getTemplateFormulas().stream().filter(templateFormula -> templateFormula.getOutput().equals(templateOutput.getId())).findFirst().orElse(null);
                if (formula != null) {
                    stringBuilder.append(formula.getName()).append(": ");
                    List<String> names = templateFormulas.stream().map(TemplateFormula::getName).collect(Collectors.toList());

                    for (int i = 0; i < names.size(); i++) {
                        String s = names.get(i);
                        if (i > 0) stringBuilder.append(", ");

                        stringBuilder.append(s);
                    }
                }

                logger.debug(stringBuilder.toString());
            }
        }

        List<TemplateOutput> templateOutputsWithoutFormulaInputs = new ArrayList<>();
        for (TemplateOutput templateOutput : multiInputFormulaOutputs) {
            if (map.get(templateOutput).size() == 0) {
                templateOutputsWithoutFormulaInputs.add(templateOutput);
            }
        }

        multiInputFormulaOutputs.removeAll(templateOutputsWithoutFormulaInputs);

        List<TemplateOutput> templateOutputsWithFormulaInputs = new ArrayList<>(multiInputFormulaOutputs);
        templateOutputsWithFormulaInputs.sort((o1, o2) -> {
            TemplateFormula formula1 = templateHandler.getRcTemplate().getTemplateFormulas().stream().filter(templateFormula -> templateFormula.getOutput().equals(o1.getId())).findFirst().orElse(null);
            TemplateFormula formula2 = templateHandler.getRcTemplate().getTemplateFormulas().stream().filter(templateFormula -> templateFormula.getOutput().equals(o2.getId())).findFirst().orElse(null);
            List<TemplateFormula> neededFormulasForO1 = map.get(o1);
            List<TemplateFormula> neededFormulasForO2 = map.get(o2);
            boolean oneNeedsOutputFromTwo = false;
            boolean twoNeedsOutputFromOne = false;

            for (TemplateFormula templateFormula : neededFormulasForO1) {
                if (templateFormula.equals(formula2)) {
                    oneNeedsOutputFromTwo = true;
                    break;
                }
            }

            for (TemplateFormula templateFormula : neededFormulasForO2) {
                if (templateFormula.equals(formula1)) {
                    twoNeedsOutputFromOne = true;
                    break;
                }
            }

            if (oneNeedsOutputFromTwo && !twoNeedsOutputFromOne) return 1;
            else if (!oneNeedsOutputFromTwo && twoNeedsOutputFromOne) return -1;
            else if (oneNeedsOutputFromTwo) return 0;
            else return -1;
        });

        multiInputFormulaOutputs.clear();
        multiInputFormulaOutputs.addAll(templateOutputsWithoutFormulaInputs);
        multiInputFormulaOutputs.addAll(templateOutputsWithFormulaInputs);
    }

    private void createOutputs(List<TemplateOutput> outputs) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {

                for (TemplateOutput templateOutput : outputs) {
                    if (!templateOutput.getSeparator()) {
                        try {
                            if (templateOutput.getName() != null) {
                                logger.debug("Output creation for {}", templateOutput.getName());
                            } else if (templateOutput.getVariableName() != null) {
                                logger.debug("Output creation for output with variable name {}", templateOutput.getVariableName());
                            }

                            Label label = new Label(templateOutput.getName());
                            if (templateOutput.getNameBold()) {
                                label.setFont(Font.font(label.getFont().getFamily(), FontWeight.BOLD, getFontSize()));
                            } else {
                                label.setFont(Font.font(label.getFont().getFamily(), FontWeight.NORMAL, getFontSize()));
                            }
                            Label result = new Label();
                            result.setTextAlignment(TextAlignment.RIGHT);
                            result.setAlignment(Pos.CENTER_RIGHT);
                            if (templateOutput.getResultBold()) {
                                result.setFont(Font.font(result.getFont().getFamily(), FontWeight.BOLD, getFontSize()));
                            } else {
                                result.setFont(Font.font(label.getFont().getFamily(), FontWeight.NORMAL, getFontSize()));
                            }

                            HBox hBox = new HBox(label, result);

                            if (templateOutput.getShowTooltip()) {
                                Tooltip tooltip = new Tooltip(templateOutput.getTooltip());
                                Tooltip.install(hBox, tooltip);
                            }

                            if (templateOutput.getLink()) {
                                logger.debug("Found linked output, creating Manual Data Button");
                                JFXButton manSampleButton = new JFXButton("", JEConfig.getSVGImage(Icon.MANUAL_DATA_ENTRY, 12, 12));
                                manSampleButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.table.mansample")));

                                if (templateOutput.getTarget() != null) {
                                    try {
                                        TargetHelper th = new TargetHelper(ds, templateOutput.getTarget());

                                        manSampleButton.setOnAction(event -> {
                                            try {
                                                JEVisAttribute attribute = th.getAttribute().get(0);
                                                if (th.isValid() && th.targetObjectAccessible() && !th.getAttribute().isEmpty()) {


                                                    JEVisSample lastValue = attribute.getLatestSample();
                                                    String guiDisplayType = attribute.getType().getGUIDisplayType();

                                                    if (attribute.getType().getPrimitiveType() == JEVisConstants.PrimitiveType.STRING
                                                            && guiDisplayType.equalsIgnoreCase(GUIConstants.RANGING_VALUE.getId())) {
                                                        AttributeEditor editor = new RangingValueEditor(viewDialogContainer, attribute);
                                                        Label editorLabel = new Label(I18nWS.getInstance().getAttributeName(attribute));
                                                        VBox editorLabelVBox = new VBox(editorLabel);
                                                        editorLabelVBox.setAlignment(Pos.CENTER);

                                                        HBox content = new HBox(6, editorLabelVBox, editor.getEditor());

                                                        JFXDialog dialog = new JFXDialog();
                                                        dialog.setTransitionType(JFXDialog.DialogTransition.NONE);
                                                        dialog.setOverlayClose(false);
                                                        dialog.setContent(content);

                                                        dialog.show();
                                                    } else {
                                                        EnterDataDialog enterDataDialog = new EnterDataDialog(viewDialogContainer, ds);
                                                        enterDataDialog.setShowDetailedTarget(false);
                                                        enterDataDialog.setTarget(false, attribute);
                                                        enterDataDialog.setSample(lastValue);
                                                        enterDataDialog.setShowValuePrompt(true);

                                                        enterDataDialog.show();
                                                    }
                                                }
                                            } catch (Exception e) {
                                                logger.error("Could not determine attribute type and gui display type", e);
                                            }
                                        });

                                        hBox.getChildren().add(manSampleButton);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            if (templateOutput.getName() == null || templateOutput.getName().equals("")) {
                                hBox.setAlignment(Pos.CENTER_RIGHT);
                            }

                            if (templateOutput.getVariableName() == null || templateOutput.getVariableName().equals("")) {
                                if (templateOutput.getColSpan() > 1) {
                                    hBox.setAlignment(Pos.CENTER);
                                }
                            }

                            TemplateFormula formula = templateHandler.getRcTemplate().getTemplateFormulas().stream().filter(templateFormula -> templateFormula.getOutput().equals(templateOutput.getId())).findFirst().orElse(null);

                            if (formula != null) {
                                logger.debug("Found formula named {} with formula {}", formula.getName(), formula.getFormula());

                                DateTime start = getStart();
                                DateTime end = getEnd();

                                result.setText(NO_RESULT);
                                if (formula.getTimeRestrictionEnabled()) {
                                    TimeFrame fixedTimeFrame = null;
                                    TimeFrame reducingTimeFrame = null;

                                    for (TimeFrame timeFrame : intervalSelector.getTimeFactoryBox().getItems()) {
                                        if (formula.getFixedTimeFrame().equals(timeFrame.getID())) {
                                            fixedTimeFrame = timeFrame;
                                        } else if (formula.getReducingTimeFrame().equals(timeFrame.getID())) {
                                            reducingTimeFrame = timeFrame;
                                        }
                                    }

                                    if (fixedTimeFrame != null && reducingTimeFrame != null && !fixedTimeFrame.equals(TimeFrameFactory.NONE)) {
                                        start = fixedTimeFrame.getInterval(getStart()).getStart();
                                        end = getEnd();

                                        Period p = null;
                                        DateTime previousEndDate = null;
                                        if (!reducingTimeFrame.equals(TimeFrameFactory.NONE)) {
                                            try {
                                                p = new Period(reducingTimeFrame.getID());
                                            } catch (Exception ignored) {
                                            }

                                            if (p != null) {
                                                DateTime minusPeriodToDate = PeriodHelper.minusPeriodToDate(end, p);

                                                boolean followUp = false;

                                                if (p.equals(Period.years(1))) {
                                                    minusPeriodToDate = minusPeriodToDate.withMonthOfYear(12);
                                                    followUp = true;
                                                }

                                                if (followUp || p.equals(Period.months(1))) {
                                                    int lastDayOfMonth = minusPeriodToDate.dayOfMonth().getMaximumValue();
                                                    minusPeriodToDate = minusPeriodToDate.withDayOfMonth(lastDayOfMonth);
                                                    followUp = true;
                                                }

                                                if (followUp || p.equals(Period.days(1))) {
                                                    minusPeriodToDate = minusPeriodToDate.withHourOfDay(23);
                                                    followUp = true;
                                                }

                                                if (followUp || p.equals(Period.hours(1))) {
                                                    minusPeriodToDate = minusPeriodToDate.withMinuteOfHour(59);
                                                    followUp = true;
                                                }

                                                if (followUp || p.equals(Period.minutes(1))) {
                                                    minusPeriodToDate = minusPeriodToDate.withSecondOfMinute(59);
                                                }

                                                previousEndDate = minusPeriodToDate;
                                            } else {
                                                previousEndDate = end.minus(reducingTimeFrame.getInterval(getStart()).toDuration());
                                            }
                                        }

                                        if (previousEndDate != null && previousEndDate.isAfter(start)) {
                                            end = previousEndDate;
                                        } else if (previousEndDate != null && (previousEndDate.isBefore(start) || previousEndDate.equals(start))) {
                                            start = start.plusSeconds(1);
                                            end = start;
                                        }
                                    }
                                }

                                String formulaString = formula.getFormula();
                                Double calculate = 0d;
                                logger.debug("Start of formula creation starting with string " + formulaString);

                                boolean isText = false;
                                List<DateTime> allTimestamps = new ArrayList<>();
                                List<TemplateInput> textTypeInputs = new ArrayList<>();
                                List<TemplateInput> valueTypeInputs = new ArrayList<>();
                                List<TemplateInput> oneValueTypeInputs = new ArrayList<>();
                                List<TemplateInput> dependencyInputs = new ArrayList<>();
                                for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
                                    if (formula.getInputIds().contains(templateInput.getId())) {
                                        if (templateInput.getVariableType().equals(InputVariableType.STRING.toString()) || templateInput.getAttributeName().equals("Name")) {
                                            isText = true;
                                            formulaString = formulaString.replace(templateInput.getVariableName(), templateInput.getValue(ds, start, end));
                                        } else if (templateInput.getVariableType().equals(InputVariableType.AVG.toString())
                                                || templateInput.getVariableType().equals(InputVariableType.MIN.toString())
                                                || templateInput.getVariableType().equals(InputVariableType.MAX.toString())
                                                || templateInput.getVariableType().equals(InputVariableType.SUM.toString())) {
                                            templateInput.CreateValues(ds, intervalSelector, start, end);

                                            templateInput.getResultMap().forEach((dateTime, aDouble) -> {
                                                if (!allTimestamps.contains(dateTime)) allTimestamps.add(dateTime);
                                            });

                                            valueTypeInputs.add(templateInput);
                                        } else if (templateInput.getVariableType().equals(InputVariableType.NON_PERIODIC.toString())) {
                                            templateInput.CreateValues(ds, intervalSelector, start, end);

                                            templateInput.getResultMap().forEach((dateTime, aDouble) -> {
                                                if (!allTimestamps.contains(dateTime)) allTimestamps.add(dateTime);
                                            });

                                            oneValueTypeInputs.add(templateInput);

                                            if (templateInput.getDependency() != null) {
                                                TemplateInput dependencyInput = templateHandler.getRcTemplate().getTemplateInputs().stream().filter(ti -> ti.getId().equals(templateInput.getDependency())).findFirst().orElse(null);
                                                if (dependencyInput != null) {
                                                    dependencyInput.CreateValues(ds, intervalSelector, start, end);
                                                    dependencyInputs.add(dependencyInput);
                                                }
                                            }
                                        } else if (templateInput.getVariableType().equals(InputVariableType.LAST.toString())) {
                                            templateInput.CreateValues(ds, intervalSelector, start, end);

                                            templateInput.getResultMap().forEach((dateTime, aDouble) -> {
                                                if (!allTimestamps.contains(dateTime)) allTimestamps.add(dateTime);
                                            });

                                            oneValueTypeInputs.add(templateInput);
                                        } else if (templateInput.getVariableType().equals(InputVariableType.YEARLY_VALUE.toString())) {
                                            templateInput.CreateValues(ds, intervalSelector, start, end);

                                            templateInput.getResultMap().forEach((dateTime, aDouble) -> {
                                                if (!allTimestamps.contains(dateTime)) allTimestamps.add(dateTime);
                                            });

                                            oneValueTypeInputs.add(templateInput);
                                        } else if (templateInput.getVariableType().equals(InputVariableType.RANGING_VALUE.toString())) {

                                            String rangingValueDeterminationId = templateInput.getDependency();
                                            DateTime finalStart = start;
                                            DateTime finalEnd = end;
                                            Double rangingValueDetermination = templateHandler.getRcTemplate().getTemplateInputs().stream().filter(determinationInput -> rangingValueDeterminationId.equals(determinationInput.getId())).findFirst().map(determinationInput -> Double.parseDouble(determinationInput.getValue(ds, finalStart, finalEnd))).orElse(null);

                                            if (!allTimestamps.contains(start)) allTimestamps.add(start);

                                            formulaString = formulaString.replace(templateInput.getVariableName(), templateInput.getValue(ds, start, end, rangingValueDetermination));
                                        }
                                    }
                                }

                                if (formula.getInputIds().size() == 1 && templateOutput.getShowAnalysisLink()) {
                                    TemplateInput correspondingInput = templateHandler.getRcTemplate().getTemplateInputs().stream().filter(templateInput -> templateInput.getId().equals(formula.getInputIds().get(0))).findFirst().orElse(null);

                                    if (!correspondingInput.getAttributeName().equals("name")) {
                                        JEVisAttribute attribute = ds.getObject(correspondingInput.getObjectID()).getAttribute(correspondingInput.getAttributeName());
                                        AnalysisLinkButton analysisLinkButton = new AnalysisLinkButton(attribute);
                                        analysisLinkButton.getAnalysisRequest().setStartDate(start);
                                        analysisLinkButton.getAnalysisRequest().setEndDate(end);
                                        hBox.getChildren().add(analysisLinkButton);
                                    }

                                }

                                dependencyInputs.forEach(dependencyInput -> dependencyInput.getResultMap().forEach((dateTime, aDouble) -> {
                                    if (!allTimestamps.contains(dateTime)) allTimestamps.add(dateTime);
                                }));

                                allTimestamps.sort(Comparator.naturalOrder());

                                for (TemplateInput oneValueTypeInput : oneValueTypeInputs) {
                                    if (oneValueTypeInput.getVariableType().equals(InputVariableType.NON_PERIODIC.toString())) {
                                        List<ValueWithDateTime> valuesWithDateTimes = new ArrayList<>();
                                        oneValueTypeInput.getResultMap().forEach((dateTime, aDouble) -> valuesWithDateTimes.add(new ValueWithDateTime(dateTime, aDouble)));
                                        valuesWithDateTimes.sort(Comparator.comparing(o -> o.getDateTime().get(0)));
                                        ValueWithDateTime lastValue = valuesWithDateTimes.get(0);

                                        for (DateTime ts : allTimestamps) {
                                            if (!oneValueTypeInput.getResultMap().containsKey(ts)) {
                                                oneValueTypeInput.getResultMap().put(ts, lastValue.getValue());
                                            } else {
                                                lastValue = new ValueWithDateTime(ts, oneValueTypeInput.getResultMap().get(ts));
                                            }
                                        }
                                    } else if (oneValueTypeInput.getVariableType().equals(InputVariableType.LAST.toString())) {
                                        Double d = oneValueTypeInput.getResultMap().entrySet().stream().findFirst().map(Map.Entry::getValue).orElse(null);
                                        if (d != null) {
                                            for (DateTime ts : allTimestamps) {
                                                oneValueTypeInput.getResultMap().put(ts, d);
                                            }
                                        }
                                    } else if (oneValueTypeInput.getVariableType().equals(InputVariableType.YEARLY_VALUE.toString())) {
                                        Double d = oneValueTypeInput.getResultMap().entrySet().stream().findFirst().map(Map.Entry::getValue).orElse(null);
                                        if (d != null) {
                                            d = d / allTimestamps.size();

                                            for (DateTime ts : allTimestamps) {
                                                oneValueTypeInput.getResultMap().put(ts, d);
                                            }
                                        }
                                    }
                                }

                                Map<DateTime, Double> allResults = new HashMap<>();

                                for (DateTime ts : allTimestamps) {
                                    String s = formulaString;
                                    for (TemplateInput valueTypeInput : valueTypeInputs) {
                                        s = formulaString.replace(valueTypeInput.getVariableName(), String.valueOf(valueTypeInput.getResultMap().get(ts)));
                                    }

                                    for (TemplateInput oneValueTypeInput : oneValueTypeInputs) {
                                        s = formulaString.replace(oneValueTypeInput.getVariableName(), String.valueOf(oneValueTypeInput.getResultMap().get(ts)));
                                    }

                                    Expression expression = new Expression(s);
                                    Double d = expression.calculate();
                                    if (!d.isNaN()) {
                                        allResults.put(ts, d);
                                    }
                                }

                                for (Map.Entry<DateTime, Double> entry : allResults.entrySet()) {
                                    Double aDouble = entry.getValue();
                                    calculate += aDouble;
                                }

                                QuantityUnits qu = new QuantityUnits();

                                if (!qu.isQuantityUnit(templateOutput.getUnit())) {
                                    calculate = calculate / allResults.size();
                                }

                                logger.debug("Formula after input replacement: " + formulaString);

                                boolean needsCalculation = false;
                                for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateFormulaInputs()) {
                                    if (formula.getInputIds().contains(templateInput.getTemplateFormula())) {

                                        Double d = resultMap.get(templateInput.getTemplateFormula());
                                        if (d != null) {
                                            formulaString = formulaString.replace(templateInput.getVariableName(), d.toString());
                                        } else {
                                            formulaString = formulaString.replace(templateInput.getVariableName(), "0");
                                        }

                                        needsCalculation = true;
                                    }
                                }

                                for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
                                    if (formula.getInputIds().contains(templateInput.getId())
                                            && formulaString.contains(templateInput.getVariableName())) {

                                        Double value = templateInput.getResultMap().entrySet().stream().findFirst().map(Map.Entry::getValue).orElse(null);

                                        if (value != null) {
                                            needsCalculation = true;
                                            formulaString = formulaString.replace(templateInput.getVariableName(), String.valueOf(value));
                                        }
                                    }
                                }

                                logger.debug("Finished formula after formula input replacement: " + formulaString);

                                if (needsCalculation || formulaString.contains("if(")) {
                                    Expression expression = new Expression(formulaString);
                                    calculate = expression.calculate();
                                }

                                if (formula.getInputIds().size() == 0) {
                                    try {
                                        calculate = Double.parseDouble(formulaString);
                                    } catch (Exception e) {
                                        logger.error("No input ids and could not parse formula as value", e);
                                    }
                                }

                                if (!isText) {
                                    try {
                                        if (calculate == 0d) {
                                            Expression expression = new Expression(formulaString);
                                            calculate = expression.calculate();
                                        }
                                        if (!calculate.isNaN() && !calculate.isInfinite()) {
                                            resultMap.put(formula.getId(), calculate);
                                            logger.debug("added value of {} to result map", calculate);
                                        }
                                        if (templateOutput.getUnit() != null) {
                                            result.setText(nf.format(calculate) + " " + templateOutput.getUnit());
                                        } else {
                                            result.setText(nf.format(calculate));
                                        }

                                        succeeded();
                                    } catch (Exception e) {
                                        logger.error("Error in formula {}", formula.getName(), e);
                                    }
                                } else {
                                    result.setText(formulaString);
                                }
                            }

                            Platform.runLater(() -> gridPane.add(hBox, templateOutput.getColumn(), templateOutput.getRow(), templateOutput.getColSpan(), templateOutput.getRowSpan()));
                        } catch (Exception e) {
                            logger.error("Could not create Output {} on location Column: {} Row: {}", templateOutput.getName(), templateOutput.getColumn(), templateOutput.getRow());
                        }
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
                return null;
            }
        };

        JEConfig.getStatusBar().addTask(OutputView.class.getSimpleName(), task, null, true);
    }

    public void updateViewInputFlowPane() {

        Map<TemplateInput, TemplateInput> translationMap = new HashMap<>();
        if (selectionTemplate == null) {
            selectionTemplate = new SelectionTemplate();
        }
        linkConfiguredToTemplate(translationMap);

        Map<JEVisClass, List<TemplateInput>> groupedInputsMap = new HashMap<>();
        List<TemplateInput> ungroupedInputs = new ArrayList<>();
        for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
            if (((templateInput.getGroup() == null || templateInput.getGroup()) && templateInput.getVariableType() != null)) {
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

                logger.debug("Added {} to grouped inputs", templateInput.getVariableName());
            } else if (templateInput.getVariableType() != null && !templateInput.getVariableType().equals(InputVariableType.FORMULA.toString())) {
                ungroupedInputs.add(templateInput);
                logger.debug("Added formula {} to ungrouped inputs", templateInput.getVariableName());
            }
        }

        ungroupedInputs.sort((o1, o2) -> alphanumComparator.compare(o1.getVariableName(), o2.getVariableName()));

        int row = 0;
        Platform.runLater(() -> viewInputs.getChildren().clear());
        if (contractsGP != null) {
            row = 6;
            removeNodesFromRow(6, contractsGP);
        }

        int column = 0;
        for (TemplateInput ungroupedInput : ungroupedInputs) {
            String variableName = ungroupedInput.getVariableName();
            Label label = new Label(variableName);
            label.setTooltip(new Tooltip(ungroupedInput.getObjectClass()));
            label.setMinWidth(120);
            label.setAlignment(Pos.CENTER_LEFT);

            if (column == 6) {
                column = 0;
                row++;
            }

            try {
                JFXComboBox<JEVisObject> objectSelector = createObjectSelector(ungroupedInput.getObjectClass(), ungroupedInput.getFilter());

                try {
                    Long objectID = ungroupedInput.getObjectID();
                    if (objectID != -1L) {
                        JEVisObject selectedObject = ds.getObject(objectID);
                        objectSelector.getSelectionModel().select(selectedObject);
                    } else {
                        objectSelector.getSelectionModel().selectFirst();
                        ungroupedInput.setObjectID(objectSelector.getSelectionModel().getSelectedItem().getID());
                        TemplateInput templateInput = translationMap.get(ungroupedInput);
                        if (templateInput != null) {
                            templateInput.setObjectID(objectSelector.getSelectionModel().getSelectedItem().getID());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Could not get object {} for ungrouped input {}", ungroupedInput.getObjectID(), variableName);
                }

                objectSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(oldValue)) {
                        ungroupedInput.setObjectID(newValue.getID());
                        TemplateInput templateInput = translationMap.get(ungroupedInput);
                        if (templateInput != null) {
                            templateInput.setObjectID(newValue.getID());
                        }
                        requestUpdate();
                    }
                });

                int finalColumn = column;
                int finalRow = row;
                if (contractsGP == null) {
                    Region region = new Region();
                    region.setMinWidth(25);
                    Platform.runLater(() -> {
                        removeNode(finalRow, finalColumn, viewInputs);
                        removeNode(finalRow, finalColumn + 1, viewInputs);
                        removeNode(finalRow, finalColumn + 2, viewInputs);
                        viewInputs.add(label, finalColumn, finalRow);
                        viewInputs.add(objectSelector, finalColumn + 1, finalRow);
                        viewInputs.add(region, finalColumn + 2, finalRow);
                    });
                } else {
                    String objectClassString = ungroupedInput.getObjectClass();
                    JEVisClass jeVisClass = ds.getJEVisClass(objectClassString).getInheritance();
                    String translatedClassName;
                    if (jeVisClass != null) {
                        translatedClassName = I18nWS.getInstance().getClassName(jeVisClass.getName());
                    } else {
                        translatedClassName = I18nWS.getInstance().getClassName(objectClassString);
                    }
//                    if (label.getText().equals("")) {
//                        label.setText(translatedClassName);
//                    } else {
//                        label.setText(label.getText() + " (" + translatedClassName + ")");
//                    }
                    label.setTooltip(new Tooltip(translatedClassName));
                    Region region = new Region();
                    region.setMinWidth(25);
                    Platform.runLater(() -> {
                        removeNode(finalRow, finalColumn, contractsGP);
                        removeNode(finalRow, finalColumn + 1, contractsGP);
                        removeNode(finalRow, finalColumn + 2, contractsGP);
                        contractsGP.add(label, finalColumn, finalRow);
                        contractsGP.add(objectSelector, finalColumn + 1, finalRow);
                        contractsGP.add(region, finalColumn + 2, finalRow);
                    });
                }
            } catch (Exception e) {
                logger.error("Could not get object selector for template input {}", ungroupedInput, e);
            }

            column += 3;
        }

        List<JEVisClass> groupedInputClasses = new ArrayList<>(groupedInputsMap.keySet());
        groupedInputClasses.sort((o1, o2) -> {
            try {
                return alphanumComparator.compare(I18nWS.getInstance().getClassName(o1), I18nWS.getInstance().getClassName(o2));
            } catch (Exception e) {
                logger.error("Could not compare translated classename of {} with {}", o1, o2, e);
            }
            return -1;
        });

        for (JEVisClass jeVisClass : groupedInputClasses) {
            if (column == 6) {
                column = 0;
                row++;
            }
            List<TemplateInput> groupedInputs = groupedInputsMap.get(jeVisClass);
            TemplateInput firstGroupedInput = groupedInputs.get(0);
            StringBuilder objectNames = new StringBuilder();
            String className = null;
            try {
                className = I18nWS.getInstance().getClassName(jeVisClass);

                for (int i = 0; i < groupedInputs.size(); i++) {
                    if (i > 0) objectNames.append(", ");
                    if (i % 2 == 0) objectNames.append("\n");
                    TemplateInput groupedInput = groupedInputs.get(i);
                    objectNames.append(groupedInput.getVariableName());
                }
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            Label label = new Label(objectNames.toString());
            label.setTooltip(new Tooltip(className));
            label.setMinWidth(120);
            label.setAlignment(Pos.CENTER_LEFT);

            JFXComboBox<JEVisObject> objectSelector = createObjectSelector(firstGroupedInput.getObjectClass(), firstGroupedInput.getFilter());

            try {
                Long objectID = groupedInputs.stream().filter(input -> input.getObjectID() != -1L).findFirst().map(TemplateSelected::getObjectID).orElse(-1L);

                if (objectID != -1L) {
                    for (TemplateInput templateInput : groupedInputs) {
                        if (templateInput.getObjectID() == -1) {
                            logger.debug("Found grouped input without id {}, selecting id {}", templateInput.getVariableName(), objectID);
                            templateInput.setObjectID(objectID);
                        }
                    }

                    JEVisObject selectedObject = ds.getObject(objectID);
                    logger.debug("Found object {}:{}, selecting for {}", selectedObject.getName(), selectedObject.getID(), firstGroupedInput.getVariableName());
                    objectSelector.getSelectionModel().select(selectedObject);
                } else {
                    logger.debug("Found no object, selecting for {}", firstGroupedInput.getVariableName());
                    objectSelector.getSelectionModel().selectFirst();
                    groupedInputs.forEach(templateInput1 -> {
                        templateInput1.setObjectID(objectSelector.getSelectionModel().getSelectedItem().getID());
                        TemplateInput templateInput = translationMap.get(templateInput1);
                        if (templateInput != null) {
                            templateInput.setObjectID(objectSelector.getSelectionModel().getSelectedItem().getID());
                        }
                    });
                }
            } catch (Exception e) {
                logger.error("Could correctly assign selection values vor class {}", jeVisClass);
            }

            objectSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    groupedInputs.forEach(templateInput1 -> {
                        templateInput1.setObjectID(newValue.getID());
                        TemplateInput templateInput = translationMap.get(templateInput1);
                        if (templateInput != null) {
                            templateInput.setObjectID(newValue.getID());
                        }
                    });
                    requestUpdate();
                }
            });

            int finalColumn1 = column;
            int finalRow1 = row;
            if (contractsGP == null) {
                Region region = new Region();
                region.setMinWidth(25);
                Platform.runLater(() -> {
                    removeNode(finalRow1, finalColumn1, viewInputs);
                    removeNode(finalRow1, finalColumn1 + 1, viewInputs);
                    removeNode(finalRow1, finalColumn1 + 2, viewInputs);
                    viewInputs.add(label, finalColumn1, finalRow1);
                    viewInputs.add(objectSelector, finalColumn1 + 1, finalRow1);
                    viewInputs.add(region, finalColumn1 + 2, finalRow1);
                });
            } else {
                Region region = new Region();
                region.setMinWidth(25);
                try {
                    if (jeVisClass.getInheritance() != null) {
                        String translatedClassName = I18nWS.getInstance().getClassName(jeVisClass.getInheritance().getName());
                        label.setTooltip(new Tooltip(translatedClassName));
                    } else {
                        String translatedClassName = I18nWS.getInstance().getClassName(jeVisClass.getName());
                        label.setTooltip(new Tooltip(translatedClassName));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    removeNode(finalRow1, finalColumn1, contractsGP);
                    removeNode(finalRow1, finalColumn1 + 1, contractsGP);
                    removeNode(finalRow1, finalColumn1 + 2, contractsGP);
                    contractsGP.add(label, finalColumn1, finalRow1);
                    contractsGP.add(objectSelector, finalColumn1 + 1, finalRow1);
                    contractsGP.add(region, finalColumn1 + 2, finalRow1);
                });
            }
            column += 3;
        }

        for (TemplateOutput templateOutput : templateHandler.getRcTemplate().getTemplateOutputs()) {
            if (templateOutput.getLink()) {
                if (column == 6) {
                    column = 0;
                    row++;
                }

                Label label = new Label(templateOutput.getVariableName());

                JFXButton targetButton = new JFXButton(I18n
                        .getInstance().getString("plugin.object.attribute.target.button"),
                        JEConfig.getImage("folders_explorer.png", 18, 18));
                targetButton.wrapTextProperty().setValue(true);
                targetButton.setOnAction(getTargetButtonActionEventEventHandler(templateOutput, targetButton));
                setButtonText(templateOutput, targetButton);

                int finalColumn1 = column;
                int finalRow1 = row;
                if (contractsGP == null) {
                    Region region = new Region();
                    region.setMinWidth(25);
                    Platform.runLater(() -> {
                        removeNode(finalRow1, finalColumn1, viewInputs);
                        removeNode(finalRow1, finalColumn1 + 1, viewInputs);
                        removeNode(finalRow1, finalColumn1 + 2, viewInputs);
                        viewInputs.add(label, finalColumn1, finalRow1);
                        viewInputs.add(targetButton, finalColumn1 + 1, finalRow1);
                        viewInputs.add(region, finalColumn1 + 2, finalRow1);
                    });
                } else {
                    Region region = new Region();
                    region.setMinWidth(25);

                    Platform.runLater(() -> {
                        removeNode(finalRow1, finalColumn1, contractsGP);
                        removeNode(finalRow1, finalColumn1 + 1, contractsGP);
                        removeNode(finalRow1, finalColumn1 + 2, contractsGP);
                        contractsGP.add(label, finalColumn1, finalRow1);
                        contractsGP.add(targetButton, finalColumn1 + 1, finalRow1);
                        contractsGP.add(region, finalColumn1 + 2, finalRow1);
                    });
                }

                column += 3;
            }
        }
    }

    public EventHandler<ActionEvent> getTargetButtonActionEventEventHandler(TemplateOutput templateOutput, JFXButton targetButton) {
        return t -> {
            try {
                SelectTargetDialog selectTargetDialog = null;

                TargetHelper th = null;
                if (templateOutput.getTarget() != null) {
                    th = new TargetHelper(ds, templateOutput.getTarget());
                    if (th.isValid() && th.targetObjectAccessible()) {
                        logger.info("Target Is valid");
                        setButtonText(templateOutput, targetButton);
                    }
                }

                List<JEVisTreeFilter> allFilter = new ArrayList<>();
                JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataAndCleanDataFilter();
                JEVisTreeFilter allAttributesFilter = SelectTargetDialog.buildAllAttributesFilter();
                allFilter.add(allDataFilter);
                allFilter.add(allAttributesFilter);

                List<UserSelection> openList = new ArrayList<>();
                if (th != null && !th.getAttribute().isEmpty()) {
                    for (JEVisAttribute att : th.getAttribute())
                        openList.add(new UserSelection(UserSelection.SelectionType.Attribute, att, null, null));
                } else if (th != null && !th.getObject().isEmpty()) {
                    for (JEVisObject obj : th.getObject())
                        openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
                }

                selectTargetDialog = new SelectTargetDialog(contractsDialogContainer, allFilter, allDataFilter, null, SelectionMode.SINGLE, ds, openList);

                SelectTargetDialog finalSelectTargetDialog = selectTargetDialog;
                selectTargetDialog.setOnDialogClosed(event -> {
                    try {
                        if (finalSelectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                            logger.trace("Selection Done");

                            String newTarget = "";
                            List<UserSelection> selections = finalSelectTargetDialog.getUserSelection();
                            for (UserSelection us : selections) {
                                int index = selections.indexOf(us);
                                if (index > 0) newTarget += ";";

                                newTarget += us.getSelectedObject().getID();
                                if (us.getSelectedAttribute() != null) {
                                    newTarget += ":" + us.getSelectedAttribute().getName();
                                } else {
                                    newTarget += ":Value";
                                }
                            }
                            templateOutput.setTarget(newTarget);
                            setButtonText(templateOutput, targetButton);
                        }
                    } catch (Exception ex) {
                        logger.catching(ex);
                    }
                });
                selectTargetDialog.show();

            } catch (Exception ex) {
                logger.catching(ex);
            }
        };
    }

    void setButtonText(TemplateOutput templateOutput, JFXButton targetButton) {
        TargetHelper th = null;
        try {
            if (templateOutput.getTarget() != null) {
                th = new TargetHelper(ds, templateOutput.getTarget());
            }

            if (th != null && th.isValid() && th.targetObjectAccessible()) {

                StringBuilder bText = new StringBuilder();

                JEVisClass cleanData = ds.getJEVisClass("Clean Data");

                for (JEVisObject obj : th.getObject()) {
                    int index = th.getObject().indexOf(obj);
                    if (index > 0) bText.append("; ");

                    if (obj.getJEVisClass().equals(cleanData)) {
                        List<JEVisObject> parents = obj.getParents();
                        if (!parents.isEmpty()) {
                            for (JEVisObject parent : parents) {
                                bText.append("[");
                                bText.append(parent.getID());
                                bText.append("] ");
                                bText.append(parent.getName());
                                bText.append(" / ");
                            }
                        }
                    }

                    bText.append("[");
                    bText.append(obj.getID());
                    bText.append("] ");
                    bText.append(obj.getName());

                    if (th.isAttribute()) {

                        bText.append(" - ");
                        bText.append(th.getAttribute().get(index).getName());

                    }
                }

                Platform.runLater(() -> targetButton.setText(bText.toString()));
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    private void linkConfiguredToTemplate(Map<TemplateInput, TemplateInput> translationMap) {
        templateHandler.getRcTemplate().getTemplateInputs().stream().filter(templateInput -> !selectionTemplate.getSelectedInputs().contains(templateInput)).forEach(templateInput -> selectionTemplate.getSelectedInputs().add(templateInput));
        for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
            for (TemplateInput input : selectionTemplate.getSelectedInputs()) {
                if (templateInput.equals(input)) {
                    translationMap.put(templateInput, input);
                    templateInput.setObjectID(input.getObjectID());
                    break;
                }
            }
        }

        templateHandler.getRcTemplate().getTemplateOutputs().stream().filter(templateOutput -> !selectionTemplate.getLinkedOutputs().contains(templateOutput)).forEach(templateOutput -> selectionTemplate.getLinkedOutputs().add(templateOutput));
        for (TemplateOutput templateOutput : templateHandler.getRcTemplate().getTemplateOutputs()) {
            for (TemplateOutput output : selectionTemplate.getLinkedOutputs()) {
                if (templateOutput.equals(output)) {
                    templateOutput.setTarget(output.getTarget());
                    break;
                }
            }
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

    public void removeNodesFromRow(final int row, GridPane gridPane) {
        if (gridPane != null) {
            ObservableList<Node> children = gridPane.getChildren();
            for (Node node : children) {
                if (GridPane.getRowIndex(node) >= row) {
                    Platform.runLater(() -> gridPane.getChildren().remove(node));
                }
            }
        }
    }

    private JFXComboBox<JEVisObject> createObjectSelector(String jeVisClassName, String filter) {

        List<JEVisObject> objects = new ArrayList<>();
        try {
            List<JEVisObject> allObjects = ds.getObjects(ds.getJEVisClass(jeVisClassName), false);
            List<JEVisObject> filteredObjects = new ArrayList<>();
            for (JEVisObject jeVisObject : allObjects) {
                String objectName = TRCPlugin.getRealName(jeVisObject);

                if (filter != null && filter.contains(" ")) {
                    String[] result = filter.toLowerCase().split(" ");
                    String string = objectName.toLowerCase();
                    boolean[] results = new boolean[result.length];
                    for (int i = 0, resultLength = result.length; i < resultLength; i++) {
                        String value = result[i];
                        results[i] = string.contains(value);
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

            objects.addAll(filteredObjects);
        } catch (JEVisException e) {
            logger.error("Could not get JEVisClass {}", jeVisClassName, e);
        }

        objects.sort((o1, o2) -> {

            String prefix1 = "";
            String prefix2 = "";

            JEVisObject correctObject1 = o1;
            try {

                if (o1.getJEVisClassName().equals("Clean Data")) {
                    correctObject1 = CommonMethods.getFirstParentalDataObject(o1);
                }

                if (!ChartTools.isMultiSite(ds) && !ChartTools.isMultiDir(ds, correctObject1))
                    prefix1 = correctObject1.getName();
                else {
                    String prefix = "";
                    if (ChartTools.isMultiSite(ds)) {
                        prefix += objectRelations.getObjectPath(correctObject1);
                    }
                    if (ChartTools.isMultiDir(ds, correctObject1)) {
                        prefix += objectRelations.getRelativePath(correctObject1);
                    }

                    prefix1 = prefix + correctObject1.getName();
                }

                if (!prefix1.endsWith(o1.getName())) {
                    prefix1 += " \\ " + o1.getName();
                }

                JEVisObject correctObject2 = o2;
                if (o2.getJEVisClassName().equals("Clean Data")) {
                    correctObject2 = CommonMethods.getFirstParentalDataObject(o2);
                }

                if (!ChartTools.isMultiSite(ds) && !ChartTools.isMultiDir(ds, correctObject2))
                    prefix2 = correctObject2.getName();
                else {
                    String prefix = "";
                    if (ChartTools.isMultiSite(ds)) {
                        prefix += objectRelations.getObjectPath(correctObject2);
                    }
                    if (ChartTools.isMultiDir(ds, correctObject2)) {
                        prefix += objectRelations.getRelativePath(correctObject2);
                    }

                    prefix2 = prefix + correctObject2.getName();
                }

                if (!prefix2.endsWith(o2.getName())) {
                    prefix2 += " \\ " + o2.getName();
                }

                return alphanumComparator.compare(prefix1, prefix2);

            } catch (Exception e) {
                logger.error("Could not sort properly", e);
            }

            return alphanumComparator.compare(o1.getName(), o2.getName());
        });

        JFXComboBox<JEVisObject> objectSelector = new JFXComboBox<>(FXCollections.observableArrayList(objects));
        objectSelector.setMaxWidth(Double.MAX_VALUE);

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
                                String name = "";
                                JEVisObject correctObject = obj;
                                if (obj.getJEVisClassName().equals("Clean Data")) {
                                    correctObject = CommonMethods.getFirstParentalDataObject(obj);
                                }

                                if (!ChartTools.isMultiSite(ds) && !ChartTools.isMultiDir(ds, correctObject))
                                    name = correctObject.getName();
                                else {
                                    String prefix = "";
                                    if (ChartTools.isMultiSite(ds)) {
                                        prefix += objectRelations.getObjectPath(correctObject);
                                    }
                                    if (ChartTools.isMultiDir(ds, correctObject)) {
                                        prefix += objectRelations.getRelativePath(correctObject);
                                    }

                                    name = prefix + correctObject.getName();
                                }

                                if (!name.endsWith(obj.getName())) {
                                    name += " \\ " + obj.getName();
                                }

                                setText(name);
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

    public void setContractsGP(GridPane contractsGP) {
        this.contractsGP = contractsGP;
    }

    public void setTimeframeField(Label timeframeField) {
        this.timeframeField = timeframeField;
    }

    public HBox getDateBox() {
        return dateBox;
    }

    public GridPane getHeaderGP() {
        return headerGP;
    }

    public void setContractsDialogContainer(StackPane contractsTabDialogContainer) {
        this.contractsDialogContainer = contractsTabDialogContainer;
    }

    public void setViewDialogContainer(StackPane viewDialogContainer) {
        this.viewDialogContainer = viewDialogContainer;
    }

    public double getFontSize() {
        return fontSize;
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }
}
