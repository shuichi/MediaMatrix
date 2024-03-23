package mediamatrix.mvc;

import java.awt.Color;
import java.awt.Component;
import java.io.Serial;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public final class ColorListCellRenderer extends JLabel implements ListCellRenderer<Color> {

    @Serial
    private static final long serialVersionUID = 1L;

    public ColorListCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Color> list, Color value, int index, boolean isSelected, boolean cellHasFocus) {
        setBackground(value);
        setText(value.toString());
        return this;
    }
}
