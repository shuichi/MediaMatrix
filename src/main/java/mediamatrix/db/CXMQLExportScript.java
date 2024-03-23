package mediamatrix.db;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import mediamatrix.munsell.ColorHistogram;
import mediamatrix.munsell.ColorImpressionDataStore;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.munsell.HSVColor;

public class CXMQLExportScript extends CXMQLScript {

    private File[] files;
    private final List<String> matrixNames;
    private final PrimitiveEngine pe;

    public CXMQLExportScript() {
        super();
        pe = new PrimitiveEngine();
        matrixNames = new ArrayList<>();
    }

    public void addMatrixName(String name) {
        matrixNames.add(name);
    }

    public synchronized Object setProperty(String key, String value) {
        return pe.setProperty(key, value);
    }

    public synchronized String getProperty(String key) {
        return pe.getProperty(key);
    }

    public void setTarget(String dirname) {
        if (dirname.contains(",")) {
            final String[] names = dirname.split(",");
            final List<File> list = new ArrayList<>();
            for (String name : names) {
                final File file = new File(name.trim());
                if (file.exists()) {
                    list.add(file);
                }
            }
            files = list.toArray(File[]::new);
        } else if (new File(dirname).exists() && new File(dirname).isFile()) {
            files = new File[1];
            files[0] = new File(dirname);
        } else {
            if (getProperty("TYPE").equalsIgnoreCase("VIDEO")) {
                files = new File(dirname).listFiles((File pathname) -> pathname.toString().endsWith(".flv") || pathname.toString().endsWith(".mp4") || pathname.toString().endsWith(".mov"));
            } else if (getProperty("TYPE").equalsIgnoreCase("MUSIC")) {
                files = new File(dirname).listFiles((File pathname) -> pathname.toString().toLowerCase().endsWith(".mid") || pathname.toString().toLowerCase().endsWith(".smf"));
            } else if (getProperty("TYPE").equalsIgnoreCase("IMAGE")) {
                files = new File(dirname).listFiles((File pathname) -> pathname.toString().toLowerCase().endsWith(".jpg") || pathname.toString().toLowerCase().endsWith(".png"));
            }
        }
    }

    @Override
    public String getType() {
        return getProperty("TYPE");
    }

    @Override
    public Map<String, Object> getVars() {
        return new TreeMap<>();
    }

    @Override
    public CXMQLResultSet eval() throws Exception {
        final TreeSet<MediaDataObjectScore> result = new TreeSet<>();
        if (getProperty("FORMAT") == null) {
            for (int j = 0; j < files.length; j++) {
                final File infile = files[j];
                final File outfile = new File(infile.getParentFile(), infile.getName().substring(0, infile.getName().lastIndexOf('.')) + ".csv");
                for (String name : matrixNames) {
                    MediaMatrix mat = pe.open(infile.getAbsolutePath(), name);
                    mat.export("@" + name, new BufferedWriter(new FileWriter(outfile)));
                }
                getPropertyChangeSupport().firePropertyChange("progress", Integer.valueOf(100 * j / files.length), Integer.valueOf(100 * (j + 1) / files.length));
                result.add(new MediaDataObjectScore(new MediaDataObject(infile.getAbsolutePath()), 0d));
            }
        } else if (getProperty("FORMAT").equals("SQL")) {
            final File outfile = new File(files[0].getParentFile().getParentFile(), files[0].getParentFile().getName() + ".sql");
            final ColorImpressionKnowledge ci = ColorImpressionDataStore.getColorImpressionKnowledge(getProperty("COLORSCHEME"));
            try (BufferedWriter out = new BufferedWriter(new FileWriter(outfile))) {
                out.write("CREATE TABLE metadata (id text, feature text, score real);");
                out.newLine();
                for (int j = 0; j < files.length; j++) {
                    final File infile = files[j];
                    final BufferedImage image = ImageIO.read(infile);
                    final ColorHistogram histogram = ci.generateHistogram(image);
                    final HSVColor[] colors = ci.getColors();
                    for (HSVColor color : colors) {
                        out.write("INSERT INTO metadata (id, feature, score) VALUES('" + infile.getName() + "', '" + color.getName() + "', " + histogram.get(color).getRatio() + ");");
                        out.newLine();
                    }
                    getPropertyChangeSupport().firePropertyChange("progress", Integer.valueOf(100 * j / files.length), Integer.valueOf(100 * (j + 1) / files.length));
                    result.add(new MediaDataObjectScore(new MediaDataObject(infile.getAbsolutePath()), 0d));
                }
            }
        }
        getPropertyChangeSupport().firePropertyChange("complete", Integer.valueOf(99), Integer.valueOf(100));
        return new CXMQLResultSet(result, getType());
    }
}
