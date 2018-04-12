/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.dataprocessing.v2;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Simon
 */
public class BasicTask implements Task {

    private Function dp;
    private List<Task> dependency;

    @Override
    public Result getResult() {
        List<Result> results = new ArrayList<>();
        if (dependency != null && !dependency.isEmpty()) {
            for (Task prevTask : dependency) {
                results.add(prevTask.getResult());
            }
        }
        dp.setInput(results);
        return dp.getResult();
    }

    @Override
    public void setDataProcessor(Function dp) {
        this.dp = dp;
    }

    @Override
    public Function getDataProcessor() {
        return this.dp;
    }

    @Override
    public void setDependency(List<Task> dps) {
        this.dependency = dps;
    }

    @Override
    public List<Task> getDependency() {
        return this.dependency;
    }

}
