import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

enum TileType {
    TILE_PATH, // Walkable tile
    TILE_WALL, // Unpassable tile
    TILE_SPAWN, // Tile where ghosts spawn initially
    TILE_PACMAN_SPAWN // Tile where the player ghost will spawn initially
}

public class MapData extends JPanel {
    public static final int  GHOST_TEXTURE_COUNT = 4; // Textures to choose from for ghost sprites
    private static final int SCORE_EAT_COIN = 1;
    private static final int SCORE_EAT_GHOST = 3;
    private static final int BUFF_GRANT_SCORE = 5;
    private static final int BUFF_GRANT_TIME = 10;
    private static final HashMap<String, BufferedImage> _textures = load_textures();

    int                _width;
    int                _height;
    TileType[]         _maze_data;
    JPanel[]           _map_panels;      // Panels which create the map
    HashMap<Integer, JPanel> _buffs;     // Buff locations in the maze
    HashMap<Integer, JPanel> _coins;     // Current coins on the map
    int                _coins_created;   // Amount of coins created - pacman has to eat all of them to win
    ArrayList<Integer> _ghost_spawns;    // Positions of tiles where the ghosts may spawn
    int                _player_spawn;    // Position of a tile where the player spawn spot is
    public Sprite            _player;
    public ArrayList<Sprite> _ghosts;
    int                      _summon_ghost_counter;
    int                      _opponent_count;
    int                      _buff_count;

    private double _draw_scale;

    public MapData(String maze_filename, int opponents) {
        _opponent_count = opponents;
        _buff_count = opponents;

        set_draw_scale(1.0);
        load_map(maze_filename);
        create_map();
        create_ghosts();
    }

    private static HashMap<String, BufferedImage> load_textures() {
        HashMap<String, BufferedImage> textures = new HashMap<>();

        textures.put("coin", Application.load_image("coin.png"));
        textures.put("buff", Application.load_image("buff.png"));

        return textures;
    }

    private void reset_sprite_state() {
        _player.reset_position();
        _player.setVisible(true);
        for(Sprite sprite : _ghosts) {
            sprite.setVisible(true);
            sprite.reset_position();
        }
    }

    private void create_map() {
        this._map_panels = new JPanel[_width * _height];
        this.setLayout(new GridLayout(_width, _height));
        this.setPreferredSize(this.getPreferredSize());
        this.setVisible(true);
        this.setBackground(Color.BLACK);

        for(int y = 0; y < _height; y++) {
            for(int x = 0; x < _width; x++) {
                int index = y * _width + x;
                JPanel panel = new JPanel();
                panel.setLayout(new GridLayout(1, 1));
                panel.setVisible(true);
                panel.setPreferredSize(new Dimension(Application.TILE_SIZE, Application.TILE_SIZE));
                panel.setSize(new Dimension(Application.TILE_SIZE, Application.TILE_SIZE));
                panel.setBounds(x * Application.TILE_SIZE, y * Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE);

                switch(tile_at(x, y)) {
                    case TILE_PACMAN_SPAWN:
                    case TILE_SPAWN:
                    case TILE_PATH:
                        panel.setBackground(Color.BLACK);
                        break;
                    case TILE_WALL:
                        panel.setBackground(Color.BLUE);
                        break;
                }

                _map_panels[index] = panel;
                this.add(panel, new GridLayout(1, 1));

                if(is_coin(x, y)) {
                    JPanel coin = _coins.get(index);
                    _map_panels[index].add(coin);
                }
            }
        }
    }

    private void create_coin(int x, int y) {
        JPanel coin = new JPanel();
        coin.setOpaque(false);

        ImageIcon coin_icon = new ImageIcon(_textures.get("coin"));
        JLabel coin_label = new JLabel(coin_icon);

        coin.add(coin_label);
        coin.setBounds(x * Application.TILE_SIZE, y * Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE);
        coin.setPreferredSize(new Dimension(Application.TILE_SIZE, Application.TILE_SIZE));
        coin.setSize(new Dimension(Application.TILE_SIZE, Application.TILE_SIZE));

        int index = y * width() + x;
        _coins.put(index, coin);
    }

    private void create_ghosts() {
        _summon_ghost_counter = 0;
        _player = new Sprite(this.player_spawn(), MoveDirection.LEFT, SpriteType.PLAYER);
        _ghosts = new ArrayList<>(_opponent_count);

        _player.setVisible(true);
        JPanel panel = _map_panels[this._player_spawn];

        panel.add(_player, new GridLayout(1, 1));

        for (int i = 0; i < _opponent_count; i++)
            summon_ghost();
        System.out.println("Created " +  _ghosts.size() + "/" + _opponent_count + " ghosts");
    }

    public void resurrect_ghosts() {
        for(Sprite ghost : _ghosts) {
            ghost.setVisible(true);
        }
    }

    public void summon_ghost() {
        int valid_index = _ghost_spawns.get(((int)(Math.random() * _ghost_spawns.size())));
        Sprite ghost = new Sprite(valid_index, MoveDirection.LEFT, SpriteType.GHOST);
        ghost.setVisible(true);
        _ghosts.add(ghost);
        _map_panels[valid_index].add(ghost, new GridLayout(1, 1));
    }

    private void load_map(String maze_filename) {
        // Filename format is pacman_<size_x>x<size_y>.png
        String[] size_data_str = maze_filename.split("[_.]")[1].split("x");

        // We read maze size information from the filename
        int width  = Integer.parseInt(size_data_str[0]);
        int height = Integer.parseInt(size_data_str[1]);

        if(width <= 0 || height <= 0)
            throw new RuntimeException("Could not read width and height from filename");

        this._width = width;
        this._height = height;
        this._maze_data = new TileType[width * height];
        this._ghost_spawns = new ArrayList<>();
        this._coins = new HashMap<>();
        this._buffs = new HashMap<>();

        BufferedImage maze_image_data = Application.load_image(maze_filename);
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int colour = maze_image_data.getRGB(x, y);
                int index = y * width() + x;

                if(colour == Color.WHITE.getRGB())       { this._maze_data[index] = TileType.TILE_PATH; }
                else if(colour == Color.YELLOW.getRGB()) { this._maze_data[index] = TileType.TILE_PATH; create_coin(x, y); }
                else if(colour == Color.BLACK.getRGB())  { this._maze_data[index] = TileType.TILE_WALL; }
                else if(colour == Color.RED.getRGB())    { this._maze_data[index] = TileType.TILE_SPAWN; this._ghost_spawns.add(index); }
                else if(colour == Color.GREEN.getRGB())  { this._maze_data[index] = TileType.TILE_PACMAN_SPAWN; this._player_spawn = index; }
                else throw new RuntimeException("Unknown colour in maze data. File = '" + maze_filename + "' at position (" + x + ";" + y + ")");
            }
        }

        this._coins_created = _coins.size();

        System.out.println("Map created from file '" + maze_filename + "' with dimensions [" + width() + ";" + height() + "]");
        System.out.println("Pacman starts at position (" + (this._player_spawn % width()) + ";" + (this._player_spawn / height()) + ")");
        System.out.println("Pacman has to earn " + _coins_created + " score to win");
    }

    public boolean in_bounds(int x, int y) { return x >= 0 && x < this.width() && y >= 0 && y < this.height(); }
    public boolean in_bounds(int index) { return in_bounds(index % width(), index / width()); }
    public int width()  { return _width; }
    public int height() { return _height; }
    public TileType tile_at(int x, int y) {
        assert (in_bounds(x, y)) : "No tile exists at coordinates (" + x + ", " + y + ")";
        return _maze_data[y * _width + x];
    }
    public TileType tile_at(int index) { return tile_at(index % width(), index / width()); }
    public boolean is_path(int x, int y) { return !is_wall(x, y); }
    public boolean is_wall(int x, int y) { return tile_at(x, y) == TileType.TILE_WALL; }
    public boolean can_move(int x, int y) { return in_bounds(x, y) && is_path(x, y) && !is_occupied(x, y); }
    public boolean can_move(int index) { return can_move(index % width(), index / width()); }
    public boolean is_occupied(int x, int y) {
        for(var ghost : _ghosts)
            if(ghost.position() == y * width() + x) return true;
        return _player.position() == y * width() + x;
    }
    public boolean is_coin(int x, int y) { return _coins.containsKey(y * width() + x); }
    public boolean is_coin(int index) { return is_coin(index % width(), index / width()); }
    public int player_spawn() { return _player_spawn; }
    public void remove_coin_at(int x, int y) {
        int index = y * width() + x;
        JPanel coin = _coins.get(index);
        this._map_panels[index].remove(coin);
        this._coins.remove(index);
    }
    public void remove_coin_at(int index) { remove_coin_at(index % width(), index / width()); }
    public int player_score() { return _player.score(); }
    public int score_to_win() { return _coins_created; }
    public int buff_count() { return _buffs.size(); }
    public void create_buff(int x, int y) {
        JPanel buff = new JPanel();
        buff.setOpaque(false);

        ImageIcon buff_icon = new ImageIcon(_textures.get("buff"));
        JLabel buff_label = new JLabel(buff_icon);

        buff.add(buff_label);
        buff.setBounds(x * Application.TILE_SIZE, y * Application.TILE_SIZE, Application.TILE_SIZE, Application.TILE_SIZE);
        buff.setPreferredSize(new Dimension(Application.TILE_SIZE, Application.TILE_SIZE));
        buff.setSize(new Dimension(Application.TILE_SIZE, Application.TILE_SIZE));

        int index = y * width() + x;
        _buffs.put(index, buff);
    }

    public void remove_buff_at(int index) {
        JPanel buff = _buffs.get(index);
        this._map_panels[index].remove(buff);
        this._buffs.remove(index);
    }
    public boolean is_buff(int index) { return _buffs.containsKey(index); }
    public int coin_count() { return _coins.size(); }
    public int coins_created() { return _coins_created; }
    public int buffs_created() { return _buff_count; }

    public boolean can_move(Sprite sprite, MoveDirection direction) {
        switch(direction) {
            case UP    -> { return can_move(sprite.position() - 1); }
            case DOWN  -> { return can_move(sprite.position() + 1); }
            case LEFT  -> { return can_move(sprite.position() - width()); }
            case RIGHT -> { return can_move(sprite.position() + width()); }
        }
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        int size_x = (int)(_width  * Application.TILE_SIZE * _draw_scale);
        int size_y = (int)(_height * Application.TILE_SIZE * _draw_scale);
        return new Dimension(size_x, size_y);
    }

    public double draw_scale() {
        return _draw_scale;
    }

    public void update() {
        for(Sprite ghost : _ghosts) {
            ghost.move(this);
        }
        _player.move(this);

        if(is_coin(_player.position())) {
            this.remove_coin_at(_player.position());
            if(_player.has_doubled_score_buff()) _player.grant_score(MapData.SCORE_EAT_COIN * 2);
            else _player.grant_score(MapData.SCORE_EAT_COIN);
        }

        if(is_buff(_player.position())) {
            this.remove_buff_at(_player.position());
            give_player_random_buff();
        }

        for(Sprite ghost : _ghosts) {
            if(
                ghost.position() == _player.position() ||
                ghost.position() + 1 == _player.position() ||
                ghost.position() - 1 == _player.position() ||
                ghost.position() + this.width() == _player.position() ||
                ghost.position() - this.width() == _player.position()
            ) {
                if(!ghost.is_alive())
                    continue;

                if(_player.has_hungry_pacman_buff()) {
                    ghost.die();
                    _summon_ghost_counter = _opponent_count; // The more opponents, the longer cooldown for them to spawn
                    _player.grant_score(MapData.SCORE_EAT_GHOST);
                }

                else if(!_player.has_immune_buff()) {
                    _player.die();
                    reset_sprite_state();
                }
                break;
            }
        }
    }

    private void give_player_random_buff() {
        int choice = (int)(Math.random() * 100) % 5;
        switch(choice) {
            case 0 -> _player._death_count--;
            case 1 -> _player._score += MapData.BUFF_GRANT_SCORE;
            case 2 -> _player._ghosts_dont_eat_player_buff_time += MapData.BUFF_GRANT_TIME;
            case 3 -> _player._doubled_coin_score_buff_time += MapData.BUFF_GRANT_TIME;
            case 4 -> _player._can_eat_ghosts_buff_time += MapData.BUFF_GRANT_TIME;
        }
    }

    public void set_draw_scale(double scale) {
        _draw_scale = scale;
    }
}
