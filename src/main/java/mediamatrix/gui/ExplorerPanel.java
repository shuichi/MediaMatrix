package mediamatrix.gui;

import mediamatrix.db.FileOpenScriptGenerator;
import mediamatrix.utils.FileNameUtilities;
import mediamatrix.utils.IOUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class ExplorerPanel extends JScrollPane {

    @Serial
    private static final long serialVersionUID = -4063699836849593357L;

    private final JPopupMenu popup = new JPopupMenu("Tree");
    private final JMenuItem deleteItem = new JMenuItem("Delete");
    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Explorer");
    private final DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode("Database");
    private final DefaultMutableTreeNode queryNode = new DefaultMutableTreeNode("Query");
    private final DefaultMutableTreeNode vizNode = new DefaultMutableTreeNode("VizScript");
    private String currentQueryName;

    public ExplorerPanel(final QueryEditor editor) {
        super();
        rootNode.add(dbNode);
        rootNode.add(queryNode);
        rootNode.add(vizNode);
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        setViewportView(tree);

        for (File file : FileNameUtilities.getFilesInApplicationSubDirectory("CXMQL")) {
            final String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
            if (name.endsWith("_VIZ")) {
                addNodeIfAbsent(vizNode, name);
            } else {
                addNodeIfAbsent(queryNode, name);
            }
        }

        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedNode();
            }
        });
        popup.add(deleteItem);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick(editor, e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopupIfNeeded(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopupIfNeeded(e);
            }
        });

        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                    final DefaultMutableTreeNode selected = getSelectedNode();
                    if (selected != null && selected.getParent() == dbNode) {
                        treeModel.removeNodeFromParent(selected);
                    }
                }
            }
        });

        tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                final Object node = event.getPath().getLastPathComponent();
                if (node == rootNode) {
                    tree.expandPath(event.getPath());
                }
            }
        });

        tree.expandPath(new TreePath(dbNode.getPath()));
        tree.expandPath(new TreePath(queryNode.getPath()));
    }

    public void setDatabase(String[] dirs) {
        for (String name : dirs) {
            dbNode.add(new DefaultMutableTreeNode(new File(name)));
        }
        treeModel.reload(dbNode);
    }

    public void addDatabase(String path) {
        final File file = new File(path);
        if (!containsUserObject(dbNode, file)) {
            treeModel.insertNodeInto(new DefaultMutableTreeNode(file), dbNode, dbNode.getChildCount());
        }
    }

    public void addQuery(String name) {
        if (name.endsWith("_VIZ")) {
            addNodeIfAbsent(vizNode, name);
        } else {
            addNodeIfAbsent(queryNode, name);
        }
    }

    public String[] getDatabase() {
        final List<String> list = new ArrayList<String>();
        final Enumeration<?> children = dbNode.children();
        while (children.hasMoreElements()) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
            list.add(node.getUserObject().toString());
        }
        return list.toArray(new String[list.size()]);
    }

    public String getCurrentQueryName() {
        return currentQueryName;
    }

    private void handleDoubleClick(QueryEditor editor, MouseEvent e) {
        final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent == null || parent == rootNode) {
            return;
        }
        if (parent == dbNode) {
            final File file = (File) node.getUserObject();
            if (file.exists()) {
                editor.setText(new FileOpenScriptGenerator().generate(file));
            } else {
                DialogUtils.showDialog("Error", new JLabel(file.getAbsolutePath() + " does not exist."), tree);
            }
        } else if (parent == queryNode) {
            currentQueryName = node.getUserObject().toString();
            final File file = new File(FileNameUtilities.getApplicationSubDirectory("CXMQL"), currentQueryName + ".cxmql");
            try {
                editor.setText(IOUtilities.readString(file, "UTF-8"));
            } catch (IOException ex) {
                ErrorUtils.showDialog(ex, this);
            }
        } else if (parent == vizNode) {
            final File file = new File(FileNameUtilities.getApplicationSubDirectory("CXMQL"), node.getUserObject().toString() + ".cxmql");
            try {
                editor.setText(IOUtilities.readString(file, "UTF-8"));
            } catch (IOException ex) {
                ErrorUtils.showDialog(ex, this);
            }
        }
    }

    private void showPopupIfNeeded(MouseEvent e) {
        if (!e.isPopupTrigger() && !SwingUtilities.isRightMouseButton(e)) {
            return;
        }
        final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        tree.setSelectionPath(path);
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent == dbNode || parent == queryNode || parent == vizNode) {
            popup.show(tree, e.getX(), e.getY());
        }
    }

    private void deleteSelectedNode() {
        final DefaultMutableTreeNode selected = getSelectedNode();
        if (selected == null) {
            return;
        }
        final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selected.getParent();
        if (parent == null || parent == rootNode) {
            return;
        }
        if (parent == queryNode || parent == vizNode) {
            final String filename = selected.getUserObject().toString() + ".cxmql";
            final File file = new File(FileNameUtilities.getApplicationSubDirectory("CXMQL"), filename);
            if (file.exists()) {
                file.delete();
            }
        }
        treeModel.removeNodeFromParent(selected);
    }

    private DefaultMutableTreeNode getSelectedNode() {
        final TreePath selectedPath = tree.getSelectionPath();
        if (selectedPath == null) {
            return null;
        }
        return (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
    }

    private boolean containsUserObject(DefaultMutableTreeNode parent, Object value) {
        final Enumeration<?> children = parent.children();
        while (children.hasMoreElements()) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
            if (value.equals(node.getUserObject())) {
                return true;
            }
        }
        return false;
    }

    private void addNodeIfAbsent(DefaultMutableTreeNode parent, String value) {
        if (!containsUserObject(parent, value)) {
            treeModel.insertNodeInto(new DefaultMutableTreeNode(value), parent, parent.getChildCount());
        }
    }
}
