package org.jevis.commons.driver.inputHandler;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.impl.soap.SOAPMessage;
import org.jevis.commons.driver.Converter;
import org.w3c.dom.Document;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author broder
 */
public class GenericConverter implements Converter {
    private static final Logger logger = LogManager.getLogger(GenericConverter.class);

    private InputHandler _adapterHandler;

    @Override
    public void convertInput(InputStream input, Charset charset) {
        _adapterHandler = initializeInputHandler(input, charset);
        _adapterHandler.convertInput();
    }

    private InputHandler initializeInputHandler(Object input, Charset charset) {
        logger.info("class," + input.getClass().toString());
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

    @Override
    public Object getConvertedInput(Class convertedClass) {
        if (convertedClass == String.class) {
            return _adapterHandler.getStringInput();
        } else if (convertedClass == String[].class) {
            return _adapterHandler.getStringArrayInput();
        } else if (convertedClass == Document.class) {
            return _adapterHandler.getDocuments();
        }
        return null;
    }
}
