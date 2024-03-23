package mediamatrix.mvc;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

public final class PropertiesTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private final static Class<?>[] RETURN_TYPES = {Boolean.TYPE, Boolean.class, Integer.TYPE, Integer.class, String.class, InetAddress.class};
    private final static String[] COL_NAMES = {"Parameter", "Value"};
    protected Properties mTableProp;
    private boolean isReadOnly;
    /**
     * All properties key.
     */
    protected Vector<String> mTableKeys;

    /**
     * Create this <code>TableModel</code> with empty properties.
     */
    public PropertiesTableModel() {
        this(new Properties());
        isReadOnly = false;
    }

    /**
     * Create this <code>TableModel</code>.
     * @param prop
     */
    public PropertiesTableModel(Properties prop) {
        mTableProp = prop;
        mTableKeys = new Vector<>();
        initializeTable();
        isReadOnly = false;
    }

    public PropertiesTableModel(Properties prop, boolean flag) {
        this(prop);
        isReadOnly = flag;
    }

    /**
     * Initialize table - populate vector.
     */
    protected void initializeTable() {
        mTableKeys.clear();
        Enumeration<?> keyNames = mTableProp.propertyNames();
        while (keyNames.hasMoreElements()) {
            String key = keyNames.nextElement().toString();
            if (key.trim().equals("")) {
                continue;
            }
            mTableKeys.add(key);
        }
        Collections.sort(mTableKeys);
    }

    /**
     * Get column class - always string.
     */
    @Override
    public Class<String> getColumnClass(int index) {
        return String.class;
    }

    /**
     * Get column count.
     */
    @Override
    public int getColumnCount() {
        return COL_NAMES.length;
    }

    /**
     * Get column name.
     * @param index
     */
    @Override
    public String getColumnName(int index) {
        return COL_NAMES[index];
    }

    /**
     * Get row count.
     */
    @Override
    public int getRowCount() {
        return mTableKeys.size();
    }

    /**
     * Get value at.
     */
    @Override
    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return mTableKeys.get(row);
        } else {
            return mTableProp.getProperty(mTableKeys.get(row).toString());
        }
    }

    /**
     * Is cell editable - currently true.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return !isReadOnly;
    }

    /**
     * Set value at - does not set value - dummy metod.
     */
    @Override
    public void setValueAt(Object val, int row, int col) {
    }

    /**
     * Find column index.
     */
    @Override
    public int findColumn(String columnName) {
        int index = -1;
        for (int i = COL_NAMES.length; --i >= 0;) {
            if (COL_NAMES[i].equals(columnName)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * Reload properties.
     */
    public void reload() {
        initializeTable();
        fireTableDataChanged();
    }

    /**
     * Reload a new properties object.
     */
    public void reload(Properties prop) {
        mTableProp = prop;
        initializeTable();
        fireTableDataChanged();
    }

    /**
     * Create configuration table model.
     */
    public static PropertiesTableModel getTableModel(Object obj)
            throws Exception {
        Properties prop = new Properties();
        Class<?> clazz = obj.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        PropertyDescriptor descriptors[] = beanInfo.getPropertyDescriptors();

        if (descriptors != null) {
            for (int i = 0; i < descriptors.length; ++i) {
                Method met = descriptors[i].getReadMethod();
                if (met != null) {
                    Class<?> retType = met.getReturnType();
                    boolean isDisplayable = false;
                    for (int j = 0; j < RETURN_TYPES.length; ++j) {
                        if (RETURN_TYPES[j].equals(retType)) {
                            isDisplayable = true;
                            break;
                        }
                    }

                    if (!isDisplayable) {
                        continue;
                    }

                    Object valObj = null;
                    try {
                        valObj = met.invoke(obj, new Object[0]);
                    } catch (InvocationTargetException ex) {
                        Throwable th = ex.getCause();
                        if (th instanceof Exception) {
                            throw (Exception) th;
                        } else if (th instanceof Error) {
                            throw (Error) th;
                        }
                    }

                    String val = "";
                    if (valObj != null) {
                        val = String.valueOf(valObj);
                    }

                    String name = descriptors[i].getName();
                    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                    prop.setProperty(name, val);
                }
            }
        }
        return new PropertiesTableModel(prop);
    }
}
