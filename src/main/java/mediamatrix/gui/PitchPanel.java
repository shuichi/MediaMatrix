package mediamatrix.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import mediamatrix.db.MediaMatrix;

public class PitchPanel extends JPanel {

    private static final long serialVersionUID = 6903430797371699378L;

    public PitchPanel(final MediaMatrix mat) {
        super(new BorderLayout());
        final Java2DChartImagePanel chartPanel = new Java2DChartImagePanel(createChartSpec(mat));
        final JTabbedPane tab = new JTabbedPane();
        tab.add("Graph", chartPanel);
        tab.add("Matrix", new MediaMatrixPanel(mat));
        add(tab, BorderLayout.CENTER);
        SwingUtilities.invokeLater(chartPanel::requestRender);
    }

    private static Java2DChartRenderer.ChartSpec createChartSpec(MediaMatrix mat) {
        final double[] xValues = new double[mat.getHeight()];
        final double[] yValues = new double[mat.getHeight()];
        for (int i = 0; i < mat.getHeight(); i++) {
            xValues[i] = mat.getRow(i);
            yValues[i] = Double.NaN;
            for (int j = 0; j < mat.getWidth(); j++) {
                if (mat.get(i, j) > 0) {
                    yValues[i] = j;
                }
            }
        }

        final Java2DChartRenderer.SeriesData series = new Java2DChartRenderer.SeriesData(
                "Note",
                xValues,
                yValues,
                Java2DChartRenderer.SeriesStyle.scatter(Color.black, 2, 2));
        return new Java2DChartRenderer.ChartSpec(
                java.util.List.of(series),
                Java2DChartRenderer.ChartStyle.xyPanelStyle("Pitch", "Pitch"),
                null,
                new Java2DChartRenderer.AxisRange(0.0, 127.0));
    }
}
