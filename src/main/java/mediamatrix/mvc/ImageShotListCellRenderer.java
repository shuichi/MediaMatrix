package mediamatrix.mvc;

import java.awt.Component;
import java.awt.Font;
import java.io.Serial;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import mediamatrix.gui.ImageShot;

public final class ImageShotListCellRenderer extends JLabel implements ListCellRenderer<ImageShot> {

    @Serial
    private static final long serialVersionUID = 1L;

    public ImageShotListCellRenderer() {
        setOpaque(true);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
        setFont(new Font("Monospaced", Font.PLAIN, 10));
        setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    public Component getListCellRendererComponent(final JList<? extends ImageShot> list, final ImageShot value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        setText(Double.toString(value.getTime()));
        setIcon(new ImageIcon(value.getThumbnail()));
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
}
