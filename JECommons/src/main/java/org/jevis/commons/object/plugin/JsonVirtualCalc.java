/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.object.plugin;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @TODO: belogins into driver
 * @author Florian Simon
 */
@XmlRootElement(name = "calc")
public class JsonVirtualCalc {

    public String operator;
    private String version;
    private List<JsonInput> inputs;

    public List<JsonInput> getInputs() {
        return inputs;
    }

    @XmlElement(name = "inputs")
    public void setInputs(List<JsonInput> inputs) {
        this.inputs = inputs;
    }

    public VirtualSumData.Operator getOperatorAsEnum() {
        return VirtualSumData.Operator.valueOf(operator);
//        switch (operator) {
//            case "/":
//                return VirtualSumData.Operator.DIVIDED;
//            case "*":
//                return VirtualSumData.Operator.TIMES;
//            case "+":
//                return VirtualSumData.Operator.PLUS;
//            case "-":
//                return VirtualSumData.Operator.MINUS;
//            default:
//                return VirtualSumData.Operator.NONE;
//        }
    }

    @XmlElement(name = "operator")
    public String getOperator() {

        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @XmlElement(name = "version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
