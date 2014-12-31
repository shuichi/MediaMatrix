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
package mediamatrix.gui;

import mediamatrix.music.ColorMap;
import mediamatrix.music.DefaultColorMap;
import mediamatrix.music.Key;
import mediamatrix.music.KeyNameString;
import mediamatrix.music.TonalMusic;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class ColoredMusicPanel extends javax.swing.JPanel implements KeyNameString {

    private static final long serialVersionUID = 1L;
    private TonalMusic music;
    private ColorMap map;
    private JPopupMenu popup = new JPopupMenu();
    private JMenuItem headerItem = new JMenuItem("Color Mapping Model");
    private boolean editable;
    private int selected;
    private int onMouse;

    public ColoredMusicPanel() {
        setOpaque(true);
        setBackground(Color.WHITE);
        headerItem.setEnabled(false);
        popup.add(headerItem);
        for (int i = 0; i < DefaultColorMap.COLORMAPS.length; i++) {
            popup.add(new ColorMappingPanel(DefaultColorMap.COLORMAPS[0]));
        }
        this.map = DefaultColorMap.COLORMAPS[0];

        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                onMouse = getIndexFromPoint(e.getPoint());
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                onMouse = getIndexFromPoint(e.getPoint());
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                onMouse = -1;
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                } else {
                    selected = getIndexFromPoint(e.getPoint());
                    repaint();
                }
            }
        });

        editable = false;
        selected = -1;
        onMouse = -1;
    }

    public int getIndexFromPoint(Point p) {
        int result = -1;
        final Key[] keys = music.getKeys();
        final Dimension dim = getSize();
        int sum = 0;
        for (int i = 0; i < keys.length; i++) {
            final double pp = keys[i].getLength() / (double) music.getLength();
            final int width = (int) (dim.width * pp);
            Rectangle rec = new Rectangle(sum, 0, width, dim.height);
            if (rec.contains(p)) {
                result = i;
                break;
            }
            sum += width;
        }
        return result;
    }

    public void setColorMap(ColorMap map) {
        this.map = map;
        repaint();
    }

    public void setTonalMusic(TonalMusic music) {
        this.music = music;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (music != null) {
            final Key[] keys = music.getKeys();
            final Color[] colors = map.visualize(keys);
            final Dimension dim = getSize();
            final Graphics2D g2d = (Graphics2D) g;
            int sum = 0;
            for (int i = 0; i < colors.length; i++) {
                g2d.setColor(colors[i]);
                final double pp = keys[i].getLength() / (double) music.getLength();
                final int width = (int) (dim.width * pp);
                g2d.fillRect(sum, 0, width, dim.height);
                sum += width;
            }

            sum = 0;
            if (onMouse > -1) {
                BasicStroke dsahStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, new float[]{10.0f, 3.0f}, 0.0f);
                g2d.setStroke(dsahStroke);
                for (int i = 0; i < onMouse; i++) {
                    final double pp = keys[i].getLength() / (double) music.getLength();
                    final int width = (int) (dim.width * pp);
                    sum += width;
                }
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(2));
                final double pp = keys[onMouse].getLength() / (double) music.getLength();
                final int width = (int) (dim.width * pp);
                g2d.drawRect(sum, 0, width, dim.height);
            }


            sum = 0;
            if (selected > -1) {
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(Color.WHITE);
                for (int i = 0; i < selected; i++) {
                    final double pp = keys[i].getLength() / (double) music.getLength();
                    final int width = (int) (dim.width * pp);
                    sum += width;
                }
                final double pp = keys[selected].getLength() / (double) music.getLength();
                final int width = (int) (dim.width * pp);
                g2d.drawRect(sum, 0, width, dim.height);
            }
        }
    }
}
