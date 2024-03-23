package mediamatrix.gui;

import mediamatrix.mvc.MediaMatrixXYDataSetAdapter;
import mediamatrix.db.MediaMatrix;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.Serial;
import java.util.concurrent.ExecutionException;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleInsets;

public final class MediaMatrixSplineGraphPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = -9129290602166654043L;

    private final JTabbedPane aTabbedPane;
    private final JToolBar aToolBar;
    private final JScrollPane chartScrollPane;
    private final JCheckBox scrollCheckBox;
    private JCheckBox bgCheckBox;
    private final JLabel segmentLabel;
    private final JToolBar.Separator separator1;
    private final ChartPanel chartPanel = new ChartPanel(null);
    private final MediaMatrixPanel matrixPanel;
    private final XYSplineRenderer renderer = new XYSplineRenderer();
    private final NumberAxis xAxis = new NumberAxis("Time");
    private final NumberAxis yAxis = new NumberAxis("Score");
    private final MediaMatrix mat;
    private Color bgColor = Color.lightGray;

    public MediaMatrixSplineGraphPanel(MediaMatrix mat) {
        super();
        this.mat = mat;
        aToolBar = new JToolBar();
        segmentLabel = new JLabel();
        separator1 = new JToolBar.Separator();
        scrollCheckBox = new JCheckBox();
        bgCheckBox = new JCheckBox();
        bgCheckBox.setSelected(true);
        aTabbedPane = new JTabbedPane();
        chartScrollPane = new JScrollPane();
        setLayout(new BorderLayout());
        aToolBar.setRollover(true);
        segmentLabel.setText("Segment:" + mat.getRows().length);
        aToolBar.add(segmentLabel);
        aToolBar.add(separator1);
        bgCheckBox.setText("Background Color");
        bgCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        bgCheckBox.addActionListener((java.awt.event.ActionEvent evt) -> {
            if (bgCheckBox.isSelected()) {
                bgColor = Color.lightGray;
            } else {
                bgColor = Color.white;
            }
            redraw();
        });

        scrollCheckBox.setText("Scroll");
        scrollCheckBox.setFocusable(false);
        scrollCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        scrollCheckBox.addActionListener((java.awt.event.ActionEvent evt) -> {
            redraw();
        });
        aToolBar.add(scrollCheckBox);
        aToolBar.add(bgCheckBox);
        add(aToolBar, java.awt.BorderLayout.NORTH);
        aTabbedPane.addTab("Chart", chartScrollPane);
        matrixPanel = new MediaMatrixPanel(mat);
        aTabbedPane.addTab("Matrix", matrixPanel);
        add(aTabbedPane, java.awt.BorderLayout.CENTER);
        chartPanel.setMaximumDrawHeight(2000);
        xAxis.setAutoRangeIncludesZero(false);
        yAxis.setAutoRangeIncludesZero(false);
        final Font font = new Font("SanSerif", Font.PLAIN, 14);
        xAxis.setLabelFont(font);
        yAxis.setLabelFont(font);
        xAxis.setTickLabelFont(font);
        yAxis.setTickLabelFont(font);
        redraw();
    }

    private void redraw() {
        new SwingWorker<JFreeChart, Object>() {

            @Override
            protected JFreeChart doInBackground() throws Exception {
                final XYPlot plot = new XYPlot(new MediaMatrixXYDataSetAdapter(mat), xAxis, yAxis, renderer);
                plot.setBackgroundPaint(bgColor);
                plot.setDomainGridlinePaint(Color.lightGray);
                plot.setRangeGridlinePaint(Color.lightGray);
                plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
                final JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
                chart.setBackgroundPaint(Color.white);
                final LegendTitle legendTitle = chart.getLegend();
                legendTitle.setItemFont(new Font("SanSerif", Font.PLAIN, 14));
                return chart;
            }

            @Override
            protected void done() {
                try {
                    chartPanel.setChart(get());
                    if (scrollCheckBox.isSelected()) {
                        final Dimension size = new Dimension(mat.getRows().length * 5, 400);
                        chartPanel.setMaximumDrawWidth(100000);
                        chartPanel.setMaximumDrawHeight(100000);
                        chartPanel.setSize(size);
                        chartPanel.setPreferredSize(size);
                    } else {
                        final Dimension size = new Dimension(chartScrollPane.getViewport().getSize());
                        size.height = mat.getWidth() * 5;
                        chartPanel.setSize(size);
                        chartPanel.setPreferredSize(size);
                    }
                    chartPanel.setDomainZoomable(true);
                    chartPanel.setRangeZoomable(true);
                    chartScrollPane.setViewportView(chartPanel);
                    repaint();
                } catch (InterruptedException | ExecutionException ex) {
                    ErrorUtils.showDialog(ex, MediaMatrixSplineGraphPanel.this);
                }
            }
        }.execute();
    }
}


