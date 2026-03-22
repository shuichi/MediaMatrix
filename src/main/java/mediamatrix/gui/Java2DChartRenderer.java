package mediamatrix.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import mediamatrix.db.MediaMatrix;

public final class Java2DChartRenderer {

    private static final Font AXIS_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font TICK_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);
    private static final Font LEGEND_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 18);

    private static final Stroke AXIS_STROKE = new BasicStroke(0.5f);
    private static final Stroke GRID_STROKE = new BasicStroke(0.5f);
    private static final Stroke PLOT_OUTLINE_STROKE = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke SERIES_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);

    private static final Color CHART_BACKGROUND = Color.WHITE;
    private static final Color AXIS_COLOR = Color.GRAY;
    private static final Color GRID_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = Color.BLACK;

    private static final double CHART_TOP_INSET = 4.0;
    private static final double CHART_LEFT_INSET = 8.0;
    private static final double CHART_BOTTOM_INSET = 4.0;
    private static final double CHART_RIGHT_INSET = 8.0;
    private static final double AXIS_LABEL_GAP = 6.0;
    private static final double TICK_LABEL_GAP = 4.0;
    private static final double TICK_MARK_LENGTH = 2.0;
    private static final double LEGEND_TOP_GAP = 6.0;
    private static final double LEGEND_BOTTOM_GAP = 6.0;
    private static final double LEGEND_ROW_GAP = 4.0;
    private static final double LEGEND_ITEM_GAP = 12.0;
    private static final double LEGEND_SYMBOL_GAP = 4.0;
    private static final double LEGEND_LINE_LENGTH = 14.0;
    private static final double TITLE_GAP = 6.0;
    private static final int SPLINE_PRECISION = 5;
    private static final double AUTO_MARGIN_RATIO = 0.05;

    private static final Color[] DEFAULT_SERIES_COLORS = {
        new Color(0xFF, 0x55, 0x55),
        new Color(0x55, 0x55, 0xFF),
        new Color(0x55, 0xFF, 0x55),
        new Color(0xFF, 0xFF, 0x55),
        new Color(0xFF, 0x55, 0xFF),
        new Color(0x55, 0xFF, 0xFF),
        Color.PINK,
        Color.GRAY,
        new Color(0xC0, 0x00, 0x00),
        new Color(0x00, 0x00, 0xC0),
        new Color(0x00, 0xC0, 0x00),
        new Color(0xC0, 0xC0, 0x00),
        new Color(0xC0, 0x00, 0xC0),
        new Color(0x00, 0xC0, 0xC0),
        Color.DARK_GRAY,
        new Color(0xFF, 0x40, 0x40),
        new Color(0x40, 0x40, 0xFF),
        new Color(0x40, 0xFF, 0x40),
        new Color(0xFF, 0xFF, 0x40),
        new Color(0xFF, 0x40, 0xFF),
        new Color(0x40, 0xFF, 0xFF),
        Color.LIGHT_GRAY,
        new Color(0x80, 0x00, 0x00),
        new Color(0x00, 0x00, 0x80),
        new Color(0x00, 0x80, 0x00),
        new Color(0x80, 0x80, 0x00),
        new Color(0x80, 0x00, 0x80),
        new Color(0x00, 0x80, 0x80),
        new Color(0xFF, 0x80, 0x80),
        new Color(0x80, 0x80, 0xFF),
        new Color(0x80, 0xFF, 0x80),
        new Color(0xFF, 0xFF, 0x80),
        new Color(0xFF, 0x80, 0xFF),
        new Color(0x80, 0xFF, 0xFF)
    };

    private static final Shape[] DEFAULT_SHAPES = createStandardSeriesShapes();

    public static record ChartStyle(
            String title,
            Font titleFont,
            Color chartBackgroundColor,
            Color plotBackgroundColor,
            Color axisColor,
            Color gridColor,
            Color textColor,
            Font axisLabelFont,
            Font tickLabelFont,
            Font legendFont,
            String xAxisLabel,
            String yAxisLabel,
            boolean legendVisible,
            boolean includeDomainZero,
            boolean includeRangeZero,
            double minimumDomainTickStep,
            double minimumRangeTickStep) implements java.io.Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        public ChartStyle {
            Objects.requireNonNull(title, "title");
            Objects.requireNonNull(titleFont, "titleFont");
            Objects.requireNonNull(chartBackgroundColor, "chartBackgroundColor");
            Objects.requireNonNull(plotBackgroundColor, "plotBackgroundColor");
            Objects.requireNonNull(axisColor, "axisColor");
            Objects.requireNonNull(gridColor, "gridColor");
            Objects.requireNonNull(textColor, "textColor");
            Objects.requireNonNull(axisLabelFont, "axisLabelFont");
            Objects.requireNonNull(tickLabelFont, "tickLabelFont");
            Objects.requireNonNull(legendFont, "legendFont");
            Objects.requireNonNull(xAxisLabel, "xAxisLabel");
            Objects.requireNonNull(yAxisLabel, "yAxisLabel");
        }

        public ChartStyle withPlotBackgroundColor(Color newPlotBackgroundColor) {
            return new ChartStyle(
                    title,
                    titleFont,
                    chartBackgroundColor,
                    Objects.requireNonNull(newPlotBackgroundColor, "newPlotBackgroundColor"),
                    axisColor,
                    gridColor,
                    textColor,
                    axisLabelFont,
                    tickLabelFont,
                    legendFont,
                    xAxisLabel,
                    yAxisLabel,
                    legendVisible,
                    includeDomainZero,
                    includeRangeZero,
                    minimumDomainTickStep,
                    minimumRangeTickStep);
        }

        public static ChartStyle defaultStyle(Color plotBackgroundColor) {
            return new ChartStyle(
                    "",
                    TITLE_FONT,
                    CHART_BACKGROUND,
                    plotBackgroundColor,
                    AXIS_COLOR,
                    GRID_COLOR,
                    TEXT_COLOR,
                    AXIS_LABEL_FONT,
                    TICK_LABEL_FONT,
                    LEGEND_FONT,
                    "Time",
                    "Score",
                    true,
                    true,
                    true,
                    1.0,
                    0.0);
        }

        public static ChartStyle splinePanelStyle(Color plotBackgroundColor, Color gridColor) {
            final Font font = new Font("SansSerif", Font.PLAIN, 14);
            return new ChartStyle(
                    "",
                    TITLE_FONT,
                    CHART_BACKGROUND,
                    plotBackgroundColor,
                    AXIS_COLOR,
                    gridColor,
                    TEXT_COLOR,
                    font,
                    font,
                    font,
                    "Time",
                    "Score",
                    true,
                    false,
                    false,
                    1.0,
                    0.0);
        }

        public static ChartStyle xyPanelStyle(String title, String yAxisLabel) {
            return xyPanelStyle(title, yAxisLabel, Color.white, Color.lightGray);
        }

        public static ChartStyle xyPanelStyle(String title, String yAxisLabel, Color plotBackgroundColor, Color gridColor) {
            final Font font = new Font("SansSerif", Font.PLAIN, 14);
            return new ChartStyle(
                    Objects.requireNonNull(title, "title"),
                    TITLE_FONT,
                    CHART_BACKGROUND,
                    plotBackgroundColor,
                    AXIS_COLOR,
                    gridColor,
                    TEXT_COLOR,
                    font,
                    font,
                    font,
                    "Time",
                    Objects.requireNonNull(yAxisLabel, "yAxisLabel"),
                    false,
                    false,
                    false,
                    0.0,
                    0.0);
        }
    }

    public enum SeriesRendererType {
        SPLINE,
        STEP,
        SCATTER
    }

    public static record SeriesStyle(
            SeriesRendererType rendererType,
            Color color,
            float strokeWidth,
            boolean shapesVisible,
            int dotWidth,
            int dotHeight) implements java.io.Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        public SeriesStyle {
            Objects.requireNonNull(rendererType, "rendererType");
            Objects.requireNonNull(color, "color");
        }

        public static SeriesStyle defaultSpline(Color color) {
            return spline(color, 1.0f, true);
        }

        public static SeriesStyle spline(Color color, float strokeWidth, boolean shapesVisible) {
            return new SeriesStyle(SeriesRendererType.SPLINE, color, strokeWidth, shapesVisible, 6, 6);
        }

        public static SeriesStyle step(Color color, float strokeWidth) {
            return new SeriesStyle(SeriesRendererType.STEP, color, strokeWidth, false, 6, 6);
        }

        public static SeriesStyle scatter(Color color, int dotWidth, int dotHeight) {
            return new SeriesStyle(SeriesRendererType.SCATTER, color, 1.0f, false, dotWidth, dotHeight);
        }
    }

    public static record SeriesData(
            String name,
            double[] xValues,
            double[] yValues,
            SeriesStyle style) implements java.io.Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        public SeriesData {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(xValues, "xValues");
            Objects.requireNonNull(yValues, "yValues");
            Objects.requireNonNull(style, "style");
            xValues = xValues.clone();
            yValues = yValues.clone();
        }
    }

    public static record AxisRange(double lower, double upper) implements java.io.Serializable {

        @Serial
        private static final long serialVersionUID = 1L;
    }

    public static record ChartSpec(
            List<SeriesData> series,
            ChartStyle style,
            AxisRange domainRangeOverride,
            AxisRange rangeRangeOverride) implements java.io.Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        public ChartSpec {
            Objects.requireNonNull(series, "series");
            Objects.requireNonNull(style, "style");
            series = List.copyOf(series);
        }

        public ChartSpec(List<SeriesData> series, ChartStyle style) {
            this(series, style, null, null);
        }

        public static ChartSpec fromMediaMatrix(MediaMatrix mat, ChartStyle style) {
            final List<SeriesData> series = new ArrayList<>();
            for (int column = 0; column < mat.getWidth(); column++) {
                final double[] xValues = new double[mat.getHeight()];
                final double[] yValues = new double[mat.getHeight()];
                for (int row = 0; row < mat.getHeight(); row++) {
                    xValues[row] = row;
                    yValues[row] = mat.get(row, column);
                }
                series.add(new SeriesData(
                        mat.getColumn(column),
                        xValues,
                        yValues,
                        SeriesStyle.defaultSpline(defaultSeriesColor(column))));
            }
            return new ChartSpec(series, style);
        }
    }

    public BufferedImage createChartImage(MediaMatrix mat, Color bgColor, int width, int height) {
        return createChartImage(ChartSpec.fromMediaMatrix(mat, ChartStyle.defaultStyle(bgColor)), width, height, 1.0, 1.0);
    }

    public BufferedImage createChartImage(MediaMatrix mat, Color bgColor, int width, int height, double scaleX, double scaleY) {
        return createChartImage(ChartSpec.fromMediaMatrix(mat, ChartStyle.defaultStyle(bgColor)), width, height, scaleX, scaleY);
    }

    public BufferedImage createChartImage(MediaMatrix mat, ChartStyle style, int width, int height) {
        return createChartImage(ChartSpec.fromMediaMatrix(mat, style), width, height, 1.0, 1.0);
    }

    public BufferedImage createChartImage(MediaMatrix mat, ChartStyle style, int width, int height, double scaleX, double scaleY) {
        return createChartImage(ChartSpec.fromMediaMatrix(mat, style), width, height, scaleX, scaleY);
    }

    public BufferedImage createChartImage(ChartSpec spec, int width, int height) {
        return createChartImage(spec, width, height, 1.0, 1.0);
    }

    public BufferedImage createChartImage(ChartSpec spec, int width, int height, double scaleX, double scaleY) {
        final ChartStyle style = spec.style();
        final int physicalWidth = Math.max(1, (int) Math.ceil(width * Math.max(1.0, scaleX)));
        final int physicalHeight = Math.max(1, (int) Math.ceil(height * Math.max(1.0, scaleY)));
        final BufferedImage image = new BufferedImage(physicalWidth, physicalHeight, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2 = image.createGraphics();
        try {
            configureGraphics(g2);
            g2.setColor(style.chartBackgroundColor());
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());

            if (width < 10 || height < 10) {
                return image;
            }

            g2.scale(Math.max(1.0, scaleX), Math.max(1.0, scaleY));

            final ChartModel model = ChartModel.from(spec);
            final LegendLayout legend = layoutLegend(g2, model, style, width);
            final ChartLayout layout = layoutChart(g2, model, legend, style, width, height);

            drawTitle(g2, style, width);
            drawPlotBackground(g2, layout.dataArea, style.plotBackgroundColor());
            drawGridlines(g2, layout, style);
            drawSeries(g2, layout, model);
            drawPlotOutline(g2, layout.dataArea, style);
            drawAxes(g2, layout, style);
            drawLegend(g2, legend, style, width, height);
        } finally {
            g2.dispose();
        }
        return image;
    }

    private static void configureGraphics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static ChartLayout layoutChart(Graphics2D g2, ChartModel model, LegendLayout legend, ChartStyle style, int width, int height) {
        final FontMetrics axisLabelMetrics = g2.getFontMetrics(style.axisLabelFont());
        final FontMetrics tickMetrics = g2.getFontMetrics(style.tickLabelFont());
        final double titleSpace = style.title().isBlank() ? 0.0 : g2.getFontMetrics(style.titleFont()).getHeight() + TITLE_GAP;

        Rectangle2D.Double dataArea = new Rectangle2D.Double(
                CHART_LEFT_INSET + 64.0,
                CHART_TOP_INSET + titleSpace,
                Math.max(10.0, width - CHART_LEFT_INSET - CHART_RIGHT_INSET - 72.0),
                Math.max(10.0, height - CHART_TOP_INSET - titleSpace - CHART_BOTTOM_INSET - legend.height - 48.0));

        TickSet xTicks = TickSet.empty();
        TickSet yTicks = TickSet.empty();
        for (int i = 0; i < 3; i++) {
            xTicks = createTickSet(model.domainRange, Math.max(2, estimateHorizontalTickCount(dataArea.getWidth(), tickMetrics, model.domainRange)), style.minimumDomainTickStep());
            yTicks = createTickSet(model.rangeRange, Math.max(2, estimateVerticalTickCount(dataArea.getHeight(), tickMetrics)), style.minimumRangeTickStep());

            final double yTickWidth = maxTickLabelWidth(g2, yTicks, style);
            final double xTickHeight = tickMetrics.getHeight();
            final double left = CHART_LEFT_INSET + axisLabelMetrics.getHeight() + AXIS_LABEL_GAP + yTickWidth + TICK_LABEL_GAP + TICK_MARK_LENGTH;
            final double bottom = CHART_BOTTOM_INSET + legend.height + axisLabelMetrics.getHeight() + AXIS_LABEL_GAP + xTickHeight + TICK_LABEL_GAP + TICK_MARK_LENGTH;
            final double plotWidth = Math.max(10.0, width - left - CHART_RIGHT_INSET);
            final double plotHeight = Math.max(10.0, height - CHART_TOP_INSET - titleSpace - bottom);
            dataArea = new Rectangle2D.Double(left, CHART_TOP_INSET + titleSpace, plotWidth, plotHeight);
        }

        return new ChartLayout(dataArea, xTicks, yTicks, model.domainRange, model.rangeRange);
    }

    private static int estimateHorizontalTickCount(double plotWidth, FontMetrics metrics, Range range) {
        final String minLabel = formatTick(range.lower, 1.0);
        final String maxLabel = formatTick(range.upper, 1.0);
        final int labelWidth = Math.max(metrics.stringWidth(minLabel), metrics.stringWidth(maxLabel));
        final int slot = Math.max(18, labelWidth + 6);
        return (int) Math.max(2, Math.floor(plotWidth / slot));
    }

    private static int estimateVerticalTickCount(double plotHeight, FontMetrics metrics) {
        return (int) Math.max(2, Math.floor(plotHeight / Math.max(metrics.getHeight() + 6.0, 22.0)));
    }

    private static double maxTickLabelWidth(Graphics2D g2, TickSet tickSet, ChartStyle style) {
        final FontMetrics metrics = g2.getFontMetrics(style.tickLabelFont());
        double width = 0.0;
        for (Tick tick : tickSet.values) {
            width = Math.max(width, metrics.stringWidth(tick.label));
        }
        return width;
    }

    private static void drawTitle(Graphics2D g2, ChartStyle style, int width) {
        if (style.title().isBlank()) {
            return;
        }
        final FontMetrics metrics = g2.getFontMetrics(style.titleFont());
        g2.setFont(style.titleFont());
        g2.setColor(style.textColor());
        final double x = (width - metrics.stringWidth(style.title())) / 2.0;
        final double y = CHART_TOP_INSET + metrics.getAscent();
        g2.drawString(style.title(), (float) x, (float) y);
    }

    private static void drawPlotBackground(Graphics2D g2, Rectangle2D dataArea, Color bgColor) {
        g2.setColor(bgColor);
        g2.fill(dataArea);
    }

    private static void drawGridlines(Graphics2D g2, ChartLayout layout, ChartStyle style) {
        g2.setColor(style.gridColor());
        g2.setStroke(GRID_STROKE);
        for (Tick tick : layout.xTicks.values) {
            final double x = valueToX(tick.value, layout);
            g2.draw(new Line2D.Double(x, layout.dataArea.getMinY(), x, layout.dataArea.getMaxY()));
        }
        for (Tick tick : layout.yTicks.values) {
            final double y = valueToY(tick.value, layout);
            g2.draw(new Line2D.Double(layout.dataArea.getMinX(), y, layout.dataArea.getMaxX(), y));
        }
    }

    private static void drawPlotOutline(Graphics2D g2, Rectangle2D dataArea, ChartStyle style) {
        g2.setColor(style.axisColor());
        g2.setStroke(PLOT_OUTLINE_STROKE);
        g2.draw(dataArea);
    }

    private static void drawAxes(Graphics2D g2, ChartLayout layout, ChartStyle style) {
        g2.setColor(style.axisColor());
        g2.setStroke(AXIS_STROKE);
        g2.draw(new Line2D.Double(layout.dataArea.getMinX(), layout.dataArea.getMaxY(), layout.dataArea.getMaxX(), layout.dataArea.getMaxY()));
        g2.draw(new Line2D.Double(layout.dataArea.getMinX(), layout.dataArea.getMinY(), layout.dataArea.getMinX(), layout.dataArea.getMaxY()));

        drawXAxisTicks(g2, layout, style);
        drawYAxisTicks(g2, layout, style);
        drawAxisLabels(g2, layout, style);
    }

    private static void drawXAxisTicks(Graphics2D g2, ChartLayout layout, ChartStyle style) {
        final FontMetrics metrics = g2.getFontMetrics(style.tickLabelFont());
        g2.setFont(style.tickLabelFont());
        for (Tick tick : layout.xTicks.values) {
            final double x = valueToX(tick.value, layout);
            g2.setColor(style.axisColor());
            g2.draw(new Line2D.Double(x, layout.dataArea.getMaxY(), x, layout.dataArea.getMaxY() + TICK_MARK_LENGTH));
            g2.setColor(style.textColor());
            final double labelX = x - (metrics.stringWidth(tick.label) / 2.0);
            final double labelY = layout.dataArea.getMaxY() + TICK_MARK_LENGTH + TICK_LABEL_GAP + metrics.getAscent();
            g2.drawString(tick.label, (float) labelX, (float) labelY);
        }
    }

    private static void drawYAxisTicks(Graphics2D g2, ChartLayout layout, ChartStyle style) {
        final FontMetrics metrics = g2.getFontMetrics(style.tickLabelFont());
        g2.setFont(style.tickLabelFont());
        for (Tick tick : layout.yTicks.values) {
            final double y = valueToY(tick.value, layout);
            g2.setColor(style.axisColor());
            g2.draw(new Line2D.Double(layout.dataArea.getMinX() - TICK_MARK_LENGTH, y, layout.dataArea.getMinX(), y));
            g2.setColor(style.textColor());
            final double labelX = layout.dataArea.getMinX() - TICK_MARK_LENGTH - TICK_LABEL_GAP - metrics.stringWidth(tick.label);
            final double labelY = y + (metrics.getAscent() / 2.0) - 1.0;
            g2.drawString(tick.label, (float) labelX, (float) labelY);
        }
    }

    private static void drawAxisLabels(Graphics2D g2, ChartLayout layout, ChartStyle style) {
        final FontMetrics metrics = g2.getFontMetrics(style.axisLabelFont());
        g2.setFont(style.axisLabelFont());
        g2.setColor(style.textColor());

        if (!style.xAxisLabel().isBlank()) {
            final double x = layout.dataArea.getCenterX() - (metrics.stringWidth(style.xAxisLabel()) / 2.0);
            final double y = layout.dataArea.getMaxY() + TICK_MARK_LENGTH + TICK_LABEL_GAP + g2.getFontMetrics(style.tickLabelFont()).getHeight() + AXIS_LABEL_GAP + metrics.getAscent();
            g2.drawString(style.xAxisLabel(), (float) x, (float) y);
        }

        if (!style.yAxisLabel().isBlank()) {
            final FontRenderContext frc = g2.getFontRenderContext();
            final Rectangle2D bounds = style.axisLabelFont().getStringBounds(style.yAxisLabel(), frc);
            final AffineTransform original = g2.getTransform();
            final double rotateX = CHART_LEFT_INSET + metrics.getAscent();
            final double rotateY = layout.dataArea.getCenterY() + (bounds.getWidth() / 2.0);
            g2.rotate(-Math.PI / 2.0, rotateX, rotateY);
            g2.drawString(style.yAxisLabel(), (float) (rotateX - (bounds.getWidth() / 2.0)), (float) rotateY);
            g2.setTransform(original);
        }
    }

    private static void drawSeries(Graphics2D g2, ChartLayout layout, ChartModel model) {
        final Shape originalClip = g2.getClip();
        g2.clip(layout.dataArea);
        for (int series = 0; series < model.series.size(); series++) {
            final SeriesData seriesData = model.series.get(series);
            final List<Point2D.Double> points = collectPoints(seriesData, layout);
            if (points.isEmpty()) {
                continue;
            }

            final SeriesStyle style = seriesData.style();
            g2.setColor(style.color());
            switch (style.rendererType()) {
                case SPLINE -> drawSplineSeries(g2, points, style, series);
                case STEP -> drawStepSeries(g2, points, style);
                case SCATTER -> drawScatterSeries(g2, points, style);
            }
        }
        g2.setClip(originalClip);
    }

    private static List<Point2D.Double> collectPoints(SeriesData seriesData, ChartLayout layout) {
        final int size = Math.min(seriesData.xValues().length, seriesData.yValues().length);
        final List<Point2D.Double> points = new ArrayList<>(size);
        for (int item = 0; item < size; item++) {
            final double xValue = seriesData.xValues()[item];
            final double yValue = seriesData.yValues()[item];
            if (!Double.isFinite(xValue) || !Double.isFinite(yValue)) {
                continue;
            }
            points.add(new Point2D.Double(valueToX(xValue, layout), valueToY(yValue, layout)));
        }
        return points;
    }

    private static void drawSplineSeries(Graphics2D g2, List<Point2D.Double> points, SeriesStyle style, int seriesIndex) {
        g2.setStroke(createSeriesStroke(style.strokeWidth()));
        g2.draw(createSplinePath(points));
        if (style.shapesVisible()) {
            drawPointMarkers(g2, points, DEFAULT_SHAPES[seriesIndex % DEFAULT_SHAPES.length], style.color());
        }
    }

    private static void drawStepSeries(Graphics2D g2, List<Point2D.Double> points, SeriesStyle style) {
        g2.setStroke(createSeriesStroke(style.strokeWidth()));
        g2.draw(createStepPath(points));
    }

    private static void drawScatterSeries(Graphics2D g2, List<Point2D.Double> points, SeriesStyle style) {
        for (Point2D.Double point : points) {
            final double x = point.x - (style.dotWidth() / 2.0);
            final double y = point.y - (style.dotHeight() / 2.0);
            g2.fill(new Rectangle2D.Double(x, y, style.dotWidth(), style.dotHeight()));
        }
    }

    private static Stroke createSeriesStroke(float width) {
        return new BasicStroke(Math.max(0.5f, width), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
    }

    private static void drawPointMarkers(Graphics2D g2, List<Point2D.Double> points, Shape baseShape, Color color) {
        for (Point2D.Double point : points) {
            final AffineTransform transform = AffineTransform.getTranslateInstance(point.x, point.y);
            final Shape marker = transform.createTransformedShape(baseShape);
            g2.setColor(color);
            g2.fill(marker);
            g2.draw(marker);
        }
    }

    private static GeneralPath createSplinePath(List<Point2D.Double> points) {
        final GeneralPath path = new GeneralPath();
        final Point2D.Double start = points.get(0);
        path.moveTo(start.x, start.y);
        if (points.size() == 1) {
            return path;
        }
        if (points.size() == 2) {
            final Point2D.Double end = points.get(1);
            path.lineTo(end.x, end.y);
            return path;
        }

        final int size = points.size();
        final float[] d = new float[size];
        final float[] x = new float[size];
        final float[] a = new float[size];
        final float[] h = new float[size];
        final float[] sub = new float[size - 1];
        final float[] diag = new float[size - 1];
        final float[] sup = new float[size - 1];

        for (int i = 0; i < size; i++) {
            x[i] = (float) points.get(i).x;
            d[i] = (float) points.get(i).y;
        }
        for (int i = 1; i < size; i++) {
            h[i] = x[i] - x[i - 1];
        }
        for (int i = 1; i <= size - 2; i++) {
            diag[i] = (h[i] + h[i + 1]) / 3.0f;
            sup[i] = h[i + 1] / 6.0f;
            sub[i] = h[i] / 6.0f;
            a[i] = (d[i + 1] - d[i]) / h[i + 1] - (d[i] - d[i - 1]) / h[i];
        }
        solveTridiagonal(sub, diag, sup, a, size - 2);

        for (int i = 1; i <= size - 1; i++) {
            for (int j = 1; j <= SPLINE_PRECISION; j++) {
                final float t1 = (h[i] * j) / SPLINE_PRECISION;
                final float t2 = h[i] - t1;
                final float y = ((-a[i - 1] / 6.0f * (t2 + h[i]) * t1 + d[i - 1]) * t2
                        + (-a[i] / 6.0f * (t1 + h[i]) * t2 + d[i]) * t1) / h[i];
                final float t = x[i - 1] + t1;
                path.lineTo(t, y);
            }
        }
        return path;
    }

    private static GeneralPath createStepPath(List<Point2D.Double> points) {
        final GeneralPath path = new GeneralPath();
        final Point2D.Double start = points.get(0);
        path.moveTo(start.x, start.y);
        for (int i = 1; i < points.size(); i++) {
            final Point2D.Double previous = points.get(i - 1);
            final Point2D.Double current = points.get(i);
            path.lineTo(current.x, previous.y);
            path.lineTo(current.x, current.y);
        }
        return path;
    }

    private static void solveTridiagonal(float[] sub, float[] diag, float[] sup, float[] rhs, int n) {
        for (int i = 2; i <= n; i++) {
            sub[i] /= diag[i - 1];
            diag[i] -= sub[i] * sup[i - 1];
            rhs[i] -= sub[i] * rhs[i - 1];
        }
        rhs[n] /= diag[n];
        for (int i = n - 1; i >= 1; i--) {
            rhs[i] = (rhs[i] - sup[i] * rhs[i + 1]) / diag[i];
        }
    }

    private static void drawLegend(Graphics2D g2, LegendLayout legend, ChartStyle style, int width, int height) {
        if (legend.rows.isEmpty()) {
            return;
        }
        final FontMetrics metrics = g2.getFontMetrics(style.legendFont());
        g2.setFont(style.legendFont());
        double y = height - CHART_BOTTOM_INSET - legend.height + LEGEND_TOP_GAP + metrics.getAscent();
        for (LegendRow row : legend.rows) {
            double x = (width - row.width) / 2.0;
            for (LegendItem item : row.items) {
                final double midY = y - (metrics.getAscent() / 2.0) + (metrics.getHeight() / 2.0);
                final SeriesStyle itemStyle = item.style();
                g2.setColor(itemStyle.color());
                if (itemStyle.rendererType() == SeriesRendererType.SCATTER) {
                    g2.fill(new Rectangle2D.Double(
                            x + (LEGEND_LINE_LENGTH / 2.0) - (itemStyle.dotWidth() / 2.0),
                            midY - (itemStyle.dotHeight() / 2.0),
                            itemStyle.dotWidth(),
                            itemStyle.dotHeight()));
                } else {
                    g2.setStroke(createSeriesStroke(itemStyle.strokeWidth()));
                    g2.draw(new Line2D.Double(x, midY, x + LEGEND_LINE_LENGTH, midY));
                    if (itemStyle.shapesVisible()) {
                        final AffineTransform transform = AffineTransform.getTranslateInstance(x + (LEGEND_LINE_LENGTH / 2.0), midY);
                        final Shape marker = transform.createTransformedShape(DEFAULT_SHAPES[item.index() % DEFAULT_SHAPES.length]);
                        g2.fill(marker);
                        g2.draw(marker);
                    }
                }

                g2.setColor(style.textColor());
                g2.drawString(item.label(), (float) (x + LEGEND_LINE_LENGTH + LEGEND_SYMBOL_GAP), (float) y);
                x += item.width() + LEGEND_ITEM_GAP;
            }
            y += metrics.getHeight() + LEGEND_ROW_GAP;
        }
    }

    private static LegendLayout layoutLegend(Graphics2D g2, ChartModel model, ChartStyle style, int chartWidth) {
        if (!style.legendVisible() || model.series.isEmpty()) {
            return LegendLayout.empty();
        }
        final FontMetrics metrics = g2.getFontMetrics(style.legendFont());
        final double availableWidth = Math.max(10.0, chartWidth - CHART_LEFT_INSET - CHART_RIGHT_INSET);
        final List<LegendRow> rows = new ArrayList<>();
        LegendRow currentRow = new LegendRow();
        for (int i = 0; i < model.series.size(); i++) {
            final SeriesData seriesData = model.series.get(i);
            final double itemWidth = LEGEND_LINE_LENGTH + LEGEND_SYMBOL_GAP + metrics.stringWidth(seriesData.name());
            final LegendItem item = new LegendItem(i, seriesData.name(), seriesData.style(), itemWidth);
            final double requiredWidth = currentRow.items.isEmpty() ? item.width() : currentRow.width + LEGEND_ITEM_GAP + item.width();
            if (!currentRow.items.isEmpty() && requiredWidth > availableWidth) {
                rows.add(currentRow);
                currentRow = new LegendRow();
            }
            currentRow.add(item);
        }
        if (!currentRow.items.isEmpty()) {
            rows.add(currentRow);
        }
        final double rowHeight = metrics.getHeight();
        final double height = LEGEND_TOP_GAP + LEGEND_BOTTOM_GAP + rows.size() * rowHeight + Math.max(0, rows.size() - 1) * LEGEND_ROW_GAP;
        return new LegendLayout(rows, height);
    }

    private static TickSet createTickSet(Range range, int targetTickCount, double minimumStep) {
        if (range.upper <= range.lower) {
            return TickSet.single(range.lower, formatTick(range.lower, Math.max(1.0, minimumStep)));
        }
        final double roughStep = (range.upper - range.lower) / Math.max(1, targetTickCount - 1);
        final double step = Math.max(minimumStep, niceNumber(roughStep, true));
        final double first = Math.ceil(range.lower / step) * step;
        final double last = Math.floor(range.upper / step) * step;
        final List<Tick> ticks = new ArrayList<>();
        final double epsilon = step / 1000.0;
        for (double value = first; value <= last + epsilon; value += step) {
            final double normalized = normalizeTickValue(value, step);
            ticks.add(new Tick(normalized, formatTick(normalized, step)));
        }
        if (ticks.isEmpty()) {
            ticks.add(new Tick(range.lower, formatTick(range.lower, step)));
            ticks.add(new Tick(range.upper, formatTick(range.upper, step)));
        }
        return new TickSet(ticks, step);
    }

    private static double niceNumber(double value, boolean round) {
        final double exponent = Math.floor(Math.log10(value));
        final double fraction = value / Math.pow(10.0, exponent);
        final double niceFraction;
        if (round) {
            if (fraction < 1.5) {
                niceFraction = 1.0;
            } else if (fraction < 3.0) {
                niceFraction = 2.0;
            } else if (fraction < 7.0) {
                niceFraction = 5.0;
            } else {
                niceFraction = 10.0;
            }
        } else if (fraction <= 1.0) {
            niceFraction = 1.0;
        } else if (fraction <= 2.0) {
            niceFraction = 2.0;
        } else if (fraction <= 5.0) {
            niceFraction = 5.0;
        } else {
            niceFraction = 10.0;
        }
        return niceFraction * Math.pow(10.0, exponent);
    }

    private static String formatTick(double value, double step) {
        final DecimalFormat format = new DecimalFormat(decimalPattern(step), DecimalFormatSymbols.getInstance(Locale.US));
        return format.format(value);
    }

    private static String decimalPattern(double step) {
        int scale = 0;
        BigDecimal decimal = BigDecimal.valueOf(step).stripTrailingZeros().abs();
        if (decimal.scale() > 0) {
            scale = decimal.scale();
        }
        final StringBuilder pattern = new StringBuilder("0");
        if (scale > 0) {
            pattern.append('.');
            for (int i = 0; i < scale; i++) {
                pattern.append('0');
            }
        }
        return pattern.toString();
    }

    private static double normalizeTickValue(double value, double step) {
        final BigDecimal scaled = BigDecimal.valueOf(value)
                .setScale(Math.max(0, BigDecimal.valueOf(step).stripTrailingZeros().scale()), RoundingMode.HALF_UP);
        return scaled.doubleValue();
    }

    private static double valueToX(double value, ChartLayout layout) {
        if (layout.domainRange.upper <= layout.domainRange.lower) {
            return layout.dataArea.getCenterX();
        }
        final double ratio = (value - layout.domainRange.lower) / (layout.domainRange.upper - layout.domainRange.lower);
        return layout.dataArea.getMinX() + ratio * layout.dataArea.getWidth();
    }

    private static double valueToY(double value, ChartLayout layout) {
        if (layout.rangeRange.upper <= layout.rangeRange.lower) {
            return layout.dataArea.getCenterY();
        }
        final double ratio = (value - layout.rangeRange.lower) / (layout.rangeRange.upper - layout.rangeRange.lower);
        return layout.dataArea.getMaxY() - ratio * layout.dataArea.getHeight();
    }

    private static Shape[] createStandardSeriesShapes() {
        final Shape[] shapes = new Shape[10];
        final double size = 6.0;
        final double delta = size / 2.0;

        shapes[0] = new Rectangle2D.Double(-delta, -delta, size, size);
        shapes[1] = new Ellipse2D.Double(-delta, -delta, size, size);
        shapes[2] = new Polygon(intArray(0.0, delta, -delta), intArray(-delta, delta, delta), 3);
        shapes[3] = new Polygon(intArray(0.0, delta, 0.0, -delta), intArray(-delta, 0.0, delta, 0.0), 4);
        shapes[4] = new Rectangle2D.Double(-delta, -delta / 2.0, size, size / 2.0);
        shapes[5] = new Polygon(intArray(-delta, delta, 0.0), intArray(-delta, -delta, delta), 3);
        shapes[6] = new Ellipse2D.Double(-delta, -delta / 2.0, size, size / 2.0);
        shapes[7] = new Polygon(intArray(-delta, delta, -delta), intArray(-delta, 0.0, delta), 3);
        shapes[8] = new Rectangle2D.Double(-delta / 2.0, -delta, size / 2.0, size);
        shapes[9] = new Polygon(intArray(-delta, delta, delta), intArray(0.0, -delta, delta), 3);
        return shapes;
    }

    private static int[] intArray(double... values) {
        final int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (int) values[i];
        }
        return result;
    }

    private static Color defaultSeriesColor(int index) {
        return DEFAULT_SERIES_COLORS[index % DEFAULT_SERIES_COLORS.length];
    }

    private record Range(double lower, double upper) {
    }

    private record Tick(double value, String label) {
    }

    private record TickSet(List<Tick> values, double step) {

        private static TickSet empty() {
            return new TickSet(List.of(), 1.0);
        }

        private static TickSet single(double value, String label) {
            return new TickSet(List.of(new Tick(value, label)), 1.0);
        }
    }

    private record ChartLayout(Rectangle2D.Double dataArea, TickSet xTicks, TickSet yTicks, Range domainRange, Range rangeRange) {
    }

    private record LegendItem(int index, String label, SeriesStyle style, double width) {
    }

    private static final class LegendRow {

        private final List<LegendItem> items = new ArrayList<>();
        private double width;

        private void add(LegendItem item) {
            if (!items.isEmpty()) {
                width += LEGEND_ITEM_GAP;
            }
            items.add(item);
            width += item.width();
        }
    }

    private record LegendLayout(List<LegendRow> rows, double height) {

        private static LegendLayout empty() {
            return new LegendLayout(List.of(), 0.0);
        }
    }

    private record ChartModel(List<SeriesData> series, Range domainRange, Range rangeRange) {

        private static ChartModel from(ChartSpec spec) {
            final List<SeriesData> series = spec.series();
            double minX = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            for (SeriesData seriesData : series) {
                final int size = Math.min(seriesData.xValues().length, seriesData.yValues().length);
                for (int i = 0; i < size; i++) {
                    final double xValue = seriesData.xValues()[i];
                    if (Double.isFinite(xValue)) {
                        minX = Math.min(minX, xValue);
                        maxX = Math.max(maxX, xValue);
                    }

                    final double yValue = seriesData.yValues()[i];
                    if (Double.isFinite(xValue) && Double.isFinite(yValue)) {
                        minY = Math.min(minY, yValue);
                        maxY = Math.max(maxY, yValue);
                    }
                }
            }

            final ChartStyle style = spec.style();
            final Range domain = spec.domainRangeOverride() != null
                    ? createFixedRange(spec.domainRangeOverride())
                    : (Double.isFinite(minX) && Double.isFinite(maxX)
                            ? createAutoRange(minX, maxX, style.includeDomainZero())
                            : createAutoRange(0.0, 1.0, style.includeDomainZero()));
            final Range range = spec.rangeRangeOverride() != null
                    ? createFixedRange(spec.rangeRangeOverride())
                    : (Double.isFinite(minY) && Double.isFinite(maxY)
                            ? createAutoRange(minY, maxY, style.includeRangeZero())
                            : createAutoRange(0.0, 1.0, style.includeRangeZero()));
            return new ChartModel(series, domain, range);
        }
    }

    private static Range createFixedRange(AxisRange axisRange) {
        if (axisRange.upper() <= axisRange.lower()) {
            final double expansion = axisRange.lower() == 0.0 ? 1.0 : Math.abs(axisRange.lower()) * 0.1;
            return new Range(axisRange.lower() - expansion, axisRange.upper() + expansion);
        }
        return new Range(axisRange.lower(), axisRange.upper());
    }

    private static Range createAutoRange(double min, double max, boolean includeZero) {
        double lower = min;
        double upper = max;
        if (includeZero) {
            lower = Math.min(lower, 0.0);
            upper = Math.max(upper, 0.0);
        }
        if (upper <= lower) {
            final double expansion = lower == 0.0 ? 1.0 : Math.abs(lower) * 0.1;
            lower -= expansion;
            upper += expansion;
        }
        final double span = Math.max(upper - lower, 1.0e-8);
        double expandedLower = lower - (span * AUTO_MARGIN_RATIO);
        double expandedUpper = upper + (span * AUTO_MARGIN_RATIO);
        if (includeZero) {
            if (lower >= 0.0 && expandedLower < 0.0) {
                expandedLower = 0.0;
            }
            if (upper <= 0.0 && expandedUpper > 0.0) {
                expandedUpper = 0.0;
            }
        }
        return new Range(expandedLower, expandedUpper);
    }
}
