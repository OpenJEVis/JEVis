package org.jevis.commons.driver.inputHandler;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.xmlbeans.impl.soap.SOAPMessage;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 *
 * @author Broder
 */
public class InputHandlerFactory {
    
    public static InputHandler getInputConverter(Object input) {
        Charset charset = Charset.defaultCharset();
        return getInputConverter(input, charset);
    }

    public static InputHandler getInputConverter(Object input, Charset charset) {
        if (input instanceof List) {
            List tmp = (List) input;
            if (tmp.isEmpty()) {
                return null;
            }
            Object o = tmp.get(0);
            if (o instanceof String) {
                return new StringInputHandler((List<String>) input, charset);
            }

            if (o instanceof List) {
                return new StringInputHandler((List<String>) o, charset);
            }

            if (o instanceof SOAPMessage) {
                return new SOAPMessageInputHandler((List<SOAPMessage>) input, charset);
            }
            return null;
        } else if (input instanceof Object[]) {
            return new ArrayInputHandler((Object[]) input, charset);
        } else if (input instanceof File) {
            return new FileInputHandler((File) input, charset);
        } else if (input instanceof InputStream) {
            return new InputStreamHandler((InputStream) input, charset);
        }
        return null;
    }
}
