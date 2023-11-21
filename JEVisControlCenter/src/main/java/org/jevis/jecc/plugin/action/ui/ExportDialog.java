package org.jevis.jecc.plugin.action.ui;

import io.github.palexdev.materialfx.controls.MFXCheckbox;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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
        setHeaderText(I18n.getInstance().getString("plugin.action.export.message"));
        setResizable(true);
        setHeight(200);

        jfxtras.scene.layout.GridPane gridPane = new jfxtras.scene.layout.GridPane();
        gridPane.setVgap(8);
        gridPane.setHgap(15);

        Label headPlanLabel = new Label();//I18n.getInstance().getString("plugin.action.name"));
        Label headTableLabel = new Label(I18n.getInstance().getString("plugin.action.table"));
        Label headPDeteilsLabel = new Label(I18n.getInstance().getString("plugin.action.export.actions"));
        headPlanLabel.setStyle("-fx-font-weight: bold");
        headTableLabel.setStyle("-fx-font-weight: bold");
        headPDeteilsLabel.setStyle("-fx-font-weight: bold");


        gridPane.add(headPlanLabel, new GridPane.C().col(0).row(0).halignment(HPos.LEFT));
        gridPane.add(headTableLabel, new GridPane.C().col(1).row(0).halignment(HPos.CENTER));
        gridPane.add(headPDeteilsLabel, new GridPane.C().col(2).row(0).halignment(HPos.CENTER));
        gridPane.add(new Separator(Orientation.HORIZONTAL), new GridPane.C().col(0).row(1).halignment(HPos.CENTER).colSpan(3));


        int row = 1;
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
            });

            actionCheck.setOnAction(event -> {
                selection.setExportDetail(actionCheck.isSelected());
            });


            planCheck.setSelected(true);
            actionCheck.setSelected(true);

            gridPane.add(planName, new GridPane.C().col(0).row(row).halignment(HPos.LEFT));
            gridPane.add(planCheck, new GridPane.C().col(1).row(row).halignment(HPos.CENTER));
            gridPane.add(actionCheck, new GridPane.C().col(2).row(row).halignment(HPos.CENTER));

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

        ActionTable table = null;

        public Selection(ActionPlanData plan) {
            this.plan = plan;
        }

        public void setExportPlan(boolean exportPlan) {
            this.exportPlan = exportPlan;
        }

        public void setExportDetail(boolean exportDetail) {
            this.exportDetail = exportDetail;
        }

        public ActionTable getTable() {
            return table;
        }

        public void setTable(ActionTable table) {
            this.table = table;
        }
    }
}
