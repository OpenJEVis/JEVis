/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.driver.inputHandler;

import java.io.*;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Broder
 */
public class FileInputHandler extends InputHandler {

    public FileInputHandler(File file, Charset charset) {
        super(file, charset);
    }

    //input is file
    @Override
    public void convertInput() {
        System.out.println("--Convert Array Input--");
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
            Logger.getLogger(FileInputHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
