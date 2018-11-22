/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.RangeSlider;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

/**
 * @author Benjamin Reich
 * @deprecated This class is work in prozess
 */
public class ScheduleEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(ScheduleEditor.class);
    GridPane _editor = new GridPane();

    public JEVisAttribute _attribute;
    private boolean _hasChanged = false;
    private Button _treeButton;
    private boolean _readOnly = true;
    private JEVisSample newSample;
    private long lowValue;
    private long highValue;
    private int _times;
    private int _days;
    private int _newValue = 0;
    private int _oldValue = 0;
    private BooleanProperty hasChangedProperty = new SimpleBooleanProperty(false);

    public ScheduleEditor(JEVisAttribute att) {
        _editor.setAlignment(Pos.CENTER);
        _editor.setHgap(10);
        _editor.setVgap(10);
        _editor.setPadding(new Insets(25, 25, 25, 25));
        _attribute = att;
        buildGUI();

    }

    public boolean isDayInPeriod(int val) {
        return isValueSelected(val, _days);
    }

    public boolean isTimeInPeriod(int val) {
        return isValueSelected(val, _times);
    }

    private int getEndTime() {
        for (int i = 23; i > 0; i--) {
            if (isTimeInPeriod(i)) {
                return i - 1;//because its alway hour:59min
            }
        }

        return 0;
    }

    private int getStartTime() {
        for (int i = 0; i < 24; i++) {
            if (isTimeInPeriod(i)) {
                return i;
            }
        }
        return 0;
    }

    public boolean isValueSelected(int val, int n) {
        try {
            int tmp = n;
            int bitmask = 1;
            bitmask = bitmask << (val);
            return ((tmp & bitmask) != 0);
        } catch (Exception ex) {
            return false;
        }
        //System.err.println(Integer.toBinaryString());
    }

    /**
     * @param sun
     * @param sat
     * @param fri
     * @param thurs
     * @param wed
     * @param tues
     * @param mon
     * @param startTime
     * @param endTime
     * @return
     */
    public int buildBitmap(int sun, int sat, int fri, int thurs, int wed, int tues, int mon, int startTime, int endTime) {
        int[] mask = new int[31]; //31bit is allways 0
        mask[30] = sun;
        mask[29] = sat;
        mask[28] = fri;
        mask[27] = thurs;
        mask[26] = wed;
        mask[25] = tues;
        mask[24] = mon;

//        logger.info("StartTime: " + startTime + "    - Endtime: " + endTime + "\n");
        for (int i = startTime; i < endTime; i++) {
            logger.info("[" + i + "]=1");
            mask[i] = 1;
        }

//        System.out.print("bin mask for " + _attribute.getName() + ": ");
//        for (int i = 0; i <= mask.length - 1; i++) {
//            System.out.print(mask[i]);
//        }
//        logger.info("");
        int result = 0;
        for (int i = 0; i < 31; i++) {
            int bit_wert = (int) Math.pow(2, i);
            result = result + bit_wert * mask[i];
        }

        logger.info("Restult for " + _attribute.getName() + ": " + result);
        return result;

    }

    private void buildGUI() {
//        logger.info("build gui Schedule");

        BooleanProperty block = new SimpleBooleanProperty(false);

        RangeSlider sl = new RangeSlider(0, 24, 0, 24);
        StringConverter<Number> stringConverter = new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return object.intValue() + "";
            }

            @Override
            public Number fromString(String string) {
                try {
                    int intValue = Integer.parseInt(string);
                    if (intValue > 24) {
                        return 24;
                    } else if (intValue < 0) {
                        return 0;
                    } else {
                        return intValue;
                    }
                } catch (Exception ex) {
                    return 0;
                }
            }
        };

//        sl.setLabelFormatter(stringConverter);

        TextField lowValueTextField = new TextField();
        TextField highValueTextField = new TextField();

        lowValueTextField.setPrefWidth(70);
        highValueTextField.setPrefWidth(70);

        HBox.setMargin(lowValueTextField, new Insets(0, 5, 0, 5));
        HBox.setMargin(highValueTextField, new Insets(0, 5, 0, 5));

        lowValueTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                if (!block.getValue()) {
                    block.setValue(true);
                    sl.setLowValue(stringConverter.fromString(newValue).doubleValue());
                    block.setValue(false);
                }
            }
        });

        lowValueTextField.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) -> {
            if (newPropertyValue) {
                block.setValue(true);
            } else {
                sl.setLowValue(stringConverter.fromString(lowValueTextField.getText()).doubleValue());
                block.setValue(false);
            }
        });

        highValueTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                if (!block.getValue()) {
                    block.setValue(true);
                    sl.setHighValue(stringConverter.fromString(newValue).doubleValue());
                    block.setValue(false);
                }
            }
        });

        highValueTextField.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) -> {
            if (newPropertyValue) {
                block.setValue(true);
            } else {
                sl.setHighValue(stringConverter.fromString(highValueTextField.getText()).doubleValue());
                block.setValue(false);
            }
        });

        Label weekdays = new Label("-");
        HBox.setMargin(weekdays, new Insets(0, 5, 0, 5));

        sl.setPrefWidth(250);
        sl.setShowTickMarks(true);
        sl.setShowTickLabels(true);
        sl.setBlockIncrement(1);

//        sl.showTickLabelsProperty().setValue(true);
        //step size 15 * 60 secs => 15 minutes
        sl.setMajorTickUnit(2);
        sl.setMinorTickCount(1);
        sl.setSnapToTicks(true);

        sl.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                if (!block.getValue()) {
                    block.setValue(true);
                    lowValueTextField.setText(stringConverter.toString(newValue));
                    block.setValue(false);
                }
            }
        });

        sl.lowValueChangingProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) -> {
            if (newPropertyValue) {
                block.setValue(true);
            } else {
                block.setValue(false);
            }
        });

        sl.highValueProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                if (!block.getValue()) {
                    //block.setValue(true);
//                    logger.info("hight new value; " + newValue);
                    highValueTextField.setText(stringConverter.toString(newValue));
                    //block.setValue(false);
                }
            }
        });

        sl.highValueChangingProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) -> {
            if (newPropertyValue) {
                block.setValue(true);
            } else {
                block.setValue(false);
            }
        });

        _editor.getStylesheets().add("/styles/ScheduleEditor.css");

        //Weekdays
        ToggleButton monday = new ToggleButton("Mo");
        ToggleButton tuesday = new ToggleButton("Tu");
        ToggleButton wednesday = new ToggleButton("We");
        ToggleButton thursday = new ToggleButton("Th");
        ToggleButton friday = new ToggleButton("Fr");
        ToggleButton saturday = new ToggleButton("Sa");
        ToggleButton sunday = new ToggleButton("Su");

        addChangeListener(sl, monday, tuesday, wednesday, thursday, friday, saturday, sunday);

        HBox box = new HBox(5);
        HBox.setHgrow(monday, Priority.ALWAYS);
        HBox.setHgrow(tuesday, Priority.ALWAYS);
        HBox.setHgrow(wednesday, Priority.ALWAYS);
        HBox.setHgrow(thursday, Priority.ALWAYS);
        HBox.setHgrow(friday, Priority.ALWAYS);
        HBox.setHgrow(saturday, Priority.ALWAYS);
        HBox.setHgrow(sunday, Priority.ALWAYS);

        box.getChildren().addAll(monday, tuesday, wednesday, thursday, friday, saturday, sunday);
//        _editor.add(box, 0, 0, 4, 1);

        lowValueTextField.setMaxWidth(40d);
        highValueTextField.setMaxWidth(40d);

        Label weeklabel = new Label("Weekday:");
        Label timeLabel = new Label("Time:");
        GridPane.setHgrow(box, Priority.ALWAYS);

        //------------------------------x--y--xw--yw
        _editor.add(weeklabel, 0, 0);
        _editor.add(box, 1, 0, 3, 1);

        _editor.add(timeLabel, 0, 1);
        _editor.add(lowValueTextField, 1, 1);
        _editor.add(sl, 2, 1);
        _editor.add(highValueTextField, 3, 1);

//        _editor.getChildren().addAll(lowValueTextField, label1, highValueTextField, sl);
//        logger.info("start fun");
        //Load settings ind JEVisDB
        try {

            JEVisSample sample = _attribute.getLatestSample();
            boolean hasSample = sample != null;
//                   int thuB = sun.isSelected() ? 1 : 0;

            int bitMask = 0;
            if (hasSample) {
                bitMask = (int) sample.getValueAsLong().longValue();

                _times = bitMask;
                _days = ((_times >> 24) & 0xFF);
                _days = _days * 2; // shift of 1 to the left (days = 1,2,..7 - check bit 1 not 0)

                _oldValue = bitMask;
            }

//            logger.info("times value: " + _times);
//            logger.info("_days value: " + _days);
            if (hasSample) {
                int startHour = getStartTime();
                int endHour = getEndTime();

                lowValueTextField.setText("" + (startHour));
                highValueTextField.setText("" + (endHour));
//                logger.info("time: " + startHour + " - " + endHour);

                monday.setSelected(isDayInPeriod(1));
                tuesday.setSelected(isDayInPeriod(2));
                wednesday.setSelected(isDayInPeriod(3));
                thursday.setSelected(isDayInPeriod(4));
                friday.setSelected(isDayInPeriod(5));
                saturday.setSelected(isDayInPeriod(6));
                sunday.setSelected(isDayInPeriod(7));

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * @param rs
     * @param mon
     * @param tu
     * @param wed
     * @param thu
     * @param fri
     * @param sat
     * @param sun
     * @return
     */
    private void addChangeListener(RangeSlider rs, ToggleButton mon, ToggleButton tu, ToggleButton wed, ToggleButton thu, ToggleButton fri, ToggleButton sat, ToggleButton sun) {
        EventHandler handler = new EventHandler() {
            @Override
            public void handle(Event event) {
//                logger.info("EventSource: " + event.getSource().toString());
//                logger.info("mon: " + mon.isSelected());
//                logger.info("rs: " + rs.getLowValue() + " - " + rs.getHighValue());

                int sunB = sun.isSelected() ? 1 : 0;
                int satB = sat.isSelected() ? 1 : 0;
                int friB = fri.isSelected() ? 1 : 0;
                int tuB = tu.isSelected() ? 1 : 0;
                int thuB = thu.isSelected() ? 1 : 0;
                int webB = wed.isSelected() ? 1 : 0;
                int monB = mon.isSelected() ? 1 : 0;

                int map = buildBitmap(sunB, satB, friB, tuB, webB, thuB, monB, (int) rs.getLowValue(), (int) rs.getHighValue());
//                logger.info("Result: " + map);
                _newValue = map;
                hasChangedProperty.setValue(true);
            }
        };

        rs.addEventHandler(EventType.ROOT, handler);
        mon.addEventHandler(EventType.ROOT, handler);
        tu.addEventHandler(EventType.ROOT, handler);
        wed.addEventHandler(EventType.ROOT, handler);
        thu.addEventHandler(EventType.ROOT, handler);
        fri.addEventHandler(EventType.ROOT, handler);
        sat.addEventHandler(EventType.ROOT, handler);
        sun.addEventHandler(EventType.ROOT, handler);

    }

    @Override
    public boolean hasChanged() {
        return hasChangedProperty.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        logger.info("save new value: " + _newValue);
        if (hasChanged()) {
            JEVisSample newSample = _attribute.buildSample(new DateTime(), _newValue);
            newSample.commit();
            _oldValue = _newValue;
            hasChangedProperty.setValue(false);
        }
    }

    @Override
    public Node getEditor() {
        return _editor;
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

}
