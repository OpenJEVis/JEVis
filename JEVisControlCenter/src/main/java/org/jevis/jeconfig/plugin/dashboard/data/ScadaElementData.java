package org.jevis.jeconfig.plugin.dashboard.data;

public class ScadaElementData {

    private Long objectID;
    private String attribute;
    private String bgColor;
    private String type;
    private double xPos;
    private double yPos;


    public double getxPos() {
        return xPos;
    }

    public void setxPos(double xPos) {
        this.xPos = xPos;
    }

    public double getyPos() {
        return yPos;
    }

    public void setyPos(double yPos) {
        this.yPos = yPos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getObjectID() {
        return objectID;
    }

    public void setObjectID(Long objectID) {
        this.objectID = objectID;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }
}
