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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

public final class ColorMappingPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private ColorMap aMap;

    public ColorMappingPanel() {
        setOpaque(true);
    }

    public ColorMappingPanel(ColorMap aMap) {
        this();
        this.aMap = aMap;
    }

    public ColorMap getColorMap() {
        return aMap;
    }

    public void setColorMap(ColorMap aMap) {
        this.aMap = aMap;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (aMap != null) {
            final Color[] majorColors = aMap.getMajorColors();
            final Color[] minorColors = aMap.getMinorColors();
            final Dimension dim = getSize();
            final Graphics2D g2d = (Graphics2D) g;

            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D r = fm.getStringBounds(aMap.getName(), g2d);
            g2d.drawString(aMap.getName(), 1, (int) ((dim.height / 2) - (r.getHeight() / 2)) + 4);

            int sum = (int) r.getWidth() + 5;
            for (int i = 0; i < majorColors.length; i++) {
                g2d.setColor(majorColors[i]);
                int width = (dim.width - 30) / 12;
                if (dim.width % 12 != 0) {
                    width = width - 1;
                }
                g2d.fillRect(sum, 0, width, dim.height / 2);
                g2d.setColor(Color.WHITE);
                String s = ColorMap.majorKeyNames[i].replace("b", "♭");
                g2d.drawString(s, sum, dim.height / 2);
                g2d.setColor(minorColors[i]);
                g2d.fillRect(sum, dim.height / 2, width, dim.height / 2);
                g2d.setColor(Color.WHITE);
                String m = ColorMap.minorKeyNames[i].replace("b", "♭");
                m = m.toLowerCase().replace("m", "");
                g2d.drawString(m, sum, dim.height - 2);
                sum += width;
            }
        }
    }
}
