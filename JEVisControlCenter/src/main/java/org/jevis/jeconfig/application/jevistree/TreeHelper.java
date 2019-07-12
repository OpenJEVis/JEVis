/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.application.jevistree;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.CommonClasses;
import org.jevis.commons.CommonObjectTasks;
import org.jevis.commons.export.ExportMaster;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.report.ReportLink;
import org.jevis.commons.utils.ObjectHelper;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.jevis.jeconfig.dialog.*;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ToggleSwitchPlus;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Collection of common JEVisTree operations
 *
 * @author florian.simon@envidatec.com
 */
public class TreeHelper {

    private static final Logger logger = LogManager.getLogger(TreeHelper.class);

    private static long lastSearchIndex = 0L;
    private static String lastSearch = "";

    /**
     * TODO: make it like the other function where the object is an parameter
     *
     * @param tree
     */
    public static void EventDelete(JEVisTree tree) {
        logger.debug("EventDelete");
        if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
            String question = I18n.getInstance().getString("jevistree.dialog.delete.message");
            ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();
            for (TreeItem<JEVisTreeRow> item : items) {
                question += item.getValue().getJEVisObject().getName();
            }
            question += "?";

            try {
                if (items.get(0).getValue().getJEVisObject().getDataSource().getCurrentUser().canDelete(items.get(0).getValue().getJEVisObject().getID())) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.delete.title"));
                    alert.setHeaderText(null);
                    alert.setContentText(question);

                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("plugin.object.waitsave"));

                            Task<Void> delete = new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    try {
                                        for (TreeItem<JEVisTreeRow> item : items) {
                                            Long id = item.getValue().getJEVisObject().getID();
                                            item.getValue().getJEVisObject().getDataSource().deleteObject(id);
                                            if (item.getParent() != null) {
                                                item.getParent().getChildren().remove(item);
                                            }
                                        }

                                    } catch (Exception ex) {
                                        logger.catching(ex);
                                        CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                                I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                                    }
                                    return null;
                                }
                            };
                            delete.setOnSucceeded(event -> pForm.getDialogStage().close());

                            delete.setOnCancelled(event -> {
                                logger.error(I18n.getInstance().getString("plugin.object.waitsave.canceled"));
                                pForm.getDialogStage().hide();
                            });

                            delete.setOnFailed(event -> {
                                logger.error(I18n.getInstance().getString("plugin.object.waitsave.failed"));
                                pForm.getDialogStage().hide();
                            });

                            pForm.activateProgressBar(delete);
                            pForm.getDialogStage().show();

                            new Thread(delete).start();

                        } else {
                            // ... user chose CANCEL or closed the dialog
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        alert1.showAndWait();
                    });
                }
            } catch (JEVisException e) {
                logger.error("Could not get JEVis data source. ", e);
            }
        }
    }

    public static void EventDeleteAllCleanAndRaw(JEVisTree tree) {
        logger.debug("EventDeleteAllCleanAndRaw");
        try {
            if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
                String question = I18n.getInstance().getString("jevistree.dialog.delete.message");
                ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();
                for (TreeItem<JEVisTreeRow> item : items) {
                    if (items.indexOf(item) > 0 && items.indexOf(item) < items.size() - 1)
                        question += item.getValue().getJEVisObject().getName();
                }
                question += "?";

                if (tree.getJEVisDataSource().getCurrentUser().canWrite(items.get(0).getValue().getJEVisObject().getID())) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.deleteCleanAndRaw.title"));
                    alert.setHeaderText(null);
                    alert.setContentText(question);
                    GridPane gp = new GridPane();
                    gp.setHgap(4);
                    gp.setVgap(6);
                    Label cleanDataLabel = new Label("Clean Data");
                    ToggleSwitchPlus cleanData = new ToggleSwitchPlus();
                    cleanData.setSelected(true);
                    Label rawDataLabel = new Label("Raw Data");
                    ToggleSwitchPlus rawData = new ToggleSwitchPlus();
                    rawData.setSelected(false);

                    gp.add(rawDataLabel, 0, 0);
                    gp.add(rawData, 1, 0);
                    gp.add(cleanDataLabel, 0, 1);
                    gp.add(cleanData, 1, 1);

                    alert.getDialogPane().setContent(gp);

                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            try {

                                final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.dialog.deleteCleanAndRaw.title") + "...");

                                Task<Void> reload = new Task<Void>() {
                                    @Override
                                    protected Void call() {
                                        for (TreeItem<JEVisTreeRow> item : items) {
                                            deleteAllSamples(item.getValue().getJEVisObject(), rawData.selectedProperty().get(), cleanData.selectedProperty().get());
                                        }

                                        return null;
                                    }
                                };
                                reload.setOnSucceeded(event -> pForm.getDialogStage().close());

                                reload.setOnCancelled(event -> {
                                    logger.debug("Delete all samples Cancelled");
                                    pForm.getDialogStage().hide();
                                });

                                reload.setOnFailed(event -> {
                                    logger.debug("Delete all samples failed");
                                    pForm.getDialogStage().hide();
                                });

                                pForm.activateProgressBar(reload);
                                pForm.getDialogStage().show();

                                new Thread(reload).start();

                            } catch (Exception ex) {
                                logger.catching(ex);
                                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                        I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                            }
                        } else {
                            // ... user chose CANCEL or closed the dialog
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        alert1.showAndWait();
                    });

                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get JEVis data source.", e);
        }
    }

    private static void deleteAllSamples(JEVisObject object, boolean rawData, boolean cleanData) {
        try {
            JEVisAttribute value = object.getAttribute("Value");
            if (value != null) {
                if ((object.getJEVisClassName().equals("Clean Data") && cleanData)
                        || (object.getJEVisClassName().equals("Data") && rawData)) {
                    value.deleteAllSample();
                }
            }
            for (JEVisObject child : object.getChildren()) {
                deleteAllSamples(child, rawData, cleanData);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", object.getName(), object.getID());
        }
    }

    public static void openPath(JEVisTree tree, List<JEVisObject> toOpen, TreeItem<JEVisTreeRow> root, JEVisObject target) {
//        logger.info("OpenPath: " + root.getValue().getID());
//        logger.trace("OpenPath: {}", target.getID());
        for (TreeItem<JEVisTreeRow> child : root.getChildren()) {
            for (JEVisObject findObj : toOpen) {
//                logger.trace("OpenPath2: toOpen: {} in: {}", findObj.getID(), child.getValue().getJEVisObject().getID());
                if (findObj.getID().equals(child.getValue().getJEVisObject().getID())) {
                    child.expandedProperty().setValue(Boolean.TRUE);
                    openPath(tree, toOpen, child, target);
                }
                if (target.getID().equals(child.getValue().getJEVisObject().getID())) {
                    tree.getSelectionModel().select(child);
                    try {
                        VirtualFlow flow = (VirtualFlow) tree.getChildrenUnmodifiable().get(1);
                        int selected = tree.getSelectionModel().getSelectedIndex();
                        flow.show(selected);
                    } catch (Exception ex) {

                    }

                }
            }

        }
    }

    /**
     * Find and select the JEVisObject in the JEVisTree. WARNING this function
     * will go over all Items wich getChildren which will load them all.
     *
     * @param tree
     * @param startObj
     * @param findObj
     */
    public static void selectNode(JEVisTree tree, TreeItem<JEVisObject> startObj, JEVisObject findObj) {
        for (TreeItem<JEVisObject> item : startObj.getChildren()) {
            if (Objects.equals(item.getValue().getID(), findObj.getID())) {
                tree.getSelectionModel().select(item);
            } else {
                selectNode(tree, item, findObj);
            }
        }
    }

    public static void EventOpenObject(JEVisTree tree, KeyCombination keyCombination) {
        try {
            JEVisDataSource ds = tree.getJEVisDataSource();

            if (keyCombination == null || keyCombination.equals(JEVisTreeFactory.findNode)) {
                FindDialog dia = new FindDialog(ds);
                FindDialog.Response response = dia.show(I18n.getInstance().getString("jevistree.dialog.find.title")
                        , I18n.getInstance().getString("jevistree.dialog.find.message")
                        , "");

                if (response == FindDialog.Response.YES) {
                    try {
                        JEVisObject findObj = ds.getObject(Long.parseLong(dia.getResult()));
                        logger.trace("Found Object: " + findObj);
                        if (findObj != null) {
                            List<JEVisObject> toOpen = org.jevis.commons.utils.ObjectHelper.getAllParents(findObj);
                            toOpen.add(findObj);
                            logger.trace("Open Path: {}", Arrays.toString(toOpen.toArray()));

                            TreeHelper.openPath(tree, toOpen, tree.getRoot(), findObj);

                        }
                    } catch (NumberFormatException nfe) {
                        try {
                            List<JEVisObject> allObjects = ds.getObjects();
                            for (JEVisObject object : allObjects) {
                                if (object.getName().contains(dia.getResult())) {
                                    List<JEVisObject> toOpen = ObjectHelper.getAllParents(object);
                                    toOpen.add(object);
                                    logger.trace("Open Path: {}", Arrays.toString(toOpen.toArray()));

                                    TreeHelper.openPath(tree, toOpen, tree.getRoot(), object);
                                    lastSearchIndex = allObjects.indexOf(object);
                                    lastSearch = dia.getResult();
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle(I18n.getInstance().getString("jevistree.dialog.find.error.title"));
                            alert.setHeaderText("");
                            String s = I18n.getInstance().getString("jevistree.dialog.find.error.message");
                            alert.setContentText(s);
                            alert.show();
                        }
                    }

                }
            } else {
                try {
                    if (lastSearchIndex > 0 && !lastSearch.equals("")) {
                        List<JEVisObject> allObjects = ds.getObjects();
                        for (JEVisObject object : allObjects.subList((int) lastSearchIndex, allObjects.size() - 1)) {
                            if (object.getName().contains(lastSearch)) {
                                List<JEVisObject> toOpen = ObjectHelper.getAllParents(object);
                                toOpen.add(object);
                                logger.trace("Open Path: {}", Arrays.toString(toOpen.toArray()));

                                TreeHelper.openPath(tree, toOpen, tree.getRoot(), object);
                                lastSearchIndex = allObjects.indexOf(object) + 1;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.find.error.title"));
                    alert.setHeaderText("");
                    String s = I18n.getInstance().getString("jevistree.dialog.find.error.message");
                    alert.setContentText(s);
                    alert.show();
                }
            }

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(I18n.getInstance().getString("jevistree.dialog.find.error.title"));
            alert.setHeaderText("");
            String s = I18n.getInstance().getString("jevistree.dialog.find.error.message");
            alert.setContentText(s);
            alert.show();
            ex.printStackTrace();
        }
    }

    public static void moveObject(final JEVisObject moveObj, final JEVisObject targetObj) {
        logger.debug("EventMoveObject");
        try {

            // remove other parent relationships
            for (JEVisRelationship rel : moveObj.getRelationships(JEVisConstants.ObjectRelationship.PARENT)) {
                if (rel.getStartObject().equals(moveObj)) {
                    moveObj.deleteRelationship(rel);
                }
            }

            JEVisRelationship newRel = moveObj.buildRelationship(targetObj, JEVisConstants.ObjectRelationship.PARENT, JEVisConstants.Direction.FORWARD);


        } catch (Exception ex) {
            logger.catching(ex);
            CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.move.error.title"),
                    I18n.getInstance().getString("jevistree.dialog.move.error.message"), null, ex);
        }
    }

    public static void buildLink(JEVisObject linkSrcObj, final JEVisObject targetParent, String linkName) {
        try {
            JEVisObject newLinkObj = targetParent.buildObject(linkName, targetParent.getDataSource().getJEVisClass(CommonClasses.LINK.NAME));
            newLinkObj.commit();
            logger.debug("new LinkObject: " + newLinkObj);
            CommonObjectTasks.createLink(newLinkObj, linkSrcObj);
        } catch (JEVisException ex) {
            logger.error(ex);
        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    public static void EventReload(JEVisObject object, JEVisTreeItem jeVisTreeItem) {
        final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.menu.reload") + "...");

        Task<Void> reload = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    object.getDataSource().reloadAttribute(object);
                    object.getDataSource().reloadObject(object);
                    Platform.runLater(() -> {
                        JEVisTreeRow sobj = new JEVisTreeRow(object);
                        jeVisTreeItem.setValue(sobj);
                    });
                } catch (JEVisException e) {
                    logger.error("Could not reload object.");
                }

                return null;
            }
        };
        reload.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                pForm.getDialogStage().close();
            }
        });

        reload.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                logger.debug("Reload Cancel");
                pForm.getDialogStage().hide();
            }
        });

        reload.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                logger.debug("Reload failed");
                pForm.getDialogStage().hide();
            }
        });

        pForm.activateProgressBar(reload);
        pForm.getDialogStage().show();

        new Thread(reload).start();


    }


    public static void EventRename(final JEVisTree tree, JEVisObject object) {
        logger.trace("EventRename");

        NewObjectDialog dia = new NewObjectDialog();
        if (object != null) {
            try {
                if (dia.show(
                        object.getJEVisClass(),
                        object,
                        true,
                        NewObjectDialog.Type.RENAME,
                        object.getName()
                ) == NewObjectDialog.Response.YES) {

                    final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.menu.rename") + "...");

                    Task<Void> reload = new Task<Void>() {
                        @Override
                        protected Void call() {
                            try {
                                if (!dia.getCreateName().isEmpty()) {
                                    object.setName(dia.getCreateName());
                                    object.commit();
                                }

                            } catch (JEVisException ex) {
                                logger.catching(ex);
                            }

                            return null;
                        }
                    };
                    reload.setOnSucceeded(event -> pForm.getDialogStage().close());

                    reload.setOnCancelled(event -> {
                        logger.debug("Rename Cancelled");
                        pForm.getDialogStage().hide();
                    });

                    reload.setOnFailed(event -> {
                        logger.debug("Rename failed");
                        pForm.getDialogStage().hide();
                    });

                    pForm.activateProgressBar(reload);
                    pForm.getDialogStage().show();

                    new Thread(reload).start();
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        }

    }

    private final static Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");

    public static void EventDrop(final JEVisTree tree, JEVisObject dragObj, JEVisObject targetParent, CopyObjectDialog.DefaultAction mode) {
        try {
            if (targetParent.getID() != null && tree.getJEVisDataSource().getCurrentUser().canCreate(targetParent.getID())) {

                logger.trace("EventDrop");
                CopyObjectDialog dia = new CopyObjectDialog();
                CopyObjectDialog.Response re = dia.show((Stage) tree.getScene().getWindow(), dragObj, targetParent, mode);


                if (re == CopyObjectDialog.Response.MOVE) {
                    moveObject(dragObj, targetParent);
                } else if (re == CopyObjectDialog.Response.LINK) {
                    buildLink(dragObj, targetParent, dia.getCreateName());
                } else if (re == CopyObjectDialog.Response.COPY) {
                    copyObject(dragObj, targetParent, dia.getCreateName(), dia.isIncludeData(), dia.isRecursion(), dia.getCreateCount());
                }
            } else {
                Platform.runLater(() -> {
                    Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                    alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                    alert1.showAndWait();
                });

            }
        } catch (JEVisException e) {
            logger.error("Could not get jevis data source.", e);
        }
    }

    public static void copyObjectUnder(JEVisObject toCopyObj, final JEVisObject newParent, String newName,
                                       boolean includeContent, boolean recursive) throws JEVisException {
        logger.debug("-> copyObjectUnder ([{}]{}) under ([{}]{})", toCopyObj.getID(), toCopyObj.getName(), newParent.getID(), newParent.getName());

        JEVisObject newObject = newParent.buildObject(newName, toCopyObj.getJEVisClass());
        newObject.commit();

        for (JEVisAttribute originalAtt : toCopyObj.getAttributes()) {
            logger.debug("Copy attribute: {}", originalAtt);
            JEVisAttribute newAtt = newObject.getAttribute(originalAtt.getType());
            //Copy the basic attribute config
            newAtt.setDisplaySampleRate(originalAtt.getDisplaySampleRate());
            newAtt.setDisplayUnit(originalAtt.getDisplayUnit());
            newAtt.setInputSampleRate(originalAtt.getInputSampleRate());
            newAtt.setInputUnit(originalAtt.getInputUnit());
            newAtt.commit();
            //if chosen copy the samples
            if (includeContent) {
                if (originalAtt.hasSample()) {
                    logger.debug("Include samples");

                    List<JEVisSample> newSamples = new ArrayList<>();
                    for (JEVisSample sample : originalAtt.getAllSamples()) {
                        if (!originalAtt.getName().equals("Value")) {
                            logger.info("Copy sample: " + originalAtt.getName() + " Value: " + sample.getValue() + "  TS: " + sample.getTimestamp());
                        }
                        newSamples.add(newAtt.buildSample(sample.getTimestamp(), sample.getValue(), sample.getNote()));
                    }
                    logger.debug("Add samples: {}", newSamples.size());
                    newAtt.addSamples(newSamples);
                }
            }
        }

        //TODO: we need an recursive check to avoid an endless loop
        //Also copy the children if chosen
        if (recursive) {
            logger.debug("recursive is enabled");
            for (JEVisObject otherChild : toCopyObj.getChildren()) {
                copyObjectUnder(otherChild, newObject, otherChild.getName(), includeContent, recursive);
            }
        }

    }

    public static void copyObject(final JEVisObject toCopyObj, final JEVisObject newParent, String newName,
                                  boolean includeContent, boolean recursive, int createCount) {
        try {
            logger.debug("-> Copy ([{}]{}) under ([{}]{})", toCopyObj.getID(), toCopyObj.getName(), newParent.getID(), newParent.getName());

            final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.menu.copy") + "...");

            Task<Void> upload = new Task<Void>() {
                @Override
                protected Void call() {

                    try {
                        for (int i = 0; i < createCount; i++) {
                            String name = newName;
                            if (createCount > 1) {
                                name += (" " + (i + 1));
                            }
                            copyObjectUnder(toCopyObj, newParent, name, includeContent, recursive);
                        }

                    } catch (Exception ex) {
                        logger.catching(ex);
                        CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.copy.error.title"),
                                I18n.getInstance().getString("jevistree.dialog.copy.error.message"), null, ex);
                        failed();
                    }
                    return null;
                }
            };
            upload.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    pForm.getDialogStage().close();
                }
            });

            upload.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    logger.error("Upload Cancel");
                    pForm.getDialogStage().hide();
                }
            });

            upload.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    logger.error("Upload failed");
                    pForm.getDialogStage().hide();
                }
            });

            pForm.activateProgressBar(upload);
            pForm.getDialogStage().show();

            new Thread(upload).start();

        } catch (Exception ex) {
            logger.catching(ex);
            CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.copy.error.title"),
                    I18n.getInstance().getString("jevistree.dialog.copy.error.message"), null, ex);
        }
    }

    /**
     * Opens the new Object Dialog.
     *
     * @param tree
     * @param parent
     */
    public static void EventNew(final JEVisTree tree, JEVisObject parent) {
        try {
            if (parent != null && tree.getJEVisDataSource().getCurrentUser().canCreate(parent.getID())) {
                NewObjectDialog dia = new NewObjectDialog();

                if (dia.show(null, parent, false, NewObjectDialog.Type.NEW, null) == NewObjectDialog.Response.YES) {

                    final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("plugin.object.member.create") + "...");

                    Task<Void> upload = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            for (int i = 0; i < dia.getCreateCount(); i++) {
                                try {
                                    String name = dia.getCreateName();
                                    if (dia.getCreateCount() > 1) {
                                        name += " " + (i + 1);
                                    }

                                    JEVisClass createClass = dia.getCreateClass();
                                    JEVisObject newObject = parent.buildObject(name, createClass);
                                    newObject.commit();

                                    JEVisClass dataClass = newObject.getDataSource().getJEVisClass("Data");
                                    JEVisClass cleanDataClass = newObject.getDataSource().getJEVisClass("Clean Data");
                                    JEVisClass reportClass = newObject.getDataSource().getJEVisClass("Periodic Report");
                                    JEVisClass reportLinkClass = newObject.getDataSource().getJEVisClass("Report Link");
                                    JEVisClass reportAttributeClass = newObject.getDataSource().getJEVisClass("Report Attribute");
                                    JEVisClass reportPeriodConfigurationClass = newObject.getDataSource().getJEVisClass("Report Period Configuration");
                                    if (createClass.equals(dataClass) || createClass.equals(cleanDataClass)) {
                                        JEVisAttribute valueAttribute = newObject.getAttribute("Value");
                                        valueAttribute.setInputSampleRate(Period.minutes(15));
                                        valueAttribute.setDisplaySampleRate(Period.minutes(15));
                                        valueAttribute.commit();

                                        if (createClass.equals(dataClass) && dia.isWithCleanData()) {
                                            JEVisObject newCleanObject = newObject.buildObject(I18nWS.getInstance().getClassName(cleanDataClass), cleanDataClass);
                                            newCleanObject.commit();

                                            JEVisAttribute cleanDataValueAttribute = newCleanObject.getAttribute("Value");
                                            cleanDataValueAttribute.setInputSampleRate(Period.minutes(15));
                                            cleanDataValueAttribute.setDisplaySampleRate(Period.minutes(15));
                                            cleanDataValueAttribute.commit();
                                        }

                                    } else if (createClass.equals(reportClass)) {
                                        ReportWizardDialog rwd = new ReportWizardDialog(newObject);

                                        rwd.showAndWait();
                                        if (rwd.getSelections() != null) {
                                            JEVisObject reportLinkDirectory = rwd.getReportLinkDirectory();
                                            for (ReportLink rl : rwd.getReportLinkList()) {
                                                Platform.runLater(() -> {
                                                    try {
                                                        String variableName = rl.getTemplateVariableName();

                                                        JEVisObject object = reportLinkDirectory.buildObject(variableName, reportLinkClass);
                                                        object.commit();

                                                        JEVisAttribute jeVis_id = object.getAttribute("JEVis ID");
                                                        JEVisSample sample = jeVis_id.buildSample(new DateTime(), rl.getjEVisID());
                                                        sample.commit();

                                                        JEVisAttribute optionalAttribute = object.getAttribute("Optional");
                                                        JEVisSample sampleOptional = optionalAttribute.buildSample(new DateTime(), rl.isOptional());
                                                        sampleOptional.commit();

                                                        JEVisAttribute templateVariableName = object.getAttribute("Template Variable Name");
                                                        JEVisSample sample1 = templateVariableName.buildSample(new DateTime(), variableName);
                                                        sample1.commit();

                                                        JEVisObject reportAttribute = object.buildObject("Report Attribute", reportAttributeClass);
                                                        reportAttribute.commit();
                                                        JEVisAttribute attribute_name = reportAttribute.getAttribute("Attribute Name");

                                                        JEVisSample sample4 = attribute_name.buildSample(new DateTime(), rl.getReportAttribute().getAttributeName());
                                                        sample4.commit();

                                                        JEVisObject reportPeriodConfiguration = reportAttribute.buildObject("Report Period Configuration", reportPeriodConfigurationClass);
                                                        reportPeriodConfiguration.commit();

                                                        JEVisAttribute aggregationAttribute = reportPeriodConfiguration.getAttribute("Aggregation");
                                                        JEVisSample sample2 = aggregationAttribute.buildSample(new DateTime(), rl.getReportAttribute().getReportPeriodConfiguration().getReportAggregation());
                                                        sample2.commit();

                                                        JEVisAttribute periodAttribute = reportPeriodConfiguration.getAttribute("Period");
                                                        JEVisSample sample3 = periodAttribute.buildSample(new DateTime(), rl.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().toString());
                                                        sample3.commit();
                                                    } catch (JEVisException e) {
                                                        e.printStackTrace();
                                                    }
                                                });
                                            }

                                            try {
                                                JEVisFile template = rwd.createTemplate(newObject.getName());
                                                JEVisAttribute templateAttribute = newObject.getAttribute("Template");
                                                JEVisSample sample = templateAttribute.buildSample(new DateTime(), template);
                                                sample.commit();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }

                                } catch (JEVisException ex) {
                                    logger.catching(ex);

                                    if (ex.getMessage().equals("Can not create User with this name. The User has to be unique on the System")) {
                                        InfoDialog info = new InfoDialog();
                                        info.show("Waring", "Could not create user", "Could not create new user because this user exists already.");

                                    } else {
                                        ExceptionDialog errorDia = new ExceptionDialog();
                                        errorDia.show("Error", ex.getLocalizedMessage(), ex.getLocalizedMessage(), ex, null);

                                    }

                                }
                            }

                            return null;
                        }
                    };
                    upload.setOnSucceeded(event -> pForm.getDialogStage().close());

                    upload.setOnCancelled(event -> {
                        logger.error(I18n.getInstance().getString("plugin.object.waitsave.canceled"));
                        pForm.getDialogStage().hide();
                    });

                    upload.setOnFailed(event -> {
                        logger.error(I18n.getInstance().getString("plugin.object.waitsave.failed"));
                        pForm.getDialogStage().hide();
                    });

                    pForm.activateProgressBar(upload);
                    pForm.getDialogStage().show();

                    new Thread(upload).start();
                }
            } else {
                Platform.runLater(() -> {
                    Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                    alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                    alert1.showAndWait();
                });
            }
        } catch (JEVisException e) {
            logger.error("Could not get jevis data source.", e);
        }
    }

    public static void EventExportTree(JEVisObject obj) throws JEVisException {
        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        JEVisTreeFilter basicFilter = SelectTargetDialog.buildAllDataFilter();
        allFilter.add(basicFilter);
        SelectTargetDialog dia = new SelectTargetDialog(allFilter, basicFilter, null, SelectionMode.SINGLE);
        List<UserSelection> userSelection = new ArrayList<>();
        userSelection.add(new UserSelection(UserSelection.SelectionType.Object, obj));


        SelectTargetDialog.Response response = dia.show(obj.getDataSource(), "Export", userSelection);

        if (response == SelectTargetDialog.Response.OK) {
            List<JEVisObject> objects = new ArrayList<>();

            for (UserSelection us : dia.getUserSelection()) {
                objects.add(us.getSelectedObject());
            }

            try {
                ExportMaster em = new ExportMaster();
                em.setObject(objects, true);
                em.createTemplate(obj);


                DirectoryChooser fileChooser = new DirectoryChooser();

                fileChooser.setTitle("Open Resource File");
//                fileChooser.getExtensionFilters().addAll();
                File selectedFile = fileChooser.showDialog(null);
                if (selectedFile != null) {
                    em.export(selectedFile);
                }

            } catch (IOException io) {

            }


        }
    }


    public static void createCalcInput(JEVisObject calcObject, JEVisAttribute currentTarget) throws
            JEVisException {
        logger.info("Event Create new Input");

        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataFilter();
        JEVisTreeFilter allAttributesFilter = SelectTargetDialog.buildAllAttributesFilter();
        allFilter.add(allDataFilter);
        allFilter.add(allAttributesFilter);

        SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, allDataFilter, null, SelectionMode.MULTIPLE);
        if (selectTargetDialog.show(
                calcObject.getDataSource(),
                I18n.getInstance().getString("dialog.target.data.title"),
                null
        ) == SelectTargetDialog.Response.OK) {
            if (selectTargetDialog.getUserSelection() != null && !selectTargetDialog.getUserSelection().isEmpty()) {
                for (UserSelection us : selectTargetDialog.getUserSelection()) {

                    String inputName = CalculationNameFormatter.createVariableName(us.getSelectedObject());

                    JEVisClass inputClass = calcObject.getDataSource().getJEVisClass("Input");
                    JEVisObject newInputObj = calcObject.buildObject(inputName, inputClass);
                    newInputObj.commit();

                    DateTime now = new DateTime();
                    JEVisAttribute aIdentifier = newInputObj.getAttribute("Identifier");
                    JEVisSample newSample = aIdentifier.buildSample(now, inputName);
                    newSample.commit();

                    JEVisAttribute aInputData = newInputObj.getAttribute("Input Data");
                    JEVisAttribute inputDataTypeAtt = newInputObj.getAttribute("Input Data Type");

                    JEVisAttribute targetAtt = us.getSelectedAttribute();
                    if (targetAtt == null) {
                        targetAtt = us.getSelectedObject().getAttribute("Value");
                    }
                    TargetHelper th = new TargetHelper(us.getSelectedObject().getDataSource(), us.getSelectedObject(), targetAtt);
                    if (th.isValid() && th.targetAccessible()) {
                        logger.info("Target Is valid");
                        JEVisSample newTarget = aInputData.buildSample(now, th.getSourceString());
                        newTarget.commit();
                        JEVisSample periodicInputData = inputDataTypeAtt.buildSample(new DateTime(), "PERIODIC");
                        periodicInputData.commit();
                    } else {
                        logger.info("Target is not valid");
                    }
                }
            }
        }
    }

    /**
     * Opens the new Object dialog for the currently selected node in the tree
     *
     * @param tree
     */
    public static void EventNew(final JEVisTree tree) {
        final TreeItem<JEVisTreeRow> parent = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem());
        EventNew(tree, parent.getValue().getJEVisObject());
    }

}
