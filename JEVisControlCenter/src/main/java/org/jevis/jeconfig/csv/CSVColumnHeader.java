/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.csv;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisUnit;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.jevistree.plugin.SimpleTargetPlugin;
import org.jevis.jeconfig.application.unit.UnitChooserDialog;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVColumnHeader {

    private static final Logger logger = LogManager.getLogger(CSVColumnHeader.class);
    private static double FIELD_WIDTH = 210;
    private static double ROW_HIGHT = 25;
    final Button unitButton = new Button(I18n.getInstance().getString("csv.table.unit"));
    private final VBox root = new VBox(5);
    Label typeL = new Label(I18n.getInstance().getString("csv.table.meaning"));
    Label formateL = new Label(I18n.getInstance().getString("csv.table.format"));
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
    private JEVisAttribute _target = null;
    private ComboBox<String> meaning;
    private HashMap<Integer, CSVLine> _lines = new HashMap<Integer, CSVLine>();
    private HashMap<Integer, SimpleObjectProperty<Node>> _valuePropertys = new HashMap<Integer, SimpleObjectProperty<Node>>();
    private HashMap<Integer, CSVCellGraphic> _valueGraphic = new HashMap<Integer, CSVCellGraphic>();
    private DateTimeZone _selectedTimeZone = DateTimeZone.getDefault();
    private CSVTable _table;
    private String _currentFormate;

    private String _groupingSeparator;
    private char _decimalSeparator;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat();
    private Meaning currentMeaning = Meaning.Ignore;
    private int coloumNr = -1;

    public CSVColumnHeader(CSVTable table, int column) {
        coloumNr = column;
        _table = table;

        root.setPrefHeight(110);

        buildMeaningButton();
        buildIgnoreGraphic();
    }

    public double getValueAsDouble(String value) {
//        DecimalFormat df = new DecimalFormat("#.#", symbols);
//        logger.info("org value: " + value);
//        logger.info("Seperator in use: " + symbols.getDecimalSeparator());
//        String tmpValue = value;
//
//        if (getDecimalSeparator() == ',') {
//            tmpValue = tmpValue.replace('.', ' ');//removeall grouping chars
//        } else {
//            tmpValue = tmpValue.replace(',', ' ');//removeall grouping chars
//        }
//        tmpValue = tmpValue.replaceAll(" ", "");
//        tmpValue = tmpValue.trim();//some locales use the spaceas grouping
//        logger.info("Value after fix: " + tmpValue);
//
//        Number number = df.parse(tmpValue);

        String tmpValue = value;
        if (getDecimalSeparator() == ',') {
            tmpValue = tmpValue.replace('.', ' ');//removeall grouping chars
            tmpValue = tmpValue.replaceAll(",", ".");
        } else {
            tmpValue = tmpValue.replace(',', ' ');//removeall grouping chars
        }
        tmpValue = tmpValue.replaceAll(" ", "");

        Double number = Double.valueOf(tmpValue);

        return number;
    }

    public void formatAllRows() {
//        _table.setScrollBottom();
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                _table.setScrollBottom();
//            }
//        });

        Iterator it = _valuePropertys.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
//            logger.info(pairs.getKey() + " = " + pairs.getValue());

            SimpleObjectProperty prop = (SimpleObjectProperty) pairs.getValue();
            CSVCellGraphic graphic = _valueGraphic.get(pairs.getKey());
            CSVLine csvLIne = _lines.get(pairs.getKey());

            graphic.setText(getFormatedValue(csvLIne.getColumn(coloumNr)));
            graphic.setValid(valueIsValid(csvLIne.getColumn(coloumNr)));
            graphic.setToolTipText("Original: '" + csvLIne.getColumn(coloumNr) + "'");

            if (getMeaning() == Meaning.Ignore) {
                graphic.setIgnore();
                graphic.getGraphic().setDisable(true);
            } else {
                graphic.getGraphic().setDisable(false);
            }

            prop.setValue(graphic.getGraphic());

        }

//        _table.setLastScrollPosition();
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                _table.setLastScrollPosition();
//            }
//        });
    }

    public int getColumn() {
        return coloumNr;
    }

    public JEVisAttribute getTarget() {
        return _target;
    }

    private String getCurrentFormate() {
        return _currentFormate;
    }

    private String getGroupingSeparator() {
        return _groupingSeparator;
    }

    private char getDecimalSeparator() {
        return _decimalSeparator;
    }

    public SimpleObjectProperty getValueProperty(CSVLine line) {
        int lineNumber = line.getRowNumber();
        if (_valuePropertys.containsKey(lineNumber)) {
            return _valuePropertys.get(lineNumber);
        } else {
            _lines.put(lineNumber, line);

            CSVCellGraphic graphic = new CSVCellGraphic(line.getColumn(coloumNr));
            _valueGraphic.put(lineNumber, graphic);
            graphic.setText(getFormatedValue(line.getColumn(coloumNr)));
            graphic.setValid(valueIsValid(line.getColumn(coloumNr)));
            graphic.setToolTipText(line.getColumn(coloumNr));

            if (getMeaning() == Meaning.Ignore) {
                graphic.setIgnore();
                graphic.getGraphic().setDisable(true);
            }

            _valuePropertys.put(lineNumber, new SimpleObjectProperty<>(graphic.getGraphic()));
            return _valuePropertys.get(lineNumber);
        }
    }

    public String getFormatedValue(String value) {
//        logger.info("get formatedt value: " + value);
        try {

            switch (currentMeaning) {
                case Date:
                    Date date = getDateFormater().parse(value);
                    return getDateFormater().format(date);
                case DateTime:
                    Date datetime = getDateFormater().parse(value);
                    return getDateFormater().format(datetime);
                case Time:
                    Date time = getDateFormater().parse(value);
                    return getDateFormater().format(time);
                case Value:
                    //hmm lokks some kinde if strage i bet there is a better ways
                    DecimalFormat df = new DecimalFormat("###,###,###,###,###,###,###,###,###,##0.00###################################");
//                    String unit = "";
//
                    if (getTarget() != null && getTarget().getInputUnit() != null) {
                        JEVisUnit unit = getTarget().getInputUnit();
                        logger.debug("Value with unit: " + df.format(getValueAsDouble(value)) + unit.getLabel());
                        return df.format(getValueAsDouble(value)) + unit.getLabel();
                    }
//                    logger.info("unit.format: " + unit);

//                    return df.format(getValueAsDouble(value)) + unit;
                    return getValueAsDouble(value) + "";
                case Index:
                    break;
                case Ignore:
                    return value;
//                    logger.info("To Ignore");
            }
        } catch (Exception pe) {
            logger.error(pe);
            return value;
        }
        return value;
    }

    /**
     * @param value
     * @return
     * @throws ParseException
     */
    public String getTextValue(String value) throws ParseException {
        //TODO: mybee
        if (getMeaning() == Meaning.Value) {
            return value;
        } else {
            throw new ParseException(value, coloumNr);
        }
    }

    /**
     * @param value
     * @return
     * @throws ParseException
     */
    public DateTime getValueAsDate(String value) throws ParseException {
        if (getMeaning() == Meaning.Date || getMeaning() == Meaning.DateTime || getMeaning() == Meaning.Time) {
            Date datetime = getDateFormater().parse(value);
            datetime.getTime();
            return new DateTime(datetime).withZoneRetainFields(getTimeZone());

        } else {
            throw new ParseException(value, coloumNr);

        }
    }

    public SimpleDateFormat getDateFormater() {
        return dateFormatter;
    }

    /**
     * TODO replace checks with the later uses functions like getValueAsDate
     *
     * @param value
     * @return
     */
    public boolean valueIsValid(String value) {
        try {

            switch (currentMeaning) {
                case Date:
                    Date date = getDateFormater().parse(value);
                    date.getTime();
                    return true;
                case DateTime:
                    Date datetime = getDateFormater().parse(value);
                    datetime.getTime();
                    return true;
                case Time:
                    Date time = getDateFormater().parse(value);
                    time.getTime();
                    return true;
                case Value:

                    getValueAsDouble(value);
                    return true;

//                    symbols.setDecimalSeparator(getDecimalSeparator());
//                    DecimalFormat df = new DecimalFormat("#,#", symbols);
//                    String tmpValue = value;
//
//                    if (getDecimalSeparator() == ',') {
//                        tmpValue = tmpValue.replace('.', ' ');//removeall grouping chars
//                    } else {
//                        tmpValue = tmpValue.replace(',', ' ');//removeall grouping chars
//                    }
//                    tmpValue = tmpValue.trim();//some locales use the spaceas grouping
//
//                    Number number = df.parse(tmpValue);
//                    Double dValue = number.doubleValue();
//
//                    logger.info("Value is valid: " + dValue);
//                    return true;
                case Text:
                    //TODO maybe check for .... if the attriute is from type string
                    return true;
                case Index:
                    return true;
                case Ignore:
                    return true;
            }
        } catch (Exception pe) {
            return false;
        }
        return false;
    }

    public DateTimeZone getTimeZone() {
        return _selectedTimeZone;
    }

    public Meaning getMeaning() {
        return currentMeaning;
    }

    private void setMeaning(Meaning meaning) {
        currentMeaning = meaning;

        switch (meaning) {
            case Date:
                buildDateTime(Meaning.Date);
                break;
            case DateTime:
                buildDateTime(Meaning.DateTime);
                break;
            case Time:
                buildDateTime(Meaning.Time);
                break;
            case Value:
                buildValueGraphic();
                break;
            case Text:
                buildTextGraphic();
                break;
            case Index:
                break;
            case Ignore:
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        buildIgnoreGraphic();
                    }
                });

        }

        formatAllRows();
    }

    private void buildMeaningButton() {
        ObservableList<String> options = FXCollections.observableArrayList();

        for (Meaning meaningEnum : Meaning.values()) {
            options.add(meaningEnum.name());
        }

        meaning = new ComboBox<String>(options);
        meaning.getSelectionModel().selectFirst();

        meaning.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {

                if (t1 != null) {
                    setMeaning(Meaning.valueOf(t1));
                }

            }
        });

        meaning.setPrefSize(FIELD_WIDTH, ROW_HIGHT);
    }

    private void buildTextGraphic() {
        root.setPadding(new Insets(8, 8, 8, 8));

        Label targetL = new Label("Target:");
        Button targetB = buildTargetButton();

        Region spacer = new Region();

        GridPane gp = new GridPane();
        gp.setHgap(5);
        gp.setVgap(5);
        root.getChildren().setAll(gp);

        //x , y
        gp.add(typeL, 0, 0);
        gp.add(meaning, 1, 0);

        gp.add(spacer, 1, 1);

        gp.add(targetL, 0, 2);
        gp.add(targetB, 1, 2);

        spacer.setPrefSize(FIELD_WIDTH, ROW_HIGHT);
        meaning.setPrefSize(FIELD_WIDTH, ROW_HIGHT);
        targetB.setPrefSize(FIELD_WIDTH, ROW_HIGHT);

        gp.setAlignment(Pos.TOP_LEFT);
        GridPane.setHalignment(typeL, HPos.LEFT);

    }

    private void buildValueGraphic() {
        root.setPadding(new Insets(8, 8, 8, 8));

        ToggleGroup deciSepGroup = new ToggleGroup();
        Label deciSeperator = new Label(I18n.getInstance().getString("csv.deci_seperator"));
        final RadioButton comma = new RadioButton(I18n.getInstance().getString("csv.comma"));
        comma.setId("commaRadio");
        final RadioButton dot = new RadioButton(I18n.getInstance().getString("csv.dot"));
        dot.setId("dotRadio");
        Label targetL = new Label(I18n.getInstance().getString("csv.target"));
        Label unitLabel = new Label(I18n.getInstance().getString("csv.unit"));
        final Button unitButton = new Button(I18n.getInstance().getString("csv.table.unit"));
//        unitButton.setDisable(true);
        unitButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {

                try {
                    if (_target != null) {

                        UnitChooserDialog dia = new UnitChooserDialog();
                        dia.show(JEConfig.getStage(), _target);

                    } else {
                        //TODO reimplement unit
//                        Unit kwh = SI.KILO(SI.WATT.times(NonSI.HOUR));
//                        UnitChooserDialog dia = new UnitChooserDialog();
//                        dia.showSelector(JEConfig.getStage(), kwh, "");
                    }

                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }
        });

        dot.setToggleGroup(deciSepGroup);
        comma.setToggleGroup(deciSepGroup);

        deciSepGroup.selectToggle(dot);
        _decimalSeparator = '.';
        symbols.setDecimalSeparator(_decimalSeparator);

        deciSepGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
//                logger.info("Seperator changed: " + t1);
                if (t1.equals(comma)) {
//                    logger.info("sep is now ,");
                    _decimalSeparator = ',';

                } else if (t1.equals(dot)) {
//                    logger.info("sep is now .");
                    _decimalSeparator = '.';
                }
                symbols.setDecimalSeparator(_decimalSeparator);
                formatAllRows();

            }
        });

        HBox spebox = new HBox(10);
        spebox.setAlignment(Pos.CENTER_LEFT);

        spebox.getChildren().setAll(deciSeperator, dot, comma);

        Button targetB = buildTargetButton();

        GridPane gp = new GridPane();
        gp.setHgap(5);
        gp.setVgap(5);
        root.getChildren().setAll(gp);

        //x , y
        gp.add(typeL, 0, 0);
        gp.add(meaning, 1, 0);

//        gp.add(deciSeperator, 0, 1);
        gp.add(spebox, 0, 1, 2, 1);

        gp.add(targetL, 0, 2);
        gp.add(targetB, 1, 2);

        //disabled because its not working
//        gp.add(unitLabel, 0, 3);
//        gp.add(unitButton, 1, 3);
        GridPane.setHgrow(spebox, Priority.ALWAYS);
        GridPane.setHgrow(targetB, Priority.ALWAYS);
        GridPane.setHgrow(meaning, Priority.ALWAYS);

        gp.setAlignment(Pos.TOP_LEFT);
        GridPane.setHalignment(typeL, HPos.LEFT);
        //preite ,hoehe
        meaning.setPrefSize(FIELD_WIDTH, ROW_HIGHT);
        targetB.setPrefSize(FIELD_WIDTH, ROW_HIGHT);
        spebox.setPrefHeight(ROW_HIGHT);
        unitButton.setPrefSize(FIELD_WIDTH, ROW_HIGHT);
    }

    private void buildDateTime(Meaning mode) {
        root.setPadding(new Insets(8, 8, 8, 8));

        final ComboBox<String> timeZone;
        ComboBox<String> timeLocale;
        final TextField format = new TextField();
        Label timeZoneL = new Label(I18n.getInstance().getString("csv.timezone"));
        Label targetL = new Label(I18n.getInstance().getString("csv.target"));
        Label vaueLocaleL = new Label(I18n.getInstance().getString("csv.locale"));

        format.setPromptText(I18n.getInstance().getString("csv.format.prompt"));

        ObservableList<String> timeZoneOpt = FXCollections.observableArrayList();
        Set<String> allTimeZones = DateTimeZone.getAvailableIDs();

        timeZoneOpt = FXCollections.observableArrayList(allTimeZones);
        timeZone = new ComboBox<>(timeZoneOpt);
//        timeZone.getSelectionModel().select("UTC");
        timeZone.getSelectionModel().select(TimeZone.getDefault().getID());
        timeZone.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                _selectedTimeZone = DateTimeZone.forID(timeZone.getSelectionModel().getSelectedItem());
            }
        });

        switch (mode) {
            case DateTime:

                format.setText(findDateTimePattern());
                dateFormatter = new SimpleDateFormat(format.getText());
                break;
            case Date:
                dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                format.setText("yyyy-MM-dd");
                break;
            case Time:
                dateFormatter = new SimpleDateFormat("HH:mm:ss");
                format.setText("HH:mm:ss");
                break;
        }


        format.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                _currentFormate = format.getText();
                dateFormatter = new SimpleDateFormat(_currentFormate);
                formatAllRows();
            }
        });

        HBox boxFormate = new HBox(5);
        ImageView help = JEConfig.getImage("1404161580_help_blue.png", 22, 22);
        boxFormate.getChildren().setAll(format, help);

        help.setStyle("-fx-background-color: \n"
                + "        rgba(0,0,0,0.08);\n"
                + "    -fx-background-insets: 0 0 -1 0,0,1;\n"
                //                + "    -fx-background-radius: 5,5,4;\n"
                //                + "    -fx-padding: 3 30 3 30;\n"
                + "    -fx-text-fill: #242d35;\n"
                + "    -fx-font-size: 14px;");

        help.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                showHelp();
            }
        });

        //Damn workaround for fu***** layouts
//        typeL.setPrefWidth(100);
        meaning.setPrefWidth(FIELD_WIDTH);
        timeZone.setPrefWidth(FIELD_WIDTH);
        format.setPrefWidth(FIELD_WIDTH);

        boxFormate.setPrefSize(FIELD_WIDTH, ROW_HIGHT);
        meaning.setPrefSize(FIELD_WIDTH, ROW_HIGHT);
        timeZone.setPrefSize(FIELD_WIDTH, ROW_HIGHT);

        GridPane gp = new GridPane();
        gp.setHgap(5);
        gp.setVgap(5);
        root.getChildren().setAll(gp);

        //x , y
        gp.add(typeL, 0, 0);
        gp.add(meaning, 1, 0);

        gp.add(formateL, 0, 1);
        gp.add(boxFormate, 1, 1);

        gp.add(timeZoneL, 0, 2);
        gp.add(timeZone, 1, 2);

        GridPane.setHalignment(typeL, HPos.LEFT);
    }

    private void buildIgnoreGraphic() {
//        root.getChildren().removeAll();
        root.setPadding(new Insets(8, 8, 8, 8));

        GridPane gp = new GridPane();
        gp.setHgap(5);
        gp.setVgap(5);
        root.getChildren().setAll(gp);

        //x , y
        gp.add(typeL, 0, 0);
        gp.add(meaning, 1, 0);

        GridPane.setHalignment(typeL, HPos.LEFT);

    }

    /**
     * Try to find an matching DateTime pattern for the given Date String. This
     * implemtaion is very basic.
     * s
     *
     * @return
     */
    private String findDateTimePattern() {

        //SimpleObjectProperty<Node>> _valuePropertys
        String valueString = "";
        int workaround = 0;
        Iterator it = _lines.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, CSVLine> pairs = (Map.Entry) it.next();
            workaround++;
            if (workaround == 3) {
                valueString = pairs.getValue().getColumn(coloumNr);
            } else if (workaround > 3) {
                break;
            }

        }


        //Best formats are first in list
        String[] pattern = {
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd HH:mm:ss Z",
                "yyyy-MM-dd HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss Z",
                "dd-MM-yyyy HH:mm:ss"
        };

        DateTime minDate = new DateTime(1980, 1, 1, 1, 0, 0, 0);
        DateTime maxDate = DateTime.now().plusYears(2);

        for (int i = 0; i < pattern.length; i++) {
            try {
                DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern[i]);
                DateTime parsedTime = dtf.parseDateTime(valueString);
                if (parsedTime.isAfter(minDate) && parsedTime.isBefore(maxDate)) {
                    return pattern[i];
                }
            } catch (Exception ex) {
                logger.fatal(ex);
            }
        }

        return "yyyy-MM-dd HH:mm:ss";
    }

    private Button buildTargetButton() {
        final Button button = new Button(I18n.getInstance().getString("csv.import_target"));//, JEConfig.getImage("1404843819_node-tree.png", 15, 15));
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {

                List<JEVisTreeFilter> allFilter = new ArrayList<>();
                allFilter.add(SelectTargetDialog.buildAllAttributesFilter());


                SelectTargetDialog selectionDialog = new SelectTargetDialog(allFilter, null, SelectionMode.SINGLE);

                selectionDialog.setMode(SimpleTargetPlugin.MODE.ATTRIBUTE);
                if (selectionDialog.show(
                        _table.getDataSource(),
                        I18n.getInstance().getString("csv.target.title"),
                        new ArrayList<UserSelection>()
                ) == SelectTargetDialog.Response.OK) {
                    logger.trace("Selection Done");
                    for (UserSelection us : selectionDialog.getUserSelection()) {
                        try {
                            String buttonText = "";
                            if (us.getSelectedObject() != null) {
                                logger.trace("us: {}", us.getSelectedObject().getID());
                                buttonText += us.getSelectedObject().getName();
                            }
                            if (us.getSelectedAttribute() != null) {
                                logger.trace("att: {}", us.getSelectedAttribute().getName());
                                _target = us.getSelectedAttribute();
                                buttonText += "." + _target.getName();
                            }

                            button.setText(buttonText);

                            if (us.getSelectedAttribute() != null && us.getSelectedAttribute().getInputUnit() != null) {
                                unitButton.setText(us.getSelectedAttribute().getInputUnit().getLabel());
                            }
                            formatAllRows();

                        } catch (Exception ex) {
                            logger.catching(ex);
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.setTitle(I18n.getInstance().getString("csv.target.error.title"));
                            alert.setHeaderText(I18n.getInstance().getString("csv.target.error.message"));
                            alert.setContentText(ex.getMessage());

                            alert.showAndWait();
                        }
                    }
                }
            }
        });

        return button;
    }

    public Node getGraphic() {
        root.setStyle("-fx-font-size: 12px;-fx-font-weight: normal;");
        return root;
    }

    public void showHelp() {
        WebView helpView = new WebView();
        helpView.getEngine().load(getClass().getResource("/html/help_dateformate.html").toExternalForm());

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(I18n.getInstance().getString("csv.help.format.title"));
        alert.setHeaderText(I18n.getInstance().getString("csv.help.format.header"));
        alert.setContentText("");
        alert.getDialogPane().setContent(helpView);

        alert.showAndWait();

//        final Stage stage = new Stage();
//
//        stage.setTitle("Help: Formate");
//        stage.initModality(Modality.NONE);
//        stage.initOwner(JEConfig.getStage());
//
////        BorderPane root = new BorderPane();
//        VBox root = new VBox();
//
//        Scene scene = new Scene(root);
//        stage.setScene(scene);
//        stage.setWidth(750);
//        stage.setHeight(620);
//        stage.initStyle(StageStyle.UTILITY);
//
//        BorderPane header = new BorderPane();
//        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
//        header.setPadding(new Insets(10, 10, 10, 10));
//
//        Label topTitle = new Label("Help: Formate");
//        topTitle.setTextFill(Color.web("#0076a3"));
//        topTitle.setFont(Font.font("Cambria", 25));
//
//        ImageView imageView = ResourceLoader.getImage("1404161580_help_blue.png", 65, 65);
//
//        stage.getIcons().add(imageView.getImage());
//
//        VBox vboxLeft = new VBox();
//        VBox vboxRight = new VBox();
//        vboxLeft.getChildren().add(topTitle);
//        vboxLeft.setAlignment(Pos.CENTER_LEFT);
//        vboxRight.setAlignment(Pos.CENTER_LEFT);
//        vboxRight.getChildren().add(imageView);
//
//        header.setLeft(vboxLeft);
//
//        header.setRight(vboxRight);
//
//        HBox webBox = new HBox();
//        webBox.setPadding(new Insets(10));
//        WebView helpView = new WebView();
////        helpView.getEngine().loadContent(getFormateHelpText());
////        URL urlHello = getClass().getResource("/html/help_dateformate.html");
//        helpView.getEngine().load(getClass().getResource("/html/help_dateformate.html").toExternalForm());
//
//        webBox.getChildren().setAll(helpView);
//
////        TextArea helpText = new TextArea();
////        helpText.setText(ICON_QUESTION);
//        HBox buttonbox = new HBox();
//        buttonbox.setAlignment(Pos.BOTTOM_RIGHT);
//
//        Button close = new Button("Close");
//        close.setDefaultButton(true);
//        close.setCancelButton(true);
//        close.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent t) {
//                stage.hide();
//            }
//        });
//        buttonbox.getChildren().setAll(close);
//        buttonbox.setPadding(new Insets(10));
//
//        root.getChildren().setAll(header, webBox, buttonbox);
//
//        stage.show();
    }

    public enum Meaning {

        Ignore, Date, DateTime, Time, Value, Text, Index
    }

    public enum DateTimeMode {

        Date, DateTime, Time
    }

}
