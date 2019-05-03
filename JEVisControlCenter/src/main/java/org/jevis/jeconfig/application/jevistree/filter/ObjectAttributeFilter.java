package org.jevis.jeconfig.application.jevistree.filter;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;

public class ObjectAttributeFilter {

    public static final String NONE = "NONE";
    public static final String ALL = "*";

    private String attributeName = "";
    private String objectName = "";

    public ObjectAttributeFilter(String objectName, String attributeName) {
        this.attributeName = attributeName;
        this.objectName = objectName;
    }

    public boolean showClass(String jevisclass) {
        if (this.objectName.equals(ALL)) {
            return true;
        }

        if (this.objectName.equals(NONE)) {
            return false;
        }

        return objectName.equals(jevisclass);

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


        //            if(objectName.equals(ALL)){
        //                return true;
        //            }
        return attributeName.equals(this.attributeName);

    }

    public boolean showAttribute(JEVisAttribute attribute) {
        return showAttribute(attribute.getName());
    }

    public boolean showCell(JEVisTreeRow row) throws JEVisException {
        if (row.getType() == null) {
            return false;
        }

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
                '}';
    }

    public enum TYPE {
        OBJECT, ATTRIBUTE, TYPE
    }
}
