package mediamatrix.music;

public class Tempo {

    private final double time;
    private final double tempo;

    public Tempo(double time, double tempo) {
        this.time = time;
        this.tempo = tempo;
    }

    public double getTempo() {
        return tempo;
    }

    public double getTime() {
        return time;
    }
}
