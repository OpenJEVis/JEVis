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
package org.jevis.commons.object.plugin;

import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.api.JEVisUnit;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 *
 * @author Florian Simon
 */
public class VirtualDatas {

    public static List<JEVisSample> GetVirtualSamples(JEVisObject obj, DateTime from, DateTime until) throws JEVisException {

        if (obj.getJEVisClass().getName().equals("List Calculation")) {
            return GetListVirtualsample(obj, from, until);
        }

        return new ArrayList<>();
    }

    public static List<JEVisSample> GetListVirtualsample(JEVisObject obj, DateTime from, DateTime until) {
        return new ArrayList<>();
    }

}
