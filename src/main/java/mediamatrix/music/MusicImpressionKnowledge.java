package mediamatrix.music;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import mediamatrix.db.MediaMatrix;
import mediamatrix.munsell.Correlation;
import mediamatrix.utils.CSV;
import mediamatrix.utils.VectorUtils;

public class MusicImpressionKnowledge {

    private final ArrayList<String> csOrder;
    private final TreeMap<String, Double[]> valueMap;
    private final String[] keyArray = new String[]{"C", "Am", "G", "Em", "D", "Bm", "A", "F#m", "E", "C#m", "B", "G#m", "F#", "Ebm", "Db", "Bbm", "Ab", "Fm", "Eb", "Cm", "Bb", "Gm", "F", "Dm"};

    public MusicImpressionKnowledge() {
        this.valueMap = new TreeMap<String, Double[]>();
        this.csOrder = new ArrayList<String>();
    }

    public Correlation[] generateMetadata(MediaMatrix matrix) {
        final TreeSet<Correlation> set = new TreeSet<Correlation>();
        final Double[] values = new Double[keyArray.length];
        for (int i = 0; i < keyArray.length; i++) {
            values[i] = matrix.get(0, i);
        }
        final Set<String> words = valueMap.keySet();
        for (String word : words) {
            set.add(new Correlation(word, VectorUtils.innerProduct(values, valueMap.get(word))));
        }
        return set.toArray(new Correlation[set.size()]);
    }

    public Correlation[] generateMetadataInDictionaryOrder(MediaMatrix matrix) {
        final List<Correlation> result = new ArrayList<Correlation>();
        final Double[] values = new Double[keyArray.length];
        for (int i = 0; i < keyArray.length; i++) {
            values[i] = matrix.get(0, i);
        }
        final Set<String> words = valueMap.keySet();
        for (String word : words) {
            result.add(new Correlation(word, VectorUtils.innerProduct(values, valueMap.get(word))));
        }
        return result.toArray(new Correlation[result.size()]);
    }

    public Double[] getVector(String word) {
        return valueMap.get(word);
    }

    public int size() {
        return csOrder.size();
    }

    public String[] getWords() {
        return csOrder.toArray(new String[csOrder.size()]);
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("@TONALITY,");
        for (int i = 0; i < keyArray.length; i++) {
            out.append(keyArray[i]);
            if (i + 1 < keyArray.length) {
                out.append(",");
            }
        }
        out.append("\n");
        for (String word : csOrder) {
            out.append(word);
            out.append(",");
            Double[] values = valueMap.get(word);
            for (int i = 0; i < values.length; i++) {
                out.append(values[i]);
                if (i + 1 < values.length) {
                    out.append(",");
                }
            }
            out.append("\n");
        }
        return out.toString();
    }

    public void load(File table, String encode) throws IOException {
        final Reader input = new BufferedReader(new InputStreamReader(new FileInputStream(table), encode));
        final StringWriter output = new StringWriter();
        char[] buffer = new char[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        input.close();
        load(output.toString());
    }

    public void store(OutputStream out, String encode) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encode));
        writer.append(toString());
        writer.close();
    }

    public void load(InputStream in, String encode) throws IOException {
        final Reader input = new BufferedReader(new InputStreamReader(in, encode));
        final StringWriter output = new StringWriter();
        char[] buffer = new char[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        input.close();
        load(output.toString());
    }

    public void load(String table) {
        final CSV csv = new CSV();
        final BufferedReader reader = new BufferedReader(new StringReader(table));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                final List<String> list = csv.parse(line);
                if (list.size() > 1) {
                    if (!line.startsWith("@TONALITY")) {
                        final String word = list.get(0);
                        final Double[] values = new Double[list.size() - 1];
                        for (int i = 1; i < list.size(); i++) {
                            values[i - 1] = Double.parseDouble(list.get(i));
                        }
                        valueMap.put(word, VectorUtils.normalize1(values));
                        csOrder.add(word);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }
}
