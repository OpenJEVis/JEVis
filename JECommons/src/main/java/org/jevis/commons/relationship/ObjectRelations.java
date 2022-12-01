package org.jevis.commons.relationship;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;

public class ObjectRelations {
    private static final Logger logger = LogManager.getLogger(ObjectRelations.class);
    private final JEVisDataSource ds;

    public ObjectRelations(JEVisDataSource ds) {
        this.ds = ds;
    }

    public JEVisObject getBuildingParent(JEVisObject object) throws JEVisException {
        JEVisClass buildingClass = ds.getJEVisClass("Building");
        for (JEVisObject parent : object.getParents()) {
            if (parent.getJEVisClass().equals(buildingClass)) {
                return parent;
            } else {
                return getBuildingParent(parent);
            }
        }
        return null;
    }

    public JEVisObject getOrganisationParent(JEVisObject object) throws JEVisException {
        JEVisClass organizationClass = ds.getJEVisClass("Organization");
        for (JEVisObject parent : object.getParents()) {
            if (parent.getJEVisClass().equals(organizationClass)) {
                return parent;
            } else {
                return getOrganisationParent(parent);
            }
        }
        return null;
    }

    public String getObjectPath(JEVisObject object) {
        String s = "";
        try {
            JEVisObject organisation = getOrganisationParent(object);
            if (organisation != null) {
                JEVisObject motherOrganisation = getOrganisationParent(organisation);
                if (motherOrganisation != null) {
                    s += motherOrganisation.getName();
                    s += " \\ ";
                }

                s += organisation.getName();
                s += " \\ ";
            }

            JEVisObject building = getBuildingParent(object);
            if (building != null) {
                s += building.getName();
                s += " \\ ";
            }
        } catch (Exception e) {
        }

        return s;
    }

    public String getRelativePath(JEVisObject object) {

        StringBuilder s = new StringBuilder();
        JEVisObject primaryParent = getPrimaryBuildingParent(object);
        List<JEVisObject> objectList = new ArrayList<>();

        createAnalysesDirectoryList(objectList, primaryParent, object);

        for (int i = objectList.size() - 1; i > 0; i--) {
            s.append(objectList.get(i).getName());
            s.append(" \\ ");
        }

        return s.toString();
    }

    private void createAnalysesDirectoryList(List<JEVisObject> objects, JEVisObject primaryParent, JEVisObject object) {
        try {
            for (JEVisObject parent : object.getParents()) {
                if (!parent.equals(primaryParent)) {
                    objects.add(parent);
                    createAnalysesDirectoryList(objects, primaryParent, parent);
                } else break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JEVisObject getPrimaryBuildingParent(JEVisObject object) {
        JEVisObject primaryParent = null;
        try {
            for (JEVisObject dir : object.getParents()) {
                if (dir.getJEVisClassName().equals("Building")) {
                    primaryParent = dir;
                    break;
                } else {
                    primaryParent = getPrimaryBuildingParent(dir);
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        return primaryParent;
    }
}
