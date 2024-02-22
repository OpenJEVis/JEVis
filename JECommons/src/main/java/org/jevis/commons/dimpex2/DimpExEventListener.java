package org.jevis.commons.dimpex2;

import java.util.EventListener;

public interface DimpExEventListener extends EventListener {

    void fireEvent(DimpExEvent event);

}
