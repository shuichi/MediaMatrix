package mediamatrix.music;

import java.util.Arrays;

public class ChordVector {

    private String name;
    private int notes[];

    public ChordVector() {
        notes = new int[12];
    }

    public ChordVector(String name, int[] vec) {
        this.name = name;
        notes = vec;
    }

    public int sum() {
        int sum = 0;
        for (int i = 0; i < notes.length; i++) {
            sum += notes[i];
        }
        return sum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void set(int index, int value) {
        notes[index] = value;
    }

    public int get(int index) {
        return notes[index];
    }

    public double innerProduct(ChordVector vec) {
        double sum = 0;
        for (int i = 0; i < notes.length; i++) {
            sum += (vec.notes[i] * notes[i]);
        }
        return sum;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("\"");
        buff.append(name);
        buff.append("\"");
        buff.append(",");
        for (int i = 0; i < notes.length; i++) {
            buff.append(notes[i]);
            if (i + 1 < notes.length) {
                buff.append(",");
            }
        }
        return buff.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChordVector other = (ChordVector) obj;
        if (!Arrays.equals(this.notes, other.notes)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Arrays.hashCode(this.notes);
        return hash;
    }
}
