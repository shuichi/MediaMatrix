package mediamatrix.io;

import java.io.File;
import java.io.FileFilter;

public class RegexFileFilter implements FileFilter {
    
    private String filenameRegex = null;
    
    public RegexFileFilter(String regex) {
        filenameRegex = regex;
    }
    
    public boolean accept(File pathname) {
        return (pathname.isFile() && pathname.getName().matches(filenameRegex));
    }
    
    public String getFilenameRegex() {
        return filenameRegex;
    }
    
    public void setFilenameRegex(String string) {
        filenameRegex = string;
    }
}