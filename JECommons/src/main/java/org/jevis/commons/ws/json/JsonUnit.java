/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.ws.json;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simplified JEVisUnit representation in Json.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement(name = "Unit")
public class JsonUnit {

    private String formula;
    private String label;
    private String prefix;
//    private String function;
//    private List<JsonUnit> subunits;

    @XmlElement(name = "formula")
    public String getFormula() {
        return formula;
    }

    @XmlElement(name = "label")
    public String getLabel() {
        return label;
    }

    @XmlElement(name = "prefix")
    public String getPrefix() {
        return prefix;
    }

//    @XmlElement(name = "function")
//    public String getFunction() {
//        return function;
//    }
//
//    @XmlElement(name = "subunits")
//    public List<JsonUnit> getSubUnits() {
//        return subunits;
//    }
    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

//    public void setFunction(String function) {
//        this.function = function;
//    }
//
//    public void setSubUnits(List<JsonUnit> subUnits) {
//        this.subunits = subUnits;
//    }
}
