package mediamatrix.db;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;

public class RankByClause {

    private final List<CXMQLExpression> list;
    private final JexlEngine engine;

    public RankByClause(JexlEngine engine) {
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

    public Double eval(JexlContext context) throws Exception {
        Double d = null;
        for (CXMQLExpression exp : list) {
            exp.eval(context);
            if (exp.getName().equalsIgnoreCase("SCORE")) {
                d = (Double) context.get("SCORE");
            }
        }
        return d;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("RANK USING\n");
        for (CXMQLExpression exp : list) {
            buff.append("    " + exp + "\n");
        }
        return buff.toString();
    }
}
