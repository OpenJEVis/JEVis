/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEWebService.
 *
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.rest;

/**
 * Container for the Icons
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ClassIcon {

    private byte[] _iconBytes;
    private String _class;

    public ClassIcon(String _class, byte[] _iconBytes) {
        this._iconBytes = _iconBytes;
        this._class = _class;
    }

    public byte[] getIconBytes() {
        return _iconBytes;
    }

    public void setIconBytes(byte[] _iconBytes) {
        this._iconBytes = _iconBytes;
    }

    public String getJEVisClass() {
        return _class;
    }

    public void setJEVisClass(String _class) {
        this._class = _class;
    }

}
