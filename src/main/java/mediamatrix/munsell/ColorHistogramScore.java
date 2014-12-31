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

import java.io.Serializable;
import java.math.BigDecimal;

public class ColorHistogramScore implements Comparable<ColorHistogramScore>, Serializable {

    static final long serialVersionUID = -4653634481066329663L;
    private HSVColor color;
    private int count;
    private int size;
    private double ratio;

    public ColorHistogramScore() {
    }

    public ColorHistogramScore(HSVColor color, int size) {
        this.color = color;
        this.count = 0;
        this.size = size;
    }

    public ColorHistogramScore(HSVColor color, int size, int count) {
        this.color = color;
        this.size = size;
        this.count = count;
    }

    public HSVColor getColor() {
        return color;
    }

    public int getCount() {
        return count;
    }

    public double getRatio() {
        return ratio;
    }

    public double getValue() {
        return (double) count / (double) size;
    }

    public void setColor(HSVColor color) {
        this.color = color;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void increase() {
        count++;
        ratio = getValue() * 100d;
    }

    @Override
    public String toString() {
        BigDecimal aDecimal = new BigDecimal(ratio);
        return color.getName() + ": " + count + " (" + aDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue() + "%)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ColorHistogramScore)) {
            return false;
        }

        ColorHistogramScore score = (ColorHistogramScore) o;
        return (getColor().equals(score.getColor()) && getCount() == score.getCount());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.color != null ? this.color.hashCode() : 0);
        hash = 97 * hash + this.count;
        hash = 97 * hash + this.size;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.ratio) ^ (Double.doubleToLongBits(this.ratio) >>> 32));
        return hash;
    }

    @Override
    public int compareTo(ColorHistogramScore score) {
        if (this.equals(score)) {
            return 0;
        } else if (ratio < score.ratio) {
            return 1;
        } else {
            return -1;
        }
    }
}
