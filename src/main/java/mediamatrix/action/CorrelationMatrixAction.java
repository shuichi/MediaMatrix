package mediamatrix.action;

import mediamatrix.db.CorrelationMatrix;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import mediamatrix.gui.CorrelationMatrixPanel;
import mediamatrix.gui.DialogUtils;
import mediamatrix.gui.MediaDatabaseTableModel;

public class CorrelationMatrixAction extends AbstractAction {

    private static final long serialVersionUID = 7644728210126590146L;
    private final MediaDatabaseTableModel model;
    private final JTable table;

    public CorrelationMatrixAction(final JTable table, final MediaDatabaseTableModel model) {
        super("CorrelationMatrix");
        this.model = model;
        this.table = table;
    }

    public void actionPerformed(ActionEvent e) {
        int rows[] = table.getSelectedRows();
        if (rows.length > 0) {
            final Map<String, CorrelationMatrix> matrices = model.getResult().getCorrelationMatrices(rows[0]);
            final Set<String> keys = matrices.keySet();
            final JTabbedPane tab = new JTabbedPane();
            for (Iterator<String> it = keys.iterator(); it.hasNext();) {
                final String key = it.next();
                tab.add(key, new CorrelationMatrixPanel(matrices.get(key)));
            }
            DialogUtils.showDialog(model.getValueAt(rows[0], 1).toString(), tab, table);
        }
    }
}
