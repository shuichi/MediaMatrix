package mediamatrix.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    public static List<String> readLines(String body) {
        final BufferedReader reader = new BufferedReader(new StringReader(body));
        final List<String> lines = new ArrayList<String>();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (line.length() > 1) {
                    lines.add(line);
                }
            }
        } catch (IOException ignored) {
        }
        return lines;
    }
}
