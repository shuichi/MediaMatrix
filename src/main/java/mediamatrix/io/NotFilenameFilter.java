package mediamatrix.io;

import java.io.File;
import java.io.FilenameFilter;

public class NotFilenameFilter implements FilenameFilter {
    
    private final FilenameFilter filter_;
    
    public NotFilenameFilter(FilenameFilter filter) {
        filter_ = filter;
    }
    
    public boolean accept(File dir, String name) {
        return (!filter_.accept(dir, name));
    }
    
}