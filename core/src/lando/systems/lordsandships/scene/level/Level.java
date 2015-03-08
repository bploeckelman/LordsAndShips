package lando.systems.lordsandships.scene.level;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.scene.tilemap.Tile;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 3/7/2015.
 */
public class Level {

    Array<Room> rooms;


    // TODO : pass in a desired level size (num rooms and level size)
    public Level() {
        // TODO : generate rooms this way...
        //Rectangle levelRect = new Rectangle(0, 0, levelWidth, levelHeight);
        //Array<Rectangle> bspRects = partitionRect(levelRect, numRooms);
        //Array<Room> rooms = generateRooms(bspRects);
        //connectNeighbors(rooms);

        Rectangle rect = new Rectangle(0, 0,
                                       Assets.rand.nextInt(40) + 10,
                                       Assets.rand.nextInt(40) + 10);
        rooms = new Array<Room>();
        for (int i = 0; i < 10; ++i) {
            rooms.add(generateRoom((int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height));
            rect.set(rect.x + rect.width * Tile.TILE_SIZE,
                     rect.y + rect.height * Tile.TILE_SIZE,
                     Assets.rand.nextInt(40) + 10,
                     Assets.rand.nextInt(40) + 10);
        }

        generateNeighbors();
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

            room2.addNeighbor(room1, x1, y1);
            room1.addNeighbor(room2, x2, y2);
        }
    }

    public void update(float delta) {

    }

    public void render(SpriteBatch batch, Camera camera) {
        for (Room room : rooms) {
            room.render(batch, camera);
        }
    }

    public void renderDebug(Camera camera) {
        Assets.shapes.setProjectionMatrix(camera.combined);
        Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
        Assets.shapes.setColor(Color.YELLOW);
        for (Room room : rooms) {
            Assets.shapes.rect(room.position.x,
                               room.position.y,
                               room.tiles[0].length * Tile.TILE_SIZE,
                               room.tiles.length    * Tile.TILE_SIZE);
        }
        Assets.shapes.end();
    }

    // -------------------------------------------------------------------------

    private Room generateRoom(int x, int y, int width, int height) {
        int min_tile_cols = 6;
        int min_tile_rows = 6;
        if (width <= min_tile_cols || height <= min_tile_rows) {
            throw new IllegalArgumentException(
                    "Room width and height must be greater than "
                    + min_tile_cols + ", " + min_tile_rows);
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

}
