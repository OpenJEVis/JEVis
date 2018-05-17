/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.application.dialog.SelectTargetDialog2;
import org.jevis.application.dialog.SelectTargetDialog2.MODE;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.TreeHelper;
import org.jevis.application.object.tree.UserSelection;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

/**
 *
 * @author fs
 */
public class TargetEditor implements AttributeEditor {

    HBox box = new HBox();
    public JEVisAttribute _attribute;
    private boolean _hasChanged = false;
    private Button _treeButton;
    private boolean _readOnly = true;
    private JEVisSample newSample;
    private final Logger logger = LogManager.getLogger(TargetEditor.class);

    private final MODE mode;
    private JEVisTree tree;

    private final BooleanProperty _changed = new SimpleBooleanProperty(false);

    public TargetEditor(JEVisAttribute att, MODE mode, JEVisTree tree) {
        logger.debug("new TagetEditor for: {}", att.getName());
        _attribute = att;
        this.mode = mode;
        this.tree = tree;
    }

    @Override
    public boolean hasChanged() {
//        System.out.println(_attribute.getName() + " changed: " + _hasChanged);
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

    private void init() throws JEVisException {
        logger.debug("init TargetEditor");
        _treeButton = new Button(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                JEConfig.getImage("folders_explorer.png", 18, 18));

        Button gotoButton = new Button(I18n.getInstance().getString("plugin.object.attribute.target.goto"),
                JEConfig.getImage("1476393792_Gnome-Go-Jump-32.png", 18, 18));//icon
        gotoButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.attribute.target.goto.tooltip")));

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        box.setSpacing(10);

        box.getChildren().setAll(_treeButton, gotoButton, rightSpacer);

        _treeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                try {
                    SelectTargetDialog2 selectionDialog = new SelectTargetDialog2();
                    List<UserSelection> openList = new ArrayList<>();

                    try {
                        TargetHelper th;
                        if (newSample != null) {
                            th = new TargetHelper(_attribute.getDataSource(), newSample.getValueAsString());
                        } else {
                            th = new TargetHelper(_attribute.getDataSource(), _attribute);
                        }

                        if (th.isValid()) {
                            if (mode == MODE.ATTRIBUTE) {
                                if (th.hasAttribute() && th.targetAccessable()) {
                                    logger.trace("th.Att: {}", th.getAttribute());
                                    UserSelection us = new UserSelection(UserSelection.SelectionType.Attribute, th.getAttribute(), null, null);
                                    openList.add(us);
                                }
                            } else if (mode == MODE.OBJECT) {
                                if (th.hasObject() && th.targetAccessable()) {
                                    logger.trace("th.object: {}", th.getObject());
                                    UserSelection us = new UserSelection(UserSelection.SelectionType.Object, th.getObject());
                                    openList.add(us);
                                }

                            }

                        }

                    } catch (Exception jex) {
                        logger.catching(jex);
                    }

                    if (selectionDialog.show(
                            JEConfig.getStage(),
                            _attribute.getObject().getDataSource(),
                            I18n.getInstance().getString("plugin.object.attribute.target.selection"),
                            openList,
                            mode
                    ) == SelectTargetDialog2.Response.OK) {
                        logger.trace("Selection Done");
                        for (UserSelection us : selectionDialog.getUserSelection()) {
                            logger.trace("us: {}", us.getSelectedObject());

                            TargetHelper th = new TargetHelper(_attribute.getDataSource(), us.getSelectedObject(), us.getSelectedAttribute());

                            if (th.isValid() && th.targetAccessable()) {
                                newSample = _attribute.buildSample(new DateTime(), th.getSourceString());
                                setButtonText();
                            }

                            logger.trace("New Target: [{}] {}", _attribute, newSample.getValueAsString());
                            _changed.setValue(true);

                        }

                    }
                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }
        }
        );

        gotoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {

                    TargetHelper th;
                    if (newSample != null) {
                        th = new TargetHelper(_attribute.getDataSource(), newSample.getValueAsString());
                    } else {
                        th = new TargetHelper(_attribute.getDataSource(), _attribute);
                    }

                    if (th.isValid() && th.targetAccessable()) {
                        JEVisObject findObj = _attribute.getDataSource().getObject(th.getObject().getID());

                        List<JEVisObject> toOpen = org.jevis.commons.utils.ObjectHelper.getAllParents(findObj);
                        toOpen.add(findObj);
                        logger.trace("Open Path: {}", Arrays.toString(toOpen.toArray()));

                        TreeHelper.openPath(tree, toOpen, tree.getRoot(), findObj);
                    }
                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }
        });

        setButtonText();

    }

    private void setButtonText() {
        TargetHelper th;
        try {
            if (newSample != null) {
                th = new TargetHelper(_attribute.getDataSource(), newSample.getValueAsString());
            } else {
                th = new TargetHelper(_attribute.getDataSource(), _attribute);
            }

            if (th.isValid() && th.targetAccessable()) {
//                newSample = _attribute.buildSample(new DateTime(), th.getSourceString());
                String bText = "[" + th.getObject().getID() + "] " + th.getObject().getName();
                if (th.hasAttribute()) {
                    bText += " - " + th.getAttribute().getName();
                }
                _treeButton.setText(bText);
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }
}
