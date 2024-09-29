import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.KeyListener;

enum AppStateID {
    MAIN_MENU_STATE,
    GAME_STATE,
}

public abstract class AppState implements KeyListener {
    Application _app;
    JFrame _frame;

    public abstract int update();
    public abstract int render();
    public abstract AppStateID state_id();
    public abstract void create_gui();
    public abstract void every_second();
}
