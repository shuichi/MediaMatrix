/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediamatrix.action;

import mediamatrix.db.ChronoArchive;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import mediamatrix.gui.ChronoArchiveTable;
import mediamatrix.gui.DialogUtils;
import mediamatrix.gui.ErrorUtils;
import mediamatrix.gui.MediaDatabaseTableModel;

public class FrameAction extends AbstractAction {

    private static final long serialVersionUID = 7644728210126590146L;
    private final MediaDatabaseTableModel model;
    private final JTable table;

    public FrameAction(final JTable table, final MediaDatabaseTableModel model) {
        super("Video Frames");
        this.model = model;
        this.table = table;
    }

    public void actionPerformed(ActionEvent e) {
        int rows[] = table.getSelectedRows();
        if (rows.length > 0) {
            try {
                DialogUtils.showDialog(model.getValueAt(rows[0], 1).toString(), new JScrollPane(new ChronoArchiveTable(new ChronoArchive(model.getAsFile(rows[0])))), table);
            } catch (Exception ex) {
                ErrorUtils.showDialog(ex, table);
            }
        }
    }
}
