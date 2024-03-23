package mediamatrix.action;

import mediamatrix.db.ChronoArchive;
import mediamatrix.io.ChronoArchiveFileFilter;
import mediamatrix.utils.SmartJVMLauncher;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JTable;
import mediamatrix.gui.ErrorUtils;
import mediamatrix.gui.MediaDatabaseTableModel;

public class PlayAction extends AbstractAction {

    private static final long serialVersionUID = 7644728210126590146L;
    private final MediaDatabaseTableModel model;
    private final JTable table;

    public PlayAction(final JTable table, final MediaDatabaseTableModel model) {
        super("Play");
        this.model = model;
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final int rows[] = table.getSelectedRows();
        if (rows.length > 0) {
            new Thread(() -> {
                try {
                    final File file = model.getAsFile(rows[0]);
                    if (new ChronoArchiveFileFilter().accept(file)) {
                        if (new File("bin/ffplay.exe").exists()) {
                            new SmartJVMLauncher().executeCommand("ffplay", ChronoArchive.getMainContent(file).getAbsolutePath());
                        } else {
                            Desktop.getDesktop().open(ChronoArchive.getMainContent(file));
                        }
                    } else {
                        Desktop.getDesktop().open(file);
                    }
                } catch (IOException | InterruptedException ex) {
                    ErrorUtils.showDialog(ex, table);
                }
            }).start();
        }
    }
}
