package mediamatrix.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serial;
import java.io.Serializable;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Lightweight Swing-only busy indicator label with a Java2D spinner icon.
 */
public class BusySpinnerLabel extends JLabel {

    @Serial
    private static final long serialVersionUID = 8680296513830743306L;

    private static final int SPINNER_SIZE = 16;
    private static final int FRAME_COUNT = 12;

    private final String message;
    private final Timer timer;
    private final SpinnerIcon spinnerIcon = new SpinnerIcon();
    private int frameIndex;
    private boolean busy;

    public BusySpinnerLabel(String message) {
        super("", SwingConstants.CENTER);
        this.message = message == null ? "" : message;
        setText(this.message);
        setHorizontalTextPosition(SwingConstants.LEADING);
        setIconTextGap(6);
        timer = new Timer(120, e -> advanceFrame());
        setBusy(false);
    }

    public void setBusy(boolean busy) {
        if (this.busy == busy) {
            return;
        }
        this.busy = busy;
        if (busy) {
            frameIndex = 0;
            setIcon(spinnerIcon);
            repaint();
            timer.start();
        } else {
            timer.stop();
            setText(message);
            setIcon(null);
        }
    }

    public boolean isBusy() {
        return busy;
    }

    private void advanceFrame() {
        frameIndex = (frameIndex + 1) % FRAME_COUNT;
        repaint();
    }

    private final class SpinnerIcon implements Icon, Serializable {

        @Serial
        private static final long serialVersionUID = 5095221020116925393L;

        @Override
        public int getIconWidth() {
            return SPINNER_SIZE;
        }

        @Override
        public int getIconHeight() {
            return SPINNER_SIZE;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                Color color = c.getForeground();
                int diameter = SPINNER_SIZE - 5;
                int inset = (SPINNER_SIZE - diameter) / 2;
                int startAngle = 90 - (frameIndex * 360 / FRAME_COUNT);

                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 48));
                g2.drawOval(x + inset, y + inset, diameter, diameter);
                g2.setColor(color);
                g2.drawArc(x + inset, y + inset, diameter, diameter, startAngle, -105);
            } finally {
                g2.dispose();
            }
        }
    }
}
