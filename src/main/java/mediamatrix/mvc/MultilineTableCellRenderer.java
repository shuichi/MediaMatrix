package mediamatrix.mvc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

public final class MultilineTableCellRenderer extends JTextArea implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    public MultilineTableCellRenderer(float fontSize) {
        setLineWrap(true);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder());
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
