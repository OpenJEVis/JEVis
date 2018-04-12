/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.json;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ZIPContent {

    private byte[] data;
    private String name;

    public static enum TYPE {

        CLASS, ICON
    };

    private TYPE type;

    /**
     *
     * @param data
     * @param name
     * @param type
     */
    public ZIPContent(byte[] data, String name, TYPE type) {
        this.data = data;
        this.name = name;
        this.type = type;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getName() {
        switch (type) {
            case CLASS:
                return name + ".jcf";
            case ICON:
                return name + ".icon";
            default:
                return name;
        }

    }

    public void setName(String name) {
        this.name = name;
    }

}
