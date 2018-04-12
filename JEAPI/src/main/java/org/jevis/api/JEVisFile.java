/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI.
 *
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.api;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface JEVisFile {

    void saveToFile(File file) throws IOException;

    void loadFromFile(File file) throws IOException;

    void setBytes(byte[] data);

    byte[] getBytes();

    String getFilename();

    void setFilename(String name);

    String getFileExtension();
    //TODO: ? is encoding come object?
//    void setEncoding(String encoding);
//    String getEncoding();
}
