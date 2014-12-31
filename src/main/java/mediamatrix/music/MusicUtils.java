package mediamatrix.music;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.midi.InvalidMidiDataException;

public class MusicUtils {

    public static BufferedImage visualize(InputStream in, int w, int h) throws IOException, InvalidMidiDataException {
        final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = image.createGraphics();
        final TonalMusic music = new TonalityAnalyzer().analyze(in, 30);
        final Key[] keys = music.getKeys();
        final Color[] colors = DefaultColorMap.SCRIABIN.visualize(keys);
        int sum = 0;
        for (int j = 0; j < colors.length; j++) {
            g.setColor(colors[j]);
            final int width = w / keys.length;
            g.fillRect(sum, 0, width, h);
            sum += width;
        }
        return image;
    }
}
