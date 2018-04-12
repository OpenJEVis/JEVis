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

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ViewFilterFactory {

    public static ViewFilter createDefaultGraphFilter() {
        ViewFilter filter = new ViewFilter();

        filter.showAttributes(false);

        ViewFilterRowRule data = new ViewFilterRowRule("Data", "", true);
        data.setVisibleColumn(ColumnFactory.COLOR, true);
        data.setVisibleColumn(ColumnFactory.SELECT_OBJECT, true);
        data.setVisibleColumn(ColumnFactory.OBJECT_ID, false);

        filter.putRule(data);

        return filter;
    }

    public static ViewFilter createMapFilter() {
        ViewFilter filter = new ViewFilter();

        filter.showAttributes(false);

        ViewFilterRowRule data = new ViewFilterRowRule("GPS Data", "", true);
        data.setVisibleColumn(ColumnFactory.COLOR, true);
        data.setVisibleColumn(ColumnFactory.SELECT_OBJECT, true);

        filter.putRule(data);

        return filter;
    }

    public static ViewFilter createShowAllFilter() {
        ViewFilter filter = new ViewFilter();
        filter.showAttributes(true);

        return filter;
    }

}
