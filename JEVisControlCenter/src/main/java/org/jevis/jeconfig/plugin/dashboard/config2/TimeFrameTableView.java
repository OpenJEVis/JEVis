package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.table.ShortColumnCell;

public class TimeFrameTableView extends TableView<TimeFrameWidgetObject> {


    private static final Logger logger = LogManager.getLogger(TimeFrameTableView.class);

    //private Property<Widget> selectedWidget = new SimpleObjectProperty<>();

    public TimeFrameTableView(ObservableList<TimeFrameWidgetObject> list) {
        this.setEditable(true);
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<TimeFrameWidgetObject, String> idCol = new TableColumn(I18n.getInstance().getString("plugin.dashboard.timeframe.table.id"));
        idCol.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().config.getUuid())));
        idCol.setCellFactory(new ShortColumnCell<TimeFrameWidgetObject>());
        idCol.setStyle("-fx-alignment: LEFT;");
        // nrCol.setMinWidth(SMALL_WIDTH)

        TableColumn<TimeFrameWidgetObject, String> titleCol = new TableColumn(I18n.getInstance().getString("plugin.dashboard.timeframe.table.title"));
        titleCol.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().config.getTitle())));
        titleCol.setCellFactory(new ShortColumnCell<TimeFrameWidgetObject>());
        titleCol.setStyle("-fx-alignment: LEFT;");


        TableColumn<TimeFrameWidgetObject, Boolean> selectedCol = new TableColumn(I18n.getInstance().getString("plugin.dashboard.timeframe.table.selected"));
        selectedCol.setCellValueFactory(param -> param.getValue().selectedProperty());
        selectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectedCol));
        selectedCol.setStyle("-fx-alignment: CENTER;");


        TableColumn<TimeFrameWidgetObject, TimeFrameWidgetObject.End> endCol = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.timeframe.table.end"));
        endCol.setCellValueFactory(object -> object.getValue().endObjectPropertyProperty());
        endCol.setEditable(true);

        ObservableList<TimeFrameWidgetObject.End> endOptions = FXCollections.observableArrayList(TimeFrameWidgetObject.End.values());
        endCol.setCellFactory(
                ComboBoxTableCell.forTableColumn(
                        new StringConverter<TimeFrameWidgetObject.End>() {
                            @Override
                            public String toString(TimeFrameWidgetObject.End end) {
                                String string = "";
                                switch (end) {
                                    case NONE:
                                        string = I18n.getInstance().getString("plugin.dashboard.timeframe.end.none");
                                        break;
                                    case LAST_TS:
                                        string = I18n.getInstance().getString("plugin.dashboard.timeframe.end.lastts");
                                        break;
                                    case PERIODE_UNTIL:
                                        string = I18n.getInstance().getString("plugin.dashboard.timeframe.end.periodeuntil");
                                        break;
                                }
                                return string;


                            }

                            @Override
                            public TimeFrameWidgetObject.End fromString(String s) {

                                if (s.equals(I18n.getInstance().getString("plugin.dashboard.timeframe.end.none"))) {
                                    return TimeFrameWidgetObject.End.NONE;
                                } else if (s.equals(I18n.getInstance().getString("plugin.dashboard.timeframe.end.lastts"))) {
                                    return TimeFrameWidgetObject.End.LAST_TS;
                                } else if (s.equals(I18n.getInstance().getString("plugin.dashboard.timeframe.end.periodeuntil"))) {
                                    return TimeFrameWidgetObject.End.PERIODE_UNTIL;
                                }
                                return null;


                            }
                        },
                        endOptions
                )
        );

        TableColumn<TimeFrameWidgetObject, Boolean> countOfSamplesCol = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.timeframe.table.count"));
        countOfSamplesCol.setCellValueFactory(object -> object.getValue().countOfSamplesProperty());
        countOfSamplesCol.setCellFactory(CheckBoxTableCell.forTableColumn(countOfSamplesCol));
        countOfSamplesCol.setEditable(true);



        TableColumn<TimeFrameWidgetObject, TimeFrameWidgetObject.Start> startCol = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.timeframe.table.start"));
        startCol.setCellValueFactory(object -> object.getValue().startObjectPropertyProperty());
        startCol.setEditable(true);

        ObservableList<TimeFrameWidgetObject.Start> startOptions = FXCollections.observableArrayList(TimeFrameWidgetObject.Start.values());
        startCol.setCellFactory(
                ComboBoxTableCell.forTableColumn(
                        new StringConverter<TimeFrameWidgetObject.Start>() {
                            @Override
                            public String toString(TimeFrameWidgetObject.Start start) {
                                String string = "";
                                switch (start) {
                                    case NONE:
                                        string = I18n.getInstance().getString("plugin.dashboard.timeframe.start.none");
                                        break;
                                    case PERIODE_FROM:
                                        string = I18n.getInstance().getString("plugin.dashboard.timeframe.start.periodefrom");
                                        break;
                                }
                                return string;
                            }

                            @Override
                            public TimeFrameWidgetObject.Start fromString(String s) {
                                if (s.equals(I18n.getInstance().getString("plugin.dashboard.timeframe.start.none"))) {
                                    return TimeFrameWidgetObject.Start.NONE;
                                } else if (s.equals(I18n.getInstance().getString("plugin.dashboard.timeframe.start.periodefrom"))) {
                                    return TimeFrameWidgetObject.Start.PERIODE_FROM;
                                }
                                return null;
                            }
                        },
                        startOptions
                )
        );

        setItems(list);
        getColumns().addAll(idCol, typeAttributeColumn(), titleCol, startCol, endCol,countOfSamplesCol, selectedCol);


    }


    public TableColumn<TimeFrameWidgetObject, String> typeAttributeColumn() {
        Callback treeTableColumnCallback = new Callback<TableColumn<TimeFrameWidgetObject, String>, TableCell<TimeFrameWidgetObject, String>>() {
            @Override
            public TableCell<TimeFrameWidgetObject, String> call(TableColumn<TimeFrameWidgetObject, String> param) {
                TableCell<TimeFrameWidgetObject, String> cell = new TableCell<TimeFrameWidgetObject, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        if (item != null && !empty) {
                            try {
                                ImageView icon = ((TimeFrameWidgetObject) getTableRow().getItem()).getImagePreview();
                                icon.setPreserveRatio(true);
                                icon.setFitHeight(18d);
                                setGraphic(icon);
                            } catch (Exception ex) {
                                logger.warn(ex.getMessage());
                            }
                            setText(item);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                };
                return cell;
            }
        };
        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<TimeFrameWidgetObject, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<TimeFrameWidgetObject, String> param) {
                try {

                    return new SimpleStringProperty(param.getValue().getConfig().getType());

                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<TimeFrameWidgetObject, String> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.type"));

        column.setId("Type");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

        column.setPrefWidth(100);

        return column;
    }
}




