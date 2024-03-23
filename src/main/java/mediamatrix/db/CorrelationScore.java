package mediamatrix.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CorrelationScore implements Comparable<CorrelationScore>, Serializable {

    static final long serialVersionUID = 3879251447116018007L;
    private String word;
    private double value;

    public CorrelationScore() {
    }

    public CorrelationScore(String word, double value) {
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

    @Override
    public String toString() {
        BigDecimal aDecimal = new BigDecimal(value);
        return word + ": " + aDecimal.setScale(5, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CorrelationScore)) {
            return false;
        }

        CorrelationScore col = (CorrelationScore) o;
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
    public int compareTo(CorrelationScore o) {
        if (this.equals(o)) {
            return 0;
        } else if (getValue() < o.getValue()) {
            return 1;
        } else {
            return -1;
        }
    }
}
