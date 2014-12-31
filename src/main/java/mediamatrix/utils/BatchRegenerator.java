package mediamatrix.utils;

import mediamatrix.io.ChronoArchiveFileFilter;
import mediamatrix.db.ChronoArchive;
import java.io.File;

public class BatchRegenerator {

    public static void main(String[] args) throws Exception {
        File dir = new File("C:/WORKSPACE");
        File[] files = dir.listFiles(new ChronoArchiveFileFilter());
        for (int i = 0; i < files.length; i++) {
            System.out.println("CARC : " + files[i].getName());
            final File outfile = new File(files[i].getParentFile(), files[i].getName().substring(0, files[i].getName().lastIndexOf('.')) + ".flv");
            final ChronoArchive carc = new ChronoArchive(files[i]);
            carc.getMainContent().renameTo(outfile);
            carc.close();
        }
    }
}
