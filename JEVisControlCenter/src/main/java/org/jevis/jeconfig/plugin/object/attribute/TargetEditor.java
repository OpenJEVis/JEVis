/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.TreeHelper;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.dialog.SelectTargetDialog.MODE;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author fs
 */
public class TargetEditor implements AttributeEditor {

    private static final Logger logger = LogManager.getLogger(TargetEditor.class);
    private final MODE mode;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final StackPane dialogContainer;
    public JEVisAttribute _attribute;
    private final HBox box = new HBox();
    private final boolean _hasChanged = false;
    private final JEVisTree tree;
    private boolean _readOnly = true;
    JEVisSample newSample;
    private JFXButton _treeButton;

    public TargetEditor(StackPane dialogContainer, JEVisAttribute att, MODE mode, JEVisTree tree) {
        this.dialogContainer = dialogContainer;
        _attribute = att;
        this.mode = mode;
        this.tree = tree;
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            box.getChildren().clear();
            init();
        });
    }

    @Override
    public boolean hasChanged() {
//        logger.info(attribute.getName() + " changed: " + _hasChanged);
        return _changed.getValue();
    }

    @Override
    public void setReadOnly(boolean canRead) {
        _readOnly = canRead;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void commit() throws JEVisException {
        if (newSample != null) {
            newSample.commit();
        }

    }

    @Override
    public Node getEditor() {
        try {
            init();
        } catch (Exception ex) {
            logger.catching(ex);
        }

        return box;
    }

    private void init() {
        _treeButton = new JFXButton(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                JEConfig.getImage("folders_explorer.png", 18, 18));
        _treeButton.wrapTextProperty().setValue(true);

        JFXButton gotoButton = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.target.goto"),
                JEConfig.getImage("1476393792_Gnome-Go-Jump-32.png", 18, 18));//icon
        gotoButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.attribute.target.goto.tooltip")));

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        box.setSpacing(10);

        box.getChildren().setAll(_treeButton, gotoButton, rightSpacer);

        _treeButton.setOnAction(getTreeButtonActionEventEventHandler());

        gotoButton.setOnAction(event -> {
            try {

                TargetHelper th;
                if (newSample != null) {
                    th = new TargetHelper(_attribute.getDataSource(), newSample.getValueAsString());
                } else {
                    th = new TargetHelper(_attribute.getDataSource(), _attribute);
                }

                if (th.isValid() && th.targetAccessible()) {
                    JEVisObject findObj = _attribute.getDataSource().getObject(th.getObject().get(0).getID());

                    List<JEVisObject> toOpen = org.jevis.commons.utils.ObjectHelper.getAllParents(findObj);
                    toOpen.add(findObj);
                    logger.trace("Open Path: {}", Arrays.toString(toOpen.toArray()));

                    TreeHelper.openPath(tree, toOpen, tree.getRoot(), findObj);
                }
            } catch (Exception ex) {
                logger.catching(ex);
            }
        });

        setButtonText();

    }

    public EventHandler<ActionEvent> getTreeButtonActionEventEventHandler() {
        return t -> {
            try {
                SelectTargetDialog selectTargetDialog = null;

                /**
                 * TODO:
                 * The not so pretty solution to add target specific filter
                 */
                JEVisSample latestSample = _attribute.getLatestSample();
                TargetHelper th = null;
                if (latestSample != null) {
                    th = new TargetHelper(_attribute.getDataSource(), latestSample.getValueAsString());
                    if (th.isValid() && th.targetAccessible()) {
                        logger.info("Target Is valid");
                        setButtonText();
                    }
                }

                List<JEVisTreeFilter> allFilter = new ArrayList<>();
                JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataAndCleanDataFilter();
                JEVisTreeFilter allAttributesFilter = SelectTargetDialog.buildAllAttributesFilter();
                allFilter.add(allDataFilter);
                allFilter.add(allAttributesFilter);

                List<UserSelection> openList = new ArrayList<>();
                if (th != null && !th.getAttribute().isEmpty()) {
                    for (JEVisAttribute att : th.getAttribute())
                        openList.add(new UserSelection(UserSelection.SelectionType.Attribute, att, null, null));
                } else if (th != null && !th.getObject().isEmpty()) {
                    for (JEVisObject obj : th.getObject())
                        openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
                }

                if (_attribute.getObject().getJEVisClassName().equals("Alarm Configuration")) {
                    selectTargetDialog = new SelectTargetDialog(dialogContainer, allFilter, allDataFilter, null, SelectionMode.MULTIPLE, _attribute.getDataSource(), openList);
                } else {
                    selectTargetDialog = new SelectTargetDialog(dialogContainer, allFilter, allDataFilter, null, SelectionMode.SINGLE, _attribute.getDataSource(), openList);
                }

                SelectTargetDialog finalSelectTargetDialog = selectTargetDialog;
                selectTargetDialog.setOnDialogClosed(event -> {
                    try {
                        if (finalSelectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                            logger.trace("Selection Done");

                            String newTarget = "";
                            List<UserSelection> selections = finalSelectTargetDialog.getUserSelection();
                            for (UserSelection us : selections) {
                                int index = selections.indexOf(us);
                                if (index > 0) newTarget += ";";

                                newTarget += us.getSelectedObject().getID();
                                if (us.getSelectedAttribute() != null) {
                                    newTarget += ":" + us.getSelectedAttribute().getName();
                                } else {
                                    newTarget += ":Value";
                                }
                            }
                            JEVisSample newSample = _attribute.buildSample(DateTime.now(), newTarget);
                            newSample.commit();
                            setButtonText();
                        }
                    } catch (Exception ex) {
                        logger.catching(ex);
                    }
                });
                selectTargetDialog.show();

            } catch (Exception ex) {
                logger.catching(ex);
            }
        };
    }

    void setButtonText() {
        TargetHelper th;
        try {
            if (newSample != null) {
                th = new TargetHelper(_attribute.getDataSource(), newSample.getValueAsString());
            } else {
                th = new TargetHelper(_attribute.getDataSource(), _attribute);
            }

            if (th.isValid() && th.targetAccessible()) {

                StringBuilder bText = new StringBuilder();

                JEVisClass cleanData = _attribute.getDataSource().getJEVisClass("Clean Data");

                for (JEVisObject obj : th.getObject()) {
                    int index = th.getObject().indexOf(obj);
                    if (index > 0) bText.append("; ");

                    if (obj.getJEVisClass().equals(cleanData)) {
                        List<JEVisObject> parents = obj.getParents();
                        if (!parents.isEmpty()) {
                            for (JEVisObject parent : parents) {
                                bText.append("[");
                                bText.append(parent.getID());
                                bText.append("] ");
                                bText.append(parent.getName());
                                bText.append(" / ");
                            }
                        }
                    }

                    bText.append("[");
                    bText.append(obj.getID());
                    bText.append("] ");
                    bText.append(obj.getName());

                    if (th.hasAttribute()) {

                        bText.append(" - ");
                        bText.append(th.getAttribute().get(index).getName());

                    }
                }

                Platform.runLater(() -> _treeButton.setText(bText.toString()));
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }
}
