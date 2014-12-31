package mediamatrix.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import mediamatrix.db.MediaDataObject;
import mediamatrix.db.MediaMatrix;
import mediamatrix.db.PrimitiveEngine;

public class DominantFeatureSelection {

    public static void main(String[] args) throws Exception {
        dominant2();
        emergent2();
    }

    public static void dominant2() throws Exception {
        PrimitiveEngine engine = new PrimitiveEngine();
        BufferedWriter out = new BufferedWriter(new FileWriter(new File("C:/Temp/exp1-f.csv")));
        MediaDataObject[] db = engine.openDB("C:/Users/shuichi/Documents/Experiments/MediaMatrix/CARC");
        Map<String, Integer> map = null;
        for (int i = 0; i < db.length; i++) {
            MediaMatrix matrix = engine.open(db[i], "ColorImpression");
            MediaMatrix dcs = engine.dcs(matrix);
            MediaMatrix histogram = engine.histogram(dcs);
            if (map == null) {
                map = new TreeMap<String, Integer>();
                String[] columns = histogram.getColumns();
                for (int j = 0; j < columns.length; j++) {
                    map.put(columns[j], 0);
                }
            }
            List<String> attrs = engine.selectAttributesMoreThan(histogram, "average", 1);
            System.out.println(attrs.size() + " selected.");
            for (String attr : attrs) {
                map.put(attr, map.get(attr) + 1);
            }
        }
        Set<String> keys = map.keySet();
        for (String key : keys) {
            out.append(String.format("%s,%d\n", key, map.get(key)));
        }
        out.close();
    }

    public static void emergent2() throws Exception {
        PrimitiveEngine engine = new PrimitiveEngine();
        BufferedWriter out = new BufferedWriter(new FileWriter(new File("C:/Temp/exp2-f.csv")));
        MediaDataObject[] db = engine.openDB("C:/Users/shuichi/Documents/Experiments/MediaMatrix/CARC");
        Map<String, Integer> map = null;
        for (int i = 0; i < db.length; i++) {
            MediaMatrix matrix = engine.open(db[i], "ColorImpression");
            MediaMatrix ecs = engine.ecs(matrix);
            MediaMatrix histogram = engine.nonZerohistogram(ecs);
            if (map == null) {
                map = new TreeMap<String, Integer>();
                String[] columns = histogram.getColumns();
                for (int j = 0; j < columns.length; j++) {
                    map.put(columns[j], 0);
                }
            }
            List<String> attrs = engine.selectAttributesMoreThan(histogram, "average", 1);
            System.out.println(attrs.size() + " selected.");
            for (String attr : attrs) {
                map.put(attr, map.get(attr) + 1);
            }
        }
        Set<String> keys = map.keySet();
        for (String key : keys) {
            out.append(String.format("%s,%d\n", key, map.get(key)));
        }
        out.close();
    }

    public static void emergent1() throws Exception {
        PrimitiveEngine engine = new PrimitiveEngine();
        BufferedWriter out = new BufferedWriter(new FileWriter(new File("C:/Temp/exp2-f.csv")));
        MediaDataObject[] db = engine.openDB("C:/Users/shuichi/Documents/Experiments/MediaMatrix/CARC");
        for (int i = 0; i < db.length; i++) {
            MediaMatrix matrix = engine.open(db[i], "ColorImpression");
            MediaMatrix ecs = engine.ecs(matrix);
            MediaMatrix histogram = engine.nonZerohistogram(ecs);
            List<String> attrs = engine.selectAttributesMoreThan(histogram, "average", 1);
            System.out.println(attrs.size() + " selected.");
            StringBuilder builder = new StringBuilder();
            builder.append(db[i].toString());
            builder.append(",");
            builder.append(attrs.size());
            for (String name : attrs) {
                builder.append(",");
                builder.append(name);
            }
            builder.append("\n");
            out.append(builder.toString());
        }
        out.close();
    }
}
