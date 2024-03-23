package mediamatrix.utils;

import mediamatrix.io.ChronoArchiveFileFilter;
import mediamatrix.db.ChronoArchive;
import java.io.File;

public class BatchRegenerator {

    public static void main(String[] args) throws Exception {
        File dir = new File("C:/WORKSPACE");
        File[] files = dir.listFiles(new ChronoArchiveFileFilter());
        for (File file : files) {
            System.out.println("CARC : " + file.getName());
            final File outfile = new File(file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf('.')) + ".flv");
            final ChronoArchive carc = new ChronoArchive(file);
            carc.getMainContent().renameTo(outfile);
            carc.close();
        }
    }
}
