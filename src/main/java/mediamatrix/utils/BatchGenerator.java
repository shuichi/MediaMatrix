package mediamatrix.utils;

import mediamatrix.io.ChronoArchiveFileFilter;
import mediamatrix.io.VideoFileFilter;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.db.ChronoArchiveGenerator;
import mediamatrix.munsell.ColorImpressionDataStore;
import java.io.File;

public class BatchGenerator {

    public static void main(String[] args) throws Exception {
        File dir = new File(args[0]);
        File[] files = dir.listFiles(new VideoFileFilter());
        File[] generated = dir.listFiles(new ChronoArchiveFileFilter());
        final ColorImpressionKnowledge ci = ColorImpressionDataStore.getColorImpressionKnowledge("CIS2");
        for (File file : files) {
            if (isAlreadyGenerated(file, generated)) {
                System.out.println("Skipped : " + file.getName());
            } else {
                long start = System.currentTimeMillis();
                System.out.println("Video : " + file.getName());
                final File infile = file;
                final File outfile = new File(infile.getParentFile(), infile.getName().substring(0, infile.getName().lastIndexOf('.')) + ".carc");
                final ChronoArchiveGenerator generator = new ChronoArchiveGenerator(infile, outfile, 0, 1, ci);
                generator.capture();
                int c = 0;
                while (generator.hasNext()) {
                    generator.doNext();
                    c++;
                }
                generator.finish();
                long end = System.currentTimeMillis();
                System.out.println("Processed " + file.getName() + " in " + ((end - start) / 1000) + " sec.");
            }
        }
    }

    public static boolean isAlreadyGenerated(File file, File[] generated) {
        String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
        for (File gen : generated) {
            String genName = gen.getName().substring(0, gen.getName().lastIndexOf('.'));
            if (genName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
