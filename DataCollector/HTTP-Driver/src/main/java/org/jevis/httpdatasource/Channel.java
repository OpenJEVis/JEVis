/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.httpdatasource;

import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

/**
 * @author broder
 */
public class Channel {
    private String _path;
    private DateTime _lastReadout;
    private JEVisObject channelObject;

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

    public JEVisObject getChannelObject() {
        return channelObject;
    }

    public void setChannelObject(JEVisObject channelObject) {
        this.channelObject = channelObject;
    }
}
