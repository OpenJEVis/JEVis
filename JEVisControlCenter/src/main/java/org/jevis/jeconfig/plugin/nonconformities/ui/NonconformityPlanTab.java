package org.jevis.jeconfig.plugin.nonconformities.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.table.SummeryTable;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesController;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityPlan;

public class NonconformityPlanTab extends Tab {

    private static final Logger logger = LogManager.getLogger(NonconformityPlanTab.class);

    private NonconformityPlan plan;
    private NonconformityPlanTable nonconformityPlanTable;


    public NonconformityPlanTab(NonconformityPlan plan, NonconformitiesController controller, BooleanProperty updateTrigger) {
        super();

        textProperty().bind(plan.getName());
        this.plan = plan;
        this.nonconformityPlanTable = new NonconformityPlanTable(plan, plan.getNonconformityList(), updateTrigger);


        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        double maxListHeight = 100;


        Label lSuche = new Label(I18n.getInstance().getString("plugin.nonconformities.serach"));
        JFXTextField fsearch = new JFXTextField();
        fsearch.setPromptText(I18n.getInstance().getString("plugin.nonconformities.serachfor"));


        TagButton mediumButton = new TagButton(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.medium"), plan.getMediumTags(), plan.getMediumTags());
        TagButton fieldButton = new TagButton(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.field"), plan.getFieldsTags(), plan.getFieldsTags());
        TagButton stausButton = new TagButton(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.staus"), plan.getStatusTags(), plan.getStatusTags());
        TagButton seuButton = new TagButton(I18n.getInstance().getString("plugin.nonconformities.seu"),plan.getSignificantEnergyUseTags(),plan.getSignificantEnergyUseTags());



        fsearch.textProperty().addListener((observable, oldValue, newValue) -> {
            nonconformityPlanTable.setTextFilter(newValue);
            nonconformityPlanTable.filter();
        });

        TimeFilterSelector dateSelector = new TimeFilterSelector(plan);

        dateSelector.getValuePropertyProperty().addListener(new ChangeListener<DateFilter>() {
            @Override
            public void changed(ObservableValue<? extends DateFilter> observableValue, DateFilter dateFilter, DateFilter t1) {
                nonconformityPlanTable.setDateFilter(t1);
                nonconformityPlanTable.filter();
            }
        });


        Separator vSep1 = new Separator(Orientation.VERTICAL);
        Separator vSep2 = new Separator(Orientation.VERTICAL);

        GridPane.setRowSpan(vSep1, 2);
        GridPane.setRowSpan(vSep2, 2);
        gridPane.addColumn(0, lSuche, fsearch);
        gridPane.addColumn(1, vSep1);
        gridPane.addColumn(2, new Label(I18n.getInstance().getString("plugin.nonconformities.filter")), stausButton);
        gridPane.addColumn(3, new Region(), mediumButton);
        gridPane.addColumn(4, new Region(), fieldButton);
        gridPane.addColumn(5, new Region(), seuButton);
        gridPane.addColumn(6, vSep2);
        gridPane.addColumn(7, new Label(I18n.getInstance().getString("plugin.nonconformities.date")), dateSelector);


        mediumButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                logger.debug("List Changed: {}",c);
                while (c.next()) {
                    nonconformityPlanTable.setFilterMedium((ObservableList<String>) c.getList());
                    nonconformityPlanTable.filter();
                }
            }
        });


        stausButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                logger.debug("List Changed: {}",c);
                while (c.next()) {
                    nonconformityPlanTable.setStaus((ObservableList<String>) c.getList());
                    nonconformityPlanTable.filter();
                }
            }
        });
        fieldButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                logger.debug("List Changed: {}",c);
                while (c.next()) {
                    nonconformityPlanTable.setFields((ObservableList<String>) c.getList());
                    nonconformityPlanTable.filter();
                }
            }
        });

        seuButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                nonconformityPlanTable.setSeu((ObservableList<String>) change.getList());
                nonconformityPlanTable.filter();
            }
        });


        //nonconformityPlanTable.setStaus(stausButton.getSelectedTags());
        nonconformityPlanTable.setSeu(seuButton.getSelectedTags());
        nonconformityPlanTable.setMedium(mediumButton.getSelectedTags());
        nonconformityPlanTable.setFields(fieldButton.getSelectedTags());
        nonconformityPlanTable.setStaus(stausButton.getSelectedTags());
        nonconformityPlanTable.filter();







        //gridPane.add(hBox, 0, 1, 3, 1);


        BorderPane borderPane = new BorderPane();
        borderPane.setTop(gridPane);
        borderPane.setCenter(nonconformityPlanTable);
        SummeryTable summeryTable = new SummeryTable(nonconformityPlanTable);
        summeryTable.setItems(nonconformityPlanTable.getSummeryData());

//        TableSumPanel tableSumPanel = new TableSumPanel(nonconformityPlanTable.getItems());
        borderPane.setBottom(summeryTable);

        nonconformityPlanTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    controller.openDataForm(false);//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });
        setContent(borderPane);


    }

    public NonconformityPlanTab(String text, Node content, NonconformityPlan plan, BooleanProperty updateTrigger) {
        super(text, content);
        this.plan = plan;
    }

    public NonconformityPlan getNonconformityPlan() {
        return plan;
    }

    public void setActionPlan(NonconformityPlan plan) {
        this.plan = plan;
    }

    public NonconformityPlanTable getActionTable() {
        return nonconformityPlanTable;
    }
}
