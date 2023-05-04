package org.jevis.jeconfig.plugin.legal.ui;

import javafx.scene.control.Dialog;
import javafx.scene.control.TabPane;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.legal.data.LegalCadastre;
import org.jevis.jeconfig.plugin.legal.data.LegislationData;

import org.jevis.jeconfig.plugin.legal.ui.tab.AttachmentTab;
import org.jevis.jeconfig.plugin.legal.ui.tab.GeneralTab;
import org.jevis.jeconfig.tool.ScreenSize;

public class LegislationForm extends Dialog {

    private boolean isNew;

    public LegislationForm() {
    }


    public TabPane tabPane = new TabPane();


    private GeneralTab basicTab = new GeneralTab(I18n.getInstance().getString("plugin.nonconformities.form.tab.general"));

    private AttachmentTab attachmentTab = new AttachmentTab(I18n.getInstance().getString("plugin.nonconformities.form.tab.attachment"));

    private LegalCadastre legalCadastre;


    public LegislationForm(LegalCadastre legalCadastre) {

        super();
        this.initOwner(JEConfig.getStage());
        this.legalCadastre = legalCadastre;


        setTitle(I18n.getInstance().getString("plugin.Legalcadastre.legislation.dialog.title"));
        setHeaderText(null);
        setResizable(true);
        this.getDialogPane().setPrefWidth(ScreenSize.fitScreenWidth(1000));
        this.getDialogPane().setPrefHeight(ScreenSize.fitScreenHeight(950));

        widthProperty().addListener((observable, oldValue, newValue) -> tabPane.setPrefWidth(newValue.doubleValue() - 50));


        tabPane.setPrefWidth(ScreenSize.fitScreenWidth(1050));


        basicTab.setClosable(false);

        attachmentTab.setClosable(false);


        tabPane.getTabs().addAll(basicTab, attachmentTab);
        getDialogPane().setContent(tabPane);
    }

    public void setData(LegislationData data) {
        updateView(data);
        basicTab.initTab(data);
        attachmentTab.initTab(data);
    }


    private void updateView(LegislationData data) {

        basicTab.updateView(data);
        attachmentTab.updateView(data);

    }



    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
