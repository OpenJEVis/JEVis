package org.jevis.jeconfig.tool;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.jevistree.JEVisTreeItem;
import org.jevis.jeconfig.dialog.DialogHeader;

import java.util.ArrayList;
import java.util.List;

public class AttributeCopy {

    public static final String CONFIG_NAME = "Copy Settings";
    List<JEVisAttribute> selectedAttributes = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(AttributeCopy.class);

    public List<JEVisAttribute> getSelectedAttributes() {
        return selectedAttributes;
    }

    private void doPaste(List<JEVisAttribute> jeVisAttributes, List<JEVisObject> objectsTargets, boolean overwrite) {
        objectsTargets.forEach(object -> {
            logger.debug("- Checking object Object: {}", object);

            try {
                for (JEVisAttribute targetAttribute : object.getAttributes()) {
                    //System.out.println("Attribute zu kopieren: " + targetAttribute.getAllSamples().size());
                    jeVisAttributes.forEach(sourceAttribute -> {
                        if (targetAttribute.getName().equals(sourceAttribute.getName())) {
                            //System.out.println("target attribute: " + sourceAttribute);

                            javafx.concurrent.Task<Object> task = new javafx.concurrent.Task<Object>() {
                                @Override
                                protected Object call() throws Exception {
                                    StringProperty name = new SimpleStringProperty();
                                    try {
                                        logger.debug("Starting copy for: {}", targetAttribute);
                                        //System.out.println("--- Copy attribute: " + sourceAttribute.getName());
                                        name.set("[" + object.getID() + "] " + object + ":" + sourceAttribute.getName());
                                        /**
                                         JEConfig.getStatusBar().startProgressJob("Copy setting"
                                         , DashboardControl.this.widgetList.stream().filter(widget -> !widget.isStatic()).count()
                                         , I18n.getInstance().getString("plugin.dashboard.message.startupdate"));
                                         **/
                                        Platform.runLater(() -> this.updateTitle("Copy setting to " + name.get()));

                                        boolean commit = false;
                                        try {
                                            JEVisUnit inputUnit = sourceAttribute.getInputUnit();
                                            JEVisUnit displayUnit = sourceAttribute.getDisplayUnit();
                                            if (inputUnit != null) {
                                                targetAttribute.setInputUnit(inputUnit);
                                                commit = true;
                                            }
                                            if (displayUnit != null) {
                                                targetAttribute.setDisplayUnit(displayUnit);
                                                commit = true;
                                            }
                                        } catch (Exception e) {
                                            logger.error("Unit copy failed for attribute {}", sourceAttribute.getName(), e);
                                        }

                                        if (commit) {
                                            targetAttribute.commit();
                                        }

                                        if (overwrite) {
                                            logger.debug("Delete samples for: {}", targetAttribute);
                                            targetAttribute.deleteAllSample();
                                        }

                                        if (sourceAttribute.hasSample()) {
                                            targetAttribute.addSamples(sourceAttribute.getAllSamples());
                                            logger.debug("Sample to copy: {}", targetAttribute.getSampleCount());
                                        }
                                    } catch (Exception ex) {
                                        this.failed();
                                        logger.error("Widget update error: [{}]", ex, ex);
                                        ex.printStackTrace();
                                    } finally {
                                        this.done();
                                        JEConfig.getStatusBar().progressProgressJob(CONFIG_NAME, 1
                                                , I18n.getInstance().getString("dialog.attributecopy.finish") + " " + name.get());
                                    }
                                    return null;
                                }
                            };
                            JEConfig.getStatusBar().addTask(CONFIG_NAME, task, JEConfig.getImage("17_Paste_48x48.png"), true);


                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    public void startPaste(List<JEVisAttribute> jeVisAttributes, List<JEVisTreeItem> objectsTargets) {
        JFXDialog jfxDialog = new JFXDialog();
        final JFXButton ok = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.new.ok"));
        ok.setDefaultButton(true);
        final JFXButton cancel = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.new.cancel"));
        cancel.setCancelButton(true);

        GridPane gridLayout = new GridPane();
        gridLayout.setPadding(new Insets(10, 10, 10, 10));
        gridLayout.setVgap(10);

        Node header = DialogHeader.getDialogHeader("paintbrush_1055018.png", I18n.getInstance().getString("dialog.attributecopy.paste"));

        Region spacer = new Region();
        spacer.setMinWidth(20);

        final JFXCheckBox question = new JFXCheckBox(I18n.getInstance().getString("dialog.attributecopy.replace"));
        final JFXCheckBox clearBefore = new JFXCheckBox(I18n.getInstance().getString("dialog.attributecopy.delete"));
        clearBefore.selectedProperty().set(true);
        HBox hBox = new HBox(ok, cancel);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.BOTTOM_RIGHT);

        gridLayout.add(header, 0, 0, 1, 1);
        //gridLayout.add(new Separator(), 0, 1, 1, 1);
        gridLayout.add(question, 0, 3, 1, 1);
        gridLayout.add(clearBefore, 0, 4, 1, 1);
        //gridLayout.add(new Separator(), 5, 1, 1, 1);
        gridLayout.add(hBox, 0, 6, 1, 1);

        ok.setOnAction(event -> {
            List<JEVisObject> objectList = new ArrayList<>();
            List<JEVisObject> childList = new ArrayList<>();
            objectsTargets.forEach(jeVisTreeItem -> {
                objectList.add(jeVisTreeItem.getValue().getJEVisObject());
                if (question.isSelected()) {
                    try {
                        addAllChildren(childList, jeVisTreeItem.getValue().getJEVisObject());
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }
            });
            objectList.addAll(childList);
            doPaste(jeVisAttributes, objectList, clearBefore.isSelected());
            jfxDialog.close();
        });
        cancel.setOnAction(event -> {
            jfxDialog.close();
        });
        jfxDialog.setContent(gridLayout);
        jfxDialog.setDialogContainer(JEConfig.getStackPane());
        jfxDialog.show();


    }

    private void addAllChildren(List<JEVisObject> newList, JEVisObject parent) throws JEVisException {
        parent.getChildren().forEach(object -> {
            try {
                newList.add(object);
                addAllChildren(newList, object);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    public void showAttributeSelection(JEVisObject object) {
        JFXDialog jfxDialog = new JFXDialog();
        final JFXButton ok = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.new.ok"));
        ok.setDefaultButton(true);
        final JFXButton cancel = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.new.cancel"));
        cancel.setCancelButton(true);

        GridPane gridLayout = new GridPane();
        gridLayout.setPadding(new Insets(10, 10, 10, 10));
        gridLayout.setVgap(10);

        Node header = DialogHeader.getDialogHeader("paintbrush_1055018.png", I18n.getInstance().getString("dialog.attributecopy.copy"));
        Label message = new Label(I18n.getInstance().getString("dialog.attributecopy.copyquestion"));

        Region spacer = new Region();
        spacer.setMinWidth(20);

        gridLayout.add(header, 0, 0, 1, 1);
        gridLayout.add(new Separator(), 0, 1, 3, 1);
        gridLayout.add(message, 0, 3, 1, 1);
        gridLayout.add(spacer, 0, 4, 1, 1);

        GridPane listGrid = new GridPane();
        listGrid.setPadding(new Insets(10, 10, 10, 10));
        listGrid.setVgap(6);
        ScrollPane scrollPane = new ScrollPane(listGrid);
        gridLayout.add(scrollPane, 0, 5, 1, 1);


        int row = 0;
        try {

            for (JEVisAttribute jeVisAttribute : object.getAttributes()) {
                try {
                    JFXCheckBox jfxCheckBox = new JFXCheckBox(I18nWS.getInstance().getAttributeName(jeVisAttribute));
                    listGrid.add(jfxCheckBox, 1, ++row, 1, 1);
                    jfxCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            selectedAttributes.add(jeVisAttribute);
                        } else {
                            selectedAttributes.remove(jeVisAttribute);
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        gridLayout.add(new Separator(), 0, 6, 1, 1);
        HBox hBox = new HBox(ok, cancel);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.BOTTOM_RIGHT);
        gridLayout.add(hBox, 0, 7, 1, 1);

        ok.setOnAction(event -> {

            jfxDialog.close();
        });
        cancel.setOnAction(event -> {
            jfxDialog.close();
        });
        jfxDialog.setContent(gridLayout);
        jfxDialog.setDialogContainer(JEConfig.getStackPane());
        jfxDialog.show();
    }


    public void copyAttributes(List<JEVisAttribute> attributeList) {


    }

}
