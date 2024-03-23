package mediamatrix.mvc;

import mediamatrix.gui.ColoredMusicPanel;
import mediamatrix.music.TonalMusic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public final class TonalMusicTableCellRenderer extends ColoredMusicPanel implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    public TonalMusicTableCellRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value != null && value instanceof String) {
            JLabel label = new JLabel((String) value);
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
            return label;
        }

        if (value != null && value instanceof TonalMusic) {
            Rectangle rect = table.getCellRect(row, column, false);
            setSize(rect.width, rect.height);
            setTonalMusic((TonalMusic) value);
        }
        return this;
    }
}
