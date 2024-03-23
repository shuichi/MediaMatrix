package mediamatrix.dendrogram;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import mediamatrix.db.MediaMatrix;

public class Node implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Node left;
    private Node right;
    private Leaf leaf;
    private int leafIndex;

    public Node() {
    }

    public Node(Leaf leaf, int leafIndex) {
        this.leaf = leaf;
        this.leafIndex = leafIndex;
    }

    public Node(Node left, Node right) {
        this.left = left;
        this.right = right;
    }

    public MediaMatrix nodeToMatrix(MediaMatrix mat) {
        final List<Node> leaves = allLeafNodes();
        final double[] rows = new double[leaves.size()];
        for (int i = 0; i < leaves.size(); i++) {
            rows[i] = mat.getRow(leaves.get(i).getLeafIndex());
        }
        final MediaMatrix result = new MediaMatrix(rows, mat.getColumns());
        for (int i = 0; i < result.getHeight(); i++) {
            for (int j = 0; j < result.getWidth(); j++) {
                result.set(result.getRow(i), result.getColumn(j), mat.get(result.getRow(i), mat.getColumn(j)));
            }
        }
        return result;
    }

    public double averageSimilarity(Node node) {
        final List<Leaf> thisLeaves = allLeaves();
        final List<Leaf> nodeLeaves = node.allLeaves();
        double sum = 0d;
        for (Leaf leaf1 : thisLeaves) {
            for (Leaf leaf2 : nodeLeaves) {
                sum += leaf1.similarity(leaf2);
            }
        }
        return sum / (thisLeaves.size() * nodeLeaves.size());
    }

    public double averageDissimilarity(Node node) {
        final List<Leaf> thisLeaves = allLeaves();
        final List<Leaf> nodeLeaves = node.allLeaves();
        double sum = 0d;
        for (Leaf leaf1 : thisLeaves) {
            for (Leaf leaf2 : nodeLeaves) {
                sum += leaf1.dissimilarity(leaf2);
            }
        }
        return sum / (thisLeaves.size() * nodeLeaves.size());
    }

    public List<Leaf> allLeaves() {
        return allLeaves(new ArrayList<Leaf>());
    }

    private List<Leaf> allLeaves(List<Leaf> leaves) {
        if (leaf != null) {
            leaves.add(leaf);
        } else {
            left.allLeaves(leaves);
            right.allLeaves(leaves);
        }
        return leaves;
    }

    public List<Node> allLeafNodes() {
        return allLeafNodes(new ArrayList<Node>());
    }

    private List<Node> allLeafNodes(List<Node> leaves) {
        if (leaf != null) {
            leaves.add(this);
        } else {
            left.allLeafNodes(leaves);
            right.allLeafNodes(leaves);
        }
        return leaves;
    }

    public Leaf getLeaf() {
        return leaf;
    }

    public void setLeaf(Leaf leaf) {
        this.leaf = leaf;
    }

    @Override
    public String toString() {
        if (leaf != null) {
            return "leaf(" + leafIndex + ")";
        } else {
            return "node(" + left.toString() + "," + right.toString() + ")";
        }
    }

    public int getHeight() {
        return getHeight(0);
    }

    public double getCenter() {
        if (leaf == null) {
            return left.getCenter() + ((right.getCenter() - left.getCenter()) / 2);
        } else {
            return leafIndex;
        }
    }

    public double getX1Height() {
        if (leaf == null) {
            return getHeight() - left.getHeight();
        } else {
            return 0;
        }
    }

    public double getX2Height() {
        if (leaf == null) {
            return getHeight() - right.getHeight();
        } else {
            return 0;
        }
    }

    public double getX1() {
        if (leaf == null) {
            return left.getCenter();
        } else {
            return leafIndex;
        }
    }

    public double getX2() {
        if (leaf == null) {
            return right.getCenter();
        } else {
            return leafIndex;
        }
    }

    private int getHeight(int count) {
        count++;
        if (leaf == null) {
            return Math.max(left.getHeight(count), right.getHeight(count));
        } else {
            return count;
        }
    }

    public boolean isLeaf() {
        return leaf != null;
    }

    public int getLeafIndex() {
        return leafIndex;
    }

    public void setLeafIndex(int leafIndex) {
        this.leafIndex = leafIndex;
    }

    public int getWidth() {
        return allLeaves().size();
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }
}
