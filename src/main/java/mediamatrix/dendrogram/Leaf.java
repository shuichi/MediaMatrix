package mediamatrix.dendrogram;

import java.io.Serializable;

public interface Leaf extends Serializable {
    public double dissimilarity(Leaf l);
    public double similarity(Leaf l);
}
