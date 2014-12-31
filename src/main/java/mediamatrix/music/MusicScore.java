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

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MusicScore {

    private static final String[] sm_astrKeySignatures = {"Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"};
    private String source;
    private List<Note> notes;
    private List<Double> division;
    private List<Tempo> tempo;
    private double baseTempo;
    private long lengthAsTick;
    private long lengthAsMicrosecond;
    private int resolution;
    private boolean isTickPerBeat;
    private long microsecondsPerQuarterNote;
    private long smtpeOffset;
    private int timeSigElement;
    private int timeSigDenominator;
    private int clocksperMetronome;
    private int midiClock;
    private boolean isMajor;
    private int keySignature;
    private int programLSB;
    private int programMSB;
    private int programCode;

    public MusicScore() {
        notes = new ArrayList<Note>();
        tempo = new ArrayList<Tempo>();
        clearDivision();
    }

    public int getProgramCode() {
        return programCode;
    }

    public void setProgramCode(int programCode) {
        this.programCode = programCode;
    }

    public String getProgramName() {
        return programLSB + "-" + programMSB + "-" + programCode;
    }

    public int getProgramLSB() {
        return programLSB;
    }

    public void setProgramLSB(int programLSB) {
        this.programLSB = programLSB;
    }

    public int getProgramMSB() {
        return programMSB;
    }

    public void setProgramMSB(int programMSB) {
        this.programMSB = programMSB;
    }

    public void addTempo(Tempo t) {
        if (tempo.size() == 0) {
            baseTempo = t.getTempo();
        }
        tempo.add(t);
    }

    public double getBaseTempo() {
        return baseTempo;
    }

    public List<Tempo> getTempo() {
        return tempo;
    }

    public void setTempo(List<Tempo> t) {
        tempo = t;
    }

    public long getBarLength() {
        final long result = (long) ((double) microsecondsPerQuarterNote * 4.0d * ((double) timeSigElement / (double) timeSigDenominator));
        return result;
    }

    public void divideByBar() {
        final long barLength = getBarLength();
        for (int i = 1; i * barLength < lengthAsMicrosecond; i++) {
            addDivision(i * barLength);
        }
    }

    public void divideByNSecond(long microsec) {
        for (int i = 1; i * microsec <= lengthAsMicrosecond; i++) {
            addDivision(i * microsec);
        }
    }

    public double divideByN(int n) {
        double end = notes.get(notes.size() - 1).getEndTime();
        for (int i = 1; i <= n; i++) {
            addDivision(i * (end / n));
        }
        return end / n;
    }

    public List<List<Key>> analyzeFullTonalityWithNSegment(int n, KeyFinder aKeyFinder) {
        List<List<Key>> result = new ArrayList<List<Key>>();
        if (n == 1) {
            final Key[] keyScore = aKeyFinder.analyze(allNotes());
            final List<Key> keys = new ArrayList<Key>();
            result.add(keys);
            for (int j = 0; j < keyScore.length; j++) {
                keyScore[j].setLength(getLengthAsMicrosecond());
                keyScore[j].setSec(1);
                keys.add(keyScore[j]);
            }
        } else {
            double length = divideByN(n);
            int previousEnd = 0;
            for (int i = 1; i < divisionCount(); i++) {
                Note[] tempNotes = division(i);
                final Key[] keyScore = aKeyFinder.analyze(tempNotes);
                final List<Key> keys = new ArrayList<Key>();
                result.add(keys);
                for (int j = 0; j < keyScore.length; j++) {
                    keyScore[j].setLength((int) length);
                    if (tempNotes.length > 0) {
                        previousEnd = (int) tempNotes[0].getEndTime() / 1000000;
                        keyScore[j].setSec((int) (tempNotes[0].getStartTime() / 1000000));
                    } else {
                        keyScore[j].setSec(previousEnd);
                    }
                    keys.add(keyScore[j]);
                }
            }
        }
        return result;
    }

    public List<List<Key>> analyzeFullTonalityWithNsecond(long microsec, KeyFinder aKeyFinder) {
        List<List<Key>> result = new ArrayList<List<Key>>();
        divideByNSecond(microsec);
        for (int i = 1; i < divisionCount(); i++) {
            final Key[] keyScore = aKeyFinder.analyze(division(i));
            final List<Key> keys = new ArrayList<Key>();
            result.add(keys);
            for (int j = 0; j < keyScore.length; j++) {
                keyScore[j].setLength(microsec);
                keyScore[j].setSec(i * (int) (microsec / 1000000) + 1);
                keys.add(keyScore[j]);
            }
        }
        return result;
    }

    public List<List<Key>> analyzeFullTonality(KeyFinder aKeyFinder) {
        final long barLength = getBarLength();
        List<List<Key>> result = new ArrayList<List<Key>>();
        divideByBar();
        for (int i = 1; i < divisionCount(); i++) {
            final Key[] keyScore = aKeyFinder.analyze(division(i));
            final List<Key> keys = new ArrayList<Key>();
            result.add(keys);
            for (int j = 0; j < keyScore.length; j++) {
                keyScore[j].setLength(barLength);
                keyScore[j].setSec(i + 1);
                keys.add(keyScore[j]);
            }
        }
        return result;
    }

    public Key[] analyzeTonality(KeyFinder aKeyFinder) {
        final long barLength = getBarLength();
        List<Key> result = new ArrayList<Key>();
        divideByBar();
        for (int i = 1; i < divisionCount(); i++) {
            final Key[] keyScore = aKeyFinder.analyze(division(i));
            keyScore[0].setLength(barLength);
            result.add(keyScore[0]);
        }
        return result.toArray(new Key[result.size()]);
    }

    public String getName() {
        return source;
    }

    public void setName(String source) {
        this.source = source;
    }

    public String toXML() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(bos));
        encoder.writeObject(this);
        encoder.close();
        try {
            return bos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return bos.toString();
        }
    }

    public byte[] toXMLBytes() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(bos));
        encoder.writeObject(this);
        encoder.close();
        return bos.toByteArray();
    }

    public Note[] getNotes() {
        Note[] temp = new Note[notes.size()];
        return notes.toArray(temp);
    }

    public void setNotes(Note[] newNotes) {
        notes = new ArrayList<Note>();
        for (int i = 0; i < newNotes.length; i++) {
            notes.add(newNotes[i]);
        }
    }

    public double[] getDividers() {
        double[] temp = new double[division.size()];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = division.get(i).doubleValue();
        }
        return temp;
    }

    public void setDividers(double[] newDividers) {
        division = new ArrayList<Double>();
        for (int i = 0; i < newDividers.length; i++) {
            division.add(new Double(newDividers[i]));
        }
    }

    public long getLengthAsTick() {
        return lengthAsTick;
    }

    public void setLengthAsTick(long tick) {
        lengthAsTick = tick;
    }

    public long getLengthAsMicrosecond() {
        return lengthAsMicrosecond;
    }

    public void setLengthAsMicrosecond(long micsec) {
        lengthAsMicrosecond = micsec;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public double getUnitTime() {
        return getLengthAsMicrosecond() / getLengthAsTick();
    }

    public int divisionCount() {
        return division.size() + 1;
    }

    public Note[] allNotes() {
        return notes.toArray(new Note[notes.size()]);
    }

    public Note[] division(int number) {
        if (division.size() == 0) {
            return notes.toArray(new Note[notes.size()]);
        }

        if (division.size() < number) {
            throw new IndexOutOfBoundsException(number + " is over division count");
        }

        double begin = 0;
        if (number >= 1) {
            begin = division.get(number - 1).doubleValue();
        }

        double end = (double) getLengthAsMicrosecond();
        if (number < division.size()) {
            end = division.get(number).doubleValue();
        }
        return range(begin, end);
    }

    public Note[] range(double begin, double end) {
        List<Note> result = new ArrayList<Note>();
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            if (note.getStartTime() > begin && note.getStartTime() < end) {
                result.add(note);
            }
        }
        return result.toArray(new Note[result.size()]);
    }

    public void addDivision(double time) {
        division.add(new Double(time));
    }

    public void clearDivision() {
        division = new ArrayList<Double>();
    }

    public void addNote(Note note) {
        if (note != null) {
            this.notes.add(note);
        }
    }

    public void getNote(int index) {
        this.notes.get(index);
    }

    public int getKeySignature() {
        return keySignature;
    }

    public void setKeySignature(int key) {
        keySignature = key;
    }

    public void setMicrosecondsPerQuarterNote(long sec) {
        this.microsecondsPerQuarterNote = sec;
    }

    public long getMicrosecondsPerQuarterNote() {
        return microsecondsPerQuarterNote;
    }

    public long getSMTPEOffset() {
        return smtpeOffset;
    }

    public void setSMTPEOffset(long offset) {
        smtpeOffset = offset;
    }

    public int getTimeSigElement() {
        return timeSigElement;
    }

    public void setTimeSigElement(int element) {
        timeSigElement = element;
    }

    public int getTimeSigDenominator() {
        return timeSigDenominator;
    }

    public void setTimeSigDenominator(int denomi) {
        timeSigDenominator = denomi;
    }

    public int getClocksperMetronome() {
        return clocksperMetronome;
    }

    public void setClocksperMetronome(int clock) {
        clocksperMetronome = clock;
    }

    public int getMidiClock() {
        return midiClock;
    }

    public void setMidiClock(int clock) {
        midiClock = clock;
    }

    public boolean getGender() {
        return isMajor;
    }

    public void setGender(boolean gender) {
        isMajor = gender;
    }

    public String getTonality() {
        String strGender = null;
        if (isMajor) {
            strGender = "major";
        } else {
            strGender = "minor";
        }
        return sm_astrKeySignatures[getKeySignature()] + " " + strGender;
    }

    @Override
    public String toString() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bos);
        writer.println("Microseconds per Quarter-Note: " + microsecondsPerQuarterNote);
        writer.println("Time Signature: " + timeSigElement + "/" + timeSigDenominator + ", MIDI clocks per metronome tick: " + clocksperMetronome + ", 1/32 per 24 MIDI clocks: " + midiClock);
        String strGender = null;
        if (isMajor) {
            strGender = "major";
        } else {
            strGender = "minor";
        }
        writer.println("Key Signature: " + sm_astrKeySignatures[getKeySignature()] + " " + strGender);
        writer.println("------------------------------");
        for (int i = 0; i < notes.size(); i++) {
            writer.println(notes.get(i));
        }
        writer.flush();
        return bos.toString();
    }
}
