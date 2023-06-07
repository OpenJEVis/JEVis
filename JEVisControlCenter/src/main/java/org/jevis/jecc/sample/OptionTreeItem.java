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
package org.jevis.jecc.sample;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import org.jevis.api.JEVisOption;

/**
 * TreeView item to handle an JEVisOption.
 *
 * @author Florian Simon
 */
public class OptionTreeItem extends TreeItem<JEVisOption> {

    private boolean isLeaf = true;

    public OptionTreeItem(JEVisOption value) {
        super(value);

        isLeaf = value.getOptions().isEmpty();

        for (JEVisOption opt : value.getOptions()) {
//            logger.info("--addChild: " + opt.getKey());
            OptionTreeItem item = new OptionTreeItem(opt);
            item.setExpanded(true);
            super.getChildren().add(item);
        }

        super.getChildren().addListener((ListChangeListener<TreeItem<JEVisOption>>) c -> {
            if (!getChildren().isEmpty()) {
                isLeaf = false;
            }
        });

    }

    //    @Override
//    public ObservableList<TreeItem<JEVisOption>> getChildren() {
//        logger.info("-");
//
//
//        return super.getChildren(); //To change body of generated methods, choose Tools | Templates.
//    }
    @Override
    public String toString() {
        return getValue().getKey();
    }

    @Override
    public boolean isLeaf() {
        return isLeaf;

//        return getValue().getChildren().isEmpty();
    }

}
