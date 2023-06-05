package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.action.ActionController;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanOverviewData;
import org.jevis.jeconfig.plugin.action.ui.control.TagButton;

import java.util.stream.Collectors;

public class ActionTab extends Tab {

    private final SimpleBooleanProperty isOverview = new SimpleBooleanProperty(false);
    ObservableList<String> allPlans = FXCollections.observableArrayList();
    ObservableList<String> selectedPlans = FXCollections.observableArrayList();
    private ActionPlanData plan;
    private ActionTable actionTable;

    private Statistics statistics;


    public ActionTab(ActionController controller, ActionPlanData plan) {
        super();

        textProperty().bind(plan.getName());
        this.plan = plan;

        TimeFilterSelector dateSelector = new TimeFilterSelector(plan);
        statistics = new Statistics(plan, dateSelector.getValuePropertyProperty());
        actionTable = new ActionTable(plan, plan.getActionData(), statistics);
        statistics.setData(actionTable.getItems());

        setClosable(false);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setHgap(10);
        gridPane.setVgap(10);


        Label lSuche = new Label("Suche");
        JFXTextField fsearch = new JFXTextField();
        fsearch.setPromptText("Suche nach...");


        ObservableList<String> allPlans = FXCollections.observableArrayList();
        ObservableList<String> selectedPlans = FXCollections.observableArrayList();
        ObservableList<String> selectedStatus = FXCollections.observableArrayList(plan.getStatustags());
        ObservableList<String> selectedMedium = FXCollections.observableArrayList(plan.getMediumTags());
        ObservableList<String> selectedFields = FXCollections.observableArrayList(plan.getFieldsTags());
        ObservableList<String> selectedSEU = FXCollections.observableArrayList(plan.getFieldsTags());


        TagButton planFilterButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.plan"), allPlans, selectedPlans);
        TagButton statusButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.status"), plan.getStatustags(), selectedStatus);
        TagButton mediumButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.medium"), plan.getMediumTags(), selectedMedium);
        TagButton fieldsButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.bereich"), plan.getFieldsTags(), selectedFields);
        TagButton seuButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.seu"), plan.significantEnergyUseTags(), selectedSEU);

        actionTable.setFilterStatus(selectedStatus);
        actionTable.setFilterMedium(selectedMedium);
        actionTable.setFilterField(selectedFields);
        actionTable.setFilterSEU(selectedSEU);

        isOverview.set(plan instanceof ActionPlanOverviewData);
        this.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && isOverview.get()) ((ActionPlanOverviewData) plan).updateData();
        });

        //if (!isOverview.get()) planFilterButton.setMaxWidth(0);

        if (isOverview.get()) {
            planFilterButton.setDisable(false);
            actionTable.setPlanFilter(selectedPlans);
            controller.getActionPlans().addListener(new ListChangeListener<ActionPlanData>() {
                @Override
                public void onChanged(Change<? extends ActionPlanData> c) {
                    while (c.next()) {
                        if (c.wasAdded() || c.wasRemoved()) {
                            try {

                                planFilterButton.allTags().setAll(FXCollections.observableArrayList(
                                        controller.getActionPlans().stream().map(aplan -> aplan.getName().get()).collect(Collectors.toList())));

                                planFilterButton.selectedTags().setAll(FXCollections.observableArrayList(
                                        controller.getActionPlans().stream().map(aplan -> aplan.getName().get()).collect(Collectors.toList()))
                                );

                                planFilterButton.updateList();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }


                }
            });
        } else {
            planFilterButton.setDisable(true);
        }


        fsearch.textProperty().addListener((observable, oldValue, newValue) -> {
            actionTable.setTextFilter(newValue);
            actionTable.filter();
        });
        dateSelector.getValuePropertyProperty().addListener(new ChangeListener<DateFilter>() {
            @Override
            public void changed(ObservableValue<? extends DateFilter> observable, DateFilter oldValue, DateFilter newValue) {
                actionTable.setDateFilter(newValue);
                actionTable.filter();
            }
        });

        statusButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                while (c.next()) {
                    if (c.wasAdded() || c.wasRemoved()) {
                        // System.out.println("!!!!!! actiontab.ststus: " + c);
                        if (!c.wasPermutated()) actionTable.setFilterStatus((ObservableList<String>) c.getList());
                    }

                }
            }
        });
        mediumButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                while (c.next()) {
                    if (c.wasAdded() || c.wasRemoved()) {
                        //System.out.println("!!!!!! actiontab.medium: " + c);
                        if (!c.wasPermutated()) actionTable.setFilterMedium((ObservableList<String>) c.getList());
                    }

                }
            }
        });


        fieldsButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                while (c.next()) {
                    if (c.wasAdded() || c.wasRemoved()) {
                        //System.out.println("!!!!!! actiontab.field: " + c);
                        if (!c.wasPermutated()) actionTable.setFilterField((ObservableList<String>) c.getList());
                    }

                }


            }
        });

        planFilterButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                while (c.next()) {
                    if (c.wasAdded() || c.wasRemoved()) {
                        //System.out.println("!!!!!! actiontab.plan: " + c);
                        if (!c.wasPermutated()) actionTable.setPlanFilter((ObservableList<String>) c.getList());
                    }

                }
            }
        });

        seuButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                while (c.next()) {
                    if (c.wasAdded() || c.wasRemoved()) {
                        // System.out.println("!!!!!! actiontab.seu: " + c);
                        if (!c.wasPermutated()) actionTable.setFilterSEU((ObservableList<String>) c.getList());
                    }

                }
            }
        });


        Separator vSep1 = new Separator(Orientation.VERTICAL);
        Separator vSep2 = new Separator(Orientation.VERTICAL);
        GridPane.setRowSpan(vSep1, 2);
        GridPane.setRowSpan(vSep2, 2);

        gridPane.addColumn(0, lSuche, fsearch);
        gridPane.addColumn(1, vSep1);
        gridPane.addColumn(2, new Label("Filter"), statusButton);
        gridPane.addColumn(3, new Region(), mediumButton);
        gridPane.addColumn(4, new Region(), fieldsButton);
        gridPane.addColumn(5, new Region(), seuButton);
        gridPane.addColumn(6, new Region(), planFilterButton);


        gridPane.addColumn(7, vSep2);
        gridPane.addColumn(8, new Label("Zeitbereich"), dateSelector);


        BorderPane borderPane = new BorderPane();
        borderPane.setTop(gridPane);


        SummeryTable summeryTable = new SummeryTable(actionTable);
        summeryTable.setItems(actionTable.getSummeryData());

        borderPane.setCenter(actionTable);
        borderPane.setBottom(summeryTable);

        actionTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    controller.openDataForm();//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });
        setContent(borderPane);
        //actionTable.setItems(createTestData());

        actionTable.filter();


    }

    public ActionTab(String text, Node content, ActionPlanData plan) {
        super(text, content);
        this.plan = plan;
    }

    public ActionPlanData getActionPlan() {
        return plan;
    }

    public void setActionPlan(ActionPlanData plan) {
        this.plan = plan;
    }

    public ActionTable getActionTable() {
        return actionTable;
    }

    public void updateStatistics() {
        if (statistics != null) {
            statistics.update();
        }
    }

}
