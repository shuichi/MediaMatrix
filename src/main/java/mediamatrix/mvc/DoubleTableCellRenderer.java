package mediamatrix.mvc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public final class DoubleTableCellRenderer extends JLabel implements TableCellRenderer {

    @Serial
    private static final long serialVersionUID = 1L;
    private final int scale;

    public DoubleTableCellRenderer(float fontSize, int scale) {
        setOpaque(true);
        this.scale = scale;
        setFont(getFont().deriveFont(Font.PLAIN, fontSize));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Double) {
            Double d = (Double) value;
            if (d.isInfinite() || d.isNaN()) {
                System.out.println(getClass().getName() + ": " + d + "@ row: " + row + ", col: " + column);
            } else {
                BigDecimal bd = BigDecimal.valueOf((Double) value);
                setText(bd.setScale(scale, RoundingMode.HALF_UP).toString());
            }
        } else {
            setText(value.toString());
        }
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
