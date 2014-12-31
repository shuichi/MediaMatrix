package mediamatrix.action;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

public class PopupInvoker extends MouseAdapter {

    private final JTable table;
    private final JPopupMenu popup;

    public PopupInvoker(final JPopupMenu popup, final JTable table) {
        this.popup = popup;
        this.table = table;
    }

    private void showPopup(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row > -1) {
            table.getSelectionModel().setSelectionInterval(row, row);
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
            showPopup(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (table.getSelectedRow() > -1) {
            if (e.getClickCount() == 2) {
                showPopup(e);
            }
        }
    }
}
