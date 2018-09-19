package org.jevis.jeconfig.plugin.graph.view;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * {@link DragResizerXY} can be used to add mouse listeners to a {@link Region}
 * and make it resizable by the user by clicking and dragging the border in the
 * same way as a window.
 * <p>
 * Height and Width resizing is working (hopefully) properly
 *
 * <pre>
 * DragResizer.makeResizable(myAnchorPane);
 * </pre>
 *
 * @author Cannibalsticky (modified from the original DragResizer created by AndyTill)
 */
public class DragResizerXY {

    /**
     * The margin around the control that a user can click in to start resizing
     * the region.
     */
    private static final int RESIZE_MARGIN = 10;

    private final Region region;

    private double y;

    private double x;

    private boolean initMinHeight;

    private boolean initMinWidth;

    private boolean draggableZoneX, draggableZoneY;

    private boolean dragging;

    private DragResizerXY(Region aRegion) {
        region = aRegion;
    }

    public static void makeResizable(Region region) {
        final DragResizerXY resizer = new DragResizerXY(region);

        region.setOnMousePressed(event -> resizer.mousePressed(event));
        region.setOnMouseDragged(event -> resizer.mouseDragged(event));
        region.setOnMouseMoved(event -> resizer.mouseOver(event));
        region.setOnMouseReleased(event -> resizer.mouseReleased(event));
    }

    protected void mouseReleased(MouseEvent event) {
        dragging = false;
        region.setCursor(Cursor.DEFAULT);
    }

    protected void mouseOver(MouseEvent event) {
        if (isInDraggableZone(event) || dragging) {
            if (draggableZoneY) {
                region.setCursor(Cursor.S_RESIZE);
            }

            if (draggableZoneX) {
                region.setCursor(Cursor.E_RESIZE);
            }

        } else {
            region.setCursor(Cursor.DEFAULT);
        }
    }


    //had to use 2 variables for the control, tried without, had unexpected behaviour (going big was ok, going small nope.)
    protected boolean isInDraggableZone(MouseEvent event) {
        draggableZoneY = event.getY() > (region.getHeight() - RESIZE_MARGIN);
        draggableZoneX = event.getX() > (region.getWidth() - RESIZE_MARGIN);
        return (draggableZoneY || draggableZoneX);
    }

    protected void mouseDragged(MouseEvent event) {
        if (!dragging) {
            return;
        }

        if (draggableZoneY) {
            double mouseY = event.getY();

            double newHeight = region.getPrefHeight() + (mouseY - y);

            region.setPrefHeight(newHeight);

            y = mouseY;
        }

        if (draggableZoneX) {
            double mouseX = event.getX();

            double newWidth = region.getMinWidth() + (mouseX - x);

            region.setMinWidth(newWidth);

            x = mouseX;

        }

    }

    protected void mousePressed(MouseEvent event) {

        // ignore clicks outside of the draggable margin
        if (!isInDraggableZone(event)) {
            return;
        }

        dragging = true;

        // make sure that the minimum height is set to the current height once,
        // setting a min height that is smaller than the current height will
        // have no effect
        if (!initMinHeight) {
            region.setPrefHeight(region.getHeight());
            initMinHeight = true;
        }

        y = event.getY();

        if (!initMinWidth) {
            region.setMinWidth(region.getWidth());
            initMinWidth = true;
        }

        x = event.getX();
    }
}