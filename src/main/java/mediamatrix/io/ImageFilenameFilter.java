package mediamatrix.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class ImageFilenameFilter extends javax.swing.filechooser.FileFilter implements FilenameFilter {

    private static final String[] SUFFIX = {".JPEG", ".JPG", ".PNG", ".BMP", ".GIF"};

    @Override
    public boolean accept(File file) {
        for (int i = 0; i < SUFFIX.length; i++) {
            if (file.getName().toUpperCase().endsWith(SUFFIX[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(File dir, String name) {
        for (int i = 0; i < SUFFIX.length; i++) {
            if (name.toUpperCase().endsWith(SUFFIX[i])) {
                return true;
            }
        }
        return false;
    }

    public File[] filter(File[] files) {
        final List<File> list = new ArrayList<File>();
        for (int i = 0; i < files.length; i++) {
            if (accept(files[i])) {
                list.add(files[i]);
            }
        }
        return list.toArray(new File[list.size()]);
    }

    @Override
    public String getDescription() {
        return "Image File (*.jpeg, *.bmp, *.png, *.gif)";
    }
}
