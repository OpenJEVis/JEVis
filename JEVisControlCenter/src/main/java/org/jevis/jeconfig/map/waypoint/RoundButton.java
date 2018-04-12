/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.map.waypoint;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 *
 * @author broder
 */
class RoundButton extends JButton {

    public RoundButton(Color color) {
        setModel(new DefaultButtonModel());
        init(null, null);
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setBackground(color);
        setContentAreaFilled(true);
        setFocusPainted(true);
        //setVerticalAlignment(SwingConstants.TOP);
        setAlignmentY(Component.TOP_ALIGNMENT);
        initShape();
    }
    protected Shape shape, base;

    protected void initShape() {
        if (!getBounds().equals(base)) {
            Dimension s = getPreferredSize();
            base = getBounds();
            shape = new Ellipse2D.Float(0, 0, s.width - 1, s.height - 1);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(5, 5);
    }

    @Override
    protected void paintBorder(Graphics g) {
        initShape();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        //g2.setStroke(new BasicStroke(1.0f));
        g2.fill(shape);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    @Override
    public boolean contains(int x, int y) {
        initShape();
        return shape.contains(x, y);
        //or return super.contains(x, y) && ((image.getRGB(x, y) >> 24) & 0xff) > 0;
    }
}
