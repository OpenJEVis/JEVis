package org.jevis.jeconfig.plugin.action.ui.tab;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.action.data.ActionData;

public class CheckListTab extends Tab {

    Label l_titleDocument = new Label(I18n.getInstance().getString("plugin.action.needdocchange.title"));
    private CheckBox f_isNeedProcessDocument = new CheckBox();
    private CheckBox f_isNeedWorkInstruction = new CheckBox();
    private CheckBox f_isNeedTestInstruction = new CheckBox();
    private CheckBox f_isNeedDrawing = new CheckBox();
    private CheckBox f_isNeedOther = new CheckBox();
    private CheckBox f_IsNeedAdditionalMeters = new CheckBox();
    private CheckBox f_IsAffectsOtherProcess = new CheckBox();
    private CheckBox f_IsConsumptionDocumented = new CheckBox();
    private CheckBox f_isNeedCorrection = new CheckBox();
    private CheckBox f_isNeedAdditionalAction = new CheckBox();
    private CheckBox f_isTargetReached = new CheckBox();
    private ActionData names = new ActionData();
    Label l_isNeedProcessDocument = new Label(names.checkListDataProperty().get().isNeedProcessDocumentProperty().getName());
    Label l_isNeedWorkInstruction = new Label(names.checkListDataProperty().get().isNeedWorkInstructionProperty().getName());
    Label l_isNeedTestInstruction = new Label(names.checkListDataProperty().get().isNeedTestInstructionProperty().getName());
    Label l_isNeedDrawing = new Label(names.checkListDataProperty().get().isNeedDrawingProperty().getName());
    Label l_isNeedOther = new Label(names.checkListDataProperty().get().isNeedOtherProperty().getName());
    Label l_isNeedAdditionalAction = new Label(names.checkListDataProperty().get().isNeedAdditionalActionProperty().getName());
    Label l_isAffectsOtherProcess = new Label(names.checkListDataProperty().get().isAffectsOtherProcessProperty().getName());
    Label l_isTargetReached = new Label(names.checkListDataProperty().get().isTargetReachedProperty().getName());
    Label l_isNeedCorrection = new Label(names.checkListDataProperty().get().isNeedCorrectionProperty().getName());
    Label l_IsNeedAdditionalMeters = new Label(names.checkListDataProperty().get().isNeedAdditionalMetersProperty().getName());

    public CheckListTab(ActionData data) {
        super(I18n.getInstance().getString("actionform.editor.tab.checklist"));

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(gridPane);
        gridPane.setVgap(15);
        gridPane.setHgap(15);


        l_isNeedProcessDocument.setText("Prozessanweisung");
        l_isNeedWorkInstruction.setText("Arbeitsanweisung");
        l_isNeedTestInstruction.setText("Prüfanweisung");
        l_isNeedDrawing.setText("Zeichnungen");
        l_isNeedOther.setText("Sonstige");


        f_IsNeedAdditionalMeters.selectedProperty().bindBidirectional(data.checkListDataProperty().get().isNeedAdditionalMetersProperty());
        f_isNeedAdditionalAction.selectedProperty().bindBidirectional((data.checkListDataProperty().get().isNeedAdditionalActionProperty()));
        f_IsAffectsOtherProcess.selectedProperty().bindBidirectional((data.checkListDataProperty().get().isAffectsOtherProcessProperty()));
        f_isNeedCorrection.selectedProperty().bindBidirectional((data.checkListDataProperty().get().isNeedCorrectionProperty()));
        f_isNeedOther.selectedProperty().bindBidirectional((data.checkListDataProperty().get().isNeedOtherProperty()));
        f_isTargetReached.selectedProperty().bindBidirectional((data.checkListDataProperty().get().isTargetReachedProperty()));
        f_IsConsumptionDocumented.selectedProperty().bindBidirectional((data.checkListDataProperty().get().isConsumptionDocumentedProperty()));

        HBox q1 = new HBox(f_IsNeedAdditionalMeters, l_IsNeedAdditionalMeters);
        HBox q2 = new HBox(f_IsAffectsOtherProcess, l_isAffectsOtherProcess);
        HBox q3 = new HBox(f_isTargetReached, l_isTargetReached);
        HBox q4 = new HBox(f_isNeedCorrection, l_isNeedCorrection);
        HBox q5 = new HBox(f_isNeedAdditionalAction, l_isNeedAdditionalAction);
        HBox qDoc1 = new HBox(f_isNeedProcessDocument, l_isNeedProcessDocument);
        HBox qDoc2 = new HBox(f_isNeedWorkInstruction, l_isNeedWorkInstruction);
        HBox qDoc3 = new HBox(f_isNeedTestInstruction, l_isNeedTestInstruction);
        HBox qDoc4 = new HBox(f_isNeedDrawing, l_isNeedDrawing);
        HBox qDoc5 = new HBox(f_isNeedOther, l_isNeedOther);

        qDoc1.setPadding(new Insets(0, 0, 0, 20));
        qDoc2.setPadding(new Insets(0, 0, 0, 20));
        qDoc3.setPadding(new Insets(0, 0, 0, 20));
        qDoc4.setPadding(new Insets(0, 0, 0, 20));
        qDoc5.setPadding(new Insets(0, 0, 0, 20));

        q1.setPadding(new Insets(0, 0, 0, 20));
        q2.setPadding(new Insets(0, 0, 0, 20));
        q3.setPadding(new Insets(0, 0, 0, 20));
        q4.setPadding(new Insets(0, 0, 0, 20));
        q5.setPadding(new Insets(0, 0, 0, 20));

        int row = 0;

        gridPane.add(new Label(I18n.getInstance().getString("plugin.action.dependencies.title")), 0, ++row, 2, 1);
        gridPane.add(q1, 0, ++row, 2, 1);
        gridPane.add(q2, 0, ++row, 2, 1);
        //gridPane.add(q3, 0, ++row, 2, 1);
        //gridPane.add(q4, 0, ++row, 2, 1);
        gridPane.add(q5, 0, ++row, 2, 1);

        row = 9;
        gridPane.add(l_titleDocument, 0, ++row, 2, 1);
        gridPane.add(qDoc1, 0, ++row, 2, 1);
        gridPane.add(qDoc2, 0, ++row, 2, 1);
        gridPane.add(qDoc3, 0, ++row, 2, 1);
        gridPane.add(qDoc4, 0, ++row, 2, 1);
        gridPane.add(qDoc5, 0, ++row, 2, 1);


        setContent(gridPane);
    }
}
