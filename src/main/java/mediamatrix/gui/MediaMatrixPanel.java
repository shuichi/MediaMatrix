package mediamatrix.gui;

import mediamatrix.mvc.MediaMatrixTableCellRenderer;
import mediamatrix.db.MediaMatrix;
import mediamatrix.mvc.MediaMatrixTableModelAdapter;
import java.awt.BorderLayout;
import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public final class MediaMatrixPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 1403965100235015233L;

    public MediaMatrixPanel(MediaMatrix matrix) {
        super(new BorderLayout());
        
        final JTable aTable = new JTable(new MediaMatrixTableModelAdapter(matrix));
        aTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        aTable.setDefaultRenderer(Object.class, new MediaMatrixTableCellRenderer(12f));
        aTable.createDefaultColumnsFromModel();
        final DefaultListModel<Double> model = new DefaultListModel<>();
        final double[] rows = matrix.getRows();
        for (double d : rows) {
            model.addElement(new BigDecimal(d).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }
        final JScrollPane pane = new JScrollPane(aTable);
        pane.setRowHeaderView(new RowHeaderList(model, aTable));
        add(pane, BorderLayout.CENTER);
    }
}
