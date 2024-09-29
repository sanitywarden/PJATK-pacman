import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

enum MoveDirection {
    RIGHT,
    LEFT,
    UP,
    DOWN
}

enum SpriteType {
    PLAYER,
    GHOST
}

enum AnimationState {
    MOUTH_OPEN,
    MOUTH_CLOSED
}

public class Sprite extends JPanel implements Runnable {
    private static final int ANIMATION_CHANGE_COOLDOWN = 2;
    public static final int TOTAL_LIVES = 3;
    private static final HashMap<String, BufferedImage> _sprite_textures = load_textures();

    public int            _spawn_location;
    public int            _current_tile_index;
    public int            _current_tile_x;
    public int            _current_tile_y;
    public AnimationState _animation_state;
    public int            _animation_cooldown;
    BufferedImage         _sprite_texture;
    ImageIcon             _sprite_icon;
    JLabel                _image_label;
    public MoveDirection  _direction;
    public MoveDirection  _previous_direction;
    SpriteType            _type;
    int                   _death_count;
    int                   _score;

    /* Buffs:
     *   1: Give player additional life
     *   2: Give player additional score
     *   3: Ghosts don't eat the player
     *   4: Points from eating coins are doubled for a while
     *   5: Can eat the ghosts for a while
     *   You earn a buff by eating a star that appears on the map somewhere
     *   The buff you earn is random */
    int _ghosts_dont_eat_player_buff_time;
    int _doubled_coin_score_buff_time;
    int _can_eat_ghosts_buff_time;

    public Sprite(int position, MoveDirection direction, SpriteType type) {
        this.setOpaque(false);

        _spawn_location = position;
        _current_tile_index = position;
        _direction = direction;
        _type = type;
        _animation_state = AnimationState.MOUTH_CLOSED;
        _animation_cooldown = ANIMATION_CHANGE_COOLDOWN;
        _death_count = 0;
        _sprite_texture = get_valid_sprite_texture();
        _sprite_icon = new ImageIcon(_sprite_texture);
        _image_label = new JLabel(_sprite_icon);

        this.add(_image_label);
        this.setLayout(new GridLayout(1, 1));
        this.setPreferredSize(new Dimension(Application.TILE_SIZE, Application.TILE_SIZE));
        this.setSize(new Dimension(Application.TILE_SIZE, Application.TILE_SIZE));
        this.setVisible(true);
    }

    public void grant_score(int score) {
        _score += score;
    }

    public int score() {
        return _score;
    }

    public void set_position(int index) {
        _current_tile_index = index;
    }

    public void reset_position() {
        _current_tile_index = _spawn_location;
    }

    public boolean has_hungry_pacman_buff() {
        return _can_eat_ghosts_buff_time > 0;
    }

    public boolean has_doubled_score_buff() {
        return _doubled_coin_score_buff_time > 0;
    }

    public boolean has_immune_buff() {
        return _ghosts_dont_eat_player_buff_time > 0;
    }

    public boolean has_any_timed_buff() {
        return has_hungry_pacman_buff() || has_doubled_score_buff() || has_immune_buff();
    }

    public void remove_timed_buff_seconds(int time) {
        _ghosts_dont_eat_player_buff_time -= time;
        _doubled_coin_score_buff_time     -= time;
        _can_eat_ghosts_buff_time         -= time;

        // Make sure that all timers have positive time!
        _ghosts_dont_eat_player_buff_time = Math.max(_ghosts_dont_eat_player_buff_time, 0);
        _doubled_coin_score_buff_time     = Math.max(_doubled_coin_score_buff_time, 0);
        _can_eat_ghosts_buff_time         = Math.max(_can_eat_ghosts_buff_time, 0);
    }

    public int position() { return _current_tile_index; }
    public MoveDirection direction() { return _direction; }
    public boolean is_ai() { return _type == SpriteType.GHOST; }
    public boolean is_player() { return _type == SpriteType.PLAYER; }

    public void move(MapData maze) {
        switch(direction()) {
            case UP -> {
                if(maze.can_move(_current_tile_index - maze.width())) {
                    set_position(_current_tile_index - maze.width());
                    if(_previous_direction != MoveDirection.UP)
                        force_texture_update();
                }
                else if(is_ai()) set_random_direction();
            }
            case DOWN -> {
                if(maze.can_move(_current_tile_index + maze.width())) {
                    set_position(_current_tile_index + maze.width());
                    if(_previous_direction != MoveDirection.DOWN)
                        force_texture_update();
                }
                else if(is_ai()) set_random_direction();
            }
            case LEFT -> {
                if(maze.can_move(_current_tile_index - 1)) {
                    set_position(_current_tile_index - 1);
                    if(_previous_direction != MoveDirection.LEFT)
                        force_texture_update();
                }
                else if(is_ai()) set_random_direction();
            }
            case RIGHT -> {
                if(maze.can_move(_current_tile_index + 1)) {
                    set_position(_current_tile_index + 1);
                    if(_previous_direction != MoveDirection.RIGHT)
                        force_texture_update();
                }
                else if(is_ai()) set_random_direction();
            }
        }

        _current_tile_x = _current_tile_index % maze.width();
        _current_tile_y = _current_tile_index / maze.height();
        this.setBounds(_current_tile_x * Application.TILE_SIZE, _current_tile_y * Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE);
        maze._map_panels[_current_tile_index].add(this);
    }

    private static HashMap<String, BufferedImage> load_textures() {
        HashMap<String, BufferedImage> textures = new HashMap<>();

        BufferedImage pacman_move_states = Application.load_image("./pacman_move_states.png");
        textures.put("pacman_open_right", pacman_move_states.getSubimage(0, 0, Application.TILE_SIZE, Application.TILE_SIZE));
        textures.put("pacman_open_left" , pacman_move_states.getSubimage(Application.TILE_SIZE, 0, Application.TILE_SIZE, Application.TILE_SIZE));
        textures.put("pacman_open_down" , pacman_move_states.getSubimage(2 * Application.TILE_SIZE, 0, Application.TILE_SIZE, Application.TILE_SIZE));
        textures.put("pacman_open_up"   , pacman_move_states.getSubimage(3 * Application.TILE_SIZE, 0, Application.TILE_SIZE, Application.TILE_SIZE));

        textures.put("pacman_closed_right", pacman_move_states.getSubimage(0, Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE));
        textures.put("pacman_closed_left" , pacman_move_states.getSubimage(Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE));
        textures.put("pacman_closed_down" , pacman_move_states.getSubimage(2 * Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE));
        textures.put("pacman_closed_up"   , pacman_move_states.getSubimage(3 * Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE));

        BufferedImage pacman_ghosts = Application.load_image("./pacman_ghosts.png");
        textures.put("ghost_0", pacman_ghosts.getSubimage(0, 0, Application.TILE_SIZE, Application.TILE_SIZE));
        textures.put("ghost_1", pacman_ghosts.getSubimage(Application.TILE_SIZE, 0, Application.TILE_SIZE, Application.TILE_SIZE));
        textures.put("ghost_2", pacman_ghosts.getSubimage(2 * Application.TILE_SIZE, 0, Application.TILE_SIZE, Application.TILE_SIZE));
        textures.put("ghost_3", pacman_ghosts.getSubimage(3 * Application.TILE_SIZE, 0, Application.TILE_SIZE, Application.TILE_SIZE));

        return textures;
    }

    private void force_texture_update() {
        _animation_cooldown = 0;
    }

    private BufferedImage get_valid_sprite_texture() {
        if(is_player()) {
            if(_animation_state == AnimationState.MOUTH_OPEN) {
                switch(_direction) {
                    case UP ->    { return _sprite_textures.get("pacman_open_up"); }
                    case DOWN ->  { return _sprite_textures.get("pacman_open_down"); }
                    case LEFT ->  { return _sprite_textures.get("pacman_open_left"); }
                    case RIGHT -> { return _sprite_textures.get("pacman_open_right"); }
                }
            }

            else {
                switch(_direction) {
                    case UP ->    { return _sprite_textures.get("pacman_closed_up"); }
                    case DOWN ->  { return _sprite_textures.get("pacman_closed_down"); }
                    case LEFT ->  { return _sprite_textures.get("pacman_closed_left"); }
                    case RIGHT -> { return _sprite_textures.get("pacman_closed_right"); }
                }
            }
        }

        int random_ghost_texture_id = (int)(Math.random() * MapData.GHOST_TEXTURE_COUNT);
        String ghost_texture_name = "ghost_" + random_ghost_texture_id;
        return _sprite_textures.get(ghost_texture_name);
    }

    public void set_direction(MoveDirection direction) {
        _previous_direction = _direction;
        _direction = direction;
    }

    public void set_random_direction() {
        _previous_direction = _direction;
        int random_choice = (int)(Math.random() * 4);
        switch(random_choice) {
            case 0 -> _direction = MoveDirection.UP;
            case 1 -> _direction = MoveDirection.DOWN;
            case 2 -> _direction = MoveDirection.LEFT;
            case 3 -> _direction = MoveDirection.RIGHT;
        }
    }

    public void die() {
        ++this._death_count;
        this.setVisible(false);
    }

    public int death_count() {
        return this._death_count;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Application.TILE_SIZE, Application.TILE_SIZE);
    }

    @Override
    public void run() {
        while(true) {
            if(_animation_cooldown-- == 0) {
                _animation_cooldown = ANIMATION_CHANGE_COOLDOWN;
                if(_animation_state == AnimationState.MOUTH_OPEN)
                    _animation_state = AnimationState.MOUTH_CLOSED;
                else _animation_state = AnimationState.MOUTH_OPEN;

                this.remove(_image_label);

                // Give the sprite a new texture
                _sprite_texture = get_valid_sprite_texture();
                _sprite_icon = new ImageIcon(_sprite_texture);
                _image_label = new JLabel(_sprite_icon);

                this.add(_image_label);
            }

            try {
                Thread.sleep(Application.TIME_PER_UPDATE_MS);
            } catch (InterruptedException e) {
                System.out.println("From Sprite.run():" + e.getMessage());
            }
        }
    }

    public boolean is_alive() {
        return this.isVisible();
    }
}
