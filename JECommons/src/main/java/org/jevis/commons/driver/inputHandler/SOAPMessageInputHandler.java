/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.driver.inputHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.impl.soap.SOAPException;
import org.apache.xmlbeans.impl.soap.SOAPMessage;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author bf
 */
public class SOAPMessageInputHandler extends InputHandler {
    private static final Logger logger = LogManager.getLogger(SOAPMessageInputHandler.class);

    public SOAPMessageInputHandler(List<SOAPMessage> input, Charset charset) {
        super(input, charset);
    }

    @Override
    public void convertInput() {

        List<SOAPMessage> input = (List<SOAPMessage>) _rawInput;
        for (SOAPMessage m : input) {
            try {
                _document.add(m.getSOAPBody().getOwnerDocument());
            } catch (SOAPException ex) {
                logger.fatal(ex);
            }
        }
    }

}
