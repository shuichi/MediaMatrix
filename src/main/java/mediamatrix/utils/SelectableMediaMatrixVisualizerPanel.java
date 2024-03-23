package mediamatrix.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import mediamatrix.db.ChronoArchive;
import mediamatrix.db.MediaMatrix;
import mediamatrix.db.PrimitiveEngine;
import mediamatrix.gui.ColorSchemeSelectionPanel;
import mediamatrix.gui.ErrorUtils;
import mediamatrix.gui.ImageShot;
import mediamatrix.gui.VisualizationEngine;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.mvc.CARCImageShotListModel;
import mediamatrix.mvc.ImageShotListCellRenderer;

public final class SelectableMediaMatrixVisualizerPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private ArrayList<String> selectedWords;
    private JLabel canvas;
    private CARCImageShotListModel model;
    private JList<ImageShot> list;

    @SuppressWarnings("unchecked")
    public SelectableMediaMatrixVisualizerPanel(final File file, final int width, final int height) throws IOException {
        super(new BorderLayout());
        canvas = new JLabel();
        canvas.setHorizontalAlignment(SwingConstants.CENTER);
        final ColorSchemeSelectionPanel colorSchemeSelectionPanel = new ColorSchemeSelectionPanel();
        colorSchemeSelectionPanel.addPropertyChangeListener("colorschema", (PropertyChangeEvent evt) -> {
            selectedWords = (ArrayList<String>) evt.getNewValue();
        });
        list = new JList<>();
        list.setFixedCellHeight(100);
        list.setFixedCellWidth(100);
        list.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(1);
        list.setCellRenderer(new ImageShotListCellRenderer());
        final JScrollPane listScrollPane = new JScrollPane();
        listScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        listScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        listScrollPane.setViewportView(list);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        add(listScrollPane, BorderLayout.SOUTH);
        add(colorSchemeSelectionPanel, BorderLayout.WEST);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<BufferedImage, Object>() {

            @Override
            protected BufferedImage doInBackground() throws Exception {
                final PrimitiveEngine pe = new PrimitiveEngine();
                final ChronoArchive carc = new ChronoArchive(file);
                MediaMatrix mat = carc.getMatrix();
                SwingUtilities.invokeAndWait(() -> {
                    ColorImpressionKnowledge ci = carc.getColorImpressionKnowledge();
                    colorSchemeSelectionPanel.setColorScheme(ci);
                });
                model = new CARCImageShotListModel(carc);
                mat = pe.projection(mat, selectedWords);
                return new VisualizationEngine().createChartImage(mat, Color.lightGray, width, height);
            }

            @Override
            protected void done() {
                try {
                    final BufferedImage image = get();
                    canvas.setIcon(new ImageIcon(image));
                    list.setModel(model);
                    revalidate();
                    repaint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                } catch (InterruptedException | ExecutionException ex) {
                    ErrorUtils.showDialog(ex, SelectableMediaMatrixVisualizerPanel.this);
                }
            }
        }.execute();
    }
}
