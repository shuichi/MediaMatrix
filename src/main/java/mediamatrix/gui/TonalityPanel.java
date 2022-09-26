package mediamatrix.gui;

import mediamatrix.mvc.MediaMatrixXYDataSetAdapter;
import mediamatrix.db.MediaMatrix;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleInsets;

public class TonalityPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private ChartPanel chartPanel = new ChartPanel(null);
    private XYSplineRenderer renderer = new XYSplineRenderer();
    private NumberAxis xAxis = new NumberAxis("Time");
    private NumberAxis yAxis = new NumberAxis("Score");
    private MediaMatrixPanel matrixPanel;
    private MediaMatrix mat;
    private Color bgColor = Color.lightGray;

    public TonalityPanel(MediaMatrix mat) {
        initComponents();
        this.mat = mat;
        renderer.setDefaultToolTipGenerator(new XYToolTipGenerator() {

            public String generateToolTip(XYDataset dataset, int series, int item) {
                final XYSeriesCollection collection = (XYSeriesCollection) dataset;
                final XYSeries xyseries = collection.getSeries(series);
                final XYDataItem xyitem = xyseries.getDataItem(item);
                return xyseries.getKey().toString() + ": " + xyitem.getYValue();
            }
        });
        chartPanel.setDisplayToolTips(true);
        chartPanel.setMaximumDrawHeight(2000);
        xAxis.setAutoRangeIncludesZero(false);
        yAxis.setAutoRangeIncludesZero(false);
        final Font font = new Font("SanSerif", Font.PLAIN, 14);
        xAxis.setLabelFont(font);
        yAxis.setLabelFont(font);
        xAxis.setTickLabelFont(font);
        yAxis.setTickLabelFont(font);
        redraw();
    }

    private void redraw() {
        final XYPlot plot = new XYPlot(new MediaMatrixXYDataSetAdapter(mat), xAxis, yAxis, renderer);
        plot.setBackgroundPaint(bgColor);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
        final JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(Color.white);
        chartPanel.setChart(chart);
        final LegendTitle legendTitle = chart.getLegend();
        legendTitle.setItemFont(new Font("SanSerif", Font.PLAIN, 14));
        if (scrollCheckBox.isSelected()) {
            final Dimension size = new Dimension(mat.getRows().length * 5, 400);
            chartPanel.setMaximumDrawWidth(100000);
            chartPanel.setMaximumDrawHeight(100000);
            chartPanel.setSize(size);
            chartPanel.setPreferredSize(size);
        } else {
            final Dimension size = new Dimension(chartScrollPane.getSize());
            size.height = mat.getWidth() * 5;
            chartPanel.setSize(size);
            chartPanel.setPreferredSize(size);
        }
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);

        if (matrixPanel != null) {
            aTabbedPane.remove(matrixPanel);
        }
        matrixPanel = new MediaMatrixPanel(mat);
        aTabbedPane.addTab("Matrix", matrixPanel);
        chartScrollPane.setViewportView(chartPanel);
        repaint();
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
