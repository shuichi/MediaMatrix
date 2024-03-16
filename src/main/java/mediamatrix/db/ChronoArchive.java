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

import mediamatrix.munsell.ColorImpressionKnowledge;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import mediamatrix.utils.IOUtilities;

public class ChronoArchive {

    private static final int COMPRESS_LEVEL = 0;
    public static final String GLOBAL_XML = "GLOBAL.XML";
    public static final String MAIN_CONTENT = "MAIN_CONTENT";
    public static final String THUMBNAIL = "THUMBNAIL";
    public static final String ORDER_SETTING = "ORDER-SETTING";
    public static final String ITEM_COUNT = "ITEM-COUNT";
    public static final String SEGMENT_SUFFIX = "SEGMENT_SUFFIX";
    public static final String TIME_UNIT = "TIME_UNIT";
    public static final String TIME_UNIT_TYPE = "TIME_UNIT_TYPE";
    private Map<String, Object> global;
    private List<File> files;
    private List<Double> times;
    private String filename;
    private File mainContent;
    private File thumbnail;
    private String suffix;
    private String timeUnitType;
    private double timeUnit;
    private ColorImpressionKnowledge ci;
    private NeighborRelevance neighbor;
    private MediaMatrix matrix;
    private MediaMatrix cmatrix;
    private int count;

    public ChronoArchive() {
        super();
        count = 0;
        files = new ArrayList<File>();
        times = new ArrayList<Double>();
        global = new TreeMap<String, Object>();
    }

    public ChronoArchive(File file) throws IOException {
        this();
        this.filename = file.getAbsolutePath();
        open();
    }

    public ChronoArchive(String filename) throws IOException {
        this();
        this.filename = filename;
        open();
    }

    @SuppressWarnings("unchecked")
    public final void open() throws IOException {
        final ZipFile zipFile = new ZipFile(filename);
        final InputStream in = zipFile.getInputStream(zipFile.getEntry(GLOBAL_XML));
        final XMLDecoder decoder = new XMLDecoder(in);
        global = (Map<String, Object>) decoder.readObject();
        decoder.close();

        count = ((Integer) global.get(ITEM_COUNT)).intValue();
        suffix = (String) global.get(SEGMENT_SUFFIX);
        try {
            timeUnit = ((Double) global.get(TIME_UNIT)).doubleValue();
            timeUnitType = (String) global.get(TIME_UNIT_TYPE);
        } catch (NullPointerException e) {
            timeUnit = 1;
            timeUnitType = "Sec";
        }
        for (int i = 0; i < count; i++) {
            times.add((double) i);
        }

        final ZipEntry colorEntry = zipFile.getEntry("color.csv");
        if (colorEntry != null) {
            ci = new ColorImpressionKnowledge();
            ci.load(zipFile.getInputStream(colorEntry), "utf-8");
        }
        final ZipEntry neighborEntry = zipFile.getEntry("neighbor.csv");
        if (neighborEntry != null) {
            neighbor = new NeighborRelevance();
            neighbor.load(zipFile.getInputStream(neighborEntry), "utf-8");
        }
        final ZipEntry matEntry = zipFile.getEntry("MediaMatrix.obj");
        if (matEntry != null) {
            DataInputStream din = null;
            try {
                din = new DataInputStream(new BufferedInputStream(zipFile.getInputStream(matEntry)));
                matrix = new MediaMatrix();
                matrix.read(din);
            } finally {
                try {
                    if (din != null) {
                        din.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        final ZipEntry cmatEntry = zipFile.getEntry("ColorMediaMatrix.obj");
        if (cmatEntry != null) {
            DataInputStream din = null;
            try {
                din = new DataInputStream(new BufferedInputStream(zipFile.getInputStream(cmatEntry)));
                cmatrix = new MediaMatrix();
                cmatrix.read(din);
            } finally {
                try {
                    if (din != null) {
                        din.close();
                    }
                } catch (IOException e) {
                }
            }
        }

        zipFile.close();
    }

    public void update() throws IOException {
        getMainContent();
        getThumbnail();
        for (int i = 0; i < size(); i++) {
            files.add(getAsFile(i));
        }

        String mainContentName = (String) global.get(MAIN_CONTENT);
        final ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(this.filename)));
        output.setLevel(COMPRESS_LEVEL);
        saveEntry(output, GLOBAL_XML, globalToByte(global));
        saveEntry(output, mainContentName, fileToBytes(mainContent));
        saveEntry(output, THUMBNAIL, fileToBytes(thumbnail));
        for (int i = 0; i < files.size(); i++) {
            saveEntry(output, (i + 1) + suffix, fileToBytes(files.get(i)));
        }
        saveEntry(output, "color.csv", ci.toString().getBytes("utf-8"));
        saveEntry(output, "neighbor.csv", neighbor.toString().getBytes("utf-8"));
        saveEntry(output, "MediaMatrix.obj", matrixToBytes(matrix));
        saveEntry(output, "ColorMediaMatrix.obj", matrixToBytes(cmatrix));
        output.close();

        for (int i = 0; i < files.size(); i++) {
            files.get(i).delete();
        }
        
    }

    public void store(OutputStream out) throws IOException {
        if (mainContent != null) {
            global.put(MAIN_CONTENT, mainContent.getName());
        }
        if (thumbnail != null) {
            global.put(THUMBNAIL, Boolean.TRUE);
        } else {
            global.put(THUMBNAIL, Boolean.FALSE);
        }

        global.put(ITEM_COUNT, count);
        global.put(SEGMENT_SUFFIX, suffix);
        global.put(TIME_UNIT, timeUnit);
        global.put(TIME_UNIT_TYPE, timeUnitType);

        final ZipOutputStream output = new ZipOutputStream(out);
        output.setLevel(COMPRESS_LEVEL);
        saveEntry(output, GLOBAL_XML, globalToByte(global));

        if (mainContent != null) {
            saveEntry(output, mainContent.getName(), fileToBytes(mainContent));
        }
        if (thumbnail != null) {
            saveEntry(output, THUMBNAIL, fileToBytes(thumbnail));
        }
        for (int i = 0; i < files.size(); i++) {
            saveEntry(output, (i + 1) + suffix, fileToBytes(files.get(i)));
        }
        saveEntry(output, "color.csv", ci.toString().getBytes("utf-8"));
        saveEntry(output, "neighbor.csv", neighbor.toString().getBytes("utf-8"));
        saveEntry(output, "MediaMatrix.obj", matrixToBytes(matrix));
        saveEntry(output, "ColorMediaMatrix.obj", matrixToBytes(cmatrix));
        output.close();
    }

    public ChronoArchive subArchive(int begin, int end) {
        final ChronoArchive carc = new ChronoArchive();
        carc.ci = ci;
        carc.times = times.subList(begin, end);
        carc.count = carc.times.size();
        carc.filename = this.filename;
        carc.suffix = this.suffix;
        carc.timeUnit = this.timeUnit;
        carc.timeUnitType = this.timeUnitType;
        carc.global = new TreeMap<String, Object>(this.global);
        carc.matrix = new PrimitiveEngine().select(matrix, begin, end);
        return carc;
    }

    public String getFileName() {
        return filename;
    }

    public void setFileName(String filename) {
        this.filename = filename;
    }

    public void add(File file, double time) {
        count++;
        files.add(file);
        times.add(time);
    }

    public double getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(double timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getTimeUnitType() {
        return timeUnitType;
    }

    public void setTimeUnitType(String timeUnitType) {
        this.timeUnitType = timeUnitType;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public File getMainContent() throws IOException {
        if (mainContent == null) {
            final ZipFile zipFile = new ZipFile(filename);
            String mainContentName = (String) global.get(MAIN_CONTENT);
            if (mainContentName != null) {
                mainContent = File.createTempFile("temp", mainContentName.substring(mainContentName.lastIndexOf('.')));
                final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mainContent));
                final BufferedInputStream bin = new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry(mainContentName)));
                final byte[] buf = new byte[1024 * 512];
                int size = 0;
                while ((size = bin.read(buf)) != -1) {
                    bos.write(buf, 0, size);
                }
                bos.close();
                bin.close();
            }
            zipFile.close();
        }
        return mainContent;
    }

    public void setMainContent(File mainContent) {
        this.mainContent = mainContent;
    }

    public File getThumbnail() throws IOException {
        if (thumbnail == null) {
            final ZipFile zipFile = new ZipFile(filename);
            thumbnail = File.createTempFile("temp", ".jpg");
            final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(thumbnail));
            final BufferedInputStream bin = new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry(THUMBNAIL)));
            final byte[] buf = new byte[1024 * 512];
            int size = 0;
            while ((size = bin.read(buf)) != -1) {
                bos.write(buf, 0, size);
            }
            bos.close();
            bin.close();
            zipFile.close();
        }
        return thumbnail;
    }

    public void setThumbnail(File thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int size() {
        return count;
    }

    public double getSegmentTime(int index) {
        return times.get(index);
    }

    public BufferedImage getImage(int index) throws IOException {
        final ZipFile zipFile = new ZipFile(filename);
        final BufferedInputStream bin = new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry((index + 1) + suffix)));
        final BufferedImage image = ImageIO.read(bin);
        zipFile.close();
        return image;
    }

    public File getAsFile(int index) throws IOException {
        final ZipFile zipFile = new ZipFile(filename);
        final BufferedInputStream bin = new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry((index + 1) + suffix)));
        final File file = IOUtilities.saveAsTempFile(IOUtilities.readAllBytes(bin), ".jpg");
        zipFile.close();
        return file;
    }

    public static BufferedImage getImage(File file, int index) throws IOException {
        final ZipFile zipFile = new ZipFile(file);
        final BufferedInputStream bin = new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry((index + 1) + ".jpg")));
        final BufferedImage image = ImageIO.read(bin);
        zipFile.close();
        return image;
    }

    public MediaMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(MediaMatrix matrix) {
        this.matrix = matrix;
    }

    public void setColorMatrix(MediaMatrix cmatrix) {
        this.cmatrix = cmatrix;
    }

    public MediaMatrix getColorMatrix() {
        return cmatrix;
    }

    public NeighborRelevance getNeighborRelevance() {
        return neighbor;
    }

    public void setNeighborRelevance(NeighborRelevance neighbor) {
        this.neighbor = neighbor;
    }

    public ColorImpressionKnowledge getColorImpressionKnowledge() {
        return ci;
    }

    public void setColorImpressionKnowledge(ColorImpressionKnowledge ci) {
        this.ci = ci;
    }

    public void close() {
        if (mainContent != null) {
            mainContent.delete();
        }
    }

    private byte[] globalToByte(Map<String, Object> global) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(bos);
        enc.writeObject(global);
        enc.close();
        return bos.toByteArray();
    }

    private byte[] fileToBytes(File aFile) throws IOException {
        final BufferedInputStream in = new BufferedInputStream(new FileInputStream(aFile));
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024 * 512];
        int size = 0;
        while ((size = in.read(buf)) != -1) {
            bos.write(buf, 0, size);
        }
        bos.close();
        return bos.toByteArray();
    }

    private byte[] matrixToBytes(MediaMatrix mat) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        mat.write(out);
        return bos.toByteArray();
    }

    private void saveEntry(ZipOutputStream output, String name, byte[] dat) throws IOException {
        final ZipEntry entry = new ZipEntry(name);
        entry.setCompressedSize(dat.length);
        entry.setCrc(getCRCValue(dat));
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(dat.length);
        entry.setTime(System.currentTimeMillis());
        output.putNextEntry(entry);
        for (int i = 0; i < dat.length; i++) {
            output.write(dat[i]);
        }
        output.closeEntry();
    }

    private long getCRCValue(byte[] dat) throws IOException {
        CRC32 crc = new CRC32();
        crc.update(dat);
        return crc.getValue();
    }

    public static BufferedImage loadThumbnail(File file) throws IOException {
        BufferedImage image = null;
        final ZipFile zipFile = new ZipFile(file);
        if (zipFile.getEntry(THUMBNAIL) != null) {
            final BufferedInputStream bin = new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry(THUMBNAIL)));
            image = ImageIO.read(bin);
        }
        zipFile.close();
        return image;
    }

    public static MediaMatrix readMatrix(File file) throws IOException {
        MediaMatrix mat = null;
        final ZipFile zipFile = new ZipFile(file);
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry("MediaMatrix.obj"))));
            mat = new MediaMatrix();
            mat.read(in);
            mat.setId(file.getAbsolutePath());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
        zipFile.close();
        return mat;
    }

    public static MediaMatrix readColorMatrix(File file) throws IOException {
        MediaMatrix mat = null;
        final ZipFile zipFile = new ZipFile(file);
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry("ColorMediaMatrix.obj"))));
            mat = new MediaMatrix();
            mat.read(in);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
        zipFile.close();
        return mat;
    }

    @SuppressWarnings("unchecked")
    public static File getMainContent(File file) throws IOException {
        File mainContent = null;
        final ZipFile zipFile = new ZipFile(file);
        final InputStream in = zipFile.getInputStream(zipFile.getEntry(GLOBAL_XML));
        final XMLDecoder decoder = new XMLDecoder(in);
        Map<String, Object> xml = (Map<String, Object>) decoder.readObject();
        decoder.close();
        String mainContentName = (String) xml.get(MAIN_CONTENT);
        if (mainContentName != null) {
            mainContent = File.createTempFile("temp", mainContentName.substring(mainContentName.lastIndexOf('.')));
            final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mainContent));
            final BufferedInputStream bin = new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry(mainContentName)));
            final byte[] buf = new byte[1024 * 512];
            int size = 0;
            while ((size = bin.read(buf)) != -1) {
                bos.write(buf, 0, size);
            }
            bos.close();
            bin.close();
        }
        zipFile.close();
        return mainContent;
    }
}
