package org.jevis.jecc.plugin.action.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.skin.TableViewSkin;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.action.data.NPVYearData;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class NPVTableView extends TableView<NPVYearData> {

    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    public NPVTableView(ObservableList<NPVYearData> data) {
        super();
        setItems(data);
        TableColumn<NPVYearData, Integer> yearCol = new TableColumn(I18n.getInstance().getString("plugin.action.nvp.year"));
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        yearCol.setEditable(false);
        yearCol.setPrefWidth(50);

        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));

        TableColumn<NPVYearData, Double> depositCol = new TableColumn(I18n.getInstance().getString("plugin.action.nvp.deposit"));
        depositCol.setCellValueFactory(new PropertyValueFactory<>("deposit"));
        depositCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        depositCol.setEditable(true);
        depositCol.setPrefWidth(160);

        TableColumn<NPVYearData, Double> investmentCol = new TableColumn(I18n.getInstance().getString("plugin.action.nvp.investment"));
        investmentCol.setCellValueFactory(param -> param.getValue().investment.asObject());
        investmentCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        investmentCol.setPrefWidth(160);
        //investmentCol.setCellFactory(buildDateTimeFactory());

        TableColumn<NPVYearData, Double> netamountCol = new TableColumn(I18n.getInstance().getString("plugin.action.nvp.netamount"));
        netamountCol.setCellValueFactory(param -> param.getValue().netamount.asObject());
        netamountCol.setPrefWidth(150);

        TableColumn<NPVYearData, Double> cdpCol = new TableColumn(I18n.getInstance().getString("plugin.action.nvp.cdp"));
        cdpCol.setCellValueFactory(param -> param.getValue().discountedCashFlow.asObject());
        //NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        cdpCol.setPrefWidth(150);

        Callback<TableColumn<NPVYearData, Double>, TableCell<NPVYearData, Double>> currencyFactory = new Callback<TableColumn<NPVYearData, Double>, TableCell<NPVYearData, Double>>() {
            @Override
            public TableCell<NPVYearData, Double> call(TableColumn<NPVYearData, Double> tc) {
                return new TableCell<NPVYearData, Double>() {

                    @Override
                    protected void updateItem(Double price, boolean empty) {
                        super.updateItem(price, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(currencyFormat.format(price));

                        }
                    }
                };
            }
        };


        cdpCol.setCellFactory(currencyFactory);
        netamountCol.setCellFactory(currencyFactory);
        investmentCol.setCellFactory(currencyFactory);
        depositCol.setCellFactory(currencyFactory);


        this.getColumns().addAll(yearCol, depositCol, investmentCol, netamountCol, cdpCol);
        this.getColumns().forEach(column -> {
            column.setSortable(false);
            column.setStyle("-fx-alignment: CENTER-RIGHT;");
        });
        this.setEditable(true);

        autoFitTable();
    }

    public void autoFitTable() {
        for (TableColumn<NPVYearData, ?> column : this.getColumns()) {
            try {
                if (getSkin() != null) {
                    columnToFitMethod.invoke(getSkin(), column, -1);
                }
            } catch (Exception e) {
            }
        }
    }

}
