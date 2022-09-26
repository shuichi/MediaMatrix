package mediamatrix.gui;

import mediamatrix.db.CorrelationScore;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JScrollPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class StatisticsChartPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private JScrollPane view;
    private Set<CorrelationScore> scores;
    private String title;

    public StatisticsChartPanel(String title, Set<CorrelationScore> scores) {
        initComponents();
        this.scores = scores;
        this.title = title;
        update();
    }

    private void update() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final String seriesKey = "Color-Emotion";
        for (Iterator<CorrelationScore> it = scores.iterator(); it.hasNext();) {
            final CorrelationScore score = it.next();
            dataset.addValue(score.getValue(), seriesKey, score.getWord());
        }
        final Font font = new Font("SanSerif", Font.PLAIN, 12);
        final JFreeChart chart = ChartFactory.createBarChart(title, "Key", "Score", dataset, PlotOrientation.HORIZONTAL, false, false, false);
        chart.getTitle().setFont(new Font("SansSerif", Font.PLAIN, 14));
        chart.setBackgroundPaint(Color.white);
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDefaultItemLabelFont(font);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        final CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setLabelFont(font);
        categoryAxis.setTickLabelFont(font);
        categoryAxis.setCategoryMargin(0.2);
        categoryAxis.setUpperMargin(0.02);
        categoryAxis.setLowerMargin(0.02);
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(font);
        rangeAxis.setTickLabelFont(font);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperMargin(0.10);
        final ChartPanel chartPanel = new ChartPanel(chart);
        final Dimension size = new Dimension(800, 3000);
        chartPanel.setMaximumDrawHeight(3000);
        chartPanel.setSize(size);
        chartPanel.setPreferredSize(size);
        if (view != null) {
            remove(view);
            view = null;
        }
        view = new JScrollPane(chartPanel);
        add(view, BorderLayout.CENTER);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aToolBar = new javax.swing.JToolBar();
        aLabel = new javax.swing.JLabel();
        sortComboBox = new javax.swing.JComboBox<String>();

        setLayout(new java.awt.BorderLayout());

        aToolBar.setRollover(true);

        aLabel.setText("Order By:");
        aToolBar.add(aLabel);

        sortComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Score", "Word" }));
        sortComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortComboBoxActionPerformed(evt);
            }
        });
        aToolBar.add(sortComboBox);

        add(aToolBar, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

private void sortComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortComboBoxActionPerformed
    update();
}//GEN-LAST:event_sortComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aLabel;
    private javax.swing.JToolBar aToolBar;
    private javax.swing.JComboBox<String> sortComboBox;
    // End of variables declaration//GEN-END:variables
}
