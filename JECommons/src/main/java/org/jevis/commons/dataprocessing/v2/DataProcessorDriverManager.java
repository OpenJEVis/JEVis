/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.dataprocessing.v2;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;

/**
 *
 * @author Florian Simon
 */
public class DataProcessorDriverManager {

    public static Function loadDriver(JEVisObject obj) throws MalformedURLException, ClassNotFoundException, JEVisException, InstantiationException, IllegalAccessException {
        Function dp = null;

        //Workaround befor the driver loading is implementet
        if (obj.getAttribute("ID").getLatestSample().getValueAsString().equals("Transformer")) {
            dp = new TransformerProcessor();
        } else if (obj.getAttribute("ID").getLatestSample().getValueAsString().equals("Differential")) {
            dp = new DifferentialProcessor();
        } else if (obj.getAttribute("ID").getLatestSample().getValueAsString().equals("Raw Data")) {
            dp = new InputProcessor();
        }

        if (dp != null) {
            dp.setObject(obj);
        }

        return dp;

//        String driverClassName = obj.getAttribute(DataProcessorDriver.ATTRIBUTE_MAINCLASS).getLatestSample().getValueAsString();
        //        JEVisFile jarFile = obj.getAttribute(DataProcessorDriver.ATTRIBUTE_SOURCE_FILE).getLatestSample().getValueAsFile();
        //        boolean isEnabled = obj.getAttribute(DataProcessorDriver.ATTRIBUTE_ENABLED).getLatestSample().getValueAsBoolean();
        //
        //        System.out.println("jarFile: " + jarFile.getFilename());
        //
        ////        URL classUrl = new URL("file:///home/kent/eclipsews/SmallExample/bin/IndependentClass.class");
        //        URL classUrl = new URL("file://" + jarFile.getFilename());
        //        URL[] classUrls = {classUrl};
        //        URLClassLoader ucl = new URLClassLoader(classUrls);
        //        Class c = ucl.loadClass(driverClassName); // LINE 14
        //        for (Field f : c.getDeclaredFields()) {
        //            System.out.println("Field name" + f.getName());
        //        }
        //
        //        return (DataProcessor) c.newInstance();
    }

}
