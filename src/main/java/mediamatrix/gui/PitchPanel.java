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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

public class PitchPanel extends JPanel {

    private static final long serialVersionUID = 6903430797371699378L;

    public PitchPanel(final MediaMatrix mat) {
        super(new BorderLayout());
        new SwingWorker<JFreeChart, Object>() {

            @Override
            protected JFreeChart doInBackground() throws Exception {
                JFreeChart chart = ChartFactory.createScatterPlot("Pitch", "Time", "Pitch", new PitchMatrixXYDataset(mat), PlotOrientation.VERTICAL, false, true, false);
                XYPlot plot = (XYPlot) chart.getPlot();
                XYDotRenderer renderer = new XYDotRenderer();
                renderer.setDotWidth(2);
                renderer.setDotHeight(2);
                plot.setRenderer(renderer);
                plot.setBackgroundPaint(Color.white);
                plot.setDomainGridlinePaint(Color.lightGray);
                plot.setRangeGridlinePaint(Color.lightGray);
                plot.getRenderer().setSeriesPaint(0, Color.black);
                NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                domainAxis.setAutoRangeIncludesZero(false);
                final Font font = new Font("SanSerif", Font.PLAIN, 14);
                domainAxis.setLabelFont(font);
                rangeAxis.setLabelFont(font);
                domainAxis.setTickLabelFont(font);
                rangeAxis.setTickLabelFont(font);
                return chart;
            }

            @Override
            protected void done() {
                try {
                    JFreeChart chart = get();
                    ChartPanel chartPanel = new ChartPanel(chart);
                    chartPanel.setVerticalAxisTrace(true);
                    chartPanel.setHorizontalAxisTrace(true);
                    chartPanel.setDomainZoomable(true);
                    chartPanel.setRangeZoomable(true);
                    chart.setBackgroundPaint(Color.white);
                    JTabbedPane tab = new JTabbedPane();
                    tab.add("Graph", chartPanel);
                    tab.add("Matrix", new MediaMatrixPanel(mat));
                    add(tab, BorderLayout.CENTER);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PitchPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(PitchPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.execute();
    }
}

class PitchMatrixXYDataset extends AbstractXYDataset implements XYDataset, DomainInfo, RangeInfo {

    private static final long serialVersionUID = -4668895404886138678L;
    private Double[][] xValues;
    private Double[][] yValues;
    private int seriesCount;
    private int itemCount;
    private Number domainMin;
    private Number domainMax;
    private Number rangeMin;
    private Number rangeMax;
    private Range domainRange;
    private Range range;

    public PitchMatrixXYDataset(MediaMatrix mat) {
        this.seriesCount = 1;
        this.itemCount = mat.getHeight();
        this.xValues = new Double[seriesCount][itemCount];
        this.yValues = new Double[seriesCount][itemCount];

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < mat.getHeight(); i++) {
            this.xValues[0][i] = mat.getRow(i);
            if (this.xValues[0][i] < minX) {
                minX = this.xValues[0][i];
            }
            if (this.xValues[0][i] > maxX) {
                maxX = this.xValues[0][i];
            }
            for (int j = 0; j < mat.getWidth(); j++) {
                if (mat.get(i, j) > 0) {
                    this.yValues[0][i] = Double.valueOf(j);
                }
            }
        }

        this.domainMin = minX;
        this.domainMax = maxX;
        this.domainRange = new Range(minX, maxX);

        this.rangeMin = Double.valueOf(0);
        this.rangeMax = Double.valueOf(127);
        this.range = new Range(0, 127);

    }

    @Override
    public Number getX(int series, int item) {
        return this.xValues[series][item];
    }

    @Override
    public Number getY(int series, int item) {
        return this.yValues[series][item];
    }

    @Override
    public int getSeriesCount() {
        return this.seriesCount;
    }

    @Override
    public Comparable<String> getSeriesKey(int series) {
        return "Note";
    }

    @Override
    public int getItemCount(int series) {
        return this.itemCount;
    }

    public double getDomainLowerBound() {
        return this.domainMin.doubleValue();
    }

    @Override
    public double getDomainLowerBound(boolean includeInterval) {
        return this.domainMin.doubleValue();
    }

    public double getDomainUpperBound() {
        return this.domainMax.doubleValue();
    }

    @Override
    public double getDomainUpperBound(boolean includeInterval) {
        return this.domainMax.doubleValue();
    }

    public Range getDomainBounds() {
        return this.domainRange;
    }

    @Override
    public Range getDomainBounds(boolean includeInterval) {
        return this.domainRange;
    }

    public Range getDomainRange() {
        return this.domainRange;
    }

    public double getRangeLowerBound() {
        return this.rangeMin.doubleValue();
    }

    @Override
    public double getRangeLowerBound(boolean includeInterval) {
        return this.rangeMin.doubleValue();
    }

    public double getRangeUpperBound() {
        return this.rangeMax.doubleValue();
    }

    @Override
    public double getRangeUpperBound(boolean includeInterval) {
        return this.rangeMax.doubleValue();
    }

    @Override
    public Range getRangeBounds(boolean includeInterval) {
        return this.range;
    }

    public Range getValueRange() {
        return this.range;
    }

    public Number getMinimumDomainValue() {
        return this.domainMin;
    }

    public Number getMaximumDomainValue() {
        return this.domainMax;
    }

    public Number getMinimumRangeValue() {
        return this.domainMin;
    }

    public Number getMaximumRangeValue() {
        return this.domainMax;
    }
}
