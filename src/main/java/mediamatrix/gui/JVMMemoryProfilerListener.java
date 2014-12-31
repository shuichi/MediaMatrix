package mediamatrix.gui;

public interface JVMMemoryProfilerListener {
    void addScore(long total, long free);
}