package mediamatrix.utils;

import java.io.IOException;
import mediamatrix.db.ChronoArchive;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import mediamatrix.db.CorrelationScore;
import mediamatrix.gui.ImageShot;
import mediamatrix.mvc.ImageShotListCellRenderer;

public class DominanceEmergenceVisualizerPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public DominanceEmergenceVisualizerPanel(final File file, int width, int height, double threshold, boolean isECS) throws IOException {
        super(new BorderLayout());
        final ChronoArchive carc = new ChronoArchive(file);
        final JLabel imageLabel = new JLabel();
        final JList<ImageShot> imageList = new JList<ImageShot>();
        final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        final JList<CorrelationScore> csList = new JList<CorrelationScore>();
        final JScrollPane chartScrollPane = new JScrollPane(imageLabel);
        final JScrollPane listScrollPane = new JScrollPane(imageList);
        listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageList.setFixedCellHeight(100);
        imageList.setFixedCellWidth(100);
        imageList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        imageList.setVisibleRowCount(0);
        imageList.setCellRenderer(new ImageShotListCellRenderer());
        split.setTopComponent(chartScrollPane);
        split.setBottomComponent(listScrollPane);
        final JScrollPane scrollPane = new JScrollPane(csList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.WEST);
        add(split, BorderLayout.CENTER);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new DominanceEmergenceAnalysisWorker(this, imageLabel, imageList, csList, carc, width, height, threshold, isECS).execute();
    }
}
