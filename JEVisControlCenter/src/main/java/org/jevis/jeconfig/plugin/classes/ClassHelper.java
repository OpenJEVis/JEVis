/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.classes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ClassHelper {

    private static final String STRING = "String";
    private static final String BOOLEAN = "Boolean";
    private static final String DOUBLE = "Double";
    private static final String FILE = "File";
    private static final String LONG = "Long";
    private static final String SELECTION = "Selection";
    private static final String MULTI_SELECTION = "Multy Selection";
    private static final String PASSWORD_PBKDF2 = "Password PBKDF2";

    public static ObservableList<String> getAllPrimitiveTypes() {
        return FXCollections.observableArrayList(
                STRING, DOUBLE, LONG, BOOLEAN, FILE, SELECTION, MULTI_SELECTION, PASSWORD_PBKDF2);
    }

    public static String getNameforPrimitiveType(JEVisType type) {
        try {
            switch (type.getPrimitiveType()) {
                case JEVisConstants.PrimitiveType.STRING:
                    return STRING;
                case JEVisConstants.PrimitiveType.BOOLEAN:
                    return BOOLEAN;
                case JEVisConstants.PrimitiveType.DOUBLE:
                    return DOUBLE;
                case JEVisConstants.PrimitiveType.FILE:
                    return FILE;
                case JEVisConstants.PrimitiveType.LONG:
                    return LONG;
                case JEVisConstants.PrimitiveType.SELECTION:
                    return SELECTION;
                case JEVisConstants.PrimitiveType.MULTI_SELECTION:
                    return MULTI_SELECTION;
                case JEVisConstants.PrimitiveType.PASSWORD_PBKDF2:
                    return PASSWORD_PBKDF2;
            }
        } catch (Exception ex) {
            return "";
        }
        return "Unknown";
    }

    public static int getIDforPrimitiveType(String name) throws JEVisException {
        switch (name) {
            case STRING:
                return JEVisConstants.PrimitiveType.STRING;
            case BOOLEAN:
                return JEVisConstants.PrimitiveType.BOOLEAN;
            case DOUBLE:
                return JEVisConstants.PrimitiveType.DOUBLE;
            case FILE:
                return JEVisConstants.PrimitiveType.FILE;
            case LONG:
                return JEVisConstants.PrimitiveType.LONG;
            case SELECTION:
                return JEVisConstants.PrimitiveType.SELECTION;
            case MULTI_SELECTION:
                return JEVisConstants.PrimitiveType.MULTI_SELECTION;
            case PASSWORD_PBKDF2:
                return JEVisConstants.PrimitiveType.PASSWORD_PBKDF2;
        }
        return -1;
    }
}
