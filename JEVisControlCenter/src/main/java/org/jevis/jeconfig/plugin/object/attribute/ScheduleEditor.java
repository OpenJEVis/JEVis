/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTimePicker;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.commons.json.JsonScheduler;
import org.jevis.commons.json.JsonSchedulerRule;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;

import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Benjamin Reich
 */
public class ScheduleEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(ScheduleEditor.class);
    public JEVisAttribute _attribute;
    private VBox box = new VBox();
    private BooleanProperty hasChangedProperty = new SimpleBooleanProperty(false);
    private SimpleStringProperty _newValue = new SimpleStringProperty("");
    private JsonScheduler inputValue;

    public ScheduleEditor(JEVisAttribute att) {
        _attribute = att;
    }

    public ScheduleEditor() {
        init();
    }

    public ScheduleEditor(JsonScheduler jsonScheduler) {
        this.inputValue = jsonScheduler;
        init();
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            box.getChildren().clear();
            init();
        });
    }

    private void fillTab(Tab ruleTab, JsonSchedulerRule rule) {

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(5);

        JFXTimePicker start = buildStartPicker(rule);
        JFXTimePicker end = buildEndPicker(rule);

        List<ToggleButton> buttonListMonths = buildMonthButtons(rule);
        HBox monthButtons = new HBox();
        monthButtons.getChildren().setAll(buttonListMonths);

        List<ToggleButton> buttonListDayOfMonth = buildDayOfMonthButtons(rule);
        HBox dayOfMonthButtons = new HBox();
        dayOfMonthButtons.getChildren().setAll(buttonListDayOfMonth);

        List<ToggleButton> buttonListWeekDay = buildWeekDayButtons(rule);
        HBox weekDayButtons = new HBox();
        weekDayButtons.getChildren().setAll(buttonListWeekDay);

        Label monthLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.label"));
        Label dayOfMonthLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.dayofmonth.label"));
        Label weekDayLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.weekday.label"));
        Label timeLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.time.label"));

        int row = 0;
        gridPane.add(monthLabel, 0, row, 1, 1);
        gridPane.add(monthButtons, 1, row, 2, 1);
        row++;

        gridPane.add(dayOfMonthLabel, 0, row, 1, 1);
        gridPane.add(dayOfMonthButtons, 1, row, 2, 1);
        row++;

        gridPane.add(weekDayLabel, 0, row, 1, 1);
        gridPane.add(weekDayButtons, 1, row, 2, 1);
        row++;

        gridPane.add(timeLabel, 0, row, 1, 1);
        gridPane.add(start, 1, row, 1, 1);
        gridPane.add(end, 2, row, 1, 1);

        ruleTab.setContent(gridPane);
    }

    private List<ToggleButton> buildDayOfMonthButtons(JsonSchedulerRule rule) {
        String in = rule.getDayOfMonth();

        boolean all = false;
        boolean last = false;
        List<ToggleButton> buttonList = new ArrayList<>();
        List<Integer> selected = new ArrayList<>();

        if (in != null) {
            if (in.equals("*")) {
                all = true;
            } else if (in.equals("LAST")) {
                last = true;
            } else {
                selected = stringToIntList(in);
            }
        }

        ToggleButton toggleButtonAll = new ToggleButton("All");
        if (all) toggleButtonAll.setSelected(true);

        ToggleButton toggleButtonLast = new ToggleButton("Last");
        if (last) toggleButtonLast.setSelected(true);

        List<ToggleButton> dayButtonList = new ArrayList<>();
        for (int i = 1; i < 32; i++) {
            ToggleButton toggleButton = new ToggleButton(i + "");
            if (selected.contains(i)) {
                toggleButton.setSelected(true);
            }

            int finalI = i;
            toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setDayOfMonth(updateDayOfMonthString(rule, finalI, newValue, toggleButtonAll, toggleButtonLast)));

            dayButtonList.add(toggleButton);
        }

        toggleButtonAll.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                rule.setDayOfMonth("*");
                dayButtonList.forEach(toggleButton -> toggleButton.setSelected(false));
                toggleButtonLast.setSelected(false);
            }
        });

        toggleButtonLast.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                rule.setDayOfMonth("LAST");
                dayButtonList.forEach(toggleButton -> toggleButton.setSelected(false));
                toggleButtonAll.setSelected(false);
            }

        });

        buttonList.add(toggleButtonAll);
        buttonList.addAll(dayButtonList);
        buttonList.add(toggleButtonLast);

        return buttonList;
    }

    private String updateDayOfMonthString(JsonSchedulerRule rule, int finalI, Boolean newValue, ToggleButton toggleButtonAll, ToggleButton toggleButtonLast) {
        String oldString = rule.getDayOfMonth();

        if (!oldString.contains("*") && !oldString.contains("LAST")) {
            List<Integer> list = stringToIntList(oldString);
            if (newValue) {
                if (!list.contains(finalI)) {
                    list.add(finalI);
                    return listToString(list);
                }
            } else {
                if (list.contains(finalI)) {
                    list.remove(finalI);
                    return listToString(list);
                }
            }
        } else {
            if (newValue) {
                if (toggleButtonAll.selectedProperty().get()) toggleButtonAll.setSelected(false);
                if (toggleButtonLast.selectedProperty().get()) toggleButtonLast.setSelected(false);
                return String.valueOf(finalI);
            }
        }

        return oldString;
    }

    private JFXTimePicker buildEndPicker(JsonSchedulerRule rule) {
        JFXTimePicker end = new JFXTimePicker();
        end.set24HourView(true);
        end.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));
        LocalTime endTime = null;
        if (rule.getEndTimeHours() != null && rule.getEndTimeMinutes() != null) {
            try {
                endTime = LocalTime.of(Integer.parseInt(rule.getEndTimeHours()), Integer.parseInt(rule.getEndTimeMinutes()));
            } catch (Exception e) {
                logger.error("Could not parse end time from json.");
            }
        }
        if (endTime != null) end.valueProperty().setValue(endTime);

        end.valueProperty().addListener((observable, oldValue, newValue) -> {
            rule.setEndTimeHours(String.valueOf(newValue.getHour()));
            rule.setEndTimeMinutes(String.valueOf(newValue.getMinute()));
        });
        return end;
    }

    private JFXTimePicker buildStartPicker(JsonSchedulerRule rule) {
        JFXTimePicker start = new JFXTimePicker();
        start.set24HourView(true);
        start.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));
        LocalTime startTime = null;
        if (rule.getStartTimeHours() != null && rule.getStartTimeMinutes() != null) {
            try {
                startTime = LocalTime.of(Integer.parseInt(rule.getStartTimeHours()), Integer.parseInt(rule.getStartTimeMinutes()));
            } catch (Exception e) {
                logger.error("Could not parse start time from json.");
            }
        }
        if (startTime != null) start.valueProperty().setValue(startTime);

        start.valueProperty().addListener((observable, oldValue, newValue) -> {
            rule.setStartTimeHours(String.valueOf(newValue.getHour()));
            rule.setStartTimeMinutes(String.valueOf(newValue.getMinute()));
        });

        return start;
    }

    private List<Integer> stringToIntList(String s) {
        if (Objects.nonNull(s)) {
            List<String> tempList = new ArrayList<>(Arrays.asList(s.split(", ")));
            List<Integer> integers = new ArrayList<>();
            for (String str : tempList) if (str.contains(", ")) str.replace(", ", "");

            for (String str : tempList) {
                integers.add(Integer.parseInt(str));
            }

            return integers;
        } else return new ArrayList<>();
    }

    private String listToString(List<Integer> listString) {
        if (listString != null) {
            StringBuilder sb = new StringBuilder();
            if (listString.size() > 1) {
                for (Integer i : listString) {
                    int index = listString.indexOf(i);
                    sb.append(i.toString());
                    if (index < listString.size() - 1) sb.append(", ");
                }
            } else if (listString.size() == 1) sb.append(listString.get(0));
            return sb.toString();
        } else return "";
    }

    private List<ToggleButton> buildWeekDayButtons(JsonSchedulerRule rule) {

        List<Integer> selected = stringToIntList(rule.getDayOfWeek());

        ToggleButton mon = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.weekday.mon"));
        ToggleButton tue = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.weekday.tue"));
        ToggleButton wed = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.weekday.wed"));
        ToggleButton thu = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.weekday.thu"));
        ToggleButton fri = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.weekday.fri"));
        ToggleButton sat = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.weekday.sat"));
        ToggleButton sun = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.weekday.sun"));

        for (Integer i : selected) {
            switch (i) {
                case 1:
                    mon.setSelected(true);
                    break;
                case 2:
                    tue.setSelected(true);
                    break;
                case 3:
                    wed.setSelected(true);
                    break;
                case 4:
                    thu.setSelected(true);
                    break;
                case 5:
                    fri.setSelected(true);
                    break;
                case 6:
                    sat.setSelected(true);
                    break;
                case 7:
                    sun.setSelected(true);
                    break;
            }
        }

        mon.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setDayOfWeek(updateListSelectionWeekdays(rule, 1, newValue)));
        tue.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setDayOfWeek(updateListSelectionWeekdays(rule, 2, newValue)));
        wed.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setDayOfWeek(updateListSelectionWeekdays(rule, 3, newValue)));
        thu.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setDayOfWeek(updateListSelectionWeekdays(rule, 4, newValue)));
        fri.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setDayOfWeek(updateListSelectionWeekdays(rule, 5, newValue)));
        sat.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setDayOfWeek(updateListSelectionWeekdays(rule, 6, newValue)));
        sun.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setDayOfWeek(updateListSelectionWeekdays(rule, 7, newValue)));

        return Arrays.asList(mon, tue, wed, thu, fri, sat, sun);
    }

    private String updateListSelectionWeekdays(JsonSchedulerRule rule, Integer i, Boolean newValue) {
        List<Integer> selected = stringToIntList(rule.getDayOfWeek());

        if (selected.contains(i)) {
            if (!newValue) {
                selected.remove(i);
            }
        } else {
            if (newValue) {
                selected.add(i);
            }
        }

        selected.sort(Integer::compareTo);

        return listToString(selected);
    }

    private String updateListSelectionMonthOfYear(JsonSchedulerRule rule, Integer i, Boolean newValue) {
        List<Integer> selected = stringToIntList(rule.getMonths());

        if (selected.contains(i)) {
            if (!newValue) {
                selected.remove(i);
            }
        } else {
            if (newValue) {
                selected.add(i);
            }
        }

        selected.sort(Integer::compareTo);

        return listToString(selected);
    }

    private List<ToggleButton> buildMonthButtons(JsonSchedulerRule rule) {

        List<Integer> selected = stringToIntList(rule.getMonths());

        ToggleButton jan = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jan"));
        ToggleButton feb = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.feb"));
        ToggleButton mar = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.mar"));
        ToggleButton apr = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.apr"));
        ToggleButton may = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.may"));
        ToggleButton jun = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jun"));
        ToggleButton jul = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jul"));
        ToggleButton aug = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.aug"));
        ToggleButton sep = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.sep"));
        ToggleButton oct = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.oct"));
        ToggleButton nov = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.nov"));
        ToggleButton dec = new ToggleButton(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.dec"));

        for (Integer i : selected) {
            switch (i) {
                case 1:
                    jan.setSelected(true);
                    break;
                case 2:
                    feb.setSelected(true);
                    break;
                case 3:
                    mar.setSelected(true);
                    break;
                case 4:
                    apr.setSelected(true);
                    break;
                case 5:
                    may.setSelected(true);
                    break;
                case 6:
                    jun.setSelected(true);
                    break;
                case 7:
                    jul.setSelected(true);
                    break;
                case 8:
                    aug.setSelected(true);
                    break;
                case 9:
                    sep.setSelected(true);
                    break;
                case 10:
                    oct.setSelected(true);
                    break;
                case 11:
                    nov.setSelected(true);
                    break;
                case 12:
                    dec.setSelected(true);
                    break;
            }
        }

        jan.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 1, newValue)));
        feb.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 2, newValue)));
        mar.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 3, newValue)));
        apr.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 4, newValue)));
        may.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 5, newValue)));
        jun.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 6, newValue)));
        jul.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 7, newValue)));
        aug.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 8, newValue)));
        sep.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 9, newValue)));
        oct.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 10, newValue)));
        nov.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 11, newValue)));
        dec.selectedProperty().addListener((observable, oldValue, newValue) -> rule.setMonths(updateListSelectionMonthOfYear(rule, 12, newValue)));

        return Arrays.asList(jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec);
    }

    @Override
    public boolean hasChanged() {
        return hasChangedProperty.getValue();
    }

    @Override
    public void commit() throws JEVisException {

    }

    @Override
    public Node getEditor() {
        try {
            init();
        } catch (Exception ex) {
            logger.catching(ex);
        }

        return box;
    }

    private void init() {
        if (inputValue == null) {
            inputValue = createDefaultConfig();
        }

        ObservableList<String> timeZones = FXCollections.observableArrayList();
        timeZones.add("");
        timeZones.add("UTC");
        timeZones.add("Europe/Berlin");

        timeZones.addAll(FXCollections.observableArrayList(org.joda.time.DateTimeZone.getAvailableIDs()));

        JFXComboBox jfxComboBoxTimeZone = new JFXComboBox(timeZones);

        jfxComboBoxTimeZone.getSelectionModel().select(inputValue.getTimezone());

        Callback<ListView<String>, ListCell<String>> cellFactoryTimeZone = new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }

                };
            }

        };
        jfxComboBoxTimeZone.setCellFactory(cellFactoryTimeZone);
        jfxComboBoxTimeZone.setButtonCell(cellFactoryTimeZone.call(null));

        jfxComboBoxTimeZone.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            inputValue.setTimezone(newValue.toString());
        });

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        List<JsonSchedulerRule> schedulerRuleList = inputValue.getRules();
        for (JsonSchedulerRule rule : schedulerRuleList) {
            int index = schedulerRuleList.indexOf(rule);
            if (rule.getId() == null) rule.setId(String.valueOf(index));
            Tab newTab = new Tab(index + "");
            tabPane.getTabs().add(newTab);
            fillTab(newTab, rule);
        }

        JFXButton jfxButtonAddRule = new JFXButton();
        jfxButtonAddRule.setGraphic(JEConfig.getImage("list-add.png", 16, 16));
        jfxButtonAddRule.setOnAction(event -> {
            JsonSchedulerRule newRule = new JsonSchedulerRule();
            int max = 0;
            for (JsonSchedulerRule jsonSchedulerRule : inputValue.getRules()) {
                if (jsonSchedulerRule.getId() != null) {
                    try {
                        int val = Integer.parseInt(jsonSchedulerRule.getId());
                        if (val > max) max = val;
                    } catch (Exception e) {
                        logger.error("Could not parse jsonSchedulerRule.getId()");
                    }
                }
            }
            String name = String.valueOf(max + 1);
            newRule.setId(name);

            inputValue.getRules().add(newRule);
            Tab newTab = new Tab(name);
            fillTab(newTab, newRule);
            tabPane.getTabs().add(newTab);
        });

        JFXButton jfxButtonDeleteRule = new JFXButton();
        jfxButtonDeleteRule.setGraphic(JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", 16, 16));
        jfxButtonDeleteRule.setOnAction(event -> {
            int index = tabPane.getSelectionModel().getSelectedIndex();
            inputValue.getRules().remove(index);
            tabPane.getTabs().remove(index);
        });

        HBox addDeleteBox = new HBox();
        addDeleteBox.getChildren().setAll(jfxButtonAddRule, jfxButtonDeleteRule);

        box.getChildren().setAll(jfxComboBoxTimeZone, addDeleteBox, tabPane);
    }

    private JsonScheduler createDefaultConfig() {
        return new JsonScheduler();
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return hasChangedProperty;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        //TODO
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }

    public void setInputValue(JsonScheduler inputValue) {
        this.inputValue = inputValue;
    }

    public SimpleStringProperty _newValueProperty() {
        return _newValue;
    }
}
