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

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVTable extends TableView<CSVLine> {

    private static Logger logger = LogManager.getLogger(CSVTable.class);
    private CSVParser _parser;
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

            columnName = "";

            TableColumn<CSVLine, String> column = new TableColumn(columnName);
            final CSVColumnHeader header = new CSVColumnHeader(this, i);
            _header.add(header);
            column.setSortable(false);//layout problem
            column.setPrefWidth(310);

//            column.prefWidthProperty().bind(widthProperty().divide(_parser.getColumnCount()));
//            column.setPrefWidth(widthProperty().doubleValue() / _parser.getColumnCount());
            final int rowID = i;

            column.setCellValueFactory(p -> {
                if (p != null) {
                    try {
                        return header.getValueProperty(p.getValue());
                    } catch (NullPointerException ex) {

                    }
                }

                return new SimpleObjectProperty<>("");
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
                    protected void succeeded() {
                        super.succeeded();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle(I18n.getInstance().getString("csv.import.dialog.success.header"));
                        alert.setHeaderText(null);
                        alert.setContentText(I18n.getInstance().getString("csv.import.dialog.success.message"));
                        alert.showAndWait();
                    }

                    @Override
                    protected void failed() {
                        super.failed();

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(I18n.getInstance().getString("csv.import.dialog.failed.title"));
                        alert.setHeaderText(null);
                        alert.setContentText(I18n.getInstance().getString("csv.import.dialog.failed.message"));
                        alert.showAndWait();
                    }

                    @Override
                    protected Void call() {
                        updateMessage(I18n.getInstance().getString("csv.progress.message"));
                        CSVColumnHeader tsColumn = null;
                        for (CSVColumnHeader header : _header) {
                            if (header.getMeaning() == CSVColumnHeader.Meaning.DateTime) {
                                tsColumn = header;
                                break;
                            } else if (header.getMeaning() == CSVColumnHeader.Meaning.Date) {
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
                                        DateTime ts = tsColumn.getValueAsDate(line.getColumn(tsColumn.getColumn()));
                                        if (header.getMeaning() == CSVColumnHeader.Meaning.Value) {
                                            Double value = header.getValueAsDouble(line.getColumn(header.getColumn()));
                                            JEVisAttribute targetAtt = header.getTarget();
                                            String note = "CSV Import by " + _ds.getCurrentUser().getAccountName();
                                            JEVisSample newSample = targetAtt.buildSample(ts, value, note);
                                            _newSamples.add(newSample);
                                        }

                                    } catch (Exception pe) {
                                        logger.error("error while building sample");
                                        //pe.printStackTrace();
                                    }
                                }
                                try {
                                    logger.debug("Import " + _newSamples.size() + " sample into " + header.getTarget().getObject().getID() + "." + header.getTarget().getName());
                                    header.getTarget().addSamples(_newSamples);

                                } catch (JEVisException ex) {
                                    logger.error("error while import sample" + ex);
                                }

                            }
                        }

                        succeeded();
                        return null;
                    }
                };
            }
        };
        ProgressDialog pd = new ProgressDialog(service);
        pd.setHeaderText(I18n.getInstance().getString("csv.progress.header"));
        pd.setTitle(I18n.getInstance().getString("csv.progress.title"));

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
        }
    }


    public void refreshTable() {
        ObservableList<CSVLine> tmpItem = FXCollections.observableArrayList();
        FXCollections.copy(tmpItem, getItems());
        setItems(tmpItem);

    }

}
