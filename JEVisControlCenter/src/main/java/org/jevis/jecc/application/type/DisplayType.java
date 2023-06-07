/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.application.type;

/**
 * @author fs
 */
public class DisplayType {

    private String id;
    private int primitivType;

    public DisplayType(String id, int primitivType) {
        this.id = id;
        this.primitivType = primitivType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrimitivType() {
        return primitivType;
    }

    public void setPrimitivType(int primitivType) {
        this.primitivType = primitivType;
    }

}
