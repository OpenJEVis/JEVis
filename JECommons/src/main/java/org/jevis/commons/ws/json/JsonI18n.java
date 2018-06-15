package org.jevis.commons.ws.json;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "I18n")
public class JsonI18n {


    private String key;
    private String value;
    private String type;


    public JsonI18n() {
    }

    @XmlElement(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlElement(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(name = "key")
    public String getKey() {

        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
