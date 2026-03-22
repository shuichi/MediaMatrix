package mediamatrix.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import mediamatrix.db.MediaMatrix;

public class TempoPanel extends JPanel {

    private static final long serialVersionUID = 8814721574976916780L;

    public TempoPanel(final MediaMatrix mat) {
        super(new BorderLayout());
        final Java2DChartImagePanel chartPanel = new Java2DChartImagePanel(createChartSpec(mat));
        final JTabbedPane tab = new JTabbedPane();
        tab.add("Graph", chartPanel);
        tab.add("Matrix", new MediaMatrixPanel(mat));
        add(tab, BorderLayout.CENTER);
        SwingUtilities.invokeLater(chartPanel::requestRender);
    }

    private static Java2DChartRenderer.ChartSpec createChartSpec(MediaMatrix mat) {
        final ArrayList<Double> xValues = new ArrayList<>();
        final ArrayList<Double> yValues = new ArrayList<>();
        double previous = 0d;
        for (int i = 0; i < mat.getHeight(); i++) {
            final double time = mat.getRow(i);
            final double value = mat.get(time, "tempo");
            if (previous != value) {
                xValues.add(time);
                yValues.add(value);
                previous = value;
            }
        }

        final Java2DChartRenderer.SeriesData series = new Java2DChartRenderer.SeriesData(
                "Tempo",
                toArray(xValues),
                toArray(yValues),
                Java2DChartRenderer.SeriesStyle.step(Color.black, 3.0f));
        return new Java2DChartRenderer.ChartSpec(
                java.util.List.of(series),
                Java2DChartRenderer.ChartStyle.xyPanelStyle("Tempo", "Value"));
    }

    private static double[] toArray(ArrayList<Double> values) {
        final double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }
}
