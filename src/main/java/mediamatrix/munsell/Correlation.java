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

public class Correlation implements Comparable<Correlation>, Serializable {

    static final long serialVersionUID = 3879251447116018007L;
    private String word;
    private double value;

    public Correlation() {
    }

    public Correlation(String word, double value) {
        this.word = word;
        this.value = value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public double getValue() {
        return value;
    }

    public static Double[] toDoubleVector(Correlation[] correlations) {
        Double[] result = new Double[correlations.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = correlations[i].value;
        }
        return result;
    }

    @Override
    public String toString() {
        BigDecimal aDecimal = new BigDecimal(value);
        return word + ": " + aDecimal.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Correlation)) {
            return false;
        }

        Correlation col = (Correlation) o;
        return (getWord().equals(col.getWord()) && getValue() == col.getValue());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.word != null ? this.word.hashCode() : 0);
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        return hash;
    }

    @Override
    public int compareTo(Correlation col) {
        if (this.equals(col)) {
            return 0;
        } else if (getValue() < col.getValue()) {
            return 1;
        } else {
            return -1;
        }
    }
}
