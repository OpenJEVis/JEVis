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
package org.jevis.application.login;

import org.jevis.application.connection.Connection;
import java.util.LinkedList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;

import org.jevis.application.application.PreloadedApplication;

public class Model {

    // Singleton Instance

    private static Model instance = new Model();

    // Singleton Constructor
    private Model() {
        // make constructor private
    }

    // Singleton get Instance
    public static Model getInstance() {
        return instance;
    }

    private DoubleProperty loadProgress = new SimpleDoubleProperty(0.0);
    private StringProperty userName = new SimpleStringProperty();
    private StringProperty password = new SimpleStringProperty();
    private PreloadedApplication consumer = null;
    private Stage stage = null;
    private Connection activeConnection = null;

    // private Connections connections = new Connections();
    private List<Connection> connections = new LinkedList<Connection>();

    public DoubleProperty getLoadProgressProperty() {
        return loadProgress;
    }

    public double getLoadProgress() {
        return loadProgress.get();
    }

    public void setLoadProgress(double value) {
        loadProgress.set(value);
    }

    public StringProperty getUserNameProperty() {
        return userName;
    }

    public String getUserName() {
        return userName.getValueSafe();
    }

    public void setUserName(String name) {
        userName.set(name);
    }

    public StringProperty getPasswordProperty() {
        return password;
    }

    public String getPassword() {
        return password.getValueSafe();
    }

    public PreloadedApplication getConsumer() {
        return consumer;
    }

    public void setConsumer(PreloadedApplication consumer) {
        this.consumer = consumer;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public Connection getActiveConnection() {
        return activeConnection;
    }

    public void setActiveConnection(Connection activeConnection) {
        this.activeConnection = activeConnection;
    }

}
