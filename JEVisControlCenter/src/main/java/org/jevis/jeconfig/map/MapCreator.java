/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.map;

import org.jevis.jeconfig.map.waypoint.SwingWaypoint;
import org.jevis.jeconfig.map.waypoint.SwingWaypointOverlayPainter;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 *
 * @author broder
 */
public class MapCreator {

    private JXMapViewer mapViewer;

    public MapCreator(JXMapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }

//    public void drawMap(Set<MapPlugin.DataModel> routesRaw) {
//        List<GPSRoute> routes = new ArrayList<>();
//        for (MapPlugin.DataModel currentRoute : routesRaw) {
//
//            DateTime selectedEnd = currentRoute.getSelectedEnd();
//            DateTime selectedStart = currentRoute.getSelectedStart();
//
//            List<JEVisSample> latSamples = currentRoute.getLatitudeAttribute().getSamples(selectedStart, selectedEnd);
//            List<JEVisSample> longSamples = currentRoute.getLongitudeAttribute().getSamples(selectedStart, selectedEnd);
//            try {
//                GPSRoute gpsRoute = getGPSSamples(latSamples, longSamples);
//                gpsRoute.setColor(currentRoute.getColor());
//                routes.add(gpsRoute);
//            } catch (JEVisException ex) {
//                Logger.getLogger(MapViewPlugin.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        drawMap(routes);
//    }
    private GeoPosition getFocus(List<GeoPosition> positions) {

        double allLat = 0;
        double allLong = 0;
        for (GeoPosition currentPos : positions) {
            allLat += currentPos.getLatitude();
            allLong += currentPos.getLongitude();
        }

        double avgLat = allLat / (double) positions.size();
        double avgLong = allLong / (double) positions.size();

        GeoPosition focus = new GeoPosition(avgLat, avgLong);
        return focus;
    }

    public void drawMap(List<GPSRoute> gpsRoutes) {
        mapViewer.removeAll();

        // Create a TileFactoryInfo for OpenStreetMap
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        mapViewer.setTileFactory(tileFactory);

        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        List<GeoPosition> allpositions = new ArrayList<>();
        List<SwingWaypoint> allWaypoints = new ArrayList<>();
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        for (GPSRoute currentRoute : gpsRoutes) {
            java.awt.Color color = ColorCalc.getColorFromJavaFX(currentRoute.getColor());

            // Create waypoints from the geo-positions
            Set<SwingWaypoint> waypoints = new HashSet<>();

            List<GeoPosition> positions = new ArrayList<>();
            for (GPSSample sample : currentRoute.getGpsSample()) {
                GeoPosition curPos = new GeoPosition(sample.getLatitude(), sample.getLongitude());
                positions.add(curPos);

                SwingWaypoint defaultWaypoint = new SwingWaypoint(curPos, color, sample.getDate());
                waypoints.add(defaultWaypoint);
            }
            allWaypoints.addAll(waypoints);
            allpositions.addAll(positions);

            // Create a track from the geo-positions
            RoutePainter routePainter = new RoutePainter(positions, color);
            // Set the focus
            mapViewer.zoomToBestFit(new HashSet<GeoPosition>(positions), 0.7);
            painters.add(routePainter);
            WaypointPainter<SwingWaypoint> swingWaypointPainter = new SwingWaypointOverlayPainter();
            swingWaypointPainter.setWaypoints(waypoints);
            painters.add(swingWaypointPainter);
        }

        mapViewer.setCenterPosition(getFocus(allpositions));
        mapViewer.zoomToBestFit(new HashSet<>(allpositions), 0.7);

        // Set the overlay painter
//        mapViewer.setOverlayPainter(swingWaypointPainter);
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);

        // Add the JButtons to the map viewer
        for (SwingWaypoint w : allWaypoints) {
            mapViewer.add(w.getButton());
//            w.getButton().setVisible(true);
        }
        mapViewer.setVisible(false);
        mapViewer.setVisible(true);

    }

//    private GPSRoute getGPSSamples(List<JEVisSample> latSamples, List<JEVisSample> longSamples) throws JEVisException {
//        GPSRoute route = new GPSRoute();
//        if (latSamples.size() != longSamples.size()) {
//            logger.info("long and lat samplelist differ in length");
//            return route;
//        }
//        List<GPSSample> gpsSamples = new ArrayList<>();
//        for (int i = 0; i < latSamples.size(); i++) {
//            DateTime timestamp = latSamples.get(i).getTimestamp();
//            Double latValue = latSamples.get(i).getValueAsDouble();
//            Double longValue = longSamples.get(i).getValueAsDouble();
//            GPSSample sample = new GPSSample();
//            sample.setLatitude(latValue);
//            sample.setLongitude(longValue);
//            sample.setTimestamp(timestamp);
//            gpsSamples.add(sample);
//        }
//        route.setGpsSample(gpsSamples);
//        return route;
//    }
    public void drawEmtyMap() {
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);

        mapViewer.setTileFactory(tileFactory);
        GeoPosition frankfurt = new GeoPosition(50, 7, 0, 8, 41, 0);

        // Set the focus
//        mapViewer.zoomToBestFit(, 0.7);
        mapViewer.setAddressLocation(frankfurt);

        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Create waypoints from the geo-positions
        Set<SwingWaypoint> waypoints = new HashSet<SwingWaypoint>(Arrays.asList());

        // Create a track from the geo-positions
        List<GeoPosition> track = Arrays.asList();
        RoutePainter routePainter = new RoutePainter(track, Color.RED);

        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);
        WaypointPainter<SwingWaypoint> swingWaypointPainter = new SwingWaypointOverlayPainter();
        swingWaypointPainter.setWaypoints(waypoints);
        painters.add(swingWaypointPainter);

        // Set the overlay painter
//        mapViewer.setOverlayPainter(swingWaypointPainter);
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);

        // Add the JButtons to the map viewer
        for (SwingWaypoint w : waypoints) {
            mapViewer.add(w.getButton());
        }
        mapViewer.setVisible(false);
        mapViewer.setVisible(true);

    }

    public void drawDefaultMap() {

        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);

        mapViewer.setTileFactory(tileFactory);

        GeoPosition frankfurt = new GeoPosition(50, 7, 0, 8, 41, 0);
        GeoPosition wiesbaden = new GeoPosition(50, 5, 0, 8, 14, 0);
        GeoPosition mainz = new GeoPosition(50, 0, 0, 8, 16, 0);
        GeoPosition darmstadt = new GeoPosition(49, 52, 0, 8, 39, 0);
        GeoPosition offenbach = new GeoPosition(50, 6, 0, 8, 46, 0);

        // Set the focus
//        mapViewer.zoomToBestFit(, 0.7);
        mapViewer.setAddressLocation(frankfurt);

        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Create waypoints from the geo-positions
        Set<SwingWaypoint> waypoints = new HashSet<SwingWaypoint>(Arrays.asList(
                new SwingWaypoint(frankfurt, Color.RED),
                new SwingWaypoint(wiesbaden, Color.RED),
                new SwingWaypoint(mainz, Color.RED),
                new SwingWaypoint(darmstadt, Color.RED),
                new SwingWaypoint(offenbach, Color.RED)));

        // Create a track from the geo-positions
        List<GeoPosition> track = Arrays.asList(frankfurt, wiesbaden, mainz, darmstadt, offenbach);
        RoutePainter routePainter = new RoutePainter(track, Color.RED);

        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);
        WaypointPainter<SwingWaypoint> swingWaypointPainter = new SwingWaypointOverlayPainter();
        swingWaypointPainter.setWaypoints(waypoints);
        painters.add(swingWaypointPainter);

        // Set the overlay painter
//        mapViewer.setOverlayPainter(swingWaypointPainter);
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);

        // Add the JButtons to the map viewer
        for (SwingWaypoint w : waypoints) {
            mapViewer.add(w.getButton());
        }
        mapViewer.setVisible(false);
        mapViewer.setVisible(true);
    }
}
