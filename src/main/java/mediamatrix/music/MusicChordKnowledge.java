package mediamatrix.music;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import mediamatrix.utils.CSV;

public class MusicChordKnowledge {

    private int[] tones;
    private List<ChordVector> chords;

    public MusicChordKnowledge() {
        chords = new ArrayList<ChordVector>();
    }

    public String findChord(int[] vec) {
        final ChordVector o = new ChordVector(null, vec);
        for (ChordVector chordVector : chords) {
            if (chordVector.innerProduct(o) >= 3) {
                return chordVector.getName();
            }
        }
        if (o.sum() >= 3) {
            return "OC";
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("@CHORD,");
        for (int i = 0; i < tones.length; i++) {
            out.append(Integer.toString(tones[i]));
            if (i + 1 < tones.length) {
                out.append(",");
            }
        }
        out.append("\n");
        for (ChordVector chordVector : chords) {
            out.append(chordVector);
            out.append("\n");
        }
        return out.toString();
    }

    public void load(InputStream in) throws IOException {
        final Reader input = new BufferedReader(new InputStreamReader(in));
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
                    if (line.startsWith("@CHORD")) {
                        tones = new int[list.size() - 1];
                        for (int i = 1; i < list.size(); i++) {
                            tones[i - 1] = Integer.parseInt((list.get(i)).trim());
                        }
                    } else {
                        final String word = list.get(0);
                        final int[] values = new int[list.size() - 1];
                        for (int i = 1; i < list.size(); i++) {
                            values[i - 1] = Integer.parseInt(list.get(i));
                        }
                        chords.add(new ChordVector(word, values));
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }
}
