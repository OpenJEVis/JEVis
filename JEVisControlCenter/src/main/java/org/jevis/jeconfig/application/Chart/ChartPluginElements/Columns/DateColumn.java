package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class DateColumn extends TreeTableColumn<JEVisTreeRow, DateTime> implements ChartPluginColumn {
    public static String COLUMN_ID = "DateColumn";

    private TreeTableColumn<JEVisTreeRow, DateTime> dateColumn;
    private AnalysisDataModel data;
    private JEVisTree tree;
    private String columnName;
    private DATE_TYPE type;
    private final JEVisDataSource dataSource;

    public DateColumn(JEVisTree tree, JEVisDataSource dataSource, String columnName, DATE_TYPE type) {
        this.tree = tree;
        this.dataSource = dataSource;
        this.columnName = columnName;
        this.type = type;
    }

    private DatePicker buildDatePicker(ChartDataRow data, DATE_TYPE type) {

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

        JFXDatePicker datePicker;
        JFXTimePicker timePicker;

        List<ChartDataRow> singletonList = Collections.singletonList(data);

        PickerCombo pickerCombo = new PickerCombo(getData(), singletonList, false);

        if (type == DATE_TYPE.START) {
            datePicker = pickerCombo.getStartDatePicker();
            timePicker = pickerCombo.getStartTimePicker();
        } else {
            datePicker = pickerCombo.getEndDatePicker();
            timePicker = pickerCombo.getEndTimePicker();
        }

        return datePicker;
    }

    public TreeTableColumn<JEVisTreeRow, DateTime> getDateColumn() {
        return dateColumn;
    }

    @Override
    public void setGraphDataModel(AnalysisDataModel analysisDataModel) {
        this.data = analysisDataModel;
        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, DateTime> column = new TreeTableColumn(columnName);
        column.setPrefWidth(160);
        column.setId(COLUMN_ID);
        column.setCellValueFactory(param -> {
            try {
                ChartDataRow data = getData(param.getValue().getValue());
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
                        ChartDataRow data = getData(getTreeTableRow().getItem());

                        if (type == DATE_TYPE.START) {
                            data.setSelectedStart(newValue);
                        } else {
                            data.setSelectedEnd(newValue);
                        }
                    }

                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {
                                if (getTreeTableRow().getItem() != null && tree != null
                                        && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {
                                    StackPane stackPane = new StackPane();

                                    ChartDataRow data = getData(getTreeTableRow().getItem());
                                    DatePicker dp = buildDatePicker(data, type);

                                    ImageView imageMarkAll = new ImageView(imgMarkAll);
                                    imageMarkAll.fitHeightProperty().set(13);
                                    imageMarkAll.fitWidthProperty().set(13);

                                    Button tb = new Button("", imageMarkAll);
                                    tb.setTooltip(tooltipMarkAll);

                                    tb.setOnAction(event -> {
                                        if (type == DATE_TYPE.START) {
                                            LocalDate ld = dp.valueProperty().get();
                                            DateTime newDateTimeStart = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 0, 0, 0, 0);
                                            getData().getSelectedData().forEach(mdl -> {
                                                if (!mdl.getSelectedcharts().isEmpty()) {
                                                    mdl.setSelectedStart(newDateTimeStart);
                                                }
                                            });
                                        } else if (type == DATE_TYPE.END) {
                                            LocalDate ld = dp.valueProperty().get();
                                            DateTime newDateTimeEnd = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 23, 59, 59, 999);
                                            getData().getSelectedData().forEach(mdl -> {
                                                if (!mdl.getSelectedcharts().isEmpty()) {
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

                                    dp.setDisable(!data.isSelectable());
                                    tb.setDisable(!data.isSelectable());
                                    setGraphic(stackPane);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                };

                return cell;
            }
        });

        this.dateColumn = column;
    }

    @Override
    public AnalysisDataModel getData() {
        return this.data;
    }

    @Override
    public JEVisDataSource getDataSource() {
        return dataSource;
    }

    public enum DATE_TYPE {

        START, END
    }
}
