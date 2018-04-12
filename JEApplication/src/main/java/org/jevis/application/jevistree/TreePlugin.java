/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.application.jevistree;

import java.util.List;
import javafx.scene.control.TreeTableColumn;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface TreePlugin {

    public void setTree(JEVisTree tree);

    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns();

    public void selectionFinished();

    public String getTitel();

//
//    public int getID();
//
//    public void setID();
}
