package mediamatrix.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Lightweight Swing-only status bar that supports fixed-width items.
 */
public class StatusBar extends JPanel {

    @Serial
    private static final long serialVersionUID = 1734826110205106970L;

    private int nextGridX;

    public StatusBar() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }

    public Component add(Component comp, Constraint constraint) {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = nextGridX++;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 4);
        if (constraint != null && constraint.getFixedWidth() > 0) {
            gbc.weightx = 0.0;
            final int fixedWidth = constraint.getFixedWidth();
            final Dimension preferred = comp.getPreferredSize();
            final int preferredHeight = preferred != null ? preferred.height : 24;
            final int minimumHeight = comp.getMinimumSize() != null ? comp.getMinimumSize().height : preferredHeight;
            final int maximumHeight = comp.getMaximumSize() != null ? comp.getMaximumSize().height : Short.MAX_VALUE;
            comp.setPreferredSize(new Dimension(fixedWidth, preferredHeight));
            comp.setMinimumSize(new Dimension(fixedWidth, minimumHeight));
            comp.setMaximumSize(new Dimension(fixedWidth, maximumHeight));
        } else {
            gbc.weightx = 1.0;
        }
        super.add(comp, gbc);
        return comp;
    }

    public static final class Constraint {

        private int fixedWidth = -1;

        public int getFixedWidth() {
            return fixedWidth;
        }

        public void setFixedWidth(int fixedWidth) {
            this.fixedWidth = fixedWidth;
        }
    }
}
