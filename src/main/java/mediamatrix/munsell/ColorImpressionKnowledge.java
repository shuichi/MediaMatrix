package mediamatrix.munsell;

import mediamatrix.utils.CSV;
import mediamatrix.utils.VectorUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ColorImpressionKnowledge implements Serializable {

    @Serial
    private static final long serialVersionUID = 3173787927085394572L;

    private HSVColor[] colors;
    private final ArrayList<String> csOrder;
    private final TreeMap<String, Double[]> valueMap;
    private final TreeMap<String, BufferedImage> images;
    private final TreeMap<Integer, HSVColor> mapper;
    private static final int INTERVAL = 4;

    public ColorImpressionKnowledge() {
        this.valueMap = new TreeMap<>();
        this.mapper = new TreeMap<>();
        this.csOrder = new ArrayList<>();
        this.images = new TreeMap<>();
    }

    public ColorImpressionKnowledge duplicate() {
        final ColorImpressionKnowledge ci = new ColorImpressionKnowledge();
        ci.load(toString());
        return ci;
    }

    public void remove(String word) {
        valueMap.remove(word);
        csOrder.remove(word);
        images.remove(word);
    }

    public Double[] getColorVector(String word) {
        return valueMap.get(word);
    }

    public void add(String word, ColorHistogram histogram) {
        final Double[] values = new Double[histogram.size()];
        for (int i = 0; i < colors.length; i++) {
            values[i] = histogram.get(colors[i]).getRatio();
        }
        valueMap.put(word, VectorUtils.normalize1(values));
        csOrder.add(word);
        images.put(word, createHistogramImage(word));
    }

    public void add(String word, Double[] values) {
        valueMap.put(word, values);
        csOrder.add(word);
        images.put(word, createHistogramImage(word));
    }

    public BufferedImage createClusterdImage(BufferedImage image) {
        BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < image.getWidth() - 1; i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color c = new Color(image.getRGB(i, j));
                HSVColor output = getMunsellColor(c.getRed(), c.getGreen(), c.getBlue());
                target.setRGB(i, j, output.getRGB());
            }
        }
        return target;
    }

    public BufferedImage createClusterdImage(BufferedImage image, String[] selection) {
        BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < image.getWidth() - 1; i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color c = new Color(image.getRGB(i, j));
                HSVColor output = getMunsellColor(c.getRed(), c.getGreen(), c.getBlue());
                boolean isSelected = false;
                for (String selection1 : selection) {
                    if (output.getName().startsWith(selection1)) {
                        target.setRGB(i, j, c.getRGB());
                        isSelected = true;
                        break;
                    }
                }
                if (!isSelected) {
                    target.setRGB(i, j, new Color(255, 255, 255).getRGB());
                }
            }
        }
        return target;
    }

    public HSVColor[] getColors() {
        return colors;
    }

    public HSVColor findColor(String name) {
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].getName().equalsIgnoreCase(name)) {
                return colors[i];
            }
        }
        return null;
    }

    public int size() {
        return csOrder.size();
    }

    public String[] getWords() {
        return csOrder.toArray(String[]::new);
    }

    public int wordToCode(String word) {
        return csOrder.indexOf(word) + 1;
    }

    public String toPrintName(String word) {
        return word + "(cs" + wordToCode(word) + ")";
    }

    public BufferedImage getHistogramImage(String word) {
        return images.get(word);
    }

    private BufferedImage createHistogramImage(String word) {
        double h = 30;
        double w = 100;
        final BufferedImage image = new BufferedImage((int) w, (int) h, BufferedImage.TYPE_INT_RGB);
        final Double[] values = valueMap.get(word);
        final Graphics2D g2 = image.createGraphics();

        double xstart = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > 0) {
                g2.setPaint(colors[i]);
                double width = values[i] * 100;
                g2.fill(new Rectangle2D.Double(xstart, 0d, width, h));
                xstart += width;
            }
        }
        g2.setPaint(Color.black);
        g2.drawRect(0, 0, 99, 29);
        return image;
    }

    public ColorHistogram generateHistogram(BufferedImage image) {
        final TreeMap<HSVColor, ColorHistogramScore> scores = new TreeMap<>();
        for (HSVColor color : colors) {
            scores.put(color, new ColorHistogramScore(color, image.getWidth() * image.getHeight()));
        }
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int c = image.getRGB(i, j);
                if (c != 0) {
                    scores.get(getMunsellColor(new Color(c))).increase();
                }
            }
        }
        return new ColorHistogram(scores);
    }

    public Correlation[] generateMetadata(ColorHistogram histogram) {
        final TreeSet<Correlation> set = new TreeSet<Correlation>();
        final Double[] values = new Double[colors.length];
        for (int i = 0; i < colors.length; i++) {
            values[i] = histogram.get(colors[i]).getValue();
        }
        final Set<String> words = valueMap.keySet();
        for (String word : words) {
            set.add(new Correlation(word, VectorUtils.innerProduct(values, valueMap.get(word))));
        }
        return set.toArray(new Correlation[set.size()]);
    }

    public double generateMetadata(float[] histogram, String word) {
        return VectorUtils.innerProduct(histogram, valueMap.get(word));
    }

    public Correlation[] generateMetadataInDictionaryOrder(ColorHistogram histogram) {
        final List<Correlation> result = new ArrayList<Correlation>();
        final Double[] values = new Double[colors.length];
        for (int i = 0; i < colors.length; i++) {
            values[i] = histogram.get(colors[i]).getValue();
        }
        final Set<String> words = valueMap.keySet();
        for (String word : words) {
            result.add(new Correlation(word, VectorUtils.innerProduct(values, valueMap.get(word))));
        }
        return result.toArray(new Correlation[result.size()]);
    }

    public HSVColor getMunsellColor(Color c) {
        return getMunsellColor(c.getRed(), c.getGreen(), c.getBlue());
    }

    public HSVColor getMunsellColor(int r, int g, int b) {
        r = r / INTERVAL * INTERVAL;
        g = g / INTERVAL * INTERVAL;
        b = b / INTERVAL * INTERVAL;
        return mapper.get(new Color(r, g, b).getRGB());
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("@COLORS,");
        for (int i = 0; i < colors.length; i++) {
            final HSVColor c = colors[i];
            out.append("\"");
            out.append(c.getName());
            out.append("(");
            out.append(Integer.toString(c.getRed()));
            out.append(",");
            out.append(Integer.toString(c.getGreen()));
            out.append(",");
            out.append(Integer.toString(c.getBlue()));
            out.append(")");
            out.append("\"");
            if (i + 1 < colors.length) {
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
                    if (line.startsWith("@COLORS")) {
                        colors = new HSVColor[list.size() - 1];
                        for (int i = 1; i < list.size(); i++) {
                            colors[i - 1] = HSVColor.createHSVColor((list.get(i)).trim());
                        }
                    } else {
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

        for (String word : csOrder) {
            images.put(word, createHistogramImage(word));
        }
        for (int r = 0; r < 256; r += INTERVAL) {
            for (int g = 0; g < 256; g += INTERVAL) {
                for (int b = 0; b < 256; b += INTERVAL) {
                    final Color c = new Color(r, g, b);
                    final HSVColor inputColor = new HSVColor(c.getRGB());
                    HSVColor result = null;
                    double min = Double.MAX_VALUE;
                    for (int i = 0; i < colors.length; i++) {
                        final double distance = colors[i].distance(inputColor);
                        if (min > distance) {
                            min = distance;
                            result = colors[i];
                        }
                    }
                    mapper.put(c.getRGB(), result);
                }
            }
        }
    }
}
