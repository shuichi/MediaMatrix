/* MediaMatrix -- A Programable Database Engine for Multimedia
 * Copyright (C) 2008-2010 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mediamatrix.gui;

import mediamatrix.db.ChronoArchive;
import mediamatrix.db.CorrelationScore;
import mediamatrix.db.NeighborRelevance;
import mediamatrix.db.MediaMatrix;
import mediamatrix.db.PrimitiveEngine;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.utils.ImageUtilities;
import mediamatrix.mvc.DoubleTableCellRenderer;
import mediamatrix.mvc.ImpressionWordTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import mediamatrix.mvc.ImageTableCellRenderer;

public final class RollerPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final SpinnerNumberModel model = new SpinnerNumberModel(0.4d, 0.0d, 1.0d, 0.001d);
    private final JScrollPane hvScrollPane = new JScrollPane();
    private final File file;
    private transient ChronoArchive carc;

    public RollerPanel(File file) {
        super(new BorderLayout());
        this.file = file;
        hvScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        hvScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        hvScrollPane.setBorder(BorderFactory.createEtchedBorder());
        final JSpinner spinner = new JSpinner(model);
        final JButton applyButton = new JButton("Apply");
        applyButton.addActionListener((ActionEvent e) -> {
            updateContent();
        });
        final JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spinner.setPreferredSize(new Dimension(100, applyButton.getPreferredSize().height));
        toolbar.add(new JLabel("Threshold:"));
        toolbar.add(spinner);
        toolbar.add(applyButton);
        updateContent();
        add(toolbar, BorderLayout.NORTH);
        add(hvScrollPane, BorderLayout.CENTER);
    }

    public static Set<Integer> selectDelimiters(ChronoArchive carc, double threshold) {
        final NeighborRelevance neighbor = carc.getNeighborRelevance();
        final Set<Integer> delims = new TreeSet<>();
        for (int i = 0; i < neighbor.size(); i++) {
            if (neighbor.getValue(i) > threshold) {
                delims.add((int) neighbor.getEnd(i));
            }
        }
        return delims;
    }

    private void updateContent() {
        try {
            carc = new ChronoArchive(file);
        } catch (IOException ex) {
            ErrorUtils.showDialog(ex, this);
            return;
        }
        final Set<Integer> delims = selectDelimiters(carc, model.getNumber().doubleValue());
        final List<ClusterInformationPanel> panels = new ArrayList<>();
        final StackableSplitPane aStackableSplitPane = new StackableSplitPane();
        int begin = 0;
        int index = 0;
        for (Iterator<Integer> it = delims.iterator(); it.hasNext();) {
            final Integer end = it.next();
            final ChronoArchive subcarc = carc.subArchive(begin, end);
            final ClusterInformationPanel panel = new ClusterInformationPanel(index++, subcarc);
            panels.add(panel);
            begin = end;
        }
        for (ClusterInformationPanel clusterInformationPanel : panels) {
            clusterInformationPanel.setSize(new Dimension(200, getPreferredSize().height));
            clusterInformationPanel.setPreferredSize(new Dimension(200, getPreferredSize().height));
            aStackableSplitPane.addComponentColumn(clusterInformationPanel);
        }
        hvScrollPane.getViewport().setView(aStackableSplitPane);
    }

    final class ClusterInformationPanel extends JPanel {

        @Serial
        private static final long serialVersionUID = 1L;

        public ClusterInformationPanel(final int index, final ChronoArchive c) {
            super(new BorderLayout());
            add(new JLabel("" + index), BorderLayout.NORTH);
            final JList<Image> aList = new JList<>();
            final JSplitPane aSplitPane = new JSplitPane();
            final JScrollPane listScrollPane = new JScrollPane();
            final JTable scoreTable = new JTable();
            final JScrollPane tableScrollPane = new JScrollPane();
            final PrimitiveEngine pe = new PrimitiveEngine();
            final MediaMatrix mat = c.getMatrix();
            listScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            listScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            listScrollPane.setMinimumSize(new java.awt.Dimension(23, 50));
            listScrollPane.setViewportView(aList);
            tableScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            tableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            tableScrollPane.setViewportView(scoreTable);
            aSplitPane.setDividerLocation(200);
            aSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            aSplitPane.setContinuousLayout(true);
            aSplitPane.setOneTouchExpandable(true);
            aSplitPane.setTopComponent(listScrollPane);
            aSplitPane.setRightComponent(tableScrollPane);

            aList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
            aList.setCellRenderer(new ImageTableCellRenderer());
            aList.setFixedCellHeight(55);
            aList.setFixedCellWidth(55);
            aList.setVisibleRowCount(0);
            aList.setModel(new CARCListModel(carc, c));

            scoreTable.setModel(new CorrelationScoreTableModel(new ArrayList<>(pe.sortedScore(pe.histogram(mat)))));
            final ColorImpressionKnowledge ci = carc.getColorImpressionKnowledge();
            scoreTable.setDefaultRenderer(String.class, new ImpressionWordTableCellRenderer(14f, ci));
            scoreTable.setDefaultRenderer(Double.class, new DoubleTableCellRenderer(13f, 2));
            scoreTable.setRowHeight(55);
            aList.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        try {
                            final int i = aList.getSelectedIndex();
                            final BufferedImage image = carc.getImage((int) c.getSegmentTime(i));
                            DialogUtils.showDialog(carc.getFileName(), new MunsellImagePanel(image, c.getColorImpressionKnowledge()), aList);
                        } catch (IOException ex) {
                            ErrorUtils.showDialog(ex, aList);
                        }
                    }
                }
            });
            add(aSplitPane, java.awt.BorderLayout.CENTER);
        }
    }
}

class CARCListModel extends AbstractListModel<Image> {

    private static final long serialVersionUID = 1L;
    private final transient ChronoArchive originalCarc;
    private final transient ChronoArchive carc;
    private final transient WeakHashMap<Integer, BufferedImage> imgCache;

    public CARCListModel(ChronoArchive originalCarc, ChronoArchive carc) {
        this.originalCarc = originalCarc;
        this.carc = carc;
        imgCache = new WeakHashMap<>();
    }

    @Override
    public Image getElementAt(int index) {
        BufferedImage im = imgCache.get(index);
        if (im == null) {
            try {
                im = ImageUtilities.createThumbnail(originalCarc.getImage((int) carc.getSegmentTime(index)), 50, 50);
                imgCache.put(index, im);
            } catch (IOException ex) {
            }
        }
        return im;
    }

    @Override
    public int getSize() {
        return carc.size();
    }
}
