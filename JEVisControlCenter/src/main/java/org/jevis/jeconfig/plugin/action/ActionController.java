package org.jevis.jeconfig.plugin.action;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanOverviewData;
import org.jevis.jeconfig.plugin.action.ui.ActionForm;
import org.jevis.jeconfig.plugin.action.ui.ActionPlanForm;
import org.jevis.jeconfig.plugin.action.ui.ActionTab;
import org.jevis.jeconfig.plugin.action.ui.NewActionDialog;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ActionController {
    private static final Logger logger = LogManager.getLogger(ActionController.class);
    private final ActionPlugin plugin;
    private final ScrollPane scrollPane = new ScrollPane();
    private final AnchorPane contentPane = new AnchorPane();
    private ObservableList<ActionPlanData> actionPlans;
    private ObservableList<ActionPlanData> actionPlansFilters;

    private ObservableList<String> actionPlanNames;
    private TabPane tabPane = new TabPane();
    private BooleanProperty isOverviewTab = new SimpleBooleanProperty(true);


    public ActionController(ActionPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadActionView() {

        actionPlans = FXCollections.observableArrayList();
        actionPlanNames = FXCollections.observableArrayList();
        actionPlansFilters = FXCollections.observableArrayList();


        actionPlans.addListener(new ListChangeListener<ActionPlanData>() {
            @Override
            public void onChanged(Change<? extends ActionPlanData> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(actionPlan -> {
                            buildTabPane(actionPlan);
                            actionPlanNames.add(actionPlan.getName().get());
                        });

                    }
                }
            }
        });


        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println();
            isOverviewTab.set(getActiveActionPlan() instanceof ActionPlanOverviewData);
            System.out.println("Tab is Overview: " + isOverviewTab.get());
        });


        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);


    }


    private void buildTabPane(ActionPlanData plan) {
        //ActionTable actionTable = new ActionTable(plan, plan.getActionData());
        //actionTable.enableSumRow(true);
        ActionTab tab = new ActionTab(this, plan);
        tab.setClosable(false);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue instanceof ActionTab) {
                ActionPlanData actionPlan = ((ActionTab) newValue).getActionPlan();
                //actionPlan.loadActionList();
            }
        });

    }

    public ObservableList<ActionPlanData> getActionPlans() {
        return actionPlans;
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
                ActionPlanData actionPlan = new ActionPlanData(newObject);

                actionPlan.setDefaultValues(Locale.GERMANY);//For now only German is Supportet
                actionPlan.commit();
                actionPlans.add(actionPlan);
                tabPane.getSelectionModel().selectLast();

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
                tab.getActionTable().filter();
                //tab.getActionPlan().removeAction(tab.getActionTable().getSelectionModel().getSelectedItem());
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

            int nextNr = getActiveActionPlan().getNextActionNr();
            JEVisObject actionObject = actionDirObj.buildObject(nextNr + "", actionClass);
            actionObject.commit();
            ActionData newAction = new ActionData(tab.getActionPlan(), actionObject);
            newAction.nrProperty().set(nextNr);
            newAction.fromUser.set(actionDirObj.getDataSource().getCurrentUser().getAccountName());
            newAction.commit();
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

            //AtomicBoolean isFirstPlan = new AtomicBoolean(true);


            ActionPlanOverviewData overviewData = new ActionPlanOverviewData(this);
            ActionTab overviewTab = new ActionTab(this, overviewData);
            tabPane.getTabs().add(0, overviewTab);

            planObjs.forEach(jeVisObject -> {
                ActionPlanData plan = new ActionPlanData(jeVisObject);
                plan.loadActionList();
                actionPlans.add(plan);


                /* the new Overview need all data ready :(
                if (isFirstPlan.get()) plan.loadActionList();
                isFirstPlan.set(false);
                 */
            });
            overviewData.updateData();

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

    public ActionPlanData getActiveActionPlan() {
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
            data.setNew(false);
            data.commit();
        } else {
            if (data.isNew()) {
                data.getActionPlan().removeAction(data);
            } else {
                try {
                    getActiveActionPlan().reloadAction(data);
                } catch (Exception ex) {
                    logger.error(ex, ex);
                }
            }

        }


    }

    public boolean isIsOverviewTab() {
        return isOverviewTab.get();
    }

    public BooleanProperty isOverviewTabProperty() {
        return isOverviewTab;
    }
}
