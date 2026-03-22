package mediamatrix.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import mediamatrix.db.MediaMatrix;

public final class MediaMatrixJava2DChartPanel extends JLabel {

    @Serial
    private static final long serialVersionUID = 1L;

    private final MediaMatrix matrix;
    private transient Java2DChartRenderer renderer;
    private Java2DChartRenderer.ChartStyle chartStyle;
    private transient SwingWorker<RenderResult, Object> renderWorker;
    private int renderRequestId;

    public MediaMatrixJava2DChartPanel(MediaMatrix matrix, Java2DChartRenderer.ChartStyle chartStyle) {
        this.matrix = Objects.requireNonNull(matrix, "matrix");
        this.renderer = new Java2DChartRenderer();
        this.chartStyle = Objects.requireNonNull(chartStyle, "chartStyle");
        setOpaque(true);
        setBackground(chartStyle.chartBackgroundColor());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                requestRender();
            }
        });
    }

    public void setChartStyle(Java2DChartRenderer.ChartStyle chartStyle) {
        final Java2DChartRenderer.ChartStyle newStyle = Objects.requireNonNull(chartStyle, "chartStyle");
        if (newStyle.equals(this.chartStyle)) {
            return;
        }
        this.chartStyle = newStyle;
        setBackground(newStyle.chartBackgroundColor());
        requestRender();
    }

    public void setChartSize(Dimension size) {
        final Dimension normalized = normalizeSize(size);
        if (normalized.equals(getPreferredSize())) {
            requestRender();
            return;
        }
        setPreferredSize(normalized);
        setSize(normalized);
        revalidate();
        requestRender();
    }

    public void requestRender() {
        final int logicalWidth = resolveLogicalWidth();
        final int logicalHeight = resolveLogicalHeight();
        if (logicalWidth <= 1 || logicalHeight <= 1) {
            return;
        }

        final int requestId = ++renderRequestId;
        if (renderWorker != null && !renderWorker.isDone()) {
            renderWorker.cancel(true);
        }
        final HiDpiSupport.ScaleFactor scaleFactor = HiDpiSupport.scaleFactor(this);
        final Java2DChartRenderer.ChartStyle style = chartStyle;
        renderWorker = new SwingWorker<>() {
            @Override
            protected RenderResult doInBackground() {
                final BufferedImage image = renderer().createChartImage(matrix, style, logicalWidth, logicalHeight, scaleFactor.scaleX(), scaleFactor.scaleY());
                return new RenderResult(requestId, image, logicalWidth, logicalHeight);
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    final RenderResult result = get();
                    if (result.requestId() != renderRequestId) {
                        return;
                    }
                    setIcon(new HiDpiImageIcon(result.image(), result.logicalWidth(), result.logicalHeight()));
                    repaint();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (CancellationException ignored) {
                } catch (ExecutionException ex) {
                    final Throwable cause = ex.getCause();
                    if (cause instanceof Exception exception) {
                        ErrorUtils.showDialog(exception, MediaMatrixJava2DChartPanel.this);
                    } else {
                        ErrorUtils.showDialog(new RuntimeException(cause), MediaMatrixJava2DChartPanel.this);
                    }
                }
            }
        };
        renderWorker.execute();
    }

    public void setPlotBackground(Color plotBackgroundColor) {
        setChartStyle(chartStyle.withPlotBackgroundColor(plotBackgroundColor));
    }

    private int resolveLogicalWidth() {
        return Math.max(1, getWidth() > 0 ? getWidth() : getPreferredSize().width);
    }

    private int resolveLogicalHeight() {
        return Math.max(1, getHeight() > 0 ? getHeight() : getPreferredSize().height);
    }

    private static Dimension normalizeSize(Dimension size) {
        final Dimension dimension = size == null ? new Dimension(1, 1) : new Dimension(size);
        dimension.width = Math.max(1, dimension.width);
        dimension.height = Math.max(1, dimension.height);
        return dimension;
    }

    private Java2DChartRenderer renderer() {
        if (renderer == null) {
            renderer = new Java2DChartRenderer();
        }
        return renderer;
    }

    private record RenderResult(int requestId, BufferedImage image, int logicalWidth, int logicalHeight) {
    }
}
