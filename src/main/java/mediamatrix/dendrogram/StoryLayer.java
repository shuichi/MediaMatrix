package mediamatrix.dendrogram;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import mediamatrix.db.ChronoArchive;
import mediamatrix.db.MediaMatrix;
import mediamatrix.db.PrimitiveEngine;

public class StoryLayer {

    private final Map<Integer, List<Node>> layers;
    private final MediaMatrix matrix;
    private final int size;

    public StoryLayer(ChronoArchive carc) {
        final Node rootNode = new StoryDendrogram(null).getRootNode();
        this.matrix = carc.getMatrix();
        this.layers = new TreeMap<Integer, List<Node>>();
        int max = rootNode.allLeaves().size();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            List<Node> nodes = getLayer(new ArrayList<Node>(), i, 0, rootNode);
            layers.put(i, nodes);
            if (nodes.size() == max) {
                break;
            }
        }

        int temp = 0;
        final Set<Integer> keys = layers.keySet();
        for (Integer i : keys) {
            temp = Math.max(temp, i);
        }
        size = temp + 1;
    }

    public MediaMatrix getMatrix() {
        return matrix;
    }

    public List<Node> getLayer(int level) {
        return layers.get(level);
    }

    public List<MediaMatrix> getLayerMatrix(int level) {
        final List<Node> nodes = layers.get(level);
        final List<MediaMatrix> result = new ArrayList<MediaMatrix>();
        final PrimitiveEngine pe = new PrimitiveEngine();
        for (Node node : nodes) {
            result.add(node.nodeToMatrix(matrix));
        }
        return result;
    }

    public int size() {
        return size;
    }

    private List<Node> getLayer(List<Node> list, int requiredLevel, int currentLevel, Node node) {
        if (requiredLevel <= currentLevel) {
            list.add(node);
            return list;
        }
        if (node.isLeaf()) {
            list.add(node);
            return list;
        }
        if (!node.isLeaf()) {
            currentLevel++;
            getLayer(list, requiredLevel, currentLevel, node.getLeft());
            getLayer(list, requiredLevel, currentLevel, node.getRight());
        }
        return list;
    }

    public int findLayer(int count) {
        int min = Integer.MAX_VALUE;
        int result = 0;
        for (int i = 0; i < size(); i++) {
            int diff = Math.abs(getLayer(i).size() - count);
            min = Math.min(min, diff);
            if (min == diff) {
                result = i;
            }
        }
        return result;
    }
}
