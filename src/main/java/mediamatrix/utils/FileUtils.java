package mediamatrix.utils;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class FileUtils {

    

    public static JFileChooser createJFileChooserWithImagePreview() {
        final JLabel previewLabel = new JLabel();
        final JFileChooser fcsr = new JFileChooser();
        final JScrollPane scroll = new JScrollPane(previewLabel);
        scroll.setPreferredSize(new Dimension(200, 0));
        fcsr.setAccessory(scroll);
        fcsr.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                File file = fcsr.getSelectedFile();
                if (file == null) {
                    return;
                }
                if (file.isDirectory()) {
                    return;
                }
                try {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) {
                        Image tempIm = ImageUtilities.createThumbnail(image, 200, 200);
                        BufferedImage thumImage = ImageUtilities.imageToBufferedImage(tempIm);
                        previewLabel.setIcon(new ImageIcon(thumImage));
                        previewLabel.setText("");
                    }
                } catch (IOException ex) {
                    previewLabel.setText("Preview can't be created");
                }
            }
        });
        return fcsr;
    }
}
