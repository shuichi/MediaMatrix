package mediamatrix.munsell;

import mediamatrix.utils.FileNameUtilities;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ColorImpressionDataStore {

    private static boolean INITIALIZED = false;
    private static final Map<String, ColorImpressionKnowledge> CACHE = new HashMap<String, ColorImpressionKnowledge>();

    private static void initDataStore() throws IOException {
        if (!INITIALIZED) {
            final File dbDir = new File(FileNameUtilities.getApplicationDirectory(), "ColorImpression");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            for (String res : new String[]{"CIC", "CIS2"}) {
                final URL url = ColorImpressionKnowledge.class.getResource("/" + ColorImpressionDataStore.class.getPackage().getName().replaceAll("\\.", "/") + "/" + res + ".csv");
                final ColorImpressionKnowledge ci = new ColorImpressionKnowledge();
                ci.load(url.openStream(), "UTF-8");
                CACHE.put(res, ci);
            }
            INITIALIZED = true;
        }
    }

    public static void registerColorImpressionKnowledge(String name, ColorImpressionKnowledge ci) throws IOException {
        initDataStore();
        final File dbDir = new File(FileNameUtilities.getApplicationDirectory(), "ColorImpression");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(dbDir, name + ".csv")));
        ci.store(bos, "UTF-8");
        CACHE.put(name, ci);
    }

    public static void registerRemoteColorImpressionKnowledge(String name, ColorImpressionKnowledge ci) throws IOException {
        CACHE.put(name, ci);
    }

    public static String[] getColorImpressionKnowledgeList() throws IOException {
        initDataStore();
        final Set<String> set = new TreeSet<String>();
        set.addAll(CACHE.keySet());
        final File dbDir = new File(FileNameUtilities.getApplicationDirectory(), "ColorImpression");
        final File[] files = dbDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            set.add(file.getName().substring(0, file.getName().lastIndexOf('.')));
        }
        return set.toArray(new String[set.size()]);
    }

    public static ColorImpressionKnowledge getColorImpressionKnowledge(String res) throws IOException {
        initDataStore();
        ColorImpressionKnowledge ci = CACHE.get(res);
        if (ci == null) {
            final File dbDir = new File(FileNameUtilities.getApplicationDirectory(), "ColorImpression");
            final File file = new File(dbDir, res + ".csv");
            if (file.exists()) {
                ci = new ColorImpressionKnowledge();
                ci.load(new BufferedInputStream(new FileInputStream(file)), "UTF-8");
                CACHE.put(file.getName().substring(0, file.getName().lastIndexOf('.')), ci);
            }
        }
        return ci;
    }
}
