package mediamatrix.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import javax.sound.midi.InvalidMidiDataException;
import mediamatrix.db.CXMQLResultSet;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.table.AbstractTableModel;
import mediamatrix.db.ChronoArchive;
import mediamatrix.music.DefaultColorMap;
import mediamatrix.music.Key;
import mediamatrix.music.TonalMusic;
import mediamatrix.music.TonalityAnalyzer;
import mediamatrix.utils.ImageUtilities;
import mediamatrix.utils.Score;

public final class MediaDatabaseTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -2001898417808046329L;
    protected transient final CXMQLResultSet result;
    protected transient final List<BufferedImage> images;

    public MediaDatabaseTableModel(CXMQLResultSet result) throws IOException, InvalidMidiDataException {
        this.result = result;
        images = new ArrayList<>();

        if (result.getType().equalsIgnoreCase("Video")) {
            for (int i = 0; i < result.size(); i++) {
                final Set<Score<Integer, Double>> frames = result.getFrameCorrelation(i);
                if (frames != null) {
                    final BufferedImage image = new BufferedImage(505, 50, BufferedImage.TYPE_INT_ARGB);
                    final Graphics2D g2 = image.createGraphics();
                    int count = 0;
                    int position = 0;
                    int previous = 0;
                    g2.drawImage(ChronoArchive.loadThumbnail(new File(result.getId(i))), position, 0, 100, 50, Color.BLACK, null);
                    position += 100 + 1;
                    for (Score<Integer, Double> score : frames) {
                        if (previous == score.key || Math.abs(previous - score.key) < 10) {
                        } else {
                            try {
                                g2.drawImage(ChronoArchive.getImage(new File(result.getId(i)), score.key), position, 0, 100, 50, Color.BLACK, null);
                                position += 100 + 1;
                                if (++count > 4) {
                                    break;
                                }
                                previous = score.key;
                            } catch (IOException e) {
                            }
                        }
                    }
                    images.add(image);
                } else {
                    images.add(ImageUtilities.createThumbnail(ChronoArchive.loadThumbnail(new File(result.getId(i))), 50, 50));
                }
            }
        } else if (result.getType().equalsIgnoreCase("Music")) {
            for (int i = 0; i < result.size(); i++) {
                final BufferedImage image = new BufferedImage(300, 15, BufferedImage.TYPE_INT_RGB);
                final Graphics2D g = image.createGraphics();
                final TonalMusic music = new TonalityAnalyzer().analyze(new File(result.getId(i)), 30);
                final Key[] keys = music.getKeys();
                final Color[] colors = DefaultColorMap.SCRIABIN.visualize(keys);
                int sum = 0;
                for (Color color : colors) {
                    g.setColor(color);
                    final int width = 300 / keys.length;
                    g.fillRect(sum, 0, width, 15);
                    sum += width;
                }
                images.add(image);
            }
        } else if (result.getType().equalsIgnoreCase("Image")) {
            for (int i = 0; i < result.size(); i++) {
                if (result.getId(i).startsWith("http://")) {
                    images.add(ImageUtilities.createThumbnail(ImageIO.read(URI.create(result.getId(i)).toURL()), 50, 50));
                } else {
                    images.add(ImageUtilities.createThumbnail(ImageIO.read(new File(result.getId(i))), 50, 50));
                }
            }
        }
    }

    public String getType() {
        return result.getType();
    }

    public CXMQLResultSet getResult() {
        return result;
    }

    public File getAsFile(int row) {
        return new File(result.getId(row));
    }

    public boolean isURL(int row) {
        return result.getId(row).startsWith("http://");
    }

    public URL getAsURL(int row) throws MalformedURLException {
        return URI.create(result.getId(row)).toURL();
    }

    public URL getEntityAsURL(int row) throws MalformedURLException {
        return URI.create(result.getEntityID(row)).toURL();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getRowCount() {
        return result.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        switch (columnIndex) {
            case 0 -> value = images.get(rowIndex);
            case 1 -> value = result.getName(rowIndex);
            case 2 -> value = result.getValue(rowIndex);
            default -> {
            }
        }
        return value;
    }

    @Override
    public String getColumnName(int column) {
        String name = null;
        switch (column) {
            case 0 -> name = "Visual";
            case 1 -> name = "Title";
            case 2 -> name = "Correlation";
            default -> {
            }
        }
        return name;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class<?> cls = String.class;
        switch (columnIndex) {
            case 0 -> cls = Image.class;
            case 1 -> cls = String.class;
            case 2 -> cls = Double.class;
            default -> {
            }
        }
        return cls;
    }
}
