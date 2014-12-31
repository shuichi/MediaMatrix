package mediamatrix.music;

import javax.swing.table.AbstractTableModel;

public class TonalMusicTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private TonalMusic[] values;

    public TonalMusicTableModel() {
        super();
        this.values = new TonalMusic[0];
    }

    public TonalMusic getTonalMusic(int row) {
        return values[row];
    }

    public TonalMusicTableModel(TonalMusic[] values) {
        super();
        this.values = values;
    }

    public TonalMusic[] getValues() {
        return values;
    }

    public void setValues(TonalMusic[] values) {
        this.values = values;
        fireTableDataChanged();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return TonalMusic.class;
            default:
                throw new IllegalArgumentException(columnIndex + " is out of bounds");
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Name";
            case 1:
                return "Tonality-Color";
            default:
                throw new IllegalArgumentException(column + " is out of bounds");
        }
    }

    public int getRowCount() {
        return values.length;
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return values[rowIndex].getName();
            case 1:
                return values[rowIndex];
            default:
                throw new IllegalArgumentException(columnIndex + " is out of bounds");
        }
    }
}
