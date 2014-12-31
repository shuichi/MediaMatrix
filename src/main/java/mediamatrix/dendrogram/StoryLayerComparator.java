package mediamatrix.dendrogram;

import mediamatrix.munsell.ColorHistogram;
import mediamatrix.munsell.ColorHistogramScore;
import mediamatrix.munsell.HSVColor;
import mediamatrix.utils.VectorUtils;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import mediamatrix.db.ChronoArchive;
import mediamatrix.db.MediaMatrix;

public class StoryLayerComparator {

    public static void main(String[] args) throws Exception {
        final String file1 = "C:/WORKSPACE/spiceandwolf_01.carc";
        final String file2 = "C:/WORKSPACE/spiceandwolf_02.carc";
        System.out.println(file1 + "   <>   " + file2);
        final StoryLayer layer1 = new StoryLayer(new ChronoArchive(new File(file1)));
        final StoryLayer layer2 = new StoryLayer(new ChronoArchive(new File(file2)));
        final long start = System.currentTimeMillis();
        final double[][] result = correlation(layer1, layer2, true, true);
        double sum = 0d;
        for (int i = 0; i < result.length; i++) {
            double score = VectorUtils.average(result[i]);
            sum += score;
            System.out.println(i + " --> " + score + "(" + Math.log(i + 1) * score + ")");
        }
        System.out.println((System.currentTimeMillis() - start) / 1000.0d);
        System.out.println(sum);
    }

    public static double[][] correlation(StoryLayer l1, StoryLayer l2, boolean useSimilarity, boolean useISE) {
        final double[][] result = new double[l1.size()][];
        for (int i = 0; i < l1.size(); i++) {
            if (useSimilarity) {
                if (useISE) {
                    final List<Node> nodes1 = l1.getLayer(i);
                    final int level = l2.findLayer(nodes1.size());
                    final List<Node> nodes2 = l2.getLayer(level);
                    System.out.println("Comparing: " + i + " <--> " + level);
                    result[i] = layerSimilarityWithISE(nodes1, nodes2, l1.getMatrix(), l2.getMatrix());
                } else {
                    final List<Node> nodes1 = l1.getLayer(i);
                    final int level = l2.findLayer(nodes1.size());
                    final List<Node> nodes2 = l2.getLayer(level);
                    System.out.println("Comparing: " + i + " <--> " + level);
                    result[i] = layerSimilarity(nodes1, nodes2, l1.getMatrix(), l2.getMatrix(), new TreeMap<String, Double>());
                }
            } else {
                if (useISE) {
                    final List<Node> nodes1 = l1.getLayer(i);
                    final int level = l2.findLayer(nodes1.size());
                    final List<Node> nodes2 = l2.getLayer(level);
                    System.out.println("Comparing: " + i + " <--> " + level);
                    result[i] = layerDisimilarityWithISE(nodes1, nodes2);
                } else {
                    final List<Node> nodes1 = l1.getLayer(i);
                    final int level = l2.findLayer(nodes1.size());
                    final List<Node> nodes2 = l2.getLayer(level);
                    System.out.println("Comparing: " + i + " <--> " + level);
                    result[i] = layerDisimilarity(nodes1, nodes2, new TreeMap<String, Double>());
                }
            }
        }
        return result;
    }

    public static double[] ise(Node node, MediaMatrix mat) {
        double[] result = new double[mat.getWidth()];
        List<Node> leaves = node.allLeafNodes();
        for (int i = 0; i < result.length; i++) {
            for (Node n : leaves) {
                result[i] += mat.get(n.getLeafIndex(), i);
            }
            if (result[i] <= 0) {
                result[i] = 0;
            } else {
                result[i] = Math.log(1 + (1.0 / result[i]));
            }
        }
        return result;
    }

    public static double[] ise(Node node) {
        final List<Leaf> leaves = node.allLeaves();
        final double[] result = new double[((ColorHistogram) leaves.get(0)).size()];
        final HSVColor[] colors = ((ColorHistogram) leaves.get(0)).getScores().keySet().toArray(new HSVColor[result.length]);
        for (int i = 0; i < result.length; i++) {
            for (Leaf leaf : leaves) {
                final ColorHistogram histogram = (ColorHistogram) leaf;
                final Map<HSVColor, ColorHistogramScore> scores = histogram.getScores();
                result[i] += scores.get(colors[i]).getValue();
            }
            if (result[i] <= 0) {
                result[i] = 0;
            } else {
                result[i] = Math.log(1 + (1.0 / result[i]));
            }
        }
        return result;
    }

    private static double[] layerSimilarityWithISE(List<Node> nodes1, List<Node> nodes2, MediaMatrix mat1, MediaMatrix mat2) {
        final int size = Math.min(nodes1.size(), nodes2.size());
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            final Node node1 = nodes1.get(i);
            final double[] ise1 = ise(node1, mat1);
            final Node node2 = nodes2.get(i);
            final double[] ise2 = ise(node2, mat2);
            final List<Node> leaves1 = node1.allLeafNodes();
            final List<Node> leaves2 = node2.allLeafNodes();
            double sum = 0d;
            for (int j = 0; j < leaves1.size(); j++) {
                final Node leaf1 = leaves1.get(j);
                for (int k = 0; k < leaves2.size(); k++) {
                    final Node leaf2 = leaves2.get(k);
                    for (int l = 0; l < mat1.getWidth(); l++) {
                        sum += (mat1.get(leaf1.getLeafIndex(), l) * ise1[l] * mat2.get(leaf2.getLeafIndex(), l) * ise2[l]);
                    }
                }
            }
            result[i] = sum / (leaves1.size() * leaves2.size());
        }
        return result;
    }

    private static double[] layerDisimilarityWithISE(List<Node> nodes1, List<Node> nodes2) {
        final int size = Math.min(nodes1.size(), nodes2.size());
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            final Node node1 = nodes1.get(i);
            final double[] ise1 = ise(node1);
            final Node node2 = nodes2.get(i);
            final double[] ise2 = ise(node2);
            final List<Node> leaves1 = node1.allLeafNodes();
            final List<Node> leaves2 = node2.allLeafNodes();
            double sum = 0d;
            for (int j = 0; j < leaves1.size(); j++) {
                final Node leaf1 = leaves1.get(j);
                final ColorHistogram histogram1 = (ColorHistogram) leaf1.getLeaf();
                for (int k = 0; k < leaves2.size(); k++) {
                    final Node leaf2 = leaves2.get(k);
                    final ColorHistogram histogram2 = (ColorHistogram) leaf2.getLeaf();
                    final HSVColor[] colors = histogram1.getScores().keySet().toArray(new HSVColor[histogram1.size()]);
                    for (int m = 0; m < colors.length; m++) {
                        sum += histogram1.getScores().get(colors[m]).getValue() * ise1[m] * histogram2.getScores().get(colors[m]).getValue() * ise2[m];
                    }
                }
            }
            result[i] = sum / (leaves1.size() * leaves2.size());
        }
        return result;
    }

    private static double[] layerSimilarity(List<Node> nodes1, List<Node> nodes2, MediaMatrix mat1, MediaMatrix mat2, Map<String, Double> cache) {
        final int size = Math.min(nodes1.size(), nodes2.size());
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            final Node node1 = nodes1.get(i);
            final Node node2 = nodes2.get(i);
            final List<Node> leaves1 = node1.allLeafNodes();
            final List<Node> leaves2 = node2.allLeafNodes();
            double sum = 0d;
            for (int j = 0; j < leaves1.size(); j++) {
                final Node leaf1 = leaves1.get(j);
                for (int k = 0; k < leaves2.size(); k++) {
                    final Node leaf2 = leaves2.get(k);
                    final String key = leaf1.getLeafIndex() + "-" + leaf2.getLeafIndex();
                    if (cache.containsKey(key)) {
                        sum += cache.get(key);
                    } else {
                        double t = 0d;
                        for (int l = 0; l < mat1.getWidth(); l++) {
                            t += (mat1.get(leaf1.getLeafIndex(), l) * mat2.get(leaf2.getLeafIndex(), l));
                        }
                        sum += t;
                        cache.put(key, t);
                    }
                }
            }
            result[i] = sum / (leaves1.size() * leaves2.size());
        }
        return result;
    }

    private static double[] layerDisimilarity(List<Node> nodes1, List<Node> nodes2, Map<String, Double> cache) {
        final int size = Math.min(nodes1.size(), nodes2.size());
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            final Node node1 = nodes1.get(i);
            final Node node2 = nodes2.get(i);
            final List<Node> leaves1 = node1.allLeafNodes();
            final List<Node> leaves2 = node2.allLeafNodes();
            double sum = 0d;
            for (int j = 0; j < leaves1.size(); j++) {
                final Node leaf1 = leaves1.get(j);
                for (int k = 0; k < leaves2.size(); k++) {
                    final Node leaf2 = leaves2.get(k);
                    final String key = leaf1.getLeafIndex() + "-" + leaf2.getLeafIndex();
                    if (cache.containsKey(key)) {
                        sum += cache.get(key);
                    } else {
                        double t = leaf1.getLeaf().dissimilarity(leaf2.getLeaf());
                        sum += t;
                        cache.put(key, t);
                    }
                }
            }
            result[i] = sum / (leaves1.size() * leaves2.size());
        }
        return result;
    }
}
