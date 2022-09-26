/* MediaMatrix -- A Programable Database Engine for Multimedia
 * Copyright (C) 2008-2010 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mediamatrix.db;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;
import mediamatrix.gui.ImageShot;
import mediamatrix.io.ChronoArchiveFileFilter;
import mediamatrix.io.ImageFilenameFilter;
import mediamatrix.io.MIDIFileFilter;
import mediamatrix.munsell.ColorHistogram;
import mediamatrix.munsell.ColorImpressionDataStore;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.munsell.Correlation;
import mediamatrix.music.MidiAnalyzer;
import mediamatrix.music.Note;
import mediamatrix.music.MusicScore;
import mediamatrix.music.Tempo;
import mediamatrix.utils.IOUtilities;
import mediamatrix.utils.Score;

public class PrimitiveEngine {

    protected Properties params;

    public PrimitiveEngine() {
        params = new Properties();
        try {
            params.load(getClass().getResourceAsStream("/mediamatrix/properties/defaults.properties"));
        } catch (IOException ignored) {
        }
    }

    public synchronized Object setProperty(String key, String value) {
        return params.setProperty(key, value);
    }

    public synchronized String getProperty(String key) {
        return params.getProperty(key);
    }

    public List<ImageShot> getImageShotList(MediaMatrix mat, double threshold) throws IOException {
        final ChronoArchive carc = new ChronoArchive(mat.getId());
        final List<Double> sumList = new ArrayList<Double>();
        for (int i = 0; i < mat.getHeight(); i++) {
            double sum = 0d;
            for (int j = 0; j < mat.getWidth(); j++) {
                sum += (mat.get(i, j) * mat.get(i, j));
            }
            sum = Math.sqrt(sum);
            sumList.add(sum);
        }
        double totalSum = 0d;
        for (Double d : sumList) {
            totalSum += d;
        }
        final double average = totalSum / sumList.size();
        final List<ImageShot> result = new ArrayList<ImageShot>();
        for (int i = 0; i < sumList.size(); i++) {
            if (sumList.get(i) > average * threshold) {
                result.add(new ImageShot(i, carc.getImage(i)));
            }
        }
        return result;
    }

    public MediaDataObject[] openDB(String dirName) throws IOException {
        List<MediaDataObject> result = new ArrayList<MediaDataObject>();
        if (new File(dirName).exists() && new File(dirName).isDirectory()) {
            final File dir = new File(dirName);
            File[] files = dir.listFiles(new ChronoArchiveFileFilter());
            if (files.length == 0) {
                files = dir.listFiles(new MIDIFileFilter());
            }
            if (files.length == 0) {
                files = dir.listFiles(new ImageFilenameFilter());
            }
            for (int i = 0; i < files.length; i++) {
                result.add(new MediaDataObject(files[i].getAbsolutePath()));
            }
        }
        return result.toArray(new MediaDataObject[result.size()]);
    }

    public MediaMatrix open(MediaDataObject obj, String matrixName) throws IOException, MediaMatrixException {
        return open(obj.getId(), matrixName);
    }

    public MediaMatrix open(String fileName, String matrixName) throws IOException, MediaMatrixException {
        MediaMatrix result = null;
        if (fileName.startsWith("http://")) {
            // TODO support other media types
            result = convert(ImageIO.read(new URL(fileName)));
        } else if (fileName.contains(",")) {
            final String[] names = fileName.split(",");
            final List<File> temp = new ArrayList<File>();
            for (int i = 0; i < names.length; i++) {
                final File f = new File(names[i].trim());
                if (f.exists()) {
                    temp.add(f);
                }
            }
            final File[] files = temp.toArray(new File[temp.size()]);
            final ImageFilenameFilter imgFilter = new ImageFilenameFilter();
            final List<BufferedImage> images = new ArrayList<BufferedImage>();
            for (int i = 0; i < files.length; i++) {
                if (imgFilter.accept(files[i].getParentFile(), files[i].getName())) {
                    images.add(ImageIO.read(files[i]));
                }
            }
            result = convert(images.toArray(new BufferedImage[images.size()]));
        } else {
            final File file = new File(fileName);
            if (new MIDIFileFilter().accept(file.getParentFile(), file.getName())) {
                result = openMusic(fileName, matrixName);
            } else if (new ChronoArchiveFileFilter().accept(file.getParentFile(), file.getName())) {
                if (matrixName.equalsIgnoreCase("ColorImpression")) {
                    result = ChronoArchive.readMatrix(file);
                } else if (matrixName.equalsIgnoreCase("ColorHistogram")) {
                    result = ChronoArchive.readColorMatrix(file);
                }
            } else if (new ImageFilenameFilter().accept(file.getParentFile(), file.getName())) {
                result = convert(ImageIO.read(file));
            }
        }
        return result;
    }

    public MediaMatrix openMusic(InputStream in, String filename, String matrixName) throws IOException, MediaMatrixException {
        try {
            final double denomi = Double.parseDouble(params.getProperty("DENOMI"));
            final double ratio = Double.parseDouble(params.getProperty("RATIO"));
            final MidiAnalyzer analyzer = new MidiAnalyzer(new BufferedInputStream(in));
            MediaMatrix result = null;
            MusicScore score = null;
            if (params.getProperty("TRACK") != null) {
                score = analyzer.parseMultiTrack()[Integer.parseInt(params.getProperty("TRACK"))];
            } else if (params.getProperty("TRACKS") != null) {
                String[] nums = params.getProperty("TRACKS").split(",");
                int[] tracks = new int[nums.length];
                for (int i = 0; i < nums.length; i++) {
                    tracks[i] = Integer.parseInt(nums[i]);
                }
                score = analyzer.parseSpecificTracks(tracks);
            } else {
                score = analyzer.parse();
            }
            analyzer.close();
            score.setName(filename);
            double barLength = 0d;
            if (params.getProperty("DIV_MODE").equals("RELATIVE")) {
                barLength = Math.ceil(score.getLengthAsMicrosecond() / (1000d * denomi) / ratio);
            } else {
                barLength = Math.ceil(score.getBarLength() / (1000d * denomi)) * ratio;
            }
            if (barLength < 1.0) {
                barLength = 180.0 * ratio;
            }
            final MediaMatrix mat = convertPitch(score, denomi);
            if (matrixName.equalsIgnoreCase("Tonality")) {
                if (params.getProperty("START") != null && params.getProperty("END") != null) {
                    final int start = Integer.parseInt(params.getProperty("START"));
                    final int end = Integer.parseInt(params.getProperty("END"));
                    result = tonality(select(mat, start, end), barLength);
                } else {
                    result = tonality(mat, barLength);
                }
            } else if (matrixName.equalsIgnoreCase("Pitch")) {
                result = mat;
            }
            return result;
        } catch (InvalidMidiDataException ex) {
            throw new MediaMatrixException(ex);
        }
    }

    public MediaMatrix openMusic(String filename, String matrixName) throws IOException, MediaMatrixException {
        return openMusic(new FileInputStream(filename), filename, matrixName);
    }

    public MediaMatrix tonality(MediaMatrix mat, double barLength) {
        final MediaMatrix result = tonality(segment(twelveTone(mat), barLength));
        return result;
    }

    public MediaMatrix asIs(MediaMatrix mat) {
        return mat;
    }

    public MediaMatrix transmit(MediaMatrix mat, Properties props) throws IOException {
        final Map<String, double[]> vectors = new TreeMap<String, double[]>();
        final Set<Object> propKeys = props.keySet();
        for (Object k : propKeys) {
            final String[] elems = props.getProperty(k.toString()).split(",");
            final double[] defs = new double[elems.length];
            for (int i = 0; i < defs.length; i++) {
                defs[i] = Double.parseDouble(elems[i]);
            }
            vectors.put(k.toString(), defs);
        }
        final Set<String> keys = vectors.keySet();
        final MediaMatrix result = new MediaMatrix(mat.getRows(), keys.toArray(new String[keys.size()]));
        for (int i = 0; i < mat.getHeight(); i++) {
            for (String key : keys) {
                double sum = 0;
                double[] vec = vectors.get(key);
                for (int j = 0; j < mat.getWidth(); j++) {
                    sum += (vec[j] * mat.get(i, j));
                }
                result.set(result.getRow(i), key, sum);
            }
        }
        return result;
    }

    public MediaMatrix minus(MediaMatrix mat1, MediaMatrix mat2) {
        final MediaMatrix result = new MediaMatrix(mat1.getRows(), mat1.getColumns());
        for (int i = 0; i < mat1.getHeight(); i++) {
            for (int j = 0; j < mat1.getWidth(); j++) {
                result.set(mat1.getRow(i), mat1.getColumn(j), mat1.get(i, j) - mat2.get(i, j));
            }
        }
        return result;
    }

    public MediaMatrix integrate(String[] columns, MediaMatrix[] matrices) {
        final MediaMatrix result = new MediaMatrix(matrices[0].getRows(), columns);
        for (int i = 0; i < matrices.length; i++) {
            for (int j = 0; j < matrices[i].getHeight(); j++) {
                result.set(matrices[i].getRow(j), columns[i], matrices[i].get(j, 0));
            }
        }
        return result;
    }

    public MediaMatrix plus(MediaMatrix mat[]) {
        final MediaMatrix result = new MediaMatrix(mat[0].getRows(), mat[0].getColumns());
        for (int k = 0; k < mat.length; k++) {
            for (int i = 0; i < mat[k].getHeight(); i++) {
                for (int j = 0; j < mat[k].getWidth(); j++) {
                    result.set(result.getRow(i), result.getColumn(j), result.get(i, j) + mat[k].get(i, j));
                }
            }
        }
        return result;
    }

    public MediaMatrix divide(MediaMatrix mat1, MediaMatrix mat2) {
        final MediaMatrix result = new MediaMatrix(mat1.getRows(), mat1.getColumns());
        for (int i = 0; i < mat1.getHeight(); i++) {
            for (int j = 0; j < mat1.getWidth(); j++) {
                if (mat2.get(i, j) != 0) {
                    result.set(mat1.getRow(i), mat1.getColumn(j), mat1.get(i, j) / mat2.get(i, j));
                }
            }
        }
        return result;
    }

    public MediaMatrix tonality(MediaMatrix mat) {
        final MediaMatrix result = new MediaMatrix(mat.getRows(), new String[]{"C", "Am", "G", "Em", "D", "Bm", "A", "F#m", "E", "C#m", "B", "G#m", "F#", "Ebm", "Db", "Bbm", "Ab", "Fm", "Eb", "Cm", "Bb", "Gm", "F", "Dm"});
        final double[] MAJOR_PROFILE = {6.35, 2.23, 3.48, 2.33, 4.38, 4.09, 2.52, 5.19, 2.39, 3.66, 2.29, 2.88};
        final double[] MINOR_PROFILE = {6.33, 2.68, 3.52, 5.38, 2.60, 3.53, 2.54, 4.75, 3.98, 2.69, 3.34, 3.17};
        double total = 0.0;
        double average = 0.0;
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
        for (int i = 0; i < mat.getHeight(); i++) {
            int totalDuration = 0;
            final double[][] keyProfile = new double[24][12];
            final double[] inputProf = new double[12];
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
            for (int j = 0; j < mat.getWidth(); j++) {
                totalDuration += mat.get(i, j);
                inputProf[j] += mat.get(i, j);

            }

            final double averageDuration = totalDuration / 12.0;
            double major_sumsq = 0.0;
            double minor_sumsq = 0.0;
            double input_sumsq = 0.0;
            for (int j = 0; j < 12; j++) {
                minor_sumsq += MINOR_PROFILE[j] * MINOR_PROFILE[j];
                major_sumsq += MAJOR_PROFILE[j] * MAJOR_PROFILE[j];
                input_sumsq += Math.pow((inputProf[j] - averageDuration), 2.0);
            }

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

                String keyName;
                if (key == 0 || key == 12) {
                    keyName = "C";
                } else if (key == 1 || key == 2 || key == 13 || key == 14) {
                    keyName = "D";
                } else if (key == 3 || key == 4 || key == 15 || key == 16) {
                    keyName = "E";
                } else if (key == 5 || key == 6 || key == 17 || key == 18) {
                    keyName = "F";
                } else if (key == 7 || key == 19) {
                    keyName = "G";
                } else if (key == 8 || key == 9 || key == 20 || key == 21) {
                    keyName = "A";
                } else {
                    keyName = "B";
                }
                if (key == 1 || key == 13 || key == 3 || key == 15 || key == 8 || key == 20 || key == 10 || key == 22) {
                    keyName += "b";
                }
                if (key == 6 || key == 18) {
                    keyName += "#";
                }
                if (key >= 12) {
                    keyName += "m";
                }
                if (keyName.equals("Dbm")) {
                    keyName = "C#m";
                }
                if (keyName.equals("Abm")) {
                    keyName = "G#m";
                }
                if (Double.toString(value).equals("NaN")) {
                    value = 0d;
                }
                result.set(result.getRow(i), keyName, value);
            }
        }
        return result;
    }

    public MediaMatrix rowMax(MediaMatrix mat) {
        final MediaMatrix result = new MediaMatrix(mat.getRows(), new String[]{"score"});
        for (int i = 0; i < mat.getHeight(); i++) {
            double max = 0;
            for (int j = 0; j < mat.getWidth(); j++) {
                if (max < mat.get(i, j)) {
                    max = mat.get(i, j);
                }
            }
            result.set(result.getRow(i), "score", max);
        }
        return result;
    }

    public MediaMatrix position(MediaMatrix mat, double max, double min, double avg) {
        final MediaMatrix result = new MediaMatrix(mat.getRows(), new String[]{"score"});
        for (int i = 0; i < mat.getHeight(); i++) {
            if (mat.get(i, 0) == 0) {
                result.set(result.getRow(i), "score", 0d);
            } else if (mat.get(i, 0) - avg >= 0) {
                result.set(result.getRow(i), "score", (mat.get(i, 0) - avg) / (max - avg));
            } else {
                result.set(result.getRow(i), "score", (mat.get(i, 0) - avg) / (avg - max));
            }
        }
        return result;
    }

    public MediaMatrix pitchNumber(MediaMatrix mat) {
        final MediaMatrix result = new MediaMatrix(mat.getRows(), new String[]{"pitch", "length"});
        for (int i = 0; i < mat.getHeight(); i++) {
            double length = 0;
            int pitch = 0;
            for (int j = 0; j < mat.getWidth(); j++) {
                if (mat.get(i, j) > 0) {
                    pitch = j + 1;
                    length = mat.get(i, j);
                }
            }
            result.set(result.getRow(i), "pitch", (double) pitch);
            result.set(result.getRow(i), "length", length);
        }
        return result;
    }

    public MediaMatrix selectHighestPitch(MediaMatrix mat) {
        final MediaMatrix result = new MediaMatrix(mat.getRows(), mat.getColumns());
        for (int i = 0; i < result.getHeight(); i++) {
            int max = 0;
            double value = 0d;
            for (int j = 0; j < result.getWidth(); j++) {
                if (mat.get(result.getRow(i), result.getColumn(j)) > 0) {
                    max = j;
                    value = mat.get(result.getRow(i), result.getColumn(j));
                }
            }
            result.set(result.getRow(i), result.getColumn(max), value);
        }
        return result;
    }

    public MediaMatrix distribute(MediaMatrix mat1, MediaMatrix mat2) {
        final MediaMatrix result = new MediaMatrix(mat1.getRows(), mat2.getColumns());
        for (int i = 0; i < mat2.getHeight(); i++) {
            for (int j = 0; j < mat2.getWidth(); j++) {
                if (mat2.get(i, j) > 0) {
                    double sum = mat2.get(i, j);
                    for (double k = 0; k < sum && mat2.getRow(i) + k <= mat1.getRow(mat1.getHeight() - 1); k++) {
                        result.set(mat2.getRow(i) + k, mat2.getColumn(j), 1d);
                    }
                }
            }
        }
        return result;
    }

    public MediaMatrix rangeSet(MediaMatrix mat1, MediaMatrix mat2) {
        final MediaMatrix result = new MediaMatrix(mat1.getRows(), mat2.getColumns());
        for (int i = 0; i < mat2.getHeight(); i++) {
            for (int j = 0; j < mat2.getWidth(); j++) {
                if (mat2.get(i, j) > 0) {
                    int start = 0;
                    for (int k = 0; k < mat1.getHeight() && mat1.getRow(k) <= mat2.getRow(i); k++) {
                        start = k;
                    }
                    int end = 0;
                    if (i < mat2.getHeight() - 1) {
                        for (int k = 0; k < mat1.getHeight() && mat1.getRow(k) <= mat2.getRow(i + 1); k++) {
                            end = k;
                        }
                    } else {
                        end = mat1.getHeight() - 1;
                    }
                    for (int k = start; k <= end; k++) {
                        result.set(mat1.getRow(k), mat2.getColumn(j), mat2.get(i, j));
                    }
                }
            }
        }
        return result;
    }

    public MediaMatrix segment(MediaMatrix mat, double length) {
        if (mat.getHeight() == 0) {
            return mat;
        }
        int count = 0;
        for (double i = mat.getRow(0); i < mat.getRow(mat.getHeight() - 1); i += length) {
            count++;
        }
        final double[] row = new double[count];
        count = 0;
        for (double i = mat.getRow(0); i < mat.getRow(mat.getHeight() - 1); i += length) {
            row[count++] = i + length;
        }
        final MediaMatrix result = new MediaMatrix(row, mat.getColumns());
        for (double i = mat.getRow(0); i < mat.getRow(mat.getHeight() - 1); i += length) {
            int rowStart = 0;
            for (int j = 0; j < mat.getHeight() && mat.getRow(j) <= i; j++) {
                rowStart = j;
            }
            for (int j = 0; j < mat.getWidth(); j++) {
                double sum = 0;
                for (int k = rowStart; k < mat.getHeight() && mat.getRow(k) < i + length; k++) {
                    sum += mat.get(k, j);
                }
                result.set(i + length, mat.getColumn(j), sum);
            }
        }
        return result;
    }

    public MediaMatrix rangeSum(MediaMatrix mat, double start, double end) {
        final MediaMatrix result = new MediaMatrix(new double[]{end}, mat.getColumns());
        int rowStart = 0;
        for (int i = 0; i < mat.getHeight() && mat.getRow(i) <= start; i++) {
            rowStart = i;
        }
        for (int j = 0; j < mat.getWidth(); j++) {
            double sum = 0;
            for (int i = rowStart; i < mat.getHeight() && mat.getRow(i) < end; i++) {
                sum += mat.get(i, j);
            }
            result.set(end, mat.getColumn(j), sum);
        }
        return result;
    }

    public MediaMatrix continuity(MediaMatrix mat) {
        final MediaMatrix result = new MediaMatrix(mat.getRows(), mat.getColumns());
        int sum = 0;
        for (int i = 0; i < mat.getHeight() - 1; i++) {
            boolean flag = true;
            for (int j = 0; j < mat.getWidth(); j++) {
                if (mat.get(i, j) != mat.get(i + 1, j)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                sum++;
            } else {
                for (int j = 0; j < mat.getWidth(); j++) {
                    if (mat.get(i, j) > 0) {
                        result.set(mat.getRow(i - sum), mat.getColumn(j), mat.get(i, j) + sum);
                    }
                }
                sum = 0;
            }
        }
        return removeZeroRow(result);
    }

    public MediaMatrix verticalGain(MediaMatrix mat, int gain) {
        final String[] column = mat.getColumns();
        final double[] row = mat.getRows();
        final MediaMatrix result = new MediaMatrix(row, column);
        for (int i = 0; i < mat.getHeight(); i++) {
            for (int j = 0; j < mat.getWidth(); j++) {
                if (mat.get(i, j) > 0) {
                    for (int k = 0; k <= gain; k++) {
                        double d = row[k];
                        result.set(result.getRow(i + k), result.getColumn(j), 1.0d);
                        if (i - k >= 0) {
                            result.set(result.getRow(i - k), result.getColumn(j), 1.0d);
                        }
                    }
                }
            }
        }
        return result;
    }

    public MediaMatrix select(MediaMatrix mat, int begin, int end) {
        double[] originalRows = mat.getRows();
        double[] rows = new double[end - begin];
        int index = 0;
        for (int i = begin; i < end; i++) {
            rows[index++] = originalRows[i];
        }
        final MediaMatrix result = new MediaMatrix(rows, mat.getColumns());
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < result.getWidth(); j++) {
                result.set(rows[i], result.getColumn(j), mat.get(rows[i], result.getColumn(j)));
            }
        }
        return result;
    }

    public MediaMatrix select(MediaMatrix mat, List<Double> rows) {
        final MediaMatrix result = new MediaMatrix(mat.getRows(), mat.getColumns());
        for (int i = 0; i < rows.size(); i++) {
            for (int j = 0; j < result.getWidth(); j++) {
                result.set(rows.get(i), result.getColumn(j), mat.get(rows.get(i), result.getColumn(j)));
            }
        }
        return result;
    }

    public List<String> selectAttributesMoreThan(MediaMatrix mat, String type, double t) {
        final List<String> attrs = new ArrayList<String>();
        if ("average".equalsIgnoreCase(type)) {
            double sum = 0d;
            for (int i = 0; i < mat.getWidth(); i++) {
                sum += mat.get(0, i);
            }
            t = t * (sum / mat.getWidth());
        }
        for (int i = 0; i < mat.getWidth(); i++) {
            if (mat.get(0, i) >= t) {
                attrs.add(mat.getColumn(i));
            }
        }
        return attrs;
    }

    public List<String> selectAttributesLessThan(MediaMatrix mat, String type, double t) {
        final List<String> attrs = new ArrayList<String>();
        if ("average".equalsIgnoreCase(type)) {
            double sum = 0d;
            for (int i = 0; i < mat.getWidth(); i++) {
                sum += mat.get(0, i);
            }
            t = t * (sum / mat.getWidth());
        }
        for (int i = 0; i < mat.getWidth(); i++) {
            if (mat.get(0, i) <= t) {
                attrs.add(mat.getColumn(i));
            }
        }
        return attrs;
    }

    public MediaMatrix removeZeroRow(MediaMatrix mat) {
        final List<Double> tempRow = new LinkedList<Double>();
        for (int i = 0; i < mat.getHeight(); i++) {
            double sum = 0;
            for (int j = 0; j < mat.getWidth(); j++) {
                sum += mat.get(i, j);
            }
            if (sum > 0) {
                tempRow.add(mat.getRow(i));
            }
        }
        final double[] row = new double[tempRow.size()];
        for (int i = 0; i < row.length; i++) {
            row[i] = tempRow.get(i);
        }
        final MediaMatrix result = new MediaMatrix(row, mat.getColumns());
        for (int i = 0; i < result.getHeight(); i++) {
            for (int j = 0; j < result.getWidth(); j++) {
                result.set(result.getRow(i), result.getColumn(j), mat.get(result.getRow(i), result.getColumn(j)));
            }
        }
        return result;
    }

    public MediaMatrix twelveTone(MediaMatrix mat) {
        final String[] column = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        final MediaMatrix result = new MediaMatrix(mat.getRows(), column);
        for (int i = 0; i < mat.getHeight(); i++) {
            for (int j = 0; j < mat.getWidth(); j++) {
                if (mat.get(i, j) > 0) {
                    result.set(result.getRow(i), result.getColumn(j % 12), 1.0d);
                }
            }
        }
        return result;
    }

    public MediaMatrix convertTempo(MusicScore score, double denomi) {
        MediaMatrix mat = null;
        final List<Tempo> tempo = score.getTempo();
        final double[] tempRow = new double[tempo.size()];
        for (int j = 0; j < tempo.size(); j++) {
            tempRow[j] = Math.ceil(tempo.get(j).getTime() / (1000d * denomi));
        }
        mat = new MediaMatrix(tempRow, new String[]{"tempo"});
        for (int j = 0; j < tempo.size(); j++) {
            final Tempo t = tempo.get(j);
            final double time = Math.ceil(t.getTime() / (1000d * denomi));
            final double value = t.getTempo() / (1000d * denomi);
            mat.set(time, "tempo", value);
        }
        return mat;
    }

    public MediaMatrix convertPitch(MusicScore score, double denomi) {
        final int millisec = (int) Math.ceil(score.getLengthAsMicrosecond() / (1000d * denomi));
        final String[] column = new String[128];
        for (int j = 0; j < column.length; j++) {
            column[j] = "p" + (j + 1);
        }
        final double[] noteRow = new double[millisec];
        for (int j = 0; j < noteRow.length; j++) {
            noteRow[j] = (double) j;
        }
        final MediaMatrix mat = new MediaMatrix(noteRow, column);
        final Note[] notes = score.allNotes();
        for (int j = 0; j < notes.length; j++) {
            if (notes[j].getVelocity() > 0) {
                for (double k = 0; k < notes[j].getLength(); k += (1000d * denomi)) {
                    final double time = Math.ceil((k + notes[j].getStartTime()) / (1000d * denomi));
                    if (mat.containRow(time)) {
                        mat.set(time, "p" + notes[j].getKeyNumber(), 1d);
                    } else {
                        System.err.println(mat.getId() + " does not contain row[" + time + "]");
                    }
                }
            }
        }
        return mat;
    }

    public MediaMatrix convertContinuousPitch(MusicScore score, double denomi) {
        final Note[] notes = score.allNotes();
        final TreeSet<Double> times = new TreeSet<Double>();
        for (int i = 0; i < notes.length; i++) {
            Note note = notes[i];
            times.add(note.getStartTime() / (1000d * denomi));
        }
        final double[] noteRow = new double[times.size()];
        int t = 0;
        for (Iterator<Double> it = times.iterator(); it.hasNext();) {
            noteRow[t++] = it.next();
        }
        final String[] column = new String[128];
        for (int i = 0; i < column.length; i++) {
            column[i] = "p" + (i + 1);
        }
        final MediaMatrix pmat = new MediaMatrix(noteRow, column);
        for (int i = 0; i < notes.length; i++) {
            if (notes[i].getVelocity() > 0) {
                pmat.set(notes[i].getStartTime() / (1000d * denomi), "p" + notes[i].getKeyNumber(), notes[i].getLength());
            }
        }
        return pmat;
    }

    public MediaMatrix convertContinuousTempo(MusicScore score, double denomi) {
        final List<Tempo> tempo = score.getTempo();
        final double[] tempRow = new double[tempo.size()];
        for (int i = 0; i < tempo.size(); i++) {
            Tempo t = tempo.get(i);
            tempRow[i] = t.getTime() / (1000d * denomi);
        }
        final MediaMatrix tmat = new MediaMatrix(tempRow, new String[]{"tempo"});
        for (int i = 0; i < tempo.size(); i++) {
            Tempo t = tempo.get(i);
            double time = t.getTime() / (1000d * denomi);
            double value = ((t.getTempo() - score.getBaseTempo()) * -1) / (1000d * denomi);
            tmat.set(time, "tempo", value);
        }
        return tmat;
    }

    public MediaMatrix convertContinuousVelocity(MusicScore score, double denomi) {
        final Note[] notes = score.allNotes();
        final TreeSet<Double> times = new TreeSet<Double>();
        for (int i = 0; i < notes.length; i++) {
            Note note = notes[i];
            times.add(note.getStartTime() / (1000d * denomi));
        }
        final double[] noteRow = new double[times.size()];
        int t = 0;
        for (Iterator<Double> it = times.iterator(); it.hasNext();) {
            noteRow[t++] = it.next();
        }
        final MediaMatrix vmat = new MediaMatrix(noteRow, new String[]{"velocity"});
        for (int i = 0; i < notes.length; i++) {
            vmat.set(notes[i].getStartTime() / (1000d * denomi), "velocity", notes[i].getVelocity());
        }
        return vmat;
    }

    public MediaMatrix keywords(String keywords) throws IOException {
        final String[] keys = keywords.split(",");
        for (int i = 0; i < keys.length; i++) {
            keys[i] = keys[i].trim();
        }
        return convert(keys);
    }

    public MediaMatrix add(MediaMatrix mat1, MediaMatrix mat2) {
        final MediaMatrix result = new MediaMatrix(mat1.getRows(), mat1.getColumns());
        for (int i = 0; i < mat1.getHeight(); i++) {
            for (int j = 0; j < mat1.getWidth(); j++) {
                double value = mat1.get(i, j) + mat2.get(i, j);
                result.set(mat1.getRow(i), mat1.getColumn(j), value);
            }
        }
        return result;
    }

    public MediaMatrix union(String filenames) throws IOException {
        final String[] keys = filenames.split(",");
        for (int i = 0; i < keys.length; i++) {
            keys[i] = keys[i].trim();
        }
        final BufferedImage[] images = new BufferedImage[keys.length];
        for (int i = 0; i < images.length; i++) {
            images[i] = ImageIO.read(new File(keys[i]));
        }
        return convert(images);
    }

    public ColorImpressionKnowledge getColorImpressionKnowledge() throws IOException {
        final String res = getProperty(CXMQLParameterNames.COLORSCHEME);
        ColorImpressionKnowledge ci = ColorImpressionDataStore.getColorImpressionKnowledge(res);
        if (ci == null) {
            if (res.startsWith("http://")) {
                byte[] buff = IOUtilities.download(new URL(res));
                ci = new ColorImpressionKnowledge();
                ci.load(new ByteArrayInputStream(buff), "UTF-8");
            } else if (new File(res).exists()) {
                ci = new ColorImpressionKnowledge();
                ci.load(new BufferedInputStream(new FileInputStream(new File(res))), "UTF-8");
            }
            if (ci != null) {
                ColorImpressionDataStore.registerRemoteColorImpressionKnowledge(res, ci);
            }
        }
        return ci;
    }

    public MediaMatrix convert(String[] keywords) throws IOException {
        final ColorImpressionKnowledge ci = getColorImpressionKnowledge();
        final double[] row = new double[1];
        row[0] = 0.0d;
        final MediaMatrix mat = new MediaMatrix(row, ci.getWords());
        for (int i = 0; i < keywords.length; i++) {
            mat.set(0d, keywords[i], 1d);
        }
        return mat;
    }

    public MediaMatrix convert(BufferedImage[] images) throws IOException {
        final double[] row = new double[images.length];
        for (int i = 0; i < row.length; i++) {
            row[i] = i;
        }
        final ColorImpressionKnowledge ci = getColorImpressionKnowledge();
        final MediaMatrix mat = new MediaMatrix(row, ci.getWords());
        for (int i = 0; i < row.length; i++) {
            final ColorHistogram histogram = ci.generateHistogram(images[i]);
            final Correlation[] correlations = ci.generateMetadata(histogram);
            for (Correlation correlation : correlations) {
                mat.set(row[i], correlation.getWord(), correlation.getValue());
            }
        }
        return mat;
    }

    public MediaMatrix convert(BufferedImage image) throws IOException {
        final double[] row = new double[]{0d};
        final ColorImpressionKnowledge ci = getColorImpressionKnowledge();
        final MediaMatrix mat = new MediaMatrix(row, ci.getWords());
        final ColorHistogram histogram = ci.generateHistogram(image);
        final Correlation[] correlations = ci.generateMetadata(histogram);
        for (Correlation correlation : correlations) {
            mat.set(0d, correlation.getWord(), correlation.getValue());
        }
        return mat;
    }

    public MediaMatrix any() {
        return new MediaMatrix();
    }

    public double zero() {
        return 0d;
    }

    public MediaMatrix weight1(MediaMatrix mat, MediaMatrix ignored, MediaMatrix vec) {
        return weight1(mat, vec);
    }

    public MediaMatrix mult(MediaMatrix mat, MediaMatrix vec) {
        final MediaMatrix result = new MediaMatrix(mat.getRows(), mat.getColumns());
        result.setId(mat.getId());
        for (int i = 0; i < mat.getHeight(); i++) {
            for (int j = 0; j < mat.getWidth(); j++) {
                double value = mat.get(i, j) * vec.get(0, j);
                if (Double.isNaN(value)) {
                    value = 0;
                }
                result.set(mat.getRow(i), mat.getColumn(j), value);
            }
        }
        return result;
    }

    public MediaMatrix vmult(MediaMatrix mat) {
        final double[] row = new double[]{0d};
        final MediaMatrix result = new MediaMatrix(row, mat.getColumns());
        for (int j = 0; j < mat.getWidth(); j++) {
            double value = 0;
            for (int i = 0; i < mat.getHeight() - 1; i = i + 2) {
                value += mat.get(i, j) * mat.get(i + 1, j);
                if (Double.isNaN(value)) {
                    value = 0;
                }
            }
            result.set(0d, mat.getColumn(j), value);
        }
        normalize(result);
        return result;
    }

    public MediaMatrix weight1(MediaMatrix mat, MediaMatrix vec) {
        final MediaMatrix qmat = new MediaMatrix(mat.getRows(), mat.getColumns());
        double max = max(vec);
        for (int i = 0; i < mat.getHeight() && i < vec.getHeight(); i++) {
            for (int j = 0; j < mat.getWidth(); j++) {
                double value = mat.get(i, j) * vec.get(i, 0) / max;
                qmat.set(mat.getRow(i), mat.getColumn(j), value);
            }
        }
        return qmat;
    }

    public MediaMatrix weight2(MediaMatrix mat, MediaMatrix ignored, MediaMatrix vec) {
        return weight2(mat, vec);
    }

    public MediaMatrix weight2(MediaMatrix mat, MediaMatrix vec) {
        final MediaMatrix qmat = new MediaMatrix(mat.getRows(), mat.getColumns());
        double max = max(vec);
        for (int i = 0; i < mat.getHeight(); i++) {
            for (int j = 0; j < mat.getWidth(); j++) {
                double value = mat.get(i, j) * ((vec.get(i, 0) / max) + 1);
                qmat.set(mat.getRow(i), mat.getColumn(j), value);
            }
        }
        return qmat;
    }

    public double max(MediaMatrix vec) {
        double max = 0d;
        for (int i = 0; i < vec.getHeight(); i++) {
            if (vec.get(i, 0) > max) {
                max = vec.get(i, 0);
            }
        }
        return max;
    }

    public MediaMatrix average(MediaMatrix mat1, MediaMatrix mat2, MediaMatrix vec) {
        mat1 = weight2(mat1, null, vec);
        mat2 = weight2(mat2, null, vec);
        final MediaMatrix result = new MediaMatrix(mat1.getRows(), mat1.getColumns());
        for (int i = 0; i < mat1.getHeight(); i++) {
            for (int j = 0; j < mat1.getWidth(); j++) {
                result.set(mat1.getRow(i), mat1.getColumn(j), (mat1.get(i, j) + mat2.get(i, j)) / 2d);
            }
        }
        return result;
    }

    public MediaMatrix correlationVector(String method, MediaMatrix mat1, MediaMatrix mat2) {
        final MediaMatrix result = new MediaMatrix(mat1.getRows(), new String[]{"correlation"});
        if ("innerproduct".equalsIgnoreCase(method)) {
            for (int i = 0; i < mat1.getHeight(); i++) {
                double sum = 0d;
                for (int j = 0; j < mat1.getWidth(); j++) {
                    sum += (mat1.get(i, j) * mat2.get(i, j));
                }
                result.set(mat1.getRow(i), "correlation", sum);
            }
        } else if ("difference".equalsIgnoreCase(method)) {
            for (int i = 0; i < mat1.getHeight(); i++) {
                double sum = 0d;
                for (int j = 0; j < mat1.getWidth(); j++) {
                    sum += Math.abs(mat1.get(i, j) - mat2.get(i, j));
                }

                result.set(mat1.getRow(i), "correlation", sum);
            }
        }
        return result;
    }

    public CorrelationMatrix correlationMatrix(String method, MediaMatrix mat1, MediaMatrix mat2) {
        final CorrelationMatrix result = new CorrelationMatrix(mat1.getHeight(), mat2.getHeight());
        for (int i = 0; i < mat1.getHeight(); i++) {
            for (int j = 0; j < mat2.getHeight(); j++) {
                double sum = 0d;
                for (int k = 0; k < mat1.getWidth(); k++) {
                    sum += (mat1.get(i, k) * mat2.get(j, k));
                }
                result.set(i, j, sum);
            }
        }
        return result;
    }

    public CorrelationMatrix densityCorrelationMatrix(MediaMatrix mat, int size, double len, double delta, int t) {
        final CorrelationMatrix result = new CorrelationMatrix(mat.getHeight(), 1);
        for (double i = 0; i < mat.getRow(mat.getHeight() - 1); i = i + delta) {
            MediaMatrix m = sub(mat, i, len);
            if (Math.abs(m.getHeight() - size) <= t) {
                for (int j = 0; j < m.getHeight(); j++) {
                    result.set(mat.getRowIndex(m.getRow(j)), 0, 1);
                }
            }
        }
        return result;
    }

    public int alignment(CorrelationMatrix mat) {
        return mat.mostRelevantShift();
    }

    public MediaMatrix slide(CorrelationMatrix mat, int num) {
        double[] values = mat.getShift(num);
        double[] rows = new double[values.length];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }
        final MediaMatrix result = new MediaMatrix(rows, new String[]{"correlation"});
        for (int i = 0; i < rows.length; i++) {
            result.set(rows[i], "correlation", values[i]);
        }
        return result;
    }

    public double sum(CorrelationMatrix mat, int num) {
        double[] vec = mat.getShift(num);
        double sum = 0d;
        for (int i = 0; i < vec.length; i++) {
            sum += vec[i] * vec[i];
        }
        return Math.sqrt(sum);
    }

    public double sum(CorrelationMatrix mat) {
        double sum = 0d;
        for (int i = 0; i < mat.getWidth(); i++) {
            for (int j = 0; j < mat.getHeight(); j++) {
                sum += mat.get(i, j);
            }
        }
        return sum / (mat.getWidth() * mat.getHeight());
    }

    public double mult(double d1, double d2) {
        return d1 * d2;
    }

    public double sum(MediaMatrix vec) {
        double sum = 0d;
        for (int i = 0; i < vec.getHeight(); i++) {
            sum += vec.get(i, 0);
        }
        return sum;
    }

    public void normalize(MediaMatrix mat) {
        for (int i = 0; i < mat.getHeight(); i++) {
            double norm = 0d;
            for (int j = 0; j < mat.getWidth(); j++) {
                norm += (mat.get(i, j) * mat.get(i, j));
            }
            norm = Math.sqrt(norm);
            if (norm != Double.NaN && norm > 0) {
                for (int j = 0; j < mat.getWidth(); j++) {
                    double d = mat.get(i, j) / norm;
                    mat.set(mat.getRow(i), mat.getColumn(j), d);
                }
            }
        }
    }

    public double innerproduct(MediaMatrix mat1, MediaMatrix mat2) {
        double sum = 0d;
        for (int i = 0; i < mat1.getHeight(); i++) {
            for (int j = 0; j < mat1.getWidth(); j++) {
                sum += (mat1.get(i, j) * mat2.get(i, j));
            }
        }
        return sum;
    }

    public double innerproductByVector(MediaMatrix vec, MediaMatrix mat2) {
        double sum = 0d;
        for (int i = 0; i < mat2.getHeight(); i++) {
            double cor = 0d;
            for (int j = 0; j < vec.getWidth(); j++) {
                cor += (vec.get(0, j) * mat2.get(i, j));
            }
            sum += cor;
        }
        return sum / mat2.getHeight();
    }

    public Set<Score<Integer, Double>> frame_correlation(MediaMatrix vec, MediaMatrix mat2) {
        final Set<Score<Integer, Double>> result = new TreeSet<Score<Integer, Double>>();
        for (int i = 0; i < mat2.getHeight(); i++) {
            double cor = 0d;
            for (int j = 0; j < vec.getWidth(); j++) {
                double d = mat2.get(i, j);
                if (d < 0) {
                    d = 0;
                }
                cor += (vec.get(0, j) * d);
            }
            final Score<Integer, Double> score = new Score<Integer, Double>(i, cor);
            result.add(score);
        }
        return result;
    }

    public double average(Set<Score<Integer, Double>> result) {
        double sum = 0d;
        for (Score<Integer, Double> score : result) {
            sum += score.score;
        }
        return sum / result.size();
    }

    public MediaMatrix histogram(MediaMatrix mat) {
        final MediaMatrix result = new MediaMatrix(new double[]{1d}, mat.getColumns());
        result.setId(mat.getId());
        for (int j = 0; j < mat.getWidth(); j++) {
            double sum = 0d;
            for (int i = 0; i < mat.getHeight(); i++) {
                sum += mat.get(i, j);
            }
            result.set(1d, result.getColumn(j), sum);
        }
        return result;
    }

    public MediaMatrix nonZerohistogram(MediaMatrix mat) {
        final MediaMatrix result = new MediaMatrix(new double[]{1d}, mat.getColumns());
        result.setId(mat.getId());
        for (int j = 0; j < mat.getWidth(); j++) {
            double sum = 0d;
            for (int i = 0; i < mat.getHeight(); i++) {
                sum += mat.get(i, j);
            }
            if (sum >= 0) {
                result.set(1d, result.getColumn(j), sum);
            } else {
                result.set(1d, result.getColumn(j), 0);
            }
        }
        return result;
    }

    public MediaMatrix dcs(MediaMatrix mat) {
        final MediaMatrix result = histogram(mat);
        for (int i = 0; i < result.getWidth(); i++) {
            result.set(1d, result.getColumn(i), result.get(0, i) / (double) mat.getHeight());
        }
        return mult(mat, result);
    }

    public MediaMatrix ecs(MediaMatrix mat) {
        final MediaMatrix idcs = histogram(mat);
        for (int i = 0; i < idcs.getWidth(); i++) {
            idcs.set(1d, idcs.getColumn(i), Math.log(1.0 / idcs.get(0, i)));
        }
        return mult(mat, idcs);
    }

    public Set<CorrelationScore> sortedScore(MediaMatrix mat) {
        Set<CorrelationScore> result = new TreeSet<CorrelationScore>();
        for (int j = 0; j < mat.getWidth(); j++) {
            String word = mat.getColumn(j);
            result.add(new CorrelationScore(word, mat.get(0, j)));
        }
        return result;
    }

    public MediaMatrix topk(MediaMatrix mat, int k) {
        final MediaMatrix result = new MediaMatrix(mat.getRows(), mat.getColumns());
        for (int i = 0; i < mat.getHeight(); i++) {
            TreeSet<CorrelationScore> set = new TreeSet<CorrelationScore>();
            for (int j = 0; j < mat.getWidth(); j++) {
                set.add(new CorrelationScore(mat.getColumn(j), mat.get(i, j)));
            }
            int kt = 0;
            for (Iterator<CorrelationScore> it = set.iterator(); it.hasNext() && kt++ < k;) {
                CorrelationScore correlation = it.next();
                result.set(mat.getRow(i), correlation.getWord(), correlation.getValue());
            }
        }
        return result;
    }

    public MediaMatrix nonZeroRows(MediaMatrix mat) {
        final TreeSet<Integer> temp = new TreeSet<Integer>();
        for (int i = 0; i < mat.getHeight(); i++) {
            for (int j = 0; j < mat.getWidth(); j++) {
                if (mat.get(i, j) > 0) {
                    temp.add(i);
                }
            }
        }
        final double[] rows = new double[temp.size()];
        int index = 0;
        for (Iterator<Integer> it = temp.iterator(); it.hasNext();) {
            rows[index++] = mat.getRow(it.next());
        }
        final MediaMatrix result = new MediaMatrix(rows, mat.getColumns());
        for (int i = 0; i < result.getHeight(); i++) {
            for (int j = 0; j < result.getWidth(); j++) {
                result.set(result.getRow(i), result.getColumn(j), mat.get(result.getRow(i), result.getColumn(j)));
            }
        }
        return result;
    }

    public MediaMatrix differenceMatrix(MediaMatrix mat) {
        double[] tempRows = mat.getRows();
        double[] rows = new double[tempRows.length - 1];
        System.arraycopy(tempRows, 0, rows, 0, tempRows.length - 1);
        final MediaMatrix result = new MediaMatrix(rows, mat.getColumns());
        for (int i = 0; i < mat.getHeight() - 1; i++) {
            for (int j = 0; j < mat.getWidth(); j++) {
                result.set(result.getRow(i), result.getColumn(j), Math.abs(mat.get(i, j) - mat.get(i + 1, j)));
            }
        }
        return result;
    }

    public MediaMatrix ceil(MediaMatrix mat) {
        final String[] column = mat.getColumns();
        final double[] row = mat.getRows();
        final MediaMatrix result = new MediaMatrix(row, column);
        for (int i = 0; i < mat.getHeight(); i++) {
            for (int j = 0; j < mat.getWidth(); j++) {
                if (mat.get(i, j) > 0) {
                    result.set(result.getRow(i), result.getColumn(j), 1.0d);
                }
            }
        }
        return result;
    }

    public int count(MediaMatrix mat) {
        return mat.getRows().length;
    }

    public MediaMatrix cutNegative(MediaMatrix mat) {
        final String[] column = mat.getColumns();
        final double[] row = mat.getRows();
        final MediaMatrix result = new MediaMatrix(row, column);
        for (int i = 0; i < mat.getHeight(); i++) {
            for (int j = 0; j < mat.getWidth(); j++) {
                if (mat.get(i, j) < 0) {
                    result.set(result.getRow(i), result.getColumn(j), 0.0d);
                } else {
                    result.set(result.getRow(i), result.getColumn(j), mat.get(i, j));
                }
            }
        }
        return result;
    }

    public MediaMatrix sub(MediaMatrix mat, double begin, double end) {
        final String[] column = mat.getColumns();
        final List<Double> tempRow = new ArrayList<Double>();
        for (int i = 0; i < mat.getHeight(); i++) {
            double d = mat.getRow(i);
            if (d >= begin && d <= end) {
                tempRow.add(d);
            }
        }
        final double[] row = new double[tempRow.size()];
        for (int i = 0; i < row.length; i++) {
            row[i] = tempRow.get(i);
        }

        final MediaMatrix result = new MediaMatrix(row, column);
        for (int i = 0; i < result.getHeight(); i++) {
            for (int j = 0; j < result.getWidth(); j++) {
                result.set(result.getRow(i), result.getColumn(j), mat.get(result.getRow(i), result.getColumn(j)));
            }
        }
        return result;
    }

    public MediaMatrix projection(MediaMatrix mat, List<String> attributes) {
        final MediaMatrix result = new MediaMatrix(mat.getRows(), attributes.toArray(new String[attributes.size()]));
        result.setId(mat.getId());
        for (int i = 0; i < mat.getHeight(); i++) {
            for (int j = 0; j < attributes.size(); j++) {
                result.set(mat.getRow(i), attributes.get(j), mat.get(mat.getRow(i), attributes.get(j)));
            }
        }
        System.out.println(attributes.size() + " selected.");
        return result;
    }

    public MediaMatrix cluster(MediaMatrix mat, int c) {
        final double timeCluster = mat.getHeight() / (double) c;
        final String[] column = mat.getColumns();
        final double[] row = new double[c];
        for (int i = 0; i < row.length; i++) {
            row[i] = i * timeCluster;
        }
        final MediaMatrix result = new MediaMatrix(row, column);
        for (int i = 0; i < mat.getWidth(); i++) {
            final String word = mat.getColumn(i);

            for (int j = 0; j < row.length; j++) {
                final double begin = row[j];
                double end = 0d;
                if (j < row.length - 1) {
                    end = row[j + 1];
                } else {
                    end = mat.getRow(mat.getHeight() - 1);
                }

                double sum = 0d;
                for (int k = 0; k < mat.getHeight(); k++) {
                    if (mat.getRow(k) >= begin && mat.getRow(k) < end) {
                        sum += mat.get(k, i);
                    }
                }
                result.set(begin, word, sum);
            }
        }
        normalize(result);
        return result;
    }

    public MediaMatrix selectMoreThan(MediaMatrix mat, double t) {
        final List<Double> tempRow = new LinkedList<Double>();
        for (int i = 0; i < mat.getHeight(); i++) {
            boolean longer = true;
            for (int j = 0; j < 12; j++) {
                if (mat.get(i, j) == 0d || mat.get(i, j) >= t) {
                    continue;
                } else {
                    longer = false;
                    break;
                }
            }
            if (longer) {
                tempRow.add(mat.getRow(i));
            }
        }
        return select(mat, tempRow);
    }

    public MediaMatrix selectLessThan(MediaMatrix mat, double t) {
        final List<Double> tempRow = new LinkedList<Double>();
        for (int i = 0; i < mat.getHeight(); i++) {
            boolean longer = true;
            for (int j = 0; j < 12; j++) {
                if (mat.get(i, j) == 0d || mat.get(i, j) < t) {
                    continue;
                } else {
                    longer = false;
                    break;
                }
            }
            if (longer) {
                tempRow.add(mat.getRow(i));
            }
        }
        return select(mat, tempRow);
    }
}
