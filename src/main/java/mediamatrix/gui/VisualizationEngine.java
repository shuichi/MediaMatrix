package mediamatrix.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import mediamatrix.db.ChronoArchive;
import mediamatrix.db.MediaMatrix;
import mediamatrix.dendrogram.Node;
import mediamatrix.utils.ImageUtilities;

public class VisualizationEngine {

    public BufferedImage createChartImage(MediaMatrix mat, Color bgColor, int width, int height) {
        return new Java2DChartRenderer().createChartImage(mat, bgColor, width, height);
    }

    public BufferedImage createChartImage(MediaMatrix mat, Color bgColor, int width, int height, double scaleX, double scaleY) {
        return new Java2DChartRenderer().createChartImage(mat, bgColor, width, height, scaleX, scaleY);
    }

    public BufferedImage drawDendrogram(Node node, ChronoArchive carc, int margin, int itemWidth, int itemHeight) throws IOException {
        int h = (node.getHeight() * itemHeight) + (margin * 2);
        int w = (node.getWidth() * itemWidth) + (margin * 2);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        drawNode(node, carc, g, margin, itemWidth, itemHeight);
        return img;
    }

    private void drawNode(Node node, ChronoArchive carc, Graphics2D g, int margin, int itemWidth, int itemHeight) throws IOException {
        int h = node.getHeight() * itemHeight + margin;
        if (!node.isLeaf()) {
            drawNode(node.getLeft(), carc, g, margin, itemWidth, itemHeight);
            drawNode(node.getRight(), carc, g, margin, itemWidth, itemHeight);
            int x1 = (int) Math.ceil(node.getX1() * itemWidth) + margin;
            int x2 = (int) Math.ceil(node.getX2() * itemWidth) + margin;
            g.drawLine(x1, h, x2, h);
            g.drawLine(x1, h, x1, h - (int) Math.ceil(node.getX1Height() * itemHeight));
            g.drawLine(x2, h, x2, h - (int) Math.ceil(node.getX2Height() * itemHeight));
        } else {
            Image im = ImageUtilities.createThumbnail(carc.getImage(node.getLeafIndex()), 50, 50);
            g.drawImage(im, ((node.getLeafIndex() * itemWidth)) + (margin / 2), (margin / 2) + 5, null);
        }
    }
}
