package mediamatrix.db;

import java.io.IOException;
import java.util.List;
import mediamatrix.utils.StringUtils;

public class CXMQLVisualizeParser {

    public static final String VISUALIZE_BY = "VISUALIZE BY";
    public static final String WITH = "WITH";
    public static final String FROM = "FROM";

    public CXMQLVisualizeParser() {
    }

    public CXMQLVisualizeScript parse(String query) {
        final CXMQLVisualizeScript script = new CXMQLVisualizeScript();
        final List<String> lines = StringUtils.readLines(query);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("//") || line.startsWith("#")) {
                continue;
            } else if (line.startsWith(VISUALIZE_BY)) {
                final String[] keyvalue = line.substring(VISUALIZE_BY.length()).trim().replace("[", "").replace("]", "").split(",");
                if (!keyvalue[0].equalsIgnoreCase("DEFAULT")) {
                    for (String kv : keyvalue) {
                        String[] elems = kv.split("=");
                        script.setProperty(elems[0].trim(), elems[1].trim());
                    }
                }
            } else if (line.startsWith(FROM)) {
                final String param = line.substring(FROM.length()).trim().replace("[", "").replace("]", "").replace("*", "");
                final String[] elems = param.split("@");
                script.setTarget(elems[0].trim());
                script.setMatrixName(elems[1].replace("(", "").replace(")", "").trim());
            } else if (line.startsWith(WITH)) {
                script.add(line.substring(WITH.length()).trim().split("->"));
                while (i + 1 < lines.size()) {
                    i++;
                    script.add(lines.get(i).split("->"));
                }
            }
        }
        return script;
    }
}
