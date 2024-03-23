package mediamatrix.gui;

import mediamatrix.utils.ImageUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.Serial;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

public final class LargeImagePanel extends javax.swing.JPanel {

    @Serial
    private static final long serialVersionUID = 1L;
    private transient BufferedImage image;
    private transient BufferedImage viewImage;
    private transient JComponent comp;
    private Point previous;

    public LargeImagePanel(BufferedImage img) {
        initComponents();
        this.image = img;
        this.viewImage = img;
        imageSlider.setValue(0);
        imageSlider.setMaximum(viewImage.getWidth());
        comp = new JComponent() {

            public static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
                final Graphics2D g2 = (Graphics2D) g;
                final int x = imageSlider.getValue();
                final int width = getWidth();
                final int height = viewImage.getHeight();
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (viewImage.getWidth() > i + x && viewImage.getHeight() > j) {
                            g2.setColor(new Color(viewImage.getRGB(i + x, j)));
                            g2.drawLine(i, j, i, j);
                        }
                    }
                }
            }
        };
        comp.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                imageSlider.setMaximum(viewImage.getWidth() - comp.getWidth());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                imageSlider.setMaximum(viewImage.getWidth() - comp.getWidth());
            }

            @Override
            public void componentShown(ComponentEvent e) {
                imageSlider.setMaximum(viewImage.getWidth() - comp.getWidth());
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                imageSlider.setMaximum(viewImage.getWidth() - comp.getWidth());
            }
        });
        comp.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int diff = Math.abs(e.getPoint().x - previous.x);
                if (e.getPoint().x > previous.x) {
                    diff = diff * -1;
                }
                previous = e.getPoint();
                imageSlider.setValue(imageSlider.getValue() + diff);
            }
        });
        comp.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(CursorUtils.getOpenHandCursor());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                previous = e.getPoint();
                setCursor(CursorUtils.getClosedHandCursor());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(CursorUtils.getOpenHandCursor());
            }
        });
        add(comp, BorderLayout.CENTER);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imageSlider = new javax.swing.JSlider();
        jPanel1 = new javax.swing.JPanel();
        ratioSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        updateButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        imageSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                imageSliderStateChanged(evt);
            }
        });
        add(imageSlider, java.awt.BorderLayout.PAGE_END);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        ratioSpinner.setModel(new javax.swing.SpinnerNumberModel(100, 10, 100, 10));
        jPanel1.add(ratioSpinner);

        jLabel1.setText("%");
        jPanel1.add(jLabel1);

        updateButton.setText("Update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });
        jPanel1.add(updateButton);

        add(jPanel1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void imageSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_imageSliderStateChanged
        if (comp != null) {
            comp.repaint();
        }
    }//GEN-LAST:event_imageSliderStateChanged

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<BufferedImage, Object>() {

            @Override
            protected BufferedImage doInBackground() throws Exception {
                comp.setVisible(false);
                viewImage = ImageUtilities.imageToBufferedImage(ImageUtilities.createThumbnail(image, (int) Math.ceil(image.getWidth() * 0.01d * ((Integer) ratioSpinner.getValue())), image.getHeight()));
                return viewImage;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                imageSlider.setMaximum(viewImage.getWidth() - comp.getWidth());
                comp.setVisible(true);
            }
        }.execute();
    }//GEN-LAST:event_updateButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider imageSlider;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSpinner ratioSpinner;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
}
