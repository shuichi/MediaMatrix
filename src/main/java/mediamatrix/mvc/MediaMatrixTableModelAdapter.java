package mediamatrix.mvc;

import mediamatrix.db.MediaMatrix;
import java.math.BigDecimal;
import javax.swing.table.AbstractTableModel;

public class MediaMatrixTableModelAdapter extends AbstractTableModel {

    private static final long serialVersionUID = -766449466523923420L;
    private MediaMatrix matrix;

    public MediaMatrixTableModelAdapter(MediaMatrix matrix) {
        this.matrix = matrix;
    }

    @Override
    public int getColumnCount() {
        return matrix.getWidth();
    }

    @Override
    public int getRowCount() {
        return matrix.getHeight();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            BigDecimal bd = new BigDecimal(matrix.get(rowIndex, columnIndex));
            return bd.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

    @Override
    public String getColumnName(int column) {
        String val = matrix.getColumn(column).toString();
        return val;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }
}
