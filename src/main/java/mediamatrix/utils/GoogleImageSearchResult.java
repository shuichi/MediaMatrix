package mediamatrix.utils;

public class GoogleImageSearchResult {

    private String url;
    private String tbUrl;

    public GoogleImageSearchResult(String url, String tbUrl) {
        this.url = url;
        this.tbUrl = tbUrl;
    }

    public String getTbUrl() {
        return tbUrl;
    }

    public void setTbUrl(String tbUrl) {
        this.tbUrl = tbUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return tbUrl + " --> " + url;
    }
}
