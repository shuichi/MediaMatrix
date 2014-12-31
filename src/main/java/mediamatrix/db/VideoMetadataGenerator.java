package mediamatrix.db;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import mediamatrix.munsell.ColorHistogram;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.munsell.Correlation;

public class VideoMetadataGenerator {

    public File getFFmpeg() throws IOException {
        File exec = null;
        if (System.getProperty("os.name").indexOf("Windows") >= 0) {
            exec = new File("C:/ffmpeg/bin/ffmpeg.exe");
        } else {
            exec = new File("/usr/bin/ffmpeg");
        }
        return exec;
    }

    public FFMpegShotSet capture(File aFile, File workingDirectory, int offset, double freq) throws IOException, InterruptedException {
        if (workingDirectory == null) {
            workingDirectory = new File(System.getProperty("java.io.tmpdir"));
        }
        final File tempDir = new File(workingDirectory, aFile.getName() + "-temp" + System.currentTimeMillis());
        tempDir.mkdirs();
        final List<String> argsList = new ArrayList<String>();
        argsList.add(getFFmpeg().getAbsolutePath());
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
        printInputStream(p.getInputStream());
        p.waitFor();
        return new FFMpegShotSet(tempDir);
    }

    private void printInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            System.out.println(line);
        }
    }

    public MediaMatrix process(File infile, int offset, double freq) throws IOException {
        MediaMatrix mat = null;
        FFMpegShotSet shot = null;
        ColorImpressionKnowledge ci = new ColorImpressionKnowledge();
        ci.load(getClass().getResourceAsStream("/" + ColorImpressionKnowledge.class.getPackage().getName().replaceAll("\\.", "/") + "/CIS2.csv"), "UTF-8");
        try {
            shot = capture(infile, null, offset, freq);
            final double[] row = new double[shot.size()];
            for (int i = 0; i < row.length; i++) {
                row[i] = freq * i;
            }
            mat = new MediaMatrix(row, ci.getWords());

            for (int i = 0; i < shot.size(); i++) {
                BufferedImage image = shot.get(i);
                final ColorHistogram histogram = ci.generateHistogram(image);
                final Correlation[] correlations = ci.generateMetadata(histogram);
                final Map<String, Object> metadata = new TreeMap<String, Object>();
                for (int j = 0; j < correlations.length; j++) {
                    metadata.put(correlations[j].getWord(), new Double(correlations[j].getValue()));
                }
                final Set<String> keys = metadata.keySet();
                for (String key : keys) {
                    mat.set(new Double(freq * i), key, ((Double) metadata.get(key)));
                }
            }
        } catch (IOException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        } finally {
            if (shot != null) {
                shot.close();
            }
        }
        return mat;
    }
}
