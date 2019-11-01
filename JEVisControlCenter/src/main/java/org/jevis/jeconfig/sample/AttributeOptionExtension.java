/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisOption;
import org.jevis.api.JEVisSample;
import org.jevis.commons.config.BasicOption;
import org.joda.time.DateTimeZone;

import java.util.List;

/**
 * Attribute editor extension to configure JEVIsOptions in an generic way.
 *
 * @author Florian Simon
 * @TODO: The current JEVisAttributeSQL implementation is limited to 10240 chars
 * but that does not have to be the case for all implementations.
 */
public class AttributeOptionExtension implements SampleEditorExtension {
    private static final Logger logger = LogManager.getLogger(AttributeOptionExtension.class);

    private BorderPane _view = new BorderPane();
    private JEVisAttribute _att = null;

    public AttributeOptionExtension(JEVisAttribute att) {
        _att = att;
        update();
    }

    private void addContexMenu(TreeTableView<JEVisOption> treeview) {
        ContextMenu menu = new ContextMenu();

        MenuItem removeMenuItem = new MenuItem("Remove Option");
        MenuItem addMenuItem = new MenuItem("Add new Option");
        menu.getItems().addAll(addMenuItem, removeMenuItem);

        try {
            Menu addProcess = new AddProcessChainOptionMenuItem(treeview, _att.getDataSource());
            menu.getItems().add(addProcess);
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        addMenuItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    TreeItem<JEVisOption> treeItem = treeview.getSelectionModel().getSelectedItem();
                    JEVisOption newOption = new BasicOption();
                    newOption.setKey("New Option");

                    treeItem.getValue().addOption(newOption, true);

                    TreeItem<JEVisOption> newItem = new TreeItem<>(newOption);
                    treeItem.getChildren().add(newItem);
                } catch (Exception ex) {
                    logger.info("Error while deleting option: " + ex);
                }
            }
        });

        removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                for (TreeItem<JEVisOption> treeItem : treeview.getSelectionModel().getSelectedItems()) {
                    try {
                        if (!treeItem.equals(treeview.getRoot())) {

                            JEVisOption delOption = treeItem.getValue();
                            treeItem.getParent().getValue().removeOption(delOption);

                            treeItem.getParent().getChildren().remove(treeItem);
                        }
                    } catch (Exception ex) {
                        logger.error("Error while adding option: " + ex);
                    }

                }
            }
        });

        treeview.setContextMenu(menu);

    }

    private void buildView() {
        _view = new BorderPane();

        TreeTableView<JEVisOption> tree = new TreeTableView<>();

        TreeTableColumn<JEVisOption, String> optionColumn = new TreeTableColumn<>("Option");
        tree.getColumns().add(optionColumn);
        optionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getKey()));
        optionColumn.setEditable(true);
        optionColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        optionColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<JEVisOption, String>>() {

            @Override
            public void handle(TreeTableColumn.CellEditEvent<JEVisOption, String> event) {
                final JEVisOption item = event.getRowValue().getValue();
                item.setKey(event.getNewValue());
            }
        });

        TreeTableColumn<JEVisOption, String> valueColumn = new TreeTableColumn<>("Value");
        tree.getColumns().add(valueColumn);
        valueColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getValue()));
        valueColumn.setEditable(true);
        valueColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        valueColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<JEVisOption, String>>() {

            @Override
            public void handle(TreeTableColumn.CellEditEvent<JEVisOption, String> event) {
                final JEVisOption item = event.getRowValue().getValue();
                item.setValue(event.getNewValue());
            }
        });

        TreeTableColumn<JEVisOption, String> descriptColumn = new TreeTableColumn<>("Description");
        tree.getColumns().add(descriptColumn);
        descriptColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getDescription()));
        descriptColumn.setEditable(true);
        descriptColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        descriptColumn.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<JEVisOption, String>>() {

            @Override
            public void handle(TreeTableColumn.CellEditEvent<JEVisOption, String> event) {
                final JEVisOption item = event.getRowValue().getValue();
                item.setDescription(event.getNewValue());
            }
        });

        tree.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

//        TreeTableColumn<JEVisOption, String> ccountColumn = new TreeTableColumn<>("Children");
        //Disabled for the moment
//        tree.getColumns().add(ccountColumn);
//        ccountColumn.setCellValueFactory(param -> new ReadOnlyObjectProperty<Integer>(param.getValue().getValue().getChildren().size())   );
//        ccountColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<JEVisOption, String> p) -> {
//            try {
//                if (p != null && p.getValue() != null && p.getValue().getValue() != null) {
//                    TreeItem<JEVisOption> item = p.getValue();
//                    JEVisOption selectionObject = item.getValue();
//
//                    return new ReadOnlyObjectWrapper<String>(selectionObject.getChildren().size() + "");
//
//                } else {
//                    return new ReadOnlyObjectWrapper<String>("Emty");
//                }
//
//            } catch (Exception ex) {
//                logger.info("Error in Column Fatory: " + ex);
//                return new ReadOnlyObjectWrapper<String>("Error");
//            }
//
//        });
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tree.setEditable(true);

        tree.setRoot(buildTreeItems(_att));

        addContexMenu(tree);

        _view.setCenter(tree);

    }

    private TreeItem<JEVisOption> buildTreeItems(JEVisAttribute att) {
//        logger.info("buildTreeItems: " + att.getName() + "    " + att.getOptions().size());

        //we need an new fake root option for the tree, this on will be hidden
        TreeItem<JEVisOption> fakeRoot = new OptionTreeItem(new JEVisOptionTreeRoot(att));

        fakeRoot.setExpanded(true);

        return fakeRoot;

    }

    @Override
    public boolean isForAttribute(JEVisAttribute obj) {
        return true;
    }

    @Override
    public Node getView() {
        return _view;
    }

    @Override
    public String getTitle() {
        return "Options";
    }

    @Override
    public void disableEditing(boolean disable) {
        //TODO
    }

    @Override
    public void setSamples(JEVisAttribute att, List<JEVisSample> samples) {
        //Nothing to do
    }

    @Override
    public void setDateTimeZone(DateTimeZone dateTimeZone) {

    }

    @Override
    public void update() {
        buildView();
    }

    @Override
    public boolean sendOKAction() {
        try {
            _att.commit();
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return true;
    }

}
