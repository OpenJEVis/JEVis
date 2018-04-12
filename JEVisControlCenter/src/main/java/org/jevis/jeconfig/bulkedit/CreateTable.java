package org.jevis.jeconfig.bulkedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Pair;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.GridChange;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetColumn;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisUnit;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.ImageConverter;

/**
 *
 * @author Zeyd Bilal Calis
 */
// CreateTable wurde um eine neue Tabelle zu erzeugen implementiert.
// CreateTable hat zwei untere Klassen die erste ist CreateNewTable und die zweite ist CreateNewDataTable.
// CreateNewDataTable wurde nur fuer das "Data" Objekt implementiert. Wenn man in JEConfig ein Data Objekt definiert,wird diese
// Klasse aufgerufen, fuer die alle andere definierte Objekten wird die CreateNewTable Klasse aufgeruden.
public class CreateTable {

    //declarations
    private final ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
    private ObservableList<SpreadsheetCell> cells;
    private SpreadsheetView spv;
    private GridBase grid;
    private Stage stage = new Stage();
    private JEVisClass createClass;
    private int rowCount;
    private int columnCount;
    private ObservableList<String> columnHeaderNames = FXCollections.observableArrayList();
    private ObservableList<String> columnHeaderNamesDataTable = FXCollections.observableArrayList();
    //Pair list ist fuer die Objektnamen und Attribute
    private ObservableList<Pair<String, ArrayList<String>>> pairList = FXCollections.observableArrayList();
    //Die Units kommen aus JEVisUnit.Prefix.values
    private ObservableList<String> listUnits = FXCollections.observableArrayList();
    //Die Unitsymbols kommen aus der addSymbols()
    private ObservableList<String> listUnitSymbols = FXCollections.observableArrayList();

    public static enum Type {

        NEW, RENAME, EDIT
    };

    public static enum Response {

        NO, YES, CANCEL
    };

    private Response response = Response.CANCEL;

    public CreateTable() {
    }

    public Response show(Stage owner, final JEVisClass jclass, final JEVisObject parent, boolean fixClass, Type type, String objName) {
        ObservableList<JEVisClass> options = FXCollections.observableArrayList();
        try {
            if (type == Type.NEW) {
                options = FXCollections.observableArrayList(parent.getAllowedChildrenClasses());
            }
        } catch (JEVisException ex) {
            Logger.getLogger(CreateTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        //cellFactory for the ComboBox 
        Callback<ListView<JEVisClass>, ListCell<JEVisClass>> cellFactory = new Callback<ListView<JEVisClass>, ListCell<JEVisClass>>() {
            @Override
            public ListCell<JEVisClass> call(ListView<JEVisClass> param) {
                final ListCell<JEVisClass> cell = new ListCell<JEVisClass>() {
                    {
                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(JEVisClass item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            HBox box = new HBox(5);
                            box.setAlignment(Pos.CENTER_LEFT);
                            try {
                                ImageView icon = ImageConverter.convertToImageView(item.getIcon(), 15, 15);
                                Label cName = new Label(item.getName());
                                cName.setTextFill(Color.BLACK);
                                box.getChildren().setAll(icon, cName);

                            } catch (JEVisException ex) {
                                Logger.getLogger(CreateTable.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            setGraphic(box);

                        }
                    }
                };
                return cell;
            }
        };

        //ComboBox ist fuer die Kinder vom ausgewaehlten Parent zu zeigen.
        ComboBox<JEVisClass> classComboBox = new ComboBox<JEVisClass>(options);
        classComboBox.setCellFactory(cellFactory);
        classComboBox.setButtonCell(cellFactory.call(null));
        classComboBox.setMinWidth(250);
        classComboBox.getSelectionModel().selectFirst();
        //Wähle das erste Item aus und initialisiere createClass.
        createClass = classComboBox.getSelectionModel().getSelectedItem();

        Button createBtn = new Button("Create Structure");
        Button cancelBtn = new Button("Cancel");

        //Wenn createclass ein JEconfig "Data" object ist,wird CreateNewDataTable aufgerufen.
        //Wenn nicht wird CreateNewTable aufgerufen.
        try {
            if (createClass.getName().equals("Data")) {
                new CreateNewDataTable(createBtn);
            } else {
                new CreateNewTable();
            }
        } catch (JEVisException ex) {
            Logger.getLogger(CreateTable.class.getName()).log(Level.SEVERE, null, ex);
        }

        BorderPane root = new BorderPane();
        //root.setPadding(new Insets(3));

        HBox hBoxTop = new HBox();
        hBoxTop.setSpacing(10);
        //hBoxTop.setPadding(new Insets(3, 3, 3, 3));
        Label lClass = new Label("Class:");
        //Help Button ist fuer den WebBrowser in den WebBrowser wird die batch_mode_help.html Datei aufgerufen.
        Button help = new Button("Help", JEConfig.getImage("quick_help_icon.png", 22, 22));
        Separator sep1 = new Separator();
        hBoxTop.getChildren().addAll(lClass, classComboBox, sep1, help);

        root.setTop(hBoxTop);

        HBox hBoxBottom = new HBox();
        hBoxBottom.setSpacing(10);
        //hBoxBottom.setPadding(new Insets(0, 3, 3, 3));
        hBoxBottom.getChildren().addAll(createBtn, cancelBtn);
        hBoxBottom.setAlignment(Pos.BASELINE_RIGHT);
        root.setBottom(hBoxBottom);

        root.setCenter(spv);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("styles/Table.css");
        //Die inputs werden schrit zu schrit so abgespeichert.
        createBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
                for (int i = 0; i < grid.getRowCount(); i++) {
                    //Schritt1 : In der ersten Spalte steht der Objektname. 
                    String spcObjectName = rows.get(i).get(0).getText();
                    //Schritt2 : Wenn der Objektname nicht leer ist werden die Attribute gelesen.
                    if (!spcObjectName.equals("")) {
                        //Schritt3 : Ab zweiten Spalten fangen wir die Attribute abzulesen.
                        //Die Attribute werden in eine Liste abgespeichert.
                        ArrayList<String> attributes = new ArrayList<>();
                        for (int j = 1; j < grid.getColumnCount(); j++) {
                            SpreadsheetCell spcAttribut = rows.get(i).get(j);
                            attributes.add(spcAttribut.getText());
                        }
                        //Schritt4 : Objektname und die Attribute werden in die pairList abgepeichert.
                        //Diese Liste wird in der fireEventCreateTable() Methode von der Klasse ObjectTree.java aufgerufen.                     
                        pairList.add(new Pair(spcObjectName, attributes));
                    }
                }
                response = Response.YES;
            }
        });

        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
                response = Response.CANCEL;

            }
        });
        //Wenn man vom ComboBox ein neues Objekt auswählt,wird die Tabelle neue Strukturiert.
        classComboBox.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    rows.clear();
                    columnHeaderNames.clear();
                    columnHeaderNamesDataTable.clear();
                    pairList.clear();
                    createClass = classComboBox.getSelectionModel().getSelectedItem();

                    if (createClass.getName().equals("Data")) {
                        new CreateNewDataTable(createBtn);
                        root.setCenter(spv);
                    } else {
                        new CreateNewTable();
                        root.setCenter(spv);
                    }
                } catch (JEVisException ex) {
                    Logger.getLogger(CreateTable.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        //Help Button für die help Datei.
        help.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WebBrowser webBrowser = new WebBrowser();
            }
        });

        stage.setTitle("Bulk Create");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setScene(scene);
        stage.setWidth(1250);
        stage.setHeight(800);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.showAndWait();

        return response;
    }

    public ObservableList<Pair<String, ArrayList<String>>> getPairList() {
        return pairList;
    }

    public JEVisClass getCreateClass() {
        return createClass;
    }

    public ObservableList<String> getColumnHeaderNames() {
        return columnHeaderNames;
    }

    // Erstelle eine neue Tabelle
    class CreateNewTable {

        public CreateNewTable() {
            try {
                //Zeilen anzahl ist 1000
                //Spalten-Anzahl ist gleich Typen-Anzahl von der ausgewählten JEVisClass.
                //Spalten-Anzahl : Klassen Attribute(Types) und +1 ist fuer die Objectname.
                rowCount = 1000;
                columnCount = createClass.getTypes().size() + 1;
            } catch (JEVisException ex) {
                Logger.getLogger(CreateTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Ab hier wird die Tabelle und ihre Eigenschaften erzeugt.
            grid = new GridBase(rowCount, columnCount);

            for (int row = 0; row < grid.getRowCount(); ++row) {
                cells = FXCollections.observableArrayList();
                for (int column = 0; column < grid.getColumnCount(); ++column) {
                    cells.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1, ""));
                }

                rows.add(cells);
            }
            grid.setRows(rows);
            grid.setRowHeightCallback(new GridBase.MapBasedRowHeightFactory(generateRowHeight()));
            spv = new SpreadsheetViewTable(rows, grid);

            spv.setGrid(grid);

            ObservableList<SpreadsheetColumn> colList = spv.getColumns();

            for (SpreadsheetColumn colListElement : colList) {
                colListElement.setPrefWidth(150);
            }

            spv.setEditable(true);
            spv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            // Spaltennamen werden in die columnHeaderNames speichert(Object Name und Typnames).
            columnHeaderNames.add("Object Name");
            try {

                //Get and set the typenames from a class.
                //Typenames werden von der Ausgewählte Klasse aufgerufen.Diese Namen werden in der columnHeaderNames Liste verwendet.
                for (int i = 0; i < createClass.getTypes().size(); i++) {
                    columnHeaderNames.add(createClass.getTypes().get(i).getName());
                }

            } catch (JEVisException ex) {
                Logger.getLogger(CreateTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            spv.getGrid().getColumnHeaders().addAll(columnHeaderNames);
        }
    }

    // Erstelle eine neue Data-Tabelle
    // Diese Klasse spezial nur fuer das Data-Object implementiert.
    class CreateNewDataTable {

        public CreateNewDataTable(Button createBtn) {
            //Erzeuge eine fixe Tabelle mit dieser Spaltennamen.
            String[] colNames = {"Object Name", "Display Prefix", "Display Symbol", "Display Sample Rate", "Input Prefix", "Input Symbol", "Input Sample Rate"};
            rowCount = 1000;

            try {
                int typeSize = createClass.getTypes().size() - 1;
                columnCount = colNames.length + typeSize;
            } catch (JEVisException ex) {
                Logger.getLogger(CreateTable.class.getName()).log(Level.SEVERE, null, ex);
            }

            grid = new GridBase(rowCount, columnCount);

            for (int row = 0; row < grid.getRowCount(); ++row) {
                cells = FXCollections.observableArrayList();
                for (int column = 0; column < grid.getColumnCount(); ++column) {
                    cells.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1, ""));
                }
                rows.add(cells);
            }
            grid.setRows(rows);
            grid.setRowHeightCallback(new GridBase.MapBasedRowHeightFactory(generateRowHeight()));
            spv = new SpreadsheetViewTable(rows, grid);

            spv.setGrid(grid);

            ObservableList<SpreadsheetColumn> colList = spv.getColumns();
            //Die Breite von der Spalte wird als 150 eingesetzt.
            for (SpreadsheetColumn colListElement : colList) {
                colListElement.setPrefWidth(150);
            }

            spv.setEditable(true);
            spv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            try {
                //Get and set Typenames außer Value attribute
                for (int i = 0; i < createClass.getTypes().size(); i++) {
                    if (!createClass.getTypes().get(i).getName().equals("Value")) {
                        columnHeaderNames.add(createClass.getTypes().get(i).getName());
                    }
                }
            } catch (JEVisException ex) {
                Logger.getLogger(EditTable.CreateNewEditTable.class.getName()).log(Level.SEVERE, null, ex);
            }

            columnHeaderNamesDataTable.addAll(colNames);
            columnHeaderNamesDataTable.addAll(columnHeaderNames);
            spv.getGrid().getColumnHeaders().addAll(columnHeaderNamesDataTable);

            addUnits();
            addSymbols();

            //GridChangeEvent kontrolliert die Änderungen in der Tabelle und ueberprüft mit hilfe des inputControls ob sie richtrig sind oder nicht.
            spv.getGrid().addEventHandler(GridChange.GRID_CHANGE_EVENT, new EventHandler<GridChange>() {

                @Override
                public void handle(GridChange event) {
                    inputControl(createBtn);
                }
            });
        }
    }

    //Hier wird die Eingaben fuer die Spalten(Prefix,Symbol und Sample Rate ) ueberprueft.
    public void inputControl(Button createBtn) {
        ObservableList<String> listPrefix = FXCollections.observableArrayList();
        ObservableList<String> listSymbols = FXCollections.observableArrayList();
        ObservableList<String> listSampleRateControl = FXCollections.observableArrayList();

        Pattern pattern = Pattern.compile("[P]([0-9]+[M])?([0-9][W])?[T]([0-9]+[H])?([0-9]+[M])?([0-9]+[S])?");

        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcDisplayPrefix = rows.get(i).get(1);
            if (!spcDisplayPrefix.getText().equals("")) {
                listPrefix.add(spcDisplayPrefix.getText());
            }
        }
        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcInputPrefix = rows.get(i).get(4);
            if (!spcInputPrefix.getText().equals("")) {
                listPrefix.add(spcInputPrefix.getText());
            }
        }
        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcDisplaySymbol = rows.get(i).get(2);
            if (!spcDisplaySymbol.getText().equals("")) {
                listSymbols.add(spcDisplaySymbol.getText());
            }
        }
        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcInputSymbol = rows.get(i).get(5);
            if (!spcInputSymbol.getText().equals("")) {
                listSymbols.add(spcInputSymbol.getText());
            }
        }

        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcDisplaySampleRate = rows.get(i).get(3);
            if (!spcDisplaySampleRate.getText().equals("")) {
                Matcher matcher = pattern.matcher(spcDisplaySampleRate.getText());
                if (!matcher.matches()) {
                    listSampleRateControl.add(spcDisplaySampleRate.getText());
                }
            }
        }
        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcInputSampleRate = rows.get(i).get(6);
            if (!spcInputSampleRate.getText().equals("")) {
                Matcher matcher = pattern.matcher(spcInputSampleRate.getText());
                if (!matcher.matches()) {
                    listSampleRateControl.add(spcInputSampleRate.getText());
                }
            }
        }

        if (listUnits.containsAll(listPrefix) && listUnitSymbols.containsAll(listSymbols) && listSampleRateControl.isEmpty()) {
            createBtn.setDisable(false);
        } else {
            createBtn.setDisable(true);
        }

        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcDisplayPrefix = rows.get(i).get(1);
            if (!spcDisplayPrefix.getText().equals("")) {
                if (!listUnits.contains(spcDisplayPrefix.getText())) {
                    spcDisplayPrefix.getStyleClass().add("spreadsheet-cell-error");
                } else {
                    spcDisplayPrefix.getStyleClass().remove("spreadsheet-cell-error");
                }
            } else {
                spcDisplayPrefix.getStyleClass().remove("spreadsheet-cell-error");
            }
        }

        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcInputPrefix = rows.get(i).get(4);
            if (!spcInputPrefix.getText().equals("")) {
                if (!listUnits.contains(spcInputPrefix.getText())) {
                    spcInputPrefix.getStyleClass().add("spreadsheet-cell-error");
                } else {
                    spcInputPrefix.getStyleClass().remove("spreadsheet-cell-error");
                }
            } else {
                spcInputPrefix.getStyleClass().remove("spreadsheet-cell-error");
            }
        }
        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcDisplaySymbol = rows.get(i).get(2);
            if (!spcDisplaySymbol.getText().equals("")) {
                if (!listUnitSymbols.contains(spcDisplaySymbol.getText())) {
                    spcDisplaySymbol.getStyleClass().add("spreadsheet-cell-error");
                } else {
                    spcDisplaySymbol.getStyleClass().remove("spreadsheet-cell-error");
                }
            } else {
                spcDisplaySymbol.getStyleClass().remove("spreadsheet-cell-error");
            }
        }

        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcInputSymbol = rows.get(i).get(5);
            if (!spcInputSymbol.getText().equals("")) {
                if (!listUnitSymbols.contains(spcInputSymbol.getText())) {
                    spcInputSymbol.getStyleClass().add("spreadsheet-cell-error");
                } else {
                    spcInputSymbol.getStyleClass().remove("spreadsheet-cell-error");
                }
            } else {
                spcInputSymbol.getStyleClass().remove("spreadsheet-cell-error");
            }
        }

        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcDisplaySampleRate = rows.get(i).get(3);
            Matcher matcher = pattern.matcher(spcDisplaySampleRate.getText());
            if (!spcDisplaySampleRate.getText().equals("")) {
                if (!matcher.matches()) {
                    spcDisplaySampleRate.getStyleClass().add("spreadsheet-cell-error");
                } else {
                    spcDisplaySampleRate.getStyleClass().remove("spreadsheet-cell-error");
                }
            } else {
                spcDisplaySampleRate.getStyleClass().remove("spreadsheet-cell-error");
            }
        }

        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcInputSampleRate = rows.get(i).get(6);
            Matcher matcher = pattern.matcher(spcInputSampleRate.getText());
            if (!spcInputSampleRate.getText().equals("")) {
                if (!matcher.matches()) {
                    spcInputSampleRate.getStyleClass().add("spreadsheet-cell-error");
                } else {
                    spcInputSampleRate.getStyleClass().remove("spreadsheet-cell-error");
                }
            } else {
                spcInputSampleRate.getStyleClass().remove("spreadsheet-cell-error");
            }
        }

        listSymbols.clear();
        listPrefix.clear();
        listSampleRateControl.clear();
    }

    // Hier wird die Zellenhöhe anpasst.
    private Map<Integer, Double> generateRowHeight() {
        Map<Integer, Double> rowHeight = new HashMap<>();
        for (int i = 0; i < grid.getRowCount(); i++) {
            rowHeight.put(i, 30.0);
        }
        return rowHeight;
    }

    //Get the Prefix values from JEVisUnit.Prefix.
    private void addUnits() {
        JEVisUnit.Prefix[] prefixes = JEVisUnit.Prefix.values();

        for (int i = 0; i < prefixes.length; i++) {
            String strPrefix = prefixes[i].toString();
            listUnits.add(strPrefix);
        }
    }

    // JEVIS Unit symbols
    private void addSymbols() {
        listUnitSymbols.addAll("m/s\u00B2",
                "g", "mol", "atom", "rad", "bit", "\u0025", "centiradian", "dB", "\u00b0", "\u0027", "byte", "rev", "\u00A8", "sphere", "sr", "rad/s\u00B2", "rad/s", "Bq", "Ci", "Hz",
                "m\u00B2", "a", "ha", "cm\u00B2", "km\u00B2", "kat", "\u20AC", "\u20A6", "\u20B9", "\u0024", "*\u003F*", "\u00A5", "Hits/cm\u00B2", "Hits/m\u00B2", "\u03A9/cm\u00B2", "bit/s", "\u002D", "s", "m", "h", "day", "day_sidereal",
                "week", "month", "year", "year_calendar", "year_sidereal", "g/\u0028cms\u0029", "F", "C", "e", "Fd", "Fr", "S", "A", "Gi", "H", "V", "\u03A9", "J",
                "eV", "erg", "N", "dyn", "kgf", "lbf", "lx", "La", "W/m\u00B2", "m\u00B2/s", "cm\u00B2/s", "\u00C5", "ua", "cm", "foot_survey_us", "ft", "in", "km", "ly",
                "mi", "mm", "nmi", "pc", "pixel", "pt", "yd", "W", "Wb", "Mx", "T", "G", "kg", "u", "me", "t", "oz", "lb", "ton_uk", "ton_us", "kg/s",
                "cd", "hp", "lm", "var", "Pa", "atm", "bar", "in Hg", "mmHg", "Gy", "rem", "Sv", "rd", "Rd", "rev/s", "grade", "K", "\u00b0C", "\u00b0F", "\u00b0R",
                "Nm", "Wh", "Ws", "m/s", "c", "km/h", "kn", "Mach", "mph", "m\u00B3", "in\u00B3", "gallon_dry_us", "gal", "gallon_uk", "l", "oz_uk", "kg/m\u00B3", "m\u00B3/s");
    }
}
