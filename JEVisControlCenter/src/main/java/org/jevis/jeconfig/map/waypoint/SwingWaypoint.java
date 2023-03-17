package org.jevis.jeconfig.map.waypoint;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A waypoint that is represented by a button on the map.
 *
 * @author Daniel Stahr
 */
public class SwingWaypoint extends DefaultWaypoint {

    private final JButton button;

    public SwingWaypoint(GeoPosition coord, Color color, String date) {
        super(coord);

        button = new RoundButton(color);
//        button.addMouseListener(new SwingWaypointMouseListener());

//        double roundLong = Math.round(coord.getLongitude() * 100.0) / 100.0;
//        double roundLat = Math.round(coord.getLatitude() * 100.0) / 100.0;
        double roundLong = coord.getLongitude();
        double roundLat = coord.getLatitude();
        button.setToolTipText("<html>Long:" + roundLong + "<br>" + "Lat:" + roundLat + "<br>Date:" + date + "</html>");
    }

    public SwingWaypoint(GeoPosition coord, Color color) {
        super(coord);

        button = new RoundButton(color);
//        button.addMouseListener(new SwingWaypointMouseListener());

//        double roundLong = Math.round(coord.getLongitude() * 100.0) / 100.0;
//        double roundLat = Math.round(coord.getLatitude() * 100.0) / 100.0;
        double roundLong = coord.getLongitude();
        double roundLat = coord.getLatitude();
        button.setToolTipText("<html>Long:" + roundLong + "<br>" + "Lat:" + roundLat + "</html>");
    }

    public JButton getButton() {
        return button;
    }

    private class SwingWaypointMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            JOptionPane.showMessageDialog(button, "You clicked on a button");
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}
