package mediamatrix.db;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import mediamatrix.utils.Score;

public class MediaDataObjectScore implements Comparable<MediaDataObjectScore>, Serializable {

    @Serial
    private static final long serialVersionUID = 1643318691129821182L;

    private MediaDataObject mediaObject;
    private TreeMap<String, CorrelationMatrix> matrices;
    private TreeSet<Score<Integer, Double>> frameCorrelation;
    private double value;

    public MediaDataObjectScore() {
    }

    public MediaDataObjectScore(MediaDataObject mediaObject, double value) {
        this.mediaObject = mediaObject;
        this.value = value;
        this.matrices = new TreeMap<>();
    }

    public CorrelationMatrix getCorrelationMatrix(String key) {
        return matrices.get(key);
    }

    public void putCorrelationMatrix(String key, CorrelationMatrix correlationMatrix) {
        this.matrices.put(key, correlationMatrix);
    }

    public TreeSet<Score<Integer, Double>> getFrameCorrelation() {
        return frameCorrelation;
    }

    public void setFrameCorrelation(TreeSet<Score<Integer, Double>> frameCorrelation) {
        this.frameCorrelation = frameCorrelation;
    }

    public Map<String, CorrelationMatrix> getCorrelationMatrices() {
        return matrices;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public MediaDataObject getMediaObject() {
        return mediaObject;
    }

    public void setMediaObject(MediaDataObject mediaObject) {
        this.mediaObject = mediaObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MediaDataObjectScore other = (MediaDataObjectScore) obj;
        if (this.mediaObject != other.mediaObject && (this.mediaObject == null || !this.mediaObject.equals(other.mediaObject))) {
            return false;
        }
        if (this.value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.mediaObject != null ? this.mediaObject.hashCode() : 0);
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        return hash;
    }

    @Override
    public int compareTo(MediaDataObjectScore o) {
        if (this.equals(o)) {
            return 0;
        } else if (getValue() < o.getValue()) {
            return 1;
        } else {
            return -1;
        }
    }
}
