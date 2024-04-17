/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.plugin.object.attribute;


import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jecc.application.jevistree.JEVisTree;
import org.jevis.jecc.application.jevistree.TreeHelper;
import org.jevis.jecc.application.jevistree.UserSelection;
import org.jevis.jecc.dialog.Response;
import org.jevis.jecc.dialog.SelectTargetDialog.MODE;
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
    private final HBox box = new HBox();
    private final boolean _hasChanged = false;
    private final JEVisTree tree;
    public JEVisAttribute _attribute;
    JEVisSample newSample;
    private boolean _readOnly = true;
    private Button _treeButton;
    private boolean initialized = false;

    public TargetEditor(JEVisAttribute att, MODE mode, JEVisTree tree) {
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
            if (!initialized) {
                init();
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }

        return box;
    }

    private void init() {
        _treeButton = new Button(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                ControlCenter.getImage("folders_explorer.png", 18, 18));
        _treeButton.wrapTextProperty().setValue(true);

        Button gotoButton = new Button(I18n.getInstance().getString("plugin.object.attribute.target.goto"),
                ControlCenter.getImage("1476393792_Gnome-Go-Jump-32.png", 18, 18));//icon
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

                if (th.isValid() && th.targetObjectAccessible()) {
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

        initialized = true;
    }

    public EventHandler<ActionEvent> getTreeButtonActionEventEventHandler() {
        return t -> {
            try {
                TreeSelectionDialog treeSelectionDialog = null;

                /**
                 * TODO:
                 * The not so pretty solution to add target specific filter
                 */
                JEVisSample latestSample = _attribute.getLatestSample();
                JEVisType attributeType = _attribute.getType();
                boolean showAttributes = attributeType.getGUIDisplayType().equals("Attribute Target");

                TargetHelper th = null;
                if (latestSample != null) {
                    th = new TargetHelper(_attribute.getDataSource(), latestSample.getValueAsString());
                    if (th.isValid() && th.targetObjectAccessible()) {
                        logger.info("Target Is valid");
                        setButtonText();
                    }
                }

                List<JEVisClass> classes = new ArrayList<>();

                List<UserSelection> openList = new ArrayList<>();
                if (th != null && !th.getAttribute().isEmpty()) {
                    for (JEVisAttribute att : th.getAttribute()) {
                        openList.add(new UserSelection(UserSelection.SelectionType.Attribute, att, null, null));
                    }
                } else if (th != null && !th.getObject().isEmpty()) {
                    for (JEVisObject obj : th.getObject()) {
                        openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
                    }
                }

                boolean isChannel = false;
                JEVisClass channelClass = _attribute.getDataSource().getJEVisClass(JC.Channel.name);
                List<JEVisClass> channelHeirs = channelClass.getHeirs();


                if (_attribute.getObject().getJEVisClassName().equals("Alarm Configuration")
                        || channelHeirs.contains(_attribute.getObject().getJEVisClass())) {
                    treeSelectionDialog = new TreeSelectionDialog(_attribute.getDataSource(), classes, SelectionMode.MULTIPLE, openList, showAttributes);
                } else {
                    treeSelectionDialog = new TreeSelectionDialog(_attribute.getDataSource(), classes, SelectionMode.SINGLE, openList, showAttributes);
                }

                TreeSelectionDialog finalSelectTargetDialog = treeSelectionDialog;
                treeSelectionDialog.setOnCloseRequest(event -> {
                    try {
                        if (finalSelectTargetDialog.getResponse() == Response.OK) {
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
                treeSelectionDialog.show();

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

            if (th.isValid() && th.targetObjectAccessible()) {

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

                    if (th.isAttribute()) {

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
