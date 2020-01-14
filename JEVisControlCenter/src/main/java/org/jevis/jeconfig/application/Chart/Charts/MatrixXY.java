package org.jevis.jeconfig.application.Chart.Charts;

public class MatrixXY {
    private int x;
    private int y;

    public MatrixXY(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MatrixXY) {
            MatrixXY otherObj = (MatrixXY) obj;
            return x == otherObj.getX() && y == otherObj.getY();
        } else return false;
    }
}
