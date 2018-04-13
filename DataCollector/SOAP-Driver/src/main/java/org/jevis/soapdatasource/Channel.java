/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.soapdatasource;

import org.joda.time.DateTime;

/**
 *
 * @author bf
 */
public class Channel {

    private String _path;
    private DateTime _lastReadout;
    private String _template;

    public String getTemplate() {
        return _template;
    }

    public void setTemplate(String _template) {
        this._template = _template;
    }

    public String getPath() {
        return _path;
    }

    public void setPath(String _path) {
        this._path = _path;
    }

    public DateTime getLastReadout() {
        return _lastReadout;
    }

    public void setLastReadout(DateTime _lastReadout) {
        this._lastReadout = _lastReadout;
    }

}
