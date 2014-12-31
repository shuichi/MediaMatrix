package mediamatrix.io;

import java.io.File;
import java.io.FilenameFilter;

public class FileFilenameFilter implements FilenameFilter {
    
    @Override
    public boolean accept(File dir, String name) {
        return (new File(dir, name).isFile());
    }
    
}