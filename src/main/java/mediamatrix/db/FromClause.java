package mediamatrix.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;

public class FromClause {

    private MediaDataObject[] files;
    private final List<String> expressionList;
    private final List<MediaMatrixGenerationClause> generationClauseList;
    private final JexlEngine engine;
    private String dirname;

    public FromClause(JexlEngine engine) {
        this.engine = engine;
        this.expressionList = new ArrayList<String>();
        this.generationClauseList = new ArrayList<MediaMatrixGenerationClause>();
    }

    public void setTarget(String dirname) {
        this.dirname = dirname;
    }

    public void add(MediaMatrixGenerationClause generation) {
        generationClauseList.add(generation);
    }

    public void add(String expression) {
        expressionList.add(expression);
    }

    public MediaDataObject get(int index) {
        return files[index];
    }

    public int size() {
        return files.length;
    }

    public void prepare() throws IOException {
        if (dirname.startsWith("Google:")) {
            files = new PrimitiveEngine().openDB(dirname);
        } else if (dirname.contains(",")) {
            final String[] names = dirname.split(",");
            final List<MediaDataObject> temp = new ArrayList<MediaDataObject>();
            for (int j = 0; j < names.length; j++) {
                final File file = new File(names[j].trim());
                if (file.exists()) {
                    temp.add(new MediaDataObject(file.getAbsolutePath()));
                }
            }
            files = temp.toArray(new MediaDataObject[temp.size()]);
        } else if (new File(dirname).exists() && new File(dirname).isFile()) {
            files = new MediaDataObject[1];
            files[0] = new MediaDataObject(dirname);
        } else {
            files = new PrimitiveEngine().openDB(dirname);
        }
    }

    @SuppressWarnings("unchecked")
    public void eval(int index, JexlContext context, PrimitiveEngine pe) throws Exception {
        for (MediaMatrixGenerationClause generationClause : generationClauseList) {
            final MediaMatrix matrix = generationClause.eval(files[index], pe);
            context.set("EACH_" + generationClause.getName(), matrix);
        }
        for (String expression : expressionList) {
            for (MediaMatrixGenerationClause generationClause : generationClauseList) {
                final String name = "EACH_" + generationClause.getName();
                if (expression.contains(name)) {
                    final JexlExpression jexlExpression = engine.createExpression("pe." + expression);
                    final Object result = jexlExpression.evaluate(context);
                    context.set(name, result);
                }
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder buff = new StringBuilder();
        buff.append("FROM\n");
        for (MediaMatrixGenerationClause mediaMatrixGenerationClause : generationClauseList) {
            buff.append("    ");
            buff.append(mediaMatrixGenerationClause.toString());
            buff.append("\n");
        }
        for (String exp : expressionList) {
            buff.append("    ");
            buff.append(exp);
            buff.append("\n");
        }
        return buff.toString();
    }
}
