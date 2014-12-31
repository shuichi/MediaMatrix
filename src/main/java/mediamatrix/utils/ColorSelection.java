package mediamatrix.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import mediamatrix.munsell.ColorHistogram;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.munsell.Correlation;

public class ColorSelection {

    public static void main(String[] args) throws Exception {
        String[] chinese = new String[]{"R/V", "YR/P", "Y/V", "GY/P", "G/V", "P/Dp", "N/10", "R/S", "YR/Vp", "Y/S", "N/2", "R/Dp", "YR/Dp", "Y/B", "R/Dk", "Y/P", "R/Dgr", "Y/Vp", "Y/L", "Y/Dp"};
        String[] italian = new String[]{"R/V", "YR/V", "Y/B", "GY/V", "G/V", "BG/Vp", "R/S", "YR/B", "Y/Vp", "GY/S", "G/Dp", "N/10", "R/B", "YR/P", "Y/P", "GY/B", "G/L", "G/Vp", "GY/L", "RP/V"};
        File file = new File("C:/Temp/chuka4.jpg");
        final BufferedImage image = ImageIO.read(file);
        final ColorImpressionKnowledge ci = new ColorImpressionKnowledge();
        ci.load(new File("C:/Users/shuichi/Documents/MediaMatrix/ColorImpression/CIS2.csv"), "UTF-8");
        final ColorHistogram histogram = ci.generateHistogram(image);
        final BufferedImage clusteredImage = ci.createClusterdImage(image, chinese);
        ImageIO.write(clusteredImage, "jpg", new File("C:/Temp/chuka4-selected.jpg"));
    }
}
