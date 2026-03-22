package mediamatrix.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import mediamatrix.db.MediaMatrix;
import org.junit.jupiter.api.Test;

class Java2DChartRendererTest {

    @Test
    void rendersMatrixChartWithExpectedSize() {
        final MediaMatrix matrix = createSampleMatrix();
        final BufferedImage actual = new Java2DChartRenderer().createChartImage(matrix, Color.LIGHT_GRAY, 900, 420);

        assertEquals(900, actual.getWidth());
        assertEquals(420, actual.getHeight());
        assertContainsNonBackgroundPixels(actual, Color.white.getRGB());
    }

    @Test
    void rendersSingleSeriesSplineChart() {
        final Java2DChartRenderer.ChartSpec spec = new Java2DChartRenderer.ChartSpec(
                List.of(new Java2DChartRenderer.SeriesData(
                        "Velocity",
                        new double[] {0.25, 1.0, 2.4, 3.8, 5.0},
                        new double[] {36.0, 48.0, 44.0, 60.0, 52.0},
                        Java2DChartRenderer.SeriesStyle.spline(Color.black, 1.0f, false))),
                Java2DChartRenderer.ChartStyle.xyPanelStyle("Velocity", "Value"));

        final BufferedImage actual = new Java2DChartRenderer().createChartImage(spec, 760, 320);

        assertEquals(760, actual.getWidth());
        assertEquals(320, actual.getHeight());
        assertContainsColor(actual, Color.black.getRGB());
    }

    @Test
    void rendersStepChart() {
        final Java2DChartRenderer.ChartSpec spec = new Java2DChartRenderer.ChartSpec(
                List.of(new Java2DChartRenderer.SeriesData(
                        "Tempo",
                        new double[] {0.0, 1.0, 2.5, 3.2, 4.8},
                        new double[] {120.0, 120.0, 136.0, 96.0, 144.0},
                        Java2DChartRenderer.SeriesStyle.step(Color.black, 3.0f))),
                Java2DChartRenderer.ChartStyle.xyPanelStyle("Tempo", "Value"));

        final BufferedImage actual = new Java2DChartRenderer().createChartImage(spec, 760, 320);

        assertEquals(760, actual.getWidth());
        assertEquals(320, actual.getHeight());
        assertContainsColor(actual, Color.black.getRGB());
    }

    @Test
    void rendersScatterChartWithFixedRange() {
        final Java2DChartRenderer.ChartSpec spec = new Java2DChartRenderer.ChartSpec(
                List.of(new Java2DChartRenderer.SeriesData(
                        "Note",
                        new double[] {0.25, 0.75, 1.25, 1.75, 2.25, 2.75},
                        new double[] {42.0, 47.0, 55.0, 51.0, 63.0, 60.0},
                        Java2DChartRenderer.SeriesStyle.scatter(Color.black, 2, 2))),
                Java2DChartRenderer.ChartStyle.xyPanelStyle("Pitch", "Pitch"),
                null,
                new Java2DChartRenderer.AxisRange(0.0, 127.0));

        final BufferedImage actual = new Java2DChartRenderer().createChartImage(spec, 760, 320);

        assertEquals(760, actual.getWidth());
        assertEquals(320, actual.getHeight());
        assertContainsColor(actual, Color.black.getRGB());
    }

    @Test
    void chartSpecAffectsRenderedOutput() {
        final Java2DChartRenderer renderer = new Java2DChartRenderer();
        final Java2DChartRenderer.ChartSpec left = new Java2DChartRenderer.ChartSpec(
                List.of(new Java2DChartRenderer.SeriesData(
                        "Series",
                        new double[] {0.0, 1.0, 2.0},
                        new double[] {10.0, 20.0, 30.0},
                        Java2DChartRenderer.SeriesStyle.spline(Color.black, 1.0f, false))),
                Java2DChartRenderer.ChartStyle.xyPanelStyle("A", "Value"));
        final Java2DChartRenderer.ChartSpec right = new Java2DChartRenderer.ChartSpec(
                List.of(new Java2DChartRenderer.SeriesData(
                        "Series",
                        new double[] {0.0, 1.0, 2.0},
                        new double[] {30.0, 20.0, 10.0},
                        Java2DChartRenderer.SeriesStyle.spline(Color.black, 1.0f, false))),
                Java2DChartRenderer.ChartStyle.xyPanelStyle("B", "Value"));

        final BufferedImage first = renderer.createChartImage(left, 600, 240);
        final BufferedImage second = renderer.createChartImage(right, 600, 240);

        assertNotEquals(imageFingerprint(first), imageFingerprint(second));
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

    private static void assertContainsNonBackgroundPixels(BufferedImage image, int backgroundRgb) {
        boolean found = false;
        for (int y = 0; y < image.getHeight() && !found; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != backgroundRgb) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
    }

    private static void assertContainsColor(BufferedImage image, int rgb) {
        boolean found = false;
        for (int y = 0; y < image.getHeight() && !found; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) == rgb) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
    }

    private static long imageFingerprint(BufferedImage image) {
        long hash = 1469598103934665603L;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                hash ^= image.getRGB(x, y);
                hash *= 1099511628211L;
            }
        }
        return hash;
    }
}
