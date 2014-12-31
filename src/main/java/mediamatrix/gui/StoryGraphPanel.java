package mediamatrix.gui;

import mediamatrix.mvc.MediaMatrixXYDataSetAdapter;
import mediamatrix.db.ChronoArchive;
import mediamatrix.db.MediaMatrix;
import mediamatrix.db.PrimitiveEngine;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class StoryGraphPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private ChartPanel chartPanel = new ChartPanel(null);
    private XYSplineRenderer renderer = new XYSplineRenderer();
    private NumberAxis xAxis = new NumberAxis("Time");
    private NumberAxis yAxis = new NumberAxis("Score");
    private MediaMatrixPanel matrixPanel;
    private MediaMatrix originalMat;
    private MediaMatrix viewMat;
    private MediaMatrix ivf;
    private Color bgColor = Color.lightGray;
    private ChronoArchive carc;

    public StoryGraphPanel(ChronoArchive c, MediaMatrix mat, MediaMatrix ivf) {
        this.carc = c;
        this.originalMat = mat;
        this.ivf = ivf;
        initComponents();
        renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

            @Override
            public String generateToolTip(XYDataset dataset, int series, int item) {
                final XYSeriesCollection collection = (XYSeriesCollection) dataset;
                final XYSeries xyseries = collection.getSeries(series);
                final XYDataItem xyitem = xyseries.getDataItem(item);
                return xyseries.getKey().toString() + ": " + xyitem.getYValue() + " at " + xyitem.getXValue();
            }
        });
        chartPanel.setDisplayToolTips(true);
        chartPanel.setMaximumDrawHeight(2000);
        chartPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent e) {
                if (e.getTrigger().getClickCount() >= 2) {
                    if (e.getEntity() instanceof XYItemEntity) {
                        final XYItemEntity entity = (XYItemEntity) e.getEntity();
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        SwingWorker<BufferedImage, Object> worker = new SwingWorker<BufferedImage, Object>() {

                            @Override
                            protected BufferedImage doInBackground() throws Exception {
                                int index = (int) entity.getDataset().getXValue(entity.getSeriesIndex(), entity.getItem());
                                return carc.getImage(index);
                            }

                            @Override
                            protected void done() {
                                try {
                                    final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(StoryGraphPanel.this));
                                    dialog.getContentPane().setLayout(new BorderLayout());
                                    dialog.getContentPane().add(new MunsellImagePanel(get(), carc.getColorImpressionKnowledge()), BorderLayout.CENTER);
                                    dialog.pack();
                                    dialog.setLocationRelativeTo(null);
                                    dialog.setVisible(true);
                                } catch (Exception ex) {
                                    StringWriter out = new StringWriter();
                                    ex.printStackTrace(new PrintWriter(out));
                                    JOptionPane.showMessageDialog(StoryGraphPanel.this, out.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            }
                        };
                        worker.execute();
                    }
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent e) {
            }
        });
        ((SpinnerNumberModel) clusterSpinner.getModel()).setMaximum(new Integer(mat.getHeight()));
        ((SpinnerNumberModel) clusterSpinner.getModel()).setValue(1);
        maxLabel.setText("/" + mat.getHeight());
        yAxis.setAutoRangeIncludesZero(false);
        final Font font = new Font("SanSerif", Font.PLAIN, 14);
        xAxis.setLabelFont(font);
        yAxis.setLabelFont(font);
        xAxis.setTickLabelFont(font);
        yAxis.setTickLabelFont(font);
        redraw();
    }

    private void redraw() {
        new SwingWorker<JFreeChart, Object>() {

            @Override
            protected JFreeChart doInBackground() throws Exception {
                final PrimitiveEngine pe = new PrimitiveEngine();
                viewMat = pe.cluster(originalMat, ((Integer) clusterSpinner.getValue()).intValue());
                if (ivf != null) {
                    viewMat = pe.mult(viewMat, ivf);
                    viewMat = pe.topk(viewMat, 10);
                }
                final XYPlot plot = new XYPlot(new MediaMatrixXYDataSetAdapter(viewMat), xAxis, yAxis, renderer);
                plot.setBackgroundPaint(bgColor);
                plot.setDomainGridlinePaint(Color.white);
                plot.setRangeGridlinePaint(Color.white);
                plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
                final JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
                chart.setBackgroundPaint(Color.white);
                final LegendTitle legendTitle = chart.getLegend();
                legendTitle.setItemFont(new Font("SanSerif", Font.PLAIN, 14));
                return chart;
            }

            @Override
            protected void done() {
                try {
                    chartPanel.setChart(get());
                    if (scrollCheckBox.isSelected()) {
                        final Dimension size = new Dimension(viewMat.getRows().length * 5, 400);
                        chartPanel.setMaximumDrawWidth(100000);
                        chartPanel.setMaximumDrawHeight(100000);
                        chartPanel.setSize(size);
                        chartPanel.setPreferredSize(size);
                    } else {
                        final Dimension size = new Dimension(chartScrollPane.getSize());
                        size.height = viewMat.getWidth() * 5;
                        chartPanel.setSize(size);
                        chartPanel.setPreferredSize(size);
                    }
                    chartPanel.setDomainZoomable(true);
                    chartPanel.setRangeZoomable(true);
                    if (matrixPanel != null) {
                        aTabbedPane.remove(matrixPanel);
                    }
                    matrixPanel = new MediaMatrixPanel(viewMat);
                    aTabbedPane.addTab("Matrix", matrixPanel);
                    chartScrollPane.setViewportView(chartPanel);
                    repaint();
                } catch (InterruptedException ex) {
                    Logger.getLogger(StoryGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(StoryGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.execute();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aTabbedPane = new javax.swing.JTabbedPane();
        chartScrollPane = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        segmentLabel = new javax.swing.JLabel();
        clusterSpinner = new javax.swing.JSpinner();
        maxLabel = new javax.swing.JLabel();
        scrollCheckBox = new javax.swing.JCheckBox();
        bgCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        aTabbedPane.addTab("Chart", chartScrollPane);

        add(aTabbedPane, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        segmentLabel.setText("Segment:");
        jPanel1.add(segmentLabel);

        clusterSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        clusterSpinner.setMinimumSize(new java.awt.Dimension(60, 20));
        clusterSpinner.setPreferredSize(new java.awt.Dimension(60, 20));
        clusterSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                clusterSpinnerStateChanged(evt);
            }
        });
        jPanel1.add(clusterSpinner);

        maxLabel.setText("/");
        jPanel1.add(maxLabel);

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

private void clusterSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_clusterSpinnerStateChanged
    redraw();
}//GEN-LAST:event_clusterSpinnerStateChanged

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
    private javax.swing.JSpinner clusterSpinner;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel maxLabel;
    private javax.swing.JCheckBox scrollCheckBox;
    private javax.swing.JLabel segmentLabel;
    // End of variables declaration//GEN-END:variables
}
