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
package org.jevis.commons.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;

/**
 *
 * @author broder
 * @author NeroBurner
 */
public class ByteClassLoader extends URLClassLoader {

    private URL url;
    
    public ByteClassLoader(URL url) {
        super(new URL[] { url });
        this.url = url;
    }

    public static Class loadDriver(JEVisFile parserFile, String className) throws JEVisException, MalformedURLException, ClassNotFoundException, IOException {
        System.out.println("load Driver: "+className);
        
        // Write jar from JEVis to temporary file
        File tmpJar = File.createTempFile(parserFile.getFilename(), ".jar");
        FileOutputStream fos = new FileOutputStream(tmpJar);
        fos.write(parserFile.getBytes());
        
        // Load jar from temporary file
        URL url = tmpJar.toURI().toURL();
        System.out.println("jarURL: " + url.toString());
        ClassLoader cl = new ByteClassLoader(url);
        Class c = cl.loadClass(className);
        
        // Close temporary file
        fos.close();
        return c;
    }

}
