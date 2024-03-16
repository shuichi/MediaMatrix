/* Library for Tonality -- MIDI File Analyzer
 * Copyright (C) 2007 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
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
package mediamatrix.music;

import java.io.Serializable;

public class Key implements Serializable, Comparable<Key> {

    static final long serialVersionUID = -7282252807145337513L;
    private int code;
    private double score;
    private long length;
    private int sec;

    public Key() {
    }

    public Key(int code, double score) {
        this.code = code;
        this.score = score;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
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
        final Key other = (Key) obj;
        if (this.code != other.code) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.code;
        return hash;
    }

    @Override
    public int compareTo(Key o) {
        return Double.valueOf(this.score).compareTo(o.score);
    }

    @Override
    public String toString() {
        String key;
        if (code == 0 || code == 12) {
            key = "C";
        } else if (code == 1 || code == 2 || code == 13 || code == 14) {
            key = "D";
        } else if (code == 3 || code == 4 || code == 15 || code == 16) {
            key = "E";
        } else if (code == 5 || code == 6 || code == 17 || code == 18) {
            key = "F";
        } else if (code == 7 || code == 19) {
            key = "G";
        } else if (code == 8 || code == 9 || code == 20 || code == 21) {
            key = "A";
        } else {
            key = "B";
        }

        if (code == 1 || code == 13 || code == 3 || code == 15 || code == 8 || code == 20 || code == 10 || code == 22) {
            key += "b";
        }
        if (code == 6 || code == 18) {
            key += "#";
        }

        if (code >= 12) {
            key += "m";
        }

        return key;
    }
}
