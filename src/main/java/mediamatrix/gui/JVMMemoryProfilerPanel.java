/* Aspect-ARM -- Active Capability for Multimedia
 * Copyright (C) 2004 - 2006 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package mediamatrix.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class JVMMemoryProfilerPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private TimeSeries total;
    private TimeSeries free;
    private JVMMemoryProfiler profiler;
    private int historyCount = 20;
    private int frequency = 200;

    public JVMMemoryProfilerPanel() {
        initComponents();
        profiler = new JVMMemoryProfiler(frequency);
        profiler.addListener(new JVMMemoryProfilerListener() {

            @Override
            public void addScore(long t, long f) {
                total.add(new Millisecond(), t);
                free.add(new Millisecond(), f);
            }
        });

        total = new TimeSeries("Total Memory");
        total.setMaximumItemCount(historyCount);
        free = new TimeSeries("Free Memory");
        free.setMaximumItemCount(historyCount);

        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(total);
        dataset.addSeries(free);

        final DateAxis domain = new DateAxis("Time");
        final NumberAxis range = new NumberAxis("Memory");
        domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        final XYItemRenderer renderer = new DefaultXYItemRenderer();
        renderer.setSeriesPaint(0, Color.red);
        renderer.setSeriesPaint(1, Color.green);
        renderer.setBaseStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        final XYPlot plot = new XYPlot(dataset, domain, range, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        domain.setAutoRange(true);
        domain.setLowerMargin(0.0);
        domain.setUpperMargin(0.0);
        domain.setTickLabelsVisible(true);

        final JFreeChart chart = new JFreeChart("JVM Memory Usage", new Font("SansSerif", Font.BOLD, 24), plot, true);
        chart.setBackgroundPaint(Color.white);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), BorderFactory.createLineBorder(Color.black)));
        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 12));

        add(chartPanel, BorderLayout.CENTER);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        stopWatchButton = new javax.swing.JToggleButton();

        setLayout(new java.awt.BorderLayout());

        stopWatchButton.setText("Start");
        stopWatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopWatchButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(stopWatchButton);

        add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    private void stopWatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopWatchButtonActionPerformed
        if (stopWatchButton.isSelected()) {
            profiler.start();
            stopWatchButton.setText("Stop");
        } else {
            profiler.stop();
            stopWatchButton.setText("Start");
        }
    }//GEN-LAST:event_stopWatchButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton stopWatchButton;
    // End of variables declaration//GEN-END:variables
}
