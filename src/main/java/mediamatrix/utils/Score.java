package mediamatrix.utils;

import java.util.Objects;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Score<T extends Comparable, V extends Comparable> implements Comparable<Score> {

    public final T key;
    public final V score;

    public Score(T key, V score) {
        this.key = key;
        this.score = score;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Score<T, V> other = (Score<T, V>) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.score, other.score)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.key);
        hash = 83 * hash + Objects.hashCode(this.score);
        return hash;
    }

    @Override
    public int compareTo(Score o) {
        if (o.equals(this)) {
            return 0;
        } else if (o.score.equals(score)) {
            return key.compareTo(o.key);
        } else {
            return o.score.compareTo(score);
        }
    }

    public String toString() {
        return "Score{" + "key=" + key + ", score=" + score + '}';
    }
}