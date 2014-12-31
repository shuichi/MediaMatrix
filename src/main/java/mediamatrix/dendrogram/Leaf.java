package mediamatrix.dendrogram;

public interface Leaf {
    public double dissimilarity(Leaf l);
    public double similarity(Leaf l);
}
