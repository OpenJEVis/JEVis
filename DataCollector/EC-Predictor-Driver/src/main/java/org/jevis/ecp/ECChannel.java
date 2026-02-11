package org.jevis.ecp;

import org.jevis.api.JEVisObject;

public class ECChannel {

    private final JEVisObject channelObject;
    private final JEVisObject source;
    private final JEVisObject target;

    public ECChannel(JEVisObject channelObject, JEVisObject source, JEVisObject target) {
        this.channelObject = channelObject;
        this.source = source;
        this.target = target;
    }

    public JEVisObject getChannelObject() {
        return channelObject;
    }

    public JEVisObject getSource() {
        return source;
    }

    public JEVisObject getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "org.jevis.ecpdatasource.ECChannel{" +
                "source=" + source +
                ", target=" + target +
                '}';
    }
}
