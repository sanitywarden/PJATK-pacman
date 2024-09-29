import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Application {
    public final static int DESIRED_UPDATES = 10;
    public final static int ONE_SECOND_MS = 1000;
    public final static int TIME_PER_UPDATE_MS = ONE_SECOND_MS / DESIRED_UPDATES;
    public static final int TILE_SIZE = 16;

    private static Application _app;
    private AppState _current_state;
    private final HashMap<AppStateID, AppState> _states;
    public ArrayList<ScoreboardEntryData> _scoreboard_data;

    private Application() {
        this._states = new HashMap<>();
        this._scoreboard_data = read_scoreboard();

        add_state(new MainMenuState(this));
        add_state(new GameState(this));

        change_state(AppStateID.MAIN_MENU_STATE);
    }

    public static Application get_instance() {
        if(_app == null) _app = new Application();
        return _app;
    }

    public void handle_exit(int code) {
        if(code == 0)
            System.out.println("Everything good");
        else System.out.println("Something went wrong: code " + code);
        System.out.println("Scoreboard: " + _scoreboard_data);
        save_game_result_entry(_scoreboard_data);
        System.exit(code);
    }

    public static BufferedImage load_image(String filename) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filename));
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        if(image == null)
            System.out.println("Failed to load image: " + filename);
        else System.out.println("Successfully loaded '" + filename + "'");
        return image;
    }

    public int run() {
        int code = 0;
        int time_since_last_second_ms = 0;
        while(true) {
            code = _current_state.update();
            if (code != 0) {
                System.out.println("Error from " + _current_state.state_id() + ".update()");
                return code;
            }

            code = _current_state.render();
            if (code != 0) {
                System.out.println("Error from " + _current_state.state_id() + ".render()");
                return code;
            }

            try {
                Thread.sleep(TIME_PER_UPDATE_MS);
                time_since_last_second_ms += TIME_PER_UPDATE_MS;
                if(time_since_last_second_ms >= ONE_SECOND_MS) {
                    time_since_last_second_ms %= ONE_SECOND_MS;
                    _current_state.every_second();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return code;
            }
        }
    }

    public void add_scoreboard_entry(ScoreboardEntryData entry) {
        _scoreboard_data.add(entry);
    }

    public void add_state(AppState state) {
        _states.put(state.state_id(), state);
    }

    public AppState get_state(AppStateID state_id) {
        return _states.get(state_id);
    }

    public void change_state(AppStateID state_id) {
        System.out.println(_current_state == null
                ? "Changing state to: " + state_id
                : "Changing state from: " + _current_state.state_id() + " to " + state_id
        );

        // Change the state
        _current_state = get_state(state_id);

        // If it's the first time it is used, create the GUI
        // and make sure to switch it on
        if(_current_state._frame == null)
            _current_state.create_gui();

        if(_current_state._frame != null)
            _current_state._frame.setVisible(true);
    }

    public void save_game_result_entry(ArrayList<ScoreboardEntryData> entries) {
        try {
            System.out.println("Attempting to save scoreboard data");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./scoreboard.serialised"));
            for(ScoreboardEntryData entry : entries)
                out.writeObject(entry);
        } catch (FileNotFoundException e) {
            System.out.println("save_game_result_entry(): File not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("save_game_result_entry(): IO Exception: " + e.getMessage());
        }
    }

    public ArrayList<ScoreboardEntryData> read_scoreboard() {
        ArrayList<ScoreboardEntryData> entries = new ArrayList<>();

        try {
            System.out.println("Attempting to read scoreboard data");
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("./scoreboard.serialised"));
            while(true) {
                try {
                    ScoreboardEntryData entry = (ScoreboardEntryData)in.readObject();
                    entries.add(entry);
                } catch (EOFException e) {
                    System.out.println("End of file reached");
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("read_scoreboard(): File not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("read_scoreboard(): IO Exception: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("read_scoreboard(): ClassNotFoundException: " + e.getMessage());
        }

        return entries;
    }
}
