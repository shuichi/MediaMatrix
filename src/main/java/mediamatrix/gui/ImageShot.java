package mediamatrix.gui;

import java.awt.image.BufferedImage;

public class ImageShot {

    private double time;
    private BufferedImage image;
    private BufferedImage thumbnail;

    public ImageShot(double time, BufferedImage image) {
        this.time = time;
        this.image = image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(BufferedImage thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public BufferedImage getImage() {
        return image;
    }

    public double getTime() {
        return time;
    }
}
