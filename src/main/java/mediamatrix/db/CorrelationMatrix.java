package mediamatrix.db;

import java.io.Serializable;

public class CorrelationMatrix implements Serializable {

    private static final long serialVersionUID = 6714492139247039423L;
    private double[][] values;
    private int mostRelevantSlideNumber;
    private double[] mostRelevantSlide;

    public CorrelationMatrix(int width, int height) {
        values = new double[width][height];
    }

    public int getWidth() {
        return values.length;
    }

    public int getHeight() {
        return values[0].length;
    }

    public double get(int x, int y) {
        return values[x][y];
    }

    public void set(int x, int y, double v) {
        values[x][y] = v;
    }

    public int mostRelevantShift() {
        if (mostRelevantSlide == null) {
            double norm = 0d;
            for (int i = 0; i < getWidth() && i < getHeight(); i++) {
                double[] vec = getShift(i);
                double tempNorm = 0d;
                for (int j = 0; j < vec.length; j++) {
                    tempNorm += (vec[j] * vec[j]);
                }
                tempNorm = Math.sqrt(tempNorm);
                if (norm < tempNorm) {
                    norm = tempNorm;
                    mostRelevantSlideNumber = i;
                }

                vec = getShift(-1 * i);
                tempNorm = 0d;
                for (int j = 0; j < vec.length; j++) {
                    tempNorm += (vec[j] * vec[j]);
                }
                tempNorm = Math.sqrt(tempNorm);
                if (norm < tempNorm) {
                    norm = tempNorm;
                    mostRelevantSlideNumber = -1 * i;
                }
            }
            mostRelevantSlide = getShift(mostRelevantSlideNumber);
        }
        return mostRelevantSlideNumber;
    }

    public double[] getShift(int num) {
        double[] result = null;
        if (num == 0) {
            result = new double[Math.min(getHeight(), getWidth())];
            for (int i = 0; i < result.length; i++) {
                result[i] = values[i][i];
            }
        } else if (num > 0) {
            if (getWidth() > num) {
                result = new double[Math.min(getWidth() - num, getHeight())];
            } else {
                result = new double[0];
            }
            for (int i = 0; i < result.length; i++) {
                result[i] = values[i + num][i];
            }
        } else {
            if (getHeight() > -num) {
                result = new double[Math.min(getWidth(), getHeight() + num)];
            } else {
                result = new double[0];
            }
            for (int i = 0; i < result.length; i++) {
                result[i] = values[i][i - num];
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "CorrelationMatrix(" + getHeight() + " x " + getWidth() + ")";
    }
}
