package mediamatrix.gui;

import mediamatrix.db.ChronoArchive;
import mediamatrix.db.MediaMatrix;
import mediamatrix.db.PrimitiveEngine;
import mediamatrix.io.ChronoArchiveFileFilter;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.utils.ImageUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public final class MediaInspectorPanel extends javax.swing.JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ColorSchemeSelectionPanel colorSchemeSelectionPanel;
    private Point previous;
    private final JPanel canvasPanel;
    private final LinkedList<MediaMatrixChart> chartList;
    private final LinkedList<Lens> lensList;
    private final HandMoveMouseListener listener;
    private JDialog lensDialog;
    private ArrayList<String> selectedWords;

    @SuppressWarnings("unchecked")
    public MediaInspectorPanel() {
        initComponents();
        chartList = new LinkedList<>();
        lensList = new LinkedList<>();
        listener = new HandMoveMouseListener();
        canvasPanel = new JPanel() {

            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        canvasPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        canvasPanel.setOpaque(true);
        canvasScrollPane.setViewportView(canvasPanel);
        colorSchemeSelectionPanel = new ColorSchemeSelectionPanel();
        colorSchemeSelectionPanel.addPropertyChangeListener("colorschema", (PropertyChangeEvent evt) -> {
            selectedWords = (ArrayList<String>) evt.getNewValue();
        });
        add(colorSchemeSelectionPanel, BorderLayout.WEST);
        aFileChooser.setFileFilter(new ChronoArchiveFileFilter());
    }

    private void drawCharts() {
        canvasPanel.setVisible(false);
        canvasPanel.removeAll();
        final List<Lens> reversedLens = new ArrayList<>(lensList);
        Collections.reverse(reversedLens);
        for (int i = 0; i < reversedLens.size(); i++) {
            Lens lens = reversedLens.get(i);
            canvasPanel.add(lens.getPanel(), new org.netbeans.lib.awtextra.AbsoluteConstraints(lens.getX(), 0, lens.getWidth(), canvasPanel.getHeight()));
        }
        final List<MediaMatrixChart> reversed = new ArrayList<>(chartList);
        Collections.reverse(reversed);
        for (int i = 0; i < reversed.size(); i++) {
            MediaMatrixChart mediaMatrixChart = reversed.get(i);
            if (i == reversed.size() - 1) {
                mediaMatrixChart.setTransparent(false);
            } else {
                mediaMatrixChart.setTransparent(true);
            }
            canvasPanel.add(mediaMatrixChart.getLabel(), new org.netbeans.lib.awtextra.AbsoluteConstraints(mediaMatrixChart.getX(), mediaMatrixChart.getY(), -1, -1));
        }
        canvasPanel.setVisible(true);
        canvasPanel.invalidate();
        canvasPanel.repaint();
    }

    public void open(File file) {
        for (MediaMatrixChart mediaMatrixChart : chartList) {
            mediaMatrixChart.getLabel().removeMouseListener(listener);
            mediaMatrixChart.getLabel().removeMouseMotionListener(listener);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new DrawWorker(file).execute();
    }

    private void updateLens() {
        for (Lens lens : lensList) {
            for (MediaMatrixChart mediaMatrixChart : chartList) {
                if (lens.getX() > mediaMatrixChart.getX() && lens.getX() < mediaMatrixChart.getX() + mediaMatrixChart.getImage().getWidth()) {
                    final ChronoArchive carc = mediaMatrixChart.getCarc();
                    final double ratio = (double) (lens.getX() - mediaMatrixChart.getX() - mediaMatrixChart.getXMargin()) / (double) (mediaMatrixChart.getLineWidth());
                    final int start = (int) ((carc.size() + ((Number) marginSpinner.getValue()).intValue()) * ratio);
                    final DefaultListModel<ImageShot> model = new DefaultListModel<>();
                    for (int i = start; i < (start + 5) && i < carc.size(); i++) {
                        try {
                            ImageShot shot = new ImageShot(i, ImageUtilities.imageToBufferedImage(ImageUtilities.createThumbnail(carc.getImage(i), 90, 90)));
                            model.addElement(shot);
                        } catch (IOException ex) {
                            ErrorUtils.showDialog(ex, this);
                        }
                    }
                    mediaMatrixChart.getList().setModel(model);
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aFileChooser = new javax.swing.JFileChooser();
        aToolBar = new javax.swing.JToolBar();
        openButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        addLensButton = new javax.swing.JButton();
        removeLensButton = new javax.swing.JButton();
        marginSpinner = new javax.swing.JSpinner();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        separateToggleButton = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel4 = new javax.swing.JLabel();
        widthSpinner = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        heightSpinner = new javax.swing.JSpinner();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        redrawButton = new javax.swing.JButton();
        redrawAllButton = new javax.swing.JButton();
        aTabbedPane = new javax.swing.JTabbedPane();
        contentSplitPane = new javax.swing.JSplitPane();
        canvasScrollPane = new javax.swing.JScrollPane();
        lensPanel = new javax.swing.JPanel();
        lensScrollPane = new javax.swing.JScrollPane();
        lensBoxPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        aToolBar.setRollover(true);

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediamatrix/resources/Fileopen.png"))); // NOI18N
        openButton.setText("Open");
        openButton.setFocusable(false);
        openButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        aToolBar.add(openButton);

        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediamatrix/resources/Fileclose.png"))); // NOI18N
        closeButton.setText("Close");
        closeButton.setFocusable(false);
        closeButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        aToolBar.add(closeButton);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediamatrix/resources/Filesave.png"))); // NOI18N
        jButton1.setText("Save as");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        aToolBar.add(jButton1);
        aToolBar.add(jSeparator3);

        addLensButton.setText("Add lens");
        addLensButton.setFocusable(false);
        addLensButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        addLensButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLensButtonActionPerformed(evt);
            }
        });
        aToolBar.add(addLensButton);

        removeLensButton.setText("Remove lens");
        removeLensButton.setFocusable(false);
        removeLensButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        removeLensButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeLensButtonActionPerformed(evt);
            }
        });
        aToolBar.add(removeLensButton);

        marginSpinner.setModel(new javax.swing.SpinnerNumberModel());
        marginSpinner.setMaximumSize(new java.awt.Dimension(50, 32767));
        aToolBar.add(marginSpinner);
        aToolBar.add(jSeparator2);

        separateToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediamatrix/resources/Window.png"))); // NOI18N
        separateToggleButton.setText("Separate");
        separateToggleButton.setFocusable(false);
        separateToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        separateToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separateToggleButtonActionPerformed(evt);
            }
        });
        aToolBar.add(separateToggleButton);
        aToolBar.add(jSeparator1);

        jLabel4.setText("Width:");
        aToolBar.add(jLabel4);

        widthSpinner.setModel(new javax.swing.SpinnerNumberModel(800, 300, 20000, 1));
        widthSpinner.setMaximumSize(new java.awt.Dimension(80, 32767));
        aToolBar.add(widthSpinner);

        jLabel5.setText("Height:");
        aToolBar.add(jLabel5);

        heightSpinner.setModel(new javax.swing.SpinnerNumberModel(800, 300, 2000, 1));
        heightSpinner.setMaximumSize(new java.awt.Dimension(80, 32767));
        aToolBar.add(heightSpinner);
        aToolBar.add(jSeparator5);

        redrawButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediamatrix/resources/Colortable.png"))); // NOI18N
        redrawButton.setText("Redraw");
        redrawButton.setFocusable(false);
        redrawButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        redrawButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redrawButtonActionPerformed(evt);
            }
        });
        aToolBar.add(redrawButton);

        redrawAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediamatrix/resources/Colortable.png"))); // NOI18N
        redrawAllButton.setText("Redraw all");
        redrawAllButton.setFocusable(false);
        redrawAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        redrawAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redrawAllButtonActionPerformed(evt);
            }
        });
        aToolBar.add(redrawAllButton);

        add(aToolBar, java.awt.BorderLayout.PAGE_START);

        contentSplitPane.setDividerLocation(500);
        contentSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        contentSplitPane.setContinuousLayout(true);

        canvasScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        canvasScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        contentSplitPane.setLeftComponent(canvasScrollPane);

        lensPanel.setLayout(new java.awt.BorderLayout());

        lensScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        lensScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        lensBoxPanel.setLayout(new javax.swing.BoxLayout(lensBoxPanel, javax.swing.BoxLayout.Y_AXIS));
        lensScrollPane.setViewportView(lensBoxPanel);

        lensPanel.add(lensScrollPane, java.awt.BorderLayout.CENTER);

        contentSplitPane.setRightComponent(lensPanel);

        aTabbedPane.addTab("Inspector Canvas", contentSplitPane);

        add(aTabbedPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        aFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (aFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            open(aFileChooser.getSelectedFile());
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private void redrawButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redrawButtonActionPerformed
        for (MediaMatrixChart mediaMatrixChart : chartList) {
            mediaMatrixChart.getLabel().removeMouseListener(listener);
            mediaMatrixChart.getLabel().removeMouseMotionListener(listener);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new RedrawWorker(chartList.get(chartList.size() - 1)).execute();
    }//GEN-LAST:event_redrawButtonActionPerformed

    private void redrawAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redrawAllButtonActionPerformed
        for (MediaMatrixChart mediaMatrixChart : chartList) {
            mediaMatrixChart.getLabel().removeMouseListener(listener);
            mediaMatrixChart.getLabel().removeMouseMotionListener(listener);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new RedrawAllWorker().execute();
    }//GEN-LAST:event_redrawAllButtonActionPerformed

    private void addLensButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLensButtonActionPerformed
        final JPanel p = new JPanel() {

            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(10, 10, 10, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.addMouseListener(listener);
        p.addMouseMotionListener(listener);
        p.setSize(10, canvasPanel.getHeight());
        Lens lens = new Lens(p, 10, p.getWidth());
        lensList.add(lens);
        canvasPanel.add(p, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, p.getWidth(), p.getHeight()));
    }//GEN-LAST:event_addLensButtonActionPerformed

    private void separateToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separateToggleButtonActionPerformed
        if (separateToggleButton.isSelected()) {
            lensDialog = new JDialog(SwingUtilities.getWindowAncestor(this), ModalityType.MODELESS);
            lensDialog.setTitle("Lens");
            contentSplitPane.remove(lensPanel);
            lensDialog.getContentPane().setLayout(new BorderLayout());
            lensDialog.getContentPane().add(lensPanel, BorderLayout.CENTER);
            lensDialog.setSize(400, 400);
            lensDialog.setLocationRelativeTo(null);
            lensDialog.setVisible(true);
        } else {
            lensDialog.getContentPane().remove(lensPanel);
            lensDialog.dispose();
            contentSplitPane.setBottomComponent(lensPanel);
        }
    }//GEN-LAST:event_separateToggleButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        chartList.remove(chartList.get(0));
        drawCharts();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void removeLensButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLensButtonActionPerformed
    }//GEN-LAST:event_removeLensButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser aFileChooser;
    private javax.swing.JTabbedPane aTabbedPane;
    private javax.swing.JToolBar aToolBar;
    private javax.swing.JButton addLensButton;
    private javax.swing.JScrollPane canvasScrollPane;
    private javax.swing.JButton closeButton;
    private javax.swing.JSplitPane contentSplitPane;
    private javax.swing.JSpinner heightSpinner;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JPanel lensBoxPanel;
    private javax.swing.JPanel lensPanel;
    private javax.swing.JScrollPane lensScrollPane;
    private javax.swing.JSpinner marginSpinner;
    private javax.swing.JButton openButton;
    private javax.swing.JButton redrawAllButton;
    private javax.swing.JButton redrawButton;
    private javax.swing.JButton removeLensButton;
    private javax.swing.JToggleButton separateToggleButton;
    private javax.swing.JSpinner widthSpinner;
    // End of variables declaration//GEN-END:variables

    class DrawWorker extends SwingWorker<MediaMatrixChart, Object> {

        private final File file;

        public DrawWorker(File file) {
            this.file = file;
        }

        @Override
        protected MediaMatrixChart doInBackground() throws Exception {
            final PrimitiveEngine pe = new PrimitiveEngine();
            final ChronoArchive carc = new ChronoArchive(file);
            MediaMatrix mat = carc.getMatrix();
            SwingUtilities.invokeAndWait(() -> {
                ColorImpressionKnowledge ci = carc.getColorImpressionKnowledge();
                colorSchemeSelectionPanel.setColorScheme(ci);
            });
            int width = ((Integer) widthSpinner.getModel().getValue());
            int height = ((Integer) heightSpinner.getModel().getValue());
            mat = pe.projection(mat, selectedWords);
            final BufferedImage image = new VisualizationEngine().createChartImage(mat, Color.lightGray, width, height);
            return new MediaMatrixChart(new ChronoArchive(file), mat, image, chartList.size() * 20, chartList.size() * 20, image.getWidth(), image.getHeight());
        }

        @Override
        protected void done() {
            try {
                final MediaMatrixChart chart = get();
                final JList<ImageShot> list = new JList<>();
                final DefaultListModel<ImageShot> model = new DefaultListModel<>();
                list.setFixedCellHeight(100);
                list.setFixedCellWidth(100);
                list.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
                list.setVisibleRowCount(1);
                list.setModel(model);
                list.setCellRenderer(new ImageListCellRenderer());
                final JScrollPane listScrollPane = new JScrollPane();
                listScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                listScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                listScrollPane.setViewportView(list);
                lensBoxPanel.add(listScrollPane);
                chart.setList(list);
                final JLabel label = new JLabel(new ImageIcon(chart.getImage()));
                chart.setLabel(label);
                label.setOpaque(false);
                chartList.add(chart);
                drawCharts();
                for (MediaMatrixChart mediaMatrixChart : chartList) {
                    mediaMatrixChart.getLabel().addMouseListener(listener);
                    mediaMatrixChart.getLabel().addMouseMotionListener(listener);
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } catch (InterruptedException | ExecutionException ex) {
                ErrorUtils.showDialog(ex, MediaInspectorPanel.this);
            }
        }
    }

    class RedrawAllWorker extends SwingWorker<Object, Object> {

        public RedrawAllWorker() {
        }

        @Override
        protected Object doInBackground() throws Exception {
            int width = ((Integer) widthSpinner.getModel().getValue());
            int height = ((Integer) heightSpinner.getModel().getValue());
            for (MediaMatrixChart chart : chartList) {
                final PrimitiveEngine pe = new PrimitiveEngine();
                final MediaMatrix mat = pe.projection(chart.getMatrix(), selectedWords);
                final BufferedImage image = new VisualizationEngine().createChartImage(mat, Color.lightGray, width, height);
                chart.setImage(image);
                chart.setHeight(height);
                chart.setWidth(width);
            }
            return chartList;
        }

        @Override
        protected void done() {
            drawCharts();
            for (MediaMatrixChart mediaMatrixChart : chartList) {
                mediaMatrixChart.getLabel().addMouseListener(listener);
                mediaMatrixChart.getLabel().addMouseMotionListener(listener);
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    class RedrawWorker extends SwingWorker<MediaMatrixChart, Object> {

        private final MediaMatrixChart chart;

        public RedrawWorker(MediaMatrixChart chart) {
            this.chart = chart;
        }

        @Override
        protected MediaMatrixChart doInBackground() throws Exception {
            int width = ((Integer) widthSpinner.getModel().getValue());
            int height = ((Integer) heightSpinner.getModel().getValue());
            final PrimitiveEngine pe = new PrimitiveEngine();
            final MediaMatrix mat = pe.projection(chart.getMatrix(), selectedWords);
            final BufferedImage image = new VisualizationEngine().createChartImage(mat, Color.lightGray, width, height);
            chart.setImage(image);
            chart.setHeight(height);
            chart.setWidth(width);
            return chart;
        }

        @Override
        protected void done() {
            drawCharts();
            for (MediaMatrixChart mediaMatrixChart : chartList) {
                mediaMatrixChart.getLabel().addMouseListener(listener);
                mediaMatrixChart.getLabel().addMouseMotionListener(listener);
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    class HandMoveMouseListener extends MouseAdapter implements MouseMotionListener, Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public void mouseDragged(MouseEvent e) {
            if (e.getComponent() instanceof JLabel jLabel) {
                final MediaMatrixChart chart = MediaMatrixChart.findByLabel(chartList, jLabel);
                int xdiff = e.getPoint().x - previous.x;
                int ydiff = e.getPoint().y - previous.y;
                chart.setX(chart.getX() + xdiff);
                chart.setY(chart.getY() + ydiff);
            } else {
                final Lens lens = Lens.findByPanel(lensList, (JPanel) e.getComponent());
                int xdiff = e.getPoint().x - previous.x;
                lens.setX(lens.getX() + xdiff);
            }
            drawCharts();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            setCursor(CursorUtils.getOpenHandCursor());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        @Override
        public void mousePressed(MouseEvent e) {
            previous = e.getPoint();
            if (e.getComponent() instanceof JLabel) {
                final MediaMatrixChart chart = MediaMatrixChart.findByLabel(chartList, (JLabel) e.getComponent());
                chartList.remove(chart);
                chartList.add(chart);
            } else {
                final Lens lens = Lens.findByPanel(lensList, (JPanel) e.getComponent());
                lensList.remove(lens);
                lensList.add(lens);
            }
            setCursor(CursorUtils.getClosedHandCursor());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            updateLens();
            setCursor(CursorUtils.getOpenHandCursor());
        }
    }

    class ImageListCellRenderer extends JLabel implements ListCellRenderer<ImageShot> {

        private static final long serialVersionUID = 1L;

        public ImageListCellRenderer() {
            setOpaque(false);
            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.CENTER);
            setVerticalTextPosition(JLabel.BOTTOM);
            setHorizontalTextPosition(JLabel.CENTER);
            setFont(new Font("Monospaced", Font.PLAIN, 10));
            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        public Component getListCellRendererComponent(final JList<? extends ImageShot> list, final ImageShot value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            setText(Double.toString(value.getTime()));
            setIcon(new ImageIcon(value.getThumbnail()));
            return this;
        }
    }
}

class Lens implements Serializable {

    public static final long serialVersionUID = 1L;
    private JPanel panel;
    private int x;
    private int width;
    private int previousX;

    public static Lens findByPanel(List<Lens> list, JPanel panel) {
        Lens result = null;
        for (Lens lens : list) {
            if (lens.panel == panel) {
                result = lens;
                break;
            }
        }
        return result;
    }

    public Lens(JPanel panel, int x, int width) {
        this.panel = panel;
        this.x = x;
        this.width = width;
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }

    public int getPreviousX() {
        return previousX;
    }

    public void setPreviousX(int previousX) {
        this.previousX = previousX;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }
}

class MediaMatrixChart implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private transient ChronoArchive carc;
    private JList<ImageShot> list;
    private MediaMatrix matrix;
    private transient BufferedImage image;
    private transient BufferedImage alphaImage;
    private JLabel label;
    private int x;
    private int y;
    private int width;
    private int height;
    private int previousX;
    private int previousY;
    private int lineWidth;
    private int xMargin;

    public static MediaMatrixChart findByLabel(List<MediaMatrixChart> charts, JLabel label) {
        MediaMatrixChart result = null;
        for (MediaMatrixChart mediaMatrixChart : charts) {
            if (mediaMatrixChart.label == label) {
                result = mediaMatrixChart;
                break;
            }
        }
        return result;
    }

    public MediaMatrixChart(ChronoArchive carc, MediaMatrix mat, BufferedImage image, int x, int y, int width, int height) {
        this.carc = carc;
        this.matrix = mat;
        this.image = image;
        alphaImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g2 = (Graphics2D) alphaImage.getGraphics();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color c = new Color(image.getRGB(i, j));
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
                g2.drawLine(i, j, i, j);
            }
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        int line = 0;
        for (int i = image.getHeight() - 1; i >= 0; i--) {
            int count = 0;
            for (int j = 0; j < image.getWidth(); j++) {
                if (new Color(image.getRGB(j, i)).equals(new Color(128, 128, 128))) {
                    count++;
                }
            }
            int max = Math.max(this.lineWidth, count);
            if (max == count) {
                line = i;
                this.lineWidth = count;
            }
        }
        for (int i = 0; i < image.getWidth(); i++) {
            if (new Color(image.getRGB(i, line)).equals(new Color(128, 128, 128))) {
                this.xMargin = i;
                break;
            }
        }
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public int getXMargin() {
        return xMargin;
    }

    public void setXMargin(int xMargin) {
        this.xMargin = xMargin;
    }

    public JList<ImageShot> getList() {
        return list;
    }

    public void setList(JList<ImageShot> list) {
        this.list = list;
    }

    public ChronoArchive getCarc() {
        return carc;
    }

    public void setCarc(ChronoArchive carc) {
        this.carc = carc;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.alphaImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g2 = (Graphics2D) alphaImage.getGraphics();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color c = new Color(image.getRGB(i, j));
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
                g2.drawLine(i, j, i, j);
            }
        }

        int line = 0;
        for (int i = image.getHeight() - 1; i >= 0; i--) {
            int count = 0;
            for (int j = 0; j < image.getWidth(); j++) {
                if (new Color(image.getRGB(j, i)).equals(new Color(128, 128, 128))) {
                    count++;
                }
            }
            int max = Math.max(this.lineWidth, count);
            if (max == count) {
                line = i;
                this.lineWidth = count;
            }
        }
        for (int i = 0; i < image.getWidth(); i++) {
            if (new Color(image.getRGB(i, line)).equals(new Color(128, 128, 128))) {
                this.xMargin = x;
                break;
            }
        }

    }

    public BufferedImage getAlphaImage() {
        return alphaImage;
    }

    public void setAlphaImage(BufferedImage alphaImage) {
        this.alphaImage = alphaImage;
    }

    public MediaMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(MediaMatrix matrix) {
        this.matrix = matrix;
    }

    public void setTransparent(boolean trans) {
        if (trans) {
            ((ImageIcon) label.getIcon()).setImage(alphaImage);
        } else {
            ((ImageIcon) label.getIcon()).setImage(image);
        }
    }

    public boolean isTransparent() {
        return ((ImageIcon) label.getIcon()).getImage() == alphaImage;
    }

    public JLabel getLabel() {
        return label;
    }

    public void setLabel(JLabel label) {
        this.label = label;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPreviousX() {
        return previousX;
    }

    public void setPreviousX(int previousX) {
        this.previousX = previousX;
    }

    public int getPreviousY() {
        return previousY;
    }

    public void setPreviousY(int previousY) {
        this.previousY = previousY;
    }
}
