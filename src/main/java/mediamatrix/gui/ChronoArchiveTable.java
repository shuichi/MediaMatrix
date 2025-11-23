package mediamatrix.gui;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import mediamatrix.db.ChronoArchive;
import mediamatrix.db.CorrelationScore;
import mediamatrix.db.MediaMatrix;
import mediamatrix.mvc.DoubleTableCellRenderer;
import mediamatrix.mvc.ImageTableCellRenderer;
import mediamatrix.mvc.MultilineTableCellRenderer;
import mediamatrix.utils.ImageUtilities;

public final class ChronoArchiveTable extends JTable {

    private static final long serialVersionUID = -2615961218813612330L;

    public ChronoArchiveTable(final ChronoArchive carc) {
        addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    try {
                        int row = getSelectedRow();
                        final BufferedImage image = carc.getImage(row);
                        final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(ChronoArchiveTable.this));
                        dialog.setTitle(carc.getFileName() + " [" + row + "]");
                        dialog.getContentPane().setLayout(new BorderLayout());
                        dialog.getContentPane().add(new MunsellImagePanel(image, carc.getColorImpressionKnowledge()), BorderLayout.CENTER);
                        dialog.pack();
                        dialog.setLocationRelativeTo(null);
                        dialog.setVisible(true);
                    } catch (IOException ex) {
                        ErrorUtils.showDialog(ex, ChronoArchiveTable.this);
                    }
                }
            }
        });

        setModel(new CARCTableModel(carc));
        setDefaultRenderer(Image.class, new ImageTableCellRenderer());
        setDefaultRenderer(Double.class, new DoubleTableCellRenderer(14f, 0));
        setDefaultRenderer(String.class, new MultilineTableCellRenderer(14f));
        createDefaultColumnsFromModel();
        setRowHeight(100);
        final TableColumnModel cmodel = getColumnModel();
        cmodel.getColumn(0).setPreferredWidth(100);
        cmodel.getColumn(0).setMaxWidth(100);
        cmodel.getColumn(0).setWidth(100);
        cmodel.getColumn(1).setPreferredWidth(50);
        cmodel.getColumn(1).setWidth(50);
        cmodel.getColumn(1).setMaxWidth(50);
    }
}

final class CARCTableModel extends AbstractTableModel {

    @Serial
    private static final long serialVersionUID = 1L;
    private transient final ChronoArchive carc;
    private transient final HashMap<Integer, BufferedImage> imgCache;
    private transient final HashMap<Integer, String> strCache;

    public CARCTableModel(final ChronoArchive carc) {
        this.carc = carc;
        imgCache = new HashMap<Integer, BufferedImage>();
        strCache = new HashMap<Integer, String>();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Image.class;
            case 1:
                return Double.class;
            default:
                return String.class;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Image";
            case 1:
                return "Sec.";
            default:
                return "Metadata";
        }
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getRowCount() {
        return carc.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        switch (columnIndex) {
            case 0:
                value = imgCache.get(rowIndex);
                if (value == null) {
                    try {
                        value = ImageUtilities.createThumbnail(carc.getImage(rowIndex), 100, 100);
                        imgCache.put(rowIndex, (BufferedImage) value);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            case 1:
                value = carc.getTimeUnit() * rowIndex;
                break;
            case 2:
                value = strCache.get(rowIndex);
                if (value == null) {
                    value = topKMetadataString(carc, rowIndex, 10);
                    strCache.put(rowIndex, (String) value);
                }
                break;
            default:
                value = null;
        }
        return value;
    }

    public static String topKMetadataString(ChronoArchive carc, int index, int k) {
        final TreeSet<CorrelationScore> scores = new TreeSet<CorrelationScore>(new Comparator<CorrelationScore>() {

            @Override
            public int compare(CorrelationScore o1, CorrelationScore o2) {
                int result = 0;
                if (o1.getValue() == o2.getValue()) {
                    result = o1.getWord().compareTo(o2.getWord());
                } else if (o1.getValue() > o2.getValue()) {
                    return -1;
                } else {
                    return 1;
                }
                return result;
            }
        });
        final MediaMatrix mat = carc.getMatrix();
        final Double d = mat.getRow(index);
        for (String key : mat.getColumns()) {
            scores.add(new CorrelationScore(key, mat.get(d, key)));
        }
        int i = 0;
        final StringBuffer buff = new StringBuffer();
        for (Iterator<CorrelationScore> it = scores.iterator(); it.hasNext(); ) {
            if (++i >= k) {
                break;
            }
            CorrelationScore score = it.next();
            buff.append(carc.getColorImpressionKnowledge().toPrintName(score.getWord()));
            buff.append("=");
            buff.append(new BigDecimal(score.getValue()).setScale(5, RoundingMode.HALF_UP).doubleValue());
            if (it.hasNext() && i + 1 < k) {
                buff.append(", ");
            }
        }
        return buff.toString();
    }
}
