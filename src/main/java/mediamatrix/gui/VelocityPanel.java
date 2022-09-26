package mediamatrix.gui;

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
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleInsets;

public class VelocityPanel extends JPanel {

    private static final long serialVersionUID = 2037729591706452824L;

    public VelocityPanel(final MediaMatrix mat) {
        super(new BorderLayout());
        new SwingWorker<JFreeChart, Object>() {

            @Override
            protected JFreeChart doInBackground() throws Exception {
                NumberAxis xAxis = new NumberAxis("Time");
                NumberAxis yAxis = new NumberAxis("Value");
                XYSeries series = new XYSeries("Velocity");
                final Font font = new Font("SanSerif", Font.PLAIN, 14);
                xAxis.setLabelFont(font);
                yAxis.setLabelFont(font);
                xAxis.setTickLabelFont(font);
                yAxis.setTickLabelFont(font);
                double previous = 0d;
                for (int i = 0; i < mat.getHeight(); i++) {
                    final double time = mat.getRow(i);
                    if (previous != mat.get(time, "velocity")) {
                        series.add(new XYDataItem(time, mat.get(time, "velocity")));
                        previous = mat.get(time, "velocity");
                    }
                }
                XYDataset data = new XYSeriesCollection(series);
                xAxis.setAutoRangeIncludesZero(false);
                yAxis.setAutoRangeIncludesZero(false);
                XYSplineRenderer renderer = new XYSplineRenderer();
                XYPlot plot = new XYPlot(data, xAxis, yAxis, renderer);
                plot.setBackgroundPaint(Color.white);
                plot.setDomainGridlinePaint(Color.lightGray);
                plot.setRangeGridlinePaint(Color.lightGray);
                plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
                plot.getRenderer().setSeriesPaint(0, Color.black);
                JFreeChart chart = new JFreeChart("Velocity", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
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
                    Logger.getLogger(VelocityPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(VelocityPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.execute();
    }
}
