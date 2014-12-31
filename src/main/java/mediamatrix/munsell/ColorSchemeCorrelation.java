package mediamatrix.munsell;

import java.util.Map;
import java.util.Set;

public class ColorSchemeCorrelation {

    static final long serialVersionUID = 251895384982248119L;
    private Map<String, Double> scores;

    public ColorSchemeCorrelation() {
    }

    public ColorSchemeCorrelation(Correlation[] c) {
        for (Correlation correlation : c) {
            scores.put(correlation.getWord(), correlation.getValue());
        }
    }

    public double dissimilarity(ColorSchemeCorrelation other) {
        double sum = 0d;
        final Set<String> keys = scores.keySet();
        for (String k : keys) {
            sum += Math.abs(get(k) - other.get(k));
        }
        return sum / 2;
    }

    public double similarity(ColorSchemeCorrelation other) {
        double sum = 0d;
        final Set<String> keys = scores.keySet();
        for (String k : keys) {
            sum += (get(k) * other.get(k));
        }
        return sum;
    }

    public Double get(String name) {
        return scores.get(name);
    }

    public int size() {
        return scores.size();
    }
}
