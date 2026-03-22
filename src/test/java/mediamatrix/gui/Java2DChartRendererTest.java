package mediamatrix.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import mediamatrix.db.MediaMatrix;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.jupiter.api.Test;

class Java2DChartRendererTest {

    @Test
    void rendersImageCloseToJFreeChartOutput() {
        final MediaMatrix matrix = createSampleMatrix();
        final BufferedImage actual = new Java2DChartRenderer().createChartImage(matrix, Color.LIGHT_GRAY, 900, 420);
        final BufferedImage expected = createBaseline(matrix, Color.LIGHT_GRAY, 900, 420);

        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        assertTrue(averagePixelDifference(expected, actual) < 35.0);
    }

    @Test
    void rendersAtRequestedHiDpiScale() {
        final MediaMatrix matrix = createSampleMatrix();
        final BufferedImage actual = new Java2DChartRenderer().createChartImage(matrix, Color.LIGHT_GRAY, 320, 180, 2.0, 2.0);

        assertEquals(640, actual.getWidth());
        assertEquals(360, actual.getHeight());
    }

    @Test
    void hiDpiIconKeepsLogicalSize() {
        final BufferedImage image = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB);
        final HiDpiImageIcon icon = new HiDpiImageIcon(image, 320, 180);

        assertEquals(320, icon.getIconWidth());
        assertEquals(180, icon.getIconHeight());
        assertEquals(image, icon.getImage());
    }

    private static MediaMatrix createSampleMatrix() {
        final double[] rows = new double[24];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }
        final MediaMatrix matrix = new MediaMatrix(rows, new String[] {"Alpha", "Beta", "Gamma"});
        for (int i = 0; i < rows.length; i++) {
            matrix.set(rows[i], "Alpha", Math.sin(i * 0.3) * 0.8 + 0.2);
            matrix.set(rows[i], "Beta", Math.cos(i * 0.2) * 0.5 + 0.7);
            matrix.set(rows[i], "Gamma", (i % 5) * 0.18 - 0.1);
        }
        return matrix;
    }

    private static BufferedImage createBaseline(MediaMatrix matrix, Color bgColor, int width, int height) {
        final XYSplineRenderer renderer = new XYSplineRenderer();
        final NumberAxis xAxis = new NumberAxis("Time");
        final NumberAxis yAxis = new NumberAxis("Score");
        final XYPlot plot = new XYPlot(createDataset(matrix), xAxis, yAxis, renderer);
        plot.setBackgroundPaint(bgColor);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
        final JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(Color.white);
        final LegendTitle legendTitle = chart.getLegend();
        legendTitle.setItemFont(new Font("SanSerif", Font.PLAIN, 14));
        return chart.createBufferedImage(width, height);
    }

    private static XYSeriesCollection createDataset(MediaMatrix matrix) {
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for (int column = 0; column < matrix.getWidth(); column++) {
            final XYSeries series = new XYSeries(matrix.getColumn(column));
            for (int row = 0; row < matrix.getHeight(); row++) {
                series.add(row, matrix.get(row, column));
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    private static double averagePixelDifference(BufferedImage expected, BufferedImage actual) {
        long total = 0;
        final long pixels = (long) expected.getWidth() * expected.getHeight();
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                final int expectedRgb = expected.getRGB(x, y);
                final int actualRgb = actual.getRGB(x, y);
                total += Math.abs(((expectedRgb >> 16) & 0xFF) - ((actualRgb >> 16) & 0xFF));
                total += Math.abs(((expectedRgb >> 8) & 0xFF) - ((actualRgb >> 8) & 0xFF));
                total += Math.abs((expectedRgb & 0xFF) - (actualRgb & 0xFF));
            }
        }
        return total / (double) pixels;
    }
}
