package mediamatrix.dendrogram;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class StoryDendrogram implements Serializable {

    public static final long serialVersionUID = 1L;
    private Node node;
    private final List<Node> leaves;

    public StoryDendrogram(List<Node> items) {
        this.leaves = items;
    }

    public Node getRootNode() {
        if (node == null) {
            node = dendrogram(leaves, new TreeMap<String, Double>());
        }
        return node;
    }

    public Node findParentOf(Node n) {
        List<Node> allNodes = traverse(new ArrayList<Node>(), getRootNode());
        for (Node target : allNodes) {
            if (!target.isLeaf()) {
                if (target.getRight() == n || target.getLeft() == n) {
                    return node;
                }
            }
        }
        return null;
    }

    public List<Node> traverse(List<Node> list, Node n) {
        list.add(n);
        if (!n.isLeaf()) {
            traverse(list, n.getLeft());
            traverse(list, n.getRight());
        }
        return list;
    }

    private Node dendrogram(List<Node> leaves, Map<String, Double> cache) {
        if (leaves.size() > 1) {
            final Set<NodeRelevance> delimiters = new TreeSet<NodeRelevance>();
            Node previous = null;
            for (int i = 0; i < leaves.size(); i++) {
                if (i > 0) {
                    double score = 0d;
                    final String key = previous.toString() + "<>" + leaves.get(i).toString();
                    if (cache.containsKey(key)) {
                        score = cache.get(key);
                    } else {
                        score = leaves.get(i).averageDissimilarity(previous);
                        cache.put(key, score);
                    }
                    delimiters.add(new NodeRelevance(previous, leaves.get(i), score));
                }
                previous = leaves.get(i);
            }
            final Node tempNode = delimiters.iterator().next().toNode();
            final List<Node> result = new ArrayList<Node>();
            for (int i = 0; i < leaves.size(); i++) {
                if (leaves.get(i) == tempNode.getLeft() && leaves.get(i + 1) == tempNode.getRight()) {
                    i++;
                    result.add(tempNode);
                } else {
                    result.add(leaves.get(i));
                }
            }
            System.out.println("Dendrogram " + result.size());
            return dendrogram(result, cache);
        } else {
            return leaves.get(0);
        }
    }
}

class NodeRelevance implements Comparable<NodeRelevance> {

    private Node left;
    private Node right;
    private double value;

    public NodeRelevance(Node left, Node right, double value) {
        this.left = left;
        this.right = right;
        this.value = value;
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

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Node toNode() {
        Node node = new Node();
        node.setLeft(left);
        node.setRight(right);
        return node;
    }

    @Override
    public String toString() {
        return left + "--" + right + " -> " + value;
    }

    @Override
    public int compareTo(NodeRelevance o) {
        int c = new Double(value).compareTo(o.value);
        if (c == 0) {
            c = new Integer(right.getLeafIndex()).compareTo(left.getLeafIndex());
        }
        return c;
    }
}
