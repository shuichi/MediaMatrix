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
import java.util.HashMap;
import java.util.Map;

public class ColorMap extends HashMap<String, Color> implements KeyNameString {

    private static final long serialVersionUID = 1L;
    private String name;

    public ColorMap() {
        super();
    }

    public ColorMap(String name) {
        this();
        this.name = name;
    }
    private static final String[] keyNames = new String[]{C_dur, a_moll, G_dur,
        e_moll, D_dur, h_moll,
        A_dur, fis_moll, E_dur,
        cis_moll, H_dur, gis_moll,
        Fis_dur, dis_moll, Cis_dur,
        ais_moll, F_dur, d_moll,
        B_dur, g_moll, Es_dur,
        c_moll, As_dur, f_moll,
        Des_dur, b_moll, Ges_dur,
        es_moll, Ces_dur, as_moll, "Bbm", "Dbm"
    };
    public static final String[] majorKeyNames = new String[]{C_dur, Cis_dur, D_dur, Es_dur, E_dur, F_dur, Fis_dur, G_dur, As_dur, A_dur, B_dur, H_dur};
    public static final String[] minorKeyNames = new String[]{c_moll, cis_moll, d_moll, es_moll, e_moll, f_moll, fis_moll, g_moll, as_moll, a_moll, b_moll, h_moll};
    private static final Map<String, String> enharmonic = new HashMap<String, String>();
    

    static {
        enharmonic.put(Des_dur, Cis_dur);
        enharmonic.put(Cis_dur, Des_dur);
        enharmonic.put(Ges_dur, Fis_dur);
        enharmonic.put(Fis_dur, Ges_dur);
        enharmonic.put(Ces_dur, H_dur);
        enharmonic.put(b_moll, ais_moll);
        enharmonic.put(ais_moll, b_moll);
        enharmonic.put(es_moll, dis_moll);
        enharmonic.put(dis_moll, es_moll);
        enharmonic.put(as_moll, gis_moll);
        enharmonic.put(gis_moll, as_moll);
        enharmonic.put(ais_moll, "Bbm");
        enharmonic.put("Bbm", ais_moll);
        enharmonic.put(cis_moll, "Dbm");
        enharmonic.put("Dbm", cis_moll);
    }
    private static final Map<String, String> majorMinorMap = new HashMap<String, String>();
    

    static {
        majorMinorMap.put(C_dur, c_moll);
        majorMinorMap.put(Cis_dur, cis_moll);
        majorMinorMap.put(D_dur, d_moll);
        majorMinorMap.put(Es_dur, es_moll);
        majorMinorMap.put(E_dur, e_moll);
        majorMinorMap.put(F_dur, f_moll);
        majorMinorMap.put(Fis_dur, fis_moll);
        majorMinorMap.put(G_dur, g_moll);
        majorMinorMap.put(As_dur, as_moll);
        majorMinorMap.put(A_dur, a_moll);
        majorMinorMap.put(B_dur, b_moll);
        majorMinorMap.put(H_dur, h_moll);
    }

    public Color[] getMajorColors() {
        Color[] colors = new Color[12];
        colors[0] = get(C_dur);
        colors[1] = get(Cis_dur);
        colors[2] = get(D_dur);
        colors[3] = get(Es_dur);
        colors[4] = get(E_dur);
        colors[5] = get(F_dur);
        colors[6] = get(Fis_dur);
        colors[7] = get(G_dur);
        colors[8] = get(As_dur);
        colors[9] = get(A_dur);
        colors[10] = get(B_dur);
        colors[11] = get(H_dur);
        return colors;
    }

    public Color[] getMinorColors() {
        Color[] colors = new Color[12];
        colors[0] = get(c_moll);
        colors[1] = get(cis_moll);
        colors[2] = get(d_moll);
        colors[3] = get(es_moll);
        colors[4] = get(e_moll);
        colors[5] = get(f_moll);
        colors[6] = get(fis_moll);
        colors[7] = get(g_moll);
        colors[8] = get(as_moll);
        colors[9] = get(a_moll);
        colors[10] = get(b_moll);
        colors[11] = get(h_moll);
        return colors;
    }

    public Color[] visualize(Key[] keys) {
        Color[] colors = new Color[keys.length];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = getColor(keys[i].toString());
        }
        return colors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor(String keyName) {
        if (!isValidKeyName(keyName)) {
            throw new IllegalArgumentException(keyName + " is invalid key name.");
        }
        Color color = get(keyName);
        if (color == null && enharmonic.containsKey(keyName)) {
            color = get(enharmonic.get(keyName));
        }
        return color;
    }

    public void setColor(String keyName, Color color) {
        if (!isValidKeyName(keyName)) {
            throw new IllegalArgumentException(keyName + " is invalid key name.");
        }

        put(keyName, color);
        if (enharmonic.containsKey(keyName)) {
            put(enharmonic.get(keyName), color);
        }
        if (majorMinorMap.containsKey(keyName)) {
            put(majorMinorMap.get(keyName), darken(color));
        }
    }

    private boolean isValidKeyName(String keyName) {
        for (int i = 0; i < keyNames.length; i++) {
            if (keyNames[i].equals(keyName)) {
                return true;
            }
        }
        return false;
    }

    private Color darken(Color color) {
        int r = color.getRed() - 50;
        if (r < 0) {
            r = 0;
        }
        int g = color.getGreen() - 50;
        if (g < 0) {
            g = 0;
        }
        int b = color.getBlue() - 50;
        if (b < 0) {
            b = 0;
        }
        return new Color(r, g, b);
    }
}
