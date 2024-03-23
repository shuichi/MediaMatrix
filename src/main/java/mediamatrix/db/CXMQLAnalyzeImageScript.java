package mediamatrix.db;

import mediamatrix.io.ImageFilenameFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class CXMQLAnalyzeImageScript extends CXMQLScript {

    private File[] files;

    public CXMQLAnalyzeImageScript() {
        super();
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
            files = new File(dirname).listFiles(new ImageFilenameFilter());
        }
    }

    @Override
    public String getType() {
        return "image";
    }

    @Override
    public Map<String, Object> getVars() {
        return new TreeMap<>();
    }

    @Override
    public CXMQLResultSet eval() throws Exception {
        final TreeSet<MediaDataObjectScore> result = new TreeSet<>();
        for (File file : files) {
            result.add(new MediaDataObjectScore(new MediaDataObject(file.getAbsolutePath()), 0d));
        }
        return new CXMQLResultSet(result, getType());
    }
}
