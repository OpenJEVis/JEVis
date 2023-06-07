/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jecc.application.jevistree;

import javafx.scene.control.TreeTableColumn;

import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface TreePlugin {

    void setTree(JEVisTree tree);

    List<TreeTableColumn<JEVisTreeRow, Long>> getColumns();

    void selectionFinished();

    String getTitle();

//
//    public int getID();
//
//    public void setID();
}
