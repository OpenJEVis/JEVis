package org.jevis.ecp;

import org.jevis.api.JEVisObject;

public class ECChannel {

    private JEVisObject source;
    private JEVisObject target;

    public ECChannel(JEVisObject source, JEVisObject target) {
        this.source = source;
        this.target = target;
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
