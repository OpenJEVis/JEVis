package org.jevis.jecc.plugin.action.ui;

import io.github.palexdev.materialfx.controls.MFXCheckbox;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import jfxtras.scene.layout.GridPane;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.plugin.action.data.ActionPlanData;

import java.util.ArrayList;
import java.util.List;

public class ExportDialog extends Dialog {
    List<Selection> userSelection = new ArrayList<>();

    public ExportDialog(ObservableList<ActionPlanData> actionPlanData) {
        super();
        this.initOwner(ControlCenter.getStage());

        setTitle(I18n.getInstance().getString("action.export.title"));
        setHeaderText("Select Actions to Export");
        setResizable(true);

        jfxtras.scene.layout.GridPane gridPane = new jfxtras.scene.layout.GridPane();
        gridPane.setVgap(8);
        gridPane.setHgap(8);

        Label headPlanLabel = new Label("Tabelle");
        Label headPDeteilsLabel = new Label("Deteils");

        gridPane.add(new Region(), new GridPane.C().col(0).row(0).halignment(HPos.LEFT));
        gridPane.add(headPlanLabel, new GridPane.C().col(1).row(0).halignment(HPos.LEFT));
        gridPane.add(headPDeteilsLabel, new GridPane.C().col(2).row(0).halignment(HPos.LEFT));


        int row = 0;
        for (ActionPlanData actionPlan : actionPlanData) {
            row++;
            Label planName = new Label(actionPlan.getName().get());
            Selection selection = new Selection(actionPlan);
            userSelection.add(selection);

            MFXCheckbox planCheck = new MFXCheckbox();
            MFXCheckbox actionCheck = new MFXCheckbox();

            actionCheck.setOnAction(event -> selection.setExportDetail(actionCheck.isSelected()));
            planCheck.setOnAction(event -> {
                selection.setExportPlan(planCheck.isSelected());
                selection.setExportDetail(planCheck.isSelected());
                actionCheck.setSelected(planCheck.isSelected());
            });


            planCheck.setSelected(true);
            actionCheck.setSelected(true);

            gridPane.add(planName, new GridPane.C().col(0).row(row).halignment(HPos.LEFT));
            gridPane.add(planCheck, new GridPane.C().col(1).row(row).halignment(HPos.LEFT));
            gridPane.add(actionCheck, new GridPane.C().col(2).row(row).halignment(HPos.LEFT));

        }


        getDialogPane().setContent(gridPane);

    }

    public List<Selection> getSelection() {
        return userSelection;
    }


    public class Selection {

        ActionPlanData plan = null;
        boolean exportPlan = true;
        boolean exportDetail = true;

        public Selection(ActionPlanData plan) {
            this.plan = plan;
        }

        public void setExportPlan(boolean exportPlan) {
            this.exportPlan = exportPlan;
        }

        public void setExportDetail(boolean exportDetail) {
            this.exportDetail = exportDetail;
        }
    }
}
