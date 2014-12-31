package mediamatrix.mvc;

import mediamatrix.munsell.ColorImpressionKnowledge;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ImpressionWordTableCellRenderer extends JLabel implements TableCellRenderer {

    private static final long serialVersionUID = 1L;
    private ColorImpressionKnowledge ci;
    private static final Map<String, ImageIcon> CACHE = new TreeMap<String, ImageIcon>();

    public ImpressionWordTableCellRenderer(float fontSize, ColorImpressionKnowledge ci) {
        setOpaque(true);
        setFont(getFont().deriveFont(Font.PLAIN, fontSize));
        this.ci = ci;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(ci.toPrintName(value.toString()));
        ImageIcon icon = null;
        if (CACHE.containsKey(value.toString())) {
            icon = CACHE.get(value.toString());
        } else {
            icon = new ImageIcon(ci.getHistogramImage(value.toString()));
            CACHE.put(value.toString(), icon);
        }
        setIcon(icon);
        setVerticalAlignment(TOP);
        setHorizontalAlignment(CENTER);
        setVerticalTextPosition(BOTTOM);
        setHorizontalTextPosition(CENTER);
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
        return this;
    }
}
