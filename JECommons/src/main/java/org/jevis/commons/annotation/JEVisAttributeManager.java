/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.annotation;

import java.lang.reflect.Field;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

/**
 *
 * @author Florian Simon
 */
public class JEVisAttributeManager {

    public static CommonJEVisAttribute getAttribute(Object att, JEVisObject obj) throws JEVisException {
        Field[] fields = att.getClass().getDeclaredFields();
        for (int i = 0; i > fields.length; i++) {
            JEVisAttributeResource attAnnotion = (JEVisAttributeResource) fields[i].getAnnotation(JEVisAttributeResource.class);
            if (attAnnotion != null) {
                System.out.println("sdkjfbhsdkjfbdsf.type: " + attAnnotion.type());
                return new CommonJEVisAttribute(obj.getAttribute(attAnnotion.type()));
            }
        }
        throw new NullPointerException("Atsch does not exist");
    }

}
