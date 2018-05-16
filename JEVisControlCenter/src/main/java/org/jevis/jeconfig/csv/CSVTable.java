/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVTable extends TableView<CSVLine> {

    private CSVParser _parser;
    private double _lastBarPositionV;
    private double _lastBarPositionH;
    private JEVisDataSource _ds;
    private List<CSVColumnHeader> _header = new ArrayList<>();

    public CSVTable(JEVisDataSource ds, CSVParser parser) {
        super();
        _parser = parser;
        _ds = ds;
        setItems(FXCollections.observableArrayList(parser.getRows()));
        setMaxHeight(1024);
        build();

    }

    private void build() {
        //Simple column guessing base in the secound last line
        _header = new ArrayList<>();

        for (int i = 0; i < _parser.getColumnCount(); i++) {
            String columnName = "Column " + i;
//            try {
//                if (_parser.getRows().size() - 1 > 1) {
//                    CSVLine testLIne = _parser.getRows().get(_parser.getRows().size() - 1);
//                    System.out.println("guess for: " + testLIne.getColumn(i));
//                    columnName += "\n" + CSVValueGuesser.guessType(testLIne.getColumn(i));
//
//                }
//            } catch (Exception ex) {
//                System.out.println("error while guessing values");
//            }
            columnName = "";

            TableColumn<CSVLine, String> column = new TableColumn(columnName);
            final CSVColumnHeader header = new CSVColumnHeader(this, i);
            _header.add(header);
            column.setSortable(false);//layout problem
            column.setPrefWidth(310);

//            column.prefWidthProperty().bind(widthProperty().divide(_parser.getColumnCount()));
//            column.setPrefWidth(widthProperty().doubleValue() / _parser.getColumnCount());
            final int rowID = i;

            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CSVLine, String>, ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<CSVLine, String> p) {
                    if (p != null) {
                        try {
                            return header.getValueProperty(p.getValue());
                        } catch (NullPointerException ex) {

                        }
                    }

                    return new SimpleObjectProperty<>("");
                }
            });

            column.setGraphic(header.getGraphic());

            getColumns().add(column);

        }
    }

    public boolean doImport() {

        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        updateMessage(I18n.getInstance().getString("csv.progress.message"));
                        CSVColumnHeader tsColumn = null;
                        for (CSVColumnHeader header : _header) {
                            if (header.getMeaning() == CSVColumnHeader.Meaning.DateTime) {
                                tsColumn = header;
                                break;
                            }else if(header.getMeaning() == CSVColumnHeader.Meaning.Date){
                                tsColumn = header;
                                break;
                            }
                        }

                        if (tsColumn == null) {
                            //TODO check for an Date and an Time Coloum an combine to DateTime
                        }

                        if (tsColumn == null) {
                            failed();
//                    return false;
                        }

                        //find values and import them
                        for (CSVColumnHeader header : _header) {
                            if (header.getMeaning() == CSVColumnHeader.Meaning.Value || header.getMeaning() == CSVColumnHeader.Meaning.Text) {
                                List<JEVisSample> _newSamples = new ArrayList<>();

                                for (CSVLine line : _parser.getRows()) {
                                    try {
                                        DateTime ts= tsColumn.getValueAsDate(line.getColumn(tsColumn.getColumn()));
                                        if (header.getMeaning() == CSVColumnHeader.Meaning.Value) {
                                            Double value = header.getValueAsDouble(line.getColumn(header.getColumn()));
                                            JEVisSample newSample = header.getTarget().buildSample(ts, value, "CSV Import by " + _ds.getCurrentUser().getAccountName());
                                            _newSamples.add(newSample);
                                        }

                                    } catch (Exception pe) {
                                        System.out.println("error while building sample");
                                        pe.printStackTrace();
                                    }
                                }
                                try {
                                    System.out.println("Import " + _newSamples.size() + " sample into " + header.getTarget().getObject().getID() + "." + header.getTarget().getName());
                                    header.getTarget().addSamples(_newSamples);

                                } catch (JEVisException ex) {
                                    System.out.println("error while import sample");
                                    Logger.getLogger(CSVTable.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        }

                        return null;
                    }
                };
            }
        };
        ProgressDialog pd = new ProgressDialog(service);
        pd.setHeaderText(I18n.getInstance().getString("csv.progress.header"));
        pd.setTitle(I18n.getInstance().getString("csv.progress.title"));
//        pd.setContentText("Importing Samples2");
        service.start();
        return true;
    }

    public JEVisDataSource getDataSource() {
        return _ds;
    }

    public void setScrollTop() {
        final int size = getItems().size();
        if (size > 0) {
            scrollTo(1);
        };
    }

    public void setScrollBottom() {
        final int size = getItems().size();
        if (size > 0) {
            scrollTo(size - 1);
        };

//        getItems().addListener(new ListChangeListener<CSVLine>() {
//
//            @Override
//            public void onChanged(ListChangeListener.Change<? extends CSVLine> change) {
//                final int size = getItems().size();
//                if (size > 0) {
//                    scrollTo(size - 1);
//                }
//            }
//        });
//        for (Node n : lookupAll(".scroll-bar")) {
//            if (n instanceof ScrollBar) {
//                ScrollBar bar = (ScrollBar) n;
//                System.out.println(bar.getOrientation() + ": range " + bar.getMin() + " => " + bar.getMax() + ", value " + bar.getValue());
////                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
////                    _lastBarPositionV = bar.getValue();
////                } else {
////                    _lastBarPositionH = bar.getValue();
////                }
//
//                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
//                    bar.setValue(100.0d);
//                    System.out.println(bar.getOrientation() + ": range " + bar.getMin() + " => " + bar.getMax() + ", value " + bar.getValue());
//                }
//
//            }
//        }
    }

    public void setLastScrollPosition() {
//        System.out.println("Set last Pos: " + _lastBarPositionV + " " + _lastBarPositionH);
        for (Node n : lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    bar.setValue(_lastBarPositionV);
                } else {
                    bar.setValue(_lastBarPositionH);
                }
            }
        }
    }

    public void refreshTable() {
        ObservableList<CSVLine> tmpItem = FXCollections.observableArrayList();
        FXCollections.copy(tmpItem, getItems());
        setItems(tmpItem);

//        final List<CSVLine> items = getItems();
//        if (items == null || items.size() == 0) {
//            return;
//        }
//
//        final CSVLine item = getItems().get(0);
//        items.remove(0);
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                items.add(0, item);
//            }
//        });
    }

}
