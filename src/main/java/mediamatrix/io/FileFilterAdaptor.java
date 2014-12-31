package mediamatrix.io;

import java.io.File;
import java.io.FilenameFilter;
import javax.swing.filechooser.FileFilter;


public class FileFilterAdaptor extends FileFilter{
    
    private final FilenameFilter baseFilter;
    private final String description;
    

    public FileFilterAdaptor(FilenameFilter baseFilter, String description) {
        this.baseFilter = baseFilter;
        this.description = description;
    }
    
    
    public boolean accept(File f) {
        return baseFilter.accept(f.getParentFile(), f.getName());
    }
    
    
    public String getDescription() {
        return description;
    }
    
}
