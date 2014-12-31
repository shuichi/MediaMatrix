package mediamatrix.munsell;

public class CIEDE2000 {

    public static double distance(double[] lab1, double[] lab2) {
        double L1 = lab1[0];
        double a1 = lab1[1];
        double b1 = lab1[2];
        double L2 = lab2[0];
        double a2 = lab2[1];
        double b2 = lab2[2];
        double Cab1 = Math.sqrt(a1 * a1 + b1 * b1);
        double Cab2 = Math.sqrt(a2 * a2 + b2 * b2);
        double CabAvg = (Cab1 + Cab2) / 2;
        double CabAvg7 = Math.pow(CabAvg, 7);
        double G = 1 + (1 - Math.sqrt(CabAvg7 / (CabAvg7 + 6103515625.0))) / 2;
        double ap1 = G * a1;
        double ap2 = G * a2;
        double Cp1 = Math.sqrt(ap1 * ap1 + b1 * b1);
        double Cp2 = Math.sqrt(ap2 * ap2 + b2 * b2);
        double CpProd = Cp1 * Cp2;

        double hp1 = Math.atan2(b1, ap1);
        // ensure hue is between 0 and 2pi
        if (hp1 < 0) {
            hp1 += 6.283185307179586476925286766559;
        }

        double hp2 = Math.atan2(b2, ap2);
        // ensure hue is between 0 and 2pi
        if (hp2 < 0) {
            hp2 += 6.283185307179586476925286766559;
        }

        double dL = L2 - L1;
        double dC = Cp2 - Cp1;

        // computation of hue difference
        double dhp = 0.0;
        // set hue difference to zero if the product of chromas is zero
        if (CpProd != 0) {
            dhp = hp2 - hp1;
            if (dhp > Math.PI) {
                dhp -= 6.283185307179586476925286766559;
            } else if (dhp < -Math.PI) {
                dhp += 6.283185307179586476925286766559;
            }
        }

        double dH = 2 * Math.sqrt(CpProd) * Math.sin(dhp / 2);

        // weighting functions
        double Lp = (L1 + L2) / 2 - 50;
        double Cp = (Cp1 + Cp2) / 2;

        // average hue computation
        double hp = (hp1 + hp2) / 2;

        // identify positions for which abs hue diff exceeds 180 degrees
        if (Math.abs(hp1 - hp2) > Math.PI) {
            hp -= Math.PI;
        }
        // ensure hue is between 0 and 2pi
        if (hp < 0) {
            hp += 6.283185307179586476925286766559;
        }

        double LpSqr = Lp * Lp;

        double Sl = 1 + 0.015 * LpSqr / Math.sqrt(20 + LpSqr);

        double Sc = 1 + 0.045 * Cp;

        double hphp = hp + hp;
        double T = 1 - 0.17 * Math.cos(hp - 0.52359877559829887307710723054658)
                + 0.24 * Math.cos(hphp)
                + 0.32 * Math.cos(hphp + hp + 0.10471975511965977461542144610932)
                - 0.20 * Math.cos(hphp + hphp - 1.0995574287564276334619251841478);

        double Sh = 1 + 0.015 * Cp * T;

        double powerBase = hp - 4.799655442984406;
        double deltaThetaRad = 1.0471975511965977461542144610932 * Math.exp(-5.25249016001879 * powerBase * powerBase);
        double Cp7 = Math.pow(Cp, 7);
        double Rc = 2 * Math.sqrt(Cp7 / (Cp7 + 6103515625.0));
        double RT = -Math.sin(deltaThetaRad) * Rc;

        double dLSl = dL / Sl;
        double dCSc = dC / Sc;
        double dHSh = dH / Sh;
        return Math.sqrt(dLSl * dLSl + dCSc * dCSc + dHSh * dHSh + RT * dCSc * dHSh);
    }
}
