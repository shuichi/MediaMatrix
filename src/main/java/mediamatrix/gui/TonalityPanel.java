package mediamatrix.gui;

import mediamatrix.db.MediaMatrix;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.SwingUtilities;

public final class TonalityPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private final MediaMatrixJava2DChartPanel chartPanel;
    private final MediaMatrixPanel matrixPanel;
    private final MediaMatrix mat;
    private Color bgColor = Color.lightGray;

    public TonalityPanel(MediaMatrix mat) {
        initComponents();
        this.mat = mat;
        chartPanel = new MediaMatrixJava2DChartPanel(mat, createChartStyle());
        matrixPanel = new MediaMatrixPanel(mat);
        chartScrollPane.setViewportView(chartPanel);
        aTabbedPane.addTab("Matrix", matrixPanel);
        chartScrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!scrollCheckBox.isSelected()) {
                    redraw();
                }
            }
        });
        redraw();
    }

    private void redraw() {
        chartPanel.setChartStyle(createChartStyle());
        chartPanel.setChartSize(resolveChartSize());
        if (scrollCheckBox.isSelected()) {
            chartScrollPane.getHorizontalScrollBar().setUnitIncrement(24);
        }
        SwingUtilities.invokeLater(chartPanel::requestRender);
        repaint();
    }

    private Java2DChartRenderer.ChartStyle createChartStyle() {
        return Java2DChartRenderer.ChartStyle.splinePanelStyle(bgColor, Color.white);
    }

    private Dimension resolveChartSize() {
        if (scrollCheckBox.isSelected()) {
            return new Dimension(Math.max(1, mat.getRows().length * 5), 400);
        }
        final Dimension extent = chartScrollPane.getViewport().getExtentSize();
        final int width = Math.max(1, extent.width > 0 ? extent.width : chartScrollPane.getWidth());
        return new Dimension(width, Math.max(1, mat.getWidth() * 5));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aTabbedPane = new javax.swing.JTabbedPane();
        chartScrollPane = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        scrollCheckBox = new javax.swing.JCheckBox();
        bgCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        aTabbedPane.addTab("Chart", chartScrollPane);

        add(aTabbedPane, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        scrollCheckBox.setText("Scroll");
        scrollCheckBox.setFocusable(false);
        scrollCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        scrollCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scrollCheckBoxActionPerformed(evt);
            }
        });
        jPanel1.add(scrollCheckBox);

        bgCheckBox.setSelected(true);
        bgCheckBox.setText("Background Color");
        bgCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgCheckBoxActionPerformed(evt);
            }
        });
        jPanel1.add(bgCheckBox);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

private void scrollCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollCheckBoxActionPerformed
    redraw();
}//GEN-LAST:event_scrollCheckBoxActionPerformed

private void bgCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgCheckBoxActionPerformed
    if (bgCheckBox.isSelected()) {
        bgColor = Color.lightGray;
    } else {
        bgColor = Color.white;
    }
    redraw();
}//GEN-LAST:event_bgCheckBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane aTabbedPane;
    private javax.swing.JCheckBox bgCheckBox;
    private javax.swing.JScrollPane chartScrollPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox scrollCheckBox;
    // End of variables declaration//GEN-END:variables
}
