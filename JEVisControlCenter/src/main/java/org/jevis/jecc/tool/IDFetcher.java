package org.jevis.jecc.tool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;

public class IDFetcher {
    private static final Logger logger = LogManager.getLogger(IDFetcher.class);

    public IDFetcher(JEVisObject obj) {
        List<JEVisObject> object = new ArrayList<>();
        object.add(obj);
        try {
            getChildrenIDS(object, obj);
            printIDs(object);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void getChildrenIDS(List<JEVisObject> objects, JEVisObject parent) throws JEVisException {

        for (JEVisObject object : parent.getChildren()) {
            try {
                objects.add(object);
                getChildrenIDS(objects, object);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void printIDs(List<JEVisObject> object) {
        logger.debug("==============");
        object.forEach(object1 -> {
            logger.debug(object1.getID() + ",");
        });
        logger.debug("==============");
    }

}



