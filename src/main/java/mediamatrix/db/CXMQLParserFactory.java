package mediamatrix.db;

import java.util.List;
import mediamatrix.utils.StringUtils;

public class CXMQLParserFactory {

    public CXMQLParserFactory() {
    }

    public CXMQLParser getParser(String query) {
        final List<String> lines = StringUtils.readLines(query);
        String type = null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("//") || line.startsWith("#")) {
                continue;
            } else if (line.startsWith(CXMQLParser.GENERATE)) {
                return new CXMQLGenerateParser();
            } else if (line.startsWith(CXMQLParser.EXPORT)) {
                return new CXMQLExportParser();
            } else if (line.startsWith(CXMQLParser.ANALYZE_BY)) {
                return new CXMQLParser();
            } else if (line.startsWith(CXMQLParser.DELTA_INDEX)) {
                return new CXMQLReIndexParser();
            }
        }
        return null;
    }
}
