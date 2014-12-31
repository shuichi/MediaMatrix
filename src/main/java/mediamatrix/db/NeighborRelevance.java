package mediamatrix.db;

import mediamatrix.utils.CSV;
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

public class NeighborRelevance {

    private final List<Double> begins;
    private final List<Double> ends;
    private final List<Double> values;

    public NeighborRelevance() {
        begins = new ArrayList<Double>();
        ends = new ArrayList<Double>();
        values = new ArrayList<Double>();
    }

    public int size() {
        return values.size();
    }

    public double getBegin(int index) {
        return begins.get(index);
    }

    public double getEnd(int index) {
        return ends.get(index);
    }

    public double getValue(int index) {
        return values.get(index);
    }

    public void add(double begin, double end, double value) {
        begins.add(begin);
        ends.add(end);
        values.add(value);
    }

    @Override
    public String toString() {
        final StringBuffer out = new StringBuffer();
        for (int i = 0; i < begins.size(); i++) {
            out.append(begins.get(i));
            out.append(",");
            out.append(ends.get(i));
            out.append(",");
            out.append(values.get(i));
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
                    begins.add(Double.parseDouble(list.get(0)));
                    ends.add(Double.parseDouble(list.get(1)));
                    values.add(Double.parseDouble(list.get(2)));
                }
            }
        } catch (IOException ignored) {
        }
    }
}
