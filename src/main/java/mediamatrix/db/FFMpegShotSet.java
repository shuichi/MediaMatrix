package mediamatrix.db;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class FFMpegShotSet {

    private final File dir;
    private int index;
    private File[] files;

    public FFMpegShotSet(File dir) {
        this.dir = dir;
        files = dir.listFiles((File pathname) -> pathname.getName().toLowerCase().endsWith(".jpg"));
        Arrays.sort(files, (File f1, File f2) -> {
            String s1 = f1.getName().substring(0, f1.getName().lastIndexOf('.'));
            String s2 = f2.getName().substring(0, f2.getName().lastIndexOf('.'));
            return Integer.parseInt(s1) - Integer.parseInt(s2);
        });
        index = 0;
    }

    public static FFMpegShotSet capture(File aFile, File workingDirectory, int offset, double freq) throws IOException, InterruptedException {
        if (workingDirectory == null) {
            workingDirectory = new File(System.getProperty("java.io.tmpdir"));
        }
        final File tempDir = new File(workingDirectory, aFile.getName() + "-temp" + System.currentTimeMillis());
        tempDir.mkdirs();

        final List<String> argsList = new ArrayList<>();
        argsList.add(findExecutable("ffmpeg").getAbsolutePath());
        argsList.add("-i");
        argsList.add(aFile.getAbsolutePath());
        argsList.add("-ss");
        argsList.add("" + offset);
        argsList.add("-f");
        argsList.add("image2");
        argsList.add("-vcodec");
        argsList.add("mjpeg");
        argsList.add("-qscale");
        argsList.add("1");
        argsList.add("-qmin");
        argsList.add("1");
        argsList.add("-qmax");
        argsList.add("1");
        argsList.add("-r");
        argsList.add("" + freq);
        argsList.add(new File(tempDir, "%d.jpg").getAbsolutePath());

        final ProcessBuilder aProcessBuilder = new ProcessBuilder(argsList);
        aProcessBuilder.redirectErrorStream(true);
        Process p = aProcessBuilder.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            System.out.println(line);
        }
        p.waitFor();

        return new FFMpegShotSet(tempDir);
    }

    public static String version() throws IOException, InterruptedException {
        final List<String> argsList = new ArrayList<>();
        argsList.add(findExecutable("ffmpeg").getAbsolutePath());
        argsList.add("-version");

        final ProcessBuilder aProcessBuilder = new ProcessBuilder(argsList);
        aProcessBuilder.redirectErrorStream(true);
        final List<String> lines = new ArrayList<>(); 
        Process p = aProcessBuilder.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            lines.add(line);
        }
        p.waitFor();

        return lines.get(0);
    }

    public static File findExecutable(String cmd) {
        File executable = null;
        String path = null;
        final Map<String, String> env = System.getenv();
        if (env.containsKey("Path")) {
            path = env.get("Path");
        } else if (env.containsKey("PATH")) {
            path = env.get("PATH");
        } else if (env.containsKey("path")) {
            path = env.get("path");
        }

        if (System.getProperty("os.name").contains("Windows")) {
            cmd = cmd + ".exe";
        }

        final String[] dirs = path.split(File.pathSeparator);
        final List<String> lists = new ArrayList<>();
        lists.add(new File(System.getProperty("java.library.path")).getAbsolutePath());
        lists.add(new File("").getAbsolutePath());
        lists.addAll(Arrays.asList(dirs));
        lists.add("/usr/local/bin");
        lists.add("/opt/homebrew/bin");

        for (Iterator<String> it = lists.iterator(); it.hasNext();) {
            final File target = new File(it.next(), cmd);
            if (target.exists()) {
                executable = target;
                break;
            }
        }


        return executable;
    }

    public File getDir() {
        return dir;
    }

    public void close() {
        for (File file : files) {
            file.delete();
        }
        dir.delete();
    }

    public int size() {
        return files.length;
    }

    public BufferedImage get(int i) throws IOException {
        final File aFile = files[i];
        return ImageIO.read(aFile);
    }

    public File getFile(int i) throws IOException {
        return files[i];
    }

    public void zeroPosition() {
        index = 0;
    }

    public boolean hasNext() {
        return files.length > index;
    }

    public BufferedImage next() throws IOException {
        return get(index++);
    }
}
