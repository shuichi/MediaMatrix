package mediamatrix.db;

import java.util.List;
import mediamatrix.utils.StringUtils;

public class CXMQLGenerateParser extends CXMQLParser {

    public CXMQLGenerateParser() {
    }

    @Override
    public CXMQLScript parse(String query) {
        final CXMQLGenerateScript generateScript = new CXMQLGenerateScript();
        final List<String> lines = StringUtils.readLines(query);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("//") || line.startsWith("#")) {
                continue;
            } else if (line.startsWith(GENERATE)) {
                final String generate = line.split("WITH")[0];
                final String with = line.split("WITH")[1].trim();
                final String dirname = generate.substring(GENERATE.length()).trim().replace("[", "").replace("]", "").replace("*", "");
                generateScript.setTarget(dirname);
                final String[] keyvalue = with.trim().replace("[", "").replace("]", "").split(",");
                if (!keyvalue[0].equalsIgnoreCase("DEFAULT")) {
                    for (String kv : keyvalue) {
                        String[] elems = kv.split("=");
                        generateScript.setProperty(elems[0].trim(), elems[1].trim());
                    }
                }
            }
        }
        return generateScript;
    }
}
