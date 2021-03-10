package org.jevis.jeconfig.tool;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;

public class IDFetcher {


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
        System.out.println("==============");
        object.forEach(object1 -> {
            System.out.print(object1.getID() + ",");
        });
        System.out.println("==============");
    }

}



