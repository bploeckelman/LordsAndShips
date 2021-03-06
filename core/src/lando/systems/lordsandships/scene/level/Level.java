package lando.systems.lordsandships.scene.level;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.lordsandships.scene.level.objects.Light;
import lando.systems.lordsandships.scene.tilemap.Tile;
import lando.systems.lordsandships.scene.tilemap.TileType;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

import java.util.LinkedList;

/**
 * Brian Ploeckelman created on 3/7/2015.
 */
public class Level {

    // Default level is one room, roughly the same size as the game window
    private static final int default_level_width  = Constants.win_width;
    private static final int default_level_height = Constants.win_height;
    private static final int default_level_depth  = 0;

    BSP                   bsp;
    Leaf                  leaf;
    Rectangle             bounds;
    Array<Room>           rooms;
    ObjectMap<Leaf, Leaf> neighbors;

    public boolean renderAllRooms = false;

    public Level() {
        this(default_level_width, default_level_height, default_level_depth);
    }

    public Level(int width, int height, int depth) {
        bounds = new Rectangle(0, 0, width, height);
        bsp = new BSP(bounds, depth);
        rooms = generateRooms(bsp);
        neighbors = connectNeighbors(bsp);
    }

    public Leaf occupied() {
        return leaf;
    }

    Array<Leaf> nextLeaves = new Array<Leaf>();

    public void nextRoom() {
        if (nextLeaves.size == 0) {
            nextLeaves = bsp.getLeaves();
        }
        leaf = nextLeaves.pop();
    }

    public Rectangle getNextRoomBounds() {
        if (nextLeaves.size == 0) {
            nextLeaves = bsp.getLeaves();
        }
        if (nextLeaves.size != 0) return nextLeaves.peek().bounds;
        else                      return null;
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    public void update(float delta) {
        if (occupied() != null && occupied().room() != null) {
            occupied().room().update(delta);
        }
    }

    public void render(SpriteBatch batch, Camera camera) {
        if (renderAllRooms) {
            for (Room room : rooms) {
                room.render(batch, camera);
            }
        } else {
            if (leaf.room != null) {
                leaf.room.render(batch, camera);
            }
        }
//        batch.end();
//        renderDebug(camera);
//        batch.setProjectionMatrix(camera.combined);
//        batch.begin();
    }

    final LinkedList<Leaf> leaves  = new LinkedList<Leaf>();
    final Vector2 center1 = new Vector2();
    final Vector2 center2 = new Vector2();
    final boolean room_outlines = false;
    final boolean leaf_outlines = false;
    final boolean leaf_connects = true;

    public void renderDebug(Camera camera) {
        Assets.shapes.setProjectionMatrix(camera.combined);
        Assets.shapes.begin(ShapeRenderer.ShapeType.Line);

        // Draw room outlines
        if (room_outlines) {
            Assets.shapes.setColor(Color.YELLOW);
            for (Leaf leaf : bsp.getLeaves()) {
                if (leaf.room != null)
                    Assets.renderRect(leaf.room.bounds);
            }
        }

        // Draw bsp outlines
        if (leaf_outlines) {
            Assets.shapes.setColor(Color.RED);
            leaves.clear();
            leaves.addFirst(bsp.root);
            while (!leaves.isEmpty()) {
                Leaf leaf = leaves.removeFirst();
                if (leaf.child1 != null) leaves.addFirst(leaf.child1);
                if (leaf.child2 != null) leaves.addFirst(leaf.child2);
                Assets.renderRect(leaf.bounds);
            }
        }

        // Draw neighbor connections
        if (leaf_connects) {
            for (ObjectMap.Entry<Leaf, Leaf> entry : neighbors.entries()) {
                entry.key.bounds.getCenter(center1);
                entry.value.bounds.getCenter(center2);
                if      (entry.key.level == 1) Assets.shapes.setColor(0.0f, 0.0f, 0.0f, 1f);
                else if (entry.key.level == 2) Assets.shapes.setColor(0.2f, 0.2f, 0.2f, 1f);
                else if (entry.key.level == 3) Assets.shapes.setColor(0.4f, 0.4f, 0.4f, 1f);
                else if (entry.key.level == 4) Assets.shapes.setColor(0.6f, 0.6f, 0.6f, 1f);
                else if (entry.key.level == 5) Assets.shapes.setColor(0.8f, 0.8f, 0.8f, 1f);
                else if (entry.key.level == 6) Assets.shapes.setColor(1.0f, 1.0f, 0.0f, 1f);
                Assets.shapes.line(center1.x, center1.y, center2.x, center2.y);
            }
        }

        // Draw level outline
        final float margin = 5;
        Assets.shapes.setColor(Color.MAGENTA);
        Assets.shapes.rect(bounds.x - margin, bounds.y - margin, bounds.width + 2 * margin, bounds.height + 2 * margin);

        Assets.shapes.end();
    }

    // -------------------------------------------------------------------------
    // Implementation Methods
    // -------------------------------------------------------------------------

    private Array<Room> generateRooms(BSP bsp) {
        Array<Room> rooms = new Array<Room>();

        for (Leaf leaf : bsp.getLeaves()) {
//            rooms.add(generateEmptyRoom(leaf));
            Room room = generateRoom(leaf);
            int lightIndex = 0;

            // add enclosing walls temporarily
            int width  = room.tilesWide();
            int height = room.tilesHigh();
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    boolean madeWall = false;

                    if      (x == 0 && y == 0)                   { room.tiles[y][x].type = TileType.CORNER_OUTER_SW; madeWall = true; }
                    else if (x == width - 1 && y == height - 1)  { room.tiles[y][x].type = TileType.CORNER_OUTER_NE; madeWall = true; }
                    else if (x == 0 && y == height - 1)          { room.tiles[y][x].type = TileType.CORNER_OUTER_NW; madeWall = true; }
                    else if (x == width - 1 && y == 0)           { room.tiles[y][x].type = TileType.CORNER_OUTER_SE; madeWall = true; }
                    else if (x == 0)                             { room.tiles[y][x].type = TileType.WALL_VERT_W;     madeWall = true; }
                    else if (x == width - 1)                     { room.tiles[y][x].type = TileType.WALL_VERT_E;     madeWall = true; }
                    else if (y == 0)                             { room.tiles[y][x].type = TileType.WALL_HORIZ_S;    madeWall = true; }
                    else if (y == height - 1)                    { room.tiles[y][x].type = TileType.WALL_HORIZ_N;    madeWall = true; }

                    if (madeWall) {
                        room.walkable[y][x] = false;
                    }
                }
            }

            for (int i = 0; i < room.lights.length; ++i) {
                boolean isFloorTile = false;
                float x = 0f, y = 0f;
                while (!isFloorTile) {
                    int tilex = Assets.rand.nextInt(width);
                    int tiley = Assets.rand.nextInt(height);
                    if (room.tile(tilex, tiley).type.equals(TileType.FLOOR)) {
                        isFloorTile = true;
                    }
                    x = room.bounds.x + tilex * Tile.TILE_SIZE + Tile.TILE_SIZE / 2f;
                    y = room.bounds.y + tiley * Tile.TILE_SIZE + Tile.TILE_SIZE / 2f;
                }

                final Light light = new Light();
                light.setPosition(x, y);
                light.setColor(Assets.rand.nextFloat(),
                               Assets.rand.nextFloat(),
                               Assets.rand.nextFloat(),
                               Assets.rand.nextFloat());
                final Animation anim = new Animation(0.1f, Assets.brazierFrames);
                anim.setPlayMode(Animation.PlayMode.LOOP);
                light.setCustomAnimation(anim);
                light.enable();
                room.lights[i] = light;
            }

            rooms.add(room);
            if (this.leaf == null)
                this.leaf = leaf;
        }

        return rooms;
    }

    private Room generateEmptyRoom(Leaf leaf) {
        int x      = (int)  leaf.bounds.x;
        int y      = (int)  leaf.bounds.y;
        int width  = (int) (leaf.bounds.width  / Tile.TILE_SIZE);
        int height = (int) (leaf.bounds.height / Tile.TILE_SIZE);

        Room room = Room.createEmpty(x, y, width, height);
        leaf.room = room;
        return room;
    }

    private Room generateRoom(Leaf leaf) {
        int x      = (int)  leaf.bounds.x;
        int y      = (int)  leaf.bounds.y;
        int width  = (int) (leaf.bounds.width  / Tile.TILE_SIZE);
        int height = (int) (leaf.bounds.height / Tile.TILE_SIZE);

        int min_tile_cols = 6;
        int min_tile_rows = 6;
        if (width <= min_tile_cols || height <= min_tile_rows) {
            throw new IllegalArgumentException(
                    "Room width and height must be greater than " + min_tile_cols + ", " + min_tile_rows);
        }

        Room room = new Room(x, y, width, height);

        // Store the room in the specified leaf
        leaf.room = room;

        // Create a bunch of random walkability regions
        int num_iterations = 10;
        for (int i = 0; i < num_iterations; ++i) {
            // TODO : try diminishing rectangle dimensions on each iteration
            int x0 = Assets.rand.nextInt(width / 4) + 1;
            int y0 = Assets.rand.nextInt(height / 4) + 1;
            int x1 = x0 + Assets.rand.nextInt(width  - x0 - 1) + 2;
            int y1 = y0 + Assets.rand.nextInt(height - y0 - 1) + 2;

            boolean walkable = true;
            if (Assets.rand.nextFloat() < 0.2f) {
                walkable = false;
            }

            for (int ix = x0; ix < x1; ++ix) {
                for (int iy = y0; iy < y1; ++iy) {
                    room.walkable[iy][ix] = walkable;
                }
            }
        }

        // Set adjacency scores based on walkability states
        room.calculateAdjacency();

        // Set tile types based on adjacency values
        room.generateTiles();

        return room;
    }

    private ObjectMap<Leaf, Leaf> connectNeighbors(BSP bsp) {
        ObjectMap<Leaf, Leaf> neighbors = new ObjectMap<Leaf, Leaf>();

        Array<Leaf> queue;
        Leaf[]      level;
        for (int i = bsp.depth; i > 0; --i) {
            queue = new Array<Leaf>();
            bsp.getLevel(i, queue);
            level = queue.toArray(Leaf.class);
            //Gdx.app.log("LEAVES", "" + queue.size + " leaves at depth " + i);
            for (Leaf leaf1 : level) {
                for (Leaf leaf2 : level) {
                    if (leaf1 == leaf2) continue;
                    if (leaf1.parent == leaf2.parent) {
                        neighbors.put(leaf1, leaf2);
                    }
                }
            }
        }

        return neighbors;
    }

}
