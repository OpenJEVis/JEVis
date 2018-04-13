/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedatacollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jevis.api.JEVisObject;

/**
 *
 * @author bf
 */
public class ThreadHandler {

    private List<JEVisObject> _requests = Collections.synchronizedList(new ArrayList());
    private static List<Long> _activeThreads = Collections.synchronizedList(new ArrayList());

    public ThreadHandler(List<JEVisObject> requests) {
        for (JEVisObject object : requests) {
            _requests.add(object);
        }
    }

    synchronized public boolean hasRequest() {
        if (_requests.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    synchronized public JEVisObject getNextDataSource() {
        for (JEVisObject currentReq : _requests) {
            _requests.remove(currentReq);
            return currentReq;
        }
        return null;
    }

    synchronized public int getNumberActiveRequests() {
        return _activeThreads.size();
    }

    synchronized public void removeActiveRequest(Long threadID) {
        _activeThreads.remove(threadID);
    }

    synchronized public void addActiveThread(long threadid) {
        _activeThreads.add(threadid);
    }

    synchronized public List<Long> getActiveThreads() {
        return _activeThreads;
    }
}
