/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.jevistree.plugin;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;
import org.jevis.application.jevistree.TreePlugin;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class MapPlugin implements TreePlugin {
    private static final Logger logger = LogManager.getLogger(MapPlugin.class);

    private JEVisTree _tree;
    private String _title = "GIS #1";
    private Map<Long, List<JEVisSample>> _samples = new HashMap<>();
    private Map<String, DataModel> _data = new HashMap<>();

    private enum DATE_TYPE {

        START, END
    }

    @Override
    public void setTree(JEVisTree tree) {
        _tree = tree;
    }

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> column = new TreeTableColumn(_title);

        TreeTableColumn<JEVisTreeRow, Color> colorColumn = buildColorColumn(_tree, "Color");
        TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, "Load");
        TreeTableColumn<JEVisTreeRow, DateTime> startDateColumn = buildDateColumn(_tree, "Start Date", DATE_TYPE.START);
        TreeTableColumn<JEVisTreeRow, DateTime> endDateColumn = buildDateColumn(_tree, "End Date", DATE_TYPE.END);

        column.getColumns().addAll(colorColumn, selectColumn, startDateColumn, endDateColumn);

        list.add(column);

        return list;
    }

    @Override
    public void selectionFinished() {
        //Will happen if the user peress some kinde of OK button
        logger.info("selectionFinished()");
        for (Map.Entry<String, DataModel> entrySet : _data.entrySet()) {
            String key = entrySet.getKey();
            DataModel value = entrySet.getValue();
            if (value.getSelected()) {
                logger.info("key: " + key);
            }

        }

    }

    public void setTitle(String title) {
        _title = title;
    }

    @Override
    public String getTitle() {
        return _title;
    }

    private DataModel getData(JEVisTreeRow row) {
        String id = row.getID();
        if (_data.containsKey(id)) {
            return _data.get(id);
        } else {
//            logger.info("add" + row.getJEVisObject());
            DataModel newData = new DataModel();
            newData.setObject(row.getJEVisObject());
            newData.setLongitude(row.getJEVisObject());
            newData.setLatitude(row.getJEVisObject());

            _data.put(id, newData);
            return newData;
        }
    }

    private TreeTableColumn<JEVisTreeRow, Color> buildColorColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, Color> column = new TreeTableColumn(columnName);
        column.setPrefWidth(130);
        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, Color>, ObservableValue<Color>>() {

            @Override
            public ObservableValue<Color> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, Color> param) {
                MapPlugin.DataModel data = getData(param.getValue().getValue());
                return new ReadOnlyObjectWrapper<>(data.getColor());
//                return param.getValue().getValue().getColorProperty();
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Color>, TreeTableCell<JEVisTreeRow, Color>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Color> call(TreeTableColumn<JEVisTreeRow, Color> param) {

                TreeTableCell<JEVisTreeRow, Color> cell = new TreeTableCell<JEVisTreeRow, Color>() {

                    @Override
                    public void commitEdit(Color newValue) {
                        super.commitEdit(newValue);
                        MapPlugin.DataModel data = getData(getTreeTableRow().getItem());
                        data.setColor(newValue);
//                        getTreeTableRow().getItem().getColorProperty().setValue(newValue);
                    }

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                MapPlugin.DataModel data = getData(getTreeTableRow().getItem());
                                ColorPicker colorPicker = new ColorPicker();
                                hbox.getChildren().setAll(colorPicker);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                colorPicker.setValue(item);

                                colorPicker.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        commitEdit(colorPicker.getValue());
                                    }
                                });

                                colorPicker.setDisable(!data.isSelectable());
                            }

                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    private DatePicker buildDatePicker(DataModel data, DATE_TYPE type) {

        LocalDate ld = null;

        if (data.getSelectedStart() != null) {
            if (type == DATE_TYPE.START) {
                ld = LocalDate.of(
                        data.getSelectedStart().getYear(),
                        data.getSelectedStart().getMonthOfYear(),
                        data.getSelectedStart().getDayOfMonth()
                );
            } else {
                ld = LocalDate.of(
                        data.getSelectedEnd().getYear(),
                        data.getSelectedEnd().getMonthOfYear(),
                        data.getSelectedEnd().getDayOfMonth()
                );
            }
        }

        DatePicker dp = new DatePicker(ld);

        final Callback<DatePicker, DateCell> dayCellFactory
                = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        LocalDate ldBeginn = LocalDate.of(
                                data.getTimestampFromFirstSample().getYear(),
                                data.getTimestampFromFirstSample().getMonthOfYear(),
                                data.getTimestampFromFirstSample().getDayOfMonth());
                        LocalDate ldEnd = LocalDate.of(
                                data.getTimestampFromLastSample().getYear(),
                                data.getTimestampFromLastSample().getMonthOfYear(),
                                data.getTimestampFromLastSample().getDayOfMonth());

                        if (data.getTimestampFromFirstSample() != null && item.isBefore(ldBeginn)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }

                        if (data.getTimestampFromFirstSample() != null && item.isAfter(ldEnd)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }

                    }
                };
            }
        };
        dp.setDayCellFactory(dayCellFactory);

        return dp;
    }

    private TreeTableColumn<JEVisTreeRow, DateTime> buildDateColumn(JEVisTree tree, String columnName, DATE_TYPE type) {
        TreeTableColumn<JEVisTreeRow, DateTime> column = new TreeTableColumn(columnName);
        column.setPrefWidth(130);
        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, DateTime>, ObservableValue<DateTime>>() {

            @Override
            public ObservableValue<DateTime> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, DateTime> param) {
                try {
                    DataModel data = getData(param.getValue().getValue());
                    DateTime date;
                    if (type == DATE_TYPE.START) {
                        date = data.getSelectedStart();
                    } else {
                        date = data.getSelectedEnd();
                    }

                    return new ReadOnlyObjectWrapper<>(date);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return new ReadOnlyObjectWrapper<>(null);
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, DateTime>, TreeTableCell<JEVisTreeRow, DateTime>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, DateTime> call(TreeTableColumn<JEVisTreeRow, DateTime> param) {

                TreeTableCell<JEVisTreeRow, DateTime> cell = new TreeTableCell<JEVisTreeRow, DateTime>() {

                    @Override
                    public void commitEdit(DateTime newValue) {
                        super.commitEdit(newValue);
                        DataModel data = getData(getTreeTableRow().getItem());

                        if (type == DATE_TYPE.START) {
                            data.setSelectedStart(newValue);
                        } else {
                            data.setSelectedEnd(newValue);
                        }

//                        getTreeTableRow().getItem().getColorProperty().setValue(newValue);
                    }

                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                DataModel data = getData(getTreeTableRow().getItem());
                                DatePicker dp = buildDatePicker(data, type);

                                hbox.getChildren().setAll(dp);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);

                                dp.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        LocalDate ld = dp.getValue();
                                        DateTime jodaTime = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 0, 0);
                                        commitEdit(jodaTime);
                                    }
                                });
                            }

                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    private TreeTableColumn<JEVisTreeRow, Boolean> buildSelectionColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn(columnName);
        column.setPrefWidth(60);
        column.setEditable(true);

        //replace to use the datamodel
//        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<SelectionTreeRow, Boolean> param) -> param.getValue().getValue().getObjectSelectedProperty());
        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, Boolean>, ObservableValue<Boolean>>() {

            @Override
            public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, Boolean> param) {
                DataModel data = getData(param.getValue().getValue());
                return new ReadOnlyObjectWrapper<>(data.getSelected());
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {

                TreeTableCell<JEVisTreeRow, Boolean> cell = new TreeTableCell<JEVisTreeRow, Boolean>() {

                    @Override
                    public void commitEdit(Boolean newValue) {
                        super.commitEdit(newValue);
                        getTreeTableRow().getItem().getObjectSelectedProperty().setValue(newValue);
                        DataModel data = getData(getTreeTableRow().getItem());
                        data.setSelected(newValue);
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            CheckBox cbox = new CheckBox();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                DataModel data = getData(getTreeTableRow().getItem());
                                hbox.getChildren().setAll(cbox);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                cbox.setSelected(item);

                                cbox.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        commitEdit(cbox.isSelected());
                                    }
                                });

                                if (data.hasSample()) {
                                    cbox.setDisable(false);
                                } else {
                                    cbox.setDisable(true);
                                }

                            }

                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    public Map<String, MapPlugin.DataModel> getSelectedData() {
        return _data;
    }

    public class DataModel {

        private DateTime _selectedStart;
        private DateTime _selectedEnd;
        private JEVisObject _object;
        private boolean _selected = false;
        private Color _color = Color.LIGHTBLUE;
        private JEVisAttribute _longitude;
        private JEVisAttribute _latitude;

        public DataModel() {
        }

        public boolean getSelected() {

            return _selected;
        }

        public void setSelected(boolean selected) {
            _selected = selected;
            logger.info("is selectec: " + _object.getName());
        }

        public DateTime getSelectedStart() {

            if (_selectedStart != null) {
                return _selectedStart;
            } else {
                _selectedStart = getTimestampFromFirstSample();
            }
            return _selectedStart;

//            if (_selectedStart != null && getAttribute() != null) {
//                System.out.print("-");
////                logger.info("getSelectedStart1 " + getAttribute().getTimestampFromFirstSample());
//                return getAttribute().getTimestampFromFirstSample();
//            }
//            System.out.print(".");
////            logger.info("getSelectedStart2 " + _selectedStart);
//            return _selectedStart;
        }

        public Color getColor() {
            return _color;
        }

        public void setColor(Color _color) {
            this._color = _color;
        }

        public void setSelectedStart(DateTime selectedStart) {
            this._selectedStart = selectedStart;
        }

        public DateTime getSelectedEnd() {
            if (_selectedEnd != null) {
                return _selectedEnd;
            } else {
                _selectedEnd = getTimestampFromLastSample();
            }
            return _selectedEnd;

//            if (_selectedEnd != null && getAttribute() != null) {
//                return getAttribute().getTimestampFromLastSample();
//            }
//
//            return _selectedEnd;
        }

        public void setSelectedEnd(DateTime selectedEnd) {
            this._selectedEnd = selectedEnd;
        }

        public JEVisObject getObject() {
            return _object;
        }

        public void setObject(JEVisObject _object) {
//            logger.info("new DataModel: " + _object);
            this._object = _object;
        }

        public JEVisAttribute getLongitudeAttribute() {
            if (_longitude == null) {
                try {
                    if (getObject().getJEVisClass().getName().equals("GPS Data")) {
                        JEVisAttribute values = getObject().getAttribute("Longitude");
                        _longitude = values;
                    }
//                    return values;
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }

            return _longitude;
        }

        public JEVisAttribute getLatitudeAttribute() {
            if (_latitude == null) {
//                logger.info("att is null");
                try {
                    if (getObject().getJEVisClass().getName().equals("GPS Data")) {
                        JEVisAttribute values = getObject().getAttribute("Latitude");
                        _latitude = values;
                    }
//                    return values;
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }

            return _latitude;
        }

        //        public void setAttribute(JEVisAttribute _attribute) {
//            this._attribute = _attribute;
//        }
//        public Color getColor() {
//            return _color;
//        }
//
//        public void setColor(Color _color) {
//            this._color = _color;
//        }
        public boolean isSelectable() {
//            if (isGPSData() != null && getAttribute().hasSample()) {
            boolean selectable = isGPSData() && hasSample();
            return selectable;
        }

        private boolean isGPSData() {
            try {
                return getObject().getJEVisClass().getName().equals("GPS Data");
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
            return false;
        }

        private boolean hasSample() {
            boolean hasSample = false;
            if (_latitude != null && _longitude != null) {
                if (_latitude.hasSample() && _longitude.hasSample()) {
                    hasSample = true;
                }
            }
            return hasSample;
        }

        private DateTime getTimestampFromLastSample() {
            DateTime until = null;
            if (_latitude != null && _longitude != null) {
                until = _latitude.getTimestampFromLastSample();
                if (_longitude.getTimestampFromLastSample().isBefore(until)) {
                    until = _longitude.getTimestampFromLastSample();
                }
            }
            return until;
        }

        private DateTime getTimestampFromFirstSample() {
            DateTime from = null;
            if (_latitude != null && _longitude != null) {
                from = _latitude.getTimestampFromFirstSample();
                if (_longitude.getTimestampFromFirstSample().isAfter(from)) {
                    from = _longitude.getTimestampFromFirstSample();
                }
            }
            return from;
        }

        private void setLongitude(JEVisObject jeVisObject) {
            try {
                _longitude = jeVisObject.getAttribute("Longitude");
                logger.info("long" + _longitude);
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        }

        private void setLatitude(JEVisObject jeVisObject) {
            try {
                _latitude = jeVisObject.getAttribute("Latitude");
                logger.info("lat" + _latitude);
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        }

    }

}
