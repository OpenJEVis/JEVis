/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.json;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jevis.api.JEVisOption;

/**
 *
 * @author Florian Simon
 */
@XmlRootElement(name = "option")
public class JsonOption {

    //all variables are null so that the elements will be empty in the json string
    String key;
    String value;
    String description;
    JsonOption _parent = null;
    List<JsonOption> options = null;

    public JsonOption() {
    }

    public JsonOption(JEVisOption opt) {
        key = opt.getKey();
        value = opt.getValue();
        description = opt.getDescription();
//        _parent = opt.getParent();
        if (!opt.getOptions().isEmpty()) {
            options = new ArrayList<>();
        }
        for (JEVisOption child : opt.getOptions()) {
            options.add(new JsonOption(child));
        }

    }

    @XmlElement(name = "options")
    public List<JsonOption> getChildren() {
        return options;
    }

    public void setChildren(List<JsonOption> children) {
        this.options = children;
    }

    @XmlElement(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlElement(name = "key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @XmlElement(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
