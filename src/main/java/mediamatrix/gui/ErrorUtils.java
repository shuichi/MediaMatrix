package mediamatrix.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ErrorUtils {

    public static void showDialog(Exception ex, Component comp) {
        final StringWriter out = new StringWriter();
        ex.printStackTrace(new PrintWriter(out));
        JTextArea area = new JTextArea(out.toString());
        final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(comp), ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setTitle("ERROR");
        try {
            dialog.setIconImage(ImageIO.read(DialogUtils.class.getResource("/mediamatrix/resources/Icon.png")));
        } catch (IOException ignored) {
        }

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(new JScrollPane(area), BorderLayout.CENTER);
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
    }

    public static void showDialog(Exception ex) {
        final StringWriter out = new StringWriter();
        ex.printStackTrace(new PrintWriter(out));
        JOptionPane.showMessageDialog(null, out.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
    }
}
