package mediamatrix.dendrogram;

import mediamatrix.dendrogram.Node;
import mediamatrix.dendrogram.StoryDendrogram;
import mediamatrix.utils.ImageUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mediamatrix.db.ChronoArchive;

public class DendrogramGenerator {

    private final ChronoArchive arc;
    private BufferedImage image;
    private StoryDendrogram dendrogram;

    public DendrogramGenerator(ChronoArchive arc) throws IOException {
        final List<Node> leaves = new ArrayList<Node>();
        for (int i = 0; i < arc.size(); i++) {
//            leaves.add(new Node(arc.getColorHistogram(i), i));
        }
        this.arc = arc;
        this.dendrogram = new StoryDendrogram(leaves);
        this.image = drawDendrogram(dendrogram.getRootNode(), 50, 55, 20);
    }

    public StoryDendrogram getDendrogram() {
        return dendrogram;
    }

    public BufferedImage getImage() {
        return image;
    }

    public BufferedImage drawDendrogram(Node node, int margin, int itemWidth, int itemHeight) throws IOException {
        int h = (node.getHeight() * itemHeight) + (margin * 2);
        int w = (node.getWidth() * itemWidth) + (margin * 2);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        drawNode(node, g, margin, itemWidth, itemHeight);
        return img;
    }

    private void drawNode(Node node, Graphics2D g, int margin, int itemWidth, int itemHeight) throws IOException {
        int h = node.getHeight() * itemHeight + margin;
        if (!node.isLeaf()) {
            drawNode(node.getLeft(), g, margin, itemWidth, itemHeight);
            drawNode(node.getRight(), g, margin, itemWidth, itemHeight);
            int x1 = (int) Math.ceil(node.getX1() * itemWidth) + margin;
            int x2 = (int) Math.ceil(node.getX2() * itemWidth) + margin;
            g.drawLine(x1, h, x2, h);
            g.drawLine(x1, h, x1, h - (int) Math.ceil(node.getX1Height() * itemHeight));
            g.drawLine(x2, h, x2, h - (int) Math.ceil(node.getX2Height() * itemHeight));
        } else {
            Image im = ImageUtilities.createThumbnail(arc.getImage(node.getLeafIndex()), 50, 50);
            g.drawImage(im, ((node.getLeafIndex() * itemWidth)) + (margin / 2), (margin / 2) + 5, null);
        }
    }
}
