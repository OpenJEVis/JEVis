package org.jevis.jedataprocessor.gap;

import java.util.HashMap;
import java.util.Map;

import static org.jevis.jedataprocessor.gap.GapAttribute.DEFAULT_VALUE;

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
