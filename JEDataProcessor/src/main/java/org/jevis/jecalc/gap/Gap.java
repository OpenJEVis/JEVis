/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.gap;

import java.util.HashMap;
import org.jevis.jecalc.data.CleanInterval;
import java.util.List;
import java.util.Map;
import static org.jevis.jecalc.gap.Gap.GapAttribute.DEFAULT_VALUE;

/**
 *
 * @author broder
 */
public interface Gap {

    public void addInterval(CleanInterval currentInterval);

    public void setLastValue(Double rawValue);

    public void setFirstValue(Double lastValue);

    public List<CleanInterval> getIntervals();

    public Double getFirstValue();

    public Double getLastValue();

    public class GapStrategy {

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

    public enum GapMode {

        NONE, STATIC, INTERPOLATION, DEFAULT;

    }

    public enum GapAttribute {

        DEFAULT_VALUE;
    }
}
