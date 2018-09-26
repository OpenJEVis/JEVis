/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.driver.inputHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Broder
 */
public class FileInputHandler extends InputHandler {
    private static final Logger logger = LogManager.getLogger(FileInputHandler.class);

    public FileInputHandler(File file, Charset charset) {
        super(file, charset);
    }

    //input is file
    @Override
    public void convertInput() {
        logger.info("--Convert Array Input--");
        try {
            File file = (File) _rawInput;
            FileReader reader = null;
            reader = new FileReader(file);
            BufferedReader buffer = new BufferedReader(reader);
            String line = buffer.readLine();
            while (line != null) {
                _inputStream.add(new ByteArrayInputStream(line.getBytes()));
                line = buffer.readLine();
            }
        } catch (IOException ex) {
            logger.fatal(ex);
        }
    }
}
