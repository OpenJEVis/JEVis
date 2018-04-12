/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.driver.inputHandler;

import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 *
 * @author bf
 */
public class SOAPMessageInputHandler extends InputHandler {

    public SOAPMessageInputHandler(List<SOAPMessage> input, Charset charset) {
        super(input, charset);
    }

    @Override
    public void convertInput() {

        List<SOAPMessage> input = (List<SOAPMessage>) _rawInput;
        for (SOAPMessage m : input) {
            try {
                _document.add(m.getSOAPBody().extractContentAsDocument());
            } catch (SOAPException ex) {
                Logger.getLogger(SOAPMessageInputHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
