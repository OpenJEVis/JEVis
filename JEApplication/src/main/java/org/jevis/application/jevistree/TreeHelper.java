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
package org.jevis.application.jevistree;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.cache.CacheEvent;
import org.jevis.application.cache.CacheObjectEvent;
import org.jevis.application.cache.Cached;
import org.jevis.application.dialog.*;
import org.jevis.application.tools.CalculationNameFormater;
import org.jevis.commons.CommonClasses;
import org.jevis.commons.CommonObjectTasks;
import org.jevis.commons.export.ExportMaster;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author fs
 */
public class TreeHelper {

    public static Logger LOGGER = LogManager.getLogger(TreeHelper.class);

    private static SaveResourceBundle bundel = new SaveResourceBundle(AppLocale.BUNDLE_ID, AppLocale.getInstance().getLocale());

    /**
     * TODO: make it like the other function where the object is an parameter
     *
     * @param tree
     */
    public static void EventDelete(JEVisTree tree) {
        if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
            String question = tree.getRB().getString("jevistree.dialog.delete.message");
            ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();
            for (TreeItem<JEVisTreeRow> item : items) {
                question += item.getValue().getJEVisObject().getName();
            }
            question += "?";

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(tree.getRB().getString("jevistree.dialog.delete.title"));
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
                    LOGGER.catching(ex);
                    CommonDialogs.showError(tree.getRB().getString("jevistree.dialog.delete.error.title"),
                            tree.getRB().getString("jevistree.dialog.delete.error.message"), null, ex);
                }
            } else {
                // ... user chose CANCEL or closed the dialog
            }
        }

    }

    public static void openPath(JEVisTree tree, List<JEVisObject> toOpen, TreeItem<JEVisTreeRow> root, JEVisObject target) {
//        System.out.println("OpenPath: " + root.getValue().getID());
//        LOGGER.trace("OpenPath: {}", target.getID());
        for (TreeItem<JEVisTreeRow> child : root.getChildren()) {
            for (JEVisObject findObj : toOpen) {
//                LOGGER.trace("OpenPath2: toOpen: {} in: {}", findObj.getID(), child.getValue().getJEVisObject().getID());
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

    public static void EventOpenObject(JEVisTree tree) {
        try {
            JEVisDataSource ds = tree.getJEVisDataSource();
            FindDialog dia = new FindDialog(ds);
            FindDialog.Response respons = dia.show((Stage) tree.getScene().getWindow()
                    , tree.getRB().getString("jevistree.dialog.find.title")
                    , tree.getRB().getString("jevistree.dialog.find.message")
                    , "");

            if (respons == FindDialog.Response.YES) {
                JEVisObject findObj = ds.getObject(Long.parseLong(dia.getResult()));
                LOGGER.trace("Found Object: " + findObj);
                if (findObj != null) {
                    List<JEVisObject> toOpen = org.jevis.commons.utils.ObjectHelper.getAllParents(findObj);
                    toOpen.add(findObj);
                    LOGGER.trace("Open Path: {}", Arrays.toString(toOpen.toArray()));

                    TreeHelper.openPath(tree, toOpen, tree.getRoot(), findObj);

                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(tree.getRB().getString("jevistree.dialog.find.error.title"));
                    alert.setHeaderText("");
                    String s = tree.getRB().getString("jevistree.dialog.find.error.message");
                    alert.setContentText(s);
                    alert.show();
                }

            }

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(tree.getRB().getString("jevistree.dialog.find.error.title"));
            alert.setHeaderText("");
            String s = tree.getRB().getString("jevistree.dialog.find.error.message");
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
            LOGGER.catching(ex);
            CommonDialogs.showError(bundel.getString("jevistree.dialog.move.error.title"),
                    bundel.getString("jevistree.dialog.move.error.message"), null, ex);
        }
    }

    public static void buildLink(JEVisObject linkSrcObj, final JEVisObject targetParent, String linkName) {
        try {
            JEVisObject newLinkObj = targetParent.buildObject(linkName, targetParent.getDataSource().getJEVisClass(CommonClasses.LINK.NAME));
            CommonObjectTasks.createLink(newLinkObj, linkSrcObj);
        } catch (JEVisException ex) {
            LOGGER.catching(ex);
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
    }

    public static void EventReload(JEVisObject object) {
        if (object instanceof Cached) {
            ((Cached) object).fireEvent(new CacheObjectEvent(object, CacheEvent.TYPE.OBJECT_UPDATE));
        }

    }


    public static void EventRename(final JEVisTree tree, JEVisObject object) {
        LOGGER.trace("EventRename");

        NewObjectDialog dia = new NewObjectDialog();
        if (object != null) {
            try {
                if (dia.show((Stage) tree.getScene().getWindow(),
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
                        LOGGER.catching(ex);
                    }
                }
            } catch (JEVisException ex) {
                LOGGER.catching(ex);
            }
        }

    }

    public static void EventDrop(final JEVisTree tree, JEVisObject dragObj, JEVisObject targetParent, CopyObjectDialog.DefaultAction mode) {

        LOGGER.trace("EventDrop");
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
        LOGGER.debug("-> copyObjectUnder ([{}]{}) under ([{}]{})", toCopyObj.getID(), toCopyObj.getName(), newParent.getID(), newParent.getName());

        JEVisObject newObject = newParent.buildObject(newName, toCopyObj.getJEVisClass());
        newObject.commit();

        for (JEVisAttribute originalAtt : toCopyObj.getAttributes()) {
            LOGGER.debug("Copy attribute: {}", originalAtt);
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
                    LOGGER.debug("Include samples");

                    List<JEVisSample> newSamples = new ArrayList<>();
                    for (JEVisSample sample : originalAtt.getAllSamples()) {
                        if (!originalAtt.getName().equals("Value")) {
                            System.out.println("Copy sample: " + originalAtt.getName() + " Value: " + sample.getValue() + "  TS: " + sample.getTimestamp());
                        }
                        newSamples.add(newAtt.buildSample(sample.getTimestamp(), sample.getValue(), sample.getNote()));
                    }
                    LOGGER.debug("Add samples: {}", newSamples.size());
                    newAtt.addSamples(newSamples);
                }
            }
        }

        //TODO: we need an recursive check to avoid an endless loop
        //Also copy the children if chosen
        if (recursive) {
            LOGGER.debug("recursive is enabled");
            for (JEVisObject otherChild : toCopyObj.getChildren()) {
                copyObjectUnder(otherChild, newObject, otherChild.getName(), includeContent, recursive);
            }
        }

    }

    public static void copyObject(final JEVisObject toCopyObj, final JEVisObject newParent, String newName, boolean includeContent, boolean recursive) {
        try {
            LOGGER.debug("-> Copy ([{}]{}) under ([{}]{})", toCopyObj.getID(), toCopyObj.getName(), newParent.getID(), newParent.getName());

            final ProgressForm pForm = new ProgressForm("Uploading..");

            Task<Void> upload = new Task<Void>() {
                @Override
                protected Void call() {

                    try {
                        copyObjectUnder(toCopyObj, newParent, newName, includeContent, recursive);

                    } catch (Exception ex) {
                        LOGGER.catching(ex);
                        CommonDialogs.showError(bundel.getString("jevistree.dialog.copy.error.title"),
                                bundel.getString("jevistree.dialog.copy.error.message"), null, ex);
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
                    LOGGER.error("Upload Cancel");
                    pForm.getDialogStage().hide();
                }
            });

            upload.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    LOGGER.error("Upload failed");
                    pForm.getDialogStage().hide();
                }
            });

            pForm.activateProgressBar(upload);
            pForm.getDialogStage().show();

            new Thread(upload).start();

        } catch (Exception ex) {
            LOGGER.catching(ex);
            CommonDialogs.showError(bundel.getString("jevistree.dialog.copy.error.title"),
                    bundel.getString("jevistree.dialog.copy.error.message"), null, ex);
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
            if (dia.show((Stage) tree.getScene().getWindow(), null, parent, false, NewObjectDialog.Type.NEW, null) == NewObjectDialog.Response.YES) {
//                System.out.println("create new: " + dia.getCreateName() + " class: " + dia.getCreateClass() + " " + dia.getCreateCount() + " times");

                for (int i = 0; i < dia.getCreateCount(); i++) {
                    try {
                        String name = dia.getCreateName();
                        if (dia.getCreateCount() > 1) {
                            name += " " + (i + 1);
                        }

                        JEVisObject newObject = parent.buildObject(name, dia.getCreateClass());
                        newObject.commit();

                    } catch (JEVisException ex) {
                        LOGGER.catching(ex);

                        if (ex.getMessage().equals("Can not create User with this name. The User has to be unique on the System")) {
                            InfoDialog info = new InfoDialog();
                            info.show((Stage) tree.getScene().getWindow(), "Waring", "Could not create user", "Could not create new user because this user exists already.");

                        } else {
                            ExceptionDialog errorDia = new ExceptionDialog();
                            errorDia.show((Stage) tree.getScene().getWindow(), "Error", ex.getLocalizedMessage(), ex.getLocalizedMessage(), ex, null);

                        }

                    }
                }

            }
        }
    }

    public static void EventExportTree(JEVisObject obj) throws JEVisException {
        SelectTargetDialog2 dia = new SelectTargetDialog2();
        dia.allowMultySelect(true);
        List<UserSelection> userSeclection = new ArrayList<>();
        userSeclection.add(new UserSelection(UserSelection.SelectionType.Object, obj));

        SelectTargetDialog2.Response response = dia.show(null, obj.getDataSource(), "Export", userSeclection, SelectTargetDialog2.MODE.OBJECT);

        if (response == SelectTargetDialog2.Response.OK) {
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


    public static void createCalcInput(JEVisObject calcObject) throws JEVisException {
        System.out.println("Event Create new Input");
        SelectTargetDialog2 dia = new SelectTargetDialog2();
        dia.allowMultySelect(true);
        List<UserSelection> userSeclection = new ArrayList<>();
        JEVisClass inputClass = calcObject.getDataSource().getJEVisClass("Input");

        SelectTargetDialog2.Response response = dia.show(null, calcObject.getDataSource(), "Input Selection", userSeclection, SelectTargetDialog2.MODE.OBJECT);

        if (response == SelectTargetDialog2.Response.OK) {
            for (UserSelection us : dia.getUserSelection()) {
                JEVisAttribute valueAttribute = us.getSelectedAttribute();

                //Simple help for user so they do not have to select the Value attribute under Data
                if (us.getSelectedObject().getJEVisClassName().equals("Data")) {
                    valueAttribute = us.getSelectedObject().getAttribute("Value");
                }

                DateTime now = new DateTime();

                String inputName = CalculationNameFormater.crateVarName(us.getSelectedObject());

                JEVisObject newInputObj = calcObject.buildObject(inputName, inputClass);
                newInputObj.commit();

                JEVisAttribute aIdentifier = newInputObj.getAttribute("Identifier");
                JEVisSample newSample = aIdentifier.buildSample(now, inputName);
                newSample.commit();

                JEVisAttribute aInputData = newInputObj.getAttribute("Input Data");


                TargetHelper th = new TargetHelper(aInputData.getDataSource(), us.getSelectedObject(), valueAttribute);
                if (th.isValid() && th.targetAccessable()) {
                    System.out.println("Target Is valid");
                    JEVisSample newTarget = aInputData.buildSample(now, th.getSourceString());
                    newTarget.commit();
                } else {
                    System.out.println("Target is not valid");
                }


                JEVisAttribute aDataType = newInputObj.getAttribute("Input Data Type");
                JEVisSample newTypeSample = aDataType.buildSample(now, "PERIODIC");
                newTypeSample.commit();

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
