package mediamatrix.utils;

import java.beans.XMLDecoder;
import java.io.FileInputStream;
import java.util.Set;
import java.util.TreeMap;

public class Search {

    public static double[] sabi1 = new double[]{-0.68590247631073, -0.6329185366630554, -0.7334365248680115, -0.6275657415390015, -0.5687586069107056, -0.16348588466644287, -0.3450576961040497, 0.0872921571135521, -0.003383132629096508, 0.24379496276378632, 0.4430428147315979, 0.5296791791915894, 0.6584940552711487, 0.567099392414093, 0.7977770566940308, 0.5392960906028748, 0.5364573001861572, 0.43817421793937683, 0.1767805516719818, -0.148020401597023, -0.0313713476061821, -0.4416307508945465, -0.2446419894695282, -0.3917146325111389};
    public static double[] sabi2 = new double[]{-0.6818726062774658, -0.6266706585884094, -0.7147979736328125, -0.6001743674278259, -0.5612499713897705, -0.14473022520542145, -0.3316904902458191, 0.08747889846563339, 0.015167156234383583, 0.25236833095550537, 0.47705623507499695, 0.5475665330886841, 0.6490722894668579, 0.5818260908126831, 0.7747557163238525, 0.4926143288612366, 0.5345711708068848, 0.4115394055843353, 0.1780068725347519, -0.12699666619300842, -0.06717857718467712, -0.4613109529018402, -0.27183985710144043, -0.413510799407959};

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {

        TreeMap<String, double[]> map1 = null;
        TreeMap<String, double[]> map2 = null;
        XMLDecoder dec = null;
        dec = new XMLDecoder(new FileInputStream("C:/Temp/trans1.xml"));
        map1 = (TreeMap<String, double[]>) dec.readObject();
        dec.close();
        dec = new XMLDecoder(new FileInputStream("C:/Temp/trans2.xml"));
        map2 = (TreeMap<String, double[]>) dec.readObject();
        dec.close();
//        double[][] matrix = new double[][]{happy, sad, angry, cool, warm};
        double[] vec = new double[]{0, 0, 1, 0, 0};
//        double[] vec = new double[]{0,1,0,1,0};
        Set<String> keys = map2.keySet();
        for (String filename : keys) {
            double[] values = map2.get(filename);
            System.out.print(filename);
            System.out.print(",");
            double score = VectorUtils.innerProduct(values, vec);
            System.out.println(score);
        }

    }
}
