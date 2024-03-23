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
package mediamatrix.munsell;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ColorHistogram implements Serializable {

    @Serial
    private static final long serialVersionUID = 251895384982248119L;
    private TreeMap<HSVColor, ColorHistogramScore> scores;

    public ColorHistogram() {
    }

    public ColorHistogram(TreeMap<HSVColor, ColorHistogramScore> scores) {
        this.scores = scores;
    }

    public double dissimilarity(ColorHistogram l) {
        return intersrction(l);
    }

    public double similarity(ColorHistogram l) {
        final ColorHistogram other = l;
        double sum = 0;
        final Set<HSVColor> colors = scores.keySet();
        for (HSVColor color : colors) {
            final ColorHistogramScore thisScore = get(color);
            final ColorHistogramScore otherScore = other.get(color);
            sum += (thisScore.getValue() * otherScore.getValue());
        }
        return sum;
    }

    public double intersrction(ColorHistogram other) {
        double sum = 0d;
        final Set<HSVColor> colors = scores.keySet();
        for (HSVColor color : colors) {
            final ColorHistogramScore thisScore = get(color);
            final ColorHistogramScore otherScore = other.get(color);
            sum += Math.abs(thisScore.getValue() - otherScore.getValue());
        }
        return sum / 2;
    }

    public BufferedImage createHistogramImage() {
        double h = 30;
        double w = 100;
        final BufferedImage image = new BufferedImage((int) w, (int) h, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2 = image.createGraphics();

        double xstart = 0;
        final Set<HSVColor> colors = scores.keySet();
        for (HSVColor color : colors) {
            if (scores.get(color).getValue() > 0) {
                g2.setPaint(color);
                double width = scores.get(color).getValue() * 100;
                g2.fill(new Rectangle2D.Double(xstart, 0d, width, h));
                xstart += width;
            }
        }
        return image;
    }

    public Map<HSVColor, ColorHistogramScore> getScores() {
        return scores;
    }

    public void setScores(TreeMap<HSVColor, ColorHistogramScore> scores) {
        this.scores = scores;
    }

    public ColorHistogramScore[] orderedScore() {
        TreeSet<ColorHistogramScore> set = new TreeSet<ColorHistogramScore>();
        Set<HSVColor> keys = scores.keySet();
        for (HSVColor c : keys) {
            set.add(scores.get(c));
        }
        return set.toArray(new ColorHistogramScore[set.size()]);
    }

    public ColorHistogramScore get(HSVColor color) {
        return scores.get(color);
    }

    public void add(HSVColor color) {
        scores.get(color).increase();
    }

    public int size() {
        return scores.size();
    }
}
