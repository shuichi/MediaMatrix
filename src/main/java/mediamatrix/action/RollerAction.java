package mediamatrix.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTable;
import mediamatrix.gui.DialogUtils;
import mediamatrix.gui.ErrorUtils;
import mediamatrix.gui.MediaDatabaseTableModel;
import mediamatrix.gui.RollerPanel;

public class RollerAction extends AbstractAction {

    private static final long serialVersionUID = 7644728210126590146L;
    private final MediaDatabaseTableModel model;
    private final JTable table;

    public RollerAction(final JTable table, final MediaDatabaseTableModel model) {
        super("Roller");
        this.model = model;
        this.table = table;
    }

    public void actionPerformed(ActionEvent e) {
        int rows[] = table.getSelectedRows();
        if (rows.length > 0) {
            try {
                DialogUtils.showDialog(model.getValueAt(rows[0], 1).toString(), new RollerPanel(model.getAsFile(rows[0])), table);
            } catch (Exception ex) {
                ErrorUtils.showDialog(ex, table);
            }
        }
    }
}
