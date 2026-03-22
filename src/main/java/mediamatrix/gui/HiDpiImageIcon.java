package mediamatrix.gui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.io.Serial;
import javax.swing.ImageIcon;

public final class HiDpiImageIcon extends ImageIcon {

    @Serial
    private static final long serialVersionUID = 1L;

    private int logicalWidth;
    private int logicalHeight;

    public HiDpiImageIcon(Image image, int logicalWidth, int logicalHeight) {
        super(image);
        this.logicalWidth = logicalWidth;
        this.logicalHeight = logicalHeight;
    }

    @Override
    public int getIconWidth() {
        return logicalWidth;
    }

    @Override
    public int getIconHeight() {
        return logicalHeight;
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        final Image image = getImage();
        if (image != null) {
            g.drawImage(image, x, y, logicalWidth, logicalHeight, c);
        }
    }

    public void setLogicalSize(int logicalWidth, int logicalHeight) {
        this.logicalWidth = logicalWidth;
        this.logicalHeight = logicalHeight;
    }
}
