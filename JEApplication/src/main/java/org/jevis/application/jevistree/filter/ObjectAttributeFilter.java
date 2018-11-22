package org.jevis.application.jevistree.filter;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.jevistree.JEVisTreeRow;

public class ObjectAttributeFilter {

    public static final String NONE = "NONE";
    public static final String ALL = "*";

    private String attributeName = "";
    private String objectName = "";
    private String filterName = "Filter";

    public ObjectAttributeFilter(String filterName, String objectName, String attributeName) {
        this.attributeName = attributeName;
        this.objectName = objectName;
        this.filterName = filterName;
    }

    public String getFilterName() {
        return filterName;
    }

    public boolean showClass(String jevisclass) {
        if (this.objectName.equals(ALL)) {
            return true;
        }

        if (this.objectName.equals(NONE)) {
            return false;
        }

        if (objectName.equals(jevisclass)) {
            return true;
        }

        return false;
    }

    public boolean showObject(JEVisObject obj) throws JEVisException {
        return showClass(obj.getJEVisClassName());
    }

    /**
     * TODO: need additional parmether object to see if its the right object-attribute combo?
     *
     * @param attributeName
     * @return
     */
    public boolean showAttribute(String attributeName) {
        if (this.attributeName.equals(ALL)) {
            return true;
        }

        if (this.attributeName.equals(NONE)) {
            return false;
        }


        if (attributeName.equals(this.attributeName)) {
            return true;
//            if(objectName.equals(ALL)){
//                return true;
//            }

        }

        return false;
    }

    public boolean showAttribute(JEVisAttribute attribute) {
        return showAttribute(attribute.getName());
    }

    public boolean showCell(JEVisTreeRow row) throws JEVisException {
        if (row.getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
            return showAttribute(row.getJEVisAttribute());
        } else if (row.getType() == JEVisTreeRow.TYPE.OBJECT) {
            return showObject(row.getJEVisObject());
        }

        return false;
    }

    @Override
    public String toString() {
        return "ObjectAttributeFilter{" +
                "attributeName='" + attributeName + '\'' +
                ", objectName='" + objectName + '\'' +
                ", filterName='" + filterName + '\'' +
                '}';
    }

    public enum TYPE {
        OBJECT, ATTRIBUTE, TYPE
    }
}
