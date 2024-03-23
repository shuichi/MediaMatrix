package mediamatrix.db;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import mediamatrix.utils.Score;

public class CXMQLResultSet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ArrayList<MediaDataObjectScore> result;
    private final String type;

    public CXMQLResultSet(TreeSet<MediaDataObjectScore> set, String type) {
        this.result = new ArrayList<>(set);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int size() {
        return result.size();
    }

    public Set<Score<Integer, Double>> getFrameCorrelation(int row) {
        return result.get(row).getFrameCorrelation();
    }

    public CorrelationMatrix getCorrelationMatrix(int row, String key) {
        return result.get(row).getCorrelationMatrix(key);
    }

    public Map<String, CorrelationMatrix> getCorrelationMatrices(int row) {
        return result.get(row).getCorrelationMatrices();
    }

    public double getValue(int row) {
        return result.get(row).getValue();
    }

    public String getId(int row) {
        return result.get(row).getMediaObject().getId();
    }

    public String getName(int row) {
        return result.get(row).getMediaObject().getName();
    }

    public String getEntityID(int row) {
        return result.get(row).getMediaObject().getEntityID();
    }
}
