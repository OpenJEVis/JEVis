package org.jevis.commons.export;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MetaObject {

    JsonObject object;

    public MetaObject(JsonObject obj) {
        this.object = obj;
    }

    public String getKey() {
        return String.valueOf(object.getId());
    }

    public JsonObject getObject() {
        return object;
    }

    public List<String> getRequrements() {
        List<String> keyList = new ArrayList<>();
        for (JsonAttribute att : object.getAttributes()) {
            //check based on GUI Type? hardcode test
            if (att.getType().equals("Input")) {
                if (att.getLatestValue() != null) {
                    keyList.add(att.getLatestValue().getValue().split(";")[0]);
                }
            }
        }
        return keyList;
    }

}
