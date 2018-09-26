package org.jevis.commons.driver.inputHandler;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Broder
 */
public class ArrayInputHandler extends InputHandler {
    private static final Logger logger = LogManager.getLogger(ArrayInputHandler.class);

    public ArrayInputHandler(Object[] input, Charset charset) {
        super(input, charset);
    }

    //rawInput ist Object[]
    @Override
    public void convertInput() {
        logger.info("--Convert Array Input--");
        for (int i = 0; i < ((List) _rawInput).size(); i++) {
            StringBuilder builder = new StringBuilder();
            Object[] o = (Object[]) ((List) _rawInput).get(i);
            for (int j = 0; j < o.length; j++) {
                String s = (String) o[j];
                builder.append(s);
            }
            _inputStream.add(new ByteArrayInputStream(builder.toString().getBytes()));
        }
    }
}
