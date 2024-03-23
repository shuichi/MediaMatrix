package mediamatrix.action;

import mediamatrix.db.ChronoArchive;
import mediamatrix.db.MediaMatrix;
import mediamatrix.munsell.ColorImpressionKnowledge;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import mediamatrix.gui.DialogUtils;
import mediamatrix.gui.ErrorUtils;
import mediamatrix.gui.MediaDatabaseTableModel;
import mediamatrix.gui.RowHeaderList;
import mediamatrix.mvc.MediaMatrixTableModelAdapter;
import mediamatrix.mvc.MediaMatrixTableCellRenderer;

public class MediaMatrixAction extends AbstractAction {

    private static final long serialVersionUID = 7644728210126590146L;
    private final MediaDatabaseTableModel model;
    private final JTable table;

    public MediaMatrixAction(final JTable table, final MediaDatabaseTableModel model) {
        super("MediaMatrix");
        this.model = model;
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int rows[] = table.getSelectedRows();
        if (rows.length > 0) {
            try {
                final File file = model.getAsFile(rows[0]);
                final ChronoArchive carc = new ChronoArchive(file);
                final MediaMatrix matrix = carc.getMatrix();
                final JPanel panel = new JPanel(new BorderLayout());
                final JTable aTable = new JTable(new MediaMatrixTableModelAdapter(matrix));
                aTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                aTable.setDefaultRenderer(Object.class, new MediaMatrixTableCellRenderer(12f));
                aTable.createDefaultColumnsFromModel();
                final DefaultListModel<Double> lmodel = new DefaultListModel<>();
                final double[] mrows = matrix.getRows();
                for (double d : mrows) {
                    lmodel.addElement(new BigDecimal(d).setScale(1, RoundingMode.HALF_UP).doubleValue());
                }
                final JScrollPane pane = new JScrollPane(aTable);
                pane.setRowHeaderView(new RowHeaderList(lmodel, aTable));
                panel.add(pane, BorderLayout.CENTER);

                final ColorImpressionKnowledge ci = carc.getColorImpressionKnowledge();
                for (int i = 0; i < aTable.getColumnModel().getColumnCount(); i++) {
                    TableColumn col = aTable.getColumnModel().getColumn(i);
                    col.setMinWidth(100);
                    col.setHeaderRenderer(new MediaMatrixTableHeaderRenderer(matrix.getColumn(i), ci.getHistogramImage(matrix.getColumn(i))));
                }
                DialogUtils.showDialog(model.getValueAt(rows[0], 1).toString(), panel, table);
            } catch (IOException ex) {
                ErrorUtils.showDialog(ex, table);
            }
        }
    }

    private class MediaMatrixTableHeaderRenderer extends JLabel implements TableCellRenderer {

        private static final long serialVersionUID = 1L;

        public MediaMatrixTableHeaderRenderer(String name, BufferedImage im) {
            setText(name);
            setOpaque(true);
            setToolTipText(name);
            setIcon(new ImageIcon(im));
            setHorizontalTextPosition(JLabel.CENTER);
            setVerticalTextPosition(JLabel.TOP);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
            return this;
        }
    }
}
