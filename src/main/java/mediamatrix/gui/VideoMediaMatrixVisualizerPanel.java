package mediamatrix.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import mediamatrix.db.CXMQLVisualizeScript;
import mediamatrix.db.ChronoArchive;
import mediamatrix.db.CorrelationScore;
import mediamatrix.db.MediaMatrix;
import mediamatrix.db.PrimitiveEngine;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.mvc.ImageShotListCellRenderer;
import mediamatrix.mvc.ImageShotListModel;
import mediamatrix.mvc.ImpressionWordListCellRenderer;
import org.jdesktop.swingx.JXBusyLabel;

public class VideoMediaMatrixVisualizerPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private VisualizeResult result;

    public VideoMediaMatrixVisualizerPanel(final MediaMatrix matrix, final CXMQLVisualizeScript script) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());
        final int width = Integer.parseInt(script.getProperty("width"));
        final int height = Integer.parseInt(script.getProperty("height"));
        final double t = Double.parseDouble(script.getProperty("threshold"));
        add(new JPanel() {

            private static final long serialVersionUID = 1L;
            private JLabel canvas;
            private JList<ImageShot> list;
            private JList<CorrelationScore> csList;
            private JPopupMenu popup;
            private BufferedImage image;

            {
                setLayout(new BorderLayout());
                setBorder(BorderFactory.createEmptyBorder());
                popup = new JPopupMenu();
                final JMenuItem saveAs = new JMenuItem("Save As");
                popup.add(saveAs);
                canvas = new JLabel();
                canvas.setHorizontalAlignment(SwingConstants.CENTER);
                canvas.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                });
                list = new JList<ImageShot>();
                csList = new JList<CorrelationScore>();
                csList.setFixedCellHeight(60);
                csList.setFixedCellWidth(130);
                list.setFixedCellHeight(100);
                list.setFixedCellWidth(100);
                list.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
                list.setVisibleRowCount(1);
                saveAs.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser filechooser = new JFileChooser();
                        if (filechooser.showSaveDialog(canvas) == JFileChooser.APPROVE_OPTION) {
                            File file = filechooser.getSelectedFile();
                            try {
                                ImageIO.write(image, "png", file);
                            } catch (IOException ex) {
                                ErrorUtils.showDialog(ex, canvas);
                            }
                        }
                    }
                });

                list.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        if (evt.getClickCount() >= 2) {
                            try {
                                final ImageShot p = list.getSelectedValue();
                                final ColorImpressionKnowledge ci = new ChronoArchive(script.getTarget()).getColorImpressionKnowledge();
                                DialogUtils.showDialog(script.getTarget() + "  [" + p.getTime() + "]", new MunsellImagePanel(p.getImage(), ci), list);
                            } catch (IOException ex) {
                                ErrorUtils.showDialog(ex, list);
                            }
                        }
                    }
                });
                list.addListSelectionListener(new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if (!list.isSelectionEmpty()) {
                            final ImageShot p = list.getSelectedValue();
                            final TreeSet<CorrelationScore> score = new TreeSet<CorrelationScore>();
                            final int index = result.getMatrix().getRowIndex(p.getTime());
                            for (int i = 0; i < result.getMatrix().getWidth(); i++) {
                                score.add(new CorrelationScore(result.getMatrix().getColumn(i), result.getMatrix().get(index, i)));
                            }

                            final List<CorrelationScore> data = new ArrayList<CorrelationScore>(score);
                            csList.setCellRenderer(new ImpressionWordListCellRenderer(result.getCi(), 10f));
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
                            csList.revalidate();
                            csList.repaint();
                        }
                    }
                });
                list.setCellRenderer(new ImageShotListCellRenderer());
                final JScrollPane listScrollPane = new JScrollPane();
                listScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                listScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                listScrollPane.setViewportView(list);
                add(new JScrollPane(csList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.EAST);
                add(new JScrollPane(canvas), BorderLayout.CENTER);
                add(listScrollPane, BorderLayout.SOUTH);
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                final JXBusyLabel label = new JXBusyLabel(new Dimension(100, 100));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.getBusyPainter().setPaintCentered(true);
                label.getBusyPainter().setPoints(15);
                label.getBusyPainter().setHighlightColor(new Color(44, 61, 146).darker());
                label.getBusyPainter().setBaseColor(new Color(168, 204, 241).brighter());
                label.setBusy(true);
                add(label, BorderLayout.CENTER);
                new SwingWorker<VisualizeResult, Object>() {

                    @Override
                    protected VisualizeResult doInBackground() throws Exception {
                        final ImageShotListModel model = new ImageShotListModel(new PrimitiveEngine().getImageShotList(matrix, t));
                        image = new VisualizationEngine().createChartImage(matrix, Color.lightGray, width, height);
                        final ColorImpressionKnowledge ci = new ChronoArchive(script.getTarget()).getColorImpressionKnowledge();
                        return new VisualizeResult(image, model, matrix, ci);
                    }

                    @Override
                    protected void done() {
                        try {
                            result = get();
                            canvas.setIcon(new ImageIcon(result.getImage()));
                            remove(label);
                            add(new JScrollPane(canvas), BorderLayout.CENTER);
                            list.setModel(result.getModel());
                            revalidate();
                            repaint();
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        } catch (Exception ex) {
                            ErrorUtils.showDialog(ex, VideoMediaMatrixVisualizerPanel.this);
                        }
                    }
                }.execute();
            }
        }, BorderLayout.CENTER);
    }

    private class VisualizeResult {

        BufferedImage image;
        ListModel<ImageShot> model;
        MediaMatrix matrix;
        ColorImpressionKnowledge ci;

        public VisualizeResult(BufferedImage image, ListModel<ImageShot> model, MediaMatrix matrix, ColorImpressionKnowledge ci) {
            this.image = image;
            this.model = model;
            this.matrix = matrix;
            this.ci = ci;
        }

        public ColorImpressionKnowledge getCi() {
            return ci;
        }

        public void setCi(ColorImpressionKnowledge ci) {
            this.ci = ci;
        }

        public BufferedImage getImage() {
            return image;
        }

        public void setImage(BufferedImage image) {
            this.image = image;
        }

        public MediaMatrix getMatrix() {
            return matrix;
        }

        public void setMatrix(MediaMatrix matrix) {
            this.matrix = matrix;
        }

        public ListModel<ImageShot> getModel() {
            return model;
        }

        public void setModel(ListModel<ImageShot> model) {
            this.model = model;
        }
    }
}
