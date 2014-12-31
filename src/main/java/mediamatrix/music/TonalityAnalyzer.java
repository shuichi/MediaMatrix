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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;

public class TonalityAnalyzer {

    public TonalMusic analyze(InputStream in, int n) throws FileNotFoundException, IOException, InvalidMidiDataException {
        final MidiAnalyzer aMidiAnalyzer = new MidiAnalyzer(new BufferedInputStream(in));
        final long start = System.currentTimeMillis();
        final MusicScore aScore = aMidiAnalyzer.parse();
        aMidiAnalyzer.close();
        final TonalMusic music = new TonalMusic();
        final List<Key> keys = new ArrayList<Key>();
        aScore.divideByN(n);
        final KrumhanslSchmucklerKeyFinder aKeyFinder = new KrumhanslSchmucklerKeyFinder();
        for (int i = 1; i < aScore.divisionCount(); i++) {
            Note[] notes = aScore.division(i);
            if (notes.length > 0) {
                final Key[] keyScore = aKeyFinder.analyze(notes);
                keyScore[0].setLength(aScore.getBarLength());
                keys.add(keyScore[0]);
            }
        }
        music.setCount(aScore.divisionCount());
        music.setKeys(keys.toArray(new Key[keys.size()]));
        music.setName("");
        music.setLength(aScore.getLengthAsMicrosecond());
        final long end = System.currentTimeMillis();
        music.setPerformance(end - start);
        return music;
    }

    public TonalMusic analyze(File aFile, int n) throws FileNotFoundException, IOException, InvalidMidiDataException {
        final MidiAnalyzer aMidiAnalyzer = new MidiAnalyzer(new BufferedInputStream(new FileInputStream(aFile)));
        final long start = System.currentTimeMillis();
        final MusicScore aScore = aMidiAnalyzer.parse();
        aMidiAnalyzer.close();
        final TonalMusic music = new TonalMusic();
        final List<Key> keys = new ArrayList<Key>();
        aScore.divideByN(n);
        final KrumhanslSchmucklerKeyFinder aKeyFinder = new KrumhanslSchmucklerKeyFinder();
        for (int i = 1; i < aScore.divisionCount(); i++) {
            Note[] notes = aScore.division(i);
            if (notes.length > 0) {
                final Key[] keyScore = aKeyFinder.analyze(notes);
                keyScore[0].setLength(aScore.getBarLength());
                keys.add(keyScore[0]);
            }
        }
        music.setCount(aScore.divisionCount());
        music.setKeys(keys.toArray(new Key[keys.size()]));
        music.setName(aFile.getAbsolutePath());
        music.setLength(aScore.getLengthAsMicrosecond());
        final long end = System.currentTimeMillis();
        music.setPerformance(end - start);
        return music;
    }

    public TonalMusic analyze(File aFile) throws FileNotFoundException, IOException, InvalidMidiDataException {
        final MidiAnalyzer aMidiAnalyzer = new MidiAnalyzer(new BufferedInputStream(new FileInputStream(aFile)));
        final long start = System.currentTimeMillis();
        final MusicScore aScore = aMidiAnalyzer.parse();
        aMidiAnalyzer.close();
        final TonalMusic music = new TonalMusic();
        final List<Key> keys = new ArrayList<Key>();
        aScore.divideByBar();
        final KrumhanslSchmucklerKeyFinder aKeyFinder = new KrumhanslSchmucklerKeyFinder();
        for (int i = 1; i < aScore.divisionCount(); i++) {
            Note[] notes = aScore.division(i);
            if (notes.length > 0) {
                final Key[] keyScore = aKeyFinder.analyze(notes);
                keyScore[0].setLength(aScore.getBarLength());
                keys.add(keyScore[0]);
            }
        }
        music.setCount(aScore.divisionCount());
        music.setKeys(keys.toArray(new Key[keys.size()]));
        music.setName(aFile.getAbsolutePath());
        music.setLength(aScore.getLengthAsMicrosecond());
        final long end = System.currentTimeMillis();
        music.setPerformance(end - start);
        return music;
    }

    public Key analyzeEntire(File aFile) throws FileNotFoundException, IOException, InvalidMidiDataException {
        final MidiAnalyzer aMidiAnalyzer = new MidiAnalyzer(new BufferedInputStream(new FileInputStream(aFile)));
        final MusicScore aScore = aMidiAnalyzer.parse();
        aMidiAnalyzer.close();
        final KrumhanslSchmucklerKeyFinder aKeyFinder = new KrumhanslSchmucklerKeyFinder();
        final Key[] keyScore = aKeyFinder.analyze(aScore.allNotes());
        return keyScore[0];
    }
}
