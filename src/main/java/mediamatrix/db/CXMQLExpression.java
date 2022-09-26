package mediamatrix.db;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;

public class CXMQLExpression {

    private final String name;
    private final String expression;
    private final JexlEngine engine;

    public CXMQLExpression(JexlEngine engine, String name, String expression) {
        this.engine = engine;
        this.name = name;
        this.expression = expression;
    }

    @SuppressWarnings("unchecked")
    public void eval(JexlContext context) throws Exception {
        context.set(name, engine.createExpression("pe." + expression).evaluate(context));
    }

    public String getExpression() {
        return expression;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return expression + "->" + name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CXMQLExpression other = (CXMQLExpression) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.expression == null) ? (other.expression != null) : !this.expression.equals(other.expression)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + (this.expression != null ? this.expression.hashCode() : 0);
        return hash;
    }
}
