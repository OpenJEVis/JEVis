/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.driver.inputHandler;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 *
 * @author Broder
 */
public class StringInputHandler extends InputHandler {

    public StringInputHandler(List input, Charset charset) {
        super(input, charset);
    }
    //input is List<List<String>>

    @Override
    public void convertInput() {
        System.out.println("--convertiere String input--");
        List<String> input = (List<String>) _rawInput;
        for (String o : input) {
//            List tmp = (List) o;
//            for (Object m : tmp) {
//                String s = (String) m;
//            System.out.println("Value beim convert "+m);
                _inputStream.add(new ByteArrayInputStream(o.getBytes()));
//            }
        }
        System.out.println("Inputstream size " + _inputStream.size());
    }
}
