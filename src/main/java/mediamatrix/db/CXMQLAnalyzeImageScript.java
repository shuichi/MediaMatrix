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
            files = new File(dirname).listFiles(new ImageFilenameFilter());
        }
    }

    @Override
    public String getType() {
        return "image";
    }

    @Override
    public Map<String, Object> getVars() {
        return new TreeMap<String, Object>();
    }

    @Override
    public CXMQLResultSet eval() throws Exception {
        final Set<MediaDataObjectScore> result = new TreeSet<MediaDataObjectScore>();
        for (int i = 0; i < files.length; i++) {
            result.add(new MediaDataObjectScore(new MediaDataObject(files[i].getAbsolutePath()), 0d));
        }
        return new CXMQLResultSet(result, getType());
    }
}
