/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.application.jevistree.plugin;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.jevistree.TreePlugin;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author fs
 */
public class SimpleTargetPlugin implements TreePlugin {
    private static final Logger logger = LogManager.getLogger(SimpleTargetPlugin.class);
    public static String TARGET_COLUMN_ID = "targetcolumn";
    private JEVisTree _tree;
    private List<UserSelection> _preselect = new ArrayList<>();
    private List<SimpleTargetPluginData> _data = new ArrayList<>();
    private boolean allowMultiSelection = false;
    private BooleanProperty validProperty = new SimpleBooleanProperty(false);
    private MODE mode = MODE.OBJECT;
    private SimpleFilter filter = null;


    @Override
    public void setTree(JEVisTree tree) {
        _tree = tree;
    }

    public List<UserSelection> getUserSelection() {
        List<UserSelection> result = new ArrayList<>();
        for (SimpleTargetPluginData data : _data) {
            if (data.isSelected()) {
//                _preselect.add(new UserSelection(UserSelection.SelectionType.Object, data.getObj()));
                if (data.getAtt() == null) {
                    result.add(new UserSelection(UserSelection.SelectionType.Object, data.getObj()));
                } else {
                    result.add(new UserSelection(UserSelection.SelectionType.Attribute, data.getAtt(), null, null));
                }
            }
        }

        return result;
    }

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> pluginHeader = new TreeTableColumn<>(
                I18n.getInstance().getString("targetplugin.column.target"));
        pluginHeader.setId(TARGET_COLUMN_ID);

        TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree);
        selectColumn.setEditable(true);
        pluginHeader.getColumns().add(selectColumn);

        list.add(pluginHeader);


        return list;
    }

    public BooleanProperty getValidProperty() {
        return validProperty;
    }

    public void setAllowMultiSelection(boolean selection) {
        allowMultiSelection = selection;
    }

    @Override
    public void selectionFinished() {
    }

    @Override
    public String getTitle() {
        return "Selection";
    }

    private SimpleTargetPluginData getData(JEVisTreeRow row) {
        for (SimpleTargetPluginData data : _data) {
            if (row.getID().equals(data.getRow().getID())) {
                return data;
            }
        }
        for (UserSelection us : _preselect) {
            if (row.getJEVisObject().equals(us.getSelectedObject()) && row.getJEVisAttribute().equals(us.getSelectedAttribute())) {
                SimpleTargetPluginData data = new SimpleTargetPluginData(row);
                data.setSelected(true);
                _data.add(data);
                return data;
            }
        }

        SimpleTargetPluginData data = new SimpleTargetPluginData(row);
        data.setSelected(false);
        _data.add(data);
        return data;
    }

    private void deselectAllBut(JEVisTreeRow row) {
        for (SimpleTargetPluginData data : _data) {
            if (data.getRow().getID().equals(row.getID())) {
                continue;
            }

//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
            data.setSelected(false);
//                    data.getBox().setSelected(false);
//                }
//            });

        }
    }

    private TreeTableColumn<JEVisTreeRow, Boolean> buildSelectionColumn(JEVisTree tree) {
        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn("");
        column.setId(TARGET_COLUMN_ID);
        column.setPrefWidth(90);
        column.setEditable(true);

        column.setText("");
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(getData(param.getValue().getValue()).isSelected()));

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {

                return new TreeTableCell<JEVisTreeRow, Boolean>() {

                    @Override
                    public void commitEdit(Boolean newValue) {
                        super.commitEdit(newValue);

                        getTreeTableRow().getItem().setSelected(newValue);
                        getData(getTreeTableRow().getItem()).setSelected(newValue);

                        if (newValue) {
                            validProperty.setValue(true);
                        } else if (isPreselected(getTreeTableRow().getItem())) {
                            removePreSelection(getTreeTableRow().getItem());
                        }
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(null);
                        setGraphic(null);

                        if (!empty) {

                            try {
                                if (getTreeTableRow() != null && getTreeTableRow().getItem() != null && tree != null) {

                                    boolean show = _tree.getFilter().showCell(column, getTreeTableRow().getItem());
                                    if (show) {

                                        StackPane stackPane = new StackPane();
                                        StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);
                                        CheckBox box = new CheckBox();
                                        stackPane.getChildren().add(box);

                                        if (isPreselected(getTreeTableRow().getItem())) {
                                            box.setSelected(true);
                                        } else {
                                            box.setSelected(getData(getTreeTableRow().getItem()).isSelected());
                                        }

                                        box.setOnAction(event -> {
                                            if (!allowMultiSelection && box.isSelected()) {
                                                deselectAllBut(getTreeTableRow().getItem());
                                                tree.refresh();
                                            }

                                            commitEdit(box.isSelected());

                                            logger.debug("Selection: {}", box.isSelected());

                                        });

                                        setGraphic(stackPane);
                                    }


                                }
                            } catch (Exception ex) {
                                logger.error(ex);
                            }

                        }
                    }

                };
            }
        });

        return column;

    }

    private boolean isPreselected(JEVisTreeRow row) {
        for (UserSelection us : _preselect) {
            if (mode == MODE.OBJECT) {
                if (us.getSelectedObject().equals(row.getJEVisObject())) {
                    return true;
                }
            } else if (mode == MODE.ATTRIBUTE) {
                if (us.getSelectedAttribute().equals(row.getJEVisAttribute())) {
                    return true;
                }
            }

        }
        return false;
    }

    private void removePreSelection(JEVisTreeRow row) {
        List<UserSelection> tobeRemoved = new ArrayList<>();
        for (UserSelection us : _preselect) {
            if (us.getSelectedObject().equals(row.getJEVisObject())) {
                tobeRemoved.add(us);
            }
        }
        _preselect.removeAll(tobeRemoved);
    }

    private boolean isPreselected(JEVisObject obj) {
        for (UserSelection us : _preselect) {
            if (us.getSelectedObject().equals(obj)) {
                return true;
            }
        }
        return false;
    }

    public MODE getMode() {
        return mode;
    }

    public void setUserSelection(List<UserSelection> list) {
        if (list != null) {
            _preselect = list;
            _tree.openUserSelectionNoChildren(list);
        }
    }


    public enum MODE {
        OBJECT, ATTRIBUTE, FILTER
    }

    private class AttributeFilter {
        private List<String> attributes = new ArrayList<>();

        public AttributeFilter(String... attributes) {
            this.attributes = Arrays.asList(attributes);
        }

        public List<String> getAttributes() {
            return attributes;
        }
    }

    public class ObjectFilter {
        public final String ALL = "*";
        public final String NONE = "NONE";
        private boolean includeInheritance = false;
        private List<AttributeFilter> attributeFilter = new ArrayList<>();
        private boolean objectFilter = false;
        private String className = "";

        public ObjectFilter(String className, boolean includeInheritance, boolean objectFilter, List<AttributeFilter> attributeFilter) {
            this.includeInheritance = includeInheritance;
            this.attributeFilter = attributeFilter;
            this.objectFilter = objectFilter;
            this.className = className;
        }


        public boolean isIncludeInheritance() {
            return includeInheritance;
        }

        public List<AttributeFilter> getAttributeFilter() {
            return attributeFilter;
        }

        public boolean isObjectFilter() {
            return objectFilter;
        }

        public void setObjectFilter(boolean objectFilter) {
            this.objectFilter = objectFilter;
        }

        public boolean match(JEVisObject obj) {
            try {
                return obj.getJEVisClassName().equals(className);
            } catch (Exception ex) {
                return false;
            }
        }
    }

    public class SimpleFilter {

        List<ObjectFilter> filter = new ArrayList<>();


        public SimpleFilter(List<ObjectFilter> filter) {
            this.filter = filter;
        }

        public List<ObjectFilter> getFilter() {
            return filter;
        }

        public boolean show(Object object) {
            if (object instanceof JEVisObject) {
                return showObject((JEVisObject) object);
            } else if (object instanceof JEVisAttribute) {
                return showAttribute((JEVisAttribute) object);
            }
            return false;
        }

        public boolean showObject(JEVisObject jevisClass) {

            for (ObjectFilter oFilter : filter) {
                if (oFilter.isObjectFilter()
                        && oFilter.match(jevisClass)) {
                    return true;
                }
            }

            return false;
        }

        public boolean showAttribute(JEVisAttribute jevisClass) {
            for (ObjectFilter oFilter : filter) {
                if (!oFilter.isObjectFilter()) {
                    for (AttributeFilter aFilter : oFilter.getAttributeFilter()) {
                        if (aFilter.getAttributes().contains(jevisClass.getName())) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }


    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }
}
