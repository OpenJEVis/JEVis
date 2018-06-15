/*
  Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>

  This file is part of JEAPI-SQL.

  JEAPI-SQL is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation in version 3.

  JEAPI-SQL is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with
  JEAPI-SQL. If not, see <http://www.gnu.org/licenses/>.

  JEAPI-SQL is part of the OpenJEVis project, further project information are
  published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.ws.sql;

import org.jevis.api.JEVisUnit;
import org.jevis.api.JEVisUnitRelationship;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisUnitRealtionshipSQL implements JEVisUnitRelationship {

    public JEVisUnitRealtionshipSQL(Type type, String unitA, String unitB) {
    }

    @Override
    public JEVisUnit getUnitA() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisUnit getUnitB() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Type getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
