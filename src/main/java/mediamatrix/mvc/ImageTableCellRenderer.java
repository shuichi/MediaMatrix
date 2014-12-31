package mediamatrix.mvc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;

public class ImageTableCellRenderer extends JLabel implements TableCellRenderer, ListCellRenderer<Image> {

    private static final long serialVersionUID = 1L;

    public ImageTableCellRenderer() {
        setOpaque(true);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(new Color(10, 10, 10));
        setForeground(Color.white);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        ImageIcon icon = new ImageIcon((Image) value);
        setIcon(icon);
        return this;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Image> list, Image value, int index, boolean isSelected, boolean cellHasFocus) {
        ImageIcon icon = new ImageIcon(value);
        setIcon(icon);
        return this;
    }
}
