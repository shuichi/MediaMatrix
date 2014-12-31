package mediamatrix.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.imageio.ImageIO;
import mediamatrix.munsell.ColorImpressionDataStore;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.munsell.Correlation;

public class ImageMetadataGenerator {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File dir = new File("C:/Users/shuichi/Desktop/MediaMatrixDB");
        File[] files = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg");
            }
        });

        ColorImpressionKnowledge ci = ColorImpressionDataStore.getColorImpressionKnowledge("CIS2");

        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            double[] vec = null;

            File objectFile = new File(file.getCanonicalPath() + ".obj");
            if (objectFile.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objectFile));
                vec = (double[]) ois.readObject();
                ois.close();
            } else {
                Correlation[] metadata = ci.generateMetadataInDictionaryOrder(ci.generateHistogram(ImageIO.read(file)));
                vec = new double[metadata.length];
                for (int j = 0; j < metadata.length; j++) {
                    vec[j] = metadata[j].getValue();
                }
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(objectFile));
                oos.writeObject(vec);
                oos.close();
            }
            
            System.out.println(file);
            System.out.println(vec);
        }
    }
}
