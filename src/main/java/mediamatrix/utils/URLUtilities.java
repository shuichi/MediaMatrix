package mediamatrix.utils;

public class URLUtilities {

    public static String extractSuffix(String url) {
        return url.substring(url.lastIndexOf('.'));
    }
}
