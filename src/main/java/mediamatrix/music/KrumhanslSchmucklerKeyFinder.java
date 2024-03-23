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

/*
Krumhansl-Schmuckler key-finding algorithm (Krumhansl & Schmuckler, 1986)
Carol L. Krumhansl: Cognitive Foundations of Musical Pitch (Oxford Psychology Series)

We assume the K-S algorithm.  Input profile values represent total
duration of each pc. Key-profiles have been normalized linearly around
the average key-profile value. We normalize the input values similarly
by taking (input_prof[pc]-average_dur). Then we multiply each
normalized KP value by the normalized input value, and sum these
products; this gives us the numerator of the correlation expression
(as commented below). We've summed the squares of the normalized
key-profile value (major_sumsq and minor_sumsq above) and the
normalized input values (input_sumsq above), so this allows us to
calculate the denominator also.
 */
package mediamatrix.music;

import java.util.TreeSet;

public class KrumhanslSchmucklerKeyFinder implements KeyFinder {

    private static final double[] MAJOR_PROFILE = {6.35, 2.23, 3.48, 2.33, 4.38, 4.09, 2.52, 5.19, 2.39, 3.66, 2.29, 2.88};
    private static final double[] MINOR_PROFILE = {6.33, 2.68, 3.52, 5.38, 2.60, 3.53, 2.54, 4.75, 3.98, 2.69, 3.34, 3.17};

    static {
        double total = 0.0;
        double average;
        for (int i = 0; i < 12; i++) {
            total += MAJOR_PROFILE[i];
        }
        average = total / 12.0;
        for (int i = 0; i < 12; i++) {
            MAJOR_PROFILE[i] = MAJOR_PROFILE[i] - average;
        }

        total = 0.0;
        for (int i = 0; i < 12; i++) {
            total += MINOR_PROFILE[i];
        }
        average = total / 12.0;
        for (int i = 0; i < 12; i++) {
            MINOR_PROFILE[i] = MINOR_PROFILE[i] - average;
        }
    }

    public KrumhanslSchmucklerKeyFinder() {
    }

    @Override
    public Key[] analyze(Note[] notes) {
        int totalDuration = 0;
        double[][] keyProfile = new double[24][12];
        double[] inputProf = new double[12];

        for (int key = 0; key < 24; key++) {
            for (int pc = 0; pc < 12; pc++) {
                keyProfile[key][pc] = 0;
            }
        }
        for (int key = 0; key < 12; key++) {
            for (int pc = 0; pc < 12; pc++) {
                keyProfile[key][pc] = MAJOR_PROFILE[((pc - key) + 12) % 12];
            }
        }
        for (int key = 12; key < 24; key++) {
            for (int pc = 0; pc < 12; pc++) {
                keyProfile[key][pc] = MINOR_PROFILE[((pc - (key % 12)) + 12) % 12];
            }
        }

        for (Note note : notes) {
            totalDuration += (int) note.getLength();
            for (int y = 0; y < inputProf.length; y++) {
                if (note.getPitch() == y) {
                    inputProf[y] += note.getLength();
                }
            }
        }

        final double averageDuration = totalDuration / 12.0;
        double major_sumsq = 0.0;
        double minor_sumsq = 0.0;
        double input_sumsq = 0.0;

        for (int i = 0; i < MAJOR_PROFILE.length; i++) {
            major_sumsq += MAJOR_PROFILE[i] * MAJOR_PROFILE[i];
        }
        for (int i = 0; i < MINOR_PROFILE.length; i++) {
            minor_sumsq += MINOR_PROFILE[i] * MINOR_PROFILE[i];
        }

        for (int i = 0; i < 12; i++) {
            input_sumsq += Math.pow((inputProf[i] - averageDuration), 2.0);
        }

        final TreeSet<Key> score = new TreeSet<>();
        for (int key = 0; key < 24; key++) {
            double value = 0.0;
            for (int pc = 0; pc < 12; pc++) {
                double d = keyProfile[key][pc] * (inputProf[pc] - averageDuration);
                value += d;
            }
            if (key < 12) {
                double d = Math.sqrt(major_sumsq * input_sumsq);
                value = value / d;
            } else {
                double d = Math.sqrt(minor_sumsq * input_sumsq);
                value = value / d;
            }
            final Key result = new Key(key, value);
            score.add(result);
        }
        return score.descendingSet().toArray(new Key[score.size()]);
    }
}
