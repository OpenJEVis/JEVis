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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class JEVisException extends Exception {

    private static final boolean _debug = true;
    private int _code = 0;
    private String _message = "Missing Exception message";
    Logger logger = LoggerFactory.getLogger(JEVisException.class);

    /**
     *
     * @param message
     * @param faultCode
     */
    public JEVisException(String message, int faultCode) {
        super(message);
        _message = message;
        _code = faultCode;
//        debug(null);
    }

    public JEVisException(String message, int faultCode, Throwable cause) {
        super(message, cause);
        _message = message;
        _code = faultCode;
        debug(cause);
    }

    public int getCode() {
        return _code;
    }

    //TODo, add translatable interface?
    private void debug(Throwable cause) {
        logger.error("[{}] {}", _code, _message);
    }
}
