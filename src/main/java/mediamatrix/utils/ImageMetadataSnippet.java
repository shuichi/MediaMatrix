package mediamatrix.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import mediamatrix.munsell.ColorHistogram;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.munsell.Correlation;

public class ImageMetadataSnippet {

    public static void main(String[] args) throws Exception {
        final BufferedImage image = ImageIO.read(new URL("http://www.zarasu.com/f/tfsafe.cgi/lzh/bg_sky/sky0012.jpg"));
        final ColorImpressionKnowledge ci = new ColorImpressionKnowledge();
        ci.load(new File("C:/Users/shuichi/AppData/Local/MediaMatrix/ColorImpression/CIS2.csv"), "UTF-8");
        final ColorHistogram histogram = ci.generateHistogram(image);
        final Correlation[] correlations = ci.generateMetadata(histogram);
        for (int i = 0; i < correlations.length; i++) {
            System.out.println(correlations[i]);
        }
    }
}
