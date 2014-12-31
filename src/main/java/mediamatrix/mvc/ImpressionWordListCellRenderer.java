package mediamatrix.mvc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import mediamatrix.db.CorrelationScore;
import mediamatrix.munsell.ColorImpressionKnowledge;

public class ImpressionWordListCellRenderer extends JLabel implements ListCellRenderer<CorrelationScore> {

    private static final long serialVersionUID = 1L;
    private final ColorImpressionKnowledge ci;

    public ImpressionWordListCellRenderer(ColorImpressionKnowledge ci, float fontSize) {
        setOpaque(true);
        this.ci = ci;
        setFont(getFont().deriveFont(Font.PLAIN, fontSize));
        setBackground(Color.white);
        setForeground(Color.black);
        setVerticalAlignment(TOP);
        setHorizontalAlignment(CENTER);
        setVerticalTextPosition(BOTTOM);
        setHorizontalTextPosition(CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends CorrelationScore> list, CorrelationScore value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.toString());
        setIcon(new ImageIcon(ci.getHistogramImage(value.getWord())));
        return this;
    }
}
