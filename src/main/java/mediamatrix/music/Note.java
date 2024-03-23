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
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Note implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String[] sm_astrKeyNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private int pitch;
    private int octave;
    private double startTime;
    private double length;
    private int velocity;

    public Note() {
        super();
    }

    public Note(int pitch, int octave, int velocity, double startTime, double length) {
        this();
        this.pitch = pitch;
        this.octave = octave;
        this.velocity = velocity;
        this.startTime = startTime;
        this.length = length;
    }

    public Note(int keyNumber, int velocity, double startTime, double length) {
        this();
        if (keyNumber > 127) {
            throw new RuntimeException("invalid key number " + keyNumber);
        }
        this.pitch = keyNumber % 12;
        this.octave = (keyNumber / 12) - 1;
        this.velocity = velocity;
        this.startTime = startTime;
        this.length = length;
    }

    public String getPitchName() {
        return sm_astrKeyNames[pitch] + octave;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getOctave() {
        return octave;
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }

    public int getKeyNumber() {
        return ((octave + 1) * 12) + pitch;
    }

    public void setKeyNumber(int keyNumber) {
        this.pitch = keyNumber % 12;
        this.octave = (keyNumber / 12) - 1;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getEndTime() {
        return startTime + length;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setEndTime(double endTime) {
        length = endTime - startTime;
    }

    @Override
    public String toString() {
        return "[" + new BigDecimal(startTime).setScale(0, RoundingMode.HALF_UP) + "] Note " + getPitchName() + " velocity: " + velocity + " duration: " + length;
    }
}
