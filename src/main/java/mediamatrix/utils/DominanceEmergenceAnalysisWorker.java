package mediamatrix.utils;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import mediamatrix.db.ChronoArchive;
import mediamatrix.db.CorrelationScore;
import mediamatrix.db.MediaMatrix;
import mediamatrix.db.PrimitiveEngine;
import mediamatrix.gui.DialogUtils;
import mediamatrix.gui.ErrorUtils;
import mediamatrix.gui.ImageShot;
import mediamatrix.gui.MunsellImagePanel;
import mediamatrix.gui.VisualizationEngine;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.mvc.DominanceEmergenceAnalysisResult;
import mediamatrix.mvc.ImageShotListModel;
import mediamatrix.mvc.ImpressionWordListCellRenderer;

public class DominanceEmergenceAnalysisWorker extends SwingWorker<DominanceEmergenceAnalysisResult, Object> {

    private JComponent parent;
    private final JLabel imageLabel;
    private final JList<ImageShot> imageList;
    private final JList<CorrelationScore> csList;
    private final ChronoArchive carc;
    private int width;
    private int height;
    private double threshold;
    private final boolean isECS;

    public DominanceEmergenceAnalysisWorker(JComponent parent, JLabel imageLabel, JList<ImageShot> imageList, JList<CorrelationScore> csList, ChronoArchive carc, int width, int height, double threshold, boolean isECS) {
        this.parent = parent;
        this.imageLabel = imageLabel;
        this.imageList = imageList;
        this.csList = csList;
        this.carc = carc;
        this.width = width;
        this.height = height;
        this.threshold = threshold;
        this.isECS = isECS;
    }

    private List<ImageShot> analyze(MediaMatrix mat) throws IOException {
        final List<Double> sumList = new ArrayList<Double>();
        for (int i = 0; i < mat.getHeight(); i++) {
            double sum = 0d;
            for (int j = 0; j < mat.getWidth(); j++) {
                sum += (mat.get(i, j) * mat.get(i, j));
            }
            sum = Math.sqrt(sum);
            sumList.add(sum);
        }
        double totalSum = 0d;
        for (Double d : sumList) {
            totalSum += d;
        }
        final double average = totalSum / sumList.size();
        final List<ImageShot> result = new ArrayList<ImageShot>();
        for (int i = 0; i < sumList.size(); i++) {
            if (sumList.get(i) > average * threshold) {
                result.add(new ImageShot(i, carc.getImage(i)));
            }
        }
        return result;
    }

    @Override
    protected DominanceEmergenceAnalysisResult doInBackground() throws Exception {
        final PrimitiveEngine pe = new PrimitiveEngine();
        final VisualizationEngine ve = new VisualizationEngine();
        final MediaMatrix mat = carc.getMatrix();
        final ColorImpressionKnowledge ci = carc.getColorImpressionKnowledge();
        MediaMatrix result = null;
        if (isECS) {
            result = pe.ecs(mat);
        } else {
            result = pe.dcs(mat);
        }
        final BufferedImage image = ve.createChartImage(result, Color.lightGray, width, height);
        final ImageShotListModel model = new ImageShotListModel(analyze(result));
        return new DominanceEmergenceAnalysisResult(image, model, ci, result);
    }

    @Override
    protected void done() {
        try {
            final DominanceEmergenceAnalysisResult obj = get();
            imageLabel.setIcon(new ImageIcon(obj.getImage()));
            imageList.setModel(obj.getModel());
            imageList.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    if (!imageList.isSelectionEmpty()) {
                        final ImageShot p = imageList.getSelectedValue();
                        final TreeSet<CorrelationScore> score = new TreeSet<CorrelationScore>();
                        final int index = obj.getMatrix().getRowIndex(p.getTime());
                        for (int i = 0; i < obj.getMatrix().getWidth(); i++) {
                            score.add(new CorrelationScore(obj.getMatrix().getColumn(i), obj.getMatrix().get(index, i)));
                        }

                        final List<CorrelationScore> data = new ArrayList<CorrelationScore>(score);
                        csList.setCellRenderer(new ImpressionWordListCellRenderer(obj.getCi(), 10f));
                        csList.setModel(new AbstractListModel<CorrelationScore>() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public int getSize() {
                                return data.size();
                            }

                            @Override
                            public CorrelationScore getElementAt(int index) {
                                return data.get(index);
                            }
                        });
                        parent.revalidate();
                        parent.repaint();
                    }
                }
            });
            imageList.addMouseListener(new ListMouseAdapter(imageList, carc.getFileName(), obj.getCi()));
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } catch (Exception ex) {
            ErrorUtils.showDialog(ex, imageList);
        }
    }

    private class ListMouseAdapter extends MouseAdapter {

        private JList<ImageShot> imageList;
        private String filename;
        private ColorImpressionKnowledge ci;

        public ListMouseAdapter(JList<ImageShot> imageList, String filename, ColorImpressionKnowledge ci) {
            this.imageList = imageList;
            this.filename = filename;
            this.ci = ci;
        }

        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getClickCount() >= 2) {
                final ImageShot p = imageList.getSelectedValue();
                DialogUtils.showDialog(filename + "  [" + p.getTime() + "]", new MunsellImagePanel(p.getImage(), carc.getColorImpressionKnowledge()), imageList);
            }
        }
    }
}
