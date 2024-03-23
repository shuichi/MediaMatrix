package mediamatrix.mvc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public final class MediaMatrixTableCellRenderer extends JLabel implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    public MediaMatrixTableCellRenderer(float fontSize) {
        setOpaque(true);
        setFont(getFont().deriveFont(Font.PLAIN, fontSize));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value.toString());
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
        }
        return this;
    }
}