/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.application.connection;

import org.jevis.api.JEVisException;
import org.jevis.api.sql.JEVisDataSourceSQL;

public class DummyConnection {

    public void connect() {
        JEVisDataSourceSQL ds = null;
        try {
            ds = new JEVisDataSourceSQL("openjevis.org", "3306", "jevis", "jevis", "jevistest");
        } catch (JEVisException e) {
            System.out.println("DEFINE DATASOURCE FAILURE");
            e.printStackTrace();
        }

        if (ds != null) {
            if (ds.connectDB()) {
                System.out.println("DB CONNECTION ESTABLISHED");
            }
        }

        if (ds != null) {
            try {
                ds.connect("Sys Admin", "jevis");
                System.out.println("LOG IN SUCCESSFUL");
            } catch (JEVisException e) {
                System.out.println("USER LOGIN FAILURE");
                e.printStackTrace();
            }
        }
    }
}
