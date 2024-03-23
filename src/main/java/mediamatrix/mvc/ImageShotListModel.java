package mediamatrix.mvc;

import java.io.Serial;
import java.util.List;
import javax.swing.AbstractListModel;
import mediamatrix.gui.ImageShot;
import mediamatrix.utils.ImageUtilities;

public class ImageShotListModel extends AbstractListModel<ImageShot> {

    @Serial
    private static final long serialVersionUID = 5532643415531313382L;
    private transient final List<ImageShot> images;

    public ImageShotListModel(List<ImageShot> images) {
        this.images = images;
        for (ImageShot p : images) {
            p.setThumbnail(ImageUtilities.imageToBufferedImage(ImageUtilities.createThumbnail(p.getImage(), 90, 90)));
        }
    }

    @Override
    public ImageShot getElementAt(int index) {
        return images.get(index);
    }

    @Override
    public int getSize() {
        return images.size();
    }
}
