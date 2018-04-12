/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.driver;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * The interface for the Converter. Each Converter object represents an
 * converter object in the JEVis System. This class is optional. If no converter
 * object is given, the system uses the GenericConverter. In the future this
 * functionality is maybe split into several more specific converter.
 *
 * @author Broder
 */
public interface Converter {

    /**
     * Converts the input.
     *
     * @param input
     */
    public void convertInput(InputStream input, Charset charset);

    /**
     * Gets the converted Input in the given class format with given charset.
     *
     * @param convertedClass
     * @param charset
     * @return
     */
    public Object getConvertedInput(Class convertedClass);

}
