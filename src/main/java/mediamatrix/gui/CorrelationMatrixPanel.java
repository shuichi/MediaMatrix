package mediamatrix.gui;

import mediamatrix.db.CorrelationMatrix;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import static java.math.RoundingMode.HALF_UP;

public final class CorrelationMatrixPanel extends JPanel {

    private static final long serialVersionUID = 1403965100235015233L;
    private JTable aTable;
    private JScrollPane pane;
    private CorrelationMatrix matrix;
    private final CorrelationMatrixTableCellRenderer renderer;

    public CorrelationMatrixPanel(final CorrelationMatrix matrix) {
        super(new BorderLayout());
        this.matrix = matrix;
        renderer = new CorrelationMatrixTableCellRenderer(9f);

        aTable = new JTable(new AbstractTableModel() {

            private static final long serialVersionUID = 8656255087856322931L;

            @Override
            public int getColumnCount() {
                return matrix.getWidth();
            }

            @Override
            public int getRowCount() {
                return matrix.getHeight();
            }

            @Override
            public Object getValueAt(final int rowIndex, final int columnIndex) {
                double result = 0d;
                try {
                    result = new BigDecimal(matrix.get(columnIndex, rowIndex)).setScale(3, HALF_UP).doubleValue();
                } catch (NumberFormatException e) {
                    result = 0d;
                }
                return result;
            }

            @Override
            public String getColumnName(int column) {
                return "" + column;
            }
        });
        aTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        aTable.setDefaultRenderer(Object.class, renderer);
        aTable.createDefaultColumnsFromModel();
        final TableColumnModel cmodel = aTable.getColumnModel();
        for (int i = 0; i < cmodel.getColumnCount(); i++) {
            cmodel.getColumn(i).setPreferredWidth(40);
            cmodel.getColumn(i).setWidth(40);
            cmodel.getColumn(i).setMaxWidth(40);
        }
        pane = new JScrollPane(aTable);
        DefaultListModel<Double> model = new DefaultListModel<Double>();
        for (int i = 0; i < matrix.getHeight(); i++) {
            model.addElement(Double.valueOf(i));
        }
        pane.setRowHeaderView(new RowHeaderList(model, aTable));

        NumberAxis xAxis = new NumberAxis("Time");
        NumberAxis yAxis = new NumberAxis("Correlation");
        XYSeries series = new XYSeries("Correlation");
        final Font font = new Font("SanSerif", Font.PLAIN, 14);
        xAxis.setLabelFont(font);
        yAxis.setLabelFont(font);
        xAxis.setTickLabelFont(font);
        yAxis.setTickLabelFont(font);

        if (matrix.getHeight() == 1) {
            for (int i = 0; i < matrix.getWidth(); i++) {
                series.add(i, matrix.get(i, 0));
            }
        } else {
            final int slideNum = matrix.mostRelevantShift();
            final double[] values = matrix.getShift(slideNum);
            for (int i = 0; i < values.length; i++) {
                series.add(i, values[i]);
            }
        }

        final XYSeriesCollection data = new XYSeriesCollection(series);
        xAxis.setAutoRangeIncludesZero(false);
        yAxis.setAutoRangeIncludesZero(false);
        final XYPlot plot = new XYPlot(data, xAxis, yAxis, new XYSplineRenderer());
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(3.0f));
        final JFreeChart chart = new JFreeChart("Correlation Vector", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        chart.setBackgroundPaint(Color.white);
        final ChartPanel chartPanel = new ChartPanel(chart, false);
        final JTabbedPane tab = new JTabbedPane();
        tab.add("Matrix", pane);
        tab.add("Graph", chartPanel);
        setSlashPoint(matrix.mostRelevantShift());
        add(tab, BorderLayout.CENTER);
    }

    private void setSlashPoint(int num) {
        if (num == 0) {
            int size = Math.min(matrix.getHeight(), matrix.getWidth());
            for (int i = 0; i < size; i++) {
                renderer.setColoredCell(i, i);
            }
        } else if (num > 0) {
            int size = Math.min(matrix.getWidth() - num, matrix.getHeight());
            for (int i = 0; i < size; i++) {
                renderer.setColoredCell(i + num, i);
            }
        } else {
            int size = Math.min(matrix.getWidth(), matrix.getHeight() + num);
            for (int i = 0; i < size; i++) {
                renderer.setColoredCell(i, i - num);
            }
        }
    }
}

final class CorrelationMatrixTableCellRenderer extends JLabel implements TableCellRenderer {

    @Serial
    private static final long serialVersionUID = 1L;
    private final ArrayList<Cell> cells;

    public CorrelationMatrixTableCellRenderer(float fontSize) {
        setOpaque(true);
        setFont(getFont().deriveFont(Font.PLAIN, fontSize));
        cells = new ArrayList<>();
    }

    public void setColoredCell(int x, int y) {
        cells.add(new Cell(x, y));
    }

    public void clearColoredCell(int x, int y) {
        cells.clear();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value.toString());
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            boolean colored = false;
            for (Cell cell : cells) {
                if (row == cell.getY() && column == cell.getX()) {
                    colored = true;
                    break;
                }
            }
            if (colored) {
                setBackground(Color.pink);
            } else {
                setBackground(Color.white);
            }
            setForeground(Color.black);
        }
        return this;
    }

    static class Cell {

        private final int x;
        private final int y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
