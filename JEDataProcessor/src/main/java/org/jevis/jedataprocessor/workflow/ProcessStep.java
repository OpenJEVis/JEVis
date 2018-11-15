/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.workflow;

import org.jevis.jedataprocessor.data.ResourceManager;

/**
 * @author broder
 */
public interface ProcessStep {

    void run(ResourceManager resourceManager) throws Exception;

}
