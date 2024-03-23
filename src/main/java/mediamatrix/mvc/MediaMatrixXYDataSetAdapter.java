package mediamatrix.mvc;

import mediamatrix.db.MediaMatrix;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public final class MediaMatrixXYDataSetAdapter extends XYSeriesCollection {

    private static final long serialVersionUID = 1L;
    private final MediaMatrix mat;

    public MediaMatrixXYDataSetAdapter(MediaMatrix mat) {
        super();
        this.mat = mat;
        for (int i = 0; i < mat.getWidth(); i++) {
            String word = mat.getColumn(i);
            String name = word;
            final XYSeries series = new XYSeries(name);
            for (int j = 0; j < mat.getHeight(); j++) {
                series.add(j, mat.get(j, i));
            }
            addSeries(series);
        }

    }
}
