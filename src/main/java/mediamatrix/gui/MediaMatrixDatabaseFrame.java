/*  MediaMatrix -- Programmable Multimedia Database System
 * Copyright (C) 2010 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
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

import mediamatrix.utils.FileNameUtilities;
import mediamatrix.db.FileOpenScriptGenerator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serial;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.TableColumnModel;
import mediamatrix.action.ColorMediaMatrixAction;
import mediamatrix.action.CopyAsOpenAction;
import mediamatrix.action.CopyPathAction;
import mediamatrix.action.CorrelationMatrixAction;
import mediamatrix.action.FrameAction;
import mediamatrix.action.InspectorAction;
import mediamatrix.action.MediaMatrixAction;
import mediamatrix.action.PlayAction;
import mediamatrix.action.PopupInvoker;
import mediamatrix.action.RollerAction;
import mediamatrix.db.CXMQLParser;
import mediamatrix.db.CXMQLParserFactory;
import mediamatrix.db.CXMQLResultSet;
import mediamatrix.db.CXMQLScript;
import mediamatrix.db.CXMQLVisualizeParser;
import mediamatrix.db.CXMQLVisualizeScript;
import mediamatrix.db.CorrelationMatrix;
import mediamatrix.db.MediaMatrix;
import mediamatrix.mvc.ImageTableCellRenderer;
import mediamatrix.utils.IOUtilities;

public final class MediaMatrixDatabaseFrame extends javax.swing.JFrame {

    @Serial
    private static final long serialVersionUID = -2615961218813612330L;

    private final QueryEditor editor;
    private final ExplorerPanel explorer;
    private final QueryProgressDialog dialog;

    public MediaMatrixDatabaseFrame() {
        initComponents();
        try {
            setIconImage(ImageIO.read(getClass().getResource("/mediamatrix/resources/Icon.png")));
        } catch (IOException ignored) {
        }

        editor = new QueryEditor();
        explorer = new ExplorerPanel(editor);
        qrSplitPane.setTopComponent(new JScrollPane(editor));
        mainSplitPane.setLeftComponent(explorer);
        dialog = new QueryProgressDialog(this);

        getContentPane().add(new StatusBar() {

            private static final long serialVersionUID = 8229103383556972292L;

            {
                final JLabel statusLabel = new JLabel("version " + Version.VERSION);
                final StatusBar.Constraint c1 = new StatusBar.Constraint();
                c1.setFixedWidth(100);
                final JButton gcButton = new JButton();
                final StatusBar.Constraint c2 = new StatusBar.Constraint();
                add(statusLabel, c1);
                add(gcButton, c2);
                gcButton.addActionListener((ActionEvent e) -> {
                    System.gc();
                });
                final Timer timer = new Timer(1000, (ActionEvent e) -> {
                    final DecimalFormat f1 = new DecimalFormat("#,###MB");
                    final DecimalFormat f2 = new DecimalFormat("##.#");
                    final long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
                    final long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
                    final long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
                    final long used = total - free;
                    final double ratio = (used * 100 / (double) total);
                    final String info = "Java Heap Total=" + f1.format(total) + ", " + "Used=" + f1.format(used) + " (" + f2.format(ratio) + "%), " + "Max=" + f1.format(max);
                    gcButton.setText(info);
                });
                timer.start();
            }
        }, BorderLayout.SOUTH);
        if (System.getProperty("os.name").contains("Mac")) {
            openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.META_DOWN_MASK));
            openQueryMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_DOWN_MASK));
            saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.META_DOWN_MASK));
            exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_DOWN_MASK));
            execMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.META_DOWN_MASK));
            csMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.META_DOWN_MASK));
        }
        loadPrefs();
    }

    public void executeScript(final String query) {
        dialog.init(100);
        new SwingWorker<MediaDatabaseTableModel, Double>() {

            private CXMQLScript script;

            @Override
            @SuppressWarnings("unchecked")
            public MediaDatabaseTableModel doInBackground() {
                MediaDatabaseTableModel model = null;
                try {
                    final CXMQLParserFactory factory = new CXMQLParserFactory();
                    final CXMQLParser parser = factory.getParser(query);
                    script = parser.parse(query);
                    script.addPropertyChangeListener(dialog);
                    final CXMQLResultSet result = script.eval();
                    dialog.propertyChange(new PropertyChangeEvent(this, "result", 0, 1));
                    model = new MediaDatabaseTableModel(result);
                    dialog.propertyChange(new PropertyChangeEvent(this, "complete", 1, 100));
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            ErrorUtils.showDialog(ex, MediaMatrixDatabaseFrame.this);
                            dialog.propertyChange(new PropertyChangeEvent(this, "exception", 0, 1));
                        }
                    });
                }
                return model;
            }

            @Override
            @SuppressWarnings("UseSpecificCatch")
            public void done() {
                try {
                    MediaDatabaseTableModel model = get();
                    JTable table = null;
                    if (model != null) {
                        if (model.getType().equalsIgnoreCase("Video")) {
                            table = new VideoDatabaseTable(model);
                        } else if (model.getType().equalsIgnoreCase("Music")) {
                            table = new MusicDatabaseTable(model);
                        } else if (model.getType().equalsIgnoreCase("Image")) {
                            table = new ImageDatabaseTable(model);
                        }
                    }
                    if (table != null) {
                        showResult(new QueryResultPanel(table, script.getVars()), "CXMQL Query Result");
                    }
                } catch (Exception ex) {
                    dialog.propertyChange(new PropertyChangeEvent(this, "exception", 0, 1));
                    ErrorUtils.showDialog(ex, MediaMatrixDatabaseFrame.this);
                }
            }
        }.execute();
    }

    public void executeVisualizeScript(final String query) {
        editor.setText(query);
        new SwingWorker<MediaMatrix, Double>() {

            private CXMQLVisualizeScript script;

            @Override
            protected MediaMatrix doInBackground() throws Exception {
                MediaMatrix result = null;
                try {
                    final CXMQLVisualizeParser parser = new CXMQLVisualizeParser();
                    script = parser.parse(query);
                    result = script.eval();
                    if (result == null) {
                        System.err.println(String.format("CXMQLVisualizeParser Error: result is null: %s\n", query));
                        System.err.println(script.dumpContextIds());
                    }
                    
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        ErrorUtils.showDialog(ex, MediaMatrixDatabaseFrame.this);
                    });
                    return null;
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    final MediaMatrix mat = get();
                    if ("SEPARATE".equalsIgnoreCase(script.getProperty("WINDOW"))) {
                        DialogUtils.showDialog(script.getTarget() + " - MediaMatrix Visualizer", new VideoMediaMatrixVisualizerPanel(mat, script), MediaMatrixDatabaseFrame.this);
                    } else {
                        if (script.getType().equalsIgnoreCase("VIDEO")) {
                            showResult(new VideoMediaMatrixVisualizerPanel(mat, script), script.getTarget());
                        } else if (script.getType().equalsIgnoreCase("MUSIC")) {
                            if (script.getMatrixName().equalsIgnoreCase("Pitch")) {
                                showResult(new PitchPanel(mat), script.getTarget());
                            } else if (script.getMatrixName().equalsIgnoreCase("Tonality")) {
                                showResult(new TonalityPanel(mat), script.getTarget());
                            } else if (script.getMatrixName().equalsIgnoreCase("Tempo")) {
                                showResult(new TempoPanel(mat), script.getTarget());
                            }
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ErrorUtils.showDialog(ex, MediaMatrixDatabaseFrame.this);
                }
            }
        }.execute();
    }

    private void loadPrefs() {
        final Preferences prefs = Preferences.userNodeForPackage(getClass());
        final String dbListStr = prefs.get("database", "");
        if (dbListStr.length() > 0) {
            explorer.setDatabase(dbListStr.split(";"));
        }
        mainSplitPane.setDividerLocation(prefs.getInt("divider", 120));
        qrSplitPane.setDividerLocation(prefs.getInt("qrdivider", 200));
        DialogUtils.loadWindowSize(this, 800, 600);
    }

    public void storePrefs() {
        final StringBuffer buff = new StringBuffer();
        final String[] dirs = explorer.getDatabase();
        for (int i = 0; i < dirs.length; i++) {
            buff.append(dirs[i]);
            if (i + 1 < dirs.length) {
                buff.append(";");
            }
        }
        final Preferences prefs = Preferences.userNodeForPackage(getClass());
        DialogUtils.saveWindowSize(this);
        prefs.putInt("divider", mainSplitPane.getDividerLocation());
        prefs.putInt("qrdivider", qrSplitPane.getDividerLocation());
        prefs.put("database", buff.toString());
    }

    public void showResult(JComponent table, String query) {
        aTabbedPane.add(query, table);
        aTabbedPane.setSelectedIndex(aTabbedPane.getTabCount() - 1);
        final JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(5, 5));
        final JLabel label = new JLabel(query);
        panel.add(label, BorderLayout.CENTER);
        ImageIcon icon = new ImageIcon(getClass().getResource("/mediamatrix/resources/cross.png"));
        final JLabel closeButton = new JLabel(icon);
        panel.add(closeButton, BorderLayout.EAST);
        closeButton.setBorder(null);
        final Color defaultBG = closeButton.getBackground();
        aTabbedPane.setTabComponentAt(aTabbedPane.getTabCount() - 1, panel);
        closeButton.setOpaque(false);
        closeButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Component tabComp = ((Component) e.getSource()).getParent();
                int index = aTabbedPane.indexOfTabComponent(tabComp);
                aTabbedPane.remove(index);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setOpaque(true);
                closeButton.setBackground(Color.white);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setOpaque(false);
                closeButton.setBackground(defaultBG);
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        aToolBar = new javax.swing.JToolBar();
        mainSplitPane = new javax.swing.JSplitPane();
        qrSplitPane = new javax.swing.JSplitPane();
        aTabbedPane = new javax.swing.JTabbedPane();
        aMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        reindexMenuItem = new javax.swing.JMenuItem();
        openQueryMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        toolMenu = new javax.swing.JMenu();
        execMenuItem = new javax.swing.JMenuItem();
        csMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        javaVMMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        fileChooser.setDialogTitle("Open Database Directory");
        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("MediaMatrix");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        aToolBar.setFloatable(false);
        aToolBar.setRollover(true);
        aToolBar.setOpaque(false);
        getContentPane().add(aToolBar, java.awt.BorderLayout.NORTH);

        mainSplitPane.setContinuousLayout(true);

        qrSplitPane.setDividerLocation(200);
        qrSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        qrSplitPane.setContinuousLayout(true);

        aTabbedPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        qrSplitPane.setRightComponent(aTabbedPane);

        mainSplitPane.setRightComponent(qrSplitPane);

        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        openMenuItem.setMnemonic('O');
        openMenuItem.setText("Open Data");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        reindexMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        reindexMenuItem.setText("Delta Indexing");
        reindexMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reindexMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(reindexMenuItem);

        openQueryMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        openQueryMenuItem.setMnemonic('C');
        openQueryMenuItem.setText("Open CXMQL");
        openQueryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openQueryMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openQueryMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText("Save CXMQL");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        aMenuBar.add(fileMenu);

        toolMenu.setMnemonic('T');
        toolMenu.setText("Tool");

        execMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        execMenuItem.setMnemonic('E');
        execMenuItem.setText("Execute Query");
        execMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                execButtonActionPerformed(evt);
            }
        });
        toolMenu.add(execMenuItem);

        csMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        csMenuItem.setMnemonic('S');
        csMenuItem.setText("Color Scheme");
        csMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csButtonActionPerformed(evt);
            }
        });
        toolMenu.add(csMenuItem);

        aMenuBar.add(toolMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");

        javaVMMenuItem.setMnemonic('J');
        javaVMMenuItem.setText("Java VM Memory Usage");
        javaVMMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaVMMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(javaVMMenuItem);

        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About MediaMatrix");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        aMenuBar.add(helpMenu);

        setJMenuBar(aMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().isDirectory()) {
                explorer.addDatabase(fileChooser.getSelectedFile().getAbsolutePath());
                editor.setText(new FileOpenScriptGenerator().generate(fileChooser.getSelectedFile()));
            } else {
                editor.setText(new FileOpenScriptGenerator().generate(fileChooser.getSelectedFiles()));
            }
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        new AboutDialog(this, true).setVisible(true);
    }//GEN-LAST:event_aboutButtonActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        dispose();
        System.exit(0);
    }//GEN-LAST:event_formWindowClosed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        final String name = JOptionPane.showInputDialog(this, "Input a name of query", explorer.getCurrentQueryName());
        if (name != null) {
            final File dbDir = FileNameUtilities.getApplicationSubDirectory("CXMQL");
            try {
                final OutputStream out = new FileOutputStream(new File(dbDir, name + ".cxmql"));
                final OutputStreamWriter writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
                writer.write(editor.getText(), 0, editor.getText().length());
                explorer.addQuery(name);
                writer.close();
            } catch (Exception ex) {
                ErrorUtils.showDialog(ex, this);
            }
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void javaVMMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaVMMenuItemActionPerformed
        DialogUtils.showJVMMemoryStatDialog(this);
    }//GEN-LAST:event_javaVMMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        storePrefs();
    }//GEN-LAST:event_formWindowClosing

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        storePrefs();
        dispose();
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void csButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csButtonActionPerformed
        showResult(new ColorSchemePanel(), "Color Scheme Impression");
}//GEN-LAST:event_csButtonActionPerformed

    private void execButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_execButtonActionPerformed
        final String query = editor.getText();
        if (query.contains(CXMQLVisualizeParser.VISUALIZE_BY)) {
            executeVisualizeScript(query);
        } else {
            executeScript(query);
        }
}//GEN-LAST:event_execButtonActionPerformed

    private void openQueryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openQueryMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_openQueryMenuItemActionPerformed

    private void reindexMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reindexMenuItemActionPerformed
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().isDirectory()) {
                editor.setText(new FileOpenScriptGenerator().delta(fileChooser.getSelectedFile()));
            }
        }
    }//GEN-LAST:event_reindexMenuItemActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar aMenuBar;
    private javax.swing.JTabbedPane aTabbedPane;
    private javax.swing.JToolBar aToolBar;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem csMenuItem;
    private javax.swing.JMenuItem execMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem javaVMMenuItem;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem openQueryMenuItem;
    private javax.swing.JSplitPane qrSplitPane;
    private javax.swing.JMenuItem reindexMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenu toolMenu;
    // End of variables declaration//GEN-END:variables

    class QueryResultPanel extends JPanel {

        private static final long serialVersionUID = -1801889169157214469L;

        public QueryResultPanel(final JTable table, final Map<String, Object> vars) {
            super(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder());
            final JToolBar toolbar = new JToolBar();
            final JButton button = new JButton("Show Variable:");
            button.setIcon(new ImageIcon(getClass().getResource("/mediamatrix/resources/Find.png")));
            button.setOpaque(false);
            final List<String> vec = new ArrayList<>();
            final Set<String> keys = vars.keySet();
            for (final Iterator<String> it = keys.iterator(); it.hasNext();) {
                final String key = it.next();
                if (!key.startsWith("EACH") && !key.equals("pe")) {
                    vec.add(key);
                }
            }
            final JComboBox<String> box = new JComboBox<>(vec.toArray(String[]::new));
            toolbar.add(button);
            toolbar.add(box);
            toolbar.setOpaque(false);
            add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
            button.addActionListener((ActionEvent e) -> {
                if (box.getSelectedIndex() > -1) {
                    final String key = (String) box.getSelectedItem();
                    final Object obj = vars.get(key);
                    switch (obj) {
                        case MediaMatrix mediaMatrix -> DialogUtils.showDialog(key, new MediaMatrixSplineGraphPanel(mediaMatrix), QueryResultPanel.this);
                        case CorrelationMatrix correlationMatrix -> DialogUtils.showDialog(key, new CorrelationMatrixPanel(correlationMatrix), QueryResultPanel.this);
                        default -> {
                        }
                    }
                }
            });
            add(toolbar, BorderLayout.NORTH);
        }
    }

    class VideoDatabaseTable extends JTable {

        private static final long serialVersionUID = -5223630977672061873L;

        public VideoDatabaseTable(final MediaDatabaseTableModel model) {
            super(model);
            setDoubleBuffered(true);
            final JPopupMenu popup = new JPopupMenu();
            final TableColumnModel cmodel = getColumnModel();
            setDefaultRenderer(Image.class, new ImageTableCellRenderer());
            setRowHeight(50);
            setRowSelectionAllowed(true);
            cmodel.getColumn(0).setPreferredWidth(505);
            cmodel.getColumn(0).setWidth(505);
            cmodel.getColumn(0).setMaxWidth(505);
            cmodel.getColumn(2).setPreferredWidth(100);
            cmodel.getColumn(2).setWidth(100);
            cmodel.getColumn(2).setMaxWidth(100);
            final PopupInvoker popupInvoker = new PopupInvoker(popup, this);
            addMouseListener(popupInvoker);
            addKeyListener(popupInvoker);

            final JMenu script = new JMenu("Visualization Script");
            script.setMnemonic('S');
            for (File file : FileNameUtilities.getFilesInApplicationSubDirectory("CXMQL")) {
                final String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                if (name.endsWith("_VIZ") || name.endsWith("_viz")) {
                    try {
                        final String scriptString = IOUtilities.readString(file, "UTF-8");
                        final CXMQLVisualizeParser parser = new CXMQLVisualizeParser();
                        final CXMQLVisualizeScript scriptObject = parser.parse(scriptString);
                        if (scriptObject.getType().equalsIgnoreCase("VIDEO")) {
                            final JMenuItem item = new JMenuItem(name);
                            item.addActionListener((ActionEvent e) -> {
                                int rows[] = getSelectedRows();
                                if (rows.length > 0) {
                                    final File file1 = model.getAsFile(rows[0]);
                                    final String query = scriptString.replace("$FILE", file1.getAbsolutePath());
                                    executeVisualizeScript(query);
                                }
                            });
                            script.add(item);
                        }
                    } catch (IOException ex) {
                        ErrorUtils.showDialog(ex, MediaMatrixDatabaseFrame.this);
                    }
                }
            }
            final JMenuItem copy = new JMenuItem(new CopyAsOpenAction(this, model));
            copy.setMnemonic('C');
            final JMenuItem showCorrelationMatrix = new JMenuItem(new CorrelationMatrixAction(this, model));
            showCorrelationMatrix.setMnemonic('E');
            final JMenuItem showFrames = new JMenuItem(new FrameAction(this, model));
            showFrames.setMnemonic('F');
            final JMenuItem showMatrix = new JMenuItem(new MediaMatrixAction(this, model));
            showMatrix.setMnemonic('M');
            final JMenuItem showCMatrix = new JMenuItem(new ColorMediaMatrixAction(this, model));
            showCMatrix.setMnemonic('T');
            final JMenuItem showRoller = new JMenuItem(new RollerAction(this, model));
            showRoller.setMnemonic('R');
            final JMenuItem play = new JMenuItem(new PlayAction(this, model));
            play.setMnemonic('P');
            final JMenuItem copyPath = new JMenuItem(new CopyPathAction(this, model));
            copyPath.setMnemonic('y');

            popup.add(play);
            popup.addSeparator();
            popup.add(script);
            popup.addSeparator();
            popup.add(showRoller);
            popup.add(showFrames);
            popup.add(showMatrix);
            popup.add(showCMatrix);
            popup.add(showCorrelationMatrix);
            popup.addSeparator();
            popup.add(copy);
            popup.add(copyPath);
        }
    }

    class MusicDatabaseTable extends JTable {

        private static final long serialVersionUID = 1L;

        public MusicDatabaseTable(final MediaDatabaseTableModel model) {
            super(model);
            setDoubleBuffered(true);
            final JPopupMenu popup = new JPopupMenu();
            final TableColumnModel cmodel = getColumnModel();
            setDefaultRenderer(Image.class, new ImageTableCellRenderer());
            setRowHeight(15);
            cmodel.getColumn(0).setPreferredWidth(300);
            cmodel.getColumn(0).setWidth(300);
            cmodel.getColumn(0).setMaxWidth(300);
            cmodel.getColumn(2).setPreferredWidth(100);
            cmodel.getColumn(2).setWidth(100);
            cmodel.getColumn(2).setMaxWidth(100);
            addMouseListener(new PopupInvoker(popup, this));

            final JMenu script = new JMenu("Visualization Script");
            script.setMnemonic('S');
            for (File file : FileNameUtilities.getFilesInApplicationSubDirectory("CXMQL")) {
                final String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                if (name.endsWith("_VIZ") || name.endsWith("_viz")) {
                    try {
                        final String scriptString = IOUtilities.readString(file, "UTF-8");
                        final CXMQLVisualizeParser parser = new CXMQLVisualizeParser();
                        final CXMQLVisualizeScript scriptObject = parser.parse(scriptString);
                        if (scriptObject.getType().equalsIgnoreCase("MUSIC")) {
                            final JMenuItem item = new JMenuItem(name);
                            item.addActionListener((ActionEvent e) -> {
                                int rows[] = getSelectedRows();
                                if (rows.length > 0) {
                                    final File file1 = model.getAsFile(rows[0]);
                                    final String query = scriptString.replace("$FILE", file1.getAbsolutePath());
                                    executeVisualizeScript(query);
                                }
                            });
                            script.add(item);
                        }
                    } catch (IOException ex) {
                        ErrorUtils.showDialog(ex, MediaMatrixDatabaseFrame.this);
                    }
                }
            }

            final JMenuItem openInspector = new JMenuItem(new InspectorAction(this, model));
            openInspector.setMnemonic('I');
            final JMenuItem copy = new JMenuItem(new CopyAsOpenAction(this, model));
            copy.setMnemonic('C');
            final JMenuItem showCorrelationMatrix = new JMenuItem(new CorrelationMatrixAction(this, model));
            showCorrelationMatrix.setMnemonic('E');
            final JMenuItem play = new JMenuItem(new PlayAction(this, model));
            play.setMnemonic('P');

            popup.add(play);
            popup.addSeparator();
            popup.add(script);
            popup.addSeparator();
            popup.add(openInspector);
            popup.add(showCorrelationMatrix);
            popup.addSeparator();
            popup.add(copy);
        }
    }

    class ImageDatabaseTable extends JTable {

        private static final long serialVersionUID = 1L;

        public ImageDatabaseTable(final MediaDatabaseTableModel model) {
            super(model);
            setDoubleBuffered(true);
            setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            final JPopupMenu popup = new JPopupMenu();
            final TableColumnModel cmodel = getColumnModel();
            setDefaultRenderer(Image.class, new ImageTableCellRenderer());
            setRowHeight(50);
            cmodel.getColumn(0).setPreferredWidth(100);
            cmodel.getColumn(0).setWidth(100);
            cmodel.getColumn(0).setMaxWidth(100);
            cmodel.getColumn(2).setPreferredWidth(100);
            cmodel.getColumn(2).setWidth(100);
            cmodel.getColumn(2).setMaxWidth(100);
            addMouseListener(new PopupInvoker(popup, this));
            final JMenuItem openInspector = new JMenuItem(new InspectorAction(this, model));
            openInspector.setMnemonic('I');
            final JMenuItem copy = new JMenuItem(new CopyAsOpenAction(this, model));
            copy.setMnemonic('C');
            final JMenuItem copyPath = new JMenuItem(new CopyPathAction(this, model));
            copyPath.setMnemonic('y');
            popup.add(openInspector);
            popup.addSeparator();
            popup.add(copy);
            popup.add(copyPath);
        }
    }
}
