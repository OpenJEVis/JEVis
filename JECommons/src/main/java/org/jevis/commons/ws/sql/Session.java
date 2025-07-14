package org.jevis.commons.ws.sql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jevis.commons.ws.json.JsonObject;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@XmlRootElement(name = "Session")
public class Session {

    private boolean isEntraID = false;
    private JsonObject user;
    private String displayName = "-";
    private String id = UUID.randomUUID().toString();
    private String entraToken;
    private JEVisUserSQL jevisUser;

    public Session() {
    }

    public Session(boolean isEntraID, JsonObject user, String displayName, String entraToken) {
        this.isEntraID = isEntraID;
        this.user = user;
        this.displayName = displayName;
        this.entraToken = entraToken;

    }

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    @XmlElement(name = "name")
    public String getDisplayName() {
        return displayName;
    }

    @XmlElement(name = "user")
    public JsonObject getUser() {
        return user;
    }

    public void setUser(JsonObject user) {
        this.user = user;
    }

    @JsonIgnore
    public JEVisUserSQL getJevisUser() {
        return jevisUser;
    }

    public void setJevisUser(JEVisUserSQL jevisUser) {
        this.jevisUser = jevisUser;
    }

    @XmlElement(name = "token")
    public String getEntraToken() {
        return entraToken;
    }

    @Override
    public String toString() {
        return "Session{" +
                "isEntraID=" + isEntraID +
                ", displayName='" + displayName + '\'' +
                ", id='" + id + '\'' +
                ", entraToken='" + entraToken + '\'' +
                '}';
    }
}
