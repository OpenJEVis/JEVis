/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.limits;

import org.jevis.jecalc.data.CleanInterval;

import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public interface LimitBreak {

    void addInterval(CleanInterval currentInterval);

    List<CleanInterval> getIntervals();

    Double getFirstValue();

    void setFirstValue(Double lastValue);

    Double getLastValue();

    void setLastValue(Double rawValue);

}
