package mediamatrix.utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class MunsellColorTranslator {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        List<String> lines = readLines(new FileInputStream("real.txt"));
        BufferedWriter out = new BufferedWriter(new FileWriter(new File("MunsellColor.csv")));
        for (String line : lines) {
            int count = 0;
            String[] elems = line.split(" ");
            for (int i = 0; i < elems.length; i++) {
                if (elems[i].length() >= 1) {
                    out.append(elems[i].trim());
                    if (count++ < 5) {
                        out.append(",");
                    }
                }
            }
            out.newLine();
        }
        out.close();
        trans();
    }

    public static List<String> readLines(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input);
        return readLines(reader);
    }

    public static List<String> readLines(InputStream input, String encoding) throws IOException {
        if (encoding == null) {
            return readLines(input);
        } else {
            InputStreamReader reader = new InputStreamReader(input, encoding);
            return readLines(reader);
        }
    }

    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        List<String> list = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }

    public static void trans() throws Exception {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("MunsellColor.csv")));
        BufferedWriter out = new BufferedWriter(new FileWriter(new File("MunsellColorRGB.csv")));
        final CSV csv = new CSV();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            final List<String> list = csv.parse(line);
            if (list.size() > 1) {
                if (line.startsWith("H")) {
                    out.append("H,V,C,R,G,B");
                    out.newLine();
                } else {
                    StringBuffer buff = new StringBuffer();
                    buff.append(list.get(0).toString());
                    buff.append(",");
                    buff.append(list.get(1).toString());
                    buff.append(",");
                    buff.append(list.get(2).toString());
                    Color c = xyY2rgb(Double.parseDouble(list.get(3).toString()), Double.parseDouble(list.get(4).toString()), Double.parseDouble(list.get(5).toString()));
                    buff.append(",");
                    buff.append(Integer.toString(c.getRed()));
                    buff.append(",");
                    buff.append(Integer.toString(c.getGreen()));
                    buff.append(",");
                    buff.append(Integer.toString(c.getBlue()));
                    System.out.println(buff);
                    out.append(buff.toString());
                    out.newLine();
                }
            }
        }
        out.close();
        reader.close();
    }

    public static Color xyY2rgb(double x, double y, double Y) throws Exception {
        double X = Y / y * x;
        double Z = (Y / y) * (1 - x - y);
        int r = (int) Math.ceil((3.240479 * X) - (1.53715 * Y) - (0.498535 * Z));
        if (r < 0) {
            r = 0;
        }
        if (r > 255) {
            r = 255;
        }
        int g = (int) Math.ceil((-0.969256 * X) + (1.875991 * Y) + (0.041556 * Z));
        if (g < 0) {
            g = 0;
        }
        if (g > 255) {
            g = 255;
        }
        int b = (int) Math.ceil((0.055648 * X) - (-0.204043 * Y) + (1.057311 * Z));
        if (b < 0) {
            b = 0;
        }
        if (b > 255) {
            b = 255;
        }
        return new Color(r, g, b);
    }
}
