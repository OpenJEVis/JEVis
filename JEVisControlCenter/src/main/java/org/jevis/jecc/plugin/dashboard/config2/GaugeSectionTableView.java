package org.jevis.jecc.plugin.dashboard.config2;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.control.ColorPickerAdv;

public class GaugeSectionTableView extends TableView<GaugeSectionPojo> {

    private static final Logger logger = LogManager.getLogger(GaugeSectionTableView.class);

    public GaugeSectionTableView(ObservableList<GaugeSectionPojo> observableList) {
        super(observableList);
        this.setEditable(true);
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<GaugeSectionPojo, String> startCol = new TableColumn(I18n.getInstance().getString("plugin.dashboard.gaugewidget.start"));
        startCol.setCellValueFactory(param -> param.getValue().getStartasStringProperty());
        startCol.setCellFactory(TextFieldTableCell.forTableColumn());
        startCol.setStyle("-fx-alignment: LEFT;");


        TableColumn<GaugeSectionPojo, String> endCol = new TableColumn(I18n.getInstance().getString("plugin.dashboard.gaugewidget.end"));
        endCol.setCellValueFactory(param -> param.getValue().getEndAsStringProperty());
        endCol.setCellFactory(TextFieldTableCell.forTableColumn());
        endCol.setStyle("-fx-alignment: LEFT;");
        startCol.setEditable(true);
        endCol.setEditable(true);


        this.getColumns().addAll(startCol, endCol, buildColor());


    }

    public TableColumn<GaugeSectionPojo, Color> buildColor() {

        Callback treeTableColumnCallback = new Callback<TableColumn<GaugeSectionPojo, Color>, TableCell<GaugeSectionPojo, Color>>() {
            @Override
            public TableCell<GaugeSectionPojo, Color> call(TableColumn<GaugeSectionPojo, Color> param) {
                TableCell<GaugeSectionPojo, Color> cell = new TableCell<GaugeSectionPojo, Color>() {
                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        if (item != null && !empty) {
                            ColorPickerAdv colorPicker = new ColorPickerAdv();
                            colorPicker.setMaxWidth(100d);
                            colorPicker.setValue(item);

                            colorPicker.setStyle("-fx-color-label-visible: false ;");
                            colorPicker.setOnAction(event -> {
                                GaugeSectionPojo gaugeSectionPojo = getTableRow().getItem();
                                gaugeSectionPojo.setColor(colorPicker.getValue());
                            });

//                            addFocusRefreshListener(colorPicker);

                            setGraphic(colorPicker);
                        } else {
                            setGraphic(null);
                        }

                    }
                };
                return cell;
            }
        };

        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<GaugeSectionPojo, Color>, ObservableValue<Color>>() {
            @Override
            public ObservableValue<Color> call(TableColumn.CellDataFeatures<GaugeSectionPojo, Color> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue().getColor());
                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<GaugeSectionPojo, Color> column = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.gaugewidget.color"));
        column.setId("Color");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

        return column;
    }

}
