package org.jevis.jeconfig.tool;

import org.jevis.api.JEVisObject;

import java.util.List;


public class TreeInbreedFinder {

    public TreeInbreedFinder(List<JEVisObject> object) {

        object.forEach(object1 -> {
            System.out.println("Object: " + object1.getID());
            String path = "";
            getParent(1, path, object1);
            System.out.println("#-> " + path);
        });
    }


    private void getParent(int level, String path, JEVisObject object) {
        try {
            if (object.getID().equals(9271l)) {
                System.out.println(object);
            }

            level++;
            if (object != null) {
                path = path + "/" + object.getID();
                if (object.getParent() != null) {
                    getParent(level, path, object.getParent());
                    System.out.println(path);
                }
            }

        } catch (Exception ex) {
            ex.getMessage();
        }
    }

}
