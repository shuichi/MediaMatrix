package mediamatrix.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CursorUtils {

    protected static Cursor openHandCursor;
    protected static Cursor closedHandCursor;

    public static Cursor getOpenHandCursor() {
        if (openHandCursor == null) {
            try {
                BufferedImage image = ImageIO.read(CursorUtils.class.getResourceAsStream("/mediamatrix/resources/hand_open.png"));
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Dimension dim = toolkit.getBestCursorSize(image.getWidth(), image.getHeight());
                if (dim.width != image.getWidth() || dim.height != image.getHeight()) {
                    BufferedImage newimage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D g2 = (Graphics2D) newimage.getGraphics();
                    g2.drawImage(image, 0, 0, new Color(255, 0, 0, 0), null);
                    image = newimage;
                }
                openHandCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(15, 15), "OpenHandCursor");
            } catch (IOException ignored) {
            }
        }
        return openHandCursor;
    }

    public static Cursor getClosedHandCursor() {
        if (closedHandCursor == null) {
            try {
                BufferedImage image = ImageIO.read(CursorUtils.class.getResourceAsStream("/mediamatrix/resources/hand_closed.png"));
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Dimension dim = toolkit.getBestCursorSize(image.getWidth(), image.getHeight());
                if (dim.width != image.getWidth() || dim.height != image.getHeight()) {
                    BufferedImage newimage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D g2 = (Graphics2D) newimage.getGraphics();
                    g2.drawImage(image, 0, 0, new Color(255, 0, 0, 0), null);
                    image = newimage;
                }
                closedHandCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(15, 15), "ClosedHandCursor");
            } catch (IOException ignored) {
            }
        }
        return closedHandCursor;
    }
}
