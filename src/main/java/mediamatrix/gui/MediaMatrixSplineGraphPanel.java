package mediamatrix.gui;

import mediamatrix.db.MediaMatrix;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.Serial;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

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
    private final MediaMatrixJava2DChartPanel chartPanel;
    private final MediaMatrixPanel matrixPanel;
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
        chartPanel = new MediaMatrixJava2DChartPanel(mat, createChartStyle());
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
        chartScrollPane.setViewportView(chartPanel);
        chartScrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!scrollCheckBox.isSelected()) {
                    redraw();
                }
            }
        });
        redraw();
    }

    private void redraw() {
        chartPanel.setChartStyle(createChartStyle());
        chartPanel.setChartSize(resolveChartSize());
        if (scrollCheckBox.isSelected()) {
            chartScrollPane.getHorizontalScrollBar().setUnitIncrement(24);
        }
        SwingUtilities.invokeLater(chartPanel::requestRender);
        repaint();
    }

    private Java2DChartRenderer.ChartStyle createChartStyle() {
        return Java2DChartRenderer.ChartStyle.splinePanelStyle(bgColor, Color.lightGray);
    }

    private Dimension resolveChartSize() {
        if (scrollCheckBox.isSelected()) {
            return new Dimension(Math.max(1, mat.getRows().length * 5), 400);
        }
        final Dimension extent = chartScrollPane.getViewport().getExtentSize();
        final int width = Math.max(1, extent.width > 0 ? extent.width : chartScrollPane.getWidth());
        return new Dimension(width, Math.max(1, mat.getWidth() * 5));
    }
}
