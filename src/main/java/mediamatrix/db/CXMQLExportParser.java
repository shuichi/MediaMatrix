package mediamatrix.db;

import java.util.List;
import mediamatrix.utils.StringUtils;

public class CXMQLExportParser extends CXMQLParser {

    public CXMQLExportParser() {
    }

    @Override
    public CXMQLScript parse(String query) {
        final CXMQLExportScript script = new CXMQLExportScript();
        final List<String> lines = StringUtils.readLines(query);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("//") || line.startsWith("#")) {
            } else if (line.startsWith(EXPORT)) {
                final String generate = line.split("WITH")[0];
                final String with = line.split("WITH")[1].trim();
                final String param = generate.substring(EXPORT.length()).trim().replace("[", "").replace("]", "").replace("*", "");
                final String[] elems = param.split("@");
                for (int j = 1; j < elems.length; j++) {
                    String matrixName = elems[j].replace("(", "").replace(")", "").trim();
                    script.addMatrixName(matrixName);
                }
                final String[] keyvalue = with.trim().replace("[", "").replace("]", "").split(",");
                if (!keyvalue[0].equalsIgnoreCase("DEFAULT")) {
                    for (String kv : keyvalue) {
                        String[] kvs = kv.split("=");
                        script.setProperty(kvs[0].trim(), kvs[1].trim());
                    }
                }
                script.setTarget(elems[0].trim());
            }
        }
        return script;
    }
}
