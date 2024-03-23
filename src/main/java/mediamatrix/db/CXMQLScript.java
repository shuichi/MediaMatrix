package mediamatrix.db;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;

@SuppressWarnings("this-escape")
public abstract class CXMQLScript {

    private final PropertyChangeSupport propertyChangeSupport;

    public CXMQLScript() {
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public abstract String getType();

    public abstract Map<String, Object> getVars();

    public abstract CXMQLResultSet eval() throws Exception;

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().removePropertyChangeListener(l);
    }
}
