package mediamatrix.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleImageSearchAPI {

    private final String key;
    private final String referer;

    public GoogleImageSearchAPI(final String key, final String ref) {
        this.key = key;
        this.referer = ref;
    }

    public List<GoogleImageSearchResult> search(final String query, final int count) throws IOException {
        final List<GoogleImageSearchResult> result = new ArrayList<GoogleImageSearchResult>();
        for (int i = 0; i < count / 4; i++) {
            final List<GoogleImageSearchResult> temp = doSearch(URLEncoder.encode(query, "UTF-8"), i * 4);
            for (GoogleImageSearchResult r : temp) {
                result.add(r);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<GoogleImageSearchResult> doSearch(final String query, final int start) throws IOException {
        final URL url = new URL("http://ajax.googleapis.com/ajax/services/search/images?v=1.0&" + "q=" + query + "&key=" + key + "&userip=" + InetAddress.getLocalHost().getHostAddress().toString() + "&start=" + start);
        final URLConnection connection = url.openConnection();
        connection.addRequestProperty("Referer", referer);
        final StringBuilder builder = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            builder.append(line);
        }
        final List<GoogleImageSearchResult> result = new ArrayList<GoogleImageSearchResult>();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> userData = mapper.readValue(builder.toString(), Map.class);
        Map<String, Object> responseData = (Map<String, Object>) userData.get("responseData");
        List<Map<String, String>> list = (List<Map<String, String>>) responseData.get("results");
        for (Map<String, String> map : list) {
            result.add(new GoogleImageSearchResult(map.get("url").toString(), map.get("tbUrl").toString()));
        }
        return result;
    }
}
