package mediamatrix.db;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mediamatrix.munsell.ColorImpressionDataStore;
import mediamatrix.munsell.ColorImpressionKnowledge;

public class CXMQLReIndexScript extends CXMQLScript {

    private File[] files;
    private String[] features;
    private final PrimitiveEngine pe;
    private int previous;

    public CXMQLReIndexScript() {
        super();
        pe = new PrimitiveEngine();
    }

    public synchronized Object setProperty(String key, String value) {
        return pe.setProperty(key, value);
    }

    public String getProperty(String key) {
        return pe.getProperty(key);
    }

    public String[] getFeatures() {
        return features;
    }

    public void setFeatures(String[] features) {
        for (int i = 0; i < features.length; i++) {
            features[i] = features[i].trim();
        }
        this.features = features;
    }

    public void setTarget(String dirname) {
        if (dirname.contains(",")) {
            final String[] names = dirname.split(",");
            final List<File> list = new ArrayList<File>();
            for (int j = 0; j < names.length; j++) {
                final File file = new File(names[j].trim());
                if (file.exists()) {
                    list.add(file);
                }
            }
            files = list.toArray(new File[list.size()]);
        } else if (new File(dirname).exists() && new File(dirname).isFile()) {
            files = new File[1];
            files[0] = new File(dirname);
        } else {
            files = new File(dirname).listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.toString().toLowerCase().endsWith(".carc");
                }
            });
        }
    }

    @Override
    public String getType() {
        return "video";
    }

    @Override
    public Map<String, Object> getVars() {
        return new TreeMap<String, Object>();
    }

    @Override
    public CXMQLResultSet eval() throws Exception {
        final ColorImpressionKnowledge ci = ColorImpressionDataStore.getColorImpressionKnowledge(pe.getProperty(CXMQLParameterNames.COLORSCHEME));
        if (ci == null) {
            throw new IllegalStateException(pe.getProperty(CXMQLParameterNames.COLORSCHEME) + " not found.");
        }

        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final Set<MediaDataObjectScore> result = new TreeSet<MediaDataObjectScore>();
        final List<Future<MediaDataObjectScore>> futures = new ArrayList<Future<MediaDataObjectScore>>();

        for (int i = 0; i < files.length; i++) {
            final int index = i;
            futures.add(executor.submit(new Callable<MediaDataObjectScore>() {

                @Override
                public MediaDataObjectScore call() throws Exception {
                    final ChronoArchive carc = new ChronoArchive(files[index]);
                    final MediaMatrix colorMatrix = carc.getColorMatrix();
                    final MediaMatrix mat = carc.getMatrix();
                    float[] rowVector = new float[colorMatrix.getHeight()];
                    for (int j = 0; j < features.length; j++) {
                        for (int k = 0; k < colorMatrix.getHeight(); k++) {
                            rowVector[k] = (float) ci.generateMetadata(colorMatrix.getRowVector(k), features[j]);
                        }
                        mat.addColumn(features[j], rowVector);
                    }
                    carc.setColorImpressionKnowledge(ci);
                    carc.setMatrix(mat);
                    carc.update();
                    final MediaDataObjectScore score = new MediaDataObjectScore(new MediaDataObject(files[index].getAbsolutePath()), 0d);
                    return score;
                }
            }));
        }

        try {
            for (int i = 0; i < futures.size(); i++) {
                result.add(futures.get(i).get());
                int current = 0;
                if (files.length - 1 == 0) {
                    current = 100;
                } else {
                    current = 100 * i / (files.length - 1);
                }
                getPropertyChangeSupport().firePropertyChange("progress", previous, current);
                previous = current;
            }
        } finally {
            executor.shutdown();
        }

        return new CXMQLResultSet(result, getType());
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("DELTA INDEX ");
        for (int i = 0; i < features.length; i++) {
            out.append(features[i]);
            if (i + 1 != features.length) {
                out.append(",");
            }
        }
        out.append(" [");
        for (int i = 0; i < files.length; i++) {
            out.append(files[i].toString());
            if (i + 1 != files.length) {
                out.append(",");
            }
        }
        out.append("]");
        return out.toString();
    }
}
