package mediamatrix.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import mediamatrix.gui.DialogUtils;
import mediamatrix.gui.ErrorUtils;

public class MediaMatrixVisualizerPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final SpinnerNumberModel wModel = new SpinnerNumberModel(800, 400, 20000, 10);
    private final SpinnerNumberModel hModel = new SpinnerNumberModel(800, 400, 20000, 10);
    private final SpinnerNumberModel tModel = new SpinnerNumberModel(1.2d, 0d, 10d, 0.1d);
    private final JSpinner wSpinner = new JSpinner(wModel);
    private final JSpinner hSpinner = new JSpinner(hModel);
    private final JSpinner tSpinner = new JSpinner(tModel);
    private final JButton applyButton = new JButton("Apply");
    private final JTabbedPane tab = new JTabbedPane();
    private final File file;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                    File file = new File("C:/Temp/electro.carc");
                    JFrame frame = DialogUtils.showFrame(file.getAbsolutePath(), new MediaMatrixVisualizerPanel(file));
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(MediaMatrixVisualizerPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(MediaMatrixVisualizerPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(MediaMatrixVisualizerPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(MediaMatrixVisualizerPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public MediaMatrixVisualizerPanel(final File file) {
        super(new BorderLayout());
        this.file = file;
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateContent();
            }
        });
        final JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wSpinner.setPreferredSize(new Dimension(100, applyButton.getPreferredSize().height));
        hSpinner.setPreferredSize(new Dimension(100, applyButton.getPreferredSize().height));
        toolbar.add(new JLabel("Width:"));
        toolbar.add(wSpinner);
        toolbar.add(new JLabel("Height:"));
        toolbar.add(hSpinner);
        toolbar.add(new JLabel("Threshold:"));
        toolbar.add(tSpinner);
        toolbar.add(applyButton);
        add(toolbar, BorderLayout.NORTH);
        add(tab, BorderLayout.CENTER);
        updateContent();
    }

    private void updateContent() {
        tab.removeAll();
        try {
            int width = ((Number) wModel.getValue()).intValue();
            int height = ((Number) hModel.getValue()).intValue();
            double t = ((Number) tModel.getValue()).doubleValue();
            tab.addTab("MediaMatrix", new SelectableMediaMatrixVisualizerPanel(file, width, height));
            tab.addTab("Dominant Emotion", new DominanceEmergenceVisualizerPanel(file, width, height, t, false));
            tab.addTab("Emergent Emotion", new DominanceEmergenceVisualizerPanel(file, width, height, t, true));
        } catch (IOException ex) {
            ErrorUtils.showDialog(ex, this);
        }
    }
}
