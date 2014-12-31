/* MediaMatrix -- A Programable Database Engine for Multimedia
 * Copyright (C) 2008-2010 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mediamatrix.db;

import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.munsell.ColorHistogram;
import mediamatrix.munsell.Correlation;
import mediamatrix.munsell.HSVColor;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ChronoArchiveGenerator {

    private FFMpegShotSet shot;
    private ColorImpressionKnowledge ci;
    private ChronoArchive arc;
    private NeighborRelevance neighbor;
    private File infile;
    private File outfile;
    private int offset;
    private double freq;
    private int index;
    private ColorHistogram previous = null;
    private MediaMatrix mat;
    private MediaMatrix cmat;

    public ChronoArchiveGenerator(File infile, File outfile, int offset, double freq, ColorImpressionKnowledge ci) {
        this.infile = infile;
        this.outfile = outfile;
        this.offset = offset;
        this.freq = freq;
        this.ci = ci;
        index = 0;
        neighbor = new NeighborRelevance();
        arc = new ChronoArchive();
        arc.setMainContent(infile);
        arc.setSuffix(".jpg");
        arc.setTimeUnitType("Sec");
        arc.setTimeUnit(1 / freq);
    }

    public void capture() throws IOException, InterruptedException {
        shot = FFMpegShotSet.capture(infile, null, offset, freq);
        final double[] row = new double[shot.size()];
        for (int i = 0; i < row.length; i++) {
            row[i] = arc.getTimeUnit() * i;
        }
        mat = new MediaMatrix(row, ci.getWords());

        final HSVColor[] colors = ci.getColors();
        final String[] strColors = new String[colors.length];
        for (int i = 0; i < colors.length; i++) {
            strColors[i] = colors[i].getName();
        }
        cmat = new MediaMatrix(mat.getRows(), strColors);
    }

    public int size() {
        return shot.size();
    }

    public void doNext() throws IOException {
        final File imageFile = shot.getFile(index);
        final BufferedImage image = shot.get(index);
        final ColorHistogram histogram = ci.generateHistogram(image);
        final Correlation[] correlations = ci.generateMetadata(histogram);
        final Map<String, Object> metadata = new TreeMap<String, Object>();
        for (int i = 0; i < correlations.length; i++) {
            metadata.put(correlations[i].getWord(), new Double(correlations[i].getValue()));
        }

        final Set<String> keys = metadata.keySet();
        for (String key : keys) {
            mat.set(new Double(arc.getTimeUnit() * index), key, ((Double) metadata.get(key)));
        }
        for (HSVColor c : ci.getColors()) {
            cmat.set(new Double(arc.getTimeUnit() * index), c.getName(), ((Double) histogram.get(c).getValue()));
        }
        arc.add(imageFile, index);
        if (index > 0) {
            final double score = histogram.intersrction(previous);
            neighbor.add((double) index - 1, (double) index, score);
        }
        previous = histogram;
        index++;
    }

    public boolean hasNext() {
        return index < shot.size();
    }

    public void finish() throws IOException {
        try {
            if (shot.size() > 5) {
                arc.setThumbnail(shot.getFile(5));
            } else {
                arc.setThumbnail(shot.getFile(0));
            }
            arc.setColorImpressionKnowledge(ci);
            arc.setNeighborRelevance(neighbor);
            arc.setMatrix(mat);
            arc.setColorMatrix(cmat);
            arc.store(new BufferedOutputStream(new FileOutputStream(outfile.getAbsolutePath())));
            arc = null;
            ci = null;
        } finally {
            shot.close();
            shot = null;
        }
    }
}
