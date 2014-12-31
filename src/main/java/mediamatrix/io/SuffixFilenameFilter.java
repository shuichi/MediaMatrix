package mediamatrix.io;

import java.io.File;
import java.io.FilenameFilter;
import javax.swing.filechooser.FileFilter;

public class SuffixFilenameFilter extends FileFilter implements FilenameFilter {

    private final String[] suffixes;

    public SuffixFilenameFilter(String suffix) {
        suffixes = new String[1];
        suffixes[0] = suffix;
    }

    public SuffixFilenameFilter(String[] suff) {
        suffixes = new String[suff.length];
        System.arraycopy(suff, 0, suffixes, 0, suffixes.length);
    }

    public boolean accept(File dir, String name) {
        boolean flag = false;
        for (int i = 0; i < suffixes.length; i++) {
            if (name.endsWith(suffixes[i])) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public boolean accept(File f) {
        boolean flag = false;
        for (int i = 0; i < suffixes.length; i++) {
            if (f.getName().endsWith(suffixes[i])) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public String getDescription() {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < suffixes.length; i++) {
            buff.append(suffixes[i]);
            if (i < suffixes.length - 1) {
                buff.append(", ");
            }
        }
        return buff.toString();
    }
}
