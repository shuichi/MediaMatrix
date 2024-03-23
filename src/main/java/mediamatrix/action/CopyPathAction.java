package mediamatrix.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTable;
import mediamatrix.gui.MediaDatabaseTableModel;

public class CopyPathAction extends AbstractAction {

    private static final long serialVersionUID = 7644728210126590146L;
    private final MediaDatabaseTableModel model;
    private final JTable table;

    public CopyPathAction(final JTable table, final MediaDatabaseTableModel model) {
        super("Copy the path for this item");
        this.model = model;
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final int rows[] = table.getSelectedRows();
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final StringBuffer buff = new StringBuffer();
        for (int i = 0; i < rows.length; i++) {
            buff.append(model.getValueAt(i, 1));
            if (i + 1 < rows.length) {
                buff.append("\n");
            }
        }
        final StringSelection selection = new StringSelection(buff.toString());
        clipboard.setContents(selection, selection);
    }
}
