package mediamatrix.gui;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class DialogUtils {

    private static JDialog jvmDialog;

    public static void showJVMMemoryStatDialog(Window parent) {
        if (jvmDialog == null) {
            jvmDialog = new JDialog(parent);
            jvmDialog.setModal(false);
            jvmDialog.setTitle("JVM Memory Profiler");
            jvmDialog.getContentPane().add(new JVMMemoryProfilerPanel(), BorderLayout.CENTER);
            jvmDialog.pack();
        }
        jvmDialog.setVisible(true);
    }

    public static void saveWindowSize(Window window) {
        final Preferences prefs = Preferences.userNodeForPackage(window.getClass());
        prefs.putInt("x", window.getBounds().x);
        prefs.putInt("y", window.getBounds().y);
        prefs.putInt("w", window.getBounds().width);
        prefs.putInt("h", window.getBounds().height);
    }

    public static void loadWindowSize(Window window, int defaultWidth, int defaultHeight) {
        final Preferences prefs = Preferences.userNodeForPackage(window.getClass());
        int x = prefs.getInt("x", 0);
        int y = prefs.getInt("y", 0);
        int w = prefs.getInt("w", defaultWidth);
        int h = prefs.getInt("h", defaultHeight);
        final Rectangle maxRectangle = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        if (w + x > maxRectangle.width) {
            x = 0;
            w = maxRectangle.width;
        }
        if (h + y > maxRectangle.height) {
            y = 0;
            h = maxRectangle.height;
        }
        if (x < 0) {
            x = 0;
        }
        if (System.getProperty("os.name").indexOf("Mac") >= 0 && y < 20) {
            y = 20;
        }
        window.setBounds(x, y, w, h);
    }

    public static void showDialog(String title, JComponent comp, Window window) {
        final JDialog dialog = new JDialog(window, ModalityType.MODELESS);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setTitle(title);
        try {
            dialog.setIconImage(ImageIO.read(DialogUtils.class.getResource("/mediamatrix/resources/Icon.png")));
        } catch (IOException ignored) {
        }

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(comp, BorderLayout.CENTER);
        dialog.pack();
        final Rectangle maxRectangle = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int x = 0;
        int y = 0;
        int w = dialog.getWidth();
        int h = dialog.getHeight();
        if (w + x > maxRectangle.width) {
            x = 0;
            w = maxRectangle.width;
        }
        if (h < 400) {
            h = 400;
        }
        if (w < 400) {
            w = 400;
        }
        if (h > 400) {
            h = 400;
        }
        if (w > 400) {
            w = 400;
        }

        if (h + y > maxRectangle.height) {
            y = 0;
            h = maxRectangle.height;
        }
        if (x < 0) {
            x = 0;
        }
        if (System.getProperty("os.name").indexOf("Mac") >= 0 && y < 20) {
            y = 20;
        }
        dialog.setBounds(x, y, w, h);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

        public static JFrame showFrame(String title, JComponent comp) {
        final JFrame dialog = new JFrame(title);
        try {
            dialog.setIconImage(ImageIO.read(DialogUtils.class.getResource("/mediamatrix/resources/Icon.png")));
        } catch (IOException ignored) {
        }

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(comp, BorderLayout.CENTER);
        dialog.pack();
        final Rectangle maxRectangle = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int x = 0;
        int y = 0;
        int w = dialog.getWidth();
        int h = dialog.getHeight();
        if (w + x > maxRectangle.width) {
            x = 0;
            w = maxRectangle.width;
        }
        if (h < 400) {
            h = 400;
        }
        if (w < 400) {
            w = 400;
        }
        if (h + y > maxRectangle.height) {
            y = 0;
            h = maxRectangle.height;
        }
        if (x < 0) {
            x = 0;
        }
        if (System.getProperty("os.name").indexOf("Mac") >= 0 && y < 20) {
            y = 20;
        }
        dialog.setBounds(x, y, w, h);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return dialog;
    }


    public static void showDialog(String title, JComponent comp, JComponent parent) {
        showDialog(title, comp, SwingUtilities.getWindowAncestor(parent));
    }
}
