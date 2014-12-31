/* MediaMatrix -- Automatic Movie Processor
 * Copyright (C) 2008 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
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
package mediamatrix.munsell;

import java.awt.Color;
import java.io.Serializable;

public class HSVColor extends Color implements Serializable, Comparable<HSVColor> {

    static final long serialVersionUID = 3423377840286848289L;
    private String hueName;
    private String toneName;
    private float h;
    private float s;
    private float v;
    private double[] hsCyl = new double[2];

    public HSVColor() {
        super(0);
        init();
    }

    public static HSVColor createHSVColor(String str) {
        str = str.replace('"', ' ');
        final String[] kv = str.trim().split("\\(");
        final String name = kv[0];
        final String value = kv[1].replace(")", "");
        final String[] values = value.split(",");
        final HSVColor c = new HSVColor(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
        c.setName(name);
        return c;
    }

    public HSVColor(int rgb) {
        super(rgb);
        init();
    }

    public HSVColor(int r, int g, int b) {
        super(r, g, b);
        init();
    }

    public HSVColor(int r, int g, int b, int a) {
        super(r, g, b, a);
        init();
    }

    public HSVColor(double h, double s, double v) {
        super(Color.HSBtoRGB((float) h, (float) s, (float) v));
        this.h = (float) h;
        this.s = (float) s;
        this.v = (float) v;
    }

    public String getHueName() {
        return hueName;
    }

    public String getToneName() {
        return toneName;
    }

    private void init() {
        float[] hsv = new float[3];
        hsv = Color.RGBtoHSB(getRed(), getGreen(), getBlue(), hsv);
        this.h = hsv[0];
        this.s = hsv[1];
        this.v = hsv[2];
        hsCyl[0] = hsv[1] * Math.cos(2 * Math.PI * hsv[0]);
        hsCyl[1] = hsv[1] * Math.sin(2 * Math.PI * hsv[0]);
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }

    public float getS() {
        return s;
    }

    public void setS(float s) {
        this.s = s;
    }

    public float getV() {
        return v;
    }

    public void setV(float v) {
        this.v = v;
    }

    public void setHSx(double v) {
        hsCyl[0] = v;
    }

    public void setHSy(double v) {
        hsCyl[1] = v;
    }

    public double getHSx() {
        return hsCyl[0];
    }

    public double getHSy() {
        return hsCyl[1];
    }

    public void setName(String name) {
        final int pos = name.indexOf('/');
        hueName = name.substring(0, pos);
        toneName = name.substring(pos + 1);
    }

    public String getName() {
        if (hueName != null && toneName != null) {
            return hueName + "/" + toneName;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        String name = null;
        if (hueName != null && toneName != null) {
            name = hueName + "/" + toneName;
        } else {
            name = "HSV(" + getH() + "," + getS() + "," + getV() + ")";
        }
        return name;
    }


    public String getHexCode() {
        String strhex = "#";
        return (strhex + Integer.toHexString( getRGB() & 0x00ffffff ));
    }

    public double distance(HSVColor color) {
        return triangleDistance(color);
    }

    public double triangleDistance(HSVColor color) {
        final double xDiff = getV() * getHSx() - color.getV() * color.getHSx();
        final double yDiff = getV() * getHSy() - color.getV() * color.getHSy();
        final double valueDiff = getV() - color.getV();
        return (xDiff * xDiff) + (yDiff * yDiff) + (valueDiff * valueDiff);
    }

    public double godloveDistance(HSVColor color) {
        double deltaHue = Math.abs(getH() - color.getH());
        double deltaSat = Math.abs(getS() - color.getS());
        double deltaVal = Math.abs(getV() - color.getV());
        return Math.sqrt(2.0 * getS() * color.getS() * (1.0 - Math.cos(Math.PI * deltaHue / 180.0)) + (deltaSat * deltaSat) + (16.0 * deltaVal * deltaVal));
    }

    public double euqlidDistance(HSVColor color) {
        double hueTemp = getH() - color.getH();
        if (hueTemp < 0 || hueTemp > 360.0) {
            hueTemp = hueTemp % 360.0;
        }
        final double hueDiff = hueTemp;
        final double saturationDiff = getS() - color.getS();
        final double valueDiff = getV() - color.getV();
        return (hueDiff * hueDiff) + (saturationDiff * saturationDiff) + (valueDiff * valueDiff);
    }

    @Override
    public int compareTo(HSVColor o) {
        int thisVal = this.getRGB();
        int anotherVal = o.getRGB();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HSVColor other = (HSVColor) obj;
        if (this.h != other.h) {
            return false;
        }
        if (this.s != other.s) {
            return false;
        }
        if (this.v != other.v) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Float.floatToIntBits(this.h);
        hash = 73 * hash + Float.floatToIntBits(this.s);
        hash = 73 * hash + Float.floatToIntBits(this.v);
        return hash;
    }
}
