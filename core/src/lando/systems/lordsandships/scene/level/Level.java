package lando.systems.lordsandships.scene.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.lordsandships.scene.tilemap.Tile;
import lando.systems.lordsandships.utils.Assets;

import java.util.LinkedList;

/**
 * Brian Ploeckelman created on 3/7/2015.
 */
public class Level {

    RectBSP                       bsp;
    Rectangle                     bounds;
    Room                          room;
    Array<Room>                   rooms;
    RectBSP.Leaf                  occupiedLeaf;
    ObjectMap<RectBSP.Leaf, Room> leafRoomMap;
    ObjectMap<RectBSP.Leaf, RectBSP.Leaf> neighbors;

    public Level() {
        // TODO : pass in a desired level size (num rooms and level size)
        final int level_width  = 500 * Tile.TILE_SIZE;
        final int level_height = 500 * Tile.TILE_SIZE;
        final int level_depth  = 5;

        bounds = new Rectangle(0, 0, level_width, level_height);
        bsp    = new RectBSP(bounds, level_depth);
        rooms  = generateRooms(bsp);
        neighbors = connectNeighbors(bsp);
    }

    private ObjectMap<RectBSP.Leaf, RectBSP.Leaf> connectNeighbors(RectBSP bsp) {
        ObjectMap<RectBSP.Leaf, RectBSP.Leaf> neighbors = new ObjectMap<RectBSP.Leaf, RectBSP.Leaf>();

        Array<RectBSP.Leaf> queue;
        RectBSP.Leaf[]      level;
        for (int i = bsp.depth; i > 0; --i) {
            queue = new Array<RectBSP.Leaf>();
            bsp.getLevel(i, queue);
            level = queue.toArray(RectBSP.Leaf.class);
            Gdx.app.log("LEAVES", "" + queue.size + " leaves at depth " + i);
            for (RectBSP.Leaf leaf1 : level) {
                for (RectBSP.Leaf leaf2 : level) {
                    if (leaf1 == leaf2) continue;
                    if (leaf1.parent == leaf2.parent) {
                        neighbors.put(leaf1, leaf2);
                    }
                }
            }
        }

        return neighbors;
    }

    public Rectangle getOccupiedRoomBounds() {
        return occupiedLeaf.rect;
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    public void update(float delta) {
        room = leafRoomMap.get(occupiedLeaf);
    }

    public void render(SpriteBatch batch, Camera camera) {
        room.render(batch, camera);
        renderDebug(camera);
    }

    LinkedList<RectBSP.Leaf> leaves = new LinkedList<RectBSP.Leaf>();
    Vector2 center1 = new Vector2();
    Vector2 center2 = new Vector2();


    public void renderDebug(Camera camera) {
        final boolean room_outlines = false;
        final boolean leaf_outlines = false;
        final boolean leaf_connects = true;

        Assets.shapes.setProjectionMatrix(camera.combined);
        Assets.shapes.begin(ShapeRenderer.ShapeType.Line);

        // Draw room outlines
        if (room_outlines) {
            Assets.shapes.setColor(Color.YELLOW);
            for (Room room : rooms) {
                Assets.shapes.rect(room.bounds.x, room.bounds.y, room.bounds.width, room.bounds.height);
            }
        }

        // Draw bsp outlines
        if (leaf_outlines) {
            Assets.shapes.setColor(Color.RED);
            leaves.clear();
            leaves.push(bsp.root);
            RectBSP.Leaf leaf;
            while (!leaves.isEmpty()) {
                leaf = leaves.pop();
                if (leaf.child1 != null) leaves.push(leaf.child1);
                if (leaf.child2 != null) leaves.push(leaf.child2);
                Assets.shapes.rect(leaf.rect.x, leaf.rect.y, leaf.rect.width, leaf.rect.height);
            }
        }

        // Draw neighbor connections
        if (leaf_connects) {
            for (ObjectMap.Entry<RectBSP.Leaf, RectBSP.Leaf> entry : neighbors.entries()) {
                entry.key.rect.getCenter(center1);
                entry.value.rect.getCenter(center2);
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

    private Array<Room> generateRooms(RectBSP bsp) {
        leafRoomMap = new ObjectMap<RectBSP.Leaf, Room>();

        Array<Room> rooms = new Array<Room>();

        for (RectBSP.Leaf leaf : bsp.getLeaves()) {
            Room newRoom = generateRoom(leaf);
            leafRoomMap.put(leaf, newRoom);
            rooms.add(newRoom);

            if (occupiedLeaf == null) occupiedLeaf = leaf;
            if (room == null) room = newRoom;
        }

        return rooms;
    }

    private Room generateRoom(RectBSP.Leaf leaf) {
        int x      = (int)  leaf.rect.x;
        int y      = (int)  leaf.rect.y;
        int width  = (int) (leaf.rect.width  / Tile.TILE_SIZE);
        int height = (int) (leaf.rect.height / Tile.TILE_SIZE);

        int min_tile_cols = 6;
        int min_tile_rows = 6;
        if (width <= min_tile_cols || height <= min_tile_rows) {
            throw new IllegalArgumentException(
                    "Room width and height must be greater than " + min_tile_cols + ", " + min_tile_rows);
        }

        Room room = new Room(x, y, width, height);

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

    private void generateNeighbors() {
        int num_neighbors = Assets.rand.nextInt(rooms.size - 1) + 1;
        for (int i = 0; i < num_neighbors; ++i) {
            int j = Assets.rand.nextInt(rooms.size);
            int k = Assets.rand.nextInt(rooms.size);
            if (j == k) { j = 0; }

            Room room1 = rooms.get(j);
            Room room2 = rooms.get(k);

            // TODO : pick a room edge, find two tiles closest to that edge
            // TODO : set tiles to door tiles, add connectivity info
            int x1 = 0;
            int y1 = 0;
            int x2 = 0;
            int y2 = 0;

//            room2.addNeighbor(room1, x1, y1);
//            room1.addNeighbor(room2, x2, y2);
        }
    }

    Array<RectBSP.Leaf> nextLeaves = new Array<RectBSP.Leaf>();
    public void nextRoom() {
        if (nextLeaves.size == 0) {
            nextLeaves = bsp.getLeaves();
        }
        occupiedLeaf = nextLeaves.pop();
    }
    public Rectangle getNextRoomBounds() {
        if (nextLeaves.size == 0) {
            nextLeaves = bsp.getLeaves();
        }
        return nextLeaves.peek().rect;
    }

    // -------------------------------------------------------------------------
    // EXTRACT US
    // -------------------------------------------------------------------------

    class RectBSP {

        class Leaf {
            Rectangle rect;
            Leaf      parent, child1, child2;
            int       level;

            public Leaf(Leaf parent, Rectangle rect) {
                this.parent = parent;
                this.rect   = rect;
                this.child1 = null;
                this.child2 = null;
                this.level  = (parent == null) ? 1 : parent.level + 1;
            }

            public Array<Leaf> getLeaves() {
                Array<Leaf> leaves = new Array<Leaf>();
                if (child1 == null && child2 == null) {
                    leaves.add(this);
                } else {
                    leaves.addAll(child1.getLeaves());
                    leaves.addAll(child2.getLeaves());
                }

                return leaves;
            }

            public Array<Leaf> getLevel(int i, Array<Leaf> queue) {
                if (queue == null) {
                    queue = new Array<Leaf>();
                }
                if (i == 1) {
                    queue.add(this);
                } else {
                    if (child1 != null) child1.getLevel(i - 1, queue);
                    if (child2 != null) child2.getLevel(i - 1, queue);
                }
                return queue;
            }

        }

        Leaf root;
        int  depth;

        final boolean discard_by_ratio   = true;
        final float   split_ratio_height = 0.45f;
        final float   split_ratio_width  = 0.45f;

        public RectBSP(Rectangle rootRect, int depth) {
            root = new Leaf(null, rootRect);
            partition(root, depth);

            RectBSP.Leaf leaf = root;
            while (leaf.child1 != null) {
                leaf = leaf.child1;
            }
            this.depth = leaf.level;
            Gdx.app.log("DEPTH", "" + this.depth);
        }

        public Array<Leaf> getLeaves() {
            return root.getLeaves();
        }

        public Array<Leaf> getLevel(int i, Array<Leaf> queue) {
            return root.getLevel(i, queue);
        }

        private Leaf partition(Leaf leaf, int iteration) {
            if (iteration != 0) {
                //Gdx.app.log("PARTITION", "iter(" + iteration + "); partitioning leaf " + leaf.rect.toString());
                Leaf[] children = splitLeaf(leaf);
                leaf.child1 = partition(children[0], iteration - 1);
                leaf.child2 = partition(children[1], iteration - 1);
            }
            return leaf;
        }

        private Leaf[] splitLeaf(Leaf leaf) {
            Leaf[] children = new Leaf[2];
            if (leaf == null || leaf.rect == null) {
                return children;
            }

            Rectangle[] rects = new Rectangle[2];

            if (Assets.rand.nextBoolean()) {
                // Split vertical
                int n = (int) leaf.rect.height;
                float split_size = Assets.rand.nextInt(n) + 1;
                //Gdx.app.log("SPLIT_LEAF", "\tvertical split: " + split_size + " for n(" + n + ")");

                rects[0] = new Rectangle(leaf.rect.x, leaf.rect.y, leaf.rect.width, split_size);
                rects[1] = new Rectangle(leaf.rect.x,
                                         leaf.rect.y + rects[0].height,
                                         leaf.rect.width,
                                         leaf.rect.height - rects[0].height);

                if (discard_by_ratio) {
                    float rect0_ratio_h = rects[0].height / rects[0].width;
                    float rect1_ratio_h = rects[1].height / rects[1].width;
                    if (rect0_ratio_h < split_ratio_height || rect1_ratio_h < split_ratio_height) {
                        //Gdx.app.log("DISCARD", "discarding split ratio: 0->" + rect0_ratio_h + ", 1->" + rect1_ratio_h);
                        return splitLeaf(leaf);
                    } else {
                        //Gdx.app.log("ACCEPT", "accepting split ratio: 0->" + rect0_ratio_h + ", 1->" + rect1_ratio_h);
                    }
                }
            } else {
                // Split horizontal
                int n = (int) leaf.rect.width;
                float split_size = Assets.rand.nextInt(n);
                //Gdx.app.log("SPLIT_LEAF", "\thorizontal split: " + split_size + " for n(" + n + ")");

                rects[0] = new Rectangle(leaf.rect.x, leaf.rect.y, split_size, leaf.rect.height);
                rects[1] = new Rectangle(leaf.rect.x + rects[0].width,
                                         leaf.rect.y,
                                         leaf.rect.width - rects[0].width,
                                         leaf.rect.height);


                if (discard_by_ratio) {
                    float rect0_ratio_w = rects[0].width / rects[0].height;
                    float rect1_ratio_w = rects[1].width / rects[1].height;
                    if (rect0_ratio_w < split_ratio_width || rect1_ratio_w < split_ratio_width) {
                        //Gdx.app.log("DISCARD", "discarding split ratio: 0->" + rect0_ratio_w + ", 1->" + rect1_ratio_w);
                        return splitLeaf(leaf);
                    } else {
                        //Gdx.app.log("ACCEPT", "accepting split ratio: 0->" + rect0_ratio_w + ", 1->" + rect1_ratio_w);
                    }
                }
            }

            children[0] = new Leaf(leaf, rects[0]);
            children[1] = new Leaf(leaf, rects[1]);

            return children;
        }
    }

}
