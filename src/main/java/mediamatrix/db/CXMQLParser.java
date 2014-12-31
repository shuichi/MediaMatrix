package mediamatrix.db;

import mediamatrix.utils.StringUtils;
import java.util.List;
import org.apache.commons.jexl2.JexlEngine;

public class CXMQLParser {

    public static final String GENERATE = "GENERATE";
    public static final String EXPORT = "EXPORT";
    public static final String ANALYZE_BY = "ANALYZE BY";
    public static final String QUERY_BY = "QUERY BY";
    public static final String FROM = "FROM";
    public static final String RANK_BY = "RANK BY";
    public static final String DELTA_INDEX = "DELTA INDEX";

    public CXMQLParser() {
    }

    public CXMQLScript parse(String query) {
        final JexlEngine engine = new JexlEngine();
        final CXMQLQueryScript script = new CXMQLQueryScript();
        final QueryByClause queryBy = new QueryByClause(engine);
        final FromClause from = new FromClause(engine);
        final RankByClause rankUsing = new RankByClause(engine);

        script.setFrom(from);
        script.setQueryBy(queryBy);
        script.setRankUsing(rankUsing);

        final List<String> lines = StringUtils.readLines(query);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("//") || line.startsWith("#")) {
                continue;
            } else if (line.startsWith(ANALYZE_BY)) {
                final String[] keyvalue = line.substring(ANALYZE_BY.length()).trim().replace("[", "").replace("]", "").split(",");
                if (!keyvalue[0].equalsIgnoreCase("DEFAULT")) {
                    for (String kv : keyvalue) {
                        String[] elems = kv.split("=");
                        script.setProperty(elems[0].trim(), elems[1].trim());
                    }
                }
            } else if (line.startsWith(QUERY_BY)) {
                queryBy.add(line.substring(QUERY_BY.length()).trim().split("->"));
                while (i + 1 < lines.size() && !lines.get(i + 1).startsWith("FROM")) {
                    i++;
                    queryBy.add(lines.get(i).split("->"));
                }
            } else if (line.startsWith(FROM)) {
                final String param = line.substring(FROM.length()).trim().replace("[", "").replace("]", "").replace("*", "");
                final String[] elems = param.split("@");
                from.setTarget(elems[0].trim());
                for (int j = 1; j < elems.length; j++) {
                    String matrixName = elems[j].replace("(", "").replace(")", "").trim();
                    from.add(new MediaMatrixGenerationClause(matrixName));
                }
                while (i + 1 < lines.size() && !lines.get(i + 1).startsWith(RANK_BY)) {
                    line = lines.get(++i).trim();
                    from.add(line);
                }
                if (i + 1 >= lines.size()) {
                    break;
                }
                line = lines.get(++i);
                rankUsing.add(line.substring(RANK_BY.length()).trim().split("->"));
                while (i + 1 < lines.size()) {
                    rankUsing.add(lines.get(++i).trim().split("->"));
                }
            }
        }
        return script;
    }
}
