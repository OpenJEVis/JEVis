package org.jevis.jeconfig.application.Chart.Charts.MultiAxis;

public class PolynomialRegression {

    private int degreeNum, rs;
    private long pointsNum = 0;

    private double[][] im;
    private double[] powIm;

    public PolynomialRegression(int degree) {
        this.degreeNum = degree + 1;

        rs = 2 * degreeNum - 1;
        im = new double[degreeNum][degreeNum + 1];
        powIm = new double[rs];
    }

    public void addPoint(double x, double y) {
        if (Double.isInfinite(x) || Double.isNaN(x) || Double.isInfinite(y) || Double.isNaN(y)) {
            return;
        }

        for (int i = 1; i < rs; i++) {
            powIm[i] += Math.pow(x, i);
        }

        im[0][degreeNum] += y;
        for (int i = 1; i < degreeNum; i++) {
            im[i][degreeNum] += Math.pow(x, i) * y;
        }

        pointsNum++;
    }

    public PolynomialList getPolynomial() {
        final double[] mpcClone = powIm.clone();
        final double[][] mClone = new double[im.length][];
        for (int x = 0; x < mClone.length; x++) {
            mClone[x] = im[x];
        }

        mpcClone[0] += pointsNum;
        for (int r = 0; r < degreeNum; r++) {
            for (int c = 0; c < degreeNum; c++) {
                mClone[r][c] = mpcClone[r + c];
            }
        }
        escalate(mClone);
        final PolynomialList result = new PolynomialList(degreeNum);
        for (int j = 0; j < degreeNum; j++) {
            result.add(j, mClone[j][degreeNum]);
        }
        return result;
    }

    private void divide(double[][] A, int i, int j, int size) {
        for (int k = j + 1; k < size; k++) {
            A[i][k] = A[i][k] / A[i][j];
        }
        A[i][j] = 1;
    }

    private void escalate(final double[][] A) {
        final int n = A.length;
        final int m = A[0].length;
        int i = 0, j = 0;
        while (i < n && j < m) {
            int k = i;
            while (k < n && A[k][j] == 0) {
                k++;
            }
            if (k < n) {
                if (k != i) {
                    swap(A, i, j);
                }

                if (A[i][j] != 1) {
                    divide(A, i, j, m);
                }

                eliminate(A, i, j, n, m);
                i++;
            }
            j++;
        }
    }

    private void eliminate(double[][] A, int i, int j, int n, int size) {
        for (int k = 0; k < n; k++) {
            if (k == i || A[k][j] == 0) {
                continue;
            }

            for (int q = j + 1; q < size; q++) {
                A[k][q] -= A[k][j] * A[i][q];
            }
            A[k][j] = 0;
        }
    }

    private void swap(final double[][] A, final int i, final int j) {
        double[] temp;
        temp = A[i];
        A[i] = A[j];
        A[j] = temp;
    }
}
