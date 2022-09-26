package mediamatrix.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import mediamatrix.db.MediaMatrix;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleInsets;

public class TempoPanel extends JPanel {

    private static final long serialVersionUID = 8814721574976916780L;

    public TempoPanel(final MediaMatrix mat) {
        super(new BorderLayout());
        new SwingWorker<JFreeChart, Object>() {

            @Override
            protected JFreeChart doInBackground() throws Exception {
                NumberAxis xAxis = new NumberAxis("Time");
                NumberAxis yAxis = new NumberAxis("Value");
                XYSeries series = new XYSeries("Tempo");
                final Font font = new Font("SanSerif", Font.PLAIN, 14);
                xAxis.setLabelFont(font);
                yAxis.setLabelFont(font);
                xAxis.setTickLabelFont(font);
                yAxis.setTickLabelFont(font);
                double previous = 0d;
                for (int i = 0; i < mat.getHeight(); i++) {
                    final double time = mat.getRow(i);
                    if (previous != mat.get(time, "tempo")) {
                        series.add(time, mat.get(time, "tempo"));
                        previous = mat.get(time, "tempo");
                    }
                }
                XYSeriesCollection data = new XYSeriesCollection(series);
                xAxis.setAutoRangeIncludesZero(false);
                yAxis.setAutoRangeIncludesZero(false);
                XYStepRenderer renderer = new XYStepRenderer();
                XYPlot plot = new XYPlot(data, xAxis, yAxis, renderer);
                plot.getRenderer().setSeriesStroke(0, new BasicStroke(3.0f));
                plot.setBackgroundPaint(Color.white);
                plot.setDomainGridlinePaint(Color.lightGray);
                plot.setRangeGridlinePaint(Color.lightGray);
                plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
                plot.getRenderer().setSeriesPaint(0, Color.black);
                JFreeChart chart = new JFreeChart("Tempo", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
                chart.setBackgroundPaint(Color.white);
                return chart;
            }

            @Override
            protected void done() {
                try {
                    ChartPanel chartPanel = new ChartPanel(get(), false);
                    JTabbedPane tab = new JTabbedPane();
                    tab.add("Graph", chartPanel);
                    tab.add("Matrix", new MediaMatrixPanel(mat));
                    add(tab, BorderLayout.CENTER);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TempoPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(TempoPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.execute();

    }
}