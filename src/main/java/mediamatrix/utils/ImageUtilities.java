/* autonoesis -- Automatic Movie Processor
 * Copyright (C) 2008 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package mediamatrix.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtilities {

    public static BufferedImage imageToBufferedImage(Image img) {
        try {
            MediaTracker tracker = new MediaTracker(new Component() {

                private static final long serialVersionUID = 1L;
            });
            tracker.addImage(img, 0);
            tracker.waitForAll();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            return null;
        }

        BufferedImage bimg = null;
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            return null;
        }
        bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        bimg.setRGB(0, 0, w, h, pixels, 0, w);
        return bimg;
    }

    public static BufferedImage difference(BufferedImage fg, BufferedImage bg, double ratio) {
        for (int i = 0; i < fg.getWidth(); i++) {
            for (int j = 0; j < fg.getHeight(); j++) {
                Color fc = new Color(fg.getRGB(i, j));
                Color bc = new Color(bg.getRGB(i, j));
                int diff = Math.abs(fc.getRed() - bc.getRed()) + Math.abs(fc.getGreen() - bc.getGreen()) + Math.abs(fc.getBlue() - bc.getBlue());
                if (diff < ratio) {
                    fg.setRGB(i, j, 0);
                }
            }
        }
        return fg;
    }

    public static byte[] bufferedImageToByte(BufferedImage image, String format) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, format, bos);
        return bos.toByteArray();
    }

    public static BufferedImage byteToBufferedImage(byte[] buff) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(buff));
    }

    public static BufferedImage createThumbnail(BufferedImage image, int maxWidth, int maxHeight) {
        if (image == null) {
            return null;
        }
        final double widthRate = (double) maxWidth / image.getWidth();
        final double heightRate = (double) maxHeight / image.getHeight();
        double rate = 1;
        if (widthRate > heightRate) {
            rate = heightRate;
        } else {
            rate = widthRate;
        }
        int type = image.getType();
        if (type == BufferedImage.TYPE_CUSTOM) {
            type = BufferedImage.TYPE_INT_ARGB;
        }
        final int w = (int) (image.getWidth() * rate);
        final int h = (int) (image.getHeight() * rate);
        final Image newImage = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), new AreaAveragingScaleFilter(w, h)));
        return imageToBufferedImage(newImage);
    }
}
