/*
  Copyright (C 2013 - 2016 Envidatec GmbH <info@envidatec.com>
  <p>
  This file is part of JEWebService.
  <p>
  JEWebService is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation in version 3.
  <p>
  JEWebService is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.
  <p>
  You should have received a copy of the GNU General Public License along with
  JEWebService. If not, see <http://www.gnu.org/licenses/>.
  <p>
  JEWebService is part of the OpenJEVis project, further project information
  are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.iso.add.JEVisClasses;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class ISO50001 {
    private static JEVisClasses jc;
    private Organisation _Organisation;
    private final SQLDataSource ds;

    public ISO50001(SQLDataSource input) throws Exception {

        jc = new JEVisClasses(input);

        /* Fill the Organisation Class with JEVis Data */

        this._Organisation = new Organisation(input);
        this.ds = input;

    }

    public static JEVisClasses getJc() {
        return jc;
    }

    public Organisation getOrganisation() {
        return _Organisation;
    }

    public void setOrganisation(Organisation _Organisation) {
        this._Organisation = _Organisation;
    }

    public SQLDataSource getDs() {
        return ds;
    }


}
