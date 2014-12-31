package mediamatrix.io;

import java.io.File;
import java.io.FilenameFilter;

public class AndFilenameFilter implements FilenameFilter {

    private final FilenameFilter[] filters_;

    public AndFilenameFilter(FilenameFilter one, FilenameFilter two) {
        filters_ = new FilenameFilter[2];
        filters_[0] = one;
        filters_[1] = two;
    }

    public AndFilenameFilter(FilenameFilter[] filters) {
        filters_ = filters.clone();
    }

    public boolean accept(File dir, String name) {
        for (int i = 0; i < filters_.length; i++) {
            if (!filters_[i].accept(dir, name)) {
                return (false);
            }
        }
        return (true);
    }
}
