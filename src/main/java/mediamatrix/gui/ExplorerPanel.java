package mediamatrix.gui;

import mediamatrix.db.FileOpenScriptGenerator;
import mediamatrix.utils.FileNameUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import mediamatrix.utils.IOUtilities;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public class ExplorerPanel extends JScrollPane {

    private static final long serialVersionUID = -4063699836849593357L;
    private final JPopupMenu dbPopup = new JPopupMenu("File List");
    private final JMenuItem deleteItem = new JMenuItem("Delete");
    private final JPopupMenu queryPopup = new JPopupMenu("File List");
    private final JMenuItem deleteQueryItem = new JMenuItem("Delete");
    private final JList<File> dbList;
    private final JList<String> queryList;
    private final JList<String> vizList;
    private final DefaultListModel<File> dbModel = new DefaultListModel<File>();
    private final DefaultListModel<String> queryModel = new DefaultListModel<String>();
    private final DefaultListModel<String> vizModel = new DefaultListModel<String>();
    private String currentQueryName;

    public ExplorerPanel(final QueryEditor editor) {
        super();
        final JXTaskPaneContainer taskPane = new JXTaskPaneContainer();
        setViewportView(taskPane);
        final JXTaskPane dbGroup = new JXTaskPane();
        final JXTaskPane queryGroup = new JXTaskPane();
        final JXTaskPane vizGroup = new JXTaskPane();
        dbGroup.setTitle("Database");
        queryGroup.setTitle("Query");
        vizGroup.setTitle("VizScript");

        for (File file : FileNameUtilities.getFilesInApplicationSubDirectory("CXMQL")) {
            final String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
            if (name.endsWith("_VIZ")) {
                vizModel.addElement(name);
            } else {
                queryModel.addElement(name);
            }
        }
        vizList = new JList<String>(vizModel);
        dbList = new JList<File>(dbModel);
        queryList = new JList<String>(queryModel);
        dbGroup.add(dbList);
        queryGroup.add(queryList);
        vizGroup.add(vizList);
        taskPane.add((JComponent) dbGroup);
        taskPane.add((JComponent) queryGroup);
        taskPane.add((JComponent) vizGroup);
        vizGroup.setCollapsed(true);

        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final int index = dbList.getSelectedIndex();
                if (index > -1) {
                    dbModel.remove(index);
                }
            }
        });
        dbPopup.add(deleteItem);

        deleteQueryItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = null;
                if (queryList.getSelectedIndex() > -1) {
                    filename = queryModel.get(queryList.getSelectedIndex()) + ".cxmql";
                    queryModel.remove(queryList.getSelectedIndex());
                } else if (vizList.getSelectedIndex() > -1) {
                    filename = vizModel.get(vizList.getSelectedIndex()) + ".cxmql";
                    vizModel.remove(vizList.getSelectedIndex());
                }

                final File file = new File(FileNameUtilities.getApplicationSubDirectory("CXMQL"), filename);
                if (file.exists()) {
                    file.delete();
                }
            }
        });
        queryPopup.add(deleteQueryItem);

        dbList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final File file = dbModel.getElementAt(dbList.locationToIndex(e.getPoint()));
                    if (file.exists()) {
                        editor.setText(new FileOpenScriptGenerator().generate(file));
                    } else {
                        DialogUtils.showDialog("Error", new JLabel(file.getAbsolutePath() + " does not exist."), dbList);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    dbPopup.show(dbList, e.getX(), e.getY());
                }
            }
        });

        queryList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    currentQueryName = queryModel.get(queryList.locationToIndex(e.getPoint())).toString();
                    final File file = new File(FileNameUtilities.getApplicationSubDirectory("CXMQL"), currentQueryName + ".cxmql");
                    try {
                        editor.setText(IOUtilities.readString(file, "UTF-8"));
                    } catch (IOException ex) {
                        ErrorUtils.showDialog(ex, ExplorerPanel.this);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    queryPopup.show(queryList, e.getX(), e.getY());
                }
            }
        });

        vizList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final File file = new File(FileNameUtilities.getApplicationSubDirectory("CXMQL"), vizModel.get(vizList.locationToIndex(e.getPoint())).toString() + ".cxmql");
                    try {
                        editor.setText(IOUtilities.readString(file, "UTF-8"));
                    } catch (IOException ex) {
                        ErrorUtils.showDialog(ex, ExplorerPanel.this);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    queryPopup.show(vizList, e.getX(), e.getY());
                }
            }
        });

    }

    public void setDatabase(String[] dirs) {
        for (String name : dirs) {
            dbModel.addElement(new File(name));
        }
    }

    public void addDatabase(String path) {
        File file = new File(path);
        if (!dbModel.contains(file)) {
            dbModel.addElement(file);
        }
    }

    public void addQuery(String name) {
        if (name.endsWith("_VIZ")) {
            if (!vizModel.contains(name)) {
                vizModel.addElement(name);
            }
        } else {
            if (!queryModel.contains(name)) {
                queryModel.addElement(name);
            }
        }
    }

    public String[] getDatabase() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < dbModel.size(); i++) {
            list.add(dbModel.get(i).toString());
        }
        return list.toArray(new String[list.size()]);
    }

    public String getCurrentQueryName() {
        return currentQueryName;
    }
}
