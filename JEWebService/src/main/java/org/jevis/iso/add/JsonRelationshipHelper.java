package org.jevis.iso.add;

public class JsonRelationshipHelper {
    String name = "";
    Long from = 0L;
    Long to = 0L;
    Integer type = 0;

    public JsonRelationshipHelper(String name, Long from, Long to, Integer type) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
