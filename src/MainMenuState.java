import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

public class MainMenuState extends AppState {
    static final Dimension initial_dimensions = new Dimension(600, 400);

    public JPanel _menu_background = new JPanel();
    public JPanel _button_container;
    public JButton _start_button;
    public JButton _scoreboard_button;
    public JList _scoreboard;
    public JButton _back_to_menu;
    public JButton _quit_button;
    public JList _map_size_list;
    public JScrollPane _scroll_pane_map;
    public JScrollPane _scroll_pane_scoreboard;
    public JTextField _text_field;
    public JButton _add_entry_button;
    public JLabel _default_entry_label;

    public MainMenuState(Application app) {
        this._app = app;
        assert _app != null : "Application is null\n";
    }

    @Override
    public void every_second() {

    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public int update() {
        return 0;
    }

    @Override
    public int render() {
        return 0;
    }

    @Override
    public AppStateID state_id() {
        return AppStateID.MAIN_MENU_STATE;
    }

    @Override
    public void create_gui() {
        SwingUtilities.invokeLater(() -> {
            _menu_background = new JPanel();
            _menu_background.setVisible(true);
            _menu_background.setPreferredSize(initial_dimensions);
            _menu_background.setMinimumSize(initial_dimensions);
            _menu_background.setSize(initial_dimensions);
            _menu_background.setBackground(Color.BLACK);

            _text_field = new JTextField(16);
            _text_field.setVisible(false);

            _add_entry_button = new JButton("Save");
            _add_entry_button.setVisible(false);
            _add_entry_button.addActionListener(event -> {
                if(event.getSource() != _add_entry_button)
                    return;

                GameState gs = (GameState)_app.get_state(AppStateID.GAME_STATE);
                String username = _text_field.getText();
                int time = gs._time_passed;
                _app.add_scoreboard_entry(new ScoreboardEntryData(time, username));

                set_visibility_for_map_choice(false);
                set_visibility_for_victory_screen(false);
                set_visibility_for_scoreboard(false);
                set_visibility_for_menu(true);
            });

            _default_entry_label = new JLabel("Enter your username and save it!");
            _default_entry_label.setVisible(false);
            _default_entry_label.setForeground(Color.YELLOW);

            _start_button = new JButton("Start");
            _start_button.setBackground(Color.BLACK);
            _start_button.setForeground(Color.YELLOW);
            _start_button.setVisible(true);
            _start_button.addActionListener(event -> {
                if(event.getSource() != _start_button)
                    return;

                set_visibility_for_menu(false);
                set_visibility_for_scoreboard(false);
                set_visibility_for_victory_screen(false);
                set_visibility_for_map_choice(true);
            });

            _scoreboard_button = new JButton("Scoreboard");
            _scoreboard_button.setBackground(Color.BLACK);
            _scoreboard_button.setForeground(Color.YELLOW);
            _scoreboard_button.setVisible(true);
            _scoreboard_button.addActionListener(event -> {
                if(event.getSource() != _scoreboard_button)
                    return;

                ScoreboardEntryData[] scoreboard_data = new ScoreboardEntryData[_app._scoreboard_data.size()];
                _app._scoreboard_data.toArray(scoreboard_data);
                _scoreboard.setListData(scoreboard_data);

                set_visibility_for_menu(false);
                set_visibility_for_map_choice(false);
                set_visibility_for_victory_screen(false);
                set_visibility_for_scoreboard(true);
            });

            _quit_button = new JButton("Quit");
            _quit_button.setBackground(Color.BLACK);
            _quit_button.setForeground(Color.YELLOW);
            _quit_button.setVisible(true);
            _quit_button.addActionListener(event -> {
                if(event.getSource() != _quit_button)
                    return;
                _app.handle_exit(0);
            });

            _button_container = new JPanel();
            _button_container.setVisible(true);
            _button_container.setBackground(Color.BLACK);
            _button_container.add(_start_button, new BorderLayout());
            _button_container.add(_scoreboard_button, new BorderLayout());
            _button_container.add(_quit_button, new BorderLayout());

            String[] map_choices = { "Back", "16x16 2 opponents", "24x24 3 opponents", "32x32 4 opponents", "40x40 5 opponents", "48x48 8 opponents" };
            _map_size_list = new JList(map_choices);
            _map_size_list.setVisible(false);
            _map_size_list.setBackground(Color.BLACK);
            _map_size_list.setForeground(Color.YELLOW);
            _map_size_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            _map_size_list.addListSelectionListener(element -> {
                String button_text = _map_size_list.getSelectedValue().toString();
                if(button_text.equals("Back")) {
                    set_visibility_for_victory_screen(false);
                    set_visibility_for_scoreboard(false);
                    set_visibility_for_map_choice(false);
                    set_visibility_for_menu(true);
                    return;
                }

                // Filename format is pacman_<size_x>x<size_y>.png
                String[] size_data_str = button_text.split(" ")[0].split("x");
                String opponent_count_str = button_text.split(" ")[1];

                // We read maze size information from the filename
                String filename = "maze_" + size_data_str[0] + "x" + size_data_str[1] + ".png";
                int opponents = Integer.parseInt(opponent_count_str);

                System.out.println("Attempt to load map: '" + filename + "' with " + opponents + " opponents");

                GameState state = (GameState)_app.get_state(AppStateID.GAME_STATE);
                state.set_current_maze(filename, opponents);

                _frame.setVisible(false);
                _app.change_state(AppStateID.GAME_STATE);
            });

            _scoreboard = new JList();
            _scoreboard.setVisible(true);
            _scoreboard.setBackground(Color.BLACK);
            _scoreboard.setForeground(Color.YELLOW);

            _back_to_menu = new JButton("Back");
            _back_to_menu.setVisible(false);
            _back_to_menu.setBackground(Color.BLACK);
            _back_to_menu.setForeground(Color.YELLOW);
            _back_to_menu.addActionListener(event -> {
                if(event.getSource() != _back_to_menu)
                    return;
                set_visibility_for_map_choice(false);
                set_visibility_for_victory_screen(false);
                set_visibility_for_scoreboard(false);
                set_visibility_for_menu(true);
            });

            _menu_background.add(_back_to_menu, Component.CENTER_ALIGNMENT);

            _scroll_pane_scoreboard = new JScrollPane(_scoreboard);
            _scroll_pane_scoreboard.setVisible(false);

            _scroll_pane_map = new JScrollPane(_map_size_list);
            _scroll_pane_map.setVisible(false);

            _frame = new JFrame("Pacman | Menu");
            _frame.setLayout(new GridBagLayout());
            _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            _frame.setVisible(true);
            _frame.setResizable(false);
            _frame.addKeyListener(this);
            _frame.setResizable(true);
            _frame.setPreferredSize(new Dimension(600, 400));
            _frame.setSize(new Dimension(600, 400));
            _frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    super.componentResized(e);
                    _menu_background.setSize(_frame.getSize());
                    _menu_background.setPreferredSize(_frame.getSize());
                    _menu_background.setMinimumSize(_frame.getSize());
                }
            });

            Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension window_size = _frame.getSize();
            _frame.setLocation(screen_size.width / 2 - window_size.width / 2, screen_size.height / 2 - window_size.height / 2);

            _menu_background.add(_button_container);

            _menu_background.add(_default_entry_label, Component.CENTER_ALIGNMENT);
            _menu_background.add(_add_entry_button, Component.CENTER_ALIGNMENT);
            _menu_background.add(_text_field, Component.CENTER_ALIGNMENT);

            _menu_background.add(_scroll_pane_map, Component.CENTER_ALIGNMENT);
            _menu_background.add(_scroll_pane_scoreboard, Component.CENTER_ALIGNMENT);

            _frame.add(_menu_background);

            _frame.pack();
        });
    }

    public void set_visibility_for_menu(boolean visibility) {
        _button_container.setVisible(visibility);
        _scoreboard_button.setVisible(visibility);
        _start_button.setVisible(visibility);
        _quit_button.setVisible(visibility);
    }

    public void set_visibility_for_map_choice(boolean visibility) {
        _map_size_list.setVisible(visibility);
        _scroll_pane_map.setVisible(visibility);
    }

    public void set_visibility_for_scoreboard(boolean visibility) {
        _scoreboard_button.setVisible(visibility);
        _scoreboard.setVisible(visibility);
        _scroll_pane_scoreboard.setVisible(visibility);
        _back_to_menu.setVisible(visibility);
    }

    public void set_visibility_for_victory_screen(boolean visibility) {
        _text_field.setVisible(visibility);
        _add_entry_button.setVisible(visibility);
        _default_entry_label.setVisible(visibility);
    }
}