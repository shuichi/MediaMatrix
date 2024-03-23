package mediamatrix.utils;

import java.util.Objects;

@SuppressWarnings("rawtypes")
public class Score<T extends Comparable, V extends Comparable> implements Comparable<Score<T, V>> {

    public final T key;
    public final V score;

    public Score(T key, V score) {
        this.key = key;
        this.score = score;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final Score<T, V> other = (Score<T, V>) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return Objects.equals(this.score, other.score);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.key);
        hash = 83 * hash + Objects.hashCode(this.score);
        return hash;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(Score<T, V> o) {
        if (o.equals(this)) {
            return 0;
        } else if (o.score.equals(score)) {
            return key.compareTo(o.key);
        } else {
            return o.score.compareTo(score);
        }
    }

    @Override
    public String toString() {
        return "Score{" + "key=" + key + ", score=" + score + '}';
    }
}
