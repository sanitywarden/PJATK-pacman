import java.io.Serializable;

public class ScoreboardEntryData implements Serializable {
    private int    _time_s; // Time (in seconds) it took the player to win
    private String _name;   // Player name

    public ScoreboardEntryData(int time, String name) {
        _time_s = time;
        _name = name;
    }

    public int time() {
        return _time_s;
    }

    public String name() {
        return _name;
    }

    @Override
    public String toString() {
        return time() + " " + name();
    }
}
