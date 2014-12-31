package mediamatrix.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class JVMMemoryProfiler {
    
    private List<JVMMemoryProfilerListener> listeners;
    private Timer aTimer;
    private TimerTask profilerTask;
    private long interval;
    
    
    public JVMMemoryProfiler(long interval) {
        this.interval = interval;
        aTimer = new Timer(true);
        listeners = new ArrayList<JVMMemoryProfilerListener>();
    }
    
    
    public void start() {
        if (profilerTask == null) {
            profilerTask = new TimerTask() {
                public void run() {
                    long t = Runtime.getRuntime().totalMemory();
                    long f = Runtime.getRuntime().freeMemory();
                    addScore(t, f);
                }
            };
        }
        aTimer.schedule(profilerTask, 0, interval);
    }
    
    
    public void stop() {
        profilerTask.cancel();
        profilerTask = null;
    }
    

    public void addScore(long total, long free) {
        for (Iterator<JVMMemoryProfilerListener> iter = listeners.iterator(); iter.hasNext();) {
            JVMMemoryProfilerListener lis =iter.next();
            lis.addScore(total, free);
        }
    }
    

    public void addListener(JVMMemoryProfilerListener listener) {
        this.listeners.add(listener);
    }
    

    public boolean removeListener(JVMMemoryProfilerListener listener) {
        return listeners.remove(listener);
    }
}