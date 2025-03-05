package org.jevis.jeconfig.tool;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.jevis.jeconfig.plugin.dashboard.DashBoardPane;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.List;

/**
 * Based on https://github.com/varren/JavaFX-Resizable-Draggable-Node
 * <p>
 * <p>
 * ************* How to use ************************
 * <p>
 * Rectangle rectangle = new Rectangle(50, 50);
 * rectangle.setFill(Color.BLACK);
 * DragResizeMod.makeResizable(rectangle, null);
 * <p>
 * Pane root = new Pane();
 * root.getChildren().add(rectangle);
 * <p>
 * primaryStage.setScene(new Scene(root, 300, 275));
 * primaryStage.show();
 * <p>
 * ************* OnDragResizeEventListener **********
 * <p>
 * You need to override OnDragResizeEventListener and
 * 1) preform out of main field bounds check
 * 2) make changes to the node
 * (this class will not change anything in node coordinates)
 * <p>
 * There is defaultListener and it works only with Canvas nad Rectangle
 */

public class DragResizeMod {
    public static final OnDragResizeEventListener defaultListener = new OnDragResizeEventListener() {
        /**
         * Workaround, because the drag event takes the Effect size into the node size so we have to subtract it
         * Alternative: change the DragResizeMod.mouseDragged() effect
         **/
        private final double shadowSize = 10d;
        private BooleanProperty resizable = new SimpleBooleanProperty(false);
        private BooleanProperty snapToGrid = new SimpleBooleanProperty(false);
        private DashBoardPane dashBoardPane = null;
        private MouseEvent mouseEvent = null;


        @Override
        public void setDashBoardPane(DashBoardPane dashBoardPane) {
            this.dashBoardPane = dashBoardPane;
        }

        @Override
        public BooleanProperty snapToGridProperty() {
            return snapToGrid;
        }

        @Override
        public BooleanProperty resizeableProperty() {
            return resizable;
        }

        @Override
        public void onDrag(Node node, MouseEvent event, double x, double y, double h, double w) {
//            if (resizable.getValue()) setNodeSize(node, x, y, h - shadowSize, w - shadowSize);
            mouseEvent = event;
            if (resizable.getValue()) setNodeSize(node, x, y, h, w);
        }

        @Override
        public void onResize(Node node, MouseEvent event, double x, double y, double h, double w) {
            mouseEvent = event;
            if (resizable.getValue()) setNodeSize(node, x, y, h, w);
        }

        private void setNodeSize(Node node, double x, double y, double h, double w) {
            //System.out.println("setNodeSize: x:" + x + " y:" + y + " h:" + h + " w:" + w);
            if (dashBoardPane != null && dashBoardPane.getSnapToGrid()) {

                List<Double> xGrid = dashBoardPane.xGrids;
                List<Double> yGrid = dashBoardPane.yGrids;
                if (mouseEvent != null && mouseEvent.isShiftDown()) {
                    xGrid = dashBoardPane.xGridsFine;
                    yGrid = dashBoardPane.yGridsFine;
                }
                //Move Node
                node.setLayoutX(dashBoardPane.getNextGridX(x, xGrid));
                node.setLayoutY(dashBoardPane.getNextGridY(y, yGrid));

                //Snap to Grid
                double snapX = dashBoardPane.getNextGridX(w + ((Widget) node).getLayoutX(), xGrid) - ((Widget) node).getLayoutX();
                double snapY = dashBoardPane.getNextGridY(h + ((Widget) node).getLayoutY(), yGrid) - ((Widget) node).getLayoutY();
                //System.out.println("newNodeSize: x: " + snapX + " y:" + snapY + "  Layoutx: " + ((Widget) node).getLayoutX() + " layouty: " + ((Widget) node).getLayoutY());
                ((Widget) node).setNodeSize(snapX, snapY);


            } else {
                node.setLayoutX(x);
                node.setLayoutY(y);
                ((Widget) node).setNodeSize(w, h);
            }
        }
    };


    private static final int MARGIN = 8;
    private static final double MIN_W = 20;
    private static final double MIN_H = 20;
    private double clickX, clickY, nodeX, nodeY, nodeH, nodeW;
    private S state = S.DEFAULT;
    private Node node;
    private OnDragResizeEventListener listener = defaultListener;
    ;

    private DragResizeMod(Node node, OnDragResizeEventListener listener) {
        this.node = node;
        if (listener != null)
            this.listener = listener;
    }

    public static void makeResizable(Node node) {
        makeResizable(node, null);
    }

    public static void makeResizable(Node node, OnDragResizeEventListener listener) {
        final DragResizeMod resizer = new DragResizeMod(node, listener);

        node.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    resizer.mousePressed(event);
                }
            }
        });
        node.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                resizer.mouseDragged(event);
            }
        });
        node.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                resizer.mouseOver(event);
            }
        });
        node.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                resizer.mouseReleased(event);
            }
        });
    }

    private static Cursor getCursorForState(S state) {
        switch (state) {
            case NW_RESIZE:
                return Cursor.NW_RESIZE;
            case SW_RESIZE:
                return Cursor.SW_RESIZE;
            case NE_RESIZE:
                return Cursor.NE_RESIZE;
            case SE_RESIZE:
                return Cursor.SE_RESIZE;
            case E_RESIZE:
                return Cursor.E_RESIZE;
            case W_RESIZE:
                return Cursor.W_RESIZE;
            case N_RESIZE:
                return Cursor.N_RESIZE;
            case S_RESIZE:
                return Cursor.S_RESIZE;
            default:
                return Cursor.DEFAULT;
        }
    }

    protected void mouseReleased(MouseEvent event) {
        node.setCursor(Cursor.DEFAULT);
        state = S.DEFAULT;
    }

    protected void mouseOver(MouseEvent event) {
        S state = currentMouseState(event);
        Cursor cursor = getCursorForState(state);
        if (node instanceof Widget) {
            if (!((Widget) node).getControl().editableProperty.getValue()) {
                cursor = getCursorForState(S.DEFAULT);
            }
        }

        node.setCursor(cursor);

    }

    private S currentMouseState(MouseEvent event) {
        S state = S.DEFAULT;
        boolean left = isLeftResizeZone(event);
        boolean right = isRightResizeZone(event);
        boolean top = isTopResizeZone(event);
        boolean bottom = isBottomResizeZone(event);

        if (left && top) state = S.NW_RESIZE;
        else if (left && bottom) state = S.SW_RESIZE;
        else if (right && top) state = S.NE_RESIZE;
        else if (right && bottom) state = S.SE_RESIZE;
        else if (right) state = S.E_RESIZE;
        else if (left) state = S.W_RESIZE;
        else if (top) state = S.N_RESIZE;
        else if (bottom) state = S.S_RESIZE;
        else if (isInDragZone(event)) state = S.DRAG;

        return state;
    }

    protected void mouseDragged(MouseEvent event) {
        if (listener != null) {
            double mouseX = parentX(event.getX());
            double mouseY = parentY(event.getY());
            if (state == S.DRAG && event.isControlDown()) {
//                System.out.println("mouseDragged" + event + "  crontorl: " + event.isControlDown());
                listener.onDrag(node, event, mouseX - clickX, mouseY - clickY, nodeH, nodeW);
            } else if (state != S.DEFAULT) {
                //resizing
                double newX = nodeX;
                double newY = nodeY;
                double newH = nodeH;
                double newW = nodeW;

                // Right Resize
                if (state == S.E_RESIZE || state == S.NE_RESIZE || state == S.SE_RESIZE) {
                    newW = mouseX - nodeX;
                }
                // Left Resize
                if (state == S.W_RESIZE || state == S.NW_RESIZE || state == S.SW_RESIZE) {
                    newX = mouseX;
                    newW = nodeW + nodeX - newX;
                }

                // Bottom Resize
                if (state == S.S_RESIZE || state == S.SE_RESIZE || state == S.SW_RESIZE) {
                    newH = mouseY - nodeY;
                }
                // Top Resize
                if (state == S.N_RESIZE || state == S.NW_RESIZE || state == S.NE_RESIZE) {
                    newY = mouseY;
                    newH = nodeH + nodeY - newY;
                }

                //min valid rect Size Check
                if (newW < MIN_W) {
                    if (state == S.W_RESIZE || state == S.NW_RESIZE || state == S.SW_RESIZE)
                        newX = newX - MIN_W + newW;
                    newW = MIN_W;
                }

                if (newH < MIN_H) {
                    if (state == S.N_RESIZE || state == S.NW_RESIZE || state == S.NE_RESIZE)
                        newY = newY + newH - MIN_H;
                    newH = MIN_H;
                }

                listener.onResize(node, event, newX, newY, newH, newW);
            }
        }
    }

    protected void mousePressed(MouseEvent event) {
        if (isInResizeZone(event)) {
            setNewInitialEventCoordinates(event);
            state = currentMouseState(event);
        } else if (isInDragZone(event)) {
            setNewInitialEventCoordinates(event);
            state = S.DRAG;
        } else {
            state = S.DEFAULT;
        }
    }

    private void setNewInitialEventCoordinates(MouseEvent event) {
        nodeX = nodeX();
        nodeY = nodeY();
        nodeH = nodeH();
        nodeW = nodeW();
        clickX = event.getX();
        clickY = event.getY();
    }

    private boolean isInResizeZone(MouseEvent event) {
        return isLeftResizeZone(event) || isRightResizeZone(event)
                || isBottomResizeZone(event) || isTopResizeZone(event);
    }

    private boolean isInDragZone(MouseEvent event) {
        double xPos = parentX(event.getX());
        double yPos = parentY(event.getY());
        double nodeX = nodeX() + MARGIN;
        double nodeY = nodeY() + MARGIN;
        double nodeX0 = nodeX() + nodeW() - MARGIN;
        double nodeY0 = nodeY() + nodeH() - MARGIN;

        return (xPos > nodeX && xPos < nodeX0) && (yPos > nodeY && yPos < nodeY0);
    }

    private boolean isLeftResizeZone(MouseEvent event) {
        return intersect(0, event.getX());
    }

    private boolean isRightResizeZone(MouseEvent event) {
        return intersect(nodeW(), event.getX());
    }

    private boolean isTopResizeZone(MouseEvent event) {
        return intersect(0, event.getY());
    }

    private boolean isBottomResizeZone(MouseEvent event) {
        return intersect(nodeH(), event.getY());
    }

    private boolean intersect(double side, double point) {
        return side + MARGIN > point && side - MARGIN < point;
    }

    private double parentX(double localX) {
        return nodeX() + localX;
    }

    private double parentY(double localY) {
        return nodeY() + localY;
    }

    private double nodeX() {
        return node.getLayoutX();

//        return node.getBoundsInParent().getMinX();
    }

    private double nodeY() {
        return node.getLayoutY();
//        return node.getBoundsInParent().getMinY();
    }

    private double nodeW() {
        return node.getLayoutBounds().getWidth();
        //        return node.getBoundsInParent().getWidth();
    }

    private double nodeH() {
        return node.getLayoutBounds().getHeight();
//        return node.getBoundsInParent().getHeight();
    }

    public static enum S {
        DEFAULT,
        DRAG,
        NW_RESIZE,
        SW_RESIZE,
        NE_RESIZE,
        SE_RESIZE,
        E_RESIZE,
        W_RESIZE,
        N_RESIZE,
        S_RESIZE
    }

    public interface OnDragResizeEventListener {
        void onDrag(Node node, MouseEvent event, double x, double y, double h, double w);

        void onResize(Node node, MouseEvent event, double x, double y, double h, double w);

        BooleanProperty resizeableProperty();

        BooleanProperty snapToGridProperty();

        void setDashBoardPane(DashBoardPane dashBoardPane);


    }
}