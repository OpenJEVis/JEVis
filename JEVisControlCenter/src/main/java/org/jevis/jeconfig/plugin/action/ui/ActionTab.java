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

import java.util.stream.Collectors;

public class ActionTab extends Tab {

    private ActionPlanData plan;
    private ActionTable actionTable;


    ObservableList<String> allPlans = FXCollections.observableArrayList();
    ObservableList<String> selectedPlans = FXCollections.observableArrayList();
    private SimpleBooleanProperty isOverview = new SimpleBooleanProperty(false);


    public ActionTab(ActionController controller, ActionPlanData plan) {
        super();

        textProperty().bind(plan.getName());
        this.plan = plan;

        actionTable = new ActionTable(plan, plan.getActionData());

        setClosable(false);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setHgap(10);
        gridPane.setVgap(10);


        Label lSuche = new Label("Suche");
        JFXTextField fsearch = new JFXTextField();
        fsearch.setPromptText("Suche nach...");
        TimeFilterSelector dateSelector = new TimeFilterSelector(plan);

        ObservableList<String> allPlans = FXCollections.observableArrayList();
        ObservableList<String> selectedPlans = FXCollections.observableArrayList();


        //ObservableList<String> allPlans = FXCollections.observableArrayList(controller.getActionPlans().stream().map(aplan -> aplan.getName().get()).collect(Collectors.toList()));
        //ObservableList<String> selectedPlans = FXCollections.observableArrayList(controller.getActionPlans().stream().map(aplan -> aplan.getName().get()).collect(Collectors.toList()));

        ObservableList<String> selectedStatus = FXCollections.observableArrayList(plan.getStatustags());
        ObservableList<String> selectedMedium = FXCollections.observableArrayList(plan.getStatustags());
        ObservableList<String> selectedFields = FXCollections.observableArrayList(plan.getStatustags());


        TagButton planFilterButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.plan"), allPlans, selectedPlans);
        TagButton statusButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.status"), plan.getStatustags(), selectedStatus);
        TagButton mediumButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.medium"), plan.getMediumTags(), selectedMedium);
        TagButton fieldsButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.bereich"), plan.getFieldsTags(), selectedFields);

        actionTable.setFilterStatus(selectedStatus);
        actionTable.setFilterMedium(selectedMedium);
        actionTable.setFilterField(selectedFields);

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
                    }

                    planFilterButton.allTags.setAll(FXCollections.observableArrayList(
                            controller.getActionPlans().stream().map(aplan -> aplan.getName().get()).collect(Collectors.toList())));
                    planFilterButton.selectedTags().setAll(FXCollections.observableArrayList(
                            controller.getActionPlans().stream().map(aplan -> aplan.getName().get()).collect(Collectors.toList()))
                    );


                    planFilterButton.updateList();
                }
            });
        } else {
            planFilterButton.setDisable(true);
        }


        //planButton.setVisible(false);

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

                }
                System.out.println("StatusButton predd: " + c.getList());
                actionTable.setFilterStatus((ObservableList<String>) c.getList());
                actionTable.filter();
            }
        });
        mediumButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("List Changed: " + c);
                while (c.next()) {

                }
                actionTable.setFilterMedium((ObservableList<String>) c.getList());
                actionTable.filter();
            }
        });


        fieldsButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("List Changed: " + c);
                while (c.next()) {

                }
                actionTable.setFilterField((ObservableList<String>) c.getList());
                actionTable.filter();
            }
        });

        planFilterButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("List Changed: " + c);
                while (c.next()) {
                    actionTable.setPlanFilter((ObservableList<String>) c.getList());
                    actionTable.filter();
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
        gridPane.addColumn(5, new Region(), planFilterButton);

        gridPane.addColumn(6, vSep2);
        gridPane.addColumn(7, new Label("Zeitbereich"), dateSelector);


        BorderPane borderPane = new BorderPane();
        borderPane.setTop(gridPane);
        borderPane.setCenter(actionTable);

        TableSumPanel tableSumPanel = new TableSumPanel(actionTable.getItems());
        borderPane.setBottom(tableSumPanel);

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
}
