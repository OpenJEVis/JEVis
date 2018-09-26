/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.driver.inputHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Broder
 */
public class StringInputHandler extends InputHandler {
    private static final Logger logger = LogManager.getLogger(StringInputHandler.class);

    public StringInputHandler(List input, Charset charset) {
        super(input, charset);
    }
    //input is List<List<String>>

    @Override
    public void convertInput() {
        logger.info("--convertiere String input--");
        List<String> input = (List<String>) _rawInput;
        for (String o : input) {
//            List tmp = (List) o;
//            for (Object m : tmp) {
//                String s = (String) m;
//            logger.info("Value beim convert "+m);
            _inputStream.add(new ByteArrayInputStream(o.getBytes()));
//            }
        }
        logger.info("Inputstream size " + _inputStream.size());
    }
}
