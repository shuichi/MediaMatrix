package mediamatrix.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.introspection.JexlPermissions;

public class CXMQLVisualizeScript {

    private final List<CXMQLExpression> list;
    private final JexlEngine engine;
    private final CXMQLContext context;
    private final PrimitiveEngine pe;
    private String target;
    private String matrixName;

    public CXMQLVisualizeScript() {
        super();     
        engine = new JexlBuilder().permissions(JexlPermissions.UNRESTRICTED).create();
        context = new CXMQLContext();
        pe = new PrimitiveEngine();
        context.set("pe", pe);
        list = new ArrayList<>();
    }

    public synchronized Object setProperty(String key, String value) {
        return pe.setProperty(key.toUpperCase(), value);
    }

    public String getProperty(String key) {
        return pe.getProperty(key.toUpperCase());
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMatrixName() {
        return matrixName;
    }

    public void setMatrixName(String matrixName) {
        this.matrixName = matrixName;
    }

    public void add(String[] elems) {
        if (elems.length > 1) {
            list.add(new CXMQLExpression(engine, elems[1].trim(), elems[0].trim()));
        }
    }

    public MediaMatrix eval() throws Exception {
        context.set("TARGET", engine.createExpression("pe.open(\"" + target + "\", \"" + matrixName + "\")").evaluate(context));
        MediaMatrix mat = (MediaMatrix) context.get("TARGET");
        for (CXMQLExpression exp : list) {
            exp.eval(context);
            if (exp.getName().equalsIgnoreCase("RESULT")) {
                mat = (MediaMatrix) context.get("RESULT");
            }
        }
        return mat;
    }

    public String getType() {
        return pe.getProperty("TYPE");
    }

    public String getViewerClass() {
        return pe.getProperty("VIEWER");
    }

    public Map<String, Object> getVars() {
        return context;
    }

    public String dumpContextIds() {
        StringBuilder sb = new StringBuilder();
        sb.append("memory dump (key -> ObjectID)\n");

        for (var entry : context.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            sb.append(key).append(" -> ");

            if (value == null) {
                sb.append("null");
            } else {
                int id = System.identityHashCode(value);
                sb.append("0x").append(Integer.toHexString(id))
                        .append(" (")
                        .append(value.getClass().getName())
                        .append(')');
            }

            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }
}
