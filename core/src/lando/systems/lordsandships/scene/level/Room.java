package lando.systems.lordsandships.scene.level;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.scene.level.objects.Light;
import lando.systems.lordsandships.scene.tilemap.Tile;
import lando.systems.lordsandships.scene.tilemap.TileSet;
import lando.systems.lordsandships.scene.tilemap.TileSetRaph;
import lando.systems.lordsandships.scene.tilemap.TileType;
import lando.systems.lordsandships.utils.Assets;
import org.w3c.dom.css.Rect;

import java.util.Random;

/**
 * Brian Ploeckelman created on 3/7/2015.
 */
public class Room {

    // Neighbor tile bit flags for adjacency calculations
    private static final int N  = 1 << 0;
    private static final int NE = 1 << 1;
    private static final int E  = 1 << 2;
    private static final int SE = 1 << 3;
    private static final int S  = 1 << 4;
    private static final int SW = 1 << 5;
    private static final int W  = 1 << 6;
    private static final int NW = 1 << 7;

    private static TextureRegion sconce_texture = null;

    boolean[][] walkable;
    int[][]     adjacency;
    Tile[][]    tiles;
    TileSet     tileSet;
    Rectangle   bounds;
    Light[]     lights;

    public Room(int posx, int posy, int width, int height) {
        if (Room.sconce_texture == null) {
            Room.sconce_texture = Assets.raphAtlas.findRegion("sExplosion", 1);
        }

        walkable = new boolean[height][width];
        adjacency = new int[height][width];
        tiles = new Tile[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                walkable[y][x] = false;
                adjacency[y][x] = 0;
                tiles[y][x] = new Tile(TileType.BLANK, x, y);
            }
        }
        tileSet = new TileSetRaph();
        bounds = new Rectangle(posx, posy, width * Tile.TILE_SIZE, height * Tile.TILE_SIZE);
        lights = new Light[Assets.rand.nextInt(10) + 1];
    }

    public static Room createEmpty(int posx, int posy, int width, int height) {
        int  lightIndex = 0;
        Room room = new Room(posx, posy, width, height);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                room.walkable[y][x] = (x != 0 && x != width - 1 && y != 0 && y != height - 1);
                room.adjacency[y][x] = 0;
                if      (x == 0 && y == 0)                   room.tiles[y][x].type = TileType.CORNER_OUTER_SW;
                else if (x == width - 1 && y == height - 1)  room.tiles[y][x].type = TileType.CORNER_OUTER_NE;
                else if (x == 0 && y == height - 1)          room.tiles[y][x].type = TileType.CORNER_OUTER_NW;
                else if (x == width - 1 && y == 0)           room.tiles[y][x].type = TileType.CORNER_OUTER_SE;
                else if (x == 0)                             room.tiles[y][x].type = TileType.WALL_VERT_W;
                else if (x == width - 1)                     room.tiles[y][x].type = TileType.WALL_VERT_E;
                else if (y == 0)                             room.tiles[y][x].type = TileType.WALL_HORIZ_S;
                else if (y == height - 1)                    room.tiles[y][x].type = TileType.WALL_HORIZ_N;
                else                                         room.tiles[y][x].type = TileType.FLOOR;

                if (Assets.rand.nextFloat() < 0.2f && lightIndex < room.lights.length) {
                    final Light light = new Light();
                    light.setPosition(x * Tile.TILE_SIZE, y * Tile.TILE_SIZE);
                    light.enable();
                    room.lights[lightIndex++] = light;
                }
            }
        }

        // chop some rectangles out of the main room
        int num_rects = 10;
        for (int i = 0; i < num_rects; ++i) {
            final Random r = Assets.rand;
            int xN = r.nextInt((width - 2) / 4) + 1;
            int yN = r.nextInt((height - 2) / 4) + 1;
            int maxx = width - (xN + 1);
            int maxy = height - (yN + 1);
            int x0 = r.nextInt(maxx - 1) + 1;
            int y0 = r.nextInt(maxy - 1) + 1;
            for (int y = y0; y < y0 + yN; ++y) {
                for (int x = x0; x < x0 + xN; ++x) {
                    room.walkable[y][x] = false;
                    room.tiles[y][x].type = TileType.BLOCK;
                }
            }
        }

        room.calculateAdjacency();
        room.generateTiles();
        return room;
    }

    public Rectangle bounds() {
        return bounds;
    }

    public int tilesWide() { return tiles[0].length; }
    public int tilesHigh() { return tiles.length; }

    public boolean walkable(int x, int y) {
        if (x < 0 || x >= walkable[0].length || y < 0 || y >= walkable.length) {
            throw new IllegalArgumentException("Invalid coordinate: " + x + ", " + y);
        }
        return walkable[y][x];
    }

    public Tile tile(int x, int y) {
        if (x < 0 || x >= tiles[0].length || y < 0 || y >= tiles.length) {
            throw new IllegalArgumentException("Invalid coordinate: " + x + ", " + y);
        }
        return tiles[y][x];
    }

    public void update(float delta) {
        for (Light light : lights) {
            light.update(delta);
        }
    }

    /**
     * Assumes batch has been configured, projection set, and has begun
     *
     * @param batch
     * @param camera
     */
    public void render(SpriteBatch batch, Camera camera) {
        Tile tile;
        int width  = tiles[0].length;
        int height = tiles.length;
        Assets.font.setScale(0.25f);
//        BitmapFont.TextBounds textBounds;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                // Draw current tile
                tile = tiles[y][x];
                float px = tile.getWorldMinX() + bounds.x;
                float py = tile.getWorldMinY() + bounds.y;
//                if (walkable[y][x]) batch.setColor(Color.GREEN);
//                else                batch.setColor(Color.RED);
                batch.draw(tileSet.getTexture(tile.type), px, py, Tile.TILE_SIZE, Tile.TILE_SIZE);

                // Draw adjacency score for current tile
//                String adj = ""+adjacency[y][x];
//                textBounds = Assets.font.getBounds(adj);
//                float hw = textBounds.width  / 2;
//                float hh = textBounds.height / 2;
//                float hs = Tile.TILE_SIZE / 2;
//                batch.setColor(Color.WHITE);
//                Assets.font.draw(batch, adj, px + hs - hw, py + hs + hh);
            }
        }

        for (Light light : lights) {
            final TextureRegion texture = (light.getCurrentFrame() != null) ? light.getCurrentFrame() : sconce_texture;
            batch.draw(texture,
                       light.getPosition().x - texture.getRegionWidth()  / 2f,
                       light.getPosition().y - texture.getRegionHeight() / 2f,
                       texture.getRegionWidth(),
                       texture.getRegionHeight());
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Set bit flags in adjacency matrix based on which neighbors are walkable
     *   [ 1<<7 ] [ 1<<0 ] [ 1<<1 ]
     *   [ 1<<6 ] [  --  ] [ 1<<2 ]
     *   [ 1<<5 ] [ 1<<4 ] [ 1<<3 ]
     * These bit flags are later used for tile selection
     */
    public void calculateAdjacency() {
        int max_row = tiles.length;
        int max_col = tiles[0].length;

        for (int iy = 0; iy < tiles.length; ++iy) {
            for (int ix = 0; ix < tiles[0].length; ++ix) {
                adjacency[iy][ix] = 0;

                // Set adjacency flags based on walkability of neighbor tiles,
                // ignoring tiles that would be off the edge of the map
                // by validating neighbor indices before setting flags
                if ((iy + 1) != max_row                        && walkable[iy+1][ix  ]) adjacency[iy][ix] |= N;
                if ((ix + 1) != max_col && (iy + 1) != max_row && walkable[iy+1][ix+1]) adjacency[iy][ix] |= NE;
                if ((ix + 1) != max_col                        && walkable[iy  ][ix+1]) adjacency[iy][ix] |= E;
                if ((ix + 1) != max_col && (iy - 1) != -1      && walkable[iy-1][ix+1]) adjacency[iy][ix] |= SE;
                if ((iy - 1) != -1                             && walkable[iy-1][ix  ]) adjacency[iy][ix] |= S;
                if ((ix - 1) != -1      && (iy - 1) != -1      && walkable[iy-1][ix-1]) adjacency[iy][ix] |= SW;
                if ((ix - 1) != -1                             && walkable[iy  ][ix-1]) adjacency[iy][ix] |= W;
                if ((ix - 1) != -1      && (iy + 1) != max_row && walkable[iy+1][ix-1]) adjacency[iy][ix] |= NW;
            }
        }
    }

    public void generateTiles() {
        int  width  = tiles[0].length;
        int  height = tiles.length;
        int  adj;
        Tile tile;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                tile = tiles[y][x];
                adj = adjacency[y][x];

                if (walkable[y][x]) {
                    tile.type = TileType.FLOOR;
                    continue;
                }

                if (adj == 0) tile.type = TileType.BLANK;
                // Check for wall tiles
                else if ((adj & N) == N && (adj & S) != S && (adj & E) != E && (adj & W) != W) tile.type = TileType.WALL_HORIZ_S;
                else if ((adj & N) != N && (adj & S) == S && (adj & E) != E && (adj & W) != W) tile.type = TileType.WALL_HORIZ_N;
                else if ((adj & N) != N && (adj & S) != S && (adj & E) == E && (adj & W) != W) tile.type = TileType.WALL_VERT_W;
                else if ((adj & N) != N && (adj & S) != S && (adj & E) != E && (adj & W) == W) tile.type = TileType.WALL_VERT_E;
                // Check for outer corner tiles
                else if ((adj & S) != S && (adj & E) != E && (adj & SE) == SE) tile.type = TileType.CORNER_OUTER_NW;
                else if ((adj & S) != S && (adj & W) != W && (adj & SW) == SW) tile.type = TileType.CORNER_OUTER_NE;
                else if ((adj & N) != N && (adj & E) != E && (adj & NE) == NE) tile.type = TileType.CORNER_OUTER_SW;
                else if ((adj & N) != N && (adj & W) != W && (adj & NW) == NW) tile.type = TileType.CORNER_OUTER_SE;
                // Check for inner corner tiles
                else if ((adj & N) != N && (adj & W) != W && (adj & S) == S && (adj & E) == E) tile.type = TileType.CORNER_INNER_NW;
                else if ((adj & N) != N && (adj & W) == W && (adj & S) == S && (adj & E) != E) tile.type = TileType.CORNER_INNER_NE;
                else if ((adj & N) == N && (adj & W) != W && (adj & S) != S && (adj & E) == E) tile.type = TileType.CORNER_INNER_SW;
                else if ((adj & N) == N && (adj & W) == W && (adj & S) != S && (adj & E) != E) tile.type = TileType.CORNER_INNER_SE;
                // Check for things we don't have tiles for
                else tile.type = TileType.BLOCK;
            }
        }
    }

    public Light[] getLights() {
        return lights;
    }
}
