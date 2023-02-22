package org.jevis.jeconfig.plugin.action;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlan;
import org.jevis.jeconfig.plugin.action.ui.*;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActionController {
    private static final Logger logger = LogManager.getLogger(ActionController.class);

    private final ActionPlugin plugin;

    private final ScrollPane scrollPane = new ScrollPane();
    private final AnchorPane contentPane = new AnchorPane();
    private ObservableList<ActionPlan> actionPlans;
    private TabPane tabPane = new TabPane();

    public ActionController(ActionPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadActionView() {

        actionPlans = FXCollections.observableArrayList();
        actionPlans.addListener(new ListChangeListener<ActionPlan>() {
            @Override
            public void onChanged(Change<? extends ActionPlan> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(actionPlan -> {
                            buildTabPane(actionPlan);
                        });

                    }
                }
            }
        });

        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);


    }


    private void buildTabPane(ActionPlan plan) {
        ActionTable actionTable = new ActionTable(plan.getActionData());
        //actionTable.enableSumRow(true);
        ActionTab tab = new ActionTab(plan, actionTable);
        tab.setClosable(false);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("new tab selected: " + newValue);

            ActionPlan actionPlan = ((ActionTab) newValue).getActionPlan();
            actionPlan.loadActionList();

        });

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setHgap(10);
        gridPane.setVgap(10);


        Label lSuche = new Label("Suche");
        JFXTextField fsearch = new JFXTextField();
        fsearch.setPromptText("Suche nach...");
        TimeFilterSelector dateSelector = new TimeFilterSelector(plan);
        TagButton statusButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.status"), plan.getStatustags(), plan.getStatustags());
        TagButton mediumButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.medium"), plan.getMediumTags(), plan.getMediumTags());
        TagButton fieldsButton = new TagButton(I18n.getInstance().getString("plugin.action.filter.bereich"), plan.getFieldsTags(), plan.getFieldsTags());

        actionTable.setFilterStatus(plan.getStatustags());
        actionTable.setFilterMedium(plan.getMediumTags());
        actionTable.setFilterField(plan.getFieldsTags());

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
                    actionTable.setFilterStatus((ObservableList<String>) c.getList());
                    actionTable.filter();
                }
            }
        });
        mediumButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("List Changed: " + c);
                while (c.next()) {
                    actionTable.setFilterMedium((ObservableList<String>) c.getList());
                    actionTable.filter();
                }
            }
        });


        fieldsButton.getSelectedTags().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("List Changed: " + c);
                while (c.next()) {
                    actionTable.setFilterField((ObservableList<String>) c.getList());
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
        gridPane.addColumn(5, vSep2);
        gridPane.addColumn(6, new Label("Zeitbereich"), dateSelector);


        BorderPane borderPane = new BorderPane();
        borderPane.setTop(gridPane);
        borderPane.setCenter(actionTable);

        TableSumPanel tableSumPanel = new TableSumPanel(actionTable.getItems());
        borderPane.setBottom(tableSumPanel);

        actionTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    openDataForm();//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });
        tab.setContent(borderPane);
        //actionTable.setItems(createTestData());

    }

    public void deletePlan() {
        ActionTab tab = getActiveTab();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.action.plan.deletetitle"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.action.plan.delete"));
        Label text = new Label(I18n.getInstance().getString("plugin.action.plan.content") + "\n" + getActiveActionPlan().getName());
        text.setWrapText(true);
        alert.getDialogPane().setContent(text);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                getActiveActionPlan().delete();
                actionPlans.remove(tab.getActionPlan());
                tabPane.getTabs().remove(tab);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    public void createNewPlan() {
        NewActionDialog newActionDialog = new NewActionDialog();
        try {
            NewActionDialog.Response response = newActionDialog.show(JEConfig.getStage(), plugin.getDataSource());
            if (response == NewActionDialog.Response.YES) {

                JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass("Action Plan v2");
                JEVisObject parentDir = newActionDialog.getParent();

                JEVisObject newObject = parentDir.buildObject(newActionDialog.getCreateName(), actionPlanClass);
                newObject.commit();
                ActionPlan actionPlan = new ActionPlan(newObject);
                actionPlans.add(actionPlan);
                tabPane.getSelectionModel().selectLast();

                DateTime now = new DateTime();
                JEVisSample statusAtt = newObject.getAttribute("Custom Status").buildSample(now, "Offen;Geschlosse");
                JEVisSample fieldsAtt = newObject.getAttribute("Custom Fields").buildSample(now, "Büro,Lager,Produktion");
                JEVisSample mediumAtt = newObject.getAttribute("Custom Medium").buildSample(now, "Strom;Gas;Wasser");
                statusAtt.commit();
                fieldsAtt.commit();
                mediumAtt.commit();
            }

        } catch (Exception ex) {
            logger.error(ex);
        }


    }


    public void deleteAction() {
        ActionTab tab = getActiveTab();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.action.action.deletetitle"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.action.action.delete"));
        Label text = new Label(I18n.getInstance().getString("plugin.action.action.content") + "\n" + getSelectedData().nrProperty().get() + " " + getSelectedData().titleProperty().get());
        text.setWrapText(true);
        alert.getDialogPane().setContent(text);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                getSelectedData().delete();
                tab.getActionPlan().removeAction(tab.getActionTable().getSelectionModel().getSelectedItem());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void createNewAction() {
        ActionTab tab = getActiveTab();
        try {
            JEVisClass actionDirClass = getActiveActionPlan().getObject().getDataSource().getJEVisClass("Action Plan Directory v2");
            JEVisClass actionClass = getActiveActionPlan().getObject().getDataSource().getJEVisClass("Action");
            JEVisObject actionDirObj = null;
            if (getActiveActionPlan().getObject().getChildren(actionDirClass, false).isEmpty()) {
                actionDirObj = getActiveActionPlan().getObject().buildObject(actionDirClass.getName(), actionDirClass);
                actionDirObj.commit();
            } else {
                actionDirObj = getActiveActionPlan().getObject().getChildren(actionDirClass, false).get(0);
            }

            JEVisObject actionObject = actionDirObj.buildObject(getActiveActionPlan().getNextActionNr().toString(), actionClass);
            actionObject.commit();
            ActionData newAction = new ActionData(tab.getActionPlan(), actionObject);
            newAction.nrProperty().set(tab.getActionPlan().getNextActionNr());
            tab.getActionPlan().addAction(newAction);

            tab.getActionTable().getSelectionModel().select(newAction);
            openDataForm();//tab.getActionTable().getSelectionModel().getSelectedItem()
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void openPlanSettings() {
        ActionPlanForm actionPlanForm = new ActionPlanForm(getActiveTab().getActionPlan());
        Optional<ButtonType> result = actionPlanForm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            getActiveTab().getActionPlan().commit();
        }

    }

    public void loadActionPlans() {
        try {
            JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass("Action Plan v2");
            List<JEVisObject> planObjs = plugin.getDataSource().getObjects(actionPlanClass, true);

            AtomicBoolean isFirstPlan = new AtomicBoolean(true);
            OverviewTab overviewTab = new OverviewTab();

            tabPane.getTabs().add(overviewTab);

            planObjs.forEach(jeVisObject -> {
                ActionPlan plan = new ActionPlan(jeVisObject);
                actionPlans.add(plan);
                if (isFirstPlan.get()) plan.loadActionList();
                isFirstPlan.set(false);
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Node getContent() {
        return contentPane;
    }

    public ActionTab getActiveTab() {
        ActionTab tab = (ActionTab) tabPane.getSelectionModel().getSelectedItem();
        return tab;
    }

    public ActionPlan getActiveActionPlan() {
        return getActiveTab().getActionPlan();
    }

    public ActionData getSelectedData() {
        ActionTab tab = getActiveTab();
        if (getActiveTab().getActionTable().getSelectionModel().getSelectedItem() != null) {
            return getActiveTab().getActionTable().getSelectionModel().getSelectedItem();
        } else {
            if (tab.getActionTable().getItems().isEmpty()) {
                {
                    return null;
                }
            }

            return tab.getActionTable().getItems().get(0);
        }
    }

    public void openDataForm() {
        ActionData data = getSelectedData();
        ActionForm actionForm = new ActionForm(getActiveActionPlan(), data);

        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.action.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.action.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);


        actionForm.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);


        Optional<ButtonType> optional = actionForm.showAndWait();
        if (optional.get() == buttonTypeOne) {
            data.commit();
        } else {
            data.reload();
        }


    }


}
