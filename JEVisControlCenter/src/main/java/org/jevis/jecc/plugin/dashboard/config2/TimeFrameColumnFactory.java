package org.jevis.jecc.plugin.dashboard.config2;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.jevis.jecc.plugin.dashboard.widget.Widget;

public class TimeFrameColumnFactory extends WidgetColumnFactory {


    private static final Logger logger = LogManager.getLogger(TimeFrameColumnFactory.class);

    private Property<Widget> selectedWidget = new SimpleObjectProperty<>();


    public TimeFrameColumnFactory(DashboardControl control) {
        super(control);
    }

    public Widget getSelectedWidget() {
        return selectedWidget.getValue();
    }

    public void setSelectedWidget(Widget selectedWidget) {
        this.selectedWidget.setValue(selectedWidget);
    }

    @Override
    public TableView<Widget> buildTable(ObservableList<Widget> list) {

        this.table = new TableView<>();
        this.table.setEditable(true);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.table.getColumns().add(buildIDColoumn());
        this.table.getColumns().add(typeAttributeColumn());
        this.table.getColumns().add(titleAttributeColumn());
        this.table.getColumns().add(buildCheckBoxColumn());


        this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        /** disable all sorting, the problem is that is its the same observable list the Controller is using and we don't want so sort it**/
        this.table.getColumns().forEach(widgetTableColumn -> {
            widgetTableColumn.setSortable(false);
        });

        this.table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.table.getItems().forEach(widget -> {
                this.control.highlightWidgetInView(widget, this.table.getSelectionModel().getSelectedItems().contains(widget));
            });
        });
//        this.filteredData = new FilteredList<Widget>(list);
        this.table.setItems(list);


        return table;
    }


    @Override
    public TableColumn<Widget, String> titleAttributeColumn() {

        TableColumn<Widget, String> column = new TableColumn<>(I18n.getInstance().getString("jevistree.widget.column.title"));
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().config.getTitle()));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setId("Title");
        column.setPrefWidth(80);
        column.setEditable(false);

        return column;
    }


    private TableColumn<Widget, Boolean> buildCheckBoxColumn() {
        TableColumn tableColumn = new TableColumn();
        tableColumn.setEditable(true);
        tableColumn.setCellFactory(param -> {
            final CheckBoxTableCell<Widget, Boolean> ctCell = new CheckBoxTableCell<Widget, Boolean>();
            final BooleanProperty selected = new SimpleBooleanProperty();
            ctCell.setSelectedStateCallback(new Callback<Integer, ObservableValue<Boolean>>() {
                @Override
                public ObservableValue<Boolean> call(Integer index) {
                    return selected;
                }
            });
            selected.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> obs, Boolean wasSelected, Boolean isSelected) {
                    if (isSelected) {
                        setSelectedWidget((Widget) ctCell.getTableRow().getItem());
                    }
                }
            });
            selectedWidget.addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    try {
                        if (newValue != null && ctCell.getTableRow() != null) {
                            if (!newValue.equals((Widget) ctCell.getTableRow().getItem())) {
                                selected.setValue(false);
                            } else {
                                selected.setValue(true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            });
            return ctCell;
        });


        return tableColumn;
    }


}



