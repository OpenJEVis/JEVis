/*
  Copyright (C) 2009 - 2013 Envidatec GmbH <info@envidatec.com>

  This file is part of JEWebService.

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
package org.jevis.ws.sql.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.ws.sql.SQLDataSource;

import java.sql.PreparedStatement;

/**
 *
 * @author florian.simon@envidatec.com
 */
public class Service {

    private Logger logger = LogManager.getLogger(Service.class);

    private final SQLDataSource _connection;

    public Service(SQLDataSource ds) {
        _connection = ds;
    }


    //TODO: try-catch-finally
    public void cleanup() {
//        logger.info("AttributeTable.insert");
        String sql = "delete from relationship where startobject in (select id from object\n"
                + "where deletets is not null) or endobject in (select id from object where\n"
                + "deletets is not null);\n"
                + "delete from attribute where object in (select id from object where\n"
                + "deletets is not null);\n"
                + "delete from sample where object in (select id from object where deletets\n"
                + "is not null);\n"
                + "delete from object where deletets is not null;\n"
                + "delete from type where jevisclass not in (select name from objectclass);\n"
                + "delete from classrelationship where startclass not in (select name from\n"
                + "objectclass) or endclass not in (select name from objectclass);";

        try {
            PreparedStatement ps = _connection.getConnection().prepareStatement(sql);
            ps.executeQuery();

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

}
