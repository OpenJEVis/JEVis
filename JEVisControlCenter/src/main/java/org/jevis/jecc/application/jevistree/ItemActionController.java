package org.jevis.jecc.application.jevistree;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;

public class ItemActionController {

    private static final Logger logger = LogManager.getLogger(ItemActionController.class);
    final JEVisTree jeVisTree;
    private BooleanProperty cutEnabledProperty = new SimpleBooleanProperty(false);
    private BooleanProperty pasteEnabledProperty = new SimpleBooleanProperty(false);
    private BooleanProperty copyEnabledProperty = new SimpleBooleanProperty(false);
    private BooleanProperty copyAttEnabledProperty = new SimpleBooleanProperty(false);
    private BooleanProperty recalcEnabledProperty = new SimpleBooleanProperty(false);
    private BooleanProperty kpiWizardEnabledProperty = new SimpleBooleanProperty(false);
    private BooleanProperty manualValueEnabledProperty = new SimpleBooleanProperty(false);
    private BooleanProperty gotoSourceEnabledProperty = new SimpleBooleanProperty(false);
    private BooleanProperty deleteEnabledProperty = new SimpleBooleanProperty(false);
    private BooleanProperty createEnabledProperty = new SimpleBooleanProperty(false);

    public ItemActionController(JEVisTree jeVisTree) {
        this.jeVisTree = jeVisTree;

        jeVisTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            copyAttEnabledProperty.setValue(false);
            kpiWizardEnabledProperty.setValue(false);
            manualValueEnabledProperty.set(false);
            gotoSourceEnabledProperty.set(false);
            recalcEnabledProperty.set(false);
            deleteEnabledProperty.set(false);

            if (jeVisTree.getSelectionModel().getSelectedItems().size() <= 0) {
                return;
            }
            if (jeVisTree.getSelectionModel().getSelectedItems().size() > 1) {
                pasteEnabledProperty.setValue(false);
            }
            if (jeVisTree.getSelectionModel().getSelectedItems().size() == 1) {
                pasteEnabledProperty.setValue(!jeVisTree.getCopyObjects().isEmpty());
                copyAttEnabledProperty.setValue(true);

                try {
                    JEVisObject obj = ((TreeItem<JEVisTreeRow>) jeVisTree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
                    if (obj.getJEVisClassName().equals("Data Directory")) {
                        kpiWizardEnabledProperty.set(true);
                    } else if (obj.getAttribute("Value") != null) {
                        manualValueEnabledProperty.setValue(true);
                    }

                    if (obj.getJEVisClassName().equals("Data")
                            || obj.getJEVisClassName().equals("Data")) {
                        gotoSourceEnabledProperty.set(true);
                    }

                    createEnabledProperty.setValue(obj.getDataSource().getCurrentUser().canCreate(obj.getID()));


                } catch (Exception ex) {

                }
            }

            boolean hasNonCalcObjects = false;
            boolean canDelete = true;
            for (Object o : jeVisTree.getSelectionModel().getSelectedItems()) {
                try {
                    JEVisObject obj = ((TreeItem<JEVisTreeRow>) o).getValue().getJEVisObject();
                    if (obj.getJEVisClassName().equals("Clean Data")
                            || obj.getJEVisClassName().equals("Base Data")
                            || obj.getJEVisClassName().equals("Data")
                            || obj.getJEVisClassName().equals("Calculation")) {
                    } else {
                        hasNonCalcObjects = true;
                    }

                    if (obj.getJEVisClassName().equals("Data Directory")
                            || obj.getJEVisClassName().equals("Base Data")
                            || obj.getJEVisClassName().equals("Data")
                            || obj.getJEVisClassName().equals("Calculation")) {
                    } else {
                        hasNonCalcObjects = true;
                    }

                    if (obj.getID() < 0 || !obj.getDataSource().getCurrentUser().canDelete(obj.getID())) {
                        canDelete = false;
                    }

                } catch (Exception ex) {
                    logger.error("Error while checking TreeObject: {}", o);
                }

            }
            recalcEnabledProperty.setValue(!hasNonCalcObjects);
            deleteEnabledProperty.set(canDelete);

            //TODO:
            // if copyObject is same as past

        });
    }


    public BooleanProperty deleteEnabledPropertyProperty() {
        return deleteEnabledProperty;
    }

    public BooleanProperty createEnabledPropertyProperty() {
        return createEnabledProperty;
    }

    public BooleanProperty gotoSourceEnabledPropertyProperty() {
        return gotoSourceEnabledProperty;
    }

    public BooleanProperty copyAttEnabledPropertyProperty() {
        return copyAttEnabledProperty;
    }


    public BooleanProperty kpiWizardEnabledPropertyProperty() {
        return kpiWizardEnabledProperty;
    }

    public boolean isManualValueEnabledProperty() {
        return manualValueEnabledProperty.get();
    }

    public BooleanProperty manualValueEnabledPropertyProperty() {
        return manualValueEnabledProperty;
    }

    public BooleanProperty recalcEnabledPropertyProperty() {
        return recalcEnabledProperty;
    }

    public BooleanProperty cutEnabledPropertyProperty() {
        return cutEnabledProperty;
    }


    public BooleanProperty pasteEnabledPropertyProperty() {
        return pasteEnabledProperty;
    }


    public BooleanProperty copyEnabledPropertyProperty() {
        return copyEnabledProperty;
    }
}
