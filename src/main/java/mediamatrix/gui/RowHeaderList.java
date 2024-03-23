package mediamatrix.gui;

import java.awt.Color;
import java.awt.Component;
import java.io.Serial;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.table.JTableHeader;

public final class RowHeaderList extends JList<Double> {

    @Serial
    private static final long serialVersionUID = 3366785700812280035L;

    public RowHeaderList(ListModel<Double> model, JTable table) {
        super(model);
        setFixedCellHeight(table.getRowHeight());
        setCellRenderer(new RowHeaderRenderer(table.getTableHeader()));
    }

    class RowHeaderRenderer extends JLabel implements ListCellRenderer<Double> {

        @Serial
        private static final long serialVersionUID = 2493937942177532394L;
        
        private final JTableHeader header;

        public RowHeaderRenderer(JTableHeader header) {
            this.header = header;
            this.setOpaque(true);
            this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 2, Color.GRAY.brighter()));
            this.setHorizontalAlignment(CENTER);
            this.setForeground(header.getForeground());
            this.setBackground(header.getBackground());
            this.setFont(header.getFont());
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Double> list, Double value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(Color.GRAY.brighter());
            } else {
                this.setForeground(header.getForeground());
                this.setBackground(header.getBackground());
            }
            setText(value.toString());
            return this;
        }
    }
}
