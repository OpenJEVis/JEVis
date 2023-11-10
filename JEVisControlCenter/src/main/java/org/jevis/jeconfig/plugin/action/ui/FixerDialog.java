package org.jevis.jeconfig.plugin.action.ui;

import org.jevis.jeconfig.plugin.action.data.ActionPlanData;

/**
 * Use case specific data fixer. Can be deleted in the future.
 */
public class FixerDialog {



    /*
    final ContextMenu contextMenu = new ContextMenu();
    MenuItem item1 = new MenuItem("Fix Title");
    MenuItem item2 = new MenuItem("Fix NPV");
    MenuItem item3 = new MenuItem("Commit all");

        item1.setOnAction(actionEvent -> {
        FixerDialog fixer = new FixerDialog();
        fixer.fixTitel(plan);
    });

        item2.setOnAction(actionEvent -> {
        FixerDialog fixer = new FixerDialog();
        fixer.fixNPV(plan);
    });

        item3.setOnAction(actionEvent -> {
        FixerDialog fixer = new FixerDialog();
        fixer.commitAll(plan);
    });

    public void commitAll(ActionPlanData planData) {
        planData.getActionData().forEach(actionData -> {
            try {
                System.out.println("Commit: " + actionData.nr.get());
                actionData.commit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

    }
     if (JEConfig.getExpert()) {
            contextMenu.getItems().addAll(item1, item2, item3);
            this.setContextMenu(contextMenu);
        }
     */

    public void fixTitel(ActionPlanData planData) {

        planData.getActionData().forEach(actionData -> {
            try {
                String oldTitle = actionData.title.get();
                String oldMaßnahme = actionData.noteEnergieflussProperty().get();
                String oldDescription = actionData.desciptionProperty().get();//Prozess

                System.out.println("Nr: " + actionData.nr.get() + " oldTitle: " + oldTitle + "  oldMaßnahme: " + oldMaßnahme);
                actionData.desciptionProperty().set(oldTitle);
                actionData.title.set(oldMaßnahme);
                //actionData.commit();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


    }

    public void fixNPV(ActionPlanData planData) {
        planData.getActionData().forEach(actionData -> {
            int amountYear = actionData.npv.get().amoutYear.get();
            int overYears = actionData.npv.get().overXYear.get();

            actionData.npv.get().amoutYear.set(10);
            actionData.npv.get().overXYear.set(3);
            //actionData.commit();

        });
    }

}
