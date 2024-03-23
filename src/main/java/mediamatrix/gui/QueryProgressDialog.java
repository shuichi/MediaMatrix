package mediamatrix.gui;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import javax.swing.SwingUtilities;

public final class QueryProgressDialog extends javax.swing.JDialog implements PropertyChangeListener {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private long start;

    public QueryProgressDialog(Window frame) {
        super(frame, "Processing", ModalityType.APPLICATION_MODAL);
        initComponents();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        String strPropertyName = evt.getPropertyName();
        if ("result".equals(strPropertyName)) {
            if (SwingUtilities.isEventDispatchThread()) {
                setTitle("Preparing Result Browser");
                aProgressBar.setValue(0);
                aProgressBar.setIndeterminate(true);
                timeLabel.setText("");
                if (!isVisible()) {
                    setVisible(true);
                }
                repaint();
            } else {
                SwingUtilities.invokeLater(() -> {
                    setTitle("Preparing Result Browser");
                    aProgressBar.setValue(0);
                    aProgressBar.setIndeterminate(true);
                    timeLabel.setText("");
                    if (!isVisible()) {
                        setVisible(true);
                    }
                    repaint();
                });
            }
        }

        if ("complete".equals(strPropertyName)) {
            if (SwingUtilities.isEventDispatchThread()) {
                setVisible(false);
                dispose();
            } else {
                SwingUtilities.invokeLater(() -> {
                    setVisible(false);
                    dispose();
                });
            }
        }

        if (null != strPropertyName) switch (strPropertyName) {
            case "progress" -> {
                if (SwingUtilities.isEventDispatchThread()) {
                    if (!isVisible()) {
                        setVisible(true);
                    }
                    aProgressBar.setIndeterminate(false);
                    int progress = (Integer) evt.getNewValue();
                    aProgressBar.setValue(progress);
                    if (progress >= 100) {
                        setVisible(false);
                    } else {
                        timeLabel.setText("Elapsed Time:" + ((System.currentTimeMillis() - start) / 1000));
                        repaint();
                    }
                } else {
                    SwingUtilities.invokeLater(() -> {
                        if (!isVisible()) {
                            setVisible(true);
                        }
                        aProgressBar.setIndeterminate(false);
                        int progress = (Integer) evt.getNewValue();
                        aProgressBar.setValue(progress);
                        if (progress >= 100) {
                            setVisible(false);
                        } else {
                            timeLabel.setText("Elapsed Time:" + ((System.currentTimeMillis() - start) / 1000));
                            repaint();
                        }
                    });
                }
            }
            case "file" -> {
                if (SwingUtilities.isEventDispatchThread()) {
                    if (!isVisible()) {
                        setVisible(true);
                    }
                    aProgressBar.setIndeterminate(true);
                    setTitle("Processing: " + evt.getNewValue());
                } else {
                    SwingUtilities.invokeLater(() -> {
                        if (!isVisible()) {
                            setVisible(true);
                        }
                        setTitle("Processing: " + evt.getNewValue());
                        aProgressBar.setIndeterminate(true);
                    });
                }
            }
            case "exception" -> {
                if (SwingUtilities.isEventDispatchThread()) {
                    setVisible(false);
                    dispose();
                } else {
                    SwingUtilities.invokeLater(() -> {
                        setVisible(false);
                        dispose();
                    });
                }
            }
            default -> {
            }
        }
    }

    public void init(final int max) {
        SwingUtilities.invokeLater(() -> {
            aProgressBar.setIndeterminate(false);
            aProgressBar.setMinimum(0);
            aProgressBar.setMaximum(max);
            aProgressBar.setValue(0);
            start = System.currentTimeMillis();
            setLocationRelativeTo(null);
            pack();
            setVisible(true);
        });

    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aProgressBar = new javax.swing.JProgressBar();
        timeLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Processing");
        setModal(true);

        timeLabel.setText("Elapsed Time:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                    .addComponent(timeLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(aProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(timeLabel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar aProgressBar;
    private javax.swing.JLabel timeLabel;
    // End of variables declaration//GEN-END:variables
}
