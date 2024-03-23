package mediamatrix.gui;

import java.awt.Component;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

public final class StackableSplitPane extends JSplitPane {

    @Serial
    private static final long serialVersionUID = 1L;

    public StackableSplitPane() {
        super(JSplitPane.HORIZONTAL_SPLIT, true);
        setBorder(BorderFactory.createEmptyBorder());
        setLeftComponent(new JLabel());
        setRightComponent(null);
    }

    public boolean hasNextComponent(JComponent comp) {
        JSplitPane pane = findComponent(this, comp);
        if (pane != null && pane.getRightComponent() instanceof JSplitPane) {
            JSplitPane childPane = (JSplitPane) pane.getRightComponent();
            if (childPane.getLeftComponent() != null) {
                if (childPane.getLeftComponent().getClass().equals(comp.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addComponentColumn(JComponent comp) {
        final JSplitPane pane = findEmptyPane(this);
        if (pane == this && !pane.getLeftComponent().getClass().equals(comp.getClass())) {
            pane.setLeftComponent(comp);
        } else {
            final JSplitPane newPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, comp, new JLabel());
            newPane.setDoubleBuffered(true);
            newPane.setBorder(null);
            newPane.setDividerLocation((int) comp.getPreferredSize().getWidth());
            pane.setRightComponent(newPane);
        }
    }

    public void removeComponentColumn(JComponent comp) {
        if (!hasNextComponent(comp)) {
            return;
        }
        final JSplitPane pane = findComponent(this, comp);
        final JSplitPane parentPane = findParentColumn(this, pane);
        parentPane.setRightComponent(pane.getRightComponent());
    }

    public JComponent[] getManagedComponents() {
        return getResursivelyComponents(this, new ArrayList<>());
    }

    private JSplitPane findComponent(JSplitPane pane, JComponent comp) {
        if (pane.getLeftComponent() == comp) {
            return pane;
        } else if (pane.getRightComponent() instanceof JSplitPane jSplitPane) {
            return findComponent(jSplitPane, comp);
        }
        return null;
    }

    private JSplitPane findParentColumn(JSplitPane parentPane, JSplitPane pane) {
        Component next = parentPane.getRightComponent();
        if (next == pane) {
            return parentPane;
        } else if (parentPane == pane) {
            return pane;
        } else if (next instanceof JSplitPane jSplitPane) {
            return findParentColumn(jSplitPane, pane);
        }
        return null;
    }

    private JSplitPane findEmptyPane(JSplitPane pane) {
        if (pane.getRightComponent() instanceof JSplitPane jSplitPane) {
            return findEmptyPane(jSplitPane);
        } else {
            return pane;
        }
    }

    private JComponent[] getResursivelyComponents(JSplitPane pane, List<JComponent> list) {
        JComponent comp = (JComponent) pane.getLeftComponent();
        if (comp != null) {
            list.add(comp);
        }
        if (pane.getRightComponent() instanceof JSplitPane jSplitPane) {
            return getResursivelyComponents(jSplitPane, list);
        } else {
            return list.toArray(JComponent[]::new);
        }
    }
}
