/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.ws.json;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This Clas will build the JSon output for an Error message.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement(name = "error")
public class JSonError {

    private int status;
    private int code;
    private String message;
    private String more_info;

    public JSonError(int status, int code, String message, String more_info) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.more_info = more_info;
    }

    public JSonError() {
    }

    /**
     * Get the HTMl status code.
     *
     * @return
     */
    @XmlElement(name = "status")
    public int getStatus() {
        return status;
    }

    /**
     * Set the HTML status code.
     *
     * @param status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Get the JEVis API error code
     *
     * @return
     */
    @XmlElement(name = "code")
    public int getCode() {
        return code;
    }

    /**
     * Set the JEVis API error code
     *
     * @param code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Get an human readable error discription.
     *
     * @return
     */
    @XmlElement(name = "message")
    public String getMessage() {
        return message;
    }

    /**
     * Set the human readable error discription
     *
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get an link to more informations to this specific error.
     *
     * @return
     */
    @XmlElement(name = "more_info")
    public String getMoreInfo() {
        return more_info;
    }

    /**
     * Set the link with more information about this specific error.
     *
     * @param more_info
     */
    public void setMoreInfo(String more_info) {
        this.more_info = more_info;
    }

}
