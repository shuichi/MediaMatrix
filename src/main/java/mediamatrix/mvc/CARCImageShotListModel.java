package mediamatrix.mvc;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.AbstractListModel;
import mediamatrix.db.ChronoArchive;
import mediamatrix.gui.ImageShot;
import mediamatrix.utils.ImageUtilities;

public class CARCImageShotListModel extends AbstractListModel<ImageShot> {

    private static final long serialVersionUID = 1L;
    private final ChronoArchive carc;
    private HashMap<Integer, ImageShot> cache;

    public CARCImageShotListModel(ChronoArchive carc) {
        this.carc = carc;
        this.cache = new HashMap<Integer, ImageShot>();
        for (int i = 0; i < carc.size() && i < 100; i++) {
            try {
                BufferedImage im = ImageUtilities.imageToBufferedImage(ImageUtilities.createThumbnail(carc.getImage(i), 90, 90));
                ImageShot image = new ImageShot(i, im);
                image.setThumbnail(im);
                cache.put(i, image);
            } catch (IOException ex) {
            }
        }
    }

    @Override
    public ImageShot getElementAt(int index) {
        ImageShot image = null;
        if (cache.containsKey(index)) {
            image = cache.get(index);
        } else {
            try {
                BufferedImage im = ImageUtilities.imageToBufferedImage(ImageUtilities.createThumbnail(carc.getImage(index), 90, 90));
                image = new ImageShot(index, im);
                image.setThumbnail(im);
                cache.put(index, image);
            } catch (IOException ex) {
            }
        }
        return image;
    }

    @Override
    public int getSize() {
        return carc.size();
    }
}
