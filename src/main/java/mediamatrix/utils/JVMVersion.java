package mediamatrix.utils;

public class JVMVersion {

    private String path;
    private String majorVersion;
    private String minorVersion;
    private String cpuArc;

    public JVMVersion(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return majorVersion + "_" + minorVersion + "@" + cpuArc + " --> " + path;
    }

    public String getCpuArc() {
        return cpuArc;
    }

    public void setCpuArc(String cpuArc) {
        this.cpuArc = cpuArc;
    }

    public String getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(String majorVersion) {
        this.majorVersion = majorVersion;
    }

    public String getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(String minorVersion) {
        this.minorVersion = minorVersion;
    }
}
