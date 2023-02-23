package org.jevis.jeconfig.plugin.nonconformities.ui;

import javafx.scene.control.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;
import org.jevis.jeconfig.plugin.nonconformities.data.Nonconformities;
import org.jevis.jeconfig.plugin.nonconformities.ui.tab.AttachmentTab;
import org.jevis.jeconfig.plugin.nonconformities.ui.tab.CheckListTab;
import org.jevis.jeconfig.plugin.nonconformities.ui.tab.GeneralTab;
import org.jevis.jeconfig.tool.ScreenSize;

public class NonconformityForm extends Dialog {

    public NonconformityForm() {
    }

    private TabPane tabPane = new TabPane();


    private GeneralTab basicTab = new GeneralTab(I18n.getInstance().getString("plugin.nonconforrmities.form.tab.general"));
   // private Tab detailTab = new Tab(I18n.getInstance().getString("actionform.editor.tab.deteils"));
    private CheckListTab checkListTab = new CheckListTab(I18n.getInstance().getString("plugin.nonconforrmities.form.tab.checklist"));

    private AttachmentTab attachmentTab = new AttachmentTab(I18n.getInstance().getString("plugin.nonconforrmities.form.tab.attachment"));

    private Nonconformities nonconformities;



    public NonconformityForm(Nonconformities nonconformities) {
        super();
        this.initOwner(JEConfig.getStage());
        this.nonconformities = nonconformities;


        setTitle(I18n.getInstance().getString("actionform.editor.title"));
        setHeaderText(null);
        setResizable(true);
        this.getDialogPane().setPrefWidth(ScreenSize.fitScreenWidth(1000));
        this.getDialogPane().setPrefHeight(ScreenSize.fitScreenHeight(950));

        widthProperty().addListener((observable, oldValue, newValue) -> tabPane.setPrefWidth(newValue.doubleValue() - 50));


        tabPane.setPrefWidth(ScreenSize.fitScreenWidth(1050));

        






        basicTab.setClosable(false);

        attachmentTab.setClosable(false);


        tabPane.getTabs().addAll(basicTab, checkListTab,attachmentTab);
        getDialogPane().setContent(tabPane);
    }

    public void setData(NonconformityData data) {
        updateView(data);
        checkListTab.initTab(data);
        attachmentTab.initTab(data);
        basicTab.initTab(data);



    }












    private void updateView(NonconformityData data) {

        checkListTab.updateView(data);
        basicTab.updateView(data);
        attachmentTab.updateView(data);

    }


}
