/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.api;

import java.util.EventListener;

/**
 * @author fs
 */
public interface JEVisEventListener extends EventListener {

    void fireEvent(JEVisEvent event);

}
