/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.add;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class AssetHelper {

    private String name = new String();
    private Double yearlyconsumption = 0.0;
    private String energysource = new String();
    private Double shareoftotalesconsumption = 0.0;
    private Double shareoftotalconsumption = 0.0;
    private Double co2emissions = 0.0;
    private Double co2shareoftotal = 0.0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnergysource() {
        return energysource;
    }

    public void setEnergysource(String energysource) {
        this.energysource = energysource;
    }

    public Double getYearlyconsumption() {
        return yearlyconsumption;
    }

    public void setYearlyconsumption(Double yearlyconsumption) {
        this.yearlyconsumption = yearlyconsumption;
    }

    public Double getShareoftotalconsumption() {
        return shareoftotalconsumption;
    }

    public void setShareoftotalconsumption(Double shareoftotalconsumption) {
        this.shareoftotalconsumption = shareoftotalconsumption;
    }

    public Double getCo2emissions() {
        return co2emissions;
    }

    public void setCo2emissions(Double co2emissions) {
        this.co2emissions = co2emissions;
    }

    public Double getCo2shareoftotal() {
        return co2shareoftotal;
    }

    public void setCo2shareoftotal(Double co2shareoftotal) {
        this.co2shareoftotal = co2shareoftotal;
    }

    public Double getShareoftotalesconsumption() {
        return shareoftotalesconsumption;
    }

    public void setShareoftotalesconsumption(Double shareoftotalesconsumption) {
        this.shareoftotalesconsumption = shareoftotalesconsumption;
    }


}
