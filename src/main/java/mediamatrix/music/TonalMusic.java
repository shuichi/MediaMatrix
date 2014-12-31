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

public class TonalMusic implements Serializable {

    static final long serialVersionUID = -30400748829242334L;
    private Key[] keys;
    private String name;
    private long length;
    private long performance;
    private long count;

    public TonalMusic() {
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getPerformance() {
        return performance;
    }

    public void setPerformance(long performance) {
        this.performance = performance;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKeys(Key[] keys) {
        this.keys = keys;
    }

    public Key[] getKeys() {
        return keys;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        for (int i = 0; i < keys.length; i++) {
            hash = 67 * hash + keys[i].hashCode();
        }
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + (int) (this.length ^ (this.length >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TonalMusic other = (TonalMusic) obj;
        if (keys.length != other.keys.length) {
            return false;
        }
        for (int i = 0; i < keys.length; i++) {
            if (!keys[i].equals(other.keys[i])) {
                return false;
            }
        }
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getName());
        buffer.append(",");
        for (int i = 0; i < keys.length; i++) {
            buffer.append(keys[i].toString());
        }

        return buffer.toString();
    }
}
