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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.jevis.api.JEVisDataSource;

public class ConnectionData {

    // Define the states of a connection
    public final static int NOT_DEFINED = 0;
    public final static int DEFINITION_FAIL = 1;
    public final static int DEFINED = 2;
    public final static int CONNECTION_FAIL = 3;
    public final static int CONNECTING = 4;
    public final static int CONNECTED = 5;
    public final static int LOGIN_FAIL = 6;
    public final static int LOGGING_IN = 7;
    public final static int LOGGED_IN = 8;

    // Define member attributes
    protected IntegerProperty status = new SimpleIntegerProperty(NOT_DEFINED);
    protected JEVisDataSource ds = null;
    private StringProperty name = new SimpleStringProperty();
    private StringProperty address = new SimpleStringProperty();
    private StringProperty port = new SimpleStringProperty();
    private StringProperty db = new SimpleStringProperty();
    private StringProperty dbUser = new SimpleStringProperty();
    private StringProperty dbPass = new SimpleStringProperty();

    public StringProperty getName() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public StringProperty getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port.set(port);
    }

    public StringProperty getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db.set(db);
    }

    public StringProperty getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser.set(dbUser);
    }

    public StringProperty getDbPass() {
        return dbPass;
    }

    public void setDbPass(String dbPass) {
        this.dbPass.set(dbPass);
    }

    public IntegerProperty getStatusProperty() {
        return status;
    }

    public int getStatus() {
        return status.intValue();
    }

    public void setStatus(int status) {
        this.status.setValue(status);
    }
}
