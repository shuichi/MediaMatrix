package mediamatrix.mvc;

import java.awt.image.BufferedImage;
import javax.swing.ListModel;
import mediamatrix.db.MediaMatrix;
import mediamatrix.gui.ImageShot;
import mediamatrix.munsell.ColorImpressionKnowledge;

public class DominanceEmergenceAnalysisResult {

    BufferedImage image;
    ListModel<ImageShot> model;
    ColorImpressionKnowledge ci;
    MediaMatrix matrix;

    public DominanceEmergenceAnalysisResult(BufferedImage image, ListModel<ImageShot> model, ColorImpressionKnowledge ci, MediaMatrix matrix) {
        this.image = image;
        this.model = model;
        this.ci = ci;
        this.matrix = matrix;
    }

    public ColorImpressionKnowledge getCi() {
        return ci;
    }

    public void setCi(ColorImpressionKnowledge ci) {
        this.ci = ci;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public MediaMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(MediaMatrix matrix) {
        this.matrix = matrix;
    }

    public ListModel<ImageShot> getModel() {
        return model;
    }

    public void setModel(ListModel<ImageShot> model) {
        this.model = model;
    }
}
