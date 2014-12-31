package mediamatrix.gui;

import mediamatrix.db.CorrelationScore;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class CorrelationScoreTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private final List<CorrelationScore> scores;

    public CorrelationScoreTableModel(List<CorrelationScore> scores) {
        this.scores = scores;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return scores.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return scores.get(rowIndex).getWord();
            case 1:
                return scores.get(rowIndex).getValue();
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return Double.class;
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Color Scheme";
            case 1:
                return "Score";
            default:
                return null;
        }
    }
}


