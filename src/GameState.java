import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameState extends AppState {
    protected MapData _map;

    JFrame _frame;
    JLabel _time_label;
    JLabel _score_label;
    JLabel _pacman_lives_label;
    JLabel _active_buffs_label;
    JLabel _buff_hungry_pacman_label;
    JLabel _buff_immune_label;
    JLabel _buff_score_label;
    JPanel _info_panel;
    Dimension _initial_window_size;
    Dimension _initial_map_size;

    Thread _animation_thread;
    int _time_passed;

    public GameState(Application app) {
        this._app = app;
        assert _app != null : "Application is null\n";
    }

    @Override
    public int update() {
        if(_map == null) {
            System.out.println("Map is null\n");
            return 1;
        }

        _map.update();

        if(_map.player_score() >= _map.score_to_win()) {
            System.out.println("Player won in " + _time_passed + " seconds!");
            _frame.setVisible(false);
            _app.change_state(AppStateID.MAIN_MENU_STATE);

            MainMenuState menu = (MainMenuState)_app.get_state(AppStateID.MAIN_MENU_STATE);
            menu.set_visibility_for_menu(false);
            menu.set_visibility_for_scoreboard(false);
            menu.set_visibility_for_map_choice(false);
            menu.set_visibility_for_victory_screen(true);
        }

        if(_map._player.death_count() >= Sprite.TOTAL_LIVES) {
            System.out.println("Player lost");
            _frame.setVisible(false);
            _app.change_state(AppStateID.MAIN_MENU_STATE);
        }

        return 0;
    }

    @Override
    public int render() {
        if(_time_label != null) _time_label.setText("Time: " + _time_passed + "s");
        if(_score_label != null) _score_label.setText("Score: " + _map.player_score() + "/" + _map.score_to_win());
        if(_pacman_lives_label != null) _pacman_lives_label.setText("Lives: " + Math.abs(_map._player.death_count() - Sprite.TOTAL_LIVES));
        if(_map._player.has_any_timed_buff()
            && _active_buffs_label != null
            && _buff_immune_label != null
            && _buff_hungry_pacman_label != null
            && _buff_score_label != null)
        {
            if(_map._player.has_immune_buff())
                _buff_immune_label.setText("Immune buff: " + _map._player._ghosts_dont_eat_player_buff_time + "s");
            if(_map._player.has_hungry_pacman_buff())
               _buff_hungry_pacman_label.setText("Hungry buff: " + _map._player._can_eat_ghosts_buff_time + "s");
            if(_map._player.has_doubled_score_buff())
                _buff_score_label.setText("\nDouble score buff: " + _map._player._doubled_coin_score_buff_time + "s");
            _active_buffs_label.setVisible(_map._player.has_any_timed_buff());
            _buff_immune_label.setVisible(_map._player.has_immune_buff());
            _buff_hungry_pacman_label.setVisible(_map._player.has_hungry_pacman_buff());
            _buff_score_label.setVisible(_map._player.has_doubled_score_buff());
        }

        if(_frame != null)
            _frame.pack();
        return 0;
    }

    @Override
    public AppStateID state_id() {
        return AppStateID.GAME_STATE;
    }

    @Override
    public void create_gui() {
        assert (_map != null) : "Map is null\n";

        SwingUtilities.invokeLater(() -> {
            // Create labels to display info

            _time_label = new JLabel();
            _time_label.setForeground(Color.YELLOW);

            _score_label = new JLabel();
            _score_label.setForeground(Color.YELLOW);

            _pacman_lives_label = new JLabel();
            _pacman_lives_label.setForeground(Color.YELLOW);

            _active_buffs_label = new JLabel("Active buffs:");
            _active_buffs_label.setForeground(Color.YELLOW);
            _active_buffs_label.setVisible(false);

            _buff_hungry_pacman_label = new JLabel();
            _buff_hungry_pacman_label.setForeground(Color.YELLOW);
            _buff_hungry_pacman_label.setVisible(false);

            _buff_immune_label = new JLabel();
            _buff_immune_label.setForeground(Color.YELLOW);
            _buff_immune_label.setVisible(false);

            _buff_score_label = new JLabel();
            _buff_score_label.setForeground(Color.YELLOW);
            _buff_score_label.setVisible(false);

            _info_panel = new JPanel();
            _info_panel.setLayout(new FlowLayout(FlowLayout.CENTER));
            _info_panel.setVisible(true);
            _info_panel.setBackground(Color.BLACK);
            Dimension map_dimensions = _map.getPreferredSize();
            _info_panel.setPreferredSize(new Dimension(map_dimensions.width / 3, map_dimensions.height));
            _info_panel.add(_score_label);
            _info_panel.add(_time_label);
            _info_panel.add(_pacman_lives_label);
            _info_panel.add(_active_buffs_label);
            _info_panel.add(_buff_hungry_pacman_label);
            _info_panel.add(_buff_immune_label);
            _info_panel.add(_buff_score_label);

            JMenuBar menu = new JMenuBar();
            menu.setVisible(true);
            menu.setBackground(Color.BLACK);

            JMenu menu_button = new JMenu();
            menu_button.setVisible(true);
            menu_button.setText("Quit");
            menu_button.setBackground(Color.BLACK);
            menu_button.setForeground(Color.YELLOW);

            JMenuItem menu_exit = new JMenuItem("Quit to menu");
            menu_exit.setBackground(Color.BLACK);
            menu_exit.setForeground(Color.YELLOW);
            menu_exit.addActionListener(e -> {
                _frame.setVisible(false);
                _app.change_state(AppStateID.MAIN_MENU_STATE);
            });

            JMenuItem desktop_exit = new JMenuItem("Quit to desktop");
            desktop_exit.setBackground(Color.BLACK);
            desktop_exit.setForeground(Color.YELLOW);
            desktop_exit.addActionListener(e -> _app.handle_exit(0));

            menu_button.add(menu_exit);
            menu_button.add(desktop_exit);
            menu.add(menu_button);

            // Create a new window and display the before-chosen maze.
            _frame = new JFrame("Pacman | Good luck!");
            _frame.setForeground(Color.YELLOW);
            _frame.setLayout(new BorderLayout());
            _frame.add(_map, BorderLayout.WEST);
            _frame.add(_info_panel, BorderLayout.EAST);
            _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            _frame.setVisible(true);
            _frame.setResizable(false);
            _frame.addKeyListener(this);
            _frame.setJMenuBar(menu);
            _frame.pack();

            Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension window_size = _frame.getSize();
            _frame.setLocation(screen_size.width / 2 - window_size.width / 2, screen_size.height / 2 - window_size.height / 2);

            _initial_window_size = _frame.getSize();
            _initial_map_size = _map.getSize();

        });
    }

    @Override
    public void every_second() {
        ++this._time_passed;
        --this._map._summon_ghost_counter;
        try_spawn_buff();
        if(_map._player.has_any_timed_buff())
            _map._player.remove_timed_buff_seconds(1);
        if(_map._summon_ghost_counter == 0) {
            _map.resurrect_ghosts();
        }
    }

    private void create_animation_thread() {
        _animation_thread = new Thread(_map._player);
        _animation_thread.start();
    }

    public void set_current_maze(String maze_filename, int opponents) {
        _map = new MapData(maze_filename, opponents);
        create_animation_thread();
    }

    private void try_spawn_buff() {
        if(
            this._time_passed % 2 != 0
            || (int)(Math.random() * 100) % 4 != 0
            || _map.buff_count() == _map.buffs_created()
            || _map.coin_count() == _map.coins_created()
        ) return;

        int left = 0;
        int right = _map.width() * _map.height();
        int buff_spawn_position = -1;
        while(left < right) {
            // If not for those two semi-random values the "algorithm" would place buffs
            // with a bias towards a certain point on the map
            // It gets the job done good enough

            int random_offset = (int)(Math.random() * _map.coins_created());
            int random_side_preference = (_time_passed + random_offset) % 2;

            if(_map.can_move(left + random_offset) && !_map.is_coin(left + random_offset) && !_map.is_buff(left + random_offset) && random_side_preference == 0) {
                buff_spawn_position = left + random_offset;
                break;
            }

            else if(_map.can_move(right - random_offset) && !_map.is_coin(right - random_offset) && !_map.is_buff(right - random_offset) && random_side_preference == 1) {
                buff_spawn_position = right - random_offset;
                break;
            }

            ++left;
            --right;
        }

        if(buff_spawn_position != -1) {
            System.out.println("Attempt to spawn buff at: " + buff_spawn_position % _map.width() + " " + buff_spawn_position / _map.height());
            _map.create_buff(buff_spawn_position % _map.width(), buff_spawn_position / _map.height());

            JPanel buff = _map._buffs.get(buff_spawn_position);
            _map._map_panels[buff_spawn_position].add(buff);
        }
        else System.out.println("Failed to find good spot for buff");
    }

    private void resize_by(double scale) {
        Dimension size = _initial_window_size;
        double new_scale = _map.draw_scale() + scale;

        if(new_scale < 1.0 || new_scale > 2.0)
            return;

        Dimension new_window_size = new Dimension((int)(size.width * new_scale), (int)(size.height * new_scale));
        _frame.setSize(new_window_size);

        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        _frame.setLocation(screen_size.width / 2 - new_window_size.width / 2, screen_size.height / 2 - new_window_size.height / 2);

        _map.set_draw_scale(new_scale);
        _frame.pack();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key_code = e.getKeyCode();
        switch(key_code) {
            case KeyEvent.VK_W:
                _map._player.set_direction(MoveDirection.UP); break;
            case KeyEvent.VK_S:
                _map._player.set_direction(MoveDirection.DOWN); break;
            case KeyEvent.VK_A:
                _map._player.set_direction(MoveDirection.LEFT); break;
            case KeyEvent.VK_D:
                _map._player.set_direction(MoveDirection.RIGHT); break;
            case KeyEvent.VK_ESCAPE:
                _app.handle_exit(0);
            case KeyEvent.VK_UP:
                System.out.println("Resize up");
                resize_by(0.1);
                break;
            case KeyEvent.VK_DOWN:
                System.out.println("Resize down");
                resize_by(-0.1);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

}