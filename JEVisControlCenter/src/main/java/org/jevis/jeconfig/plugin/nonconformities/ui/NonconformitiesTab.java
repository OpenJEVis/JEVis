package org.jevis.jeconfig.plugin.nonconformities.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesController;
import org.jevis.jeconfig.plugin.nonconformities.data.Nonconformities;
import org.jevis.jeconfig.plugin.nonconformities.data.TableFilter;

public class NonconformitiesTab extends Tab {

    private Nonconformities plan;
    private NonconformitiesTable nonconformitiesTable;

    public NonconformitiesTab(Nonconformities plan, NonconformitiesController controller) {
        super();

        textProperty().bind(plan.getName());
        this.plan = plan;
        this.nonconformitiesTable = new NonconformitiesTable(plan, plan.getNonconformityList());


        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        double maxListHeight = 100;


        Label lSuche = new Label("Suche");
        JFXTextField fsearch = new JFXTextField();
        fsearch.setPromptText("Suche nach...");

        org.jevis.jeconfig.plugin.action.ui.TagButton mediumButton = new org.jevis.jeconfig.plugin.action.ui.TagButton(I18n.getInstance().getString("plugin.action.filter.medium"), plan.getMediumTags(), plan.getMediumTags());

        ComboBox<String> datumBox = new ComboBox<>();
        datumBox.setItems(FXCollections.observableArrayList("Umsetzung", "Abgeschlossen", "Erstellt"));
        datumBox.getSelectionModel().selectFirst();
        JFXTextField filterDatumText = new JFXTextField();
        filterDatumText.setPromptText("Datum...");
        ComboBox<String> comparatorBox = new ComboBox<>();
        comparatorBox.setItems(FXCollections.observableArrayList(">", "<", "="));
        comparatorBox.getSelectionModel().selectFirst();

        fsearch.textProperty().addListener((observable, oldValue, newValue) -> {
            nonconformitiesTable.setTextFilter(newValue);
            nonconformitiesTable.filter();
        });

        TimeFilterSelector dateSelector = new TimeFilterSelector(plan);

        dateSelector.getValuePropertyProperty().addListener(new ChangeListener<DateFilter>() {
            @Override
            public void changed(ObservableValue<? extends DateFilter> observableValue, DateFilter dateFilter, DateFilter t1) {
                nonconformitiesTable.setDateFilter(t1);
                nonconformitiesTable.filter();
            }
        });


        Separator vSep1 = new Separator(Orientation.VERTICAL);
        Separator vSep2 = new Separator(Orientation.VERTICAL);
        ;

        GridPane.setRowSpan(vSep1, 2);
        GridPane.setRowSpan(vSep2, 2);
        gridPane.addColumn(0, lSuche, fsearch);
        gridPane.addColumn(1, vSep1);
        gridPane.addColumn(2, new Region(), mediumButton);
        gridPane.addColumn(3, vSep2);
        gridPane.addColumn(4, new Label("Zeitbereich"), dateSelector);


        mediumButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("List Changed: " + c);
                while (c.next()) {
                    nonconformitiesTable.setFilterMedium((ObservableList<String>) c.getList());
                    nonconformitiesTable.filter();
                }
            }
        });
        nonconformitiesTable.setMedium(mediumButton.getSelectedTags());
        nonconformitiesTable.filter();


        //HBox hBox = new HBox(filterDatumText, comparatorBox, datumBox);
        EventHandler<ActionEvent> dateFilerEvent = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (comparatorBox.getSelectionModel().getSelectedItem().equals(">")) {
                    nonconformitiesTable.getTableFilter().setPlannedDateComp(TableFilter.DATE_COMPARE.BIGGER_THAN);
                } else if (comparatorBox.getSelectionModel().getSelectedItem().equals("<")) {
                    nonconformitiesTable.getTableFilter().setPlannedDateComp(TableFilter.DATE_COMPARE.SMALLER_THAN);
                } else if (comparatorBox.getSelectionModel().getSelectedItem().equals("=")) {
                    nonconformitiesTable.getTableFilter().setPlannedDateComp(TableFilter.DATE_COMPARE.EQUALS);
                }
                nonconformitiesTable.getTableFilter().setPlannedDateFilter(filterDatumText.getText());

            }
        };

        comparatorBox.setOnAction(dateFilerEvent);





        //gridPane.add(hBox, 0, 1, 3, 1);


        BorderPane borderPane = new BorderPane();
        borderPane.setTop(gridPane);
        borderPane.setCenter(nonconformitiesTable);

        TableSumPanel tableSumPanel = new TableSumPanel(nonconformitiesTable.getItems());
        borderPane.setBottom(tableSumPanel);

        nonconformitiesTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    controller.openDataForm();//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });
        setContent(borderPane);


    }

    public NonconformitiesTab(String text, Node content, Nonconformities plan) {
        super(text, content);
        this.plan = plan;
    }

    public Nonconformities getNonconformities() {
        return plan;
    }

    public void setActionPlan(Nonconformities plan) {
        this.plan = plan;
    }

    public NonconformitiesTable getActionTable() {
        return nonconformitiesTable;
    }
}
