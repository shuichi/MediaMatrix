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

import java.awt.Color;

public class DefaultColorMap implements KeyNameString {

    public static final ColorMap SCRIABIN = new ColorMap("Scriabin");
    public static final ColorMap BISHOP = new ColorMap("Bishop");
    public static final ColorMap[] COLORMAPS = new ColorMap[]{SCRIABIN, BISHOP};
    static {
        SCRIABIN.setColor(C_dur, new Color(250, 11, 12));
        SCRIABIN.setColor(Cis_dur, new Color(215, 19, 134));
        SCRIABIN.setColor(D_dur, new Color(245, 244, 60));
        SCRIABIN.setColor(Es_dur, new Color(90, 86, 133));
        SCRIABIN.setColor(E_dur, new Color(28, 91, 160));
        SCRIABIN.setColor(F_dur, new Color(160, 12, 9));
        SCRIABIN.setColor(Fis_dur, new Color(28, 13, 130));
        SCRIABIN.setColor(G_dur, new Color(248, 128, 16));
        SCRIABIN.setColor(As_dur, new Color(127, 8, 124));
        SCRIABIN.setColor(A_dur, new Color(20, 144, 51));
        SCRIABIN.setColor(B_dur, new Color(90, 86, 133));
        SCRIABIN.setColor(H_dur, new Color(28, 91, 160));

        BISHOP.setColor(C_dur, new Color(250, 11, 12));
        BISHOP.setColor(Cis_dur, new Color(160, 12, 9));
        BISHOP.setColor(D_dur, new Color(248, 128, 16));
        BISHOP.setColor(Es_dur, new Color(246, 209, 17));
        BISHOP.setColor(E_dur, new Color(245, 244, 60));
        BISHOP.setColor(F_dur, new Color(188, 224, 57));
        BISHOP.setColor(Fis_dur, new Color(20, 144, 51));
        BISHOP.setColor(G_dur, new Color(39, 164, 129));
        BISHOP.setColor(As_dur, new Color(127, 8, 124));
        BISHOP.setColor(A_dur, new Color(215, 19, 134));
        BISHOP.setColor(B_dur, new Color(217, 25, 81));
        BISHOP.setColor(H_dur, new Color(250, 11, 12));
    }
}
