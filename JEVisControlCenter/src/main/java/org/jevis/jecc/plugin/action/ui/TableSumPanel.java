package org.jevis.jecc.plugin.action.ui;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.action.data.ActionData;
import org.jevis.jecc.plugin.action.data.ActionPlanData;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


public class TableSumPanel extends GridPane {
    ActionData fakeNames = new ActionData();
    MFXTextField f_sumInvestment = new MFXTextField();
    MFXTextField f_sumSavingsYear = new MFXTextField();
    MFXTextField f_sumSavingEnergy = new MFXTextField();
    Label l_sumLabel = new Label(I18n.getInstance().getString("plugin.action.sumtable.total"));
    Label l_sumInvestment = new Label(fakeNames.npv.get().investment.getName());
    Label l_sumSavingsYear = new Label(fakeNames.npv.get().einsparung.getName());
    Label l_sumSavingEnergy = new Label(I18n.getInstance().getString("plugin.action.consumption.diff"));
    ObservableList<ActionData> data;
    NumberFormat currencyFormat = NumerFormating.getInstance().getCurrencyFormat();
    NumberStringConverter nsc = new NumberStringConverter() {
        @Override
        public String toString(Number value) {
            return currencyFormat.format(value);
        }
    };
    UnitDoubleConverter unitDoubleConverter = new UnitDoubleConverter();
    private Map<String, MFXTextField> columns = new HashMap<>();
    private ActionPlanData actionPlan;

    private TableView<StringProperty> tableView = new TableView();
    private Map<String, StringProperty> valueMap = new HashMap<>();
    private Statistics statistics;


    public TableSumPanel(ActionPlanData actionPlan, ObservableList<ActionData> data, Statistics statistics) {
        this.data = data;
        this.actionPlan = actionPlan;
        this.statistics = statistics;
        //System.out.println("Create new SumTable for: " + actionPlan.getName());
        setHgap(5);
        setVgap(5);
        setPadding(new Insets(20, 10, 10, 0));

        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));


        f_sumInvestment.setAlignment(Pos.CENTER_RIGHT);
        f_sumSavingsYear.setAlignment(Pos.CENTER_RIGHT);
        f_sumSavingEnergy.setAlignment(Pos.CENTER_RIGHT);

        // addRow(0, new Region(), l_sumInvestment, l_sumSavingsYear);
        // addRow(1, l_sumLabel, f_sumInvestment, f_sumSavingsYear);


        actionPlan.getMediumTags().addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved() || c.wasReplaced()) {
                    update();
                }
                // updateLayout();
                // updateData();
            }

        });
        data.addListener((ListChangeListener<? super ActionData>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved() || c.wasReplaced()) {
                    try {
                        update();
                        // updateLayout();
                        // updateData();
                    } catch (Exception ex) {
                    }
                }
            }
        });

        GridPane gridPane = new GridPane();
        tableView.setMaxHeight(60);
        tableView.setPrefWidth(4000);
        tableView.getStylesheets().add("/styles/ActionSumTable.css");


        StringProperty sumIcon = new SimpleStringProperty("Σ");
        TableColumn<StringProperty, String> sumCol = new TableColumn(" ");
        sumCol.setCellValueFactory(param -> sumIcon);
        sumCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        sumCol.setSortable(true);
        sumCol.setSortType(TableColumn.SortType.ASCENDING);
        tableView.getColumns().add(sumCol);
        tableView.getItems().add(new SimpleStringProperty());
        update();

        VBox hBox = new VBox(tableView);
        hBox.setFillWidth(true);
        GridPane.setHgrow(tableView, Priority.ALWAYS);
        gridPane.add(hBox, 0, 0);
        getChildren().add(gridPane);
    }


    private StringProperty getOrPut(String key) {
        if (!valueMap.containsKey(key)) {
            valueMap.put(key, new SimpleStringProperty());

            TableColumn<StringProperty, String> actionNrPropertyCol = new TableColumn(key);
            // actionNrPropertyCol.setCellValueFactory;
            actionNrPropertyCol.setCellValueFactory(param -> valueMap.get(key));
            actionNrPropertyCol.setStyle("-fx-alignment: baseline-center;");
            actionNrPropertyCol.setSortable(false);
            actionNrPropertyCol.setSortType(TableColumn.SortType.ASCENDING);
            tableView.getColumns().add(actionNrPropertyCol);


        }
        return valueMap.get(key);
    }

    public void update() {
        //System.out.println("//////////////////////// Update");
        //getChildren().clear();
        // getColumnConstraints().clear();

        statistics.update();
        data.forEach(actionData -> {
            actionData.getEnpi().update();
            actionData.getConsumption().update();
        });

        double sumInvest = 0;
        double sumEinsparrung = 0;
        for (ActionData actionData : data) {
            sumInvest += actionData.npv.get().getInvestment();
            sumEinsparrung += actionData.npv.get().einsparung.get();
        }
        getOrPut(fakeNames.npv.get().investment.getName()).set(nsc.toString(sumInvest));
        getOrPut("Einsparung/a").set(nsc.toString(sumEinsparrung));
        StringProperty savingsTotal = getOrPut("Einsparung seit IBT");
        savingsTotal.set(nsc.toString(statistics.getSumSinceImplementation()));

        for (String medium : actionPlan.getMediumTags()) {

            double result = data.stream().filter(data -> data.mediaTags.get().equals(medium))
                    .mapToDouble(actionData -> actionData.consumption.get().diff.get())
                    .sum();

            getOrPut(medium).set(NumerFormating.getInstance().getDoubleConverter().toString(result) + " kWh");
        }

        //Spacer column
        getOrPut(" ").set("");

        for (String status : actionPlan.getStatustags()) {
            int sum = data.stream().filter(actionData -> actionData.statusTags.get().equals(status))
                    .collect(Collectors.toList()).size();
            getOrPut(status).set(sum + "");
        }


    }

}
