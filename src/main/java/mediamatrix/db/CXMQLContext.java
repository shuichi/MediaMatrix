package mediamatrix.db;

import java.util.TreeMap;
import org.apache.commons.jexl3.JexlContext;

public class CXMQLContext extends TreeMap<String, Object> implements JexlContext {

    private static final long serialVersionUID = 8811777801086086457L;

    public CXMQLContext() {
        super();
    }

    @Override
    public boolean has(String name) {
        return containsKey(name);
    }

    @Override
    public void set(String name, Object value) {
        put(name, value);
    }

    @Override
    public Object get(String name) {
        return super.get(name);
    }
}
