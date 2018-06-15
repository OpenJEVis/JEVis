/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.gap;

import org.jevis.jecalc.data.CleanInterval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jevis.jecalc.gap.Gap.GapAttribute.DEFAULT_VALUE;

/**
 * @author broder
 */
public interface Gap {

    void addInterval(CleanInterval currentInterval);

    List<CleanInterval> getIntervals();

    Double getFirstValue();

    void setFirstValue(Double lastValue);

    Double getLastValue();

    void setLastValue(Double rawValue);

    enum GapMode {

        NONE, STATIC, INTERPOLATION, DEFAULT

    }

    enum GapAttribute {

        DEFAULT_VALUE
    }

    class GapStrategy {

        private final GapMode gapMode;
        private final Map<GapAttribute, String> attributeMap = new HashMap<>();

        public GapStrategy(String gapStrategy) {
            String gapModeString = gapStrategy.split(";")[0];
            gapMode = GapMode.valueOf(gapModeString.toUpperCase());
            String[] split = gapStrategy.split(";");
            for (int i = 1; i < split.length; i++) {
                attributeMap.put(DEFAULT_VALUE, split[i]);
            }
        }

        public GapMode getGapMode() {
            return gapMode;
        }

        public String getValue(GapAttribute gapAttribute) {
            return attributeMap.get(gapAttribute);
        }

    }
}
