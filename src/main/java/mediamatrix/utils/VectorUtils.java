package mediamatrix.utils;

public class VectorUtils {

    public static double sum(double[] values) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return sum;
    }

    public static double average(double[] values) {
        return sum(values) / values.length;
    }

    public static double innerProduct(double[] values, double[] target) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += (values[i] * target[i]);
        }
        return sum;
    }

    public static double innerProduct(float[] values, Double[] target) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += (values[i] * target[i]);
        }
        return sum;
    }

    public static double innerProduct(Double[] values, Double[] target) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += (values[i] * target[i]);
        }
        return sum;
    }

    public static double[] normalize(double[] values) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += (values[i] * values[i]);
        }
        sum = Math.sqrt(sum);
        double[] newValues = new double[values.length];
        for (int i = 0; i < newValues.length; i++) {
            newValues[i] = values[i] / sum;
        }

        return newValues;
    }

    public static Double[] normalize2(Double[] values) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += (values[i] * values[i]);
        }
        sum = Math.sqrt(sum);
        Double[] newValues = new Double[values.length];
        for (int i = 0; i < newValues.length; i++) {
            newValues[i] = values[i] / sum;
        }

        return newValues;
    }

    public static Double[] normalize1(Double[] values) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        Double[] newValues = new Double[values.length];
        for (int i = 0; i < newValues.length; i++) {
            newValues[i] = values[i] / sum;
        }

        return newValues;
    }
}
