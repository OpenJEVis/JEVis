/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.workflow;

import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;

/**
 * @author broder
 */
public interface ProcessStep {

    void run(ResourceManager resourceManager) throws Exception;

    default String getNote(CleanInterval currentInterval) {
        String note = "";
        try {
            note += currentInterval.getTmpSamples().get(0).getNote();
        } catch (Exception e1) {
            try {
                note += currentInterval.getRawSamples().get(0).getNote();
            } catch (Exception e2) {
            }
        }
        if (note.equals("null")) {
            note = "No Note";
        }
        return note;
    }
}
