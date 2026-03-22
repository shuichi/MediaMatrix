package mediamatrix.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

public final class Java2DChartImagePanel extends JLabel {

    @Serial
    private static final long serialVersionUID = 1L;

    private Java2DChartRenderer.ChartSpec chartSpec;
    private transient Java2DChartRenderer renderer;
    private transient SwingWorker<RenderResult, Object> renderWorker;
    private int renderRequestId;

    public Java2DChartImagePanel(Java2DChartRenderer.ChartSpec chartSpec) {
        this.chartSpec = Objects.requireNonNull(chartSpec, "chartSpec");
        this.renderer = new Java2DChartRenderer();
        setOpaque(true);
        setBackground(chartSpec.style().chartBackgroundColor());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                requestRender();
            }
        });
    }

    public void setChartSpec(Java2DChartRenderer.ChartSpec chartSpec) {
        final Java2DChartRenderer.ChartSpec newSpec = Objects.requireNonNull(chartSpec, "chartSpec");
        if (newSpec.equals(this.chartSpec)) {
            return;
        }
        this.chartSpec = newSpec;
        setBackground(newSpec.style().chartBackgroundColor());
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
        final Java2DChartRenderer.ChartSpec spec = chartSpec;
        renderWorker = new SwingWorker<>() {
            @Override
            protected RenderResult doInBackground() {
                final BufferedImage image = renderer().createChartImage(spec, logicalWidth, logicalHeight, scaleFactor.scaleX(), scaleFactor.scaleY());
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
                        ErrorUtils.showDialog(exception, Java2DChartImagePanel.this);
                    } else {
                        ErrorUtils.showDialog(new RuntimeException(cause), Java2DChartImagePanel.this);
                    }
                }
            }
        };
        renderWorker.execute();
    }

    private int resolveLogicalWidth() {
        return Math.max(1, getWidth() > 0 ? getWidth() : getPreferredSize().width);
    }

    private int resolveLogicalHeight() {
        return Math.max(1, getHeight() > 0 ? getHeight() : getPreferredSize().height);
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
