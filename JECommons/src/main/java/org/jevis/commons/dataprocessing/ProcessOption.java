/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement(name = "option")
public interface ProcessOption {

//    @XmlElement(name = "key")
//    String getKey();
    @XmlElement(name = "value")
    String getValue();

    void setValue(String value);

    String getKey();

}
