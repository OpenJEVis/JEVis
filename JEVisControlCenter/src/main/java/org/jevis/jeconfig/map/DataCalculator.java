/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.application.jevistree.plugin.MapPlugin;
import org.joda.time.DateTime;

/**
 *
 * @author broder
 */
public class DataCalculator {

    public List<GPSRoute> calcRoues(Set<MapPlugin.DataModel> routesRaw) {
        List<GPSRoute> routes = new ArrayList<>();
        for (MapPlugin.DataModel currentRoute : routesRaw) {

            DateTime selectedEnd = currentRoute.getSelectedEnd();
            DateTime selectedStart = currentRoute.getSelectedStart();

            List<JEVisSample> latSamples = currentRoute.getLatitudeAttribute().getSamples(selectedStart, selectedEnd);
            List<JEVisSample> longSamples = currentRoute.getLongitudeAttribute().getSamples(selectedStart, selectedEnd);
            try {
                GPSRoute gpsRoute = getGPSSamples(latSamples, longSamples);
                gpsRoute.setColor(currentRoute.getColor());
                gpsRoute.setName(currentRoute.getObject().getParents().get(0).getName());
                routes.add(gpsRoute);
            } catch (JEVisException ex) {
                Logger.getLogger(MapViewPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return routes;
    }

    private GPSRoute getGPSSamples(List<JEVisSample> latSamples, List<JEVisSample> longSamples) throws JEVisException {
        GPSRoute route = new GPSRoute();
        if (latSamples.size() != longSamples.size()) {
            System.out.println("long and lat samplelist differ in length");
            return route;
        }
        List<GPSSample> gpsSamples = new ArrayList<>();
        for (int i = 0; i < latSamples.size(); i++) {
            DateTime timestamp = latSamples.get(i).getTimestamp();
            Double latValue = latSamples.get(i).getValueAsDouble();
            Double longValue = longSamples.get(i).getValueAsDouble();
            GPSSample sample = new GPSSample(latValue,longValue,timestamp);
            gpsSamples.add(sample);
        }
        route.setGpsSample(gpsSamples);
        return route;
    }
}
