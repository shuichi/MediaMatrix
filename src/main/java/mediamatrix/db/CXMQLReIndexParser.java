package mediamatrix.db;

import java.util.List;
import mediamatrix.munsell.ColorImpressionDataStore;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.utils.StringUtils;

public class CXMQLReIndexParser extends CXMQLParser {

    public CXMQLReIndexParser() {
    }

    @Override
    public CXMQLScript parse(String query) {
        final CXMQLReIndexScript script = new CXMQLReIndexScript();
        final List<String> lines = StringUtils.readLines(query);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("//") || line.startsWith("#")) {
                continue;
            } else if (line.startsWith(CXMQLParser.DELTA_INDEX)) {
                final String index = line.split("WITH")[0];
                final String with = line.split("WITH")[1].trim();
                final String featurename = index.substring(CXMQLParser.DELTA_INDEX.length(), index.lastIndexOf('[')).trim();
                final String dirname = index.substring(index.indexOf('[')).trim().replace("[", "").replace("]", "").replace("*", "");
                script.setFeatures(featurename.split(","));
                script.setTarget(dirname);
                final String[] keyvalue = with.trim().replace("[", "").replace("]", "").split(",");
                if (!keyvalue[0].equalsIgnoreCase("DEFAULT")) {
                    for (String kv : keyvalue) {
                        String[] elems = kv.split("=");
                        script.setProperty(elems[0].trim(), elems[1].trim());
                    }
                }
            }
        }
        return script;
    }

    public static void main(String args[]) throws Exception {
        String line = "DELTA INDEX dark [D:/Temp] WITH [COLORSCHEME=CIS4]";

        CXMQLScript script = new CXMQLParserFactory().getParser(line).parse(line);
        System.out.println(script);

        ColorImpressionKnowledge ci = ColorImpressionDataStore.getColorImpressionKnowledge("CIS2");

        final ChronoArchive carc = new ChronoArchive("D:/Temp/eva.carc");
        final MediaMatrix colorMatrix = carc.getColorMatrix();
        float[] rowVector = new float[colorMatrix.getHeight()];
        for (int i = 0; i < colorMatrix.getHeight(); i++) {
            rowVector[i] = (float) ci.generateMetadata(colorMatrix.getRowVector(i), "vivid");
        }
        ci.add("newVivid", ci.getColorVector("vivid"));
        
        final MediaMatrix mat = carc.getMatrix();
        mat.addColumn("newVivid", rowVector);
        carc.setMatrix(mat);
        carc.setColorImpressionKnowledge(ci);
        carc.update();
    }
}
