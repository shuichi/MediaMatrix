package mediamatrix.gui;

import mediamatrix.db.MediaMatrix;
import mediamatrix.db.PrimitiveEngine;
import mediamatrix.music.DefaultColorMap;
import mediamatrix.music.Key;
import mediamatrix.music.MidiAnalyzer;
import mediamatrix.music.MusicScore;
import mediamatrix.music.TonalMusic;
import mediamatrix.music.TonalityAnalyzer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public final class MusicMediaMatrixPanel extends JPanel {

    private static final long serialVersionUID = -8556901560943216300L;
    private File file;
    private final JLabel statusLabel;

    public MusicMediaMatrixPanel() {
        initComponents();
        add(new TonalityColorPanel(), BorderLayout.SOUTH);
        final StatusBar bar = new StatusBar();
        statusLabel = new JLabel();
        final StatusBar.Constraint c1 = new StatusBar.Constraint();
        bar.add(statusLabel, c1);
        add(bar, BorderLayout.SOUTH);
    }

    public MusicMediaMatrixPanel(final File file) {
        this();
        open(file);
    }

    public final void open(final File file) {
        this.file = file;
        statusLabel.setText(file.getAbsolutePath());
        ratioSpinner.setEnabled(false);
        analysisButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new MusicOpenWorker(file).execute();
    }

    class MusicOpenWorker extends SwingWorker<Map<String, MediaMatrix>, Object> {

        private final File file;

        public MusicOpenWorker(File file) {
            this.file = file;
        }

        @Override
        protected Map<String, MediaMatrix> doInBackground() throws Exception {
            final PrimitiveEngine pe = new PrimitiveEngine();
            pe.setProperty("RATIO", ratioSpinner.getValue().toString());
            if (absoluteRadioButton.isSelected()) {
                pe.setProperty("DIV_MODE", "ABSOLUTE");
            }
            if (relativeRadioButton.isSelected()) {
                pe.setProperty("DIV_MODE", "RELATIVE");
            }
            InputStream input = new BufferedInputStream(new FileInputStream(file));
            final Map<String, MediaMatrix> result = new TreeMap<>();
            final double ratio = Double.parseDouble(pe.getProperty("RATIO"));
            final double denomi = Double.parseDouble(pe.getProperty("DENOMI"));
            final MidiAnalyzer analyzer = new MidiAnalyzer(input);
            MusicScore tempSc = null;
            if (pe.getProperty("TRACK") != null) {
                tempSc = analyzer.parseMultiTrack()[Integer.parseInt(pe.getProperty("TRACK"))];
            } else if (pe.getProperty("TRACKS") != null) {
                final String[] nums = pe.getProperty("TRACKS").split(",");
                final int[] tracks = new int[nums.length];
                for (int i = 0; i < nums.length; i++) {
                    tracks[i] = Integer.parseInt(nums[i]);
                }
                tempSc = analyzer.parseSpecificTracks(tracks);
            } else {
                tempSc = analyzer.parse();
            }
            analyzer.close();

            final MusicScore score = tempSc;
            double barLength = 0d;
            if (pe.getProperty("DIV_MODE").equals("RELATIVE")) {
                barLength = Math.ceil(score.getLengthAsMicrosecond() / (1000d * denomi) / ratio);
            } else {
                barLength = Math.ceil(score.getBarLength() / (1000d * denomi)) * ratio;
            }
            if (barLength < 1.0) {
                barLength = 180.0;
            }
            final double fixedBarLength = barLength;
            final MediaMatrix mat = pe.convertPitch(score, denomi);
            result.put("Tonality", pe.tonality(mat, fixedBarLength));
            result.put("Pitch", pe.convertContinuousPitch(score, denomi));
            result.put("Tempo", pe.convertContinuousTempo(score, denomi));
            result.put("Velocity", pe.convertContinuousVelocity(score, denomi));
            return result;
        }

        @Override
        protected void done() {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                ratioSpinner.setEnabled(true);
                analysisButton.setEnabled(true);
                final Map<String, MediaMatrix> metadata = get();
                tab.removeAll();
                tab.add("Tonality", new TonalityPanel(metadata.get("Tonality")));
                tab.addTab("Pitch", new PitchPanel(metadata.get("Pitch")));
                tab.addTab("Tempo", new TempoPanel(metadata.get("Tempo")));
                tab.addTab("Velocity", new VelocityPanel(metadata.get("Velocity")));
            } catch (InterruptedException | ExecutionException ex) {
                ErrorUtils.showDialog(ex, MusicMediaMatrixPanel.this);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jToolBar1 = new javax.swing.JToolBar();
        absoluteRadioButton = new javax.swing.JRadioButton();
        relativeRadioButton = new javax.swing.JRadioButton();
        ratioSpinner = new javax.swing.JSpinner();
        analysisButton = new javax.swing.JButton();
        tab = new javax.swing.JTabbedPane();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setRollover(true);

        buttonGroup1.add(absoluteRadioButton);
        absoluteRadioButton.setSelected(true);
        absoluteRadioButton.setText("Absolute");
        absoluteRadioButton.setFocusable(false);
        absoluteRadioButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        absoluteRadioButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jToolBar1.add(absoluteRadioButton);

        buttonGroup1.add(relativeRadioButton);
        relativeRadioButton.setText("Relative");
        relativeRadioButton.setFocusable(false);
        relativeRadioButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        relativeRadioButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jToolBar1.add(relativeRadioButton);

        ratioSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 500, 1));
        ratioSpinner.setMaximumSize(new java.awt.Dimension(50, 32767));
        jToolBar1.add(ratioSpinner);

        analysisButton.setText("Analyze");
        analysisButton.setEnabled(false);
        analysisButton.setFocusable(false);
        analysisButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        analysisButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        analysisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analysisButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(analysisButton);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);
        add(tab, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void analysisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analysisButtonActionPerformed
        open(file);
    }//GEN-LAST:event_analysisButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton absoluteRadioButton;
    private javax.swing.JButton analysisButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JSpinner ratioSpinner;
    private javax.swing.JRadioButton relativeRadioButton;
    private javax.swing.JTabbedPane tab;
    // End of variables declaration//GEN-END:variables

    class TonalityColorPanel extends JPanel {

        private static final long serialVersionUID = 631839112429739289L;

        public TonalityColorPanel() {
            super();
            setPreferredSize(new Dimension(400, 30));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (file != null) {
                try {
                    TonalMusic music = new TonalityAnalyzer().analyze(file, 30);
                    final Key[] keys = music.getKeys();
                    final Color[] colors = DefaultColorMap.SCRIABIN.visualize(keys);
                    int sum = 35;
                    for (Color color : colors) {
                        g.setColor(color);
                        final int pwidth = getWidth() / keys.length;
                        g.fillRect(sum, 0, pwidth, getHeight());
                        sum += pwidth;
                    }
                } catch (IOException | InvalidMidiDataException ex) {
                    ErrorUtils.showDialog(ex, MusicMediaMatrixPanel.this);
                }
            }
        }
    }
}
