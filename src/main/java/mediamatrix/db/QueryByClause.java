package mediamatrix.db;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;

public class QueryByClause {

    private final List<CXMQLExpression> list;
    private final JexlEngine engine;

    public QueryByClause(JexlEngine engine) {
        this.engine = engine;
        list = new ArrayList<CXMQLExpression>();
    }

    public void add(String[] elems) {
        if (elems.length > 1) {
            list.add(new CXMQLExpression(engine, elems[1].trim(), elems[0].trim()));
        }
    }

    public void add(String name, String expression) {
        list.add(new CXMQLExpression(engine, name, expression));
    }

    public MediaMatrix eval(JexlContext context) throws Exception {
        MediaMatrix mat = null;
        for (CXMQLExpression exp : list) {
            exp.eval(context);
            if (exp.getName().equalsIgnoreCase("QUERY")) {
                mat = (MediaMatrix) context.get("QUERY");
            }
        }
        return mat;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("QUERY BY\n");
        for (CXMQLExpression vmql : list) {
            buff.append("    " + vmql + "\n");
        }
        return buff.toString();
    }
}
