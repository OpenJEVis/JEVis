/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jecc.application.unit;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com> //
 */
public class UnitItem extends TreeItem<UnitObject> {

    private final UnitTree _tree;
    private boolean _doInit = true;

    public UnitItem(UnitObject obj) {
        super(obj);
        _tree = null;
    }

    public UnitItem(UnitObject obj, UnitTree tree) {
        super(obj);
        _tree = tree;
    }

    public void expandAll(boolean expand) {
        if (!isLeaf()) {
            if (isExpanded() && !expand) {
                setExpanded(expand);
            } else if (!isExpanded() && expand) {
                setExpanded(expand);
            }

            for (TreeItem child : getChildren()) {
                ((UnitItem) child).expandAll(expand);
            }
        }
    }

    @Override
    public ObservableList<TreeItem<UnitObject>> getChildren() {
        if (_doInit) {
            _doInit = false;

            if (_tree != null) {
                _tree.addChildrenList(this, super.getChildren());
            }
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        return getChildren().isEmpty();
    }
}
