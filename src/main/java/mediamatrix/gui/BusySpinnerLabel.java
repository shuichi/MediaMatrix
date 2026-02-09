package mediamatrix.gui;

import java.io.Serial;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Lightweight Swing-only busy indicator label with a rotating text spinner.
 */
public class BusySpinnerLabel extends JLabel {

    @Serial
    private static final long serialVersionUID = 8680296513830743306L;

    private static final String[] FRAMES = {"|", "/", "-", "\\"};

    private final String message;
    private final Timer timer;
    private int frameIndex;
    private boolean busy;

    public BusySpinnerLabel(String message) {
        super("", SwingConstants.CENTER);
        this.message = message == null ? "" : message;
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
            updateText();
            timer.start();
        } else {
            timer.stop();
            setText(message);
        }
    }

    public boolean isBusy() {
        return busy;
    }

    private void advanceFrame() {
        updateText();
        frameIndex = (frameIndex + 1) % FRAMES.length;
    }

    private void updateText() {
        setText(message + " " + FRAMES[frameIndex]);
    }
}
