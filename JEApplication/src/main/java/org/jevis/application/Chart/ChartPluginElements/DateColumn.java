package org.jevis.application.Chart.ChartPluginElements;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;
import org.jevis.application.jevistree.plugin.ChartPlugin;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.Observable;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class DateColumn extends TreeTableColumn<JEVisTreeRow, DateTime> implements ChartPluginColumn {
    public static String COLUMN_ID = "DateColumn";
    private final Image imgMarkAll = new Image(ChartPlugin.class.getResourceAsStream("/icons/" + "jetxee-check-sign-and-cross-sign-3.png"));
    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
    private final Tooltip tpMarkAll = new Tooltip(rb.getString("plugin.graph.dialog.changesettings.tooltip.forall"));
    private TreeTableColumn<JEVisTreeRow, DateTime> dateColumn;
    private GraphDataModel data;
    private JEVisTree tree;
    private String columnName;
    private DATE_TYPE type;

    public DateColumn(JEVisTree tree, String columnName, DATE_TYPE type) {
        this.tree = tree;
        this.columnName = columnName;
        this.type = type;
    }

    private DatePicker buildDatePicker(ChartDataModel data, DATE_TYPE type) {

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
                                data.getAttribute().getTimestampFromFirstSample().getYear(),
                                data.getAttribute().getTimestampFromFirstSample().getMonthOfYear(),
                                data.getAttribute().getTimestampFromFirstSample().getDayOfMonth());
                        LocalDate ldEnd = LocalDate.of(
                                data.getAttribute().getTimestampFromLastSample().getYear(),
                                data.getAttribute().getTimestampFromLastSample().getMonthOfYear(),
                                data.getAttribute().getTimestampFromLastSample().getDayOfMonth());

                        if (data.getAttribute().getTimestampFromFirstSample() != null && item.isBefore(ldBeginn)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }

                        if (data.getAttribute().getTimestampFromFirstSample() != null && item.isAfter(ldEnd)) {
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

    public TreeTableColumn<JEVisTreeRow, DateTime> getDateColumn() {
        return dateColumn;
    }

    @Override
    public void setGraphDataModel(GraphDataModel graphDataModel) {
        this.data = graphDataModel;
        this.data.addObserver(this);
        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, DateTime> column = new TreeTableColumn(columnName);
        column.setPrefWidth(160);
        column.setId(COLUMN_ID);
        column.setCellValueFactory(param -> {
            try {
                ChartDataModel data = getData(param.getValue().getValue());
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
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, DateTime>, TreeTableCell<JEVisTreeRow, DateTime>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, DateTime> call(TreeTableColumn<JEVisTreeRow, DateTime> param) {

                TreeTableCell<JEVisTreeRow, DateTime> cell = new TreeTableCell<JEVisTreeRow, DateTime>() {

                    @Override
                    public void commitEdit(DateTime newValue) {
                        super.commitEdit(newValue);
                        ChartDataModel data = getData(getTreeTableRow().getItem());

                        if (type == DATE_TYPE.START) {
                            data.setSelectedStart(newValue);
                        } else {
                            data.setSelectedEnd(newValue);
                        }
                    }

                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            StackPane stackPane = new StackPane();
                            if (getTreeTableRow().getItem() != null && tree != null
                                    && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                DatePicker dp = buildDatePicker(data, type);

                                ImageView imageMarkAll = new ImageView(imgMarkAll);
                                imageMarkAll.fitHeightProperty().set(12);
                                imageMarkAll.fitWidthProperty().set(12);

                                Button tb = new Button("", imageMarkAll);

                                tb.setTooltip(tpMarkAll);

                                tb.setOnAction(event -> {
                                    if (type == DATE_TYPE.START) {
                                        LocalDate ld = dp.valueProperty().get();
                                        DateTime newDateTimeStart = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 0, 0, 0, 0);
                                        getData().getSelectedData().forEach(mdl -> {
                                            if (mdl.getSelected()) {
                                                mdl.setSelectedStart(newDateTimeStart);
                                            }
                                        });
                                    } else if (type == DATE_TYPE.END) {
                                        LocalDate ld = dp.valueProperty().get();
                                        DateTime newDateTimeEnd = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 23, 59, 59, 999);
                                        getData().getSelectedData().forEach(mdl -> {
                                            if (mdl.getSelected()) {
                                                mdl.setSelectedEnd(newDateTimeEnd);
                                            }
                                        });
                                    }
                                    tree.refresh();
                                });

                                HBox hbox = new HBox();
                                hbox.getChildren().addAll(dp, tb);
                                stackPane.getChildren().add(hbox);
                                StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                dp.setOnAction(event -> {
                                    LocalDate ld = dp.getValue();
                                    DateTime jodaTime = null;
                                    if (type == DATE_TYPE.START) {
                                        jodaTime = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 0, 0, 0, 0);
                                    } else if (type == DATE_TYPE.END) {
                                        jodaTime = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 23, 59, 59, 999);
                                    }
                                    commitEdit(jodaTime);
                                });

                                if (data.getAttribute() != null && data.getAttribute().hasSample()) {
                                    stackPane.setDisable(false);
                                } else {
                                    stackPane.setDisable(true);
                                }
                            }


                            setText(null);
                            setGraphic(stackPane);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        this.dateColumn = column;
    }

    @Override
    public GraphDataModel getData() {
        return this.data;
    }

    @Override
    public void update(Observable o, Object arg) {
        update();
    }

    public enum DATE_TYPE {

        START, END
    }
}
