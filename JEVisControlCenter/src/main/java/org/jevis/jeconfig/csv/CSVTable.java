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
import javafx.collections.FXCollections;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVTable extends TableView<CSVLine> {

    private static Logger logger = LogManager.getLogger(CSVTable.class);
    private CSVParser parser;
    private JEVisDataSource ds;
    private List<CSVColumnHeader> header = new ArrayList<>();
    private String customNote = "";

    public CSVTable(JEVisDataSource ds, CSVParser parser) {
        super();
        this.parser = parser;
        this.ds = ds;
        setItems(FXCollections.observableArrayList(parser.getRows()));
        setMaxHeight(1024);
        build();

    }

    private void build() {
        //Simple column guessing base in the secound last line
        header = new ArrayList<>();

        for (int i = 0; i < parser.getColumnCount(); i++) {
            String columnName = "Column " + i;

            columnName = "";

            TableColumn<CSVLine, String> column = new TableColumn(columnName);
            final CSVColumnHeader header = new CSVColumnHeader(this, i);
            this.header.add(header);
            column.setSortable(false);//layout problem
            column.setPrefWidth(310);

//            column.prefWidthProperty().bind(widthProperty().divide(parser.getColumnCount()));
//            column.setPrefWidth(widthProperty().doubleValue() / parser.getColumnCount());
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
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle(I18n.getInstance().getString("csv.import.dialog.success.header"));
                            alert.setHeaderText(null);
                            alert.setContentText(I18n.getInstance().getString("csv.import.dialog.success.message"));
                            alert.showAndWait();
                        });
                    }

                    @Override
                    protected void failed() {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle(I18n.getInstance().getString("csv.import.dialog.failed.title"));
                            alert.setHeaderText(null);
                            alert.setContentText(I18n.getInstance().getString("csv.import.dialog.failed.message"));
                            alert.showAndWait();
                        });
                    }

                    @Override
                    protected Void call() {
                        updateMessage(I18n.getInstance().getString("csv.progress.message"));
                        CSVColumnHeader tsColumn = null;
                        CSVColumnHeader dateColumn = null;
                        CSVColumnHeader timeColumn = null;
                        List<DateTime> combinedList = null;
                        for (CSVColumnHeader header : header) {
                            if (header.getMeaning() == CSVColumnHeader.Meaning.DateTime) {
                                tsColumn = header;
                                break;
                            } else if (header.getMeaning() == CSVColumnHeader.Meaning.Date) {
                                dateColumn = header;
                                break;
                            } else if (header.getMeaning() == CSVColumnHeader.Meaning.Time) {
                                timeColumn = header;
                                break;
                            }
                        }

                        if (dateColumn != null || timeColumn != null) {
                            List<DateTime> listDate = new ArrayList<>();
                            List<DateTime> listTime = new ArrayList<>();
                            for (CSVColumnHeader header : header) {
                                if (header.getMeaning() == CSVColumnHeader.Meaning.Date) {
                                    for (CSVLine line : parser.getRows()) {
                                        try {
                                            listDate.add(header.getValueAsDate(line.getColumn(header.getColumn())));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else if (header.getMeaning() == CSVColumnHeader.Meaning.Time) {
                                    for (CSVLine line : parser.getRows()) {
                                        try {
                                            listTime.add(header.getValueAsDate(line.getColumn(header.getColumn())));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            if (listDate.size() == listTime.size()) {
                                combinedList = new ArrayList<>();
                                for (int i = 0; i < listDate.size(); i++) {
                                    DateTime dt = listDate.get(i);
                                    DateTime tt = listTime.get(i);
                                    combinedList.add(new DateTime(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(),
                                            tt.getHourOfDay(), tt.getMinuteOfHour(), tt.getSecondOfMinute()).withZoneRetainFields(dt.getZone()));
                                }
                            }
                        }

                        if (tsColumn == null) {
                            //TODO check for an Date and an Time Column and combine to DateTime
                        }

                        //find values and import them
                        for (CSVColumnHeader header : header) {
                            if (header.getMeaning() == CSVColumnHeader.Meaning.Value || header.getMeaning() == CSVColumnHeader.Meaning.Text) {
                                List<JEVisSample> _newSamples = new ArrayList<>();

                                for (CSVLine line : parser.getRows()) {
                                    try {
                                        DateTime ts = null;
                                        int rowNumber = line.getRowNumber();
                                        if (tsColumn != null) {
                                            ts = tsColumn.getValueAsDate(line.getColumn(tsColumn.getColumn()));
                                        } else if (combinedList != null) {
                                            ts = combinedList.get(rowNumber);
                                        } else {
                                            throw new JEVisException("Found no timestamp", 34253325);
                                        }
                                        if (header.getMeaning() == CSVColumnHeader.Meaning.Value) {
                                            Double value = header.getValueAsDouble(line.getColumn(header.getColumn()));
                                            JEVisAttribute targetAtt = header.getTarget();
                                            String note = "CSV Import by " + ds.getCurrentUser().getAccountName();
                                            if (!customNote.equals("")) {
                                                note += "; " + customNote;
                                            }
                                            JEVisSample newSample = targetAtt.buildSample(ts, value, note);
                                            _newSamples.add(newSample);
                                        }

                                    } catch (Exception pe) {
                                        logger.error("error while building sample");
                                        //pe.printStackTrace();
                                    }
                                }
                                try {
                                    logger.debug("Import " + _newSamples.size() + " sample(s) into " + header.getTarget().getObject().getID() + "." + header.getTarget().getName());
                                    header.getTarget().addSamples(_newSamples);
                                } catch (JEVisException ex) {
                                    logger.error("Error while importing sample(s) into " + header.getTarget().getObject().getID() + "." + header.getTarget().getName(), ex);
                                    failed();
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

        service.start();
        return true;
    }

    public JEVisDataSource getDataSource() {
        return ds;
    }

    public void setScrollTop() {
        final int size = getItems().size();
        if (size > 0) {
            scrollTo(1);
        }
    }


    public void refreshTable() {
        setItems(FXCollections.observableArrayList(parser.parse()));
    }

    public CSVParser getParser() {
        return parser;
    }

    public void setCustomNote(String customNote) {
        this.customNote = customNote;
    }
}
