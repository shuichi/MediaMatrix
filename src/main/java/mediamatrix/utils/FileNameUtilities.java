package mediamatrix.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FileNameUtilities {

    public FileNameUtilities() {
    }

    public static String getLastFileName(URL url) {
        String path = url.getPath();
        if (path == null) {
            return url.getHost();
        }

        if (path.length() == 1) {
            return url.getHost();
        }

        int pos = path.lastIndexOf('/');
        if (pos == -1 || pos == path.length() - 1) {
            return path;
        }

        return path.substring(pos + 1);
    }

    public static URL toURL(File aFile) {
        try {
            return new URL("file", "", slashify(aFile.getAbsolutePath(), aFile.isDirectory()));
        } catch (MalformedURLException ex) {
        }
        return null;
    }

    public static File getApplicationDirectory() {
        String prefDir = null;
        if (System.getProperty("os.name").equals("Windows Vista") || System.getProperty("os.name").equals("Windows 7")) {
            prefDir = System.getProperty("user.home") + System.getProperty("file.separator") + "Documents" + System.getProperty("file.separator") + "MediaMatrix";
        } else if (System.getProperty("os.name").indexOf("Windows") >= 0) {
            prefDir = System.getProperty("user.home") + System.getProperty("file.separator") + "Application Data" + System.getProperty("file.separator") + "MediaMatrix";
        } else if (System.getProperty("os.name").indexOf("Mac") >= 0) {
            prefDir = System.getProperty("user.home") + "/Library/Application Support/MediaMatrix";
        } else {
            prefDir = System.getProperty("user.home") + "/.mediamatrix";
        }
        final File dbDir = new File(prefDir);
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        return dbDir;
    }

    public static File getApplicationSubDirectory(String subdir) {
        final File dir = new File(FileNameUtilities.getApplicationDirectory(), subdir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File[] getFilesInApplicationSubDirectory(String subdir) {
        File[] files = getApplicationSubDirectory(subdir).listFiles();
        if (files == null) {
            files = new File[0];
        }
        return files;
    }

    private static String slashify(String path, boolean isDirectory) {
        String p = path;
        if (File.separatorChar != '/') {
            p = p.replace(File.separatorChar, '/');
        }
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        if (!p.endsWith("/") && isDirectory) {
            p = p + "/";
        }
        return p;
    }
}
