package mediamatrix.db;

import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.munsell.ColorImpressionDataStore;
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

public class CXMQLGenerateScript extends CXMQLScript {

    private File[] files;
    private final PrimitiveEngine pe;
    private int previous;

    public CXMQLGenerateScript() {
        super();
        pe = new PrimitiveEngine();
    }

    public synchronized Object setProperty(String key, String value) {
        return pe.setProperty(key, value);
    }

    public String getProperty(String key) {
        return pe.getProperty(key);
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

                private final String[] suffix = new String[]{"flv", "mp4", "mov", "avi", "divx", "ogv"};

                @Override
                public boolean accept(File pathname) {
                    boolean result = false;
                    for (int i = 0; i < suffix.length; i++) {
                        if (pathname.toString().endsWith(suffix[i])) {
                            result = true;
                            break;
                        }
                    }
                    return result;
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

        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final TreeSet<MediaDataObjectScore> result = new TreeSet<MediaDataObjectScore>();
        final List<Future<MediaDataObjectScore>> futures = new ArrayList<Future<MediaDataObjectScore>>();

        for (int i = 0; i < files.length; i++) {
            final int index = i;
            futures.add(executor.submit(new Callable<MediaDataObjectScore>() {

                @Override
                public MediaDataObjectScore call() throws Exception {
                    final File infile = files[index];
                    final File outfile = new File(infile.getParentFile(), infile.getName().substring(0, infile.getName().lastIndexOf('.')) + ".carc");
                    final ColorImpressionKnowledge ci = ColorImpressionDataStore.getColorImpressionKnowledge(pe.getProperty(CXMQLParameterNames.COLORSCHEME));
                    final ChronoArchiveGenerator generator = new ChronoArchiveGenerator(infile, outfile, 0, Integer.parseInt(pe.getProperty(CXMQLParameterNames.FREQ)), ci);
                    generator.capture();
                    while (generator.hasNext()) {
                        generator.doNext();
                    }
                    generator.finish();
                    return new MediaDataObjectScore(new MediaDataObject(outfile.getAbsolutePath()), 0d);
                }
            }));
        }

        getPropertyChangeSupport().firePropertyChange("progress", 1, 0);
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
        getPropertyChangeSupport().firePropertyChange("progress", previous, 100);

        return new CXMQLResultSet(result, getType());
    }
}
