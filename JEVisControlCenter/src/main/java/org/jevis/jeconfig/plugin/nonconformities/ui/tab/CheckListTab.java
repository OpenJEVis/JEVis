package org.jevis.jeconfig.plugin.nonconformities.ui.tab;

import com.jfoenix.controls.JFXCheckBox;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;

import java.util.ArrayList;
import java.util.List;

public class CheckListTab extends javafx.scene.control.Tab implements Tab {
    private JFXCheckBox f_ImmediateActionRequired = new JFXCheckBox();
    private JFXCheckBox f_EffectOnOngoingProcesses = new JFXCheckBox();
    private JFXCheckBox f_RoutinelyAffected = new JFXCheckBox();
    private JFXCheckBox f_EmployeeTrained = new JFXCheckBox();
    private JFXCheckBox f_SupplierChangeGoodsNeeded = new JFXCheckBox();
    private JFXCheckBox f_ManagementNotificationNeeded = new JFXCheckBox();
    private JFXCheckBox f_DocumentsChangesNeeded = new JFXCheckBox();

    private JFXCheckBox f_ProcessInstructions = new JFXCheckBox();
    private JFXCheckBox f_WorkInstructions = new JFXCheckBox();
    private JFXCheckBox f_TestInstructions = new JFXCheckBox();
    private JFXCheckBox f_Design = new JFXCheckBox();
    private JFXCheckBox f_Model = new JFXCheckBox();
    private JFXCheckBox f_Miscellaneous = new JFXCheckBox();

    private Label l_ImmediateActionRequired = new Label();
    private Label l_EffectOnOngoingProcesses = new Label();
    private Label l_RoutinelyAffected = new Label();
    private Label l_EmployeeTrained = new Label();
    private Label l_SupplierChangeGoodsNeeded = new Label();
    private Label l_ManagementNotificationNeeded = new Label();
    private Label l_DocumentsChangesNeeded = new Label();
    private Label l_Documents = new Label();

    private Label l_ProcessInstructions = new Label();
    private Label l_WorkInstructions = new Label();
    private Label l_TestInstructions = new Label();
    private Label l_Design = new Label();
    private Label l_Model = new Label();
    private Label l_Miscellaneous = new Label();

    GridPane gridPane = new GridPane();


    public CheckListTab() {
    }

    public CheckListTab(String s) {
        super(s);
    }

    public CheckListTab(String s, Node node) {
        super(s, node);
    }


    @Override
    public void initTab(NonconformityData data) {

        this.setClosable(false);

        gridPane.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(gridPane);

        gridPane.setVgap(15);
        gridPane.setHgap(15);



        add(gridPane,0,0,2,1,Priority.NEVER,new Label(I18n.getInstance().getString("plugin.nonconforrmities.form.tab.checklist.sideeffect")));

        add(gridPane, 0, 1, 1, 1, Priority.NEVER, f_ImmediateActionRequired);
        add(gridPane, 0, 2, 1, 1, Priority.NEVER, f_EffectOnOngoingProcesses);
        add(gridPane, 0, 3, 1, 1, Priority.NEVER, f_RoutinelyAffected);
        add(gridPane, 0, 4, 1, 1, Priority.NEVER, f_EmployeeTrained);
        add(gridPane, 0, 5, 1, 1, Priority.NEVER, f_SupplierChangeGoodsNeeded);
        add(gridPane, 0, 6, 1, 1, Priority.NEVER, f_ManagementNotificationNeeded);
        add(gridPane, 0, 7, 1, 1, Priority.NEVER, f_DocumentsChangesNeeded);
        //add(gridPane, 0, 7, 1, 1, Priority.NEVER, f_DocumentsChangesNeeded);

        add(gridPane, 1, 1, 1, 1, Priority.NEVER, l_ImmediateActionRequired);
        add(gridPane, 1, 2, 1, 1, Priority.NEVER, l_EffectOnOngoingProcesses);
        add(gridPane, 1, 3, 1, 1, Priority.NEVER, l_RoutinelyAffected);
        add(gridPane, 1, 4, 1, 1, Priority.NEVER, l_EmployeeTrained);
        add(gridPane, 1, 5, 1, 1, Priority.NEVER, l_SupplierChangeGoodsNeeded);
        add(gridPane, 1, 6, 1, 1, Priority.NEVER, l_ManagementNotificationNeeded);
        add(gridPane, 1, 7, 1, 1, Priority.NEVER, l_DocumentsChangesNeeded);
        //add(gridPane, 1, 7, 2, 1, Priority.SOMETIMES, l_Documents);

        add(gridPane,0,8,2,1,Priority.NEVER,new Label(I18n.getInstance().getString("plugin.nonconforrmities.form.tab.checklist.documentschanged")));

        add(gridPane, 0, 9, 1, 1, Priority.NEVER, f_ProcessInstructions);
        add(gridPane, 0, 10, 1, 1, Priority.NEVER, f_WorkInstructions);
        add(gridPane, 0, 11, 1, 1, Priority.NEVER, f_TestInstructions);
        add(gridPane, 0, 12, 1, 1, Priority.NEVER, f_Design);
        add(gridPane, 0, 13, 1, 1, Priority.NEVER, f_Model);
        add(gridPane, 0, 14, 1, 1, Priority.NEVER, f_Miscellaneous);

        add(gridPane, 1, 9, 1, 1, Priority.NEVER, l_ProcessInstructions);
        add(gridPane, 1, 10, 1, 1, Priority.NEVER, l_WorkInstructions);
        add(gridPane, 1, 11, 1, 1, Priority.NEVER, l_TestInstructions);
        add(gridPane, 1, 12, 1, 1, Priority.NEVER, l_Design);
        add(gridPane, 1, 13, 1, 1, Priority.NEVER, l_Model);
        add(gridPane, 1, 14, 1, 1, Priority.NEVER, l_Miscellaneous);

        addListener();


        this.setContent(scrollPane);
    }

    @Override
    public void updateView(NonconformityData data) {
        NonconformityData fake = new NonconformityData();


        f_ImmediateActionRequired.selectedProperty().bindBidirectional(data.isImmediateActionRequiredProperty());
        f_EffectOnOngoingProcesses.selectedProperty().bindBidirectional(data.isEffectOnOngoingProcessesProperty());
        f_RoutinelyAffected.selectedProperty().bindBidirectional(data.isRoutinelyAffectedProperty());
        f_EmployeeTrained.selectedProperty().bindBidirectional(data.isEmployeeTrainedProperty());
        f_SupplierChangeGoodsNeeded.selectedProperty().bindBidirectional(data.isSupplierChangeGoodsNeededProperty());
        f_ManagementNotificationNeeded.selectedProperty().bindBidirectional(data.isManagementNotificationNeededProperty());
        f_DocumentsChangesNeeded.selectedProperty().bindBidirectional(data.isDocumentsChangesNeededProperty());
        f_ProcessInstructions.selectedProperty().bindBidirectional(data.getCheckListData().isProcessInstructionsProperty());
        f_WorkInstructions.selectedProperty().bindBidirectional(data.getCheckListData().isWorkInstructionsProperty());
        f_TestInstructions.selectedProperty().bindBidirectional(data.getCheckListData().isTestInstructionsProperty());
        f_Design.selectedProperty().bindBidirectional(data.getCheckListData().isDesignProperty());
        f_Model.selectedProperty().bindBidirectional(data.getCheckListData().isModelProperty());
        f_Miscellaneous.selectedProperty().bindBidirectional(data.getCheckListData().isMiscellaneousProperty());

        l_ImmediateActionRequired.setText(fake.isImmediateActionRequiredProperty().getName());
        l_EffectOnOngoingProcesses.setText(fake.isEffectOnOngoingProcessesProperty().getName());
        l_RoutinelyAffected.setText(fake.isRoutinelyAffectedProperty().getName());
        l_EmployeeTrained.setText(fake.isEmployeeTrainedProperty().getName());
        l_SupplierChangeGoodsNeeded.setText(fake.isSupplierChangeGoodsNeededProperty().getName());
        l_ManagementNotificationNeeded.setText(fake.isManagementNotificationNeededProperty().getName());
        l_DocumentsChangesNeeded.setText(fake.isDocumentsChangesNeededProperty().getName());
        l_Documents.setText(fake.isDocumentsChangesNeededProperty().getName());
        l_ProcessInstructions.setText(fake.getCheckListData().isProcessInstructionsProperty().getName());
        l_WorkInstructions.setText(fake.getCheckListData().isWorkInstructionsProperty().getName());
        l_TestInstructions.setText(fake.getCheckListData().isTestInstructionsProperty().getName());
        l_Design.setText(fake.getCheckListData().isDesignProperty().getName());
        l_Model.setText(fake.getCheckListData().isModelProperty().getName());
        l_Miscellaneous.setText(fake.getCheckListData().isMiscellaneousProperty().getName());




    }

    private void addListener() {
        if (!f_DocumentsChangesNeeded.isSelected()) {
            disableDocumentsCheckboxes(true);
        }
        f_DocumentsChangesNeeded.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                disableDocumentsCheckboxes(false);
            } else {
                disableDocumentsCheckboxes(true);
            }
        });


    }

    private void disableDocumentsCheckboxes(boolean disable) {


        for (int i = 8; i <= 14; i++) {
            getNodesByRow(i, gridPane).forEach(node -> {
                if (disable) {
                    if (node.getClass().equals(JFXCheckBox.class)) {
                        JFXCheckBox checkbox = (JFXCheckBox) node;
                        checkbox.setSelected(false);
                    }

                    node.setVisible(false);
                    node.setDisable(true);
                }else {
                    node.setVisible(true);
                    node.setDisable(false);
                }

            });
        }
    }

    public List<Node> getNodesByRow(final int row, GridPane gridPane) {
        ObservableList<Node> childrens = gridPane.getChildren();

        List<Node> resulNodes = new ArrayList<>();
        for (Node node : childrens) {
            if(gridPane.getRowIndex(node) == row) {
                resulNodes.add(node);
            }
        }

        return resulNodes;
    }
}
