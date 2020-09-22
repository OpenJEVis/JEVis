package org.jevis.jeconfig.sample;

import com.jfoenix.controls.JFXDatePicker;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AggregationBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ProcessorBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.TimeZoneBox;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class ControlPane extends GridPane {

    private static final Logger logger = LogManager.getLogger(ControlPane.class);
    private final Button ok = new Button(I18n.getInstance().getString("attribute.editor.save"));
    private final JFXDatePicker startDate = new JFXDatePicker();
    private final JFXDatePicker endDate = new JFXDatePicker();
    private final Button cancel = new Button(I18n.getInstance().getString("attribute.editor.cancel"));

    private final Button reloadButton = new Button("", JEConfig.getImage("1403018303_Refresh.png", 12, 12));
    private final Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.progress.tooltip"));
    private final Label timeHeader = new Label(I18n.getInstance().getString("attribute.editor.timerange"));
    private final Label processorHeader = new Label(I18n.getInstance().getString("attribute.editor.dataprocessing"));
    private final Label dataProcessorLabel = new Label(I18n.getInstance().getString("attribute.editor.processor"));
    private final Label aggregationLabel = new Label(I18n.getInstance().getString("attribute.editor.aggregate"));
    private final Label endLabel = new Label(I18n.getInstance().getString("attribute.editor.until"));
    private final Label startLabel = new Label(I18n.getInstance().getString("attribute.editor.from"));
    private final Label tzLabel = new Label(I18n.getInstance().getString("attribute.editor.tz"));
    private final Label reloadLabel = new Label(I18n.getInstance().getString("attribute.editor.reload"));
    private final AggregationBox aggregationField = new AggregationBox(AggregationPeriod.NONE);
    private final ProcessorBox processorField;
    // private RangeSlider dateSlider = new RangeSlider(0, 100, 10, 90);
    private final Region spacer2 = new Region();
    private final HBox buttonPanel = new HBox();
    private final Region spacer = new Region();
    private final Region spacerRow = new Region();
    private final Region spacerRow2 = new Region();
    private final TimeZoneBox timeZoneBox = new TimeZoneBox();
    private DateTimeZone dateTimeZone = DateTimeZone.getDefault();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZone(dateTimeZone);
    private LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);
    private JEVisAttribute attribute;
    private AggregationPeriod period = AggregationPeriod.NONE;
    private EventHandler<ActionEvent> okEvent;
    private EventHandler<ActionEvent> cancelEvent;
    private final AtomicBoolean updateing = new AtomicBoolean(false);
    private final Separator separator = new Separator(Orientation.HORIZONTAL);
    private DateTime from = null;
    private DateTime until = null;
    private EventHandler<ActionEvent> timeChangeEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {

        }
    };

    public ControlPane(JEVisAttribute attribute) {
        super();
        this.attribute = attribute;

        JEVisObject rawDataParent = attribute.getObject();
        try {
            rawDataParent = getRawDataParent(attribute.getObject());
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        processorField = new ProcessorBox(rawDataParent, attribute.getObject());
//        HBox reloadBox = new HBox(reloadLabel,reloadButton);
//        reloadBox.setAlignment(Pos.BASELINE_LEFT);

        setPadding(new Insets(8, 8, 12, 8));
        setHgap(5);
        setVgap(5);

        //reloadButton.setPadding(new Insets(2,0,2,0));
        //reloadButton.setMaxHeight(15);
//        reloadButton.setMaxSize(15,15);
//        reloadButton.heightProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("button :"+newValue);
//        });

        //add(timeHeader, 0, 0, 2, 1);
        add(startLabel, 0, 1, 1, 1);
        add(endLabel, 0, 2, 1, 1);
        add(startDate, 1, 1, 1, 1);
        add(endDate, 1, 2, 1, 1);
        add(reloadLabel, 0, 3, 1, 1);
        add(reloadButton, 1, 3, 1, 1);
        //add(reloadBox, 0, 3, 2, 1);
        //add(spacerRow2, 2, 0, 1, 4);

        add(spacerRow, 5, 0, 1, 4);
        //add(processorHeader, 6, 0, 2, 1);
        add(dataProcessorLabel, 6, 1, 1, 1);
        add(aggregationLabel, 6, 2, 1, 1);
        add(tzLabel, 6, 3, 1, 1);

        add(processorField, 7, 1, 1, 1);
        add(aggregationField, 7, 2, 1, 1);
        add(timeZoneBox, 7, 3, 1, 1);

        add(separator, 0, 4, 8, 1);
        add(buttonPanel, 0, 5, 8, 1);


        buttonPanel.getChildren().addAll(spacer, cancel, ok);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        //buttonPanel.setPadding(new Insets(10));
        buttonPanel.setSpacing(15);//10
        buttonPanel.setMaxHeight(25);
        ok.setDefaultButton(true);
        cancel.setCancelButton(true);
        reloadButton.setTooltip(reloadTooltip);
//        dateSlider.setShowTickMarks(false);
//        dateSlider.setShowTickLabels(false);
//        dateSlider.setBlockIncrement(10);
//        dateSlider.setPadding(new Insets(5, 10, 0, 20));
//        dateSlider.setMaxWidth(450);
        startDate.setMaxWidth(120);
        endDate.setMaxWidth(120);

        processorField.setMinWidth(150);
        aggregationField.setMinWidth(150);
        timeZoneBox.setMinWidth(150);
        processorField.setMaxWidth(Double.MAX_VALUE);
        aggregationField.setMaxWidth(Double.MAX_VALUE);
        timeZoneBox.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(ok, Priority.NEVER);
        HBox.setHgrow(cancel, Priority.NEVER);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        GridPane.setHgrow(spacerRow, Priority.SOMETIMES);
        //GridPane.setHgrow(spacerRow2, Priority.SOMETIMES);


        timeHeader.setStyle("-fx-font-weight: bold");
        processorHeader.setStyle("-fx-font-weight: bold");

        addListeners();

        init();
        updateView();
    }

    private void init() {
        if (attribute.hasSample()) {
            try {
                logger.debug("init from: '{}' until: '{}'", attribute.getTimestampFromFirstSample(), attribute.getTimestampFromLastSample());

                from = attribute.getTimestampFromLastSample().minusDays(7);
                until = attribute.getTimestampFromLastSample().plusDays(1);

                startDate.setValue(LocalDate.of(from.getYear(), from.getMonthOfYear(), from.getDayOfMonth()));
                endDate.setValue(LocalDate.of(until.getYear(), until.getMonthOfYear(), until.getDayOfMonth()));

            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        WorkDays wd = new WorkDays(attribute.getObject());
        if (wd.getWorkdayStart() != null) workdayStart = wd.getWorkdayStart();
        if (wd.getWorkdayEnd() != null) workdayEnd = wd.getWorkdayEnd();
    }


    private void updateView() {
        updateing.set(true);
        if (from != null && until != null) {
            try {
                logger.debug("updateView from: '{}' until: '{}'", from, until);

                startDate.valueProperty().set(LocalDate.of(from.getYear(), from.getMonthOfYear(), from.getDayOfMonth()));
                endDate.valueProperty().set(LocalDate.of(until.getYear(), until.getMonthOfYear(), until.getDayOfMonth()));

                long duration = attribute.getTimestampFromLastSample().getMillis() - attribute.getTimestampFromFirstSample().getMillis();


                Tooltip minTSTooltip = new Tooltip(attribute.getTimestampFromFirstSample().toString());
                Tooltip maxTSTooltip = new Tooltip(attribute.getTimestampFromLastSample().toString());
                startDate.setTooltip(minTSTooltip);
                endDate.setTooltip(maxTSTooltip);

                //dateSlider.setBlockIncrement(duration/5);
//                dateSlider.setMajorTickUnit(duration / 5);
//                dateSlider.setMin(attribute.getTimestampFromFirstSample().getMillis());
//                dateSlider.setMax(attribute.getTimestampFromLastSample().getMillis());
//
//                dateSlider.setShowTickMarks(true);
//                dateSlider.setShowTickLabels(true);
//
//
//                dateSlider.setLowValue(from.getMillis());
//                dateSlider.setHighValue(until.getMillis());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        updateing.set(false);
    }


    public void setOnTimeRangeChange(EventHandler<ActionEvent> event) {
        //timeChanged(new ActionEvent());
        timeChangeEvent = event;
    }

    public void setOnOK(EventHandler<ActionEvent> value) {
        okEvent = value;
    }

    public void setOnCancel(EventHandler<ActionEvent> value) {
        cancelEvent = value;
    }

    /**
     * TODO: Sider != DateFields
     * Maybe add date to this functions
     *
     * @param event
     */
    private void timeChanged(ActionEvent event) {
        logger.debug("timeChanged: {},updateing: {}", event, updateing.get());
        if (!updateing.get()) {
            logger.debug("Is not updating: {}", timeChangeEvent);

            if (timeChangeEvent != null) timeChangeEvent.handle(event);

            updateView();
        }
        {
            logger.debug("Is updating");
        }
    }

    public List<JEVisSample> getSamples() {
        List<JEVisSample> sampleList = new ArrayList<>();
        try {
            FutureTask<List<JEVisSample>> futureTask = new FutureTask<List<JEVisSample>>(new Callable<List<JEVisSample>>() {
                @Override
                public List<JEVisSample> call() {
                    List<JEVisSample> samples = new ArrayList<>();

                    try {
                        DateTime fromDate = getFromDate();
                        DateTime untilDate = getUntilDate();

                        if (workdayStart.isAfter(workdayEnd)) {
                            fromDate = fromDate.minusDays(1);
                        }

                        if (fromDate.isBefore(untilDate)) {
                            return ControlPane.this.attribute.getSamples(fromDate, untilDate, true, period.toString(), ManipulationMode.NONE.toString());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    return samples;
                }
            });

            JEConfig.getStatusBar().addTask(this.getClass().getName(), futureTask, JEConfig.getImage("1415314386_Graph.png"), true);
            try {
                sampleList = futureTask.get();
            } catch (Exception ex) {
                logger.error(ex);
            }
            //logger.error("Generate Samples: object: {} attribute: '{}' from/until: {}/{} period: {}", attribute.getObject(),attribute,fromDate,untilDate,period);

            logger.error("Samples: {}", sampleList.size());

        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }

        return sampleList;
    }

    private JEVisObject getRawDataParent(JEVisObject object) throws JEVisException {
        if (object.getJEVisClassName().equals("Data") || !object.getJEVisClassName().equals("Clean Data")) {
            return object;
        } else {
            return getRawDataParent(object.getParents().get(0));
        }
    }

    public DateTimeZone getDateTimeZone() {
        return dateTimeZone;
    }

    public JEVisAttribute getAttribute() {
        return attribute;
    }

//    public DateTime getFromDate() {
//        DateTime fromDate = new DateTime(
//                startDate.getValue().getYear(),
//                startDate.getValue().getMonth().getValue(),
//                startDate.getValue().getDayOfMonth(), 0, 0).withZone(dateTimeZone);
//
//        return fromDate;
//    }
//
//    public DateTime getUntilDate() {
//        DateTime untilDate = new DateTime(
//                endDate.getValue().getYear(),
//                endDate.getValue().getMonth().getValue(),
//                endDate.getValue().getDayOfMonth(), 23, 59, 59, 999).withZone(dateTimeZone);
//
//        return untilDate;
//    }


    public void initTimeRange(DateTime from, DateTime until) {
        this.from = from;
        this.until = until;
        ActionEvent actionEvent = new ActionEvent(this, this);
        timeChanged(actionEvent);
    }

    public DateTime getFromDate() {
        return from;
    }

    public DateTime getUntilDate() {
        return until;
    }

//    private DateTime toDateTime(LocalDate date){
//        return
//        return LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth());
//    }

    public void setFromDate(DateTime from) {


        updateing.set(true);
        this.from = from;

        //new DateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0).withZone(dateTimeZone);

        LocalDate localDate = LocalDate.of(from.getYear(), from.getMonthOfYear(), from.getDayOfMonth());
        startDate.setValue(localDate);
//        dateSlider.setLowValue(from.getMillis());

        updateing.set(false);
    }

    public void setUntilDate(DateTime until) {
        updateing.set(true);
        this.until = until;
        LocalDate localDate = LocalDate.of(until.getYear(), until.getMonthOfYear(), until.getDayOfMonth());
        endDate.setValue(localDate);
//        dateSlider.setHighValue(until.getMillis());
        updateing.set(false);

    }

    private void addListeners() {
        cancel.setOnAction(event -> {
            cancelEvent.handle(event);
        });

        ok.setOnAction(event -> {
            okEvent.handle(event);
        });

        timeZoneBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            dateTimeZone = newValue;
            timeChangeEvent.handle(new ActionEvent());
        });

        startDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            logger.error("startDate event");
            if (!updateing.get()) {
                DateTime fromDate = new DateTime(
                        startDate.getValue().getYear(),
                        startDate.getValue().getMonth().getValue(),
                        startDate.getValue().getDayOfMonth(), 0, 0).withZone(dateTimeZone);
                setFromDate(fromDate);

                ActionEvent actionEvent = new ActionEvent(startDate, null);
                timeChanged(actionEvent);
            }

        });
        endDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!updateing.get()) {
                DateTime untilDate = new DateTime(
                        endDate.getValue().getYear(),
                        endDate.getValue().getMonth().getValue(),
                        endDate.getValue().getDayOfMonth(), 23, 59, 59, 999).withZone(dateTimeZone);
                setUntilDate(untilDate);

                ActionEvent actionEvent = new ActionEvent(endDate, null);
                timeChanged(actionEvent);
            }

        });

        reloadButton.setOnAction(event -> {

            final String loading = I18n.getInstance().getString("plugin.alarms.reload.progress.message");
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            updateMessage(loading);
                            try {
                                attribute.getDataSource().reloadAttribute(attribute);
                                ActionEvent actionEvent = new ActionEvent(reloadButton, null);
                                timeChanged(actionEvent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    };
                }
            };
            ProgressDialog pd = new ProgressDialog(service);
            pd.setHeaderText(I18n.getInstance().getString("plugin.reports.reload.progress.header"));
            pd.setTitle(I18n.getInstance().getString("plugin.reports.reload.progress.title"));
            pd.getDialogPane().setContent(null);

            service.start();

        });

//        dateSlider.lowValueProperty().addListener((observable, oldValue, newValue) -> {
//            if(!updateing.get()) {
//                ActionEvent actionEvent = new ActionEvent(dateSlider, null);
//
//                setFromDate(new DateTime((long)dateSlider.getLowValue()));
//                //setUntilDate(new DateTime(dateSlider.getHighValue()));
//                timeChanged(actionEvent);
//            }
//        });
//        dateSlider.highValueProperty().addListener((observable, oldValue, newValue) -> {
//            if(!updateing.get()) {
//                ActionEvent actionEvent = new ActionEvent(dateSlider, null);
//
//                //setFromDate(new DateTime(dateSlider.getLowValue()));
//                setUntilDate(new DateTime((long)dateSlider.getHighValue()));
//                timeChanged(actionEvent);
//            }
//        });
//
//        dateSlider.setLabelFormatter(new StringConverter<Number>() {
//            @Override
//            public String toString(Number object) {
//                try {
//                    //logger.error("toString: {}",object);
//                    DateTime startDate = new DateTime(object.longValue());
//                    //logger.error("toString.result: {}",startDate);
//                    return dateTimeFormatter.print(startDate);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    return "E";
//                }
//            }
//
//            @Override
//            public Number fromString(String string) {
//                try {
//                    //logger.error("fromString: {}",string);
//                    Double timeInMS = Double.parseDouble(string);
//                    //logger.error("fromString.result: {}",string);
//                    return timeInMS;
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//                return 0;
//            }
//        });

        processorField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                attribute = newValue.getAttribute("Value");
                timeChangeEvent.handle(new ActionEvent());
            } catch (Exception ex) {
                logger.fatal(ex);
            }
        });

        aggregationField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            period = newValue;
            timeChangeEvent.handle(new ActionEvent());
        });

    }
}
