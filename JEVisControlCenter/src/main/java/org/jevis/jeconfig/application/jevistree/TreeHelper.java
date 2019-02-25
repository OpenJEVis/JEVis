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
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCombination;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.CommonClasses;
import org.jevis.commons.CommonObjectTasks;
import org.jevis.commons.export.ExportMaster;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.ObjectHelper;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.tools.CalculationNameFormater;
import org.jevis.jeconfig.dialog.*;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author fs
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
        logger.error("EventDelete");
        if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
            String question = I18n.getInstance().getString("jevistree.dialog.delete.message");
            ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();
            for (TreeItem<JEVisTreeRow> item : items) {
                question += item.getValue().getJEVisObject().getName();
            }
            question += "?";

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(I18n.getInstance().getString("jevistree.dialog.delete.title"));
            alert.setHeaderText(null);
            alert.setContentText(question);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                    for (TreeItem<JEVisTreeRow> item : items) {
                        item.getValue().getJEVisObject().getDataSource().deleteObject(item.getValue().getJEVisObject().getID());
                        if (item.getParent() != null) {
                            item.getParent().getChildren().remove(item);
                        }

                    }

                } catch (Exception ex) {
                    logger.catching(ex);
                    CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                            I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                }
            } else {
                // ... user chose CANCEL or closed the dialog
            }
        }
        System.out.println("Done delete");
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
            CommonObjectTasks.createLink(newLinkObj, linkSrcObj);
        } catch (JEVisException ex) {
            logger.error(ex);
        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    public static void EventReload(JEVisObject object) {
        /**
         * TODO make reload function for object tree
         */
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
                    try {
                        if (!dia.getCreateName().isEmpty()) {
                            object.setName(dia.getCreateName());
                            object.commit();
                        }

                    } catch (JEVisException ex) {
                        logger.catching(ex);
                    }
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        }

    }

    public static void EventDrop(final JEVisTree tree, JEVisObject dragObj, JEVisObject targetParent, CopyObjectDialog.DefaultAction mode) {

        logger.trace("EventDrop");
        CopyObjectDialog dia = new CopyObjectDialog();
        CopyObjectDialog.Response re = dia.show((Stage) tree.getScene().getWindow(), dragObj, targetParent, mode);

        if (re == CopyObjectDialog.Response.MOVE) {
            moveObject(dragObj, targetParent);
        } else if (re == CopyObjectDialog.Response.LINK) {
            buildLink(dragObj, targetParent, dia.getCreateName());
        } else if (re == CopyObjectDialog.Response.COPY) {
            for (int i = 0; i < dia.getCreateCount(); ++i) {
                copyObject(dragObj, targetParent, dia.getCreateName(), dia.isIncludeData(), dia.isRecursion());
            }

        }
    }

    public static void copyObjectUnder(JEVisObject toCopyObj, final JEVisObject newParent, String newName, boolean includeContent, boolean recursive) throws JEVisException {
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

    public static void copyObject(final JEVisObject toCopyObj, final JEVisObject newParent, String newName, boolean includeContent, boolean recursive) {
        try {
            logger.debug("-> Copy ([{}]{}) under ([{}]{})", toCopyObj.getID(), toCopyObj.getName(), newParent.getID(), newParent.getName());

            final ProgressForm pForm = new ProgressForm("Uploading..");

            Task<Void> upload = new Task<Void>() {
                @Override
                protected Void call() {

                    try {
                        copyObjectUnder(toCopyObj, newParent, newName, includeContent, recursive);

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
        NewObjectDialog dia = new NewObjectDialog();

        if (parent != null) {
            if (dia.show(null, parent, false, NewObjectDialog.Type.NEW, null) == NewObjectDialog.Response.YES) {
//                logger.info("create new: " + dia.getCreateName() + " class: " + dia.getCreateClass() + " " + dia.getCreateCount() + " times");

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
                        if (createClass.equals(dataClass) || createClass.equals(cleanDataClass)) {
                            JEVisAttribute valueAttribute = newObject.getAttribute("Value");
                            valueAttribute.setInputSampleRate(Period.minutes(15));
                            valueAttribute.setDisplaySampleRate(Period.minutes(15));
                            valueAttribute.commit();
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
            }
        }
    }

    public static void EventExportTree(JEVisObject obj) throws JEVisException {
        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        allFilter.add(SelectTargetDialog.buildAllDataFilter());
        SelectTargetDialog dia = new SelectTargetDialog(allFilter, null, SelectionMode.SINGLE);
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


    public static void createCalcInput(JEVisObject calcObject, JEVisAttribute currentTarget) throws JEVisException {
        logger.info("Event Create new Input");

        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataFilter();
        JEVisTreeFilter allAttributesFilter = SelectTargetDialog.buildAllAttributesFilter();
        allFilter.add(allDataFilter);
        allFilter.add(allAttributesFilter);

        SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, null, SelectionMode.MULTIPLE);
        if (selectTargetDialog.show(
                calcObject.getDataSource(),
                I18n.getInstance().getString("dialog.target.data.title"),
                null
        ) == SelectTargetDialog.Response.OK) {
            if (selectTargetDialog.getUserSelection() != null && !selectTargetDialog.getUserSelection().isEmpty()) {
                for (UserSelection us : selectTargetDialog.getUserSelection()) {

                    String inputName = CalculationNameFormater.createVariableName(us.getSelectedObject());

                    JEVisClass inputClass = calcObject.getDataSource().getJEVisClass("Input");
                    JEVisObject newInputObj = calcObject.buildObject(inputName, inputClass);
                    newInputObj.commit();

                    DateTime now = new DateTime();
                    JEVisAttribute aIdentifier = newInputObj.getAttribute("Identifier");
                    JEVisSample newSample = aIdentifier.buildSample(now, inputName);
                    newSample.commit();

                    JEVisAttribute aInputData = newInputObj.getAttribute("Input Data");
                    JEVisAttribute inputDataTypeAtt = newInputObj.getAttribute("Input Data Type");

                    TargetHelper th = new TargetHelper(aInputData.getDataSource(), us.getSelectedObject(), us.getSelectedAttribute());
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
